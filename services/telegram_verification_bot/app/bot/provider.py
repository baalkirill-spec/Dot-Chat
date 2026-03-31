from __future__ import annotations

import asyncio
import logging

from telegram.ext import Application, CommandHandler

from app.handlers.start import build_start_handler
from app.services.verification_service import VerificationService

logger = logging.getLogger(__name__)


class TelegramVerificationBot:
    def __init__(self, token: str, verification_service: VerificationService) -> None:
        self._token = token
        self._verification_service = verification_service
        self._application: Application | None = None
        if token:
            self._application = Application.builder().token(token).build()
            self._application.add_handler(CommandHandler("start", build_start_handler(verification_service)))

    @property
    def is_enabled(self) -> bool:
        return self._application is not None

    async def run_polling(self) -> None:
        if self._application is None:
            logger.info("telegram_bot_disabled")
            return
        await self._application.initialize()
        await self._application.start()
        if self._application.updater is not None:
            await self._application.updater.start_polling()
        logger.info("telegram_bot_started")
        await asyncio.Future()

    async def send_code(
        self,
        telegram_user_id: str,
        session_id: str,
        phone_masked: str,
        code: str,
    ) -> None:
        if self._application is None:
            return
        await self._application.bot.send_message(
            chat_id=int(telegram_user_id),
            text="\n".join(
                [
                    "Dot Chat verification code",
                    f"Phone: {phone_masked}",
                    f"Session: {session_id}",
                    f"Code: {code}",
                ],
            ),
        )
