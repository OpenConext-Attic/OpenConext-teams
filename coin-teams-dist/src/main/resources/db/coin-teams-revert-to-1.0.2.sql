ALTER TABLE `surfteams`.`requests` DROP COLUMN `id`
, DROP PRIMARY KEY
, ADD PRIMARY KEY (`group_id`, `uuid`) ;