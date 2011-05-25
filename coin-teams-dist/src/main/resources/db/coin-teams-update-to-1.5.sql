CREATE TABLE `member_attributes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `attribute_name` varchar(255) NOT NULL,
  `attribute_value` varchar(255) DEFAULT NULL,
  `member_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `member_id` (`member_id`,`attribute_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;