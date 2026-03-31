# Dot Chat Telegram Verification Service

Auxiliary Python service for Dot Chat phone verification flows that involve a Telegram bot as one delivery/confirmation channel.

## What it does

- Creates verification sessions for a phone number.
- Generates one-time numeric codes.
- Links a pending verification session to a Telegram user through a `/start <token>` deep link.
- Sends or re-sends the verification code through the Telegram bot after the user links the session.
- Verifies submitted codes and marks sessions as completed.
- Stores verification sessions in Supabase.

## Why it exists

The Android app should not know the internal Telegram bot mechanics. The intended layering is:

1. Dot Chat Android app
2. Supabase/backend layer
3. Telegram verification service
4. Telegram bot delivery/provider integration

## Suggested runtime flow

1. Android sends `POST /verification/request` with a phone number.
2. Service creates a pending session in Supabase and returns:
   - `session_id`
   - `expires_in_seconds`
   - `telegram_start_url`
3. User opens the Telegram bot with that deep link.
4. Bot links the Telegram account to the pending verification session and sends the one-time code.
5. Android submits `session_id + code` to `POST /verification/verify`.
6. Backend marks the session as verified and continues sign-in/profile setup.

## Run locally

1. Create and activate a virtual environment.
2. Install dependencies:

```bash
pip install -r requirements.txt
```

3. Copy `.env.example` to `.env` and fill in values.
4. Start the service:

```bash
python main.py
```

The FastAPI app will start on `http://127.0.0.1:8080` by default, and the bot polling loop will start if `TELEGRAM_BOT_TOKEN` is configured.

## Required Supabase table

Suggested table name: `verification_sessions`

Suggested columns:

- `id uuid primary key`
- `phone_number text not null`
- `channel text not null`
- `status text not null`
- `code_hash text not null`
- `telegram_start_token text unique not null`
- `telegram_user_id text`
- `telegram_username text`
- `attempt_count int not null default 0`
- `delivery_count int not null default 0`
- `created_at timestamptz not null`
- `expires_at timestamptz not null`
- `resend_available_at timestamptz not null`
- `verified_at timestamptz`
- `last_error text`

## Security notes

- Do not store the raw verification code in the database.
- Use the Supabase service role key only on the server side.
- Rate-limit `request`, `resend`, and `verify` actions.
- Expire verification sessions aggressively.
- Rotate deep-link start tokens per session.
- Keep Telegram bot token and Supabase service role key out of the Android app.
