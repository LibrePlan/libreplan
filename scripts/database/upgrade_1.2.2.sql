-- *********************************************************************
-- Update Database Script - LibrePlan 1.2.2
-- *********************************************************************
-- Change Log: src/main/resources/db.changelog.xml
-- Ran at: 3/15/12 10:18 AM
-- Against: libreplan@jdbc:postgresql://localhost/libreplandev
-- Liquibase version: 2.0-rc7
-- *********************************************************************

-- Lock Database
-- Changeset src/main/resources/db.changelog-1.2.xml::change-column-description-in-order_element-to-text::mrego::(Checksum: 3:f2241d994f460dca4300c84c9e8f76a0)
-- Change column description in order_element to TEXT
ALTER TABLE order_element ALTER COLUMN description TYPE TEXT;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Change column description in order_element to TEXT', NOW(), 'Modify data type', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'change-column-description-in-order_element-to-text', '2.0-rc7', '3:f2241d994f460dca4300c84c9e8f76a0', 381);

-- Changeset src/main/resources/db.changelog-1.2.xml::change-column-description-in-order_element_template-to-text::mrego::(Checksum: 3:cb7234813755a9eceb39dc4601011375)
-- Change column description in order_element_template to TEXT
ALTER TABLE order_element_template ALTER COLUMN description TYPE TEXT;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Change column description in order_element_template to TEXT', NOW(), 'Modify data type', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'change-column-description-in-order_element_template-to-text', '2.0-rc7', '3:cb7234813755a9eceb39dc4601011375', 382);

-- Release Database Lock
-- Release Database Lock
