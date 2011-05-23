create table grouper_groups (id varchar(40) not null, parent_stem varchar(40) not null,  name varchar(1024) not null,  display_name varchar(1024) not null,  description varchar(1024) default null,  primary key (id));
create table grouper_stems (id varchar(40) not null,  name varchar(40) not null,  primary key (id));
create table grouper_members (id varchar(40) not null,  subject_id varchar(40) not null,  primary key (id));
create table grouper_memberships (id varchar(40) not null, member_id varchar(40) not null, owner_group_id varchar(40),  primary key (id));
create table grouper_rpt_group_field_v (id varchar(40) not null, group_name varchar(40) not null, field_type varchar(40) not null, field_name varchar(40),  primary key (id));

-- two stems (1 default and 1 virtual organization stem)
insert into grouper_stems (id, name) values ('1', 'nl:surfnet:diensten');
insert into grouper_stems (id, name) values ('2', 'nl:surfnet:etc');

-- 6 groups where the first 5 are in the default stem
insert into grouper_groups (id, parent_stem, name, display_name, description) values ('1', '1', 'nl:surfnet:diensten:team1', 'nl:surfnet:diensten:Team 1', 'Team 1 description');
insert into grouper_groups (id, parent_stem, name, display_name, description) values ('2', '1', 'nl:surfnet:diensten:team2', 'nl:surfnet:diensten:Team 2', 'Team 2 description');
insert into grouper_groups (id, parent_stem, name, display_name, description) values ('3', '1', 'nl:surfnet:diensten:team3', 'nl:surfnet:diensten:Team 3', 'Team 3 description');
insert into grouper_groups (id, parent_stem, name, display_name, description) values ('4', '1', 'nl:surfnet:diensten:team4', 'nl:surfnet:diensten:Team 4', 'Team 4 description');
insert into grouper_groups (id, parent_stem, name, display_name, description) values ('5', '1', 'nl:surfnet:diensten:team5', 'nl:surfnet:diensten:Team 5', 'Team 5 description');
insert into grouper_groups (id, parent_stem, name, display_name, description) values ('6', '2', 'nl:surfnet:diensten:team6', 'nl:surfnet:diensten:Team 6', 'Team 6 description');

-- all of the groups are public except for group 4 and 5
insert into grouper_rpt_group_field_v (id, group_name, field_type, field_name) values ('1', 'nl:surfnet:diensten:team1', 'access', 'viewers');
insert into grouper_rpt_group_field_v (id, group_name, field_type, field_name) values ('2', 'nl:surfnet:diensten:team2', 'access', 'viewers');
insert into grouper_rpt_group_field_v (id, group_name, field_type, field_name) values ('3', 'nl:surfnet:diensten:team3', 'access', 'viewers');
insert into grouper_rpt_group_field_v (id, group_name, field_type, field_name) values ('4', 'nl:surfnet:diensten:team4', 'access', 'members');
insert into grouper_rpt_group_field_v (id, group_name, field_type, field_name) values ('5', 'nl:surfnet:diensten:team5', 'access', 'members');
insert into grouper_rpt_group_field_v (id, group_name, field_type, field_name) values ('6', 'nl:surfnet:diensten:team6', 'access', 'viewers');


-- two members
insert into grouper_members (id, subject_id) values ('1', 'urn:collab:person:test.surfguest.nl:personId');
insert into grouper_members (id, subject_id) values ('2', 'urn:collab:person:test.surfguest.nl:notId');

-- the personId member is part of 4 groups and the notId is member of group 5
insert into grouper_memberships (id, member_id, owner_group_id) values ('1', '1', '1');
insert into grouper_memberships (id, member_id, owner_group_id) values ('2', '1', '2');
insert into grouper_memberships (id, member_id, owner_group_id) values ('3', '1', '3');
insert into grouper_memberships (id, member_id, owner_group_id) values ('4', '1', '4');
insert into grouper_memberships (id, member_id, owner_group_id) values ('5', '2', '5');

