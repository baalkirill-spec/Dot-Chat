from fastapi import APIRouter, Depends, HTTPException, Query, status

from app.models import AccountStatus, AdminActor, ModerationActionRequest
from app.security import require_admin, require_super_admin
from app.services.supabase_admin import SupabaseAdminService


router = APIRouter(prefix="/accounts", tags=["accounts"])
service = SupabaseAdminService()


@router.get("")
def list_accounts(
    query: str | None = Query(default=None),
    limit: int = Query(default=50, ge=1, le=100),
    _: AdminActor = Depends(require_admin),
):
    return service.search_accounts(query=query, limit=limit)


@router.get("/{user_id}")
def get_account(
    user_id: str,
    _: AdminActor = Depends(require_admin),
):
    account = service.get_account(user_id)
    if account is None:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="account_not_found")
    return account


@router.post("/{user_id}/ban")
def ban_account(
    user_id: str,
    body: ModerationActionRequest,
    actor: AdminActor = Depends(require_admin),
):
    return service.update_status(
        user_id=user_id,
        actor=actor,
        new_status=AccountStatus.BANNED,
        reason=body.reason,
    )


@router.post("/{user_id}/unban")
def unban_account(
    user_id: str,
    body: ModerationActionRequest,
    actor: AdminActor = Depends(require_admin),
):
    return service.update_status(
        user_id=user_id,
        actor=actor,
        new_status=AccountStatus.ACTIVE,
        reason=body.reason,
    )


@router.post("/{user_id}/deactivate")
def deactivate_account(
    user_id: str,
    body: ModerationActionRequest,
    actor: AdminActor = Depends(require_admin),
):
    return service.update_status(
        user_id=user_id,
        actor=actor,
        new_status=AccountStatus.DEACTIVATED,
        reason=body.reason,
    )


@router.post("/{user_id}/delete")
def mark_account_for_deletion(
    user_id: str,
    body: ModerationActionRequest,
    actor: AdminActor = Depends(require_super_admin),
):
    return service.update_status(
        user_id=user_id,
        actor=actor,
        new_status=AccountStatus.PENDING_DELETION,
        reason=body.reason,
    )


@router.post("/{user_id}/logout-all")
def logout_all_sessions(
    user_id: str,
    body: ModerationActionRequest,
    actor: AdminActor = Depends(require_admin),
):
    service.terminate_all_sessions(user_id=user_id, actor=actor, reason=body.reason)
    return {"status": "ok"}
