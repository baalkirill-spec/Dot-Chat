from __future__ import annotations

import asyncio
import uvicorn

from app.api.routes import build_api
from app.bot.provider import TelegramVerificationBot
from app.config import get_settings
from app.logging import configure_logging
from app.services.code_service import VerificationCodeService
from app.services.rate_limit import InMemoryRateLimiter
from app.services.session_repository import SupabaseVerificationRepository
from app.services.verification_service import VerificationService


async def bootstrap() -> None:
    settings = get_settings()
    configure_logging(settings.log_level)

    repository = SupabaseVerificationRepository(
        supabase_url=settings.supabase_url,
        service_role_key=settings.supabase_service_role_key,
        table_name=settings.supabase_verification_table,
    )
    rate_limiter = InMemoryRateLimiter(
        max_requests=settings.verification_max_requests_per_window,
        window_seconds=settings.verification_rate_limit_window_seconds,
    )
    code_service = VerificationCodeService(code_length=settings.verification_code_length)
    verification_service = VerificationService(
        repository=repository,
        rate_limiter=rate_limiter,
        code_service=code_service,
        ttl_seconds=settings.verification_ttl_seconds,
        resend_cooldown_seconds=settings.verification_resend_cooldown_seconds,
        telegram_bot_username=settings.telegram_bot_username,
    )
    bot = TelegramVerificationBot(
        token=settings.telegram_bot_token,
        verification_service=verification_service,
    )
    api = build_api(verification_service=verification_service, bot_provider=bot)

    config = uvicorn.Config(
        api,
        host=settings.host,
        port=settings.port,
        log_level=settings.log_level.lower(),
    )
    server = uvicorn.Server(config)

    tasks = [asyncio.create_task(server.serve())]
    if settings.telegram_bot_token:
        tasks.append(asyncio.create_task(bot.run_polling()))

    await asyncio.gather(*tasks)


if __name__ == "__main__":
    asyncio.run(bootstrap())
