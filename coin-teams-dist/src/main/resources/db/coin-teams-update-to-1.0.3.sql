ALTER TABLE `surfteams`.`requests` DROP COLUMN `id`
, CHANGE COLUMN `uuid` `uuid` VARCHAR(255) NOT NULL DEFAULT NULL
, DROP PRIMARY KEY 
, ADD PRIMARY KEY (`uuid`, `group_id`) ;