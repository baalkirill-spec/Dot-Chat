from datetime import UTC, datetime
from uuid import uuid4

from supabase import Client, create_client

from app.config import settings
from app.models import AccountStatus, AdminActor, ManagedAccount


class SupabaseAdminService:
    def __init__(self) -> None:
        self.client: Client = create_client(
            settings.dotchat_supabase_url,
            settings.dotchat_supabase_service_role_key,
        )

    def search_accounts(self, query: str | None, limit: int = 50) -> list[ManagedAccount]:
        request = self.client.table("profiles").select("*").limit(limit)
        if query:
            normalized = query.strip()
            request = request.or_(
                ",".join(
                    [
                        f"id.eq.{normalized}",
                        f"username.ilike.%{normalized}%",
                        f"display_name.ilike.%{normalized}%",
                        f"phone_number.ilike.%{normalized}%",
                    ],
                ),
            )
        rows = request.execute().data or []
        return [ManagedAccount.model_validate(row) for row in rows]

    def get_account(self, user_id: str) -> ManagedAccount | None:
        rows = self.client.table("profiles").select("*").eq("id", user_id).limit(1).execute().data or []
        if not rows:
            return None
        return ManagedAccount.model_validate(rows[0])

    def update_status(
        self,
        *,
        user_id: str,
        actor: AdminActor,
        new_status: AccountStatus,
        reason: str | None,
    ) -> ManagedAccount | None:
        payload: dict[str, object | None] = {
            "account_status": new_status.value,
            "deleted_at": datetime.now(UTC).isoformat() if new_status == AccountStatus.PENDING_DELETION else None,
            "deactivated_at": datetime.now(UTC).isoformat() if new_status == AccountStatus.DEACTIVATED else None,
        }
        self.client.table("profiles").update(payload).eq("id", user_id).execute()
        self.append_audit_log(
            actor=actor,
            action=f"account.{new_status.value}",
            target_user_id=user_id,
            reason=reason,
        )
        return self.get_account(user_id)

    def terminate_all_sessions(self, *, user_id: str, actor: AdminActor, reason: str | None) -> None:
        self.client.table("device_sessions").delete().eq("user_id", user_id).execute()
        self.append_audit_log(
            actor=actor,
            action="account.force_logout_all",
            target_user_id=user_id,
            reason=reason,
        )

    def append_audit_log(
        self,
        *,
        actor: AdminActor,
        action: str,
        target_user_id: str,
        reason: str | None,
    ) -> None:
        self.client.table("admin_audit_logs").insert(
            {
                "id": f"audit_{uuid4().hex}",
                "admin_role": actor.role.value,
                "admin_subject": actor.subject,
                "action": action,
                "target_user_id": target_user_id,
                "reason": reason,
                "created_at": datetime.now(UTC).isoformat(),
            },
        ).execute()
