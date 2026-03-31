# Signal-Android Reference Log

This document tracks every instance where Dot Chat's architecture, patterns, or code
was informed by studying Signal-Android (GPLv3, Copyright Signal Messenger LLC).

## License Obligation

Signal-Android is licensed under the GNU General Public License v3.0.
Any code that is directly derived from Signal-Android must:
1. Include a GPL v3 notice in the file header
2. Be documented in this file with source → target mapping
3. Not be used in a way that violates the copyleft obligations

## Reference-Only Patterns (no code copied)

These are architectural ideas studied from Signal-Android and reimplemented
independently for Dot Chat's Supabase + LiveKit architecture.

| Signal Source | Dot Chat Target | What Was Learned |
|---|---|---|
| `MessageSender` queuing logic | `SupabaseChatsRepository.sendMessage()` | Retry/offline queue pattern (not yet implemented) |
| `WebRtcCallManager` state machine | `LiveKitRoomControllerImpl` | Call lifecycle states: CONNECTING → CONNECTED → RECONNECTING → ENDED → FAILED |
| `ConversationItem` bubble clustering | Future chat screen redesign | Timestamp grouping, sender clustering, tail shapes |
| `AttachmentUploadJob` | Future attachment pipeline | Chunked upload, progress tracking, retry on failure |
| Device/session tracking | `SupabaseSessionsRepository` | Multi-device session display, terminate other sessions |

## Directly Adapted Code

None yet. When code is adapted, each entry will include:
- Signal source file path and commit hash
- Dot Chat target file path
- Lines adapted
- GPL v3 header requirement status
