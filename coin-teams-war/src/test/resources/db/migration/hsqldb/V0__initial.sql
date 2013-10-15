/*
 * ENGINE BLOCK TABLES FOR GROUP_PROVIDERs
 */

DROP TABLE IF EXISTS group_provider;
CREATE TABLE group_provider (
  id bigint generated by default as identity (start with 1),
  identifier varchar(255) NOT NULL,
  name varchar(255) NOT NULL,
  classname varchar(255) NOT NULL,
  logo_url varchar(1024) DEFAULT NULL,
  PRIMARY KEY (id)
);

INSERT INTO group_provider (id, identifier, name, classname, logo_url)
VALUES
  (1,'grouper','SURFteams grouper','EngineBlock_Group_Provider_Grouper','SURFteams grouper');

  
DROP TABLE IF EXISTS group_provider_decorator;
CREATE TABLE group_provider_decorator (
  id bigint generated by default as identity (start with 1),
  group_provider_id bigint NOT NULL,
  classname varchar(255)  NOT NULL,
  PRIMARY KEY (id)
);

INSERT INTO group_provider_decorator (id, group_provider_id, classname)
VALUES
  (1,1,'EngineBlock_Group_Provider_Decorator_GroupIdReplace');


DROP TABLE IF EXISTS group_provider_decorator_option;
CREATE TABLE group_provider_decorator_option (
  group_provider_decorator_id bigint NOT NULL,
  name varchar(255)  NOT NULL,
  value varchar(255)  NOT NULL,
  PRIMARY KEY (group_provider_decorator_id,name)
);

INSERT INTO group_provider_decorator_option (group_provider_decorator_id, name, value)
VALUES (1,'replace','$1');
INSERT INTO group_provider_decorator_option (group_provider_decorator_id, name, value)
VALUES (1,'search','|urn:collab:group:teams.demo.openconext.org:(.+)|');


  
DROP TABLE IF EXISTS group_provider_filter;
CREATE TABLE group_provider_filter (
  id bigint generated by default as identity (start with 1),
  group_provider_id bigint NOT NULL,
  type varchar(255)  NOT NULL,
  classname varchar(255)  NOT NULL,
  PRIMARY KEY (id)
);

INSERT INTO group_provider_filter (id, group_provider_id, type, classname)
VALUES
  (1,1,'group','EngineBlock_Group_Provider_Filter_ModelProperty_PregReplace');


DROP TABLE IF EXISTS group_provider_filter_option;
CREATE TABLE group_provider_filter_option (
  group_provider_filter_id bigint NOT NULL,
  name varchar(255)  NOT NULL,
  value varchar(255)  NOT NULL,
  PRIMARY KEY (group_provider_filter_id,name)
);

INSERT INTO group_provider_filter_option (group_provider_filter_id, name, value)
VALUES (1,'property','id');
INSERT INTO group_provider_filter_option (group_provider_filter_id, name, value)
VALUES (1,'replace','urn:collab:group:teams.demo.openconext.org:$1');
INSERT INTO group_provider_filter_option (group_provider_filter_id, name, value)
VALUES (1,'search','|(.+)|');

  
DROP TABLE IF EXISTS group_provider_option;
CREATE TABLE group_provider_option (
  group_provider_id bigint NOT NULL,
  name varchar(255)  NOT NULL,
  value varchar(255)  NOT NULL,
  PRIMARY KEY (group_provider_id,name)
);

INSERT INTO group_provider_option (group_provider_id, name, value)
VALUES (1,'host','grouper.demo.openconext.org');
INSERT INTO group_provider_option (group_provider_id, name, value)
VALUES (1,'password','KJ75DFeg32a');
INSERT INTO group_provider_option (group_provider_id, name, value)
VALUES (1,'path','/grouper-ws/servicesRest');
INSERT INTO group_provider_option (group_provider_id, name, value)
VALUES (1,'protocol','https');
INSERT INTO group_provider_option (group_provider_id, name, value)
VALUES (1,'timeout','10');
INSERT INTO group_provider_option (group_provider_id, name, value)
VALUES (1,'user','engine');
INSERT INTO group_provider_option (group_provider_id, name, value)
VALUES (1,'version','v1_6_000');


DROP TABLE IF EXISTS group_provider_precondition;
CREATE TABLE group_provider_precondition (
  id bigint generated by default as identity (start with 1),
  group_provider_id bigint NOT NULL,
  classname varchar(255)  DEFAULT NULL,
  PRIMARY KEY (id)
);


DROP TABLE IF EXISTS group_provider_precondition_option;
CREATE TABLE group_provider_precondition_option (
  group_provider_precondition_id bigint NOT NULL,
  name varchar(255)  NOT NULL,
  value varchar(255)  NOT NULL,
  PRIMARY KEY (group_provider_precondition_id,name)
);



DROP TABLE IF EXISTS group_provider_user_oauth;
CREATE TABLE group_provider_user_oauth (
  provider_id varchar(255)  NOT NULL,
  user_id varchar(255)  NOT NULL,
  oauth_token varchar(1024)  NOT NULL,
  oauth_secret varchar(1024)  NOT NULL,
  PRIMARY KEY (provider_id,user_id)
);

DROP TABLE IF EXISTS service_provider_group_acl;
CREATE TABLE service_provider_group_acl (
  id bigint generated by default as identity (start with 1),
  group_provider_id bigint NOT NULL,
  spentityid varchar(1024) NOT NULL,
  allow_groups boolean,
  allow_members boolean,
  PRIMARY KEY (id)
);

INSERT INTO service_provider_group_acl (group_provider_id, spentityid, allow_groups, allow_members)
VALUES (1, 'http://localhost:8060', true, true);


/*
 * TEAMS TABLES
 */
DROP TABLE IF EXISTS team_external_groups;
CREATE TABLE team_external_groups (
  id bigint generated by default as identity (start with 1),
  grouper_team_id varchar(255) DEFAULT NULL,
  external_groups_id bigint DEFAULT NULL,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS external_groups;
CREATE TABLE external_groups (
  id bigint generated by default as identity (start with 1),
  description varchar(1024),
  group_provider varchar(255) DEFAULT NULL,
  identifier varchar(255) DEFAULT NULL,
  name varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
);