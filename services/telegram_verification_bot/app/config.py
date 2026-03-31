from __future__ import annotations

from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", case_sensitive=False)

    host: str = "127.0.0.1"
    port: int = 8080
    log_level: str = "INFO"

    telegram_bot_token: str = ""
    telegram_bot_username: str = "dot_chat_verification_bot"

    supabase_url: str = ""
    supabase_service_role_key: str = ""
    supabase_verification_table: str = "verification_sessions"

    verification_code_length: int = 5
    verification_ttl_seconds: int = 300
    verification_resend_cooldown_seconds: int = 60
    verification_max_requests_per_window: int = 5
    verification_rate_limit_window_seconds: int = 900


@lru_cache(maxsize=1)
def get_settings() -> Settings:
    return Settings()
