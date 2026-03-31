# Dot Chat Admin Panel Service

Internal admin surface for account moderation and support operations.

## Scope

- search accounts by id, phone, username, or display name
- view account detail
- ban / unban
- deactivate
- force logout all sessions
- append admin audit logs

This service is intentionally separate from the consumer Android app.

## Environment

Copy `.env.example` to `.env` and provide:

- `DOTCHAT_SUPABASE_URL`
- `DOTCHAT_SUPABASE_SERVICE_ROLE_KEY`
- at least one admin token:
  - `DOTCHAT_SUPER_ADMIN_TOKEN`
  - `DOTCHAT_MODERATOR_TOKEN`
  - `DOTCHAT_SUPPORT_ADMIN_TOKEN`

## Run

```bash
pip install -r requirements.txt
uvicorn main:app --reload --port 8081
```

## Auth model

Use `Authorization: Bearer <token>`.

Roles are derived from the configured token:

- `super_admin`
- `moderator`
- `support_admin`

## Expected tables

- `profiles`
- `device_sessions`
- `admin_audit_logs`

Recommended fields on `profiles`:

- `id`
- `username`
- `display_name`
- `phone_number`
- `avatar_url`
- `bio`
- `account_status`
- `deleted_at`
- `deactivated_at`

Recommended fields on `admin_audit_logs`:

- `id`
- `admin_role`
- `admin_subject`
- `action`
- `target_user_id`
- `reason`
- `metadata`
- `created_at`
