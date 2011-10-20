ALTER TABLE `teams`.`requests`
DROP COLUMN `id`
, DROP COLUMN `message`
, DROP PRIMARY KEY
, ADD PRIMARY KEY (`group_id`, `uuid`) ;

UPDATE `teams`.`requests`
SET `timestamp` = `timestamp` / 1000
WHERE `timestamp` > 10000000000;

ALTER TABLE `teams`.`invitations`
DROP COLUMN `id`
, DROP COLUMN `message`
, DROP PRIMARY KEY
, ADD PRIMARY KEY (`invitation_uiid`);

UPDATE `teams`.`invitations`
SET `timestamp` = `timestamp` / 1000
WHERE `timestamp` > 10000000000;