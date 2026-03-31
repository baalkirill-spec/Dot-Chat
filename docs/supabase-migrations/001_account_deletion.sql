-- Migration: Add account deletion support to profiles table
-- Required before delete account feature can work end-to-end.
--
-- Run this in the Supabase SQL Editor or as a migration.

-- Add account_status column if it doesn't exist
ALTER TABLE profiles
  ADD COLUMN IF NOT EXISTS account_status TEXT NOT NULL DEFAULT 'active';

-- Add deletion tracking columns
ALTER TABLE profiles
  ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

ALTER TABLE profiles
  ADD COLUMN IF NOT EXISTS deletion_requested_at TIMESTAMPTZ;

ALTER TABLE profiles
  ADD COLUMN IF NOT EXISTS deletion_reason TEXT;

-- Create index for admin queries on account status
CREATE INDEX IF NOT EXISTS idx_profiles_account_status
  ON profiles (account_status);

-- RLS: Users can only update their own account_status to 'pending_deletion'
-- (preventing users from banning other users or setting arbitrary statuses)
CREATE POLICY IF NOT EXISTS "users_can_soft_delete_own_account"
  ON profiles
  FOR UPDATE
  USING (auth.uid() = id)
  WITH CHECK (
    auth.uid() = id
    AND account_status IN ('active', 'pending_deletion')
  );

-- Admin audit log table (referenced by admin panel)
CREATE TABLE IF NOT EXISTS admin_audit_logs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  admin_subject TEXT NOT NULL,
  admin_role TEXT NOT NULL,
  action TEXT NOT NULL,
  target_user_id UUID REFERENCES profiles(id),
  reason TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Only service_role can access admin_audit_logs
ALTER TABLE admin_audit_logs ENABLE ROW LEVEL SECURITY;

-- Revoke all from anon/authenticated, only service_role can access
CREATE POLICY IF NOT EXISTS "admin_audit_logs_service_role_only"
  ON admin_audit_logs
  FOR ALL
  USING (false);
