-- *********************************************************************
-- Update Database Script
-- *********************************************************************
-- Change Log: src/main/resources/db.changelog.xml
-- Ran at: 12/21/10 10:10 AM
-- Against: naval@jdbc:postgresql://localhost/navaldev
-- Liquibase version: 2.0-rc7
-- *********************************************************************

-- Create Database Lock Table
CREATE TABLE databasechangeloglock (ID INT NOT NULL, LOCKED BOOLEAN NOT NULL, LOCKGRANTED TIMESTAMP WITH TIME ZONE, LOCKEDBY VARCHAR(255), CONSTRAINT PK_DATABASECHANGELOGLOCK PRIMARY KEY (ID));

INSERT INTO databasechangeloglock (ID, LOCKED) VALUES (1, FALSE);

-- Lock Database
-- Create Database Change Log Table
CREATE TABLE databasechangelog (ID VARCHAR(63) NOT NULL, AUTHOR VARCHAR(63) NOT NULL, FILENAME VARCHAR(200) NOT NULL, DATEEXECUTED TIMESTAMP WITH TIME ZONE NOT NULL, ORDEREXECUTED INT NOT NULL, EXECTYPE VARCHAR(10) NOT NULL, MD5SUM VARCHAR(35), DESCRIPTION VARCHAR(255), COMMENTS VARCHAR(255), TAG VARCHAR(255), LIQUIBASE VARCHAR(20), CONSTRAINT PK_DATABASECHANGELOG PRIMARY KEY (ID, AUTHOR, FILENAME));

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-1::mrego::(Checksum: 3:0819191bfc79c85258e53388e6c3a269)
CREATE TABLE advance_assignment (id BIGINT NOT NULL, version BIGINT NOT NULL, report_global_advance BOOLEAN, advance_type_id BIGINT, CONSTRAINT advance_assignment_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-1', '2.0-rc7', '3:0819191bfc79c85258e53388e6c3a269', 1);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-2::mrego::(Checksum: 3:492e699609703ebfdc7f14d0b9d3fa10)
CREATE TABLE advance_assignment_template (id BIGINT NOT NULL, version BIGINT NOT NULL, advance_type_id BIGINT, order_element_template_id BIGINT, report_global_advance BOOLEAN, max_value DECIMAL(19,2), CONSTRAINT advance_assignment_template_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-2', '2.0-rc7', '3:492e699609703ebfdc7f14d0b9d3fa10', 2);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-3::mrego::(Checksum: 3:ce0671ca8c9c310243bcc2ffb2d35523)
CREATE TABLE advance_measurement (id BIGINT NOT NULL, version BIGINT NOT NULL, date DATE, value DECIMAL(19,2), advance_assignment_id BIGINT, communication_date TIMESTAMP WITH TIME ZONE, CONSTRAINT advance_measurement_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-3', '2.0-rc7', '3:ce0671ca8c9c310243bcc2ffb2d35523', 3);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-4::mrego::(Checksum: 3:5a28bfc5ca316e6f6cd3a0b3ef5f67b9)
CREATE TABLE advance_type (id BIGINT NOT NULL, version BIGINT NOT NULL, unit_name VARCHAR(255), default_max_value DECIMAL(19,4), updatable BOOLEAN, unit_precision DECIMAL(19,4), active BOOLEAN, percentage BOOLEAN, quality_form BOOLEAN, CONSTRAINT advance_type_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-4', '2.0-rc7', '3:5a28bfc5ca316e6f6cd3a0b3ef5f67b9', 4);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-5::mrego::(Checksum: 3:dd32a9e5c58bd33f528f3a4176736205)
CREATE TABLE all_criterions (generic_resource_allocation_id BIGINT NOT NULL, criterion_id BIGINT NOT NULL);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-5', '2.0-rc7', '3:dd32a9e5c58bd33f528f3a4176736205', 5);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-6::mrego::(Checksum: 3:de107aebd954ab5c29c39bd6260b1c69)
CREATE TABLE assignment_function (id BIGINT NOT NULL, version BIGINT NOT NULL, CONSTRAINT assignment_function_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-6', '2.0-rc7', '3:de107aebd954ab5c29c39bd6260b1c69', 6);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-7::mrego::(Checksum: 3:588d5677bcc5dff757c327873f896443)
CREATE TABLE base_calendar (id BIGINT NOT NULL, version BIGINT NOT NULL, code VARCHAR(255) NOT NULL, name VARCHAR(255), code_autogenerated BOOLEAN, last_sequence_code INT, CONSTRAINT base_calendar_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-7', '2.0-rc7', '3:588d5677bcc5dff757c327873f896443', 7);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-8::mrego::(Checksum: 3:7fa824a67c2de27b2b2a3f66e5118412)
CREATE TABLE calendar_availability (id BIGINT NOT NULL, version BIGINT NOT NULL, code VARCHAR(255) NOT NULL, start_date DATE, end_date DATE, base_calendar_id BIGINT, position_in_calendar INT, CONSTRAINT calendar_availability_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-8', '2.0-rc7', '3:7fa824a67c2de27b2b2a3f66e5118412', 8);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-9::mrego::(Checksum: 3:93bc8fa1188ddda3969df439f29c8f31)
CREATE TABLE calendar_data (id BIGINT NOT NULL, version BIGINT NOT NULL, code VARCHAR(255) NOT NULL, parent BIGINT, expiring_date DATE, base_calendar_id BIGINT, position_in_calendar INT, CONSTRAINT calendar_data_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-9', '2.0-rc7', '3:93bc8fa1188ddda3969df439f29c8f31', 9);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-10::mrego::(Checksum: 3:0c72c8c2330c2408a6b079239c401a91)
CREATE TABLE calendar_exception (id BIGINT NOT NULL, version BIGINT NOT NULL, code VARCHAR(255) NOT NULL, date DATE, duration INT, calendar_exception_id BIGINT, base_calendar_id BIGINT, CONSTRAINT calendar_exception_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-10', '2.0-rc7', '3:0c72c8c2330c2408a6b079239c401a91', 10);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-11::mrego::(Checksum: 3:30ecc1c237e1c3c3db800537ffa8883d)
CREATE TABLE calendar_exception_type (id BIGINT NOT NULL, version BIGINT NOT NULL, code VARCHAR(255) NOT NULL, code_autogenerated BOOLEAN NOT NULL, name VARCHAR(255), color VARCHAR(255), not_assignable BOOLEAN, duration INT, CONSTRAINT calendar_exception_type_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-11', '2.0-rc7', '3:30ecc1c237e1c3c3db800537ffa8883d', 11);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-12::mrego::(Checksum: 3:f3c4e20ee90e3380f608f73bd0bbb630)
CREATE TABLE configuration (id BIGINT NOT NULL, version BIGINT NOT NULL, configuration_id BIGINT, company_code VARCHAR(255), generate_code_for_cost_category BOOLEAN NOT NULL, generate_code_for_calendar_exception_type BOOLEAN NOT NULL, generate_code_for_work_report_type BOOLEAN NOT NULL, generate_code_for_criterion BOOLEAN NOT NULL, generate_code_for_label BOOLEAN NOT NULL, generate_code_for_work_report BOOLEAN NOT NULL, generate_code_for_resources BOOLEAN NOT NULL, generate_code_for_types_of_work_hours BOOLEAN NOT NULL, generate_code_for_material_categories BOOLEAN NOT NULL, generate_code_for_unit_types BOOLEAN NOT NULL, generate_code_for_base_calendars BOOLEAN NOT NULL, expand_company_planning_view_charts BOOLEAN NOT NULL, expand_order_planning_view_charts BOOLEAN NOT NULL, expand_resource_load_view_charts BOOLEAN NOT NULL, monte_carlo_method_tab_visible BOOLEAN NOT NULL, progress_type INT, CONSTRAINT configuration_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-12', '2.0-rc7', '3:f3c4e20ee90e3380f608f73bd0bbb630', 12);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-13::mrego::(Checksum: 3:aaaa113557606faf71ee9f5ee1df07ce)
CREATE TABLE consolidated_value (id BIGINT NOT NULL, consolidated_value_type VARCHAR(255) NOT NULL, version BIGINT NOT NULL, date DATE, value DECIMAL(19,2), task_end_date DATE, consolidation_id BIGINT, advance_measurement_id BIGINT, CONSTRAINT consolidated_value_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-13', '2.0-rc7', '3:aaaa113557606faf71ee9f5ee1df07ce', 13);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-14::mrego::(Checksum: 3:54c846355565319d7d50bd78f4f569bb)
CREATE TABLE consolidation (id BIGINT NOT NULL, consolidation_type VARCHAR(255) NOT NULL, version BIGINT NOT NULL, dir_advance_assignment_id BIGINT, ind_advance_assignment_id BIGINT, CONSTRAINT consolidation_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-14', '2.0-rc7', '3:54c846355565319d7d50bd78f4f569bb', 14);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-15::mrego::(Checksum: 3:206376e864171d400ce1602f7ef53d49)
CREATE TABLE cost_category (id BIGINT NOT NULL, version BIGINT NOT NULL, code VARCHAR(255) NOT NULL, name VARCHAR(255), code_autogenerated BOOLEAN NOT NULL, last_hour_cost_sequence_code INT NOT NULL, enabled BOOLEAN, CONSTRAINT cost_category_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-15', '2.0-rc7', '3:206376e864171d400ce1602f7ef53d49', 15);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-16::mrego::(Checksum: 3:cf757d69a753e856b6342f679cff9fac)
CREATE TABLE criterion (id BIGINT NOT NULL, version BIGINT NOT NULL, code VARCHAR(255) NOT NULL, name VARCHAR(255), predefined_criterion_internal_name VARCHAR(255), active BOOLEAN, id_criterion_type BIGINT NOT NULL, parent BIGINT, CONSTRAINT criterion_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-16', '2.0-rc7', '3:cf757d69a753e856b6342f679cff9fac', 16);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-17::mrego::(Checksum: 3:ce6dfb988cb41f0adbe43e1855caa76e)
CREATE TABLE criterion_requirement (id BIGINT NOT NULL, criterion_requirement_type VARCHAR(255) NOT NULL, version BIGINT NOT NULL, hours_group_id BIGINT, order_element_id BIGINT, order_element_template_id BIGINT, criterion_id BIGINT, parent BIGINT, valid BOOLEAN, CONSTRAINT criterion_requirement_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-17', '2.0-rc7', '3:ce6dfb988cb41f0adbe43e1855caa76e', 17);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-18::mrego::(Checksum: 3:967c04d80a2d485f868b3c984579c4e8)
CREATE TABLE criterion_satisfaction (id BIGINT NOT NULL, version BIGINT NOT NULL, code VARCHAR(255) NOT NULL, start_date TIMESTAMP WITH TIME ZONE NOT NULL, finish_date TIMESTAMP WITH TIME ZONE, is_deleted BOOLEAN, criterion BIGINT NOT NULL, resource BIGINT NOT NULL, CONSTRAINT criterion_satisfaction_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-18', '2.0-rc7', '3:967c04d80a2d485f868b3c984579c4e8', 18);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-19::mrego::(Checksum: 3:4e9058f3e2f5b8173503167ec5c538a3)
CREATE TABLE criterion_type (id BIGINT NOT NULL, version BIGINT NOT NULL, code VARCHAR(255) NOT NULL, name VARCHAR(255), predefined_type_internal_name VARCHAR(255), description VARCHAR(255), allow_simultaneous_criterions_per_resource BOOLEAN, allow_hierarchy BOOLEAN, last_criterion_sequence_code INT, enabled BOOLEAN, code_autogenerated BOOLEAN NOT NULL, resource INT, CONSTRAINT criterion_type_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-19', '2.0-rc7', '3:4e9058f3e2f5b8173503167ec5c538a3', 19);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-20::mrego::(Checksum: 3:60a882fa6c0f1d848bf4c69f9bfff35c)
CREATE TABLE day_assignment (id BIGINT NOT NULL, day_assignment_type VARCHAR(255) NOT NULL, version BIGINT NOT NULL, duration INT NOT NULL, consolidated BOOLEAN, day DATE NOT NULL, resource_id BIGINT NOT NULL, specific_container_id BIGINT, generic_container_id BIGINT, derived_container_id BIGINT, CONSTRAINT day_assignment_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-20', '2.0-rc7', '3:60a882fa6c0f1d848bf4c69f9bfff35c', 20);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-21::mrego::(Checksum: 3:19b23f729aa196a1abed154f225db676)
CREATE TABLE dependency (id BIGINT NOT NULL, version BIGINT NOT NULL, origin BIGINT, destination BIGINT, queue_dependency BIGINT, type INT, CONSTRAINT dependency_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-21', '2.0-rc7', '3:19b23f729aa196a1abed154f225db676', 21);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-22::mrego::(Checksum: 3:947b787c1bbe91a331dd46cb1640f5e5)
CREATE TABLE derived_allocation (id BIGINT NOT NULL, version BIGINT NOT NULL, resource_allocation_id BIGINT, configurationunit BIGINT NOT NULL, CONSTRAINT derived_allocation_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-22', '2.0-rc7', '3:947b787c1bbe91a331dd46cb1640f5e5', 22);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-23::mrego::(Checksum: 3:48f908ec702ae89b5da9f0eac7479005)
CREATE TABLE derived_day_assignments_container (id BIGINT NOT NULL, version BIGINT NOT NULL, derived_allocation_id BIGINT, scenario BIGINT, CONSTRAINT derived_day_assignments_container_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-23', '2.0-rc7', '3:48f908ec702ae89b5da9f0eac7479005', 23);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-24::mrego::(Checksum: 3:84355bc36e649a2786237afa2239406b)
CREATE TABLE description_values (description_value_id BIGINT NOT NULL, fieldname VARCHAR(255), value VARCHAR(255));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-24', '2.0-rc7', '3:84355bc36e649a2786237afa2239406b', 24);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-25::mrego::(Checksum: 3:b8724c07bf0377b89a789f4068225748)
CREATE TABLE description_values_in_line (description_value_id BIGINT NOT NULL, field_name VARCHAR(255), value VARCHAR(255));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-25', '2.0-rc7', '3:b8724c07bf0377b89a789f4068225748', 25);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-26::mrego::(Checksum: 3:f9826236b01812c13b8773d5ab43eba3)
CREATE TABLE direct_advance_assignment (advance_assignment_id BIGINT NOT NULL, direct_order_element_id BIGINT, max_value DECIMAL(19,2), CONSTRAINT direct_advance_assignment_pkey PRIMARY KEY (advance_assignment_id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-26', '2.0-rc7', '3:f9826236b01812c13b8773d5ab43eba3', 26);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-27::mrego::(Checksum: 3:c793cee4a905e7c283e5670c7a78ce75)
CREATE TABLE effort_per_day (base_calendar_id BIGINT NOT NULL, effort INT, day_id INT NOT NULL);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-27', '2.0-rc7', '3:c793cee4a905e7c283e5670c7a78ce75', 27);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-28::mrego::(Checksum: 3:10e53ad074ddb8a722b7327c489af2a4)
CREATE TABLE entity_sequence (id BIGINT NOT NULL, version BIGINT NOT NULL, entity_name INT, prefix VARCHAR(255), last_value INT, number_of_digits INT, active BOOLEAN, CONSTRAINT entity_sequence_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-28', '2.0-rc7', '3:10e53ad074ddb8a722b7327c489af2a4', 28);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-29::mrego::(Checksum: 3:ffd33e338772b05e1ec66bc5f536b857)
CREATE TABLE external_company (id BIGINT NOT NULL, version BIGINT NOT NULL, name VARCHAR(255), nif VARCHAR(255), client BOOLEAN, subcontractor BOOLEAN, interacts_with_applications BOOLEAN, app_uri VARCHAR(255), our_company_login VARCHAR(255), our_company_password VARCHAR(255), company_user BIGINT, CONSTRAINT external_company_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-29', '2.0-rc7', '3:ffd33e338772b05e1ec66bc5f536b857', 29);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-30::mrego::(Checksum: 3:de8601efc174c9011c267a989abc558d)
CREATE TABLE generic_day_assignments_container (id BIGINT NOT NULL, version BIGINT NOT NULL, resource_allocation_id BIGINT, scenario BIGINT, start_date DATE, duration_start_in_first_day INT, end_date DATE, duration_in_last_day INT, CONSTRAINT generic_day_assignments_container_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-30', '2.0-rc7', '3:de8601efc174c9011c267a989abc558d', 30);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-31::mrego::(Checksum: 3:fc5ec26502de974f7ab46118f4f53196)
CREATE TABLE generic_resource_allocation (resource_allocation_id BIGINT NOT NULL, resource_type INT, CONSTRAINT generic_resource_allocation_pkey PRIMARY KEY (resource_allocation_id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-31', '2.0-rc7', '3:fc5ec26502de974f7ab46118f4f53196', 31);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-32::mrego::(Checksum: 3:b3be37758841b13194d3710d6defef04)
CREATE TABLE heading_field (heading_id BIGINT NOT NULL, fieldname VARCHAR(255), length INT, positionnumber INT);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-32', '2.0-rc7', '3:b3be37758841b13194d3710d6defef04', 32);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-33::mrego::(Checksum: 3:a951462763f5d55a3f7e10f12f81335f)
CREATE TABLE hour_cost (id BIGINT NOT NULL, version BIGINT NOT NULL, code VARCHAR(255) NOT NULL, price_cost DECIMAL(19,2), init_date DATE, end_date DATE, type_of_work_hours_id BIGINT, cost_category_id BIGINT, CONSTRAINT hour_cost_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-33', '2.0-rc7', '3:a951462763f5d55a3f7e10f12f81335f', 33);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-34::mrego::(Checksum: 3:a51224ce4bb0ca6d2b3f7bd992c65f0a)
CREATE TABLE hours_group (id BIGINT NOT NULL, version BIGINT NOT NULL, code VARCHAR(255) NOT NULL, resource_type VARCHAR(255), working_hours INT NOT NULL, percentage DECIMAL(19,2), fixed_percentage BOOLEAN, parent_order_line BIGINT, order_line_template BIGINT, CONSTRAINT hours_group_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-34', '2.0-rc7', '3:a51224ce4bb0ca6d2b3f7bd992c65f0a', 34);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-35::mrego::(Checksum: 3:8afd863ae4753040b121f5858594660b)
CREATE TABLE indirect_advance_assignment (advance_assignment_id BIGINT NOT NULL, indirect_order_element_id BIGINT, CONSTRAINT indirect_advance_assignment_pkey PRIMARY KEY (advance_assignment_id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-35', '2.0-rc7', '3:8afd863ae4753040b121f5858594660b', 35);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-36::mrego::(Checksum: 3:0754fc448f05289e922e36fe342d121d)
CREATE TABLE label (id BIGINT NOT NULL, version BIGINT NOT NULL, code VARCHAR(255) NOT NULL, name VARCHAR(255), label_type_id BIGINT, CONSTRAINT label_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-36', '2.0-rc7', '3:0754fc448f05289e922e36fe342d121d', 36);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-37::mrego::(Checksum: 3:5cfa72b6d860176124f25ff3a9d331fc)
CREATE TABLE label_type (id BIGINT NOT NULL, version BIGINT NOT NULL, code VARCHAR(255) NOT NULL, name VARCHAR(255), code_autogenerated BOOLEAN NOT NULL, last_label_sequence_code INT, CONSTRAINT label_type_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-37', '2.0-rc7', '3:5cfa72b6d860176124f25ff3a9d331fc', 37);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-38::mrego::(Checksum: 3:f831e87d1de1fc1338d42b8e833dc9c9)
CREATE TABLE limiting_resource_queue (id BIGINT NOT NULL, version BIGINT NOT NULL, resource_id BIGINT, CONSTRAINT limiting_resource_queue_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-38', '2.0-rc7', '3:f831e87d1de1fc1338d42b8e833dc9c9', 38);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-39::mrego::(Checksum: 3:6bd033fff9beeafa67d07445dbaee07f)
CREATE TABLE limiting_resource_queue_dependency (id BIGINT NOT NULL, type INT, origin_queue_element_id BIGINT, destiny_queue_element_id BIGINT, CONSTRAINT limiting_resource_queue_dependency_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-39', '2.0-rc7', '3:6bd033fff9beeafa67d07445dbaee07f', 39);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-40::mrego::(Checksum: 3:bdd3c4d65620a2a8f7b1a964a353f58f)
CREATE TABLE limiting_resource_queue_element (id BIGINT NOT NULL, version BIGINT NOT NULL, resource_allocation_id BIGINT, limiting_resource_queue_id BIGINT, earlier_start_date_because_of_gantt TIMESTAMP WITH TIME ZONE, earliest_end_date_because_of_gantt TIMESTAMP WITH TIME ZONE, creation_timestamp BIGINT, start_date DATE, start_hour INT, end_date DATE, end_hour INT, CONSTRAINT limiting_resource_queue_element_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-40', '2.0-rc7', '3:bdd3c4d65620a2a8f7b1a964a353f58f', 40);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-41::mrego::(Checksum: 3:096d3c30326f7805f4b0039355445d54)
CREATE TABLE line_field (heading_id BIGINT NOT NULL, fieldname VARCHAR(255), length INT, positionnumber INT);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-41', '2.0-rc7', '3:096d3c30326f7805f4b0039355445d54', 41);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-42::mrego::(Checksum: 3:ded3fb067f8bc57e6638e6920fd44604)
CREATE TABLE machine (machine_id BIGINT NOT NULL, name VARCHAR(255), description VARCHAR(255), CONSTRAINT machine_pkey PRIMARY KEY (machine_id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-42', '2.0-rc7', '3:ded3fb067f8bc57e6638e6920fd44604', 42);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-43::mrego::(Checksum: 3:55f995d5648794d3432adc45e49630ac)
CREATE TABLE machine_configuration_unit_required_criterions (id BIGINT NOT NULL, criterion_id BIGINT NOT NULL);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-43', '2.0-rc7', '3:55f995d5648794d3432adc45e49630ac', 43);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-44::mrego::(Checksum: 3:36bec1cf129dbbe7479b6fd37ec4cfc8)
CREATE TABLE machine_worker_assignment (id BIGINT NOT NULL, version BIGINT NOT NULL, start_date TIMESTAMP WITH TIME ZONE, finish_date TIMESTAMP WITH TIME ZONE, configuration_id BIGINT NOT NULL, worker_id BIGINT, CONSTRAINT machine_worker_assignment_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-44', '2.0-rc7', '3:36bec1cf129dbbe7479b6fd37ec4cfc8', 44);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-45::mrego::(Checksum: 3:14bba3e0f5ceb3f2fd6f8957e1838de4)
CREATE TABLE machine_workers_configuration_unit (id BIGINT NOT NULL, version BIGINT NOT NULL, name VARCHAR(255) NOT NULL, alpha DECIMAL(19,2) NOT NULL, machine BIGINT NOT NULL, CONSTRAINT machine_workers_configuration_unit_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-45', '2.0-rc7', '3:14bba3e0f5ceb3f2fd6f8957e1838de4', 45);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-46::mrego::(Checksum: 3:20a4b538a58ecad5fefff00c68060415)
CREATE TABLE material (id BIGINT NOT NULL, version BIGINT NOT NULL, code VARCHAR(255) NOT NULL, description VARCHAR(255), default_unit_price DECIMAL(19,2), unit_type BIGINT, disabled BOOLEAN, category_id BIGINT, CONSTRAINT material_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-46', '2.0-rc7', '3:20a4b538a58ecad5fefff00c68060415', 46);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-47::mrego::(Checksum: 3:20d9ca0a2dac404d34031a303d36332f)
CREATE TABLE material_assigment (id BIGINT NOT NULL, version BIGINT NOT NULL, units DECIMAL(19,2), unit_price DECIMAL(19,2), material_id BIGINT, estimated_availability TIMESTAMP WITH TIME ZONE, status INT, order_element_id BIGINT, CONSTRAINT material_assigment_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-47', '2.0-rc7', '3:20d9ca0a2dac404d34031a303d36332f', 47);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-48::mrego::(Checksum: 3:9422bfd2634488255bcbb32d73d4d9a6)
CREATE TABLE material_assigment_template (id BIGINT NOT NULL, version BIGINT NOT NULL, units DECIMAL(19,2), unit_price DECIMAL(19,2), material_id BIGINT, order_element_template_id BIGINT, CONSTRAINT material_assigment_template_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-48', '2.0-rc7', '3:9422bfd2634488255bcbb32d73d4d9a6', 48);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-49::mrego::(Checksum: 3:bb378576fc9548e6e0bf30c553be56c2)
CREATE TABLE material_category (id BIGINT NOT NULL, version BIGINT NOT NULL, code VARCHAR(255) NOT NULL, name VARCHAR(255), code_autogenerated BOOLEAN NOT NULL, last_material_sequence_code INT NOT NULL, parent_id BIGINT, CONSTRAINT material_category_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-49', '2.0-rc7', '3:bb378576fc9548e6e0bf30c553be56c2', 49);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-50::mrego::(Checksum: 3:86be514721a43f3a69e0fc5575f68bcf)
CREATE TABLE order_authorization (id BIGINT NOT NULL, order_authorization_subclass VARCHAR(255) NOT NULL, version BIGINT NOT NULL, authorization_type VARCHAR(255) NOT NULL, order_id BIGINT, user_id BIGINT, profile_id BIGINT, CONSTRAINT order_authorization_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-50', '2.0-rc7', '3:86be514721a43f3a69e0fc5575f68bcf', 50);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-51::mrego::(Checksum: 3:6a7ac8677daeb7b232876fef8bc2eb13)
CREATE TABLE order_element (id BIGINT NOT NULL, version BIGINT NOT NULL, name VARCHAR(255), description VARCHAR(255), code VARCHAR(255), init_date TIMESTAMP WITH TIME ZONE, deadline TIMESTAMP WITH TIME ZONE, last_advance_meausurement_for_spreading DECIMAL(19,2), dirty_last_advance_measurement_for_spreading BOOLEAN, parent BIGINT, template BIGINT, external_code VARCHAR(255), sum_charged_hours_id BIGINT, position_in_container INT, CONSTRAINT order_element_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-51', '2.0-rc7', '3:6a7ac8677daeb7b232876fef8bc2eb13', 51);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-52::mrego::(Checksum: 3:132f159f7460a378fe40004d8844911c)
CREATE TABLE order_element_label (order_element_id BIGINT NOT NULL, label_id BIGINT NOT NULL);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-52', '2.0-rc7', '3:132f159f7460a378fe40004d8844911c', 52);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-53::mrego::(Checksum: 3:e86194e70e6973332ef4538bdc5d7a1a)
CREATE TABLE order_element_template (id BIGINT NOT NULL, version BIGINT NOT NULL, name VARCHAR(255), description VARCHAR(255), code VARCHAR(255), start_as_days_from_beginning INT, deadline_as_days_from_beginning INT, scheduling_state_type INT, parent BIGINT, position_in_container INT, CONSTRAINT order_element_template_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-53', '2.0-rc7', '3:e86194e70e6973332ef4538bdc5d7a1a', 53);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-54::mrego::(Checksum: 3:2524421c32a9b419da3fe430869fdb1d)
CREATE TABLE order_element_template_label (order_element_template_id BIGINT NOT NULL, label_id BIGINT NOT NULL);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-54', '2.0-rc7', '3:2524421c32a9b419da3fe430869fdb1d', 54);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-55::mrego::(Checksum: 3:f07c8df4ae4b20f431ed20c225b80172)
CREATE TABLE order_element_template_quality_form (order_element_template_id BIGINT NOT NULL, quality_form_id BIGINT NOT NULL);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-55', '2.0-rc7', '3:f07c8df4ae4b20f431ed20c225b80172', 55);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-56::mrego::(Checksum: 3:d180f6978451999d3c71c31e22a05f12)
CREATE TABLE order_line (order_element_id BIGINT NOT NULL, last_hours_group_sequence_code INT, CONSTRAINT order_line_pkey PRIMARY KEY (order_element_id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-56', '2.0-rc7', '3:d180f6978451999d3c71c31e22a05f12', 56);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-57::mrego::(Checksum: 3:96dee6dd4c6fe24bdf27aec7375f97a2)
CREATE TABLE order_line_group (order_element_id BIGINT NOT NULL, CONSTRAINT order_line_group_pkey PRIMARY KEY (order_element_id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-57', '2.0-rc7', '3:96dee6dd4c6fe24bdf27aec7375f97a2', 57);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-58::mrego::(Checksum: 3:57a5a9423e13e7d46ab17b29ef36e3ac)
CREATE TABLE order_line_group_template (group_template_id BIGINT NOT NULL, CONSTRAINT order_line_group_template_pkey PRIMARY KEY (group_template_id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-58', '2.0-rc7', '3:57a5a9423e13e7d46ab17b29ef36e3ac', 58);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-59::mrego::(Checksum: 3:481dae68046dee1729404fcb4b8f6724)
CREATE TABLE order_line_template (order_line_template_id BIGINT NOT NULL, last_hours_group_sequence_code INT, CONSTRAINT order_line_template_pkey PRIMARY KEY (order_line_template_id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-59', '2.0-rc7', '3:481dae68046dee1729404fcb4b8f6724', 59);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-60::mrego::(Checksum: 3:3c79c9a14789f861812e28b85f42ba1a)
CREATE TABLE order_table (order_element_id BIGINT NOT NULL, responsible VARCHAR(255), dependencies_constraints_have_priority BOOLEAN, code_autogenerated BOOLEAN, last_order_element_sequence_code INT, work_budget DECIMAL(19,2), materials_budget DECIMAL(19,2), total_hours INT, customer_reference VARCHAR(255), external_code VARCHAR(255), state INT, customer BIGINT, base_calendar_id BIGINT, CONSTRAINT order_table_pkey PRIMARY KEY (order_element_id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-60', '2.0-rc7', '3:3c79c9a14789f861812e28b85f42ba1a', 60);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-61::mrego::(Checksum: 3:b0647147970dcc6926527bcc26e08d0d)
CREATE TABLE order_template (order_template_id BIGINT NOT NULL, base_calendar_id BIGINT, CONSTRAINT order_template_pkey PRIMARY KEY (order_template_id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-61', '2.0-rc7', '3:b0647147970dcc6926527bcc26e08d0d', 61);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-62::mrego::(Checksum: 3:7a0cee9dc0952cca4c3e2a32508a4433)
CREATE TABLE order_version (id BIGINT NOT NULL, version BIGINT NOT NULL, modification_by_owner_timestamp TIMESTAMP WITH TIME ZONE, ownerscenario BIGINT, CONSTRAINT order_version_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-62', '2.0-rc7', '3:7a0cee9dc0952cca4c3e2a32508a4433', 62);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-63::mrego::(Checksum: 3:2b9225897f01e983677b51310e4fe0c6)
CREATE TABLE planning_data (planning_data_id BIGINT NOT NULL, progress_by_duration DECIMAL(19,2), progress_by_num_hours DECIMAL(19,2), CONSTRAINT planning_data_pkey PRIMARY KEY (planning_data_id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-63', '2.0-rc7', '3:2b9225897f01e983677b51310e4fe0c6', 63);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-64::mrego::(Checksum: 3:8c6b353a40e4bbfe9ddec68b6e2ed109)
CREATE TABLE profile_roles (profile_id BIGINT NOT NULL, elt VARCHAR(255));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-64', '2.0-rc7', '3:8c6b353a40e4bbfe9ddec68b6e2ed109', 64);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-65::mrego::(Checksum: 3:181a3cfd4d56baa702ea56857d70c6a5)
CREATE TABLE profile_table (id BIGINT NOT NULL, version BIGINT NOT NULL, profile_name VARCHAR(255) NOT NULL, CONSTRAINT profile_table_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-65', '2.0-rc7', '3:181a3cfd4d56baa702ea56857d70c6a5', 65);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-66::mrego::(Checksum: 3:1ce12362bbe5c9289c1b1417b4534017)
CREATE TABLE quality_form (id BIGINT NOT NULL, version BIGINT NOT NULL, name VARCHAR(255), description VARCHAR(255), quality_form_type INT, report_advance BOOLEAN, advance_type_id BIGINT, CONSTRAINT quality_form_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-66', '2.0-rc7', '3:1ce12362bbe5c9289c1b1417b4534017', 66);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-67::mrego::(Checksum: 3:3db8192b7fc2c73f2d6f0b811b86193a)
CREATE TABLE quality_form_items (quality_form_id BIGINT NOT NULL, name VARCHAR(255), percentage DECIMAL(19,2), position INT, idx INT NOT NULL);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-67', '2.0-rc7', '3:3db8192b7fc2c73f2d6f0b811b86193a', 67);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-68::mrego::(Checksum: 3:49669d8f9f006e24a66e6baef277ebaf)
CREATE TABLE resource (id BIGINT NOT NULL, version BIGINT NOT NULL, code VARCHAR(255) NOT NULL, code_autogenerated BOOLEAN NOT NULL, limited_resource BOOLEAN NOT NULL, base_calendar_id BIGINT, CONSTRAINT resource_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-68', '2.0-rc7', '3:49669d8f9f006e24a66e6baef277ebaf', 68);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-69::mrego::(Checksum: 3:ec29eb383f779659440de1992b1b6bba)
CREATE TABLE resource_allocation (id BIGINT NOT NULL, version BIGINT NOT NULL, resources_per_day DECIMAL(19,2), intended_total_hours INT, original_total_assignment INT, task BIGINT, assignment_function BIGINT, CONSTRAINT resource_allocation_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-69', '2.0-rc7', '3:ec29eb383f779659440de1992b1b6bba', 69);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-70::mrego::(Checksum: 3:668c637d3c53b7592d3dc67fc4703cff)
CREATE TABLE resource_calendar (base_calendar_id BIGINT NOT NULL, capacity INT NOT NULL, CONSTRAINT resource_calendar_pkey PRIMARY KEY (base_calendar_id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-70', '2.0-rc7', '3:668c637d3c53b7592d3dc67fc4703cff', 70);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-71::mrego::(Checksum: 3:4f9d5316304450ab582b0858ce87c849)
CREATE TABLE resources_cost_category_assignment (id BIGINT NOT NULL, version BIGINT NOT NULL, code VARCHAR(255) NOT NULL, init_date DATE, end_date DATE, cost_category_id BIGINT, resource_id BIGINT, CONSTRAINT resources_cost_category_assignment_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-71', '2.0-rc7', '3:4f9d5316304450ab582b0858ce87c849', 71);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-72::mrego::(Checksum: 3:7a3e44f865f5a910f79c5f06e0b64f14)
CREATE TABLE roles_table (user_id BIGINT NOT NULL, elt VARCHAR(255));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-72', '2.0-rc7', '3:7a3e44f865f5a910f79c5f06e0b64f14', 72);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-73::mrego::(Checksum: 3:a635c19cae307c38b2763e3b9d58d224)
CREATE TABLE scenario (id BIGINT NOT NULL, version BIGINT NOT NULL, name VARCHAR(255), description VARCHAR(255), last_not_owned_reassignations_time_stamp TIMESTAMP WITH TIME ZONE, predecessor BIGINT, CONSTRAINT scenario_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-73', '2.0-rc7', '3:a635c19cae307c38b2763e3b9d58d224', 73);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-74::mrego::(Checksum: 3:95ac63bf89b9b48c007e8460e7e48cdb)
CREATE TABLE scenario_orders (order_id BIGINT NOT NULL, order_version_id BIGINT NOT NULL, scenario_id BIGINT NOT NULL);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-74', '2.0-rc7', '3:95ac63bf89b9b48c007e8460e7e48cdb', 74);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-75::mrego::(Checksum: 3:28e6cc28eded2e909048c08ffacc1748)
CREATE TABLE scheduling_data_for_version (id BIGINT NOT NULL, version BIGINT NOT NULL, scheduling_state_type INT, order_element_id BIGINT, CONSTRAINT scheduling_data_for_version_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-75', '2.0-rc7', '3:28e6cc28eded2e909048c08ffacc1748', 75);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-76::mrego::(Checksum: 3:f32e144cefab5640c93a41de30b01609)
CREATE TABLE scheduling_states_by_order_version (order_element_id BIGINT NOT NULL, scheduling_state_for_version_id BIGINT NOT NULL, order_version_id BIGINT NOT NULL);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-76', '2.0-rc7', '3:f32e144cefab5640c93a41de30b01609', 76);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-77::mrego::(Checksum: 3:5504fcfe56f3f27aedab1932b345ef67)
CREATE TABLE sigmoid_function (assignment_function_id BIGINT NOT NULL, CONSTRAINT sigmoid_function_pkey PRIMARY KEY (assignment_function_id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-77', '2.0-rc7', '3:5504fcfe56f3f27aedab1932b345ef67', 77);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-78::mrego::(Checksum: 3:bfb3ce3ba16ec8f8206c296f743868e5)
CREATE TABLE specific_day_assignments_container (id BIGINT NOT NULL, version BIGINT NOT NULL, resource_allocation_id BIGINT, scenario BIGINT, start_date DATE, duration_start_in_first_day INT, end_date DATE, duration_in_last_day INT, CONSTRAINT specific_day_assignments_container_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-78', '2.0-rc7', '3:bfb3ce3ba16ec8f8206c296f743868e5', 78);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-79::mrego::(Checksum: 3:7b7e2576df7e2b013c4609c905a1e89f)
CREATE TABLE specific_resource_allocation (resource_allocation_id BIGINT NOT NULL, resource BIGINT, CONSTRAINT specific_resource_allocation_pkey PRIMARY KEY (resource_allocation_id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-79', '2.0-rc7', '3:7b7e2576df7e2b013c4609c905a1e89f', 79);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-80::mrego::(Checksum: 3:f095dee1e812c9f76e21c526566bd348)
CREATE TABLE stretches (assignment_function_id BIGINT NOT NULL, date DATE NOT NULL, length_percentage DECIMAL(19,2) NOT NULL, amount_work_percentage DECIMAL(19,2) NOT NULL, stretch_position INT NOT NULL);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-80', '2.0-rc7', '3:f095dee1e812c9f76e21c526566bd348', 80);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-81::mrego::(Checksum: 3:0e6f358e54f708c3e05debfeae8027f8)
CREATE TABLE stretches_function (assignment_function_id BIGINT NOT NULL, type INT, CONSTRAINT stretches_function_pkey PRIMARY KEY (assignment_function_id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-81', '2.0-rc7', '3:0e6f358e54f708c3e05debfeae8027f8', 81);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-82::mrego::(Checksum: 3:660f1dd76f869c29fde7efc6f7e1255e)
CREATE TABLE subcontracted_task_data (id BIGINT NOT NULL, version BIGINT NOT NULL, external_company BIGINT, subcontratation_date TIMESTAMP WITH TIME ZONE, subcontract_communication_date TIMESTAMP WITH TIME ZONE, work_description VARCHAR(255), subcontract_price DECIMAL(19,2), subcontracted_code VARCHAR(255), node_without_children_exported BOOLEAN, labels_exported BOOLEAN, material_assignments_exported BOOLEAN, hours_groups_exported BOOLEAN, state INT, CONSTRAINT subcontracted_task_data_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-82', '2.0-rc7', '3:660f1dd76f869c29fde7efc6f7e1255e', 82);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-83::mrego::(Checksum: 3:43f98ac2e8ef569c2ace92091588dfc5)
CREATE TABLE sum_charged_hours (id BIGINT NOT NULL, version BIGINT NOT NULL, direct_charged_hours INT, indirect_charged_hours INT, CONSTRAINT sum_charged_hours_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-83', '2.0-rc7', '3:43f98ac2e8ef569c2ace92091588dfc5', 83);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-84::mrego::(Checksum: 3:72aa9950802d4e8da713013ae72ea212)
CREATE TABLE task (task_element_id BIGINT NOT NULL, calculated_value INT, start_constraint_type INT, constraint_date DATE, workable_days INT, allocation_direction INT, subcontrated_task_data_id BIGINT, priority INT, CONSTRAINT task_pkey PRIMARY KEY (task_element_id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-84', '2.0-rc7', '3:72aa9950802d4e8da713013ae72ea212', 84);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-85::mrego::(Checksum: 3:5f58cf1966364d46c430e933f520332c)
CREATE TABLE task_element (id BIGINT NOT NULL, version BIGINT NOT NULL, name VARCHAR(255), notes VARCHAR(255), start_date DATE NOT NULL, start_day_duration INT, end_date DATE NOT NULL, end_day_duration INT, deadline DATE, advance_percentage DECIMAL(19,4), sum_of_hours_allocated INT, parent BIGINT, base_calendar_id BIGINT, position_in_parent INT, CONSTRAINT task_element_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-85', '2.0-rc7', '3:5f58cf1966364d46c430e933f520332c', 85);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-86::mrego::(Checksum: 3:930edd522020ea950b89ea3fcc60293f)
CREATE TABLE task_group (task_element_id BIGINT NOT NULL, CONSTRAINT task_group_pkey PRIMARY KEY (task_element_id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-86', '2.0-rc7', '3:930edd522020ea950b89ea3fcc60293f', 86);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-87::mrego::(Checksum: 3:eef1806c4faaa543eabf7aa919ee1871)
CREATE TABLE task_milestone (task_element_id BIGINT NOT NULL, start_constraint_type INT, constraint_date DATE, CONSTRAINT task_milestone_pkey PRIMARY KEY (task_element_id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-87', '2.0-rc7', '3:eef1806c4faaa543eabf7aa919ee1871', 87);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-88::mrego::(Checksum: 3:ba5ed5b46b50e4e696f26943e4846b38)
CREATE TABLE task_quality_form (id BIGINT NOT NULL, version BIGINT NOT NULL, quality_form_id BIGINT, order_element_id BIGINT, report_advance BOOLEAN, CONSTRAINT task_quality_form_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-88', '2.0-rc7', '3:ba5ed5b46b50e4e696f26943e4846b38', 88);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-89::mrego::(Checksum: 3:25d42a7f59acd170516f566f91db477b)
CREATE TABLE task_quality_form_items (task_quality_form_id BIGINT NOT NULL, name VARCHAR(255), percentage DECIMAL(19,2), position INT, passed BOOLEAN, date TIMESTAMP WITH TIME ZONE, idx INT NOT NULL);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-89', '2.0-rc7', '3:25d42a7f59acd170516f566f91db477b', 89);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-90::mrego::(Checksum: 3:0220e67c3c6e052be5b2683f3aff6cd9)
CREATE TABLE task_source (id BIGINT NOT NULL, version BIGINT NOT NULL, schedulingdata BIGINT, CONSTRAINT task_source_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-90', '2.0-rc7', '3:0220e67c3c6e052be5b2683f3aff6cd9', 90);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-91::mrego::(Checksum: 3:dbeb7f14726427f4332caba3c3882781)
CREATE TABLE task_source_hours_groups (task_source_id BIGINT NOT NULL, hours_group_id BIGINT NOT NULL);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-91', '2.0-rc7', '3:dbeb7f14726427f4332caba3c3882781', 91);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-92::mrego::(Checksum: 3:a4f2631814df0304d6e1a9142e0174a1)
CREATE TABLE type_of_work_hours (id BIGINT NOT NULL, version BIGINT NOT NULL, name VARCHAR(255), code VARCHAR(255), default_price DECIMAL(19,2), enabled BOOLEAN, code_autogenerated BOOLEAN NOT NULL, CONSTRAINT type_of_work_hours_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-92', '2.0-rc7', '3:a4f2631814df0304d6e1a9142e0174a1', 92);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-93::mrego::(Checksum: 3:665d61220b4af809bb0048a73af9529d)
CREATE TABLE unit_type (id BIGINT NOT NULL, version BIGINT NOT NULL, code VARCHAR(255) NOT NULL, measure VARCHAR(255), code_autogenerated BOOLEAN NOT NULL, CONSTRAINT unit_type_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-93', '2.0-rc7', '3:665d61220b4af809bb0048a73af9529d', 93);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-94::mrego::(Checksum: 3:77fb476c14c92eda11701adb0a6d406f)
CREATE TABLE user_profiles (user_id BIGINT NOT NULL, profile_id BIGINT NOT NULL);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-94', '2.0-rc7', '3:77fb476c14c92eda11701adb0a6d406f', 94);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-95::mrego::(Checksum: 3:2e07882701f4d10d2c5249aefdae87ee)
CREATE TABLE user_table (id BIGINT NOT NULL, version BIGINT NOT NULL, login_name VARCHAR(255) NOT NULL, password VARCHAR(255) NOT NULL, email VARCHAR(255), disabled BOOLEAN, lastconnectedscenario BIGINT, CONSTRAINT user_table_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-95', '2.0-rc7', '3:2e07882701f4d10d2c5249aefdae87ee', 95);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-96::mrego::(Checksum: 3:12e1f91eae40df9eda60c20d6f9c8874)
CREATE TABLE virtual_worker (virtual_worker_id BIGINT NOT NULL, observations VARCHAR(255), CONSTRAINT virtual_worker_pkey PRIMARY KEY (virtual_worker_id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-96', '2.0-rc7', '3:12e1f91eae40df9eda60c20d6f9c8874', 96);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-97::mrego::(Checksum: 3:72db70644086a38aa90367c038a89350)
CREATE TABLE work_report (id BIGINT NOT NULL, version BIGINT NOT NULL, code VARCHAR(255) NOT NULL, date TIMESTAMP WITH TIME ZONE, code_autogenerated BOOLEAN NOT NULL, last_work_report_line_sequence_code INT, ork_report_type_id BIGINT NOT NULL, resource_id BIGINT, order_element_id BIGINT, CONSTRAINT work_report_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-97', '2.0-rc7', '3:72db70644086a38aa90367c038a89350', 97);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-98::mrego::(Checksum: 3:1d6bac1b7849a0442a162078696893e3)
CREATE TABLE work_report_label_type_assigment (id BIGINT NOT NULL, version BIGINT NOT NULL, labels_shared_by_lines BOOLEAN, position_number INT, label_type_id BIGINT, label_id BIGINT, work_report_type_id BIGINT, CONSTRAINT work_report_label_type_assigment_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-98', '2.0-rc7', '3:1d6bac1b7849a0442a162078696893e3', 98);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-99::mrego::(Checksum: 3:7f6646bbd481090fa616c199b956b014)
CREATE TABLE work_report_line (id BIGINT NOT NULL, version BIGINT NOT NULL, code VARCHAR(255) NOT NULL, num_hours INT, date TIMESTAMP WITH TIME ZONE, clock_start INT, clock_finish INT, work_report_id BIGINT, resource_id BIGINT NOT NULL, order_element_id BIGINT NOT NULL, type_work_hours_id BIGINT NOT NULL, CONSTRAINT work_report_line_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-99', '2.0-rc7', '3:7f6646bbd481090fa616c199b956b014', 99);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-100::mrego::(Checksum: 3:bbbcbaf05cf76c8236d9137a4a7a25ec)
CREATE TABLE work_report_type (id BIGINT NOT NULL, version BIGINT NOT NULL, name VARCHAR(255), code VARCHAR(255), date_is_shared_by_lines BOOLEAN, resource_is_shared_in_lines BOOLEAN, order_element_is_shared_in_lines BOOLEAN, code_autogenerated BOOLEAN NOT NULL, hours_management INT, CONSTRAINT work_report_type_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-100', '2.0-rc7', '3:bbbcbaf05cf76c8236d9137a4a7a25ec', 100);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-101::mrego::(Checksum: 3:15f1e5f6e1f7c3308b084e536e88b9b8)
CREATE TABLE worker (worker_id BIGINT NOT NULL, first_name VARCHAR(255), surname VARCHAR(255), nif VARCHAR(255), CONSTRAINT worker_pkey PRIMARY KEY (worker_id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-101', '2.0-rc7', '3:15f1e5f6e1f7c3308b084e536e88b9b8', 101);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-102::mrego::(Checksum: 3:69b1aeed86840d511ac06119ad1f3c2a)
CREATE TABLE workreports_labels (work_report_id BIGINT NOT NULL, label_id BIGINT NOT NULL);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-102', '2.0-rc7', '3:69b1aeed86840d511ac06119ad1f3c2a', 102);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-103::mrego::(Checksum: 3:57d3f2fd8967dfd691fd95d30fb1c134)
CREATE TABLE workreportslines_labels (work_report_line_id BIGINT NOT NULL, label_id BIGINT NOT NULL);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-103', '2.0-rc7', '3:57d3f2fd8967dfd691fd95d30fb1c134', 103);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-104::mrego::(Checksum: 3:0e57f86b004bedba6c607c8d2c566164)
ALTER TABLE all_criterions ADD CONSTRAINT all_criterions_pkey PRIMARY KEY (generic_resource_allocation_id, criterion_id);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Primary Key', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-104', '2.0-rc7', '3:0e57f86b004bedba6c607c8d2c566164', 104);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-105::mrego::(Checksum: 3:55a89da076a5b87ce5e49e1db3ed3fb9)
ALTER TABLE effort_per_day ADD CONSTRAINT effort_per_day_pkey PRIMARY KEY (base_calendar_id, day_id);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Primary Key', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-105', '2.0-rc7', '3:55a89da076a5b87ce5e49e1db3ed3fb9', 105);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-106::mrego::(Checksum: 3:4db964219e3dde449c0a54d58d204a7b)
ALTER TABLE machine_configuration_unit_required_criterions ADD CONSTRAINT machine_configuration_unit_required_criterions_pkey PRIMARY KEY (id, criterion_id);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Primary Key', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-106', '2.0-rc7', '3:4db964219e3dde449c0a54d58d204a7b', 106);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-107::mrego::(Checksum: 3:0d50d294d4db44aed32f8c909114645d)
ALTER TABLE order_element_label ADD CONSTRAINT order_element_label_pkey PRIMARY KEY (order_element_id, label_id);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Primary Key', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-107', '2.0-rc7', '3:0d50d294d4db44aed32f8c909114645d', 107);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-108::mrego::(Checksum: 3:9c39ac8c3d6ebc758e69da66cfc20283)
ALTER TABLE order_element_template_label ADD CONSTRAINT order_element_template_label_pkey PRIMARY KEY (order_element_template_id, label_id);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Primary Key', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-108', '2.0-rc7', '3:9c39ac8c3d6ebc758e69da66cfc20283', 108);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-109::mrego::(Checksum: 3:254739c105dc46829b4019e6cd4daa8e)
ALTER TABLE order_element_template_quality_form ADD CONSTRAINT order_element_template_quality_form_pkey PRIMARY KEY (order_element_template_id, quality_form_id);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Primary Key', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-109', '2.0-rc7', '3:254739c105dc46829b4019e6cd4daa8e', 109);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-110::mrego::(Checksum: 3:330cbde32db56aa70e52784794b743ff)
ALTER TABLE quality_form_items ADD CONSTRAINT quality_form_items_pkey PRIMARY KEY (quality_form_id, idx);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Primary Key', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-110', '2.0-rc7', '3:330cbde32db56aa70e52784794b743ff', 110);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-111::mrego::(Checksum: 3:39d0f5be8bc00a3752b5f49dd92866de)
ALTER TABLE scenario_orders ADD CONSTRAINT scenario_orders_pkey PRIMARY KEY (scenario_id, order_id);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Primary Key', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-111', '2.0-rc7', '3:39d0f5be8bc00a3752b5f49dd92866de', 111);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-112::mrego::(Checksum: 3:005cd1ac6366225c7124eb995c790329)
ALTER TABLE scheduling_states_by_order_version ADD CONSTRAINT scheduling_states_by_order_version_pkey PRIMARY KEY (order_element_id, order_version_id);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Primary Key', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-112', '2.0-rc7', '3:005cd1ac6366225c7124eb995c790329', 112);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-113::mrego::(Checksum: 3:aa9ef6bec758b8fef0d7f58d652f23ed)
ALTER TABLE stretches ADD CONSTRAINT stretches_pkey PRIMARY KEY (assignment_function_id, stretch_position);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Primary Key', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-113', '2.0-rc7', '3:aa9ef6bec758b8fef0d7f58d652f23ed', 113);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-114::mrego::(Checksum: 3:57037ae27d15fb149b2b18b70e3ac640)
ALTER TABLE task_quality_form_items ADD CONSTRAINT task_quality_form_items_pkey PRIMARY KEY (task_quality_form_id, idx);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Primary Key', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-114', '2.0-rc7', '3:57037ae27d15fb149b2b18b70e3ac640', 114);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-115::mrego::(Checksum: 3:9a325bc706e5f056e25b5029c144c5d8)
ALTER TABLE task_source_hours_groups ADD CONSTRAINT task_source_hours_groups_pkey PRIMARY KEY (task_source_id, hours_group_id);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Primary Key', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-115', '2.0-rc7', '3:9a325bc706e5f056e25b5029c144c5d8', 115);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-116::mrego::(Checksum: 3:f5bef61b4276a1bf815b80abfbbdd0dd)
ALTER TABLE user_profiles ADD CONSTRAINT user_profiles_pkey PRIMARY KEY (user_id, profile_id);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Primary Key', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-116', '2.0-rc7', '3:f5bef61b4276a1bf815b80abfbbdd0dd', 116);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-117::mrego::(Checksum: 3:7eff22e0b4befe8f4c445f1c5a58a15e)
ALTER TABLE workreports_labels ADD CONSTRAINT workreports_labels_pkey PRIMARY KEY (work_report_id, label_id);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Primary Key', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-117', '2.0-rc7', '3:7eff22e0b4befe8f4c445f1c5a58a15e', 117);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-118::mrego::(Checksum: 3:dadfa8f8caf1ae02cfc16ecbed0c8b90)
ALTER TABLE workreportslines_labels ADD CONSTRAINT workreportslines_labels_pkey PRIMARY KEY (work_report_line_id, label_id);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Primary Key', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-118', '2.0-rc7', '3:dadfa8f8caf1ae02cfc16ecbed0c8b90', 118);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-119::mrego::(Checksum: 3:138d0588ac80f0a920e484db36f4e97d)
ALTER TABLE advance_type ADD CONSTRAINT advance_type_unit_name_key UNIQUE (unit_name);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-119', '2.0-rc7', '3:138d0588ac80f0a920e484db36f4e97d', 119);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-120::mrego::(Checksum: 3:2a81f473aa411fe1bf94ff891f950f05)
ALTER TABLE base_calendar ADD CONSTRAINT base_calendar_code_key UNIQUE (code);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-120', '2.0-rc7', '3:2a81f473aa411fe1bf94ff891f950f05', 120);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-121::mrego::(Checksum: 3:465b67c4800169a5ad1a1fa4cf9937ce)
ALTER TABLE calendar_availability ADD CONSTRAINT calendar_availability_code_key UNIQUE (code);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-121', '2.0-rc7', '3:465b67c4800169a5ad1a1fa4cf9937ce', 121);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-122::mrego::(Checksum: 3:d9da052c7f29fb3f76a2a4056af176bd)
ALTER TABLE calendar_data ADD CONSTRAINT calendar_data_code_key UNIQUE (code);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-122', '2.0-rc7', '3:d9da052c7f29fb3f76a2a4056af176bd', 122);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-123::mrego::(Checksum: 3:7ebb4272b707b682e405cb30ebb3fccb)
ALTER TABLE calendar_exception ADD CONSTRAINT calendar_exception_code_key UNIQUE (code);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-123', '2.0-rc7', '3:7ebb4272b707b682e405cb30ebb3fccb', 123);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-124::mrego::(Checksum: 3:360b5d5226fa5eb0d62b8531c2812a05)
ALTER TABLE calendar_exception_type ADD CONSTRAINT calendar_exception_type_code_key UNIQUE (code);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-124', '2.0-rc7', '3:360b5d5226fa5eb0d62b8531c2812a05', 124);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-125::mrego::(Checksum: 3:963fdb094cb961a23659f3e19a1ddde3)
ALTER TABLE calendar_exception_type ADD CONSTRAINT calendar_exception_type_name_key UNIQUE (name);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-125', '2.0-rc7', '3:963fdb094cb961a23659f3e19a1ddde3', 125);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-126::mrego::(Checksum: 3:b20bb3c187e6488863f9d5481665fdac)
ALTER TABLE cost_category ADD CONSTRAINT cost_category_code_key UNIQUE (code);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-126', '2.0-rc7', '3:b20bb3c187e6488863f9d5481665fdac', 126);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-127::mrego::(Checksum: 3:f3035f410a920573b57eb459d1bcd6b1)
ALTER TABLE cost_category ADD CONSTRAINT cost_category_name_key UNIQUE (name);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-127', '2.0-rc7', '3:f3035f410a920573b57eb459d1bcd6b1', 127);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-128::mrego::(Checksum: 3:ca1cdff9e1a997ff2c7b285e84221ab3)
ALTER TABLE criterion ADD CONSTRAINT criterion_code_key UNIQUE (code);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-128', '2.0-rc7', '3:ca1cdff9e1a997ff2c7b285e84221ab3', 128);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-129::mrego::(Checksum: 3:b475e7347003735620c2366fe038ee40)
ALTER TABLE criterion ADD CONSTRAINT criterion_name_key UNIQUE (id_criterion_type, name);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-129', '2.0-rc7', '3:b475e7347003735620c2366fe038ee40', 129);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-130::mrego::(Checksum: 3:5523e28282da63951996a88d29635632)
ALTER TABLE criterion_satisfaction ADD CONSTRAINT criterion_satisfaction_code_key UNIQUE (code);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-130', '2.0-rc7', '3:5523e28282da63951996a88d29635632', 130);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-131::mrego::(Checksum: 3:484e71ddda004fcbb6f56b3a1df07c12)
ALTER TABLE criterion_type ADD CONSTRAINT criterion_type_code_key UNIQUE (code);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-131', '2.0-rc7', '3:484e71ddda004fcbb6f56b3a1df07c12', 131);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-132::mrego::(Checksum: 3:750f465a29368e3c472c7717368e5315)
ALTER TABLE criterion_type ADD CONSTRAINT criterion_type_name_key UNIQUE (name);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-132', '2.0-rc7', '3:750f465a29368e3c472c7717368e5315', 132);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-133::mrego::(Checksum: 3:ed11a2642a9063cae1be4ee314990279)
ALTER TABLE external_company ADD CONSTRAINT external_company_name_key UNIQUE (name);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-133', '2.0-rc7', '3:ed11a2642a9063cae1be4ee314990279', 133);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-134::mrego::(Checksum: 3:632199e3f5e02d7b644fef85e7c7a148)
ALTER TABLE external_company ADD CONSTRAINT external_company_nif_key UNIQUE (nif);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-134', '2.0-rc7', '3:632199e3f5e02d7b644fef85e7c7a148', 134);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-135::mrego::(Checksum: 3:f6136a87270d6999c38c78c5a4507ba0)
ALTER TABLE hour_cost ADD CONSTRAINT hour_cost_code_key UNIQUE (code);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-135', '2.0-rc7', '3:f6136a87270d6999c38c78c5a4507ba0', 135);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-136::mrego::(Checksum: 3:3b8cdb7c8a87aded69e76f1127d93338)
ALTER TABLE hours_group ADD CONSTRAINT hours_group_code_key UNIQUE (code);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-136', '2.0-rc7', '3:3b8cdb7c8a87aded69e76f1127d93338', 136);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-137::mrego::(Checksum: 3:f21de5aaf5812d3b53ff6ba37c500419)
ALTER TABLE label ADD CONSTRAINT label_code_key UNIQUE (code);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-137', '2.0-rc7', '3:f21de5aaf5812d3b53ff6ba37c500419', 137);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-138::mrego::(Checksum: 3:212ee29342faa1bb2b4a50cfdfdf9a78)
ALTER TABLE label ADD CONSTRAINT label_name_key UNIQUE (label_type_id, name);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-138', '2.0-rc7', '3:212ee29342faa1bb2b4a50cfdfdf9a78', 138);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-139::mrego::(Checksum: 3:c1cd1a61a496daaf1bc3e941f84aa781)
ALTER TABLE label_type ADD CONSTRAINT label_type_code_key UNIQUE (code);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-139', '2.0-rc7', '3:c1cd1a61a496daaf1bc3e941f84aa781', 139);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-140::mrego::(Checksum: 3:d5266cebc05f66e670752fd599970deb)
ALTER TABLE label_type ADD CONSTRAINT label_type_name_key UNIQUE (name);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-140', '2.0-rc7', '3:d5266cebc05f66e670752fd599970deb', 140);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-141::mrego::(Checksum: 3:1846e834301cbacca345ad1d30c97783)
ALTER TABLE limiting_resource_queue ADD CONSTRAINT limiting_resource_queue_resource_id_key UNIQUE (resource_id);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-141', '2.0-rc7', '3:1846e834301cbacca345ad1d30c97783', 141);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-142::mrego::(Checksum: 3:02dd707d2304800fa3353b76b4b14a79)
ALTER TABLE limiting_resource_queue_element ADD CONSTRAINT limiting_resource_queue_element_resource_allocation_id_key UNIQUE (resource_allocation_id);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-142', '2.0-rc7', '3:02dd707d2304800fa3353b76b4b14a79', 142);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-143::mrego::(Checksum: 3:c7d786a54bd2744456d86331287eaf88)
ALTER TABLE material ADD CONSTRAINT material_code_key UNIQUE (code);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-143', '2.0-rc7', '3:c7d786a54bd2744456d86331287eaf88', 143);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-144::mrego::(Checksum: 3:d83e1d31be4d2a88a30cef280323dc14)
ALTER TABLE material_category ADD CONSTRAINT material_category_code_key UNIQUE (code);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-144', '2.0-rc7', '3:d83e1d31be4d2a88a30cef280323dc14', 144);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-145::mrego::(Checksum: 3:2c8d1d5abd94c1f1b77b812e5a218703)
ALTER TABLE order_element ADD CONSTRAINT order_element_code_key UNIQUE (code);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-145', '2.0-rc7', '3:2c8d1d5abd94c1f1b77b812e5a218703', 145);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-146::mrego::(Checksum: 3:98959c39111d467dbaef246b4e77c7d4)
ALTER TABLE order_element ADD CONSTRAINT order_element_sum_charged_hours_id_key UNIQUE (sum_charged_hours_id);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-146', '2.0-rc7', '3:98959c39111d467dbaef246b4e77c7d4', 146);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-147::mrego::(Checksum: 3:2f5b3db90f5fe8d7166b8698732d89ee)
ALTER TABLE profile_table ADD CONSTRAINT profile_table_profile_name_key UNIQUE (profile_name);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-147', '2.0-rc7', '3:2f5b3db90f5fe8d7166b8698732d89ee', 147);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-148::mrego::(Checksum: 3:b6ab8ac7897d485ec2c01795fdbacba6)
ALTER TABLE quality_form ADD CONSTRAINT quality_form_name_key UNIQUE (name);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-148', '2.0-rc7', '3:b6ab8ac7897d485ec2c01795fdbacba6', 148);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-149::mrego::(Checksum: 3:4621bf17b5d622b52e70ee84a2720ce4)
ALTER TABLE resource ADD CONSTRAINT resource_base_calendar_id_key UNIQUE (base_calendar_id);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-149', '2.0-rc7', '3:4621bf17b5d622b52e70ee84a2720ce4', 149);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-150::mrego::(Checksum: 3:d706f00de9bf475742860188e15d7b41)
ALTER TABLE resource ADD CONSTRAINT resource_code_key UNIQUE (code);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-150', '2.0-rc7', '3:d706f00de9bf475742860188e15d7b41', 150);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-151::mrego::(Checksum: 3:003b820bd40132476357aae25ba7d50b)
ALTER TABLE resources_cost_category_assignment ADD CONSTRAINT resources_cost_category_assignment_code_key UNIQUE (code);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-151', '2.0-rc7', '3:003b820bd40132476357aae25ba7d50b', 151);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-152::mrego::(Checksum: 3:cbe9e0b14bac88a7b895d8bc4c4777d7)
ALTER TABLE task ADD CONSTRAINT task_subcontrated_task_data_id_key UNIQUE (subcontrated_task_data_id);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-152', '2.0-rc7', '3:cbe9e0b14bac88a7b895d8bc4c4777d7', 152);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-153::mrego::(Checksum: 3:38dc59e34869bc2425c7c820ba45cb9b)
ALTER TABLE task_source ADD CONSTRAINT task_source_schedulingdata_key UNIQUE (schedulingdata);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-153', '2.0-rc7', '3:38dc59e34869bc2425c7c820ba45cb9b', 153);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-154::mrego::(Checksum: 3:1822fb3caf8d43038c67a53fdb75e432)
ALTER TABLE type_of_work_hours ADD CONSTRAINT type_of_work_hours_code_key UNIQUE (code);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-154', '2.0-rc7', '3:1822fb3caf8d43038c67a53fdb75e432', 154);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-155::mrego::(Checksum: 3:8416952db331ffa2dd27dc4f5734036c)
ALTER TABLE type_of_work_hours ADD CONSTRAINT type_of_work_hours_name_key UNIQUE (name);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-155', '2.0-rc7', '3:8416952db331ffa2dd27dc4f5734036c', 155);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-156::mrego::(Checksum: 3:4c2b2e5a5a4ee0175cab19c8b6ee084c)
ALTER TABLE unit_type ADD CONSTRAINT unit_type_code_key UNIQUE (code);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-156', '2.0-rc7', '3:4c2b2e5a5a4ee0175cab19c8b6ee084c', 156);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-157::mrego::(Checksum: 3:c5bf072f2de5890c8b3e22c07577f5ad)
ALTER TABLE user_table ADD CONSTRAINT user_table_login_name_key UNIQUE (login_name);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-157', '2.0-rc7', '3:c5bf072f2de5890c8b3e22c07577f5ad', 157);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-158::mrego::(Checksum: 3:61b84144638971ed2b9f8a2a0fb1484f)
ALTER TABLE work_report ADD CONSTRAINT work_report_code_key UNIQUE (code);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-158', '2.0-rc7', '3:61b84144638971ed2b9f8a2a0fb1484f', 158);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-159::mrego::(Checksum: 3:edef41963c0d8e8d51ad15aa7a757420)
ALTER TABLE work_report_line ADD CONSTRAINT work_report_line_code_key UNIQUE (code);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-159', '2.0-rc7', '3:edef41963c0d8e8d51ad15aa7a757420', 159);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-160::mrego::(Checksum: 3:6d94d16cf714dd8872660b484e632513)
ALTER TABLE work_report_type ADD CONSTRAINT work_report_type_code_key UNIQUE (code);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-160', '2.0-rc7', '3:6d94d16cf714dd8872660b484e632513', 160);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-161::mrego::(Checksum: 3:c47020da3e40d9b6239153776affab17)
ALTER TABLE work_report_type ADD CONSTRAINT work_report_type_name_key UNIQUE (name);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-161', '2.0-rc7', '3:c47020da3e40d9b6239153776affab17', 161);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-162::mrego::(Checksum: 3:8c1de34a4070d7a104ef5595ff365355)
ALTER TABLE advance_assignment ADD CONSTRAINT fkd4192eaab216ed4c FOREIGN KEY (advance_type_id) REFERENCES advance_type(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-162', '2.0-rc7', '3:8c1de34a4070d7a104ef5595ff365355', 162);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-163::mrego::(Checksum: 3:00c8bf9c83805a10ad1abbfe6cabdba8)
ALTER TABLE advance_assignment_template ADD CONSTRAINT fkd9bfae6fb216ed4c FOREIGN KEY (advance_type_id) REFERENCES advance_type(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-163', '2.0-rc7', '3:00c8bf9c83805a10ad1abbfe6cabdba8', 163);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-164::mrego::(Checksum: 3:3fa310189e738082742a06c3751f2f3e)
ALTER TABLE advance_assignment_template ADD CONSTRAINT fkd9bfae6f19b9dfde FOREIGN KEY (order_element_template_id) REFERENCES order_element_template(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-164', '2.0-rc7', '3:3fa310189e738082742a06c3751f2f3e', 164);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-165::mrego::(Checksum: 3:0ed5111ef301a24755a17b99ba609336)
ALTER TABLE advance_measurement ADD CONSTRAINT fkdaba2f7f2f2d3aec FOREIGN KEY (advance_assignment_id) REFERENCES advance_assignment(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-165', '2.0-rc7', '3:0ed5111ef301a24755a17b99ba609336', 165);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-166::mrego::(Checksum: 3:f781f197771175ae97a798ae4c732613)
ALTER TABLE advance_measurement ADD CONSTRAINT fkdaba2f7fa9e53843 FOREIGN KEY (advance_assignment_id) REFERENCES direct_advance_assignment(advance_assignment_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-166', '2.0-rc7', '3:f781f197771175ae97a798ae4c732613', 166);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-167::mrego::(Checksum: 3:f82641b8c47ac3372c38bd410315f1d7)
ALTER TABLE all_criterions ADD CONSTRAINT fk7980035061f02c44 FOREIGN KEY (criterion_id) REFERENCES criterion(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-167', '2.0-rc7', '3:f82641b8c47ac3372c38bd410315f1d7', 167);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-168::mrego::(Checksum: 3:e819b9f5b305d94a874ffff77f1b0b00)
ALTER TABLE all_criterions ADD CONSTRAINT fk79800350b1524a73 FOREIGN KEY (generic_resource_allocation_id) REFERENCES generic_resource_allocation(resource_allocation_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-168', '2.0-rc7', '3:e819b9f5b305d94a874ffff77f1b0b00', 168);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-169::mrego::(Checksum: 3:e638fd6a15647613720dfab7af6c67af)
ALTER TABLE calendar_availability ADD CONSTRAINT fk557738bca44abee3 FOREIGN KEY (base_calendar_id) REFERENCES base_calendar(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-169', '2.0-rc7', '3:e638fd6a15647613720dfab7af6c67af', 169);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-170::mrego::(Checksum: 3:5f2cae397111f9c4c4112a513b9e811f)
ALTER TABLE calendar_data ADD CONSTRAINT fk74fc040ba44abee3 FOREIGN KEY (base_calendar_id) REFERENCES base_calendar(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-170', '2.0-rc7', '3:5f2cae397111f9c4c4112a513b9e811f', 170);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-171::mrego::(Checksum: 3:0cd6d510389a9b5c7e7707baabf175d1)
ALTER TABLE calendar_data ADD CONSTRAINT fk74fc040b7fa34e3f FOREIGN KEY (parent) REFERENCES base_calendar(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-171', '2.0-rc7', '3:0cd6d510389a9b5c7e7707baabf175d1', 171);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-172::mrego::(Checksum: 3:1e0dbe21557bd995cc4e10c17b06ad77)
ALTER TABLE calendar_exception ADD CONSTRAINT fka215508ea44abee3 FOREIGN KEY (base_calendar_id) REFERENCES base_calendar(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-172', '2.0-rc7', '3:1e0dbe21557bd995cc4e10c17b06ad77', 172);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-173::mrego::(Checksum: 3:3c00120a79d285e58d49014dc3b3def2)
ALTER TABLE calendar_exception ADD CONSTRAINT fka215508eadad7e51 FOREIGN KEY (calendar_exception_id) REFERENCES calendar_exception_type(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-173', '2.0-rc7', '3:3c00120a79d285e58d49014dc3b3def2', 173);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-174::mrego::(Checksum: 3:a5d223789fb73e1b821bdd45c72478c8)
ALTER TABLE configuration ADD CONSTRAINT fk733374f6cc119699 FOREIGN KEY (configuration_id) REFERENCES base_calendar(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-174', '2.0-rc7', '3:a5d223789fb73e1b821bdd45c72478c8', 174);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-175::mrego::(Checksum: 3:b7e560667260a76220664084e75a699b)
ALTER TABLE consolidated_value ADD CONSTRAINT fk35588e2db96bba28 FOREIGN KEY (advance_measurement_id) REFERENCES advance_measurement(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-175', '2.0-rc7', '3:b7e560667260a76220664084e75a699b', 175);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-176::mrego::(Checksum: 3:a04c06f20662d01674c2481258088719)
ALTER TABLE consolidated_value ADD CONSTRAINT fk35588e2d3ae24ff8 FOREIGN KEY (consolidation_id) REFERENCES consolidation(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-176', '2.0-rc7', '3:a04c06f20662d01674c2481258088719', 176);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-177::mrego::(Checksum: 3:eda6446777e754a9b28667808386fc07)
ALTER TABLE consolidation ADD CONSTRAINT fkc9400e2c9f1d6611 FOREIGN KEY (dir_advance_assignment_id) REFERENCES direct_advance_assignment(advance_assignment_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-177', '2.0-rc7', '3:eda6446777e754a9b28667808386fc07', 177);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-178::mrego::(Checksum: 3:cc43a952d1f4e90ae8ca08a86400b5b8)
ALTER TABLE consolidation ADD CONSTRAINT fkc9400e2cff2b2ba3 FOREIGN KEY (id) REFERENCES task(task_element_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-178', '2.0-rc7', '3:cc43a952d1f4e90ae8ca08a86400b5b8', 178);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-179::mrego::(Checksum: 3:3b7feac7849308d546f9e32306a0337b)
ALTER TABLE consolidation ADD CONSTRAINT fkc9400e2c430ea1de FOREIGN KEY (ind_advance_assignment_id) REFERENCES indirect_advance_assignment(advance_assignment_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-179', '2.0-rc7', '3:3b7feac7849308d546f9e32306a0337b', 179);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-180::mrego::(Checksum: 3:626dfbab1f6c43cbf5eda15af6c2d0b4)
ALTER TABLE criterion ADD CONSTRAINT fk16e20ea114a5c61 FOREIGN KEY (id_criterion_type) REFERENCES criterion_type(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-180', '2.0-rc7', '3:626dfbab1f6c43cbf5eda15af6c2d0b4', 180);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-181::mrego::(Checksum: 3:5d35e584fe5144871639463ab48448d9)
ALTER TABLE criterion ADD CONSTRAINT fk16e20ea13a156175 FOREIGN KEY (parent) REFERENCES criterion(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-181', '2.0-rc7', '3:5d35e584fe5144871639463ab48448d9', 181);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-182::mrego::(Checksum: 3:54f532733816ed1173eb0a0978bc5d80)
ALTER TABLE criterion_requirement ADD CONSTRAINT fkef395fa561f02c44 FOREIGN KEY (criterion_id) REFERENCES criterion(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-182', '2.0-rc7', '3:54f532733816ed1173eb0a0978bc5d80', 182);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-183::mrego::(Checksum: 3:edf8db4cfb961a06787936314df7b94e)
ALTER TABLE criterion_requirement ADD CONSTRAINT fkef395fa5e036cfed FOREIGN KEY (hours_group_id) REFERENCES hours_group(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-183', '2.0-rc7', '3:edf8db4cfb961a06787936314df7b94e', 183);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-184::mrego::(Checksum: 3:1e122af08a912e15ee5c66f91b6c5bfa)
ALTER TABLE criterion_requirement ADD CONSTRAINT fkef395fa5efda874f FOREIGN KEY (order_element_id) REFERENCES order_element(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-184', '2.0-rc7', '3:1e122af08a912e15ee5c66f91b6c5bfa', 184);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-185::mrego::(Checksum: 3:49c144bddd204a4b53a3fa24016b0be2)
ALTER TABLE criterion_requirement ADD CONSTRAINT fkef395fa519b9dfde FOREIGN KEY (order_element_template_id) REFERENCES order_element_template(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-185', '2.0-rc7', '3:49c144bddd204a4b53a3fa24016b0be2', 185);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-186::mrego::(Checksum: 3:7f27b6d4f3c5e0b00fb64f7131248b36)
ALTER TABLE criterion_requirement ADD CONSTRAINT fkef395fa5f41d57f2 FOREIGN KEY (parent) REFERENCES criterion_requirement(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-186', '2.0-rc7', '3:7f27b6d4f3c5e0b00fb64f7131248b36', 186);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-187::mrego::(Checksum: 3:9c92c633ec12d7a6df8b65c97ad0fa35)
ALTER TABLE criterion_satisfaction ADD CONSTRAINT fk225ff96a8c4c676c FOREIGN KEY (criterion) REFERENCES criterion(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-187', '2.0-rc7', '3:9c92c633ec12d7a6df8b65c97ad0fa35', 187);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-188::mrego::(Checksum: 3:94a94a8ce91fc86d16d4fa653a0d1453)
ALTER TABLE criterion_satisfaction ADD CONSTRAINT fk225ff96aeae850b2 FOREIGN KEY (resource) REFERENCES resource(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-188', '2.0-rc7', '3:94a94a8ce91fc86d16d4fa653a0d1453', 188);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-189::mrego::(Checksum: 3:b338072cafac27857c87f450a2ad6c46)
ALTER TABLE day_assignment ADD CONSTRAINT fkbb493f50510e7a78 FOREIGN KEY (derived_container_id) REFERENCES derived_day_assignments_container(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-189', '2.0-rc7', '3:b338072cafac27857c87f450a2ad6c46', 189);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-190::mrego::(Checksum: 3:9d5bd211cfd49f3b9fc91441f5eb908f)
ALTER TABLE day_assignment ADD CONSTRAINT fkbb493f5019256004 FOREIGN KEY (generic_container_id) REFERENCES generic_day_assignments_container(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-190', '2.0-rc7', '3:9d5bd211cfd49f3b9fc91441f5eb908f', 190);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-191::mrego::(Checksum: 3:bc7668fdd82c710bf043fa2f8a95dbeb)
ALTER TABLE day_assignment ADD CONSTRAINT fkbb493f5048d21790 FOREIGN KEY (resource_id) REFERENCES resource(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-191', '2.0-rc7', '3:bc7668fdd82c710bf043fa2f8a95dbeb', 191);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-192::mrego::(Checksum: 3:9a377a1c95da43a4a33381c22ed4e951)
ALTER TABLE day_assignment ADD CONSTRAINT fkbb493f50756348a8 FOREIGN KEY (specific_container_id) REFERENCES specific_day_assignments_container(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-192', '2.0-rc7', '3:9a377a1c95da43a4a33381c22ed4e951', 192);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-193::mrego::(Checksum: 3:73a6387f780aa23e047099f18b15b50f)
ALTER TABLE dependency ADD CONSTRAINT fkfe6ed34be838f362 FOREIGN KEY (destination) REFERENCES task_element(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-193', '2.0-rc7', '3:73a6387f780aa23e047099f18b15b50f', 193);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-194::mrego::(Checksum: 3:8b99d9b4b1544d9b15595cfcc5e5ec4f)
ALTER TABLE dependency ADD CONSTRAINT fkfe6ed34b1545e7a FOREIGN KEY (origin) REFERENCES task_element(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-194', '2.0-rc7', '3:8b99d9b4b1544d9b15595cfcc5e5ec4f', 194);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-195::mrego::(Checksum: 3:c267aaa5cd192977a88171f504e04f29)
ALTER TABLE dependency ADD CONSTRAINT fkfe6ed34b9e788f90 FOREIGN KEY (queue_dependency) REFERENCES limiting_resource_queue_dependency(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-195', '2.0-rc7', '3:c267aaa5cd192977a88171f504e04f29', 195);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-196::mrego::(Checksum: 3:8667c573c47e4d0fbcc32caa84df333a)
ALTER TABLE derived_allocation ADD CONSTRAINT fkf0260c4287b470f0 FOREIGN KEY (configurationunit) REFERENCES machine_workers_configuration_unit(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-196', '2.0-rc7', '3:8667c573c47e4d0fbcc32caa84df333a', 196);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-197::mrego::(Checksum: 3:f11c3d339e18a174df76d41cd195f9de)
ALTER TABLE derived_allocation ADD CONSTRAINT fkf0260c4275ed79ba FOREIGN KEY (resource_allocation_id) REFERENCES resource_allocation(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-197', '2.0-rc7', '3:f11c3d339e18a174df76d41cd195f9de', 197);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-198::mrego::(Checksum: 3:05a89763fa1de7b97f16102791cfefed)
ALTER TABLE derived_day_assignments_container ADD CONSTRAINT fkb83176c31b8e7cf2 FOREIGN KEY (derived_allocation_id) REFERENCES derived_allocation(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-198', '2.0-rc7', '3:05a89763fa1de7b97f16102791cfefed', 198);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-199::mrego::(Checksum: 3:c14c67e4fab2a4378ba63d3da1fb628a)
ALTER TABLE derived_day_assignments_container ADD CONSTRAINT fkb83176c3421c7cf4 FOREIGN KEY (scenario) REFERENCES scenario(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-199', '2.0-rc7', '3:c14c67e4fab2a4378ba63d3da1fb628a', 199);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-200::mrego::(Checksum: 3:382f1905a4228bd3e9f0c040c970cde3)
ALTER TABLE description_values ADD CONSTRAINT fk7d1ee2c5fec79eb0 FOREIGN KEY (description_value_id) REFERENCES work_report(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-200', '2.0-rc7', '3:382f1905a4228bd3e9f0c040c970cde3', 200);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-201::mrego::(Checksum: 3:1bac4b4ae207d8838f091c32cb2df31e)
ALTER TABLE description_values_in_line ADD CONSTRAINT fk1e6bf5475c390c4 FOREIGN KEY (description_value_id) REFERENCES work_report_line(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-201', '2.0-rc7', '3:1bac4b4ae207d8838f091c32cb2df31e', 201);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-202::mrego::(Checksum: 3:40a79714aa74711e54e351fd7ae769a8)
ALTER TABLE direct_advance_assignment ADD CONSTRAINT fk172a16c02f2d3aec FOREIGN KEY (advance_assignment_id) REFERENCES advance_assignment(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-202', '2.0-rc7', '3:40a79714aa74711e54e351fd7ae769a8', 202);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-203::mrego::(Checksum: 3:a1500cdb8370f8da80715f3150d2da41)
ALTER TABLE direct_advance_assignment ADD CONSTRAINT fk172a16c0a1127ce5 FOREIGN KEY (direct_order_element_id) REFERENCES order_element(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-203', '2.0-rc7', '3:a1500cdb8370f8da80715f3150d2da41', 203);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-204::mrego::(Checksum: 3:7ed433c571a7829ad2eb6e5389433468)
ALTER TABLE effort_per_day ADD CONSTRAINT fkc405554bfd5e49bc FOREIGN KEY (base_calendar_id) REFERENCES calendar_data(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-204', '2.0-rc7', '3:7ed433c571a7829ad2eb6e5389433468', 204);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-205::mrego::(Checksum: 3:684a3d66bf7275618fdaf47f577dca5f)
ALTER TABLE external_company ADD CONSTRAINT fkd8e14c09b36a6d51 FOREIGN KEY (company_user) REFERENCES user_table(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-205', '2.0-rc7', '3:684a3d66bf7275618fdaf47f577dca5f', 205);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-206::mrego::(Checksum: 3:f90e8c7e57222696be65ba1a75afe0e1)
ALTER TABLE generic_day_assignments_container ADD CONSTRAINT fkc01655fdee970b FOREIGN KEY (resource_allocation_id) REFERENCES generic_resource_allocation(resource_allocation_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-206', '2.0-rc7', '3:f90e8c7e57222696be65ba1a75afe0e1', 206);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-207::mrego::(Checksum: 3:d903424e7e44b1c1fb8c9d00bbaa5ff9)
ALTER TABLE generic_day_assignments_container ADD CONSTRAINT fkc01655fd421c7cf4 FOREIGN KEY (scenario) REFERENCES scenario(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-207', '2.0-rc7', '3:d903424e7e44b1c1fb8c9d00bbaa5ff9', 207);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-208::mrego::(Checksum: 3:6f03b7841548675d0c674dfc8a58a76a)
ALTER TABLE generic_resource_allocation ADD CONSTRAINT fkf788b34975ed79ba FOREIGN KEY (resource_allocation_id) REFERENCES resource_allocation(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-208', '2.0-rc7', '3:6f03b7841548675d0c674dfc8a58a76a', 208);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-209::mrego::(Checksum: 3:69684a0db21bbdbec2ffba0b151a73bd)
ALTER TABLE heading_field ADD CONSTRAINT fk2908787d415884f6 FOREIGN KEY (heading_id) REFERENCES work_report_type(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-209', '2.0-rc7', '3:69684a0db21bbdbec2ffba0b151a73bd', 209);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-210::mrego::(Checksum: 3:70d4db37a0127109f4e99eede4265e9e)
ALTER TABLE hour_cost ADD CONSTRAINT fk3b9a8148c29ad8eb FOREIGN KEY (cost_category_id) REFERENCES cost_category(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-210', '2.0-rc7', '3:70d4db37a0127109f4e99eede4265e9e', 210);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-211::mrego::(Checksum: 3:0dc5817e042530305a63787cd51c3d44)
ALTER TABLE hour_cost ADD CONSTRAINT fk3b9a8148d5b6184d FOREIGN KEY (type_of_work_hours_id) REFERENCES type_of_work_hours(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-211', '2.0-rc7', '3:0dc5817e042530305a63787cd51c3d44', 211);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-212::mrego::(Checksum: 3:1b87cb4d62001fa3a6cd5df2bfee271f)
ALTER TABLE hours_group ADD CONSTRAINT fkd9b7594f8bdc6ac6 FOREIGN KEY (order_line_template) REFERENCES order_line_template(order_line_template_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-212', '2.0-rc7', '3:1b87cb4d62001fa3a6cd5df2bfee271f', 212);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-213::mrego::(Checksum: 3:43bb14279d4807f3bb02f8c800ac6ec6)
ALTER TABLE hours_group ADD CONSTRAINT fkd9b7594f1ed629ea FOREIGN KEY (parent_order_line) REFERENCES order_line(order_element_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-213', '2.0-rc7', '3:43bb14279d4807f3bb02f8c800ac6ec6', 213);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-214::mrego::(Checksum: 3:2250070459940c11e7ff31a4192c9355)
ALTER TABLE indirect_advance_assignment ADD CONSTRAINT fk64cbad3b2f2d3aec FOREIGN KEY (advance_assignment_id) REFERENCES advance_assignment(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-214', '2.0-rc7', '3:2250070459940c11e7ff31a4192c9355', 214);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-215::mrego::(Checksum: 3:1abe6242372fb6a1b8b7d5ed11c40cf6)
ALTER TABLE indirect_advance_assignment ADD CONSTRAINT fk64cbad3b218d7620 FOREIGN KEY (indirect_order_element_id) REFERENCES order_element(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-215', '2.0-rc7', '3:1abe6242372fb6a1b8b7d5ed11c40cf6', 215);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-216::mrego::(Checksum: 3:365ccb2007a7db5a42a8558640d149e9)
ALTER TABLE indirect_advance_assignment ADD CONSTRAINT fk64cbad3b8202350f FOREIGN KEY (indirect_order_element_id) REFERENCES order_line_group(order_element_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-216', '2.0-rc7', '3:365ccb2007a7db5a42a8558640d149e9', 216);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-217::mrego::(Checksum: 3:ec8d6d84ea00f6c7ece30c6d1ada877d)
ALTER TABLE label ADD CONSTRAINT fk61f7ef4707cd777 FOREIGN KEY (label_type_id) REFERENCES label_type(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-217', '2.0-rc7', '3:ec8d6d84ea00f6c7ece30c6d1ada877d', 217);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-218::mrego::(Checksum: 3:00a5c1aff60bf17c4f63c781d3d8868f)
ALTER TABLE limiting_resource_queue ADD CONSTRAINT fk7e57469848d21790 FOREIGN KEY (resource_id) REFERENCES resource(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-218', '2.0-rc7', '3:00a5c1aff60bf17c4f63c781d3d8868f', 218);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-219::mrego::(Checksum: 3:80d78dda5a5d7a6e5b490c9b28841b16)
ALTER TABLE limiting_resource_queue_dependency ADD CONSTRAINT fk59485352e42f8d29 FOREIGN KEY (destiny_queue_element_id) REFERENCES limiting_resource_queue_element(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-219', '2.0-rc7', '3:80d78dda5a5d7a6e5b490c9b28841b16', 219);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-220::mrego::(Checksum: 3:3a8fd566182b2a101e0e843990cfa8df)
ALTER TABLE limiting_resource_queue_dependency ADD CONSTRAINT fk5948535228f2695 FOREIGN KEY (origin_queue_element_id) REFERENCES limiting_resource_queue_element(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-220', '2.0-rc7', '3:3a8fd566182b2a101e0e843990cfa8df', 220);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-221::mrego::(Checksum: 3:229616a00b74e7128b9eeb41293cd91e)
ALTER TABLE limiting_resource_queue_element ADD CONSTRAINT fk1fc5f455bd2209e8 FOREIGN KEY (limiting_resource_queue_id) REFERENCES limiting_resource_queue(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-221', '2.0-rc7', '3:229616a00b74e7128b9eeb41293cd91e', 221);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-222::mrego::(Checksum: 3:ed9f60b9f8d7881c11360f2a020bdf4b)
ALTER TABLE limiting_resource_queue_element ADD CONSTRAINT fk1fc5f45575ed79ba FOREIGN KEY (resource_allocation_id) REFERENCES resource_allocation(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-222', '2.0-rc7', '3:ed9f60b9f8d7881c11360f2a020bdf4b', 222);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-223::mrego::(Checksum: 3:fee2321152589ea84c5743175d4c180a)
ALTER TABLE line_field ADD CONSTRAINT fk88eecef415884f6 FOREIGN KEY (heading_id) REFERENCES work_report_type(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-223', '2.0-rc7', '3:fee2321152589ea84c5743175d4c180a', 223);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-224::mrego::(Checksum: 3:f6ef1f3ee7da7a147539b46f9c1cf60b)
ALTER TABLE machine ADD CONSTRAINT fk31314447937680b7 FOREIGN KEY (machine_id) REFERENCES resource(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-224', '2.0-rc7', '3:f6ef1f3ee7da7a147539b46f9c1cf60b', 224);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-225::mrego::(Checksum: 3:21557deb9ad7b4c25504862864502b1c)
ALTER TABLE machine_configuration_unit_required_criterions ADD CONSTRAINT fk95548d7861f02c44 FOREIGN KEY (criterion_id) REFERENCES criterion(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-225', '2.0-rc7', '3:21557deb9ad7b4c25504862864502b1c', 225);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-226::mrego::(Checksum: 3:24adfcd3a41ae5648ffc2002dae0e4ef)
ALTER TABLE machine_configuration_unit_required_criterions ADD CONSTRAINT fk95548d7875999a91 FOREIGN KEY (id) REFERENCES machine_workers_configuration_unit(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-226', '2.0-rc7', '3:24adfcd3a41ae5648ffc2002dae0e4ef', 226);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-227::mrego::(Checksum: 3:bdd3cffbe9466ccbf6a1a791d61230fd)
ALTER TABLE machine_worker_assignment ADD CONSTRAINT fka2c1583686b2de7a FOREIGN KEY (configuration_id) REFERENCES machine_workers_configuration_unit(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-227', '2.0-rc7', '3:bdd3cffbe9466ccbf6a1a791d61230fd', 227);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-228::mrego::(Checksum: 3:2182868d8f52e97c110e8901f8e2fc34)
ALTER TABLE machine_worker_assignment ADD CONSTRAINT fka2c158369bebcf10 FOREIGN KEY (worker_id) REFERENCES worker(worker_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-228', '2.0-rc7', '3:2182868d8f52e97c110e8901f8e2fc34', 228);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-229::mrego::(Checksum: 3:43b57b5e0d38ae69053bfcaf10d4ba8d)
ALTER TABLE machine_workers_configuration_unit ADD CONSTRAINT fkd47281efffeb5538 FOREIGN KEY (machine) REFERENCES machine(machine_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-229', '2.0-rc7', '3:43b57b5e0d38ae69053bfcaf10d4ba8d', 229);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-230::mrego::(Checksum: 3:96d1199f9a6a19da0e6bac41293cf0ab)
ALTER TABLE material ADD CONSTRAINT fk11d365279578651e FOREIGN KEY (category_id) REFERENCES material_category(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-230', '2.0-rc7', '3:96d1199f9a6a19da0e6bac41293cf0ab', 230);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-231::mrego::(Checksum: 3:d493ad51ca6712c994efb741c1cd5cee)
ALTER TABLE material ADD CONSTRAINT fk11d36527f11b2d0 FOREIGN KEY (unit_type) REFERENCES unit_type(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-231', '2.0-rc7', '3:d493ad51ca6712c994efb741c1cd5cee', 231);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-232::mrego::(Checksum: 3:611e912194a15238fe4fd70e46ecd271)
ALTER TABLE material_assigment ADD CONSTRAINT fkb3e9425b5c68337 FOREIGN KEY (material_id) REFERENCES material(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-232', '2.0-rc7', '3:611e912194a15238fe4fd70e46ecd271', 232);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-233::mrego::(Checksum: 3:4823fd19347ba5a9c14b862fac025901)
ALTER TABLE material_assigment ADD CONSTRAINT fkb3e9425efda874f FOREIGN KEY (order_element_id) REFERENCES order_element(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-233', '2.0-rc7', '3:4823fd19347ba5a9c14b862fac025901', 233);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-234::mrego::(Checksum: 3:7aa2f827cf3710d979cd5c62770417bb)
ALTER TABLE material_assigment_template ADD CONSTRAINT fk35254354b5c68337 FOREIGN KEY (material_id) REFERENCES material(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-234', '2.0-rc7', '3:7aa2f827cf3710d979cd5c62770417bb', 234);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-235::mrego::(Checksum: 3:1d77a4d184e0f24891d62762883540af)
ALTER TABLE material_assigment_template ADD CONSTRAINT fk3525435419b9dfde FOREIGN KEY (order_element_template_id) REFERENCES order_element_template(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-235', '2.0-rc7', '3:1d77a4d184e0f24891d62762883540af', 235);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-236::mrego::(Checksum: 3:354ab31aec28364faae025882682084a)
ALTER TABLE material_category ADD CONSTRAINT fk84c66516b53669f2 FOREIGN KEY (parent_id) REFERENCES material_category(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-236', '2.0-rc7', '3:354ab31aec28364faae025882682084a', 236);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-237::mrego::(Checksum: 3:fb6d4b2090d92c3a4d685cb13176ee98)
ALTER TABLE order_authorization ADD CONSTRAINT fkb92b594887287288 FOREIGN KEY (order_id) REFERENCES order_table(order_element_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-237', '2.0-rc7', '3:fb6d4b2090d92c3a4d685cb13176ee98', 237);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-238::mrego::(Checksum: 3:e91f5bf74998ca513288ed138c0da97d)
ALTER TABLE order_authorization ADD CONSTRAINT fkb92b5948edc4db41 FOREIGN KEY (profile_id) REFERENCES profile_table(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-238', '2.0-rc7', '3:e91f5bf74998ca513288ed138c0da97d', 238);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-239::mrego::(Checksum: 3:f07664fc56241ad077820f8da9bad209)
ALTER TABLE order_authorization ADD CONSTRAINT fkb92b59485567ad13 FOREIGN KEY (user_id) REFERENCES user_table(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-239', '2.0-rc7', '3:f07664fc56241ad077820f8da9bad209', 239);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-240::mrego::(Checksum: 3:3cf705e46d55107e249d5a69dbfbe650)
ALTER TABLE order_element ADD CONSTRAINT fk92271f0b1e635c19 FOREIGN KEY (parent) REFERENCES order_line_group(order_element_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-240', '2.0-rc7', '3:3cf705e46d55107e249d5a69dbfbe650', 240);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-241::mrego::(Checksum: 3:3de02419d8b4096899d78e2cd84d7e0b)
ALTER TABLE order_element ADD CONSTRAINT fk92271f0b7ec17fa6 FOREIGN KEY (sum_charged_hours_id) REFERENCES sum_charged_hours(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-241', '2.0-rc7', '3:3de02419d8b4096899d78e2cd84d7e0b', 241);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-242::mrego::(Checksum: 3:4b074cd6de5562d41ae3506d8d4b35f3)
ALTER TABLE order_element ADD CONSTRAINT fk92271f0bd97bcc8c FOREIGN KEY (template) REFERENCES order_element_template(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-242', '2.0-rc7', '3:4b074cd6de5562d41ae3506d8d4b35f3', 242);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-243::mrego::(Checksum: 3:b577e13ab196e4c075b8be09841abb4a)
ALTER TABLE order_element_label ADD CONSTRAINT fke8344cc0c1c2746e FOREIGN KEY (label_id) REFERENCES label(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-243', '2.0-rc7', '3:b577e13ab196e4c075b8be09841abb4a', 243);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-244::mrego::(Checksum: 3:9ad3d17bbbac8f995a1313d24c37c816)
ALTER TABLE order_element_label ADD CONSTRAINT fke8344cc0efda874f FOREIGN KEY (order_element_id) REFERENCES order_element(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-244', '2.0-rc7', '3:9ad3d17bbbac8f995a1313d24c37c816', 244);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-245::mrego::(Checksum: 3:6ed5e004af84bf9f2cfe8858fd2a5693)
ALTER TABLE order_element_template ADD CONSTRAINT fk4339b2e41638aab FOREIGN KEY (parent) REFERENCES order_line_group_template(group_template_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-245', '2.0-rc7', '3:6ed5e004af84bf9f2cfe8858fd2a5693', 245);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-246::mrego::(Checksum: 3:72287bc729bb6699bb3bc2210d12dfd4)
ALTER TABLE order_element_template_label ADD CONSTRAINT fk616de2a3c1c2746e FOREIGN KEY (label_id) REFERENCES label(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-246', '2.0-rc7', '3:72287bc729bb6699bb3bc2210d12dfd4', 246);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-247::mrego::(Checksum: 3:cb0dc7b3886d0b94d5052539bd683a16)
ALTER TABLE order_element_template_label ADD CONSTRAINT fk616de2a319b9dfde FOREIGN KEY (order_element_template_id) REFERENCES order_element_template(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-247', '2.0-rc7', '3:cb0dc7b3886d0b94d5052539bd683a16', 247);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-248::mrego::(Checksum: 3:359b88a585529cbf0d688ded52661d0d)
ALTER TABLE order_element_template_quality_form ADD CONSTRAINT fkfe6fb57519b9dfde FOREIGN KEY (order_element_template_id) REFERENCES order_element_template(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-248', '2.0-rc7', '3:359b88a585529cbf0d688ded52661d0d', 248);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-249::mrego::(Checksum: 3:bc3e3f311891541c94bafaa8250b3837)
ALTER TABLE order_element_template_quality_form ADD CONSTRAINT fkfe6fb5758b37665c FOREIGN KEY (quality_form_id) REFERENCES quality_form(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-249', '2.0-rc7', '3:bc3e3f311891541c94bafaa8250b3837', 249);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-250::mrego::(Checksum: 3:873b0ba69634f01ec622ca9b24d9e70a)
ALTER TABLE order_line ADD CONSTRAINT fk2d124245efda874f FOREIGN KEY (order_element_id) REFERENCES order_element(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-250', '2.0-rc7', '3:873b0ba69634f01ec622ca9b24d9e70a', 250);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-251::mrego::(Checksum: 3:c137a192ad74895a6049234b596fecb5)
ALTER TABLE order_line_group ADD CONSTRAINT fkdce41405efda874f FOREIGN KEY (order_element_id) REFERENCES order_element(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-251', '2.0-rc7', '3:c137a192ad74895a6049234b596fecb5', 251);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-252::mrego::(Checksum: 3:793cd0492af23c4c209def47673ca914)
ALTER TABLE order_line_group_template ADD CONSTRAINT fk9c819f74ddc82952 FOREIGN KEY (group_template_id) REFERENCES order_element_template(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-252', '2.0-rc7', '3:793cd0492af23c4c209def47673ca914', 252);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-253::mrego::(Checksum: 3:d76b0b4675c504576f872e68c5e9bda7)
ALTER TABLE order_line_template ADD CONSTRAINT fke4c379349fb7fc18 FOREIGN KEY (order_line_template_id) REFERENCES order_element_template(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-253', '2.0-rc7', '3:d76b0b4675c504576f872e68c5e9bda7', 253);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-254::mrego::(Checksum: 3:eee13cf5dc3e864e56c6a9b03079578e)
ALTER TABLE order_table ADD CONSTRAINT fk75a2f39da44abee3 FOREIGN KEY (base_calendar_id) REFERENCES base_calendar(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-254', '2.0-rc7', '3:eee13cf5dc3e864e56c6a9b03079578e', 254);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-255::mrego::(Checksum: 3:f9088ee9f92067cab4b207d3aae3a37b)
ALTER TABLE order_table ADD CONSTRAINT fk75a2f39d4ec080cf FOREIGN KEY (customer) REFERENCES external_company(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-255', '2.0-rc7', '3:f9088ee9f92067cab4b207d3aae3a37b', 255);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-256::mrego::(Checksum: 3:e2326617f1c9b860249a0c0777ec20f6)
ALTER TABLE order_table ADD CONSTRAINT fk75a2f39d504f463e FOREIGN KEY (order_element_id) REFERENCES order_line_group(order_element_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-256', '2.0-rc7', '3:e2326617f1c9b860249a0c0777ec20f6', 256);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-257::mrego::(Checksum: 3:adf9400fc812eb6c8f7d32aa787742e6)
ALTER TABLE order_template ADD CONSTRAINT fk6476ce4ba44abee3 FOREIGN KEY (base_calendar_id) REFERENCES base_calendar(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-257', '2.0-rc7', '3:adf9400fc812eb6c8f7d32aa787742e6', 257);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-258::mrego::(Checksum: 3:60fc7ab467e364cd6a3d72d2f4f1d736)
ALTER TABLE order_template ADD CONSTRAINT fk6476ce4b9a4a7d90 FOREIGN KEY (order_template_id) REFERENCES order_line_group_template(group_template_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-258', '2.0-rc7', '3:60fc7ab467e364cd6a3d72d2f4f1d736', 258);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-259::mrego::(Checksum: 3:a6cacd22103e0447094d9a161d7b7445)
ALTER TABLE order_version ADD CONSTRAINT fka391ce7a2380ca7 FOREIGN KEY (ownerscenario) REFERENCES scenario(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-259', '2.0-rc7', '3:a6cacd22103e0447094d9a161d7b7445', 259);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-260::mrego::(Checksum: 3:76bf1eb3a6880473927e7a101d037bd8)
ALTER TABLE planning_data ADD CONSTRAINT fkbb58534c6ccf87c5 FOREIGN KEY (planning_data_id) REFERENCES task_group(task_element_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-260', '2.0-rc7', '3:76bf1eb3a6880473927e7a101d037bd8', 260);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-261::mrego::(Checksum: 3:5e2d94e8e56a7c57936a8bce78066b96)
ALTER TABLE profile_roles ADD CONSTRAINT fkd557d8a7edc4db41 FOREIGN KEY (profile_id) REFERENCES profile_table(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-261', '2.0-rc7', '3:5e2d94e8e56a7c57936a8bce78066b96', 261);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-262::mrego::(Checksum: 3:b6e995091bd0a2aed951e2fbf3f215a3)
ALTER TABLE quality_form ADD CONSTRAINT fkf635cc04b216ed4c FOREIGN KEY (advance_type_id) REFERENCES advance_type(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-262', '2.0-rc7', '3:b6e995091bd0a2aed951e2fbf3f215a3', 262);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-263::mrego::(Checksum: 3:5a57fa35a8c7ca09756afcad21d8e86f)
ALTER TABLE quality_form_items ADD CONSTRAINT fkad6003c58b37665c FOREIGN KEY (quality_form_id) REFERENCES quality_form(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-263', '2.0-rc7', '3:5a57fa35a8c7ca09756afcad21d8e86f', 263);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-264::mrego::(Checksum: 3:a7fc2f018277eaaa7649fb46b6b27ab6)
ALTER TABLE resource ADD CONSTRAINT fkebabc40edc874c20 FOREIGN KEY (base_calendar_id) REFERENCES resource_calendar(base_calendar_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-264', '2.0-rc7', '3:a7fc2f018277eaaa7649fb46b6b27ab6', 264);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-265::mrego::(Checksum: 3:528d191ec33204cee277f7312c49cc2c)
ALTER TABLE resource_allocation ADD CONSTRAINT fke8a96db115671e92 FOREIGN KEY (assignment_function) REFERENCES assignment_function(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-265', '2.0-rc7', '3:528d191ec33204cee277f7312c49cc2c', 265);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-266::mrego::(Checksum: 3:deb083face872d4d49b666a9b8bdfaba)
ALTER TABLE resource_allocation ADD CONSTRAINT fke8a96db1ff61540d FOREIGN KEY (task) REFERENCES task(task_element_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-266', '2.0-rc7', '3:deb083face872d4d49b666a9b8bdfaba', 266);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-267::mrego::(Checksum: 3:a7b87e7b2788c237ebeef3591092532e)
ALTER TABLE resource_calendar ADD CONSTRAINT fk9619184fa44abee3 FOREIGN KEY (base_calendar_id) REFERENCES base_calendar(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-267', '2.0-rc7', '3:a7b87e7b2788c237ebeef3591092532e', 267);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-268::mrego::(Checksum: 3:8e9f116ff4830eb847589e5b4e62852e)
ALTER TABLE resources_cost_category_assignment ADD CONSTRAINT fk27c2a216c29ad8eb FOREIGN KEY (cost_category_id) REFERENCES cost_category(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-268', '2.0-rc7', '3:8e9f116ff4830eb847589e5b4e62852e', 268);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-269::mrego::(Checksum: 3:595c174489187fc62b19670995c5cbff)
ALTER TABLE resources_cost_category_assignment ADD CONSTRAINT fk27c2a21648d21790 FOREIGN KEY (resource_id) REFERENCES resource(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-269', '2.0-rc7', '3:595c174489187fc62b19670995c5cbff', 269);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-270::mrego::(Checksum: 3:d7a769415b86b93205327adc4b037e45)
ALTER TABLE roles_table ADD CONSTRAINT fk2e7e6bcc5567ad13 FOREIGN KEY (user_id) REFERENCES user_table(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-270', '2.0-rc7', '3:d7a769415b86b93205327adc4b037e45', 270);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-271::mrego::(Checksum: 3:06906c810ec8b347c75867ab21f7dc4c)
ALTER TABLE scenario ADD CONSTRAINT fkd1c57390fd99606d FOREIGN KEY (predecessor) REFERENCES scenario(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-271', '2.0-rc7', '3:06906c810ec8b347c75867ab21f7dc4c', 271);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-272::mrego::(Checksum: 3:a28d48ed75ff265b6472e9749f95d1a5)
ALTER TABLE scenario_orders ADD CONSTRAINT fk91bbca7487287288 FOREIGN KEY (order_id) REFERENCES order_table(order_element_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-272', '2.0-rc7', '3:a28d48ed75ff265b6472e9749f95d1a5', 272);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-273::mrego::(Checksum: 3:5990b5955a764200dd54eb4dd3b0a7cf)
ALTER TABLE scenario_orders ADD CONSTRAINT fk91bbca7423b85cf1 FOREIGN KEY (order_version_id) REFERENCES order_version(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-273', '2.0-rc7', '3:5990b5955a764200dd54eb4dd3b0a7cf', 273);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-274::mrego::(Checksum: 3:5d4cc1738a27175979f1c973b1bd5ea9)
ALTER TABLE scenario_orders ADD CONSTRAINT fk91bbca74c0fb9d8e FOREIGN KEY (scenario_id) REFERENCES scenario(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-274', '2.0-rc7', '3:5d4cc1738a27175979f1c973b1bd5ea9', 274);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-275::mrego::(Checksum: 3:d9130cc23b8afa27fd84f7c72d818efd)
ALTER TABLE scheduling_data_for_version ADD CONSTRAINT fk672eed98efda874f FOREIGN KEY (order_element_id) REFERENCES order_element(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-275', '2.0-rc7', '3:d9130cc23b8afa27fd84f7c72d818efd', 275);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-276::mrego::(Checksum: 3:18feaa2b4b9cbce46ea7b7a0c710de9f)
ALTER TABLE scheduling_states_by_order_version ADD CONSTRAINT fk84f61df1efda874f FOREIGN KEY (order_element_id) REFERENCES order_element(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-276', '2.0-rc7', '3:18feaa2b4b9cbce46ea7b7a0c710de9f', 276);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-277::mrego::(Checksum: 3:506fb6a53c157ce68105f451ad5958ff)
ALTER TABLE scheduling_states_by_order_version ADD CONSTRAINT fk84f61df123b85cf1 FOREIGN KEY (order_version_id) REFERENCES order_version(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-277', '2.0-rc7', '3:506fb6a53c157ce68105f451ad5958ff', 277);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-278::mrego::(Checksum: 3:b02c658ae947d20ac0ee1d207ebd210e)
ALTER TABLE scheduling_states_by_order_version ADD CONSTRAINT fk84f61df19bfe55d0 FOREIGN KEY (scheduling_state_for_version_id) REFERENCES scheduling_data_for_version(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-278', '2.0-rc7', '3:b02c658ae947d20ac0ee1d207ebd210e', 278);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-279::mrego::(Checksum: 3:c346d8903750d250fabc343064880e6f)
ALTER TABLE sigmoid_function ADD CONSTRAINT fkc9c3fe691a5e11f8 FOREIGN KEY (assignment_function_id) REFERENCES assignment_function(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-279', '2.0-rc7', '3:c346d8903750d250fabc343064880e6f', 279);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-280::mrego::(Checksum: 3:37fcf9ad58b891a096b9c95f4ea57b58)
ALTER TABLE specific_day_assignments_container ADD CONSTRAINT fkcee469987518838c FOREIGN KEY (resource_allocation_id) REFERENCES specific_resource_allocation(resource_allocation_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-280', '2.0-rc7', '3:37fcf9ad58b891a096b9c95f4ea57b58', 280);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-281::mrego::(Checksum: 3:611486b58c51ad97deab489a17b23e2e)
ALTER TABLE specific_day_assignments_container ADD CONSTRAINT fkcee46998421c7cf4 FOREIGN KEY (scenario) REFERENCES scenario(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-281', '2.0-rc7', '3:611486b58c51ad97deab489a17b23e2e', 281);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-282::mrego::(Checksum: 3:0efe8669ed086af6da655a53d4ced916)
ALTER TABLE specific_resource_allocation ADD CONSTRAINT fkf0e85724eae850b2 FOREIGN KEY (resource) REFERENCES resource(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-282', '2.0-rc7', '3:0efe8669ed086af6da655a53d4ced916', 282);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-283::mrego::(Checksum: 3:730ece3259ea1ee3ffa53563790465d8)
ALTER TABLE specific_resource_allocation ADD CONSTRAINT fkf0e8572475ed79ba FOREIGN KEY (resource_allocation_id) REFERENCES resource_allocation(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-283', '2.0-rc7', '3:730ece3259ea1ee3ffa53563790465d8', 283);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-284::mrego::(Checksum: 3:5eb111d4b478c0c9a66a3a4a112c1c6b)
ALTER TABLE stretches ADD CONSTRAINT fkee374673ae0677b8 FOREIGN KEY (assignment_function_id) REFERENCES stretches_function(assignment_function_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-284', '2.0-rc7', '3:5eb111d4b478c0c9a66a3a4a112c1c6b', 284);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-285::mrego::(Checksum: 3:b7d27d03ee27cc13c76c5262d518b876)
ALTER TABLE stretches_function ADD CONSTRAINT fk4949f0241a5e11f8 FOREIGN KEY (assignment_function_id) REFERENCES assignment_function(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-285', '2.0-rc7', '3:b7d27d03ee27cc13c76c5262d518b876', 285);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-286::mrego::(Checksum: 3:3663d9ecd66964bffcc07bcfce7bdcdc)
ALTER TABLE subcontracted_task_data ADD CONSTRAINT fk35bc4ed63804cfa FOREIGN KEY (external_company) REFERENCES external_company(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-286', '2.0-rc7', '3:3663d9ecd66964bffcc07bcfce7bdcdc', 286);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-287::mrego::(Checksum: 3:35e0a6356d11ed9dfe53c300b9e2ffee)
ALTER TABLE task ADD CONSTRAINT fk3635855b595a0 FOREIGN KEY (subcontrated_task_data_id) REFERENCES subcontracted_task_data(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-287', '2.0-rc7', '3:35e0a6356d11ed9dfe53c300b9e2ffee', 287);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-288::mrego::(Checksum: 3:4ae0336d6b9d807036d03ad041b67052)
ALTER TABLE task ADD CONSTRAINT fk3635854936bb8c FOREIGN KEY (task_element_id) REFERENCES task_element(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-288', '2.0-rc7', '3:4ae0336d6b9d807036d03ad041b67052', 288);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-289::mrego::(Checksum: 3:2da61d1b760209a704e29bfa4a22455e)
ALTER TABLE task_element ADD CONSTRAINT fkc631a642a44abee3 FOREIGN KEY (base_calendar_id) REFERENCES base_calendar(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-289', '2.0-rc7', '3:2da61d1b760209a704e29bfa4a22455e', 289);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-290::mrego::(Checksum: 3:2604fb5ddeccc31b1dccd4cd0bad4342)
ALTER TABLE task_element ADD CONSTRAINT fkc631a642a5f3c581 FOREIGN KEY (parent) REFERENCES task_group(task_element_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-290', '2.0-rc7', '3:2604fb5ddeccc31b1dccd4cd0bad4342', 290);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-291::mrego::(Checksum: 3:da9d240fdde86539e1580b84687e0189)
ALTER TABLE task_group ADD CONSTRAINT fk4d9497454936bb8c FOREIGN KEY (task_element_id) REFERENCES task_element(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-291', '2.0-rc7', '3:da9d240fdde86539e1580b84687e0189', 291);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-292::mrego::(Checksum: 3:0dc9aa5380e9a48fe9aaf306235988a4)
ALTER TABLE task_milestone ADD CONSTRAINT fkd04de7364936bb8c FOREIGN KEY (task_element_id) REFERENCES task_element(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-292', '2.0-rc7', '3:0dc9aa5380e9a48fe9aaf306235988a4', 292);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-293::mrego::(Checksum: 3:58c6315352f7cefbddeb9b3a38d1e140)
ALTER TABLE task_quality_form ADD CONSTRAINT fk4356e07eefda874f FOREIGN KEY (order_element_id) REFERENCES order_element(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-293', '2.0-rc7', '3:58c6315352f7cefbddeb9b3a38d1e140', 293);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-294::mrego::(Checksum: 3:91398a7f542f90a13d1f857ca3937de1)
ALTER TABLE task_quality_form ADD CONSTRAINT fk4356e07e8b37665c FOREIGN KEY (quality_form_id) REFERENCES quality_form(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-294', '2.0-rc7', '3:91398a7f542f90a13d1f857ca3937de1', 294);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-295::mrego::(Checksum: 3:5395eb857489412499e37c691ea36b8f)
ALTER TABLE task_quality_form_items ADD CONSTRAINT fk101a54bf87fa6b5d FOREIGN KEY (task_quality_form_id) REFERENCES task_quality_form(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-295', '2.0-rc7', '3:5395eb857489412499e37c691ea36b8f', 295);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-296::mrego::(Checksum: 3:494a1128a80caad8d24c5f3574ef6edd)
ALTER TABLE task_source ADD CONSTRAINT fk7950e0153d72bc6f FOREIGN KEY (id) REFERENCES task_element(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-296', '2.0-rc7', '3:494a1128a80caad8d24c5f3574ef6edd', 296);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-297::mrego::(Checksum: 3:fa3b4dd73dd82e3a26c6563c9828fba3)
ALTER TABLE task_source ADD CONSTRAINT fk7950e0159a2c0abd FOREIGN KEY (schedulingdata) REFERENCES scheduling_data_for_version(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-297', '2.0-rc7', '3:fa3b4dd73dd82e3a26c6563c9828fba3', 297);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-298::mrego::(Checksum: 3:e83453938c668f856d1df1537ec25d37)
ALTER TABLE task_source_hours_groups ADD CONSTRAINT fka01fe4eee036cfed FOREIGN KEY (hours_group_id) REFERENCES hours_group(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-298', '2.0-rc7', '3:e83453938c668f856d1df1537ec25d37', 298);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-299::mrego::(Checksum: 3:c5655c084990797f73bf295660881932)
ALTER TABLE task_source_hours_groups ADD CONSTRAINT fka01fe4ee8c80ccb7 FOREIGN KEY (task_source_id) REFERENCES task_source(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-299', '2.0-rc7', '3:c5655c084990797f73bf295660881932', 299);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-300::mrego::(Checksum: 3:4747b6b28b49b810444aa6af7c133fab)
ALTER TABLE user_profiles ADD CONSTRAINT fkc74605deedc4db41 FOREIGN KEY (profile_id) REFERENCES profile_table(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-300', '2.0-rc7', '3:4747b6b28b49b810444aa6af7c133fab', 300);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-301::mrego::(Checksum: 3:f73d714400c8712c0715e2da673e9509)
ALTER TABLE user_profiles ADD CONSTRAINT fkc74605de5567ad13 FOREIGN KEY (user_id) REFERENCES user_table(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-301', '2.0-rc7', '3:f73d714400c8712c0715e2da673e9509', 301);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-302::mrego::(Checksum: 3:4d50b8c05ded66b150571543bcd91a37)
ALTER TABLE user_table ADD CONSTRAINT fk7358465a4cd98327 FOREIGN KEY (lastconnectedscenario) REFERENCES scenario(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-302', '2.0-rc7', '3:4d50b8c05ded66b150571543bcd91a37', 302);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-303::mrego::(Checksum: 3:7a5e18b1ed8e9813ca4920bd0856ecd0)
ALTER TABLE virtual_worker ADD CONSTRAINT fkee053f32f2ed6dc FOREIGN KEY (virtual_worker_id) REFERENCES worker(worker_id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-303', '2.0-rc7', '3:7a5e18b1ed8e9813ca4920bd0856ecd0', 303);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-304::mrego::(Checksum: 3:e52f19afd5f5966cff359311b4c3acac)
ALTER TABLE work_report ADD CONSTRAINT fkfd7aa622efda874f FOREIGN KEY (order_element_id) REFERENCES order_element(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-304', '2.0-rc7', '3:e52f19afd5f5966cff359311b4c3acac', 304);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-305::mrego::(Checksum: 3:8548d763866edc7944ec1b0facb1560f)
ALTER TABLE work_report ADD CONSTRAINT fkfd7aa62278e4e2ea FOREIGN KEY (ork_report_type_id) REFERENCES work_report_type(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-305', '2.0-rc7', '3:8548d763866edc7944ec1b0facb1560f', 305);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-306::mrego::(Checksum: 3:fc14a87fde20c7151888cea5c48c6f39)
ALTER TABLE work_report ADD CONSTRAINT fkfd7aa62248d21790 FOREIGN KEY (resource_id) REFERENCES resource(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-306', '2.0-rc7', '3:fc14a87fde20c7151888cea5c48c6f39', 306);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-307::mrego::(Checksum: 3:23a513bb939d6d2751a1550188c32222)
ALTER TABLE work_report_label_type_assigment ADD CONSTRAINT fkbb262920c1c2746e FOREIGN KEY (label_id) REFERENCES label(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-307', '2.0-rc7', '3:23a513bb939d6d2751a1550188c32222', 307);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-308::mrego::(Checksum: 3:4144c0d934e5fd823f19ef91da210ffa)
ALTER TABLE work_report_label_type_assigment ADD CONSTRAINT fkbb262920707cd777 FOREIGN KEY (label_type_id) REFERENCES label_type(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-308', '2.0-rc7', '3:4144c0d934e5fd823f19ef91da210ffa', 308);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-309::mrego::(Checksum: 3:c0d764328d333e644989448a4cd480bb)
ALTER TABLE work_report_label_type_assigment ADD CONSTRAINT fkbb262920131853a1 FOREIGN KEY (work_report_type_id) REFERENCES work_report_type(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-309', '2.0-rc7', '3:c0d764328d333e644989448a4cd480bb', 309);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-310::mrego::(Checksum: 3:461ea2548e20f99478f3203dbf33bc16)
ALTER TABLE work_report_line ADD CONSTRAINT fke2af47f1efda874f FOREIGN KEY (order_element_id) REFERENCES order_element(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-310', '2.0-rc7', '3:461ea2548e20f99478f3203dbf33bc16', 310);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-311::mrego::(Checksum: 3:3eeefe2f703d57218dde9b05fcdbc5cf)
ALTER TABLE work_report_line ADD CONSTRAINT fke2af47f148d21790 FOREIGN KEY (resource_id) REFERENCES resource(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-311', '2.0-rc7', '3:3eeefe2f703d57218dde9b05fcdbc5cf', 311);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-312::mrego::(Checksum: 3:564cd8aac65a0319c9c3c75a486ace41)
ALTER TABLE work_report_line ADD CONSTRAINT fke2af47f1e7e1020b FOREIGN KEY (type_work_hours_id) REFERENCES type_of_work_hours(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-312', '2.0-rc7', '3:564cd8aac65a0319c9c3c75a486ace41', 312);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-313::mrego::(Checksum: 3:48d80e1c9868d51d687680b42fe47f3d)
ALTER TABLE work_report_line ADD CONSTRAINT fke2af47f1f1a3177c FOREIGN KEY (work_report_id) REFERENCES work_report(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-313', '2.0-rc7', '3:48d80e1c9868d51d687680b42fe47f3d', 313);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-314::mrego::(Checksum: 3:6c6213c38e211d11aa77f0766a18e1fe)
ALTER TABLE worker ADD CONSTRAINT fkd162537e40901220 FOREIGN KEY (worker_id) REFERENCES resource(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-314', '2.0-rc7', '3:6c6213c38e211d11aa77f0766a18e1fe', 314);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-315::mrego::(Checksum: 3:f522ccbad33e6a463b265e241c4ae0db)
ALTER TABLE workreports_labels ADD CONSTRAINT fkf796fbd0c1c2746e FOREIGN KEY (label_id) REFERENCES label(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-315', '2.0-rc7', '3:f522ccbad33e6a463b265e241c4ae0db', 315);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-316::mrego::(Checksum: 3:e5777dd44529b270c7514bc322d72105)
ALTER TABLE workreports_labels ADD CONSTRAINT fkf796fbd0f1a3177c FOREIGN KEY (work_report_id) REFERENCES work_report(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-316', '2.0-rc7', '3:e5777dd44529b270c7514bc322d72105', 316);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-317::mrego::(Checksum: 3:5a1c1fb8b9f6cce7fbaa03b255b5a033)
ALTER TABLE workreportslines_labels ADD CONSTRAINT fkcb8fc1cdc1c2746e FOREIGN KEY (label_id) REFERENCES label(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-317', '2.0-rc7', '3:5a1c1fb8b9f6cce7fbaa03b255b5a033', 317);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-318::mrego::(Checksum: 3:ad070f00871b05e431528e34c3f09cde)
ALTER TABLE workreportslines_labels ADD CONSTRAINT fkcb8fc1cd5078e161 FOREIGN KEY (work_report_line_id) REFERENCES work_report_line(id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-318', '2.0-rc7', '3:ad070f00871b05e431528e34c3f09cde', 318);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-319::mrego::(Checksum: 3:1dde4f2407d314306ae29ad174d98de3)
CREATE TABLE hibernate_unique_key (next_hi INT);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-319', '2.0-rc7', '3:1dde4f2407d314306ae29ad174d98de3', 319);

-- Changeset src/main/resources/db.changelog-database.xml::initial-database-creation-320::mrego::(Checksum: 3:5a5608b194d0ec72e2b8f53675ff8de0)
INSERT INTO hibernate_unique_key (next_hi) VALUES (1);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', '', NOW(), 'Insert Row', 'EXECUTED', 'src/main/resources/db.changelog-database.xml', 'initial-database-creation-320', '2.0-rc7', '3:5a5608b194d0ec72e2b8f53675ff8de0', 320);

-- Changeset src/main/resources/db.changelog-initial.xml::resize-precision-in-planning_data::dpino::(Checksum: 3:3ee94e7e64b7ed2592dfb71db6923a1c)
-- Resize precision for 'progress_by_duration' and 'progress_by_num_hours' fields
ALTER TABLE planning_data ALTER COLUMN progress_by_duration TYPE numeric(19,6);

ALTER TABLE planning_data ALTER COLUMN progress_by_num_hours TYPE numeric(19,6);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('dpino', 'Resize precision for ''progress_by_duration'' and ''progress_by_num_hours'' fields', NOW(), 'Modify data type (x2)', 'EXECUTED', 'src/main/resources/db.changelog-initial.xml', 'resize-precision-in-planning_data', '2.0-rc7', '3:3ee94e7e64b7ed2592dfb71db6923a1c', 321);

-- Changeset src/main/resources/db.changelog-initial.xml::add-company-logo-url-configuration-setting::ltilve::(Checksum: 3:009cd5341d49b5415bf7ec539de24c79)
-- Add new column with configuration setting
ALTER TABLE configuration ADD company_logo_url VARCHAR(255);

UPDATE configuration SET company_logo_url = NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('ltilve', 'Add new column with configuration setting', NOW(), 'Add Column, Update Data', 'EXECUTED', 'src/main/resources/db.changelog-initial.xml', 'add-company-logo-url-configuration-setting', '2.0-rc7', '3:009cd5341d49b5415bf7ec539de24c79', 322);

-- Changeset src/main/resources/db.changelog-initial.xml::add scheduling mode::ogonzalez::(Checksum: 3:8a4ed0c0131906744a85a38278180e13)
-- column for org.navalplanner.business.orders.entities.Order.schedulingMode
ALTER TABLE order_table ADD scheduling_mode INT;

UPDATE order_table SET scheduling_mode = '0';

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('ogonzalez', 'column for org.navalplanner.business.orders.entities.Order.schedulingMode', NOW(), 'Add Column, Update Data', 'EXECUTED', 'src/main/resources/db.changelog-initial.xml', 'add scheduling mode', '2.0-rc7', '3:8a4ed0c0131906744a85a38278180e13', 323);

-- Changeset src/main/resources/db.changelog-initial.xml::rename start_constraint_type in task and task milestone::ogonzalez::(Checksum: 3:0ba5792ffc0bff2a1ce571047b008796)
-- Caused by renaming org.navalplanner.business.planner.entities.TaskPositionConstraint.startConstraintType
ALTER TABLE task RENAME COLUMN start_constraint_type TO constraint_type;

ALTER TABLE task_milestone RENAME COLUMN start_constraint_type TO constraint_type;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('ogonzalez', 'Caused by renaming org.navalplanner.business.planner.entities.TaskPositionConstraint.startConstraintType', NOW(), 'Rename Column (x2)', 'EXECUTED', 'src/main/resources/db.changelog-initial.xml', 'rename start_constraint_type in task and task milestone', '2.0-rc7', '3:0ba5792ffc0bff2a1ce571047b008796', 324);

-- Changeset src/main/resources/db.changelog-initial.xml::add-scenarios-enabled-configuration-setting::ltilve::(Checksum: 3:53d99bb420a0c55c8eaa9389e3fc0ed5)
-- Add new column with scenarios visibility flag
ALTER TABLE configuration ADD scenarios_visible BOOLEAN;

UPDATE configuration SET scenarios_visible = FALSE;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('ltilve', 'Add new column with scenarios visibility flag', NOW(), 'Add Column, Update Data', 'EXECUTED', 'src/main/resources/db.changelog-initial.xml', 'add-scenarios-enabled-configuration-setting', '2.0-rc7', '3:53d99bb420a0c55c8eaa9389e3fc0ed5', 325);


-- *********************************************************************
-- Update Database Script - NavalPlan 1.0.0
-- *********************************************************************
-- Change Log: src/main/resources/db.changelog-initial.xml
-- Ran at: 1/7/11 3:49 PM
-- Against: naval@jdbc:postgresql://localhost/navaldev
-- Liquibase version: 2.0-rc7
-- *********************************************************************

-- Lock Database
-- Changeset src/main/resources/db.changelog-initial.xml::change-types-start-finish-date-criterion-satisfaction::ogonzalez::(Checksum: 3:93527c263e394c3960738fc2e9734c4c)
-- Change types of start and finish date to date for criterion satisfaction table
ALTER TABLE criterion_satisfaction ALTER COLUMN start_date TYPE DATE;

ALTER TABLE criterion_satisfaction ALTER COLUMN finish_date TYPE DATE;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('ogonzalez', 'Change types of start and finish date to date for criterion satisfaction table', NOW(), 'Modify data type (x2)', 'EXECUTED', 'src/main/resources/db.changelog-initial.xml', 'change-types-start-finish-date-criterion-satisfaction', '2.0-rc7', '3:93527c263e394c3960738fc2e9734c4c', 326);

-- Release Database Lock
-- Release Database Lock
