ALTER TABLE `teams`.`requests`
ADD COLUMN `id` BIGINT(20) NOT NULL AUTO_INCREMENT AFTER `timestamp`
, ADD COLUMN `message` TEXT DEFAULT NULL 
, DROP PRIMARY KEY 
, ADD PRIMARY KEY (`id`) 
, ADD UNIQUE INDEX `id_UNIQUE` (`id` ASC) ;

UPDATE `teams`.`requests`
SET `timestamp` = `timestamp` * 1000
WHERE `timestamp` < 10000000000;

ALTER TABLE `teams`.`invitations` ADD COLUMN `id` BIGINT(20) NOT NULL AUTO_INCREMENT AFTER `inviter`
, ADD COLUMN `message` TEXT DEFAULT NULL
, DROP PRIMARY KEY
, ADD PRIMARY KEY (`id`)
, ADD UNIQUE INDEX `id_UNIQUE` (`id` ASC) ;


UPDATE `teams`.`invitations`
SET `timestamp` = `timestamp` * 1000
WHERE `timestamp` < 10000000000;