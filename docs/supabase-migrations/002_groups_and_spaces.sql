-- Migration: Add groups table for Spaces feature
-- Groups are community spaces alongside channels, unified under "Spaces" tab.

CREATE TABLE IF NOT EXISTS groups (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  title TEXT NOT NULL,
  description TEXT DEFAULT '',
  avatar_url TEXT,
  visibility TEXT NOT NULL DEFAULT 'private',
  invite_link TEXT,
  created_by UUID REFERENCES profiles(id) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS group_members (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  group_id UUID REFERENCES groups(id) ON DELETE CASCADE NOT NULL,
  user_id UUID REFERENCES profiles(id) NOT NULL,
  role TEXT NOT NULL DEFAULT 'member',
  joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE(group_id, user_id)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_groups_created_by ON groups(created_by);
CREATE INDEX IF NOT EXISTS idx_group_members_user_id ON group_members(user_id);
CREATE INDEX IF NOT EXISTS idx_group_members_group_id ON group_members(group_id);

-- RLS for groups
ALTER TABLE groups ENABLE ROW LEVEL SECURITY;
ALTER TABLE group_members ENABLE ROW LEVEL SECURITY;

-- Anyone can read public groups
CREATE POLICY "public_groups_readable"
  ON groups FOR SELECT
  USING (visibility = 'public');

-- Members can read private groups they belong to
CREATE POLICY "members_can_read_private_groups"
  ON groups FOR SELECT
  USING (
    visibility = 'private'
    AND EXISTS (
      SELECT 1 FROM group_members
      WHERE group_members.group_id = groups.id
      AND group_members.user_id = auth.uid()
    )
  );

-- Authenticated users can create groups
CREATE POLICY "authenticated_users_can_create_groups"
  ON groups FOR INSERT
  WITH CHECK (auth.uid() = created_by);

-- Group creators can update their groups
CREATE POLICY "creators_can_update_groups"
  ON groups FOR UPDATE
  USING (auth.uid() = created_by);

-- Members can read their own memberships
CREATE POLICY "members_can_read_own_memberships"
  ON group_members FOR SELECT
  USING (auth.uid() = user_id);

-- Group admins/creators can manage memberships
CREATE POLICY "admins_can_manage_memberships"
  ON group_members FOR ALL
  USING (
    EXISTS (
      SELECT 1 FROM group_members gm
      WHERE gm.group_id = group_members.group_id
      AND gm.user_id = auth.uid()
      AND gm.role IN ('admin', 'owner')
    )
  );

-- Users can add themselves (join public groups)
CREATE POLICY "users_can_join_public_groups"
  ON group_members FOR INSERT
  WITH CHECK (
    auth.uid() = user_id
    AND EXISTS (
      SELECT 1 FROM groups
      WHERE groups.id = group_members.group_id
      AND groups.visibility = 'public'
    )
  );

-- Users can leave groups (delete own membership)
CREATE POLICY "users_can_leave_groups"
  ON group_members FOR DELETE
  USING (auth.uid() = user_id);

-- Channel posts table (for channel content pipeline)
CREATE TABLE IF NOT EXISTS channel_posts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  channel_id UUID REFERENCES channels(id) ON DELETE CASCADE NOT NULL,
  author_id UUID REFERENCES profiles(id) NOT NULL,
  text TEXT DEFAULT '',
  attachments JSONB DEFAULT '[]'::jsonb,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_channel_posts_channel_id ON channel_posts(channel_id);

ALTER TABLE channel_posts ENABLE ROW LEVEL SECURITY;

-- Channel subscribers can read posts
CREATE POLICY "subscribers_can_read_posts"
  ON channel_posts FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM channel_subscriptions
      WHERE channel_subscriptions.channel_id = channel_posts.channel_id
      AND channel_subscriptions.user_id = auth.uid()
    )
    OR EXISTS (
      SELECT 1 FROM channels
      WHERE channels.id = channel_posts.channel_id
      AND channels.visibility = 'public'
    )
  );

-- Channel owners can create posts
CREATE POLICY "owners_can_create_posts"
  ON channel_posts FOR INSERT
  WITH CHECK (
    EXISTS (
      SELECT 1 FROM channels
      WHERE channels.id = channel_posts.channel_id
      AND channels.created_by = auth.uid()
    )
  );
