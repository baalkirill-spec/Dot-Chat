from __future__ import annotations

from datetime import datetime
from enum import Enum
from typing import Optional
from uuid import uuid4

from pydantic import BaseModel, Field


class VerificationChannel(str, Enum):
    TELEGRAM_BOT = "telegram_bot"


class VerificationStatus(str, Enum):
    PENDING_LINK = "pending_link"
    CODE_SENT = "code_sent"
    VERIFIED = "verified"
    EXPIRED = "expired"
    CANCELLED = "cancelled"


class VerificationSessionRecord(BaseModel):
    id: str = Field(default_factory=lambda: str(uuid4()))
    phone_number: str
    channel: VerificationChannel = VerificationChannel.TELEGRAM_BOT
    status: VerificationStatus
    code_hash: str
    telegram_start_token: str
    telegram_user_id: Optional[str] = None
    telegram_username: Optional[str] = None
    attempt_count: int = 0
    delivery_count: int = 0
    created_at: datetime
    expires_at: datetime
    resend_available_at: datetime
    verified_at: Optional[datetime] = None
    last_error: Optional[str] = None


class VerificationRequest(BaseModel):
    phone_number: str


class VerificationStartResponse(BaseModel):
    session_id: str
    telegram_start_url: str
    expires_in_seconds: int
    resend_in_seconds: int


class VerificationResendRequest(BaseModel):
    session_id: str


class VerificationCodeSubmission(BaseModel):
    session_id: str
    phone_number: str
    code: str


class VerificationResult(BaseModel):
    verified: bool
    status: VerificationStatus


class TelegramLinkResult(BaseModel):
    session_id: str
    phone_number_masked: str
    code: str
    telegram_user_id: Optional[str] = None
