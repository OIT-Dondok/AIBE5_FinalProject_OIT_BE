ALTER TABLE member
  ADD COLUMN oauth_provider VARCHAR(20) NULL,
  ADD COLUMN oauth_provider_id VARCHAR(100) NULL,
  ADD CONSTRAINT uk_member_oauth_provider_id UNIQUE (oauth_provider, oauth_provider_id);
