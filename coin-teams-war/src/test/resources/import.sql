create table grouper_groups (id varchar(40) not null, parent_stem varchar(40) not null,  name varchar(1024) not null,  display_name varchar(1024) not null,  description varchar(1024) default null,  primary key (id));
create table grouper_stems (id varchar(40) not null,  name varchar(40) not null,  primary key (id));

insert into grouper_stems (id, name) values ('1', 'nl:surfnet:diensten');
insert into grouper_stems (id, name) values ('2', 'nl:surfnet:etc');

insert into grouper_groups (id, parent_stem, name, display_name, description) values ('1', '1', 'nl:surfnet:diensten:team1', 'nl:surfnet:diensten:Team 1', 'Team 1 description');
insert into grouper_groups (id, parent_stem, name, display_name, description) values ('2', '1', 'nl:surfnet:diensten:team2', 'nl:surfnet:diensten:Team 2', 'Team 2 description');
insert into grouper_groups (id, parent_stem, name, display_name, description) values ('3', '1', 'nl:surfnet:diensten:team3', 'nl:surfnet:diensten:Team 3', 'Team 3 description');
insert into grouper_groups (id, parent_stem, name, display_name, description) values ('4', '1', 'nl:surfnet:diensten:team4', 'nl:surfnet:diensten:Team 4', 'Team 4 description');
insert into grouper_groups (id, parent_stem, name, display_name, description) values ('5', '1', 'nl:surfnet:diensten:team5', 'nl:surfnet:diensten:Team 5', 'Team 5 description');
insert into grouper_groups (id, parent_stem, name, display_name, description) values ('6', '2', 'nl:surfnet:diensten:team6', 'nl:surfnet:diensten:Team 6', 'Team 6 description');
