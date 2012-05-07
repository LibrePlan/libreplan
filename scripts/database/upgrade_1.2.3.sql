-- *********************************************************************
-- Update Database Script - LibrePlan 1.2.3
-- *********************************************************************
-- Change Log: src/main/resources/db.changelog.xml
-- Ran at: 18/04/12 17:43
-- Against: libreplan@jdbc:postgresql://localhost/libreplandev
-- Liquibase version: 2.0-rc7
-- *********************************************************************

-- Lock Database
-- Changeset src/main/resources/db.changelog-1.2.xml::add-budget-column-to-order_line::mrego::(Checksum: 3:68630e28c83f5f0b24ffd8378526d2a7)
-- add budget column to order_line
ALTER TABLE order_line ADD budget DECIMAL(19,2);

UPDATE order_line SET budget = '0' WHERE budget IS NULL;

ALTER TABLE order_line ALTER COLUMN  budget SET NOT NULL;

ALTER TABLE order_line ALTER COLUMN  budget SET DEFAULT 0;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'add budget column to order_line', NOW(), 'Add Column, Add Not-Null Constraint, Add Default Value', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'add-budget-column-to-order_line', '2.0-rc7', '3:68630e28c83f5f0b24ffd8378526d2a7', 383);

-- Changeset src/main/resources/db.changelog-1.2.xml::add-budget-column-to-order_line_template::mrego::(Checksum: 3:0b8337c5f55c7c2d0159facda7ca80ef)
-- add budget column to order_line_template
ALTER TABLE order_line_template ADD budget DECIMAL(19,2);

UPDATE order_line_template SET budget = '0' WHERE budget IS NULL;

ALTER TABLE order_line_template ALTER COLUMN  budget SET NOT NULL;

ALTER TABLE order_line_template ALTER COLUMN  budget SET DEFAULT 0;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'add budget column to order_line_template', NOW(), 'Add Column, Add Not-Null Constraint, Add Default Value', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'add-budget-column-to-order_line_template', '2.0-rc7', '3:0b8337c5f55c7c2d0159facda7ca80ef', 384);

-- Release Database Lock
-- Release Database Lock
