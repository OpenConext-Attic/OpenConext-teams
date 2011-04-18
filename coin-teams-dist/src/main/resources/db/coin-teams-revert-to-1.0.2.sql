ALTER TABLE `teams`.`requests`
DROP COLUMN `id`
, DROP COLUMN `message`
, DROP PRIMARY KEY
, ADD PRIMARY KEY (`group_id`, `uuid`) ;

ALTER TABLE `teams`.`invitations`
DROP COLUMN `id`
, DROP PRIMARY KEY
, ADD PRIMARY KEY (`invitation_uiid`);