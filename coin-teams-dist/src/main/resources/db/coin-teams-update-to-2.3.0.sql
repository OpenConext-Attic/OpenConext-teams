ALTER TABLE `invitations` ADD `intended_role` varchar(255) default NULL;

CREATE TABLE `external_groups` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `description` longtext,
  `group_provider` varchar(255) DEFAULT NULL,
  `identifier` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;

CREATE TABLE `team_external_groups` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `grouper_team_id` varchar(255) DEFAULT NULL,
  `external_groups_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `grouper_team_id` (`grouper_team_id`,`external_groups_id`),
  KEY `FKB046E6E69AB3B3FA` (`external_groups_id`),
  CONSTRAINT `FKB046E6E69AB3B3FA` FOREIGN KEY (`external_groups_id`) REFERENCES `external_groups` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;