from fastapi import Depends, Header, HTTPException, status

from app.config import settings
from app.models import AdminActor, AdminRole


def require_admin(authorization: str | None = Header(default=None)) -> AdminActor:
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="missing_admin_token")

    token = authorization.removeprefix("Bearer ").strip()
    role = _resolve_role(token)
    if role is None:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="invalid_admin_token")

    return AdminActor(subject=f"token:{role.value}", role=role)


def require_super_admin(actor: AdminActor = Depends(require_admin)) -> AdminActor:
    if actor.role != AdminRole.SUPER_ADMIN:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="super_admin_required")
    return actor


def _resolve_role(token: str) -> AdminRole | None:
    if settings.dotchat_super_admin_token and token == settings.dotchat_super_admin_token:
        return AdminRole.SUPER_ADMIN
    if settings.dotchat_moderator_token and token == settings.dotchat_moderator_token:
        return AdminRole.MODERATOR
    if settings.dotchat_support_admin_token and token == settings.dotchat_support_admin_token:
        return AdminRole.SUPPORT_ADMIN
    return None
