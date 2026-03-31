from __future__ import annotations

from typing import Optional

from supabase import Client, create_client

from app.models.verification import VerificationSessionRecord, VerificationStatus


class SupabaseVerificationRepository:
    def __init__(self, supabase_url: str, service_role_key: str, table_name: str) -> None:
        self._client: Client = create_client(supabase_url, service_role_key)
        self._table_name = table_name

    def create(self, session: VerificationSessionRecord) -> VerificationSessionRecord:
        self._client.table(self._table_name).insert(session.model_dump(mode="json")).execute()
        return session

    def update(self, session: VerificationSessionRecord) -> VerificationSessionRecord:
        self._client.table(self._table_name).update(session.model_dump(mode="json")).eq("id", session.id).execute()
        return session

    def find_by_id(self, session_id: str) -> Optional[VerificationSessionRecord]:
        response = self._client.table(self._table_name).select("*").eq("id", session_id).limit(1).execute()
        rows = response.data or []
        return VerificationSessionRecord.model_validate(rows[0]) if rows else None

    def find_latest_by_phone(self, phone_number: str) -> Optional[VerificationSessionRecord]:
        response = (
            self._client.table(self._table_name)
            .select("*")
            .eq("phone_number", phone_number)
            .in_("status", [VerificationStatus.PENDING_LINK.value, VerificationStatus.CODE_SENT.value])
            .order("created_at", desc=True)
            .limit(1)
            .execute()
        )
        rows = response.data or []
        return VerificationSessionRecord.model_validate(rows[0]) if rows else None

    def find_by_start_token(self, start_token: str) -> Optional[VerificationSessionRecord]:
        response = self._client.table(self._table_name).select("*").eq("telegram_start_token", start_token).limit(1).execute()
        rows = response.data or []
        return VerificationSessionRecord.model_validate(rows[0]) if rows else None
