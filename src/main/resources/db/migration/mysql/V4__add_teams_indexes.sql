ALTER TABLE teams
  ADD INDEX teams_urn_index (urn);
ALTER TABLE teams
  ADD INDEX teams_name_index (name);
ALTER TABLE persons
  ADD INDEX persons_urn_index (urn);
ALTER TABLE persons
  ADD INDEX persons_name_index (name);
