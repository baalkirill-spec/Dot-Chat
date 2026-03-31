# Dot Chat Total Rewrite

## Hard Audit

The current app still mixes real backend-backed flows with legacy fake or half-true surfaces. The biggest trust failures are:

- bottom navigation was overloaded and treated service surfaces like first-class product tabs
- settings and profile previously implied more working account management than the backend truly supported
- channels still lacked a real end-to-end `channel_posts` pipeline, so showing post cards there was dishonest
- some runtime behaviors were previously decorative, especially power saving and reduced motion
- Russian localization quality remains inconsistent and needs a dedicated normalization pass
- fake network plumbing still exists in the transport layer and must be fully removed

## Rewrite Axes

1. Stability hardening
2. Navigation and information architecture rewrite
3. iOS-inspired design system rewrite
4. Honest backend wiring for visible flows
5. Calls and realtime hardening
6. Security architecture implementation
7. Delete account and moderation architecture

## Stability Hardening Plan

Primary crash and instability risks:

- Compose screens that assume non-null backend data on first render
- flow collectors that do not degrade cleanly when a Supabase table is missing
- navigation restore after process death landing on screens that expect active session state
- background -> foreground call transitions where LiveKit room state can become stale
- attachment and upload flows that need explicit failed/cancelled/retry states
- auth and session restore surfaces that must never silently fall back to false success

Required passes:

- remove remaining fake transport bindings from DI
- audit every top-level screen for null, empty, loading, and error safety
- add defensive state restoration with predictable ViewModel ownership
- make retry and failure messaging explicit for auth, messages, uploads, realtime, and calls
- verify background/foreground, rotation, and process restore on auth, chats, channels, settings, and calls

## Information Architecture And Tabs

Bottom navigation should stay compact and product-first:

1. Chats
2. Contacts
3. Channels
4. Profile

Notifications should not live as a permanent tab. They belong in the account hub because they are service-level activity, security, and moderation events rather than a primary daily destination. Settings, privacy, sessions, language, and future delete-account flows should live under profile/account hierarchy, not at tab level.

## iOS-Inspired Redesign Direction

The redesign should be iOS-inspired without copying iOS literally:

- strong large-title hierarchy on primary roots
- cleaner spacing rhythm and lighter card density
- softer rounded surfaces and quieter elevation
- grouped settings/profile sections with premium list cells
- calmer top bars and safer content insets
- more readable message typography and lighter metadata styling
- reduced-motion aware transitions instead of decorative motion everywhere

Design system rewrite targets:

- colors
- typography
- spacing scale
- shapes and radii
- button hierarchy
- text fields
- settings cells
- account hub sections
- chat bubbles and input bar
- bottom sheets and destructive actions

## Real Feature Wiring Sequence

1. Replace remaining fake DI bindings with real transport and policy layers
2. Finish auth/session/profile truthfulness
3. Complete people search, contacts, and add/remove contact backend flows
4. Harden real chat timeline, typing, delivery, and read events
5. Finish file/media upload pipeline with 200 MB validation and retry/failure states
6. Implement real channel post timeline, comments, reactions, and forwarding
7. Implement polls as a first-class message type
8. Harden LiveKit invite, join, reconnect, and participant mapping

## Backend Hardening

Supabase remains the platform source of truth:

- `profiles`
- `contacts`
- `chats`
- `chat_members`
- `chat_memberships`
- `messages`
- `attachments`
- `channels`
- `channel_subscriptions`
- `channel_posts`
- `comments`
- `reactions`
- `polls`
- `poll_votes`
- `privacy_settings`
- `device_sessions`
- `security_notifications`
- `activity_notifications`
- `moderation_reports`
- `admin_audit_logs`

Required backend work:

- row-level security for all user-owned and membership-scoped data
- storage buckets for avatars and attachments
- realtime channels for messages, typing, delivery, read state, comments, and notifications
- optimistic update rules with honest rollback
- explicit missing-table-safe behavior during rollout

## Delete Account Architecture

Delete account must not be a one-tap destructive row. The safe model is:

1. open destructive account screen from settings/account/privacy
2. explain consequences clearly
3. require explicit confirmation
4. require recent re-auth or fresh verification challenge
5. support soft delete first for MVP, with hard delete architecture behind admin/backend workflow
6. invalidate current and remote sessions
7. reset local app state after backend confirmation

Recommended backend model:

- `account_status`: active, deactivated, banned, pending_deletion
- `deleted_at`
- `deletion_requested_at`
- `deletion_reason`

## Admin Panel Architecture

Admin must be a separate internal surface, not a hidden consumer-app screen.

Scope:

- account search by id, username, display name, phone
- account detail
- ban/unban
- deactivate/reactivate
- delete account
- force logout sessions
- review moderation reports
- inspect security/session metadata
- audit log for every admin action

Roles:

1. super_admin
2. moderator
3. support_admin

Security requirements:

- separate admin auth
- role-based access control
- protected backend endpoints
- audit logging
- destructive confirmation flows
- no direct consumer-client access to admin privileges

## Security Layer Direction

Dot Chat should follow a Signal-style architecture without homemade crypto:

- dedicated security abstractions separate from transport
- device-aware key and session contracts
- future libsignal-backed session establishment
- message pipeline hooks for encrypt/decrypt/sign/verify
- multi-device-ready metadata and sender device context

The rollout can be phased, but the contracts must be real from the start.
