-- *********************************************************************
-- Update Database Script
-- *********************************************************************
-- Change Log: src/main/resources/db.changelog.xml
-- Ran at: 5/11/11 1:11 PM
-- Against: naval@jdbc:postgresql://localhost/navaldev
-- Liquibase version: 2.0-rc7
-- *********************************************************************

-- Lock Database
-- Changeset src/main/resources/db.changelog-initial.xml::remove-stretches-with-amountWorkPercentage-equal-100::dpino::(Checksum: 3:a0f7f55dd790eefb369fbf139642a868)
-- Removes all stretches which amountWorkPercentage value is 100 as now these stretches will be created automatically and never stored into DB
DELETE FROM stretches WHERE amount_work_percentage = 1.00;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('dpino', 'Removes all stretches which amountWorkPercentage value is 100 as now these stretches will be created automatically and never stored into DB', NOW(), 'Custom SQL', 'EXECUTED', 'src/main/resources/db.changelog-initial.xml', 'remove-stretches-with-amountWorkPercentage-equal-100', '2.0-rc7', '3:a0f7f55dd790eefb369fbf139642a868', 327);

-- Changeset src/main/resources/db.changelog-1.0.xml::use-capacity-instead-of-effort_duration-and-not_over_assignable::ogonzalez::(Checksum: 3:71ae3b8d8998329d68e95d8ad2135745)
-- Convert from duration + notAssignable (not over assignable) to capacity property
ALTER TABLE calendar_exception_type ADD allowed_extra_effort INT DEFAULT null;

ALTER TABLE calendar_exception_type RENAME COLUMN duration TO standard_effort;

UPDATE calendar_exception_type SET allowed_extra_effort = 0 WHERE not_assignable;

ALTER TABLE calendar_exception_type DROP COLUMN not_assignable;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('ogonzalez', 'Convert from duration + notAssignable (not over assignable) to capacity property', NOW(), 'Add Column, Rename Column, Update Data, Drop Column', 'EXECUTED', 'src/main/resources/db.changelog-1.0.xml', 'use-capacity-instead-of-effort_duration-and-not_over_assignable', '2.0-rc7', '3:71ae3b8d8998329d68e95d8ad2135745', 328);

-- Changeset src/main/resources/db.changelog-1.0.xml::use-capacity-for-exceptions::ogonzalez::(Checksum: 3:83576dfaa5871f36aa3f7d4119814f8f)
-- Convert from duration to capacity property for calendar exceptions
ALTER TABLE calendar_exception ADD allowed_extra_effort INT DEFAULT null;

ALTER TABLE calendar_exception RENAME COLUMN duration TO standard_effort;

update calendar_exception SET allowed_extra_effort =
            (select allowed_extra_effort from calendar_exception_type
                where calendar_exception_type.id = calendar_exception.calendar_exception_id);

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('ogonzalez', 'Convert from duration to capacity property for calendar exceptions', NOW(), 'Add Column, Rename Column, Custom SQL', 'EXECUTED', 'src/main/resources/db.changelog-1.0.xml', 'use-capacity-for-exceptions', '2.0-rc7', '3:83576dfaa5871f36aa3f7d4119814f8f', 329);

-- Changeset src/main/resources/db.changelog-1.0.xml::use-capacity-for-capacity-per-day-for-calendar-data::ogonzalez::(Checksum: 3:a643cf37da0098f0cad242d306bb5d05)
-- Convert from duration to capacity in effort per day for CalendarData
ALTER TABLE effort_per_day RENAME TO capacity_per_day;

ALTER TABLE capacity_per_day ADD allowed_extra_effort INT DEFAULT null;

ALTER TABLE capacity_per_day RENAME COLUMN effort TO standard_effort;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('ogonzalez', 'Convert from duration to capacity in effort per day for CalendarData', NOW(), 'Rename Table, Add Column, Rename Column', 'EXECUTED', 'src/main/resources/db.changelog-1.0.xml', 'use-capacity-for-capacity-per-day-for-calendar-data', '2.0-rc7', '3:a643cf37da0098f0cad242d306bb5d05', 330);

-- Changeset src/main/resources/db.changelog-1.0.xml::by_default_weekends_are_not_overassignable::ogonzalez::(Checksum: 3:8ddf0ca2b0fc243475ee2f4c21172565)
-- By default weekends are not over assignable
update capacity_per_day SET allowed_extra_effort = 0
        where day_id IN (5, 6) AND allowed_extra_effort IS NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('ogonzalez', 'By default weekends are not over assignable', NOW(), 'Custom SQL', 'EXECUTED', 'src/main/resources/db.changelog-1.0.xml', 'by_default_weekends_are_not_overassignable', '2.0-rc7', '3:8ddf0ca2b0fc243475ee2f4c21172565', 331);

