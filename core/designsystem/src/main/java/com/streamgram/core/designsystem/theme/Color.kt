package com.streamgram.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// ── Dot Chat Brand Colors ──────────────────────────────────────────────
val DotChatBlue = Color(0xFF3478F6)
val DotChatBlueDark = Color(0xFF5DA6FF)
val DotChatBlueSoft = Color(0xFFE8F1FF)
val DotChatGreen = Color(0xFF30D158)
val DotChatOrange = Color(0xFFFF9F0A)
val DotChatRed = Color(0xFFFF453A)
val DotChatPurple = Color(0xFF8E7CFF)

// ── Neutral Slate Palette ──────────────────────────────────────────────
val Slate950 = Color(0xFF0F1722)
val Slate900 = Color(0xFF17212B)
val Slate800 = Color(0xFF1F2B39)
val Slate700 = Color(0xFF334155)
val Slate500 = Color(0xFF6B7A90)
val Slate300 = Color(0xFFC9D3E1)
val Slate200 = Color(0xFFDDE5EF)
val Slate100 = Color(0xFFF0F4F8)
val Slate50 = Color(0xFFF6F8FB)

// ── Surfaces ───────────────────────────────────────────────────────────
val SurfaceWhite = Color(0xFFFFFFFF)
val SurfaceMuted = Color(0xFFF2F2F7)
val SurfaceDark = Color(0xFF1D2733)
val DividerLight = Color(0xFFE5E5EA)
val DividerDark = Color(0xFF243242)

// ── Alpha Utilities ────────────────────────────────────────────────────
val BlackAlpha20 = Color(0x33000000)
val BlackAlpha44 = Color(0x70000000)
val WhiteAlpha12 = Color(0x1FFFFFFF)
val WhiteAlpha20 = Color(0x33FFFFFF)

// ── Semantic Aliases ───────────────────────────────────────────────────
val Ink950 = Slate950
val Ink900 = Slate900
val Ink800 = Slate800
val Ink700 = Slate700
val Pearl50 = Slate50
val Pearl100 = Slate100
val Pearl200 = Slate200
val Pearl400 = Slate500
val Blue500 = DotChatBlue
val Blue400 = DotChatBlueDark
val Cyan300 = DotChatBlueDark
val Emerald400 = DotChatGreen
val Coral400 = DotChatRed
val Amber300 = DotChatOrange

// ── Backwards-compatible aliases (used across feature screens) ─────────
// TODO: remove these once all feature screens migrate to DotChat* names
@Deprecated("Use DotChatBlue", ReplaceWith("DotChatBlue"))
val TelegramBlue = DotChatBlue
@Deprecated("Use DotChatBlueDark", ReplaceWith("DotChatBlueDark"))
val TelegramBlueDark = DotChatBlueDark
@Deprecated("Use DotChatBlueSoft", ReplaceWith("DotChatBlueSoft"))
val TelegramBlueSoft = DotChatBlueSoft
@Deprecated("Use DotChatGreen", ReplaceWith("DotChatGreen"))
val TelegramGreen = DotChatGreen
@Deprecated("Use DotChatOrange", ReplaceWith("DotChatOrange"))
val TelegramOrange = DotChatOrange
@Deprecated("Use DotChatRed", ReplaceWith("DotChatRed"))
val TelegramRed = DotChatRed
@Deprecated("Use DotChatPurple", ReplaceWith("DotChatPurple"))
val TelegramPurple = DotChatPurple
