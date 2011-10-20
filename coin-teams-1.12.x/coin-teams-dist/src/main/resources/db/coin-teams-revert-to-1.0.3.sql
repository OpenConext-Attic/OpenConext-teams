ALTER TABLE `teams`.`invitations` ADD COLUMN `message` longtext NULL  AFTER `id` , ADD COLUMN `inviter` VARCHAR(255) NULL  AFTER `message` ;
DROP TABLE `teams`.`invitation_message`;