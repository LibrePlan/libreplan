-- *********************************************************************
-- Update Database Script
-- *********************************************************************
-- Change Log: src/main/resources/db.changelog.xml
-- Ran at: 1/19/12 11:41 AM
-- Against: libreplan@jdbc:postgresql://localhost/libreplandev
-- Liquibase version: 2.0-rc7
-- *********************************************************************

-- Lock Database
-- Changeset src/main/resources/db.changelog-1.2.xml::add-task_end_date_effort_duration-to-consolidated_value::mrego::(Checksum: 3:4ffcc289b81bfbfa1d43466ad34d9705)
-- taskEndDate attribute in class ConsolidatedValue has been changed to IntraDayDate.
--             It is needed to add some columns to store EffortDuration in ConsolidatedValue.
ALTER TABLE consolidated_value ADD task_end_date_effort_duration INT;

ALTER TABLE consolidated_value ALTER COLUMN  task_end_date_effort_duration SET DEFAULT 0;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'taskEndDate attribute in class ConsolidatedValue has been changed to IntraDayDate.
            It is needed to add some columns to store EffortDuration in ConsolidatedValue.', NOW(), 'Add Column, Add Default Value', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'add-task_end_date_effort_duration-to-consolidated_value', '2.0-rc7', '3:4ffcc289b81bfbfa1d43466ad34d9705', 376);

-- Changeset src/main/resources/db.changelog-1.2.xml::change-sum_of_hours_allocated-to-sum_of_assigned_effort::jaragunde::(Checksum: 3:075a1a017c4cac0b0480a84f3a539655)
-- Changing sum_of_hours_allocated to sum_of_assigned_effort
ALTER TABLE task_element RENAME COLUMN sum_of_hours_allocated TO sum_of_assigned_effort;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('jaragunde', 'Changing sum_of_hours_allocated to sum_of_assigned_effort', NOW(), 'Rename Column', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'change-sum_of_hours_allocated-to-sum_of_assigned_effort', '2.0-rc7', '3:075a1a017c4cac0b0480a84f3a539655', 377);

-- Changeset src/main/resources/db.changelog-1.2.xml::update-effort-values-in-sum_charged_effort::jaragunde::(Checksum: 3:f408e919dea5d8b75042efb84879380d)
-- Updating effort values (hours to seconds) in task_element table
UPDATE task_element
            SET sum_of_assigned_effort = sum_of_assigned_effort*3600;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('jaragunde', 'Updating effort values (hours to seconds) in task_element table', NOW(), 'Custom SQL', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'update-effort-values-in-sum_charged_effort', '2.0-rc7', '3:f408e919dea5d8b75042efb84879380d', 378);

-- Changeset src/main/resources/db.changelog-1.2.xml::add-new-column-check_new_version_enabled::mrego::(Checksum: 3:8e46dd07efca106657d62ade635bbf03)
-- Add new column check_new_version_enabled with default value TRUE to configuration table
ALTER TABLE configuration ADD check_new_version_enabled BOOLEAN;

ALTER TABLE configuration ALTER COLUMN  check_new_version_enabled SET DEFAULT 'TRUE';

UPDATE configuration SET check_new_version_enabled = 'TRUE' WHERE check_new_version_enabled IS NULL;

ALTER TABLE configuration ALTER COLUMN  check_new_version_enabled SET NOT NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Add new column check_new_version_enabled with default value TRUE to configuration table', NOW(), 'Add Column, Add Default Value, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'add-new-column-check_new_version_enabled', '2.0-rc7', '3:8e46dd07efca106657d62ade635bbf03', 379);

-- Changeset src/main/resources/db.changelog-1.2.xml::add-new-column-allow_to_gather_usage_stats_enabled::mrego::(Checksum: 3:93c54c45c29d479faa98e6db1af06591)
-- Add new column allow_to_gather_usage_stats_enabled with default value FALSE to configuration table
ALTER TABLE configuration ADD allow_to_gather_usage_stats_enabled BOOLEAN;

ALTER TABLE configuration ALTER COLUMN  allow_to_gather_usage_stats_enabled SET DEFAULT 'FALSE';

UPDATE configuration SET allow_to_gather_usage_stats_enabled = 'FALSE' WHERE allow_to_gather_usage_stats_enabled IS NULL;

ALTER TABLE configuration ALTER COLUMN  allow_to_gather_usage_stats_enabled SET NOT NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'Add new column allow_to_gather_usage_stats_enabled with default value FALSE to configuration table', NOW(), 'Add Column, Add Default Value, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.2.xml', 'add-new-column-allow_to_gather_usage_stats_enabled', '2.0-rc7', '3:93c54c45c29d479faa98e6db1af06591', 380);

-- Release Database Lock
-- Release Database Lock
