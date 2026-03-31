from __future__ import annotations

import logging
import secrets
from dataclasses import replace
from datetime import datetime, timedelta, timezone

from app.models.verification import (
    TelegramLinkResult,
    VerificationCodeSubmission,
    VerificationRequest,
    VerificationResult,
    VerificationSessionRecord,
    VerificationStartResponse,
    VerificationStatus,
)
from app.services.code_service import VerificationCodeService
from app.services.rate_limit import InMemoryRateLimiter
from app.services.session_repository import SupabaseVerificationRepository

logger = logging.getLogger(__name__)


class VerificationError(RuntimeError):
    pass


class VerificationService:
    def __init__(
        self,
        repository: SupabaseVerificationRepository,
        rate_limiter: InMemoryRateLimiter,
        code_service: VerificationCodeService,
        ttl_seconds: int,
        resend_cooldown_seconds: int,
        telegram_bot_username: str,
    ) -> None:
        self._repository = repository
        self._rate_limiter = rate_limiter
        self._code_service = code_service
        self._ttl_seconds = ttl_seconds
        self._resend_cooldown_seconds = resend_cooldown_seconds
        self._telegram_bot_username = telegram_bot_username

    def request_verification(self, request: VerificationRequest) -> VerificationStartResponse:
        normalized_phone = self._normalize_phone(request.phone_number)
        self._rate_limiter.consume(f"request:{normalized_phone}")

        now = datetime.now(timezone.utc)
        session_id = secrets.token_hex(16)
        raw_code = self._code_service.generate_code()
        session = VerificationSessionRecord(
            id=session_id,
            phone_number=normalized_phone,
            status=VerificationStatus.PENDING_LINK,
            code_hash=self._code_service.hash_code(session_id, raw_code),
            telegram_start_token=secrets.token_urlsafe(24),
            created_at=now,
            expires_at=now + timedelta(seconds=self._ttl_seconds),
            resend_available_at=now + timedelta(seconds=self._resend_cooldown_seconds),
        )
        self._repository.create(session)
        logger.info("verification_requested", extra={"phone_number": normalized_phone, "session_id": session_id})
        return VerificationStartResponse(
            session_id=session.id,
            telegram_start_url=f"https://t.me/{self._telegram_bot_username}?start={session.telegram_start_token}",
            expires_in_seconds=self._ttl_seconds,
            resend_in_seconds=self._resend_cooldown_seconds,
        )

    def resend_verification(self, session_id: str) -> TelegramLinkResult:
        session = self._require_session(session_id)
        self._ensure_not_expired(session)
        now = datetime.now(timezone.utc)
        if session.resend_available_at > now:
            raise VerificationError("resend_cooldown_active")

        new_code = self._code_service.generate_code()
        updated = session.model_copy(
            update={
                "code_hash": self._code_service.hash_code(session.id, new_code),
                "resend_available_at": now + timedelta(seconds=self._resend_cooldown_seconds),
                "delivery_count": session.delivery_count + 1,
                "status": VerificationStatus.CODE_SENT,
            },
        )
        self._repository.update(updated)
        return TelegramLinkResult(
            session_id=updated.id,
            phone_number_masked=self._mask_phone(updated.phone_number),
            code=new_code,
            telegram_user_id=updated.telegram_user_id,
        )

    def link_telegram_user(self, start_token: str, telegram_user_id: str, telegram_username: str | None) -> TelegramLinkResult:
        session = self._repository.find_by_start_token(start_token)
        if session is None:
            raise VerificationError("verification_session_not_found")
        self._ensure_not_expired(session)

        raw_code = self._code_service.generate_code()
        updated = session.model_copy(
            update={
                "telegram_user_id": telegram_user_id,
                "telegram_username": telegram_username,
                "code_hash": self._code_service.hash_code(session.id, raw_code),
                "status": VerificationStatus.CODE_SENT,
                "delivery_count": session.delivery_count + 1,
            },
        )
        self._repository.update(updated)
        logger.info("telegram_session_linked", extra={"session_id": session.id, "telegram_user_id": telegram_user_id})
        return TelegramLinkResult(
            session_id=updated.id,
            phone_number_masked=self._mask_phone(updated.phone_number),
            code=raw_code,
            telegram_user_id=updated.telegram_user_id,
        )

    def verify_code(self, submission: VerificationCodeSubmission) -> VerificationResult:
        session = self._require_session(submission.session_id)
        self._ensure_not_expired(session)
        normalized_phone = self._normalize_phone(submission.phone_number)
        if session.phone_number != normalized_phone:
            raise VerificationError("phone_number_mismatch")

        valid = self._code_service.verify(session.id, submission.code, session.code_hash)
        if not valid:
            updated = session.model_copy(update={"attempt_count": session.attempt_count + 1, "last_error": "invalid_code"})
            self._repository.update(updated)
            raise VerificationError("invalid_code")

        verified = session.model_copy(
            update={
                "status": VerificationStatus.VERIFIED,
                "verified_at": datetime.now(timezone.utc),
            },
        )
        self._repository.update(verified)
        logger.info("verification_completed", extra={"session_id": session.id})
        return VerificationResult(verified=True, status=VerificationStatus.VERIFIED)

    def _require_session(self, session_id: str) -> VerificationSessionRecord:
        session = self._repository.find_by_id(session_id)
        if session is None:
            raise VerificationError("verification_session_not_found")
        return session

    def _ensure_not_expired(self, session: VerificationSessionRecord) -> None:
        if session.expires_at <= datetime.now(timezone.utc):
            expired = session.model_copy(update={"status": VerificationStatus.EXPIRED, "last_error": "expired"})
            self._repository.update(expired)
            raise VerificationError("verification_session_expired")

    def _normalize_phone(self, phone_number: str) -> str:
        digits = "".join(char for char in phone_number if char.isdigit() or char == "+").strip()
        if len([char for char in digits if char.isdigit()]) < 10:
            raise VerificationError("invalid_phone_number")
        return digits if digits.startswith("+") else f"+{digits}"

    def _mask_phone(self, phone_number: str) -> str:
        visible = phone_number[-2:]
        return f"{phone_number[:3]}***{visible}"
