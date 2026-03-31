from __future__ import annotations

from fastapi import FastAPI, HTTPException

from app.bot.provider import TelegramVerificationBot
from app.models.verification import (
    VerificationCodeSubmission,
    VerificationRequest,
    VerificationResendRequest,
    VerificationResult,
    VerificationStartResponse,
)
from app.services.verification_service import VerificationError, VerificationService


def build_api(verification_service: VerificationService, bot_provider: TelegramVerificationBot) -> FastAPI:
    app = FastAPI(title="Dot Chat Telegram Verification Service", version="0.1.0")

    @app.get("/health")
    async def health() -> dict[str, str]:
        return {"status": "ok"}

    @app.post("/verification/request", response_model=VerificationStartResponse)
    async def request_verification(payload: VerificationRequest) -> VerificationStartResponse:
        try:
            return verification_service.request_verification(payload)
        except VerificationError as error:
            raise HTTPException(status_code=400, detail=str(error)) from error

    @app.post("/verification/resend")
    async def resend_verification(payload: VerificationResendRequest) -> dict[str, str]:
        try:
            result = verification_service.resend_verification(payload.session_id)
            if bot_provider.is_enabled and result.telegram_user_id:
                await bot_provider.send_code(
                    telegram_user_id=result.telegram_user_id,
                    session_id=result.session_id,
                    phone_masked=result.phone_number_masked,
                    code=result.code,
                )
            return {"status": "resent"}
        except VerificationError as error:
            raise HTTPException(status_code=400, detail=str(error)) from error

    @app.post("/verification/verify", response_model=VerificationResult)
    async def verify_code(payload: VerificationCodeSubmission) -> VerificationResult:
        try:
            return verification_service.verify_code(payload)
        except VerificationError as error:
            raise HTTPException(status_code=400, detail=str(error)) from error

    return app
