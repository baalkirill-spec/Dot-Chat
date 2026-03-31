from enum import Enum

from pydantic import BaseModel, Field


class AdminRole(str, Enum):
    SUPER_ADMIN = "super_admin"
    MODERATOR = "moderator"
    SUPPORT_ADMIN = "support_admin"


class AccountStatus(str, Enum):
    ACTIVE = "active"
    BANNED = "banned"
    DEACTIVATED = "deactivated"
    PENDING_DELETION = "pending_deletion"


class ManagedAccount(BaseModel):
    user_id: str = Field(alias="id")
    username: str | None = None
    display_name: str | None = None
    phone_number: str | None = None
    avatar_url: str | None = None
    bio: str | None = None
    account_status: AccountStatus = AccountStatus.ACTIVE
    deleted_at: str | None = None
    deactivated_at: str | None = None


class ModerationActionRequest(BaseModel):
    reason: str | None = None


class AdminActor(BaseModel):
    subject: str
    role: AdminRole
