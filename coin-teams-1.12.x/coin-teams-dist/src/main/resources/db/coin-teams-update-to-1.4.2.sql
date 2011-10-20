ALTER TABLE `teams`.`invitations` DROP COLUMN `message` , DROP COLUMN `inviter` ;

CREATE TABLE `teams`.`invitation_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `inviter` varchar(255) DEFAULT NULL,
  `message` longtext,
  `timestamp` bigint(20) NOT NULL,
  `invitation_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;