-- Changeset src/main/resources/db.changelog-1.0.xml::replace-column-limited_resource-with-resource_type::jaragunde::(Checksum: 3:b6640208fbf11943fa46d8953516bad7)
-- Replace column limited_resource with resource_type in resource table
ALTER TABLE resource ADD resource_type VARCHAR(64);

UPDATE resource SET resource_type = 'NON_LIMITING_RESOURCE' WHERE limited_resource = false;

UPDATE resource SET resource_type = 'LIMITING_RESOURCE' WHERE limited_resource = true;

ALTER TABLE resource ALTER COLUMN  resource_type SET NOT NULL;

ALTER TABLE resource DROP COLUMN limited_resource;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('jaragunde', 'Replace column limited_resource with resource_type in resource table', NOW(), 'Add Column, Update Data (x2), Add Not-Null Constraint, Drop Column', 'EXECUTED', 'src/main/resources/db.changelog-1.0.xml', 'replace-column-limited_resource-with-resource_type', '2.0-rc7', '3:b6640208fbf11943fa46d8953516bad7', 332);

-- Changeset src/main/resources/db.changelog-1.0.xml::add-new-column-changed_default_admin_password::smontes::(Checksum: 3:f1a662e7435430892ac7d6cf903c4ce8)
-- Add new column changed_default_admin_password with default value FALSE to configuration table
ALTER TABLE configuration ADD changed_default_admin_password BOOLEAN;

ALTER TABLE configuration ALTER COLUMN  changed_default_admin_password SET DEFAULT 'FALSE';

UPDATE configuration SET changed_default_admin_password = 'FALSE' WHERE changed_default_admin_password IS NULL;

ALTER TABLE configuration ALTER COLUMN  changed_default_admin_password SET NOT NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', 'Add new column changed_default_admin_password with default value FALSE to configuration table', NOW(), 'Add Column, Add Default Value, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.0.xml', 'add-new-column-changed_default_admin_password', '2.0-rc7', '3:f1a662e7435430892ac7d6cf903c4ce8', 333);

-- Changeset src/main/resources/db.changelog-1.0.xml::add-new-column-changed_default_user_password::smontes::(Checksum: 3:cf1274e98ff8796a0f4e3527004c8cd0)
-- Add new column changed_default_user_password with default value FALSE to configuration table
ALTER TABLE configuration ADD changed_default_user_password BOOLEAN;

ALTER TABLE configuration ALTER COLUMN  changed_default_user_password SET DEFAULT 'FALSE';

UPDATE configuration SET changed_default_user_password = 'FALSE' WHERE changed_default_user_password IS NULL;

ALTER TABLE configuration ALTER COLUMN  changed_default_user_password SET NOT NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', 'Add new column changed_default_user_password with default value FALSE to configuration table', NOW(), 'Add Column, Add Default Value, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.0.xml', 'add-new-column-changed_default_user_password', '2.0-rc7', '3:cf1274e98ff8796a0f4e3527004c8cd0', 334);

-- Changeset src/main/resources/db.changelog-1.0.xml::add-new-column-changed_default_wsreader_password::smontes::(Checksum: 3:90bb4041ae144714e7bc703ee73d70c1)
-- Add new column changed_default_wsreader_password with default value FALSE to configuration table
ALTER TABLE configuration ADD changed_default_wsreader_password BOOLEAN;

ALTER TABLE configuration ALTER COLUMN  changed_default_wsreader_password SET DEFAULT 'FALSE';

UPDATE configuration SET changed_default_wsreader_password = 'FALSE' WHERE changed_default_wsreader_password IS NULL;

ALTER TABLE configuration ALTER COLUMN  changed_default_wsreader_password SET NOT NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', 'Add new column changed_default_wsreader_password with default value FALSE to configuration table', NOW(), 'Add Column, Add Default Value, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.0.xml', 'add-new-column-changed_default_wsreader_password', '2.0-rc7', '3:90bb4041ae144714e7bc703ee73d70c1', 335);

-- Changeset src/main/resources/db.changelog-1.0.xml::add-new-column-changed_default_wswriter_password::smontes::(Checksum: 3:124f7fbb425a88220c72c315639a546e)
-- Add new column changed_default_wswriter_password with default value FALSE to configuration table
ALTER TABLE configuration ADD changed_default_wswriter_password BOOLEAN;

ALTER TABLE configuration ALTER COLUMN  changed_default_wswriter_password SET DEFAULT 'FALSE';

UPDATE configuration SET changed_default_wswriter_password = 'FALSE' WHERE changed_default_wswriter_password IS NULL;

