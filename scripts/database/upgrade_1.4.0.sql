-- *********************************************************************
-- Update Database Script - LibrePlan 1.4.0
-- *********************************************************************
-- Change Log: src/main/resources/db.changelog.xml
-- Ran at: 4/29/13 6:01 PM
-- Against: libreplan@jdbc:postgresql://localhost/libreplandev
-- Liquibase version: 2.0.5
-- *********************************************************************

-- Lock Database
-- Changeset src/main/resources/db.changelog-1.3.xml::add-updatable-boolean-column-to-calendar-exception-type::acarro::(Checksum: 3:0429ddb8ec22c38600f3e3b25bafeb20)
-- Add new updatable boolean column to calendar exception type with default value TRUE
ALTER TABLE calendar_exception_type ADD updatable BOOLEAN;

ALTER TABLE calendar_exception_type ALTER COLUMN  updatable SET DEFAULT TRUE;

UPDATE calendar_exception_type SET updatable = 'TRUE' WHERE updatable IS NULL;

ALTER TABLE calendar_exception_type ALTER COLUMN  updatable SET NOT NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('acarro', 'Add new updatable boolean column to calendar exception type with default value TRUE', NOW(), 'Add Column, Add Default Value, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.3.xml', 'add-updatable-boolean-column-to-calendar-exception-type', '2.0.5', '3:0429ddb8ec22c38600f3e3b25bafeb20', 438);

-- Changeset src/main/resources/db.changelog-1.3.xml::create-table-order-sync-info::miciele::(Checksum: 3:45e2401780cf78ac5187cd61a604bb4a)
-- Create new table order_sync_info
CREATE TABLE order_sync_info (id BIGINT NOT NULL, "version" BIGINT NOT NULL, last_sync_date TIMESTAMP WITH TIME ZONE NOT NULL, key VARCHAR(255) NOT NULL, connector_name VARCHAR(255) NOT NULL, order_element_id BIGINT, CONSTRAINT PK_ORDER_SYNC_INFO PRIMARY KEY (id));

ALTER TABLE order_sync_info ADD CONSTRAINT order_sync_info_order_table_fkey FOREIGN KEY (order_element_id) REFERENCES order_table (order_element_id);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('miciele', 'Create new table order_sync_info', NOW(), 'Create Table, Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.3.xml', 'create-table-order-sync-info', '2.0.5', '3:45e2401780cf78ac5187cd61a604bb4a', 439);

-- Changeset src/main/resources/db.changelog-1.3.xml::create-tables-related-to-connector-entity::rego::(Checksum: 3:8d5a3216a2cc1e0113a4b8b080ddfd3b)
-- Create tables related to Connector entity
CREATE TABLE connector (id bigserial NOT NULL, "version" BIGINT NOT NULL, name VARCHAR(255) NOT NULL, CONSTRAINT connector_pkey PRIMARY KEY (id));

CREATE TABLE connector_property (connector_id BIGINT NOT NULL, connector_property_position INT NOT NULL, property_key VARCHAR(255) NOT NULL, property_value VARCHAR(255));

ALTER TABLE connector_property ADD CONSTRAINT connector_property_pkey PRIMARY KEY (connector_id, connector_property_position);

ALTER TABLE connector_property ADD CONSTRAINT connector_property_connector_id_fkey FOREIGN KEY (connector_id) REFERENCES connector (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('rego', 'Create tables related to Connector entity', NOW(), 'Create Table (x2), Add Primary Key, Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.3.xml', 'create-tables-related-to-connector-entity', '2.0.5', '3:8d5a3216a2cc1e0113a4b8b080ddfd3b', 440);

-- Changeset src/main/resources/db.changelog-1.3.xml::create-table-job-scheduler-configuration::miciele::(Checksum: 3:0a1913a9dbb8d4504471417d1fafba5b)
-- Create new table job_scheduler_configuration
CREATE TABLE job_scheduler_configuration (id bigserial NOT NULL, "version" BIGINT NOT NULL, job_group VARCHAR(255) NOT NULL, job_name VARCHAR(255) NOT NULL, cron_expression VARCHAR(255) NOT NULL, job_class_name INT NOT NULL, connector_name VARCHAR(255), schedule BOOLEAN, CONSTRAINT PK_JOB_SCHEDULER_CONFIGURATION PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('miciele', 'Create new table job_scheduler_configuration', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-1.3.xml', 'create-table-job-scheduler-configuration', '2.0.5', '3:0a1913a9dbb8d4504471417d1fafba5b', 441);

-- Changeset src/main/resources/db.changelog-1.3.xml::add-projects_filter_period_since-column-to-user_table::ltilve::(Checksum: 3:655c5015d27f29176924aea0a75c074c)
-- Add column to store project filtering interval 'range since' parameter
ALTER TABLE user_table ADD projects_filter_period_since INT;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('ltilve', 'Add column to store project filtering interval ''range since'' parameter', NOW(), 'Add Column', 'EXECUTED', 'src/main/resources/db.changelog-1.3.xml', 'add-projects_filter_period_since-column-to-user_table', '2.0.5', '3:655c5015d27f29176924aea0a75c074c', 442);

-- Changeset src/main/resources/db.changelog-1.3.xml::add-projects_filter_period_to-column-to-user_table::ltilve::(Checksum: 3:eeed9d2d9655d8ca97db00f088e08a3e)
-- Add column to store project filtering interval 'range to' parameter
ALTER TABLE user_table ADD projects_filter_period_to INT;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('ltilve', 'Add column to store project filtering interval ''range to'' parameter', NOW(), 'Add Column', 'EXECUTED', 'src/main/resources/db.changelog-1.3.xml', 'add-projects_filter_period_to-column-to-user_table', '2.0.5', '3:eeed9d2d9655d8ca97db00f088e08a3e', 443);

-- Changeset src/main/resources/db.changelog-1.3.xml::add-resources_load_filter_period_since-column-to-user_table::ltilve::(Checksum: 3:7bcf0c413c87bcf38b4c61689f5246fa)
-- Add column to store resources load filtering interval 'range since' parameter
ALTER TABLE user_table ADD resources_load_filter_period_since INT;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('ltilve', 'Add column to store resources load filtering interval ''range since'' parameter', NOW(), 'Add Column', 'EXECUTED', 'src/main/resources/db.changelog-1.3.xml', 'add-resources_load_filter_period_since-column-to-user_table', '2.0.5', '3:7bcf0c413c87bcf38b4c61689f5246fa', 444);

-- Changeset src/main/resources/db.changelog-1.3.xml::add-resources_load_filter_period_to-column-to-user_table::ltilve::(Checksum: 3:4c4bbe1465a2536bd808e2efe000805d)
-- Add column to store resources load filtering interval 'range to' parameter
ALTER TABLE user_table ADD resources_load_filter_period_to INT;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('ltilve', 'Add column to store resources load filtering interval ''range to'' parameter', NOW(), 'Add Column', 'EXECUTED', 'src/main/resources/db.changelog-1.3.xml', 'add-resources_load_filter_period_to-column-to-user_table', '2.0.5', '3:4c4bbe1465a2536bd808e2efe000805d', 445);

-- Changeset src/main/resources/db.changelog-1.3.xml::add-projects_filter_label_id-column-to-user_table::ltilve::(Checksum: 3:7f70067dbcb427e6c7715d6fd00ff21e)
-- Add column to store label reference for default project filtering
ALTER TABLE user_table ADD projects_filter_label_id BIGINT;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('ltilve', 'Add column to store label reference for default project filtering', NOW(), 'Add Column', 'EXECUTED', 'src/main/resources/db.changelog-1.3.xml', 'add-projects_filter_label_id-column-to-user_table', '2.0.5', '3:7f70067dbcb427e6c7715d6fd00ff21e', 446);

-- Changeset src/main/resources/db.changelog-1.3.xml::add-resources_load_filter_criterion_id-column-to-user_table::ltilve::(Checksum: 3:2c80e7f49b55cd5644966b3d2b4c3b36)
-- Add column to store criterion reference for default resources load filtering
ALTER TABLE user_table ADD resources_load_filter_criterion_id BIGINT;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('ltilve', 'Add column to store criterion reference for default resources load filtering', NOW(), 'Add Column', 'EXECUTED', 'src/main/resources/db.changelog-1.3.xml', 'add-resources_load_filter_criterion_id-column-to-user_table', '2.0.5', '3:2c80e7f49b55cd5644966b3d2b4c3b36', 447);

-- Changeset src/main/resources/db.changelog-1.3.xml::add-projects_filter_label_id-fk-to-user_table::ltilve::(Checksum: 3:d4eeb6e123ec468bb3168d7b97184f4b)
-- Add Foreign Key constraint on label reference
ALTER TABLE user_table ADD CONSTRAINT user_label_fkey FOREIGN KEY (projects_filter_label_id) REFERENCES label (id) ON DELETE SET NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('ltilve', 'Add Foreign Key constraint on label reference', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.3.xml', 'add-projects_filter_label_id-fk-to-user_table', '2.0.5', '3:d4eeb6e123ec468bb3168d7b97184f4b', 448);

-- Changeset src/main/resources/db.changelog-1.3.xml::add-resources_load_filter_fk_id-column-to-user_table::ltilve::(Checksum: 3:ba6c3438f5e60e881e96f7c6b768b1c9)
-- Add Foreign Key constraint on criterion reference
ALTER TABLE user_table ADD CONSTRAINT user_criterion_fkey FOREIGN KEY (resources_load_filter_criterion_id) REFERENCES criterion (id) ON DELETE SET NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('ltilve', 'Add Foreign Key constraint on criterion reference', NOW(), 'Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.3.xml', 'add-resources_load_filter_fk_id-column-to-user_table', '2.0.5', '3:ba6c3438f5e60e881e96f7c6b768b1c9', 449);

-- Release Database Lock
-- Release Database Lock
