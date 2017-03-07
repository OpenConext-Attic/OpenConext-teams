CREATE TABLE teams (
  id          MEDIUMINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
  urn         VARCHAR(255) NOT NULL,
  name        VARCHAR(255) NOT NULL,
  description TEXT,
  viewable    BOOLEAN,
  created     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
)
  ENGINE = InnoDB;

ALTER TABLE teams
  ADD UNIQUE INDEX teams_urn_unique (urn);

CREATE TABLE persons (
  id      MEDIUMINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
  urn     VARCHAR(255) NOT NULL,
  name    VARCHAR(255) NOT NULL,
  email   VARCHAR(255) NOT NULL,
  guest   BOOLEAN,
  created TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
)
  ENGINE = InnoDB;

ALTER TABLE persons
  ADD UNIQUE INDEX persons_email_unique (email);
ALTER TABLE persons
  ADD UNIQUE INDEX persons_urn_unique (urn);

CREATE TABLE memberships (
  role      VARCHAR(255) NOT NULL,
  team_id   MEDIUMINT    NOT NULL,
  person_id MEDIUMINT    NOT NULL,
  created   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (team_id, person_id),
  FOREIGN KEY (team_id) REFERENCES teams (id)
    ON DELETE CASCADE,
  FOREIGN KEY (person_id) REFERENCES persons (id)
    ON DELETE CASCADE
)
  ENGINE = InnoDB;

