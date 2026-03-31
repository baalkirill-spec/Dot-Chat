from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

    dotchat_supabase_url: str
    dotchat_supabase_service_role_key: str
    dotchat_super_admin_token: str | None = None
    dotchat_moderator_token: str | None = None
    dotchat_support_admin_token: str | None = None


settings = Settings()
