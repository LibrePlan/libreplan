-- *********************************************************************
-- Update Database Script
-- *********************************************************************
-- Change Log: src/main/resources/db.changelog.xml
-- Ran at: 11/25/11 11:26 AM
-- Against: naval@jdbc:postgresql://localhost/navaldev
-- Liquibase version: 2.0-rc7
-- *********************************************************************

-- Lock Database
-- Changeset src/main/resources/db.changelog-1.1.xml::add-new-column-ldap-host::calvarinop::(Checksum: 3:7984328274b0af25bf454a71f709a0dc)
-- Add new column to store ldap host
ALTER TABLE configuration ADD ldap_host VARCHAR(255);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('calvarinop', 'Add new column to store ldap host', NOW(), 'Add Column', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'add-new-column-ldap-host', '2.0-rc7', '3:7984328274b0af25bf454a71f709a0dc', 341);

-- Changeset src/main/resources/db.changelog-1.1.xml::add-new-column-ldap-port::calvarinop::(Checksum: 3:a698fd66dc9b58c8e7df0ffafc6f7d1c)
-- Add new column to store ldap port
ALTER TABLE configuration ADD ldap_port VARCHAR(5);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('calvarinop', 'Add new column to store ldap port', NOW(), 'Add Column', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'add-new-column-ldap-port', '2.0-rc7', '3:a698fd66dc9b58c8e7df0ffafc6f7d1c', 342);

-- Changeset src/main/resources/db.changelog-1.1.xml::add-new-column-ldap-base::calvarinop::(Checksum: 3:be1cce8488649e3d266a5df219713071)
-- Add new column to store ldap base
ALTER TABLE configuration ADD ldap_base VARCHAR(255);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('calvarinop', 'Add new column to store ldap base', NOW(), 'Add Column', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'add-new-column-ldap-base', '2.0-rc7', '3:be1cce8488649e3d266a5df219713071', 343);

-- Changeset src/main/resources/db.changelog-1.1.xml::add-new-column-ldap-userdn::calvarinop::(Checksum: 3:c4c93711bd374e215a65f6f45b6a5f44)
-- Add new column to store ldap userdn
ALTER TABLE configuration ADD ldap_userdn VARCHAR(255);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('calvarinop', 'Add new column to store ldap userdn', NOW(), 'Add Column', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'add-new-column-ldap-userdn', '2.0-rc7', '3:c4c93711bd374e215a65f6f45b6a5f44', 344);

-- Changeset src/main/resources/db.changelog-1.1.xml::add-new-column-ldap-password::calvarinop::(Checksum: 3:22761f7b110aec44930935cb79f179d5)
-- Add new column to store ldap password
ALTER TABLE configuration ADD ldap_password VARCHAR(255);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('calvarinop', 'Add new column to store ldap password', NOW(), 'Add Column', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'add-new-column-ldap-password', '2.0-rc7', '3:22761f7b110aec44930935cb79f179d5', 345);

-- Changeset src/main/resources/db.changelog-1.1.xml::add-new-column-ldap-userid::calvarinop::(Checksum: 3:b628b315f6a3d2266cb6d280f9525f5f)
-- Add new column to store ldap userid
ALTER TABLE configuration ADD ldap_userid VARCHAR(255);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('calvarinop', 'Add new column to store ldap userid', NOW(), 'Add Column', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'add-new-column-ldap-userid', '2.0-rc7', '3:b628b315f6a3d2266cb6d280f9525f5f', 346);

-- Changeset src/main/resources/db.changelog-1.1.xml::add-new-column-ldap-save-password-db::calvarinop::(Checksum: 3:b4447892b37ee903664f9b5bd54e7a2e)
-- Add new column to store ldap passwords in database
ALTER TABLE configuration ADD ldap_save_password_db BOOLEAN;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('calvarinop', 'Add new column to store ldap passwords in database', NOW(), 'Add Column', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'add-new-column-ldap-save-password-db', '2.0-rc7', '3:b4447892b37ee903664f9b5bd54e7a2e', 347);

