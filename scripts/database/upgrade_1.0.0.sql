-- *********************************************************************
-- Update Database Script
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
