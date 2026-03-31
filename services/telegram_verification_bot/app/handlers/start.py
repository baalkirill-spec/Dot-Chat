from __future__ import annotations

from telegram import Update
from telegram.ext import ContextTypes

from app.services.verification_service import VerificationError, VerificationService


def build_start_handler(verification_service: VerificationService):
    async def handle_start(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
        if update.effective_user is None or update.effective_chat is None:
            return

        if not context.args:
            await update.effective_chat.send_message(
                "Dot Chat verification link is missing. Please return to the app and request a new code."
            )
            return

        start_token = context.args[0]
        try:
            result = verification_service.link_telegram_user(
                start_token=start_token,
                telegram_user_id=str(update.effective_user.id),
                telegram_username=update.effective_user.username,
            )
        except VerificationError as error:
            await update.effective_chat.send_message(f"Verification failed: {error}")
            return

        await update.effective_chat.send_message(
            "\n".join(
                [
                    "Dot Chat verification linked successfully.",
                    f"Phone: {result.phone_number_masked}",
                    f"Your verification code: {result.code}",
                    "Return to Dot Chat and enter this code to continue.",
                ],
            ),
        )

    return handle_start
