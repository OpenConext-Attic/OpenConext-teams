INSERT INTO `teams` (`id`, `urn`, `name`, `description`, `viewable`) VALUES (1, 'nl:surfnet:diensten:riders', 'riders', 'we are riders', 1);
INSERT INTO `teams` (`id`, `urn`, `name`, `description`, `viewable`) VALUES (2, 'nl:surfnet:diensten:giants', 'giants', 'we are giants', 1);
INSERT INTO `teams` (`id`, `urn`, `name`, `description`, `viewable`) VALUES (3, 'nl:surfnet:diensten:gliders', 'gliders', 'we are gliders', 1);

INSERT INTO `persons` (`id`, `urn`, `name`, `email`, `guest`) VALUES (1, 'urn:collab:person:surfnet.nl:jdoe','John Doe', 'john.doe@example.org', 0);
INSERT INTO `persons` (`id`, `urn`, `name`, `email`, `guest`) VALUES (2, 'urn:collab:person:surfnet.nl:mdoe','Mary Doe', 'mary.doe@example.org', 1);
INSERT INTO `persons` (`id`, `urn`, `name`, `email`, `guest`) VALUES (3, 'urn:collab:person:surfnet.nl:wdoe','William Doe', 'william.doe@example.org', 0);
INSERT INTO `persons` (`id`, `urn`, `name`, `email`, `guest`) VALUES (4, 'urn:collab:person:surfnet.nl:tdoe','Tracey Doe', 'tracey.doe@example.org', 1);
INSERT INTO `persons` (`id`, `urn`, `name`, `email`, `guest`) VALUES (5, 'urn:collab:person:surfnet.nl:rdoe','Ronald Doe', 'ronald.doe@example.org', 0);

INSERT INTO `memberships` (`id`, `role`, `team_id`, `person_id`, `urn_person`) VALUES (1, 'ADMIN', 1, 1, 'urn:collab:person:surfnet.nl:jdoe');
INSERT INTO `memberships` (`id`, `role`, `team_id`, `person_id`, `urn_person`) VALUES (2, 'MANAGER', 2, 1, 'urn:collab:person:surfnet.nl:jdoe');
INSERT INTO `memberships` (`id`, `role`, `team_id`, `person_id`, `urn_person`) VALUES (3, 'MEMBER', 3, 1, 'urn:collab:person:surfnet.nl:jdoe');
INSERT INTO `memberships` (`id`, `role`, `team_id`, `person_id`, `urn_person`) VALUES (4, 'ADMIN', 2, 2, 'urn:collab:person:surfnet.nl:mdoe');
INSERT INTO `memberships` (`id`, `role`, `team_id`, `person_id`, `urn_person`) VALUES (5, 'ADMIN', 2, 3, 'urn:collab:person:surfnet.nl:wdoe');
INSERT INTO `memberships` (`id`, `role`, `team_id`, `person_id`, `urn_person`) VALUES (6, 'MANAGER', 2, 4, 'urn:collab:person:surfnet.nl:tdoe');
INSERT INTO `memberships` (`id`, `role`, `team_id`, `person_id`, `urn_person`) VALUES (7, 'ADMIN', 3, 4, 'urn:collab:person:surfnet.nl:tdoe');
INSERT INTO `memberships` (`id`, `role`, `team_id`, `person_id`, `urn_person`) VALUES (8, 'ADMIN', 3, 5, 'urn:collab:person:surfnet.nl:rdoe');