-- Changeset src/main/resources/db.changelog-1.1.xml::add-new-column-ldap-auth-enabled::calvarinop::(Checksum: 3:979a11f6d147433ec84e6165927683fa)
-- Add new column to store ldap authentication enabled
ALTER TABLE configuration ADD ldap_auth_enabled BOOLEAN;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('calvarinop', 'Add new column to store ldap authentication enabled', NOW(), 'Add Column', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'add-new-column-ldap-auth-enabled', '2.0-rc7', '3:979a11f6d147433ec84e6165927683fa', 348);

-- Changeset src/main/resources/db.changelog-1.1.xml::add-new-column-navalplan-user::idiazt::(Checksum: 3:76c2b18d10554416d9ca9055d3c2e768)
-- Add new column to store if it is a navalplan user
ALTER TABLE user_table ADD navalplan_user BOOLEAN;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('idiazt', 'Add new column to store if it is a navalplan user', NOW(), 'Add Column', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'add-new-column-navalplan-user', '2.0-rc7', '3:76c2b18d10554416d9ca9055d3c2e768', 349);

-- Changeset src/main/resources/db.changelog-1.1.xml::delete-constraint-not-null-user-password::idiazt::(Checksum: 3:0470d6d460842219443027cdb633232c)
-- Delete constraint not null for user password
ALTER TABLE user_table ALTER COLUMN  password DROP NOT NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('idiazt', 'Delete constraint not null for user password', NOW(), 'Drop Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'delete-constraint-not-null-user-password', '2.0-rc7', '3:0470d6d460842219443027cdb633232c', 350);

-- Changeset src/main/resources/db.changelog-1.1.xml::set-default-value-navalplan-user::idiazt::(Checksum: 3:8935d0ba839c252967d398f0db3ed01d)
ALTER TABLE user_table ALTER COLUMN  navalplan_user SET DEFAULT 'TRUE';

UPDATE user_table SET navalplan_user = 'TRUE' WHERE navalplan_user IS NULL;

ALTER TABLE user_table ALTER COLUMN  navalplan_user SET NOT NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('idiazt', '', NOW(), 'Add Default Value, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'set-default-value-navalplan-user', '2.0-rc7', '3:8935d0ba839c252967d398f0db3ed01d', 351);

-- Changeset src/main/resources/db.changelog-1.1.xml::set-default-value-ldap-save-password-db::idiazt::(Checksum: 3:6bcb9c9c00f066bc7ef5e568abf2da6f)
ALTER TABLE configuration ALTER COLUMN  ldap_save_password_db SET DEFAULT 'TRUE';

UPDATE configuration SET ldap_save_password_db = 'TRUE' WHERE ldap_save_password_db IS NULL;

ALTER TABLE configuration ALTER COLUMN  ldap_save_password_db SET NOT NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('idiazt', '', NOW(), 'Add Default Value, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'set-default-value-ldap-save-password-db', '2.0-rc7', '3:6bcb9c9c00f066bc7ef5e568abf2da6f', 352);

-- Changeset src/main/resources/db.changelog-1.1.xml::set-default-value-ldap-auth-enabled::idiazt::(Checksum: 3:987742cd9637874021c47ad9388655d2)
ALTER TABLE configuration ALTER COLUMN  ldap_auth_enabled SET DEFAULT 'FALSE';

UPDATE configuration SET ldap_auth_enabled = 'FALSE' WHERE ldap_auth_enabled IS NULL;

ALTER TABLE configuration ALTER COLUMN  ldap_auth_enabled SET NOT NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('idiazt', '', NOW(), 'Add Default Value, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'set-default-value-ldap-auth-enabled', '2.0-rc7', '3:987742cd9637874021c47ad9388655d2', 353);

-- Changeset src/main/resources/db.changelog-1.1.xml::add-new-column-ldap-save-roles-db::calvarinop::(Checksum: 3:e7fc821091b4d96786edf2aa98308f3b)
-- Add new column to store ldap roles in database
ALTER TABLE configuration ADD ldap_save_roles_db BOOLEAN;

ALTER TABLE configuration ALTER COLUMN  ldap_save_roles_db SET DEFAULT 'FALSE';

UPDATE configuration SET ldap_save_roles_db = 'FALSE' WHERE ldap_save_roles_db IS NULL;

ALTER TABLE configuration ALTER COLUMN  ldap_save_roles_db SET NOT NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('calvarinop', 'Add new column to store ldap roles in database', NOW(), 'Add Column, Add Default Value, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'add-new-column-ldap-save-roles-db', '2.0-rc7', '3:e7fc821091b4d96786edf2aa98308f3b', 354);

-- Changeset src/main/resources/db.changelog-1.1.xml::create-new-table-matching-roles::calvarinop::(Checksum: 3:5afc1ada9ec4ae6bfd66368c2512e145)
-- Add new column to store ldap role property
CREATE TABLE configuration_roles_ldap (role_libreplan VARCHAR(255) NOT NULL, role_ldap VARCHAR(255) NOT NULL, id_configuration BIGINT NOT NULL, role_matching_id INT NOT NULL, CONSTRAINT PK_CONFIGURATION_ROLES_LDAP PRIMARY KEY (role_libreplan, role_ldap));

ALTER TABLE configuration_roles_ldap ADD CONSTRAINT id_configuration_fkey FOREIGN KEY (id_configuration) REFERENCES configuration(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE configuration ADD ldap_group_path VARCHAR(255);

ALTER TABLE configuration ADD ldap_role_property VARCHAR(255);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('calvarinop', 'Add new column to store ldap role property', NOW(), 'Create Table, Add Foreign Key Constraint, Add Column (x2)', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'create-new-table-matching-roles', '2.0-rc7', '3:5afc1ada9ec4ae6bfd66368c2512e145', 355);

-- Changeset src/main/resources/db.changelog-1.1.xml::add-new-column-ldap-search-query::idiazt::(Checksum: 3:8e994641ada47323406f65d515ff00a0)
-- Add new column to store ldap role search query
ALTER TABLE configuration ADD ldap_search_query VARCHAR(255);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('idiazt', 'Add new column to store ldap role search query', NOW(), 'Add Column', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'add-new-column-ldap-search-query', '2.0-rc7', '3:8e994641ada47323406f65d515ff00a0', 356);

-- Changeset src/main/resources/db.changelog-1.1.xml::add-new-column-enabled-autocomplete-login::smontes::(Checksum: 3:0542da97fbff41b95f9cf5ef10ffa322)
-- Add new column enabled_autocomplete_login with default value TRUE to configuration table
ALTER TABLE configuration ADD enabled_autocomplete_login BOOLEAN;

ALTER TABLE configuration ALTER COLUMN  enabled_autocomplete_login SET DEFAULT 'TRUE';

UPDATE configuration SET enabled_autocomplete_login = 'TRUE' WHERE enabled_autocomplete_login IS NULL;

ALTER TABLE configuration ALTER COLUMN  enabled_autocomplete_login SET NOT NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', 'Add new column enabled_autocomplete_login with default value TRUE to configuration table', NOW(), 'Add Column, Add Default Value, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'add-new-column-enabled-autocomplete-login', '2.0-rc7', '3:0542da97fbff41b95f9cf5ef10ffa322', 357);

-- Changeset src/main/resources/db.changelog-1.1.xml::add-application-language::calvarinop::(Checksum: 3:f451e552cb7903c699c88ecde02e85c6)
-- Add new column to store the language of application for this user
ALTER TABLE user_table ADD application_language INT DEFAULT '0';

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('calvarinop', 'Add new column to store the language of application for this user', NOW(), 'Add Column', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'add-application-language', '2.0-rc7', '3:f451e552cb7903c699c88ecde02e85c6', 358);

-- Changeset src/main/resources/db.changelog-1.1.xml::move-columns-from-configuration-table::calvarinop::(Checksum: 3:7243259227df39ef2f0683d4b425d369)
-- Move columns from configuration table to user table
ALTER TABLE user_table ADD expand_company_planning_view_charts BOOLEAN;

ALTER TABLE user_table ALTER COLUMN  expand_company_planning_view_charts SET DEFAULT 'FALSE';

UPDATE user_table SET expand_company_planning_view_charts = 'FALSE' WHERE expand_company_planning_view_charts IS NULL;

ALTER TABLE user_table ALTER COLUMN  expand_company_planning_view_charts SET NOT NULL;

ALTER TABLE user_table ADD expand_order_planning_view_charts BOOLEAN;

ALTER TABLE user_table ALTER COLUMN  expand_order_planning_view_charts SET DEFAULT 'TRUE';

UPDATE user_table SET expand_order_planning_view_charts = 'TRUE' WHERE expand_order_planning_view_charts IS NULL;

ALTER TABLE user_table ALTER COLUMN  expand_order_planning_view_charts SET NOT NULL;

ALTER TABLE user_table ADD expand_resource_load_view_charts BOOLEAN;

ALTER TABLE user_table ALTER COLUMN  expand_resource_load_view_charts SET DEFAULT 'TRUE';

UPDATE user_table SET expand_resource_load_view_charts = 'TRUE' WHERE expand_resource_load_view_charts IS NULL;

ALTER TABLE user_table ALTER COLUMN  expand_resource_load_view_charts SET NOT NULL;

ALTER TABLE configuration DROP COLUMN expand_company_planning_view_charts;

ALTER TABLE configuration DROP COLUMN expand_order_planning_view_charts;

ALTER TABLE configuration DROP COLUMN expand_resource_load_view_charts;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('calvarinop', 'Move columns from configuration table to user table', NOW(), 'Add Column, Add Default Value, Add Not-Null Constraint, Add Column, Add Default Value, Add Not-Null Constraint, Add Column, Add Default Value, Add Not-Null Constraint, Drop Column (x3)', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'move-columns-from-configuration-table', '2.0-rc7', '3:7243259227df39ef2f0683d4b425d369', 359);

-- Changeset src/main/resources/db.changelog-1.1.xml::add-new-fields::calvarinop::(Checksum: 3:6950443e7dca6f2af1e7d095abde62bc)
-- Add new column to store the first and last name for this user
ALTER TABLE user_table ADD first_name VARCHAR(255);

ALTER TABLE user_table ADD last_name VARCHAR(255);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('calvarinop', 'Add new column to store the first and last name for this user', NOW(), 'Add Column (x2)', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'add-new-fields', '2.0-rc7', '3:6950443e7dca6f2af1e7d095abde62bc', 360);

-- Changeset src/main/resources/db.changelog-1.1.xml::remove-configuration_roles_ldap::mrego::(Checksum: 3:88dacdfabdf225e6ed8b25e4807afeb8)
-- Remove column configuration_roles_ldap in role_matching_id
ALTER TABLE configuration_roles_ldap DROP COLUMN role_matching_id;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Remove column configuration_roles_ldap in role_matching_id', NOW(), 'Drop Column', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'remove-configuration_roles_ldap', '2.0-rc7', '3:88dacdfabdf225e6ed8b25e4807afeb8', 361);

-- Changeset src/main/resources/db.changelog-1.1.xml::add-column-progress_all_by_num_hours-in-planning_data::mrego::(Checksum: 3:9410a670cf6cccd21638c8eddb6b8b36)
-- Add column progress_all_by_num_hours in planning_data
ALTER TABLE planning_data ADD progress_all_by_num_hours numeric(19,6);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Add column progress_all_by_num_hours in planning_data', NOW(), 'Add Column', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'add-column-progress_all_by_num_hours-in-planning_data', '2.0-rc7', '3:9410a670cf6cccd21638c8eddb6b8b36', 362);

-- Changeset src/main/resources/db.changelog-1.1.xml::update-color-in-calendar_exception_type-to-default::mrego::(Checksum: 3:2f5f94374b2021dca7bc4d8245807ae4)
-- Update color in calendar_exception_type to DEFAULT
UPDATE calendar_exception_type SET color = 'DEFAULT';

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Update color in calendar_exception_type to DEFAULT', NOW(), 'Update Data', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'update-color-in-calendar_exception_type-to-default', '2.0-rc7', '3:2f5f94374b2021dca7bc4d8245807ae4', 363);

-- Changeset src/main/resources/db.changelog-1.1.xml::create-table-manual_function::mrego::(Checksum: 3:aac963ac8a7a63814472c50edf8b367a)
CREATE TABLE manual_function (assignment_function_id BIGINT NOT NULL, CONSTRAINT manual_function_pkey PRIMARY KEY (assignment_function_id));

ALTER TABLE manual_function ADD CONSTRAINT mnual_function_assignment_function_fkey FOREIGN KEY (assignment_function_id) REFERENCES assignment_function(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table, Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'create-table-manual_function', '2.0-rc7', '3:aac963ac8a7a63814472c50edf8b367a', 364);

-- Changeset src/main/resources/db.changelog-1.1.xml::change-numhours-to-effort-in-work-report-lines::idiazt::(Checksum: 3:824cfe1ec43c490e63bf2d8316f1255e)
-- Changing work_report_line numHours to effort
ALTER TABLE work_report_line RENAME COLUMN num_hours TO effort;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('idiazt', 'Changing work_report_line numHours to effort', NOW(), 'Rename Column', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'change-numhours-to-effort-in-work-report-lines', '2.0-rc7', '3:824cfe1ec43c490e63bf2d8316f1255e', 365);

-- Changeset src/main/resources/db.changelog-1.1.xml::update-numhours-values-to-effort-values::idiazt::(Checksum: 3:79a9659b3a77004c5fe13bbec8aad88c)
-- Updating numHours to effort (hours to seconds)
UPDATE work_report_line
            SET effort = effort*3600;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('idiazt', 'Updating numHours to effort (hours to seconds)', NOW(), 'Custom SQL', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'update-numhours-values-to-effort-values', '2.0-rc7', '3:79a9659b3a77004c5fe13bbec8aad88c', 366);

-- Changeset src/main/resources/db.changelog-1.1.xml::change-sum_charged_hours-to-sum_charged_effort::mrego::(Checksum: 3:202833e32d386f5a3edf903b2d5a21b4)
-- Changing sum_charged_hours to sum_charged_effort
ALTER TABLE sum_charged_hours RENAME TO sum_charged_effort;

ALTER TABLE sum_charged_effort RENAME COLUMN direct_charged_hours TO direct_charged_effort;

ALTER TABLE sum_charged_effort RENAME COLUMN indirect_charged_hours TO indirect_charged_effort;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Changing sum_charged_hours to sum_charged_effort', NOW(), 'Rename Table, Rename Column (x2)', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'change-sum_charged_hours-to-sum_charged_effort', '2.0-rc7', '3:202833e32d386f5a3edf903b2d5a21b4', 367);

-- Changeset src/main/resources/db.changelog-1.1.xml::update-effort-values-in-sum_charged_effort::mrego::(Checksum: 3:6d7d2103e92b3ac55b9658931fc3ed65)
-- Updating effort values (hours to seconds) in sum_charged_effort table
UPDATE sum_charged_effort
            SET direct_charged_effort = direct_charged_effort*3600;

UPDATE sum_charged_effort
            SET indirect_charged_effort = indirect_charged_effort*3600;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Updating effort values (hours to seconds) in sum_charged_effort table', NOW(), 'Custom SQL (x2)', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'update-effort-values-in-sum_charged_effort', '2.0-rc7', '3:6d7d2103e92b3ac55b9658931fc3ed65', 368);

-- Changeset src/main/resources/db.changelog-1.1.xml::drop-foreign-key-sum_charged_hours_id-in-order_element::dmel::(Checksum: 3:288d757a697e34248fbb5dab4177c4a0)
ALTER TABLE order_element DROP CONSTRAINT fk92271f0b7ec17fa6;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('dmel', '', NOW(), 'Drop Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'drop-foreign-key-sum_charged_hours_id-in-order_element', '2.0-rc7', '3:288d757a697e34248fbb5dab4177c4a0', 369);

-- Changeset src/main/resources/db.changelog-1.1.xml::rename-sum_charged_hours_id-to-sum_charged_effort_id::mrego::(Checksum: 3:f8140dac93702c25f7fe75d9217ef9c5)
-- Rename sum_charged_hours_id to sum_charged_effort_id in order_element
ALTER TABLE order_element RENAME COLUMN sum_charged_hours_id TO sum_charged_effort_id;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Rename sum_charged_hours_id to sum_charged_effort_id in order_element', NOW(), 'Rename Column', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'rename-sum_charged_hours_id-to-sum_charged_effort_id', '2.0-rc7', '3:f8140dac93702c25f7fe75d9217ef9c5', 370);

-- Changeset src/main/resources/db.changelog-1.1.xml::add-foreign-key-sum_charged_effort_id-in-order_element::dmel::(Checksum: 3:a8fa898f9cfaa1afa3afbab4c1c9f82b)
ALTER TABLE order_element ADD CONSTRAINT sum_charged_effort_id_fkey FOREIGN KEY (sum_charged_effort_id) REFERENCES sum_charged_effort(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('dmel', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'add-foreign-key-sum_charged_effort_id-in-order_element', '2.0-rc7', '3:a8fa898f9cfaa1afa3afbab4c1c9f82b', 371);

-- Changeset src/main/resources/db.changelog-1.1.xml::drop-column-date-in-stretches-table::mrego::(Checksum: 3:e0ed365c168583ff5c4c81c9ac79d644)
ALTER TABLE stretches DROP COLUMN date;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Drop Column', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'drop-column-date-in-stretches-table', '2.0-rc7', '3:e0ed365c168583ff5c4c81c9ac79d644', 372);

-- Changeset src/main/resources/db.changelog-1.1.xml::add-version-to-planning-data::ogonzalez::(Checksum: 3:ae9dca1ca9cb3556cf5efdca689b8fc1)
ALTER TABLE planning_data ADD version BIGINT NOT NULL DEFAULT '0';

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('ogonzalez', '', NOW(), 'Add Column', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'add-version-to-planning-data', '2.0-rc7', '3:ae9dca1ca9cb3556cf5efdca689b8fc1', 373);

-- Changeset src/main/resources/db.changelog-1.1.xml::change-navalplan_user-to-libreplan_user-in-user_table::mrego::(Checksum: 3:82c71f38665f14d5d8ef9391a5c2487d)
-- Changing navalplan_user to libreplan_user in user_table
ALTER TABLE user_table RENAME COLUMN navalplan_user TO libreplan_user;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Changing navalplan_user to libreplan_user in user_table', NOW(), 'Rename Column', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'change-navalplan_user-to-libreplan_user-in-user_table', '2.0-rc7', '3:82c71f38665f14d5d8ef9391a5c2487d', 374);

-- Changeset src/main/resources/db.changelog-1.1.xml::add-new-column-ldap-role-strategy::idiazt::(Checksum: 3:3145034f8fd26477ae1fac7da835a43e)
-- Add new column to store ldap role strategy
ALTER TABLE configuration ADD ldap_group_strategy BOOLEAN;

ALTER TABLE configuration ALTER COLUMN  ldap_group_strategy SET DEFAULT 'TRUE';

UPDATE configuration SET ldap_group_strategy = 'TRUE' WHERE ldap_group_strategy IS NULL;

ALTER TABLE configuration ALTER COLUMN  ldap_group_strategy SET NOT NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('idiazt', 'Add new column to store ldap role strategy', NOW(), 'Add Column, Add Default Value, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.1.xml', 'add-new-column-ldap-role-strategy', '2.0-rc7', '3:3145034f8fd26477ae1fac7da835a43e', 375);

-- Release Database Lock
-- Release Database Lock
