ALTER TABLE `teams`.`requests` DROP COLUMN `id`
, DROP PRIMARY KEY
, ADD PRIMARY KEY (`group_id`, `uuid`) ;