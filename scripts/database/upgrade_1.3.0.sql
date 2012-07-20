-- *********************************************************************
-- Update Database Script - LibrePlan 1.3.0
-- *********************************************************************
-- Change Log: src/main/resources/db.changelog.xml
-- Ran at: 7/20/12 8:18 AM
-- Against: libreplan@jdbc:postgresql://localhost/libreplandev
-- Liquibase version: 2.0.5
-- *********************************************************************

-- Lock Database
-- Changeset src/main/resources/db.changelog-1.2.xml::initial-database-creation-customer-comunication::smontes::(Checksum: 3:59a56c15bcd845deb7f160c2ed1ff817)
CREATE TABLE customer_comunication (id BIGINT NOT NULL, "version" BIGINT NOT NULL, deadline TIMESTAMP WITH TIME ZONE, comunication_type INT, comunication_date TIMESTAMP WITH TIME ZONE, reviewed BOOLEAN, order_id BIGINT, CONSTRAINT customer_comunication_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'initial-database-creation-customer-comunication', '2.0.5', '3:59a56c15bcd845deb7f160c2ed1ff817', 385);

-- Changeset src/main/resources/db.changelog-1.2.xml::initial-database-creation-subcontractor-comunication::smontes::(Checksum: 3:ecd951d180545b3e53241e7a862215ac)
CREATE TABLE subcontractor_comunication (id BIGINT NOT NULL, "version" BIGINT NOT NULL, comunication_type INT, comunication_date TIMESTAMP WITH TIME ZONE, reviewed BOOLEAN, subcontracted_task_data BIGINT, CONSTRAINT subcontractor_comunication_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'initial-database-creation-subcontractor-comunication', '2.0.5', '3:ecd951d180545b3e53241e7a862215ac', 386);

-- Changeset src/main/resources/db.changelog-1.2.xml::initial-database-creation-subcontractor-comunication-value::smontes::(Checksum: 3:0193a0cb269f0fca18d96d1be7628b2e)
CREATE TABLE subcontrator_comunication_values (subcontractor_comunication_id BIGINT NOT NULL, "date" TIMESTAMP WITH TIME ZONE, progress DECIMAL(19,2), idx INT NOT NULL);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'initial-database-creation-subcontractor-comunication-value', '2.0.5', '3:0193a0cb269f0fca18d96d1be7628b2e', 387);

-- Changeset src/main/resources/db.changelog-1.2.xml::rename-table-customer_comunication-to-customer_communication::smontes::(Checksum: 3:1ae733e6ccc918ffebd42a6f81d64d0b)
-- Rename table customer_comunication to customer_communication
ALTER TABLE customer_comunication RENAME TO customer_communication;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', 'Rename table customer_comunication to customer_communication', NOW(), 'Rename Table', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'rename-table-customer_comunication-to-customer_communication', '2.0.5', '3:1ae733e6ccc918ffebd42a6f81d64d0b', 388);

-- Changeset src/main/resources/db.changelog-1.2.xml::rename-column-comunication_type-to-communication_type::smontes::(Checksum: 3:3a22719f63e2217b3263184f5edfcba7)
-- Rename column comunication_type to communication_type
ALTER TABLE customer_communication RENAME COLUMN comunication_type TO communication_type;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', 'Rename column comunication_type to communication_type', NOW(), 'Rename Column', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'rename-column-comunication_type-to-communication_type', '2.0.5', '3:3a22719f63e2217b3263184f5edfcba7', 389);

-- Changeset src/main/resources/db.changelog-1.2.xml::rename-column-comunication_date-to-communication_date::smontes::(Checksum: 3:2cd6e17fc795e3658f464709fba77874)
-- Rename column comunication_date to communication_date
ALTER TABLE customer_communication RENAME COLUMN comunication_date TO communication_date;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', 'Rename column comunication_date to communication_date', NOW(), 'Rename Column', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'rename-column-comunication_date-to-communication_date', '2.0.5', '3:2cd6e17fc795e3658f464709fba77874', 390);

-- Changeset src/main/resources/db.changelog-1.2.xml::rename-table-subcontractor_comunication::smontes::(Checksum: 3:e9e6a2bcb8dfe3fed721f32999bf51f1)
-- Rename table subcontractor_comunication to subcontractor_communication
ALTER TABLE subcontractor_comunication RENAME TO subcontractor_communication;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', 'Rename table subcontractor_comunication to subcontractor_communication', NOW(), 'Rename Table', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'rename-table-subcontractor_comunication', '2.0.5', '3:e9e6a2bcb8dfe3fed721f32999bf51f1', 391);

-- Changeset src/main/resources/db.changelog-1.2.xml::rename-column-comunication_type-on-subcontractor-communication::smontes::(Checksum: 3:66fa4dd7f1e8cfc5c2a0a569eb1b7e76)
-- Rename column comunication_type to communication_type
ALTER TABLE subcontractor_communication RENAME COLUMN comunication_type TO communication_type;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', 'Rename column comunication_type to communication_type', NOW(), 'Rename Column', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'rename-column-comunication_type-on-subcontractor-communication', '2.0.5', '3:66fa4dd7f1e8cfc5c2a0a569eb1b7e76', 392);

-- Changeset src/main/resources/db.changelog-1.2.xml::rename-column-comunication_date-on-subcontractor-communication::smontes::(Checksum: 3:44d8a7bf4bbd4327cdfce81458b30554)
-- Rename column comunication_date to communication_date
ALTER TABLE subcontractor_communication RENAME COLUMN comunication_date TO communication_date;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', 'Rename column comunication_date to communication_date', NOW(), 'Rename Column', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'rename-column-comunication_date-on-subcontractor-communication', '2.0.5', '3:44d8a7bf4bbd4327cdfce81458b30554', 393);

-- Changeset src/main/resources/db.changelog-1.2.xml::rename-table-subcontractor_comunication_values::smontes::(Checksum: 3:2a590f887ff2eff855dec6ebb6ab03fd)
-- Rename table subcontractor_comunication_values to subcontractor_communication_values
ALTER TABLE subcontrator_comunication_values RENAME TO subcontractor_communication_values;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', 'Rename table subcontractor_comunication_values to subcontractor_communication_values', NOW(), 'Rename Table', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'rename-table-subcontractor_comunication_values', '2.0.5', '3:2a590f887ff2eff855dec6ebb6ab03fd', 394);

-- Changeset src/main/resources/db.changelog-1.2.xml::rename-column-subcontractor_comunication_id::smontes::(Checksum: 3:26f643ba3829536f31f78def5300d9f4)
-- Rename column subcontractor_comunication_id
ALTER TABLE subcontractor_communication_values RENAME COLUMN subcontractor_comunication_id TO subcontractor_communication_id;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', 'Rename column subcontractor_comunication_id', NOW(), 'Rename Column', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'rename-column-subcontractor_comunication_id', '2.0.5', '3:26f643ba3829536f31f78def5300d9f4', 395);

-- Changeset src/main/resources/db.changelog-1.2.xml::creation-deadline-communication::smontes::(Checksum: 3:07ef049e78ecd250d56a88850bdd5250)
CREATE TABLE deadline_communication (id BIGINT NOT NULL, "version" BIGINT NOT NULL, save_date TIMESTAMP WITH TIME ZONE, deliver_date TIMESTAMP WITH TIME ZONE, CONSTRAINT deadline_comunication_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'creation-deadline-communication', '2.0.5', '3:07ef049e78ecd250d56a88850bdd5250', 396);

-- Changeset src/main/resources/db.changelog-1.2.xml::add-delivering-date-column-to-order-entity::smontes::(Checksum: 3:5ea1e3d168533ccd24099c6ac95136c0)
-- Add new delivering date column to order
ALTER TABLE deadline_communication ADD order_id BIGINT;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', 'Add new delivering date column to order', NOW(), 'Add Column', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'add-delivering-date-column-to-order-entity', '2.0.5', '3:5ea1e3d168533ccd24099c6ac95136c0', 397);

-- Changeset src/main/resources/db.changelog-1.2.xml::creation-subcontractor-deliver-date::smontes::(Checksum: 3:8d41b20b896bae55febcc7ab03daff51)
CREATE TABLE subcontractor_deliver_date (id BIGINT NOT NULL, "version" BIGINT NOT NULL, save_date TIMESTAMP WITH TIME ZONE, subcontractor_deliver_date TIMESTAMP WITH TIME ZONE, communication_date TIMESTAMP WITH TIME ZONE, CONSTRAINT subcontrator_deliver_date_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'creation-subcontractor-deliver-date', '2.0.5', '3:8d41b20b896bae55febcc7ab03daff51', 398);

-- Changeset src/main/resources/db.changelog-1.2.xml::add-subcontracted-task-data::smontes::(Checksum: 3:1a4b3b7369aea7ffff6b1cc763ee07dd)
-- Add the column subcontracted_task_id to maintain the relation
ALTER TABLE subcontractor_deliver_date ADD subcontracted_task_data_id BIGINT;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', 'Add the column subcontracted_task_id to maintain the relation', NOW(), 'Add Column', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'add-subcontracted-task-data', '2.0.5', '3:1a4b3b7369aea7ffff6b1cc763ee07dd', 399);

-- Changeset src/main/resources/db.changelog-1.2.xml::database-creation-table-end-date-communication-to-customer::smontes::(Checksum: 3:1a860a9a519552f494435a180819a216)
CREATE TABLE end_date_communication_to_customer (id BIGINT NOT NULL, "version" BIGINT NOT NULL, save_date TIMESTAMP WITH TIME ZONE, end_date TIMESTAMP WITH TIME ZONE, communication_date TIMESTAMP WITH TIME ZONE, order_id BIGINT, CONSTRAINT end_date_communication_to_customer_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'database-creation-table-end-date-communication-to-customer', '2.0.5', '3:1a860a9a519552f494435a180819a216', 400);

-- Changeset src/main/resources/db.changelog-1.2.xml::subcontracted-date-id-column-to-end-date-communication::smontes::(Checksum: 3:6bf0c71648f12e59f928f6c505e596ae)
-- Add subcontracted date id column to end date communication to customer
ALTER TABLE end_date_communication_to_customer ADD subcontracted_task_data_id BIGINT;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', 'Add subcontracted date id column to end date communication to customer', NOW(), 'Add Column', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'subcontracted-date-id-column-to-end-date-communication', '2.0.5', '3:6bf0c71648f12e59f928f6c505e596ae', 401);

-- Changeset src/main/resources/db.changelog-1.2.xml::rename-table-end_date_comunication-to-customer::smontes::(Checksum: 3:7994a86f100e2fb01458dd96c62a4b28)
-- Rename table to end_date_communication
ALTER TABLE end_date_communication_to_customer RENAME TO end_date_communication;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', 'Rename table to end_date_communication', NOW(), 'Rename Table', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'rename-table-end_date_comunication-to-customer', '2.0.5', '3:7994a86f100e2fb01458dd96c62a4b28', 402);

-- Changeset src/main/resources/db.changelog-1.2.xml::change-mapping-order-element-and-sum-charged-effort-postgresql::mrego::(Checksum: 3:2c5d8a9bd39b9a6c647584bda021b433)
-- Change mapping between OrderElement and SumChargedEffort in MySQL
ALTER TABLE sum_charged_effort ADD order_element BIGINT;

CREATE OR REPLACE FUNCTION chageMappingBetweenOrderElementAndSumChargedEffort() RETURNS VOID AS $$
            DECLARE
                sce RECORD;
            BEGIN
                FOR sce IN SELECT sum_charged_effort_id AS id FROM order_element
                LOOP
                    EXECUTE 'UPDATE sum_charged_effort '
                        || 'SET order_element '
                        || ' = (SELECT id FROM order_element '
                            || 'WHERE sum_charged_effort_id '
                            || ' = '
                            || quote_literal(sce.id)
                            || ') '
                        || 'WHERE id '
                        || ' = '
                        || quote_literal(sce.id);
                END LOOP;
            END;
            $$ LANGUAGE plpgsql;

SELECT chageMappingBetweenOrderElementAndSumChargedEffort();

ALTER TABLE sum_charged_effort ADD CONSTRAINT sum_charged_effort_order_element_fkey FOREIGN KEY (order_element) REFERENCES order_element (id);

ALTER TABLE order_element DROP COLUMN sum_charged_effort_id;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Change mapping between OrderElement and SumChargedEffort in MySQL', NOW(), 'Add Column, Create Procedure, Custom SQL, Add Foreign Key Constraint, Drop Column', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'change-mapping-order-element-and-sum-charged-effort-postgresql', '2.0.5', '3:2c5d8a9bd39b9a6c647584bda021b433', 403);

-- Changeset src/main/resources/db.changelog-1.2.xml::creation-table-expense-sheet::smontes::(Checksum: 3:58560df3b8bda44b8057b7538e66c15d)
CREATE TABLE expense_sheet (id BIGINT NOT NULL, "version" BIGINT NOT NULL, code VARCHAR(255) NOT NULL, code_autogenerated BOOLEAN, first_expense TIMESTAMP WITH TIME ZONE, last_expense TIMESTAMP WITH TIME ZONE, total DECIMAL(19,2), description TEXT, last_expense_sheet_line_sequence_code INT, CONSTRAINT expense_sheet_pkey PRIMARY KEY (id));

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', '', NOW(), 'Create Table', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'creation-table-expense-sheet', '2.0.5', '3:58560df3b8bda44b8057b7538e66c15d', 404);

-- Changeset src/main/resources/db.changelog-1.2.xml::creation-table-expense-sheet-line::smontes::(Checksum: 3:c07a2ad8a9e8c277e13bd92806e1414e)
CREATE TABLE expense_sheet_line (id BIGINT NOT NULL, "version" BIGINT NOT NULL, code VARCHAR(255) NOT NULL, value DECIMAL(19,2), concept VARCHAR(255), "date" TIMESTAMP WITH TIME ZONE, order_element_id BIGINT, resource_id BIGINT, expense_sheet_id BIGINT, CONSTRAINT expense_sheet_line_pkey PRIMARY KEY (id));

ALTER TABLE expense_sheet_line ADD CONSTRAINT expense_sheet_line_order_element_fkey FOREIGN KEY (order_element_id) REFERENCES order_element (id);

ALTER TABLE expense_sheet_line ADD CONSTRAINT expense_sheet_line_resource_fkey FOREIGN KEY (resource_id) REFERENCES resource (id);

ALTER TABLE expense_sheet_line ADD CONSTRAINT expense_sheet_line_expense_sheet_fkey FOREIGN KEY (expense_sheet_id) REFERENCES expense_sheet (id);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', '', NOW(), 'Create Table, Add Foreign Key Constraint (x3)', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'creation-table-expense-sheet-line', '2.0.5', '3:c07a2ad8a9e8c277e13bd92806e1414e', 405);

-- Changeset src/main/resources/db.changelog-1.2.xml::add-new-column-generate-code-for-expense-sheets::smontes::(Checksum: 3:cad60515a7450740e2582ae1ce815885)
-- Add new column to generate the code for expense sheet in configuration table
ALTER TABLE configuration ADD generate_code_for_expense_sheets BOOLEAN;

ALTER TABLE configuration ALTER COLUMN  generate_code_for_expense_sheets SET DEFAULT TRUE;

UPDATE configuration SET generate_code_for_expense_sheets = 'TRUE' WHERE generate_code_for_expense_sheets IS NULL;

ALTER TABLE configuration ALTER COLUMN  generate_code_for_expense_sheets SET NOT NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', 'Add new column to generate the code for expense sheet in configuration table', NOW(), 'Add Column, Add Default Value, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'add-new-column-generate-code-for-expense-sheets', '2.0.5', '3:cad60515a7450740e2582ae1ce815885', 406);

-- Changeset src/main/resources/db.changelog-1.2.xml::modify-columns-type-in-expense-sheet-to-date::smontes::(Checksum: 3:30af5261fc7d1cc196b254962a6bc9cd)
-- modify columns type in expense sheet to date
ALTER TABLE expense_sheet ALTER COLUMN first_expense TYPE DATE;

ALTER TABLE expense_sheet ALTER COLUMN last_expense TYPE DATE;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', 'modify columns type in expense sheet to date', NOW(), 'Modify data type (x2)', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'modify-columns-type-in-expense-sheet-to-date', '2.0.5', '3:30af5261fc7d1cc196b254962a6bc9cd', 407);

-- Changeset src/main/resources/db.changelog-1.2.xml::modify-columns-type-in-expense-sheet-line-to-date::smontes::(Checksum: 3:80e8ab1008f78dd80d4d0427b8cab872)
-- modify columns type in expense sheet line to date
ALTER TABLE expense_sheet_line ALTER COLUMN "date" TYPE DATE;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', 'modify columns type in expense sheet line to date', NOW(), 'Modify data type', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'modify-columns-type-in-expense-sheet-line-to-date', '2.0.5', '3:80e8ab1008f78dd80d4d0427b8cab872', 408);

-- Changeset src/main/resources/db.changelog-1.2.xml::create3-table-sum-expenses::smontes::(Checksum: 3:5d50ee6bcc8395686dc88683de0c94f7)
-- creation table sum_expenses
CREATE TABLE sum_expenses (id BIGINT NOT NULL, "version" BIGINT NOT NULL, order_element_id BIGINT, total_direct_expenses DECIMAL(19,2), total_indirect_expenses DECIMAL(19,2), CONSTRAINT sum_expenses_pkey PRIMARY KEY (id));

ALTER TABLE sum_expenses ADD CONSTRAINT sum_expenses_order_element_fkey FOREIGN KEY (order_element_id) REFERENCES order_element (id);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', 'creation table sum_expenses', NOW(), 'Create Table, Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'create3-table-sum-expenses', '2.0.5', '3:5d50ee6bcc8395686dc88683de0c94f7', 409);

-- Changeset src/main/resources/db.changelog-1.2.xml::add-new-columns-for-currency-in-configuration::mrego::(Checksum: 3:c0e2f3cc0bd28a4cfb77c91e32b8f72e)
-- Add new columns for currency in configuration table
ALTER TABLE configuration ADD currency_code VARCHAR(255);

ALTER TABLE configuration ALTER COLUMN  currency_code SET DEFAULT 'EUR';

UPDATE configuration SET currency_code = 'EUR' WHERE currency_code IS NULL;

ALTER TABLE configuration ALTER COLUMN  currency_code SET NOT NULL;

ALTER TABLE configuration ADD currency_symbol VARCHAR(255);

ALTER TABLE configuration ALTER COLUMN  currency_symbol SET DEFAULT '€';

UPDATE configuration SET currency_symbol = '€' WHERE currency_symbol IS NULL;

ALTER TABLE configuration ALTER COLUMN  currency_symbol SET NOT NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Add new columns for currency in configuration table', NOW(), 'Add Column, Add Default Value, Add Not-Null Constraint, Add Column, Add Default Value, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'add-new-columns-for-currency-in-configuration', '2.0.5', '3:c0e2f3cc0bd28a4cfb77c91e32b8f72e', 410);

-- Changeset src/main/resources/db.changelog-1.2.xml::remove-code-from-order_element_template::jaragunde::(Checksum: 3:88f6c4f5d7bece8e045f1cae70e5cfae)
-- Remove column code in order_element_template table
ALTER TABLE order_element_template DROP COLUMN code;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('jaragunde', 'Remove column code in order_element_template table', NOW(), 'Drop Column', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'remove-code-from-order_element_template', '2.0.5', '3:88f6c4f5d7bece8e045f1cae70e5cfae', 411);

-- Changeset src/main/resources/db.changelog-1.2.xml::add-relationship-between-worker-and-user::mrego::(Checksum: 3:c1327b421dab02ba46646e25ce3033f0)
-- Add column and constraints needed for relationship between worker and user
ALTER TABLE worker ADD user_id BIGINT;

ALTER TABLE worker ADD CONSTRAINT worker_user_id_key UNIQUE (user_id);

ALTER TABLE worker ADD CONSTRAINT worker_user_fkey FOREIGN KEY (user_id) REFERENCES user_table (id);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Add column and constraints needed for relationship between worker and user', NOW(), 'Add Column, Add Unique Constraint, Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'add-relationship-between-worker-and-user', '2.0.5', '3:c1327b421dab02ba46646e25ce3033f0', 412);

-- Changeset src/main/resources/db.changelog-1.2.xml::add-monthly_timesheets_type_of_work_hours-to-configuration::mrego::(Checksum: 3:1fba4f4fa3f9838dcecc9f4c3f03adde)
-- Add new column monthly_timesheets_type_of_work_hours to
--             configuration table.
ALTER TABLE configuration ADD monthly_timesheets_type_of_work_hours BIGINT;

ALTER TABLE configuration ADD CONSTRAINT configuration_type_of_work_hours_fkey FOREIGN KEY (monthly_timesheets_type_of_work_hours) REFERENCES type_of_work_hours (id);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Add new column monthly_timesheets_type_of_work_hours to
            configuration table.', NOW(), 'Add Column, Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'add-monthly_timesheets_type_of_work_hours-to-configuration', '2.0.5', '3:1fba4f4fa3f9838dcecc9f4c3f03adde', 413);

-- Changeset src/main/resources/db.changelog-1.2.xml::add-new-column-personal-to-expense_sheet-table::mrego::(Checksum: 3:10bc5bda8b237a316785be725d7be1e3)
-- Add new column personal with default value FALSE to expense_sheet
--             table
ALTER TABLE expense_sheet ADD personal BOOLEAN;

ALTER TABLE expense_sheet ALTER COLUMN  personal SET DEFAULT FALSE;

UPDATE expense_sheet SET personal = 'FALSE' WHERE personal IS NULL;

ALTER TABLE expense_sheet ALTER COLUMN  personal SET NOT NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Add new column personal with default value FALSE to expense_sheet
            table', NOW(), 'Add Column, Add Default Value, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'add-new-column-personal-to-expense_sheet-table', '2.0.5', '3:10bc5bda8b237a316785be725d7be1e3', 414);

-- Changeset src/main/resources/db.changelog-1.2.xml::add-new-column-changed_default_wssubcontracting_password::mrego::(Checksum: 3:013b7f1448a1110847cf286f1413f035)
-- Add new column changed_default_wssubcontracting_password with
--             default value FALSE to configuration table
ALTER TABLE configuration ADD changed_default_wssubcontracting_password BOOLEAN;

ALTER TABLE configuration ALTER COLUMN  changed_default_wssubcontracting_password SET DEFAULT FALSE;

UPDATE configuration SET changed_default_wssubcontracting_password = 'FALSE' WHERE changed_default_wssubcontracting_password IS NULL;

ALTER TABLE configuration ALTER COLUMN  changed_default_wssubcontracting_password SET NOT NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Add new column changed_default_wssubcontracting_password with
            default value FALSE to configuration table', NOW(), 'Add Column, Add Default Value, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'add-new-column-changed_default_wssubcontracting_password', '2.0.5', '3:013b7f1448a1110847cf286f1413f035', 415);

-- Changeset src/main/resources/db.changelog-1.2.xml::rename-column-elt-in-roles_table-to-role::mrego::(Checksum: 3:776a769c3f1a794f6bc4435676322d25)
-- Rename column elt in roles_table to role
ALTER TABLE roles_table RENAME COLUMN elt TO role;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Rename column elt in roles_table to role', NOW(), 'Rename Column', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'rename-column-elt-in-roles_table-to-role', '2.0.5', '3:776a769c3f1a794f6bc4435676322d25', 416);

-- Changeset src/main/resources/db.changelog-1.2.xml::rename-roles-in-roles_table::mrego::(Checksum: 3:096ffd842b1b2b6ada758528f1315a4f)
-- Rename roles in roles_table
UPDATE roles_table SET role = 'ROLE_SUPERUSER' WHERE role='ROLE_ADMINISTRATION';

UPDATE roles_table SET role = 'ROLE_READ_ALL_PROJECTS' WHERE role='ROLE_READ_ALL_ORDERS';

UPDATE roles_table SET role = 'ROLE_EDIT_ALL_PROJECTS' WHERE role='ROLE_EDIT_ALL_ORDERS';

UPDATE roles_table SET role = 'ROLE_CREATE_PROJECTS' WHERE role='ROLE_CREATE_ORDER';

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Rename roles in roles_table', NOW(), 'Update Data (x4)', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'rename-roles-in-roles_table', '2.0.5', '3:096ffd842b1b2b6ada758528f1315a4f', 417);

-- Changeset src/main/resources/db.changelog-1.2.xml::rename-column-elt-in-profile_roles-to-role::mrego::(Checksum: 3:8696c001929a49d0958e563ec6070ee8)
-- Rename column elt in profile_roles to role
ALTER TABLE profile_roles RENAME COLUMN elt TO role;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Rename column elt in profile_roles to role', NOW(), 'Rename Column', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'rename-column-elt-in-profile_roles-to-role', '2.0.5', '3:8696c001929a49d0958e563ec6070ee8', 418);

-- Changeset src/main/resources/db.changelog-1.2.xml::rename-roles-in-profile_roles::mrego::(Checksum: 3:9c1e100911e9b284330a5aa3589984e5)
-- Rename roles in profile_roles
UPDATE profile_roles SET role = 'ROLE_SUPERUSER' WHERE role='ROLE_ADMINISTRATION';

UPDATE profile_roles SET role = 'ROLE_READ_ALL_PROJECTS' WHERE role='ROLE_READ_ALL_ORDERS';

UPDATE profile_roles SET role = 'ROLE_EDIT_ALL_PROJECTS' WHERE role='ROLE_EDIT_ALL_ORDERS';

UPDATE profile_roles SET role = 'ROLE_CREATE_PROJECTS' WHERE role='ROLE_CREATE_ORDER';

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Rename roles in profile_roles', NOW(), 'Update Data (x4)', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'rename-roles-in-profile_roles', '2.0.5', '3:9c1e100911e9b284330a5aa3589984e5', 419);

-- Changeset src/main/resources/db.changelog-1.2.xml::drop-column-changed_default_user_password-in-configuration::mrego::(Checksum: 3:2468aba8b41e0534a2b163e860dea252)
-- Drop column code in configuration table
ALTER TABLE configuration DROP COLUMN changed_default_user_password;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Drop column code in configuration table', NOW(), 'Drop Column', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'drop-column-changed_default_user_password-in-configuration', '2.0.5', '3:2468aba8b41e0534a2b163e860dea252', 420);

-- Changeset src/main/resources/db.changelog-1.2.xml::add-new-column-changed_default_manager_password::mrego::(Checksum: 3:6f3d2474a8785e6a221a0e2b3f098a73)
-- Add new column changed_default_manager_password with
--             default value FALSE to configuration table
ALTER TABLE configuration ADD changed_default_manager_password BOOLEAN;

ALTER TABLE configuration ALTER COLUMN  changed_default_manager_password SET DEFAULT FALSE;

UPDATE configuration SET changed_default_manager_password = 'FALSE' WHERE changed_default_manager_password IS NULL;

ALTER TABLE configuration ALTER COLUMN  changed_default_manager_password SET NOT NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Add new column changed_default_manager_password with
            default value FALSE to configuration table', NOW(), 'Add Column, Add Default Value, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'add-new-column-changed_default_manager_password', '2.0.5', '3:6f3d2474a8785e6a221a0e2b3f098a73', 421);

-- Changeset src/main/resources/db.changelog-1.2.xml::add-new-column-changed_default_hresources_password::mrego::(Checksum: 3:0d70382a0e68da8f6c7835212988162b)
-- Add new column changed_default_hresources_password with
--             default value FALSE to configuration table
ALTER TABLE configuration ADD changed_default_hresources_password BOOLEAN;

ALTER TABLE configuration ALTER COLUMN  changed_default_hresources_password SET DEFAULT FALSE;

UPDATE configuration SET changed_default_hresources_password = 'FALSE' WHERE changed_default_hresources_password IS NULL;

ALTER TABLE configuration ALTER COLUMN  changed_default_hresources_password SET NOT NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Add new column changed_default_hresources_password with
            default value FALSE to configuration table', NOW(), 'Add Column, Add Default Value, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'add-new-column-changed_default_hresources_password', '2.0.5', '3:0d70382a0e68da8f6c7835212988162b', 422);

-- Changeset src/main/resources/db.changelog-1.2.xml::add-new-column-changed_default_outsourcing_password::mrego::(Checksum: 3:f72b604280d98a102dc357ad9ef71b26)
-- Add new column changed_default_outsourcing_password with
--             default value FALSE to configuration table
ALTER TABLE configuration ADD changed_default_outsourcing_password BOOLEAN;

ALTER TABLE configuration ALTER COLUMN  changed_default_outsourcing_password SET DEFAULT FALSE;

UPDATE configuration SET changed_default_outsourcing_password = 'FALSE' WHERE changed_default_outsourcing_password IS NULL;

ALTER TABLE configuration ALTER COLUMN  changed_default_outsourcing_password SET NOT NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Add new column changed_default_outsourcing_password with
            default value FALSE to configuration table', NOW(), 'Add Column, Add Default Value, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'add-new-column-changed_default_outsourcing_password', '2.0.5', '3:f72b604280d98a102dc357ad9ef71b26', 423);

-- Changeset src/main/resources/db.changelog-1.2.xml::add-new-column-changed_default_reports_password::mrego::(Checksum: 3:f4fdaac87e8d1d2246ac482544268dc8)
-- Add new column changed_default_reports_password with
--             default value FALSE to configuration table
ALTER TABLE configuration ADD changed_default_reports_password BOOLEAN;

ALTER TABLE configuration ALTER COLUMN  changed_default_reports_password SET DEFAULT FALSE;

UPDATE configuration SET changed_default_reports_password = 'FALSE' WHERE changed_default_reports_password IS NULL;

ALTER TABLE configuration ALTER COLUMN  changed_default_reports_password SET NOT NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Add new column changed_default_reports_password with
            default value FALSE to configuration table', NOW(), 'Add Column, Add Default Value, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'add-new-column-changed_default_reports_password', '2.0.5', '3:f4fdaac87e8d1d2246ac482544268dc8', 424);

-- Release Database Lock
-- Release Database Lock
