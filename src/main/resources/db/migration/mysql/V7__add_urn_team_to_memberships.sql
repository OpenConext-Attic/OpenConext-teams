ALTER TABLE memberships
  ADD COLUMN urn_team VARCHAR(255) NOT NULL AFTER team_id;