ALTER TABLE configuration ALTER COLUMN  changed_default_wswriter_password SET NOT NULL;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('smontes', 'Add new column changed_default_wswriter_password with default value FALSE to configuration table', NOW(), 'Add Column, Add Default Value, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.0.xml', 'add-new-column-changed_default_wswriter_password', '2.0-rc7', '3:124f7fbb425a88220c72c315639a546e', 336);

-- Changeset src/main/resources/db.changelog-1.0.xml::add-intended_resources_per_day-column-to-resource-allocation::ogonzalez::(Checksum: 3:0f818026f55b70d7907ff07b6d6c7f1d)
-- add intended_resources_per_day column to resource-allocation
ALTER TABLE resource_allocation ADD intended_resources_per_day DECIMAL(19,2);

update resource_allocation SET intended_resources_per_day = resources_per_day;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('ogonzalez', 'add intended_resources_per_day column to resource-allocation', NOW(), 'Add Column, Custom SQL', 'EXECUTED', 'src/main/resources/db.changelog-1.0.xml', 'add-intended_resources_per_day-column-to-resource-allocation', '2.0-rc7', '3:0f818026f55b70d7907ff07b6d6c7f1d', 337);

-- Changeset src/main/resources/db.changelog-1.0.xml::change-original-total-assignment-to-use-effort-duration::ogonzalez::(Checksum: 3:e29e39577cdd5015b9be6999d8310544)
-- rename original_total_assignment to intended_total_assignment and now it's interpreted as an EffortDuration
ALTER TABLE resource_allocation ADD intended_total_assignment INT;

ALTER TABLE resource_allocation ALTER COLUMN  intended_total_assignment SET DEFAULT 0;

UPDATE resource_allocation SET intended_total_assignment = 3600 * original_total_assignment WHERE original_total_assignment IS NOT NULL;

ALTER TABLE resource_allocation DROP COLUMN original_total_assignment;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('ogonzalez', 'rename original_total_assignment to intended_total_assignment and now it''s interpreted as an EffortDuration', NOW(), 'Add Column, Add Default Value, Custom SQL, Drop Column', 'EXECUTED', 'src/main/resources/db.changelog-1.0.xml', 'change-original-total-assignment-to-use-effort-duration', '2.0-rc7', '3:e29e39577cdd5015b9be6999d8310544', 338);

-- Changeset src/main/resources/db.changelog-1.0.xml::add-intended_non_consolidated_effort::ogonzalez::(Checksum: 3:e7ac605310143d646b9c8fd0de19c51a)
ALTER TABLE resource_allocation ADD intended_non_consolidated_effort INT;

ALTER TABLE resource_allocation ALTER COLUMN  intended_non_consolidated_effort SET DEFAULT 0;

UPDATE resource_allocation SET intended_non_consolidated_effort = intended_total_assignment;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('ogonzalez', '', NOW(), 'Add Column, Add Default Value, Custom SQL', 'EXECUTED', 'src/main/resources/db.changelog-1.0.xml', 'add-intended_non_consolidated_effort', '2.0-rc7', '3:e7ac605310143d646b9c8fd0de19c51a', 339);

-- Changeset src/main/resources/db.changelog-1.0.xml::add-effort-duration-to-task-and-milestone::mrego::(Checksum: 3:818f79da6b908a1a7c579e60deb53e26)
-- constraintDate attribute in class TaskPositionConstraint has been changed to IntraDayDate.
--             It is needed to add some columns to store EffortDuration in Task and TaskMilestone.
ALTER TABLE task ADD constraint_date_effort_duration INT;

ALTER TABLE task_milestone ADD constraint_date_effort_duration INT;

ALTER TABLE task ALTER COLUMN  constraint_date_effort_duration SET DEFAULT 0;

ALTER TABLE task_milestone ALTER COLUMN  constraint_date_effort_duration SET DEFAULT 0;

INSERT INTO databasechangelog (AUTHOR, COMMENTS, DATEEXECUTED, DESCRIPTION, EXECTYPE, FILENAME, ID, LIQUIBASE, MD5SUM, ORDEREXECUTED) VALUES ('mrego', 'constraintDate attribute in class TaskPositionConstraint has been changed to IntraDayDate.
            It is needed to add some columns to store EffortDuration in Task and TaskMilestone.', NOW(), 'Add Column (x2), Add Default Value (x2)', 'EXECUTED', 'src/main/resources/db.changelog-1.0.xml', 'add-effort-duration-to-task-and-milestone', '2.0-rc7', '3:818f79da6b908a1a7c579e60deb53e26', 340);

-- Release Database Lock
-- Release Database Lock
