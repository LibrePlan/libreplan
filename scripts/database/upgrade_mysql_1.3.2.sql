--  *********************************************************************
--  Update Database Script - LibrePlan 1.3.2
--  *********************************************************************
--  Change Log: src/main/resources/db.changelog.xml
--  Ran at: 11/28/12 5:20 PM
--  Against: libreplan@localhost@jdbc:mysql://localhost/libreplandev
--  Liquibase version: 2.0.5
--  *********************************************************************

--  Lock Database
--  Changeset src/main/resources/db.changelog-1.3.xml::add-columns-first-and-last-timesheet_date-to-sum_charged_effort::mrego::(Checksum: 3:44643d4a95daa8bfb8eb87350ca09b9c)
--  Add columns first_timesheet_date and last_timesheet_date to
--              sum_charged_effort table
ALTER TABLE `sum_charged_effort` ADD `first_timesheet_date` DATETIME;

ALTER TABLE `sum_charged_effort` ADD `last_timesheet_date` DATETIME;

INSERT INTO `DATABASECHANGELOG` (`AUTHOR`, `COMMENTS`, `DATEEXECUTED`, `DESCRIPTION`, `EXECTYPE`, `FILENAME`, `ID`, `LIQUIBASE`, `MD5SUM`, `ORDEREXECUTED`) VALUES ('mrego', 'Add columns first_timesheet_date and last_timesheet_date to
            sum_charged_effort table', NOW(), 'Add Column (x2)', 'EXECUTED', 'src/main/resources/db.changelog-1.3.xml', 'add-columns-first-and-last-timesheet_date-to-sum_charged_effort', '2.0.5', '3:44643d4a95daa8bfb8eb87350ca09b9c', 430);

--  Changeset src/main/resources/db.changelog-1.3.xml::add-new-column-read_only-to-advance_type::mrego::(Checksum: 3:f188be8e7cb36ffc378ee1bbb1efe4c3)
--  Add new column read_only with default value FALSE to advance_type
--              table.
ALTER TABLE `advance_type` ADD `read_only` TINYINT(1);

ALTER TABLE `advance_type` ALTER `read_only` SET DEFAULT 0;

UPDATE `advance_type` SET `read_only` = '0' WHERE read_only IS NULL;

ALTER TABLE `advance_type` MODIFY `read_only` TINYINT(1) NOT NULL;

INSERT INTO `DATABASECHANGELOG` (`AUTHOR`, `COMMENTS`, `DATEEXECUTED`, `DESCRIPTION`, `EXECTYPE`, `FILENAME`, `ID`, `LIQUIBASE`, `MD5SUM`, `ORDEREXECUTED`) VALUES ('mrego', 'Add new column read_only with default value FALSE to advance_type
            table.', NOW(), 'Add Column, Add Default Value, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.3.xml', 'add-new-column-read_only-to-advance_type', '2.0.5', '3:f188be8e7cb36ffc378ee1bbb1efe4c3', 431);

--  Changeset src/main/resources/db.changelog-1.3.xml::add-new-column-finished-to-work_report_line::mrego::(Checksum: 3:4179c949aced25d73e640b35729f5dc3)
--  Add new column finished with default value FALSE to
--              work_report_line table.
ALTER TABLE `work_report_line` ADD `finished` TINYINT(1);

ALTER TABLE `work_report_line` ALTER `finished` SET DEFAULT 0;

UPDATE `work_report_line` SET `finished` = '0' WHERE finished IS NULL;

ALTER TABLE `work_report_line` MODIFY `finished` TINYINT(1) NOT NULL;

INSERT INTO `DATABASECHANGELOG` (`AUTHOR`, `COMMENTS`, `DATEEXECUTED`, `DESCRIPTION`, `EXECTYPE`, `FILENAME`, `ID`, `LIQUIBASE`, `MD5SUM`, `ORDEREXECUTED`) VALUES ('mrego', 'Add new column finished with default value FALSE to
            work_report_line table.', NOW(), 'Add Column, Add Default Value, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.3.xml', 'add-new-column-finished-to-work_report_line', '2.0.5', '3:4179c949aced25d73e640b35729f5dc3', 432);

--  Changeset src/main/resources/db.changelog-1.3.xml::add-new-column-finished_timesheets-to-sum_charged_effort::mrego::(Checksum: 3:2e0afbe29878247b314daf44d13e7953)
--  Add new column finished_timesheets with default value FALSE to
--              sum_charged_effort table.
ALTER TABLE `sum_charged_effort` ADD `finished_timesheets` TINYINT(1);

ALTER TABLE `sum_charged_effort` ALTER `finished_timesheets` SET DEFAULT 0;

UPDATE `sum_charged_effort` SET `finished_timesheets` = '0' WHERE finished_timesheets IS NULL;

ALTER TABLE `sum_charged_effort` MODIFY `finished_timesheets` TINYINT(1) NOT NULL;

INSERT INTO `DATABASECHANGELOG` (`AUTHOR`, `COMMENTS`, `DATEEXECUTED`, `DESCRIPTION`, `EXECTYPE`, `FILENAME`, `ID`, `LIQUIBASE`, `MD5SUM`, `ORDEREXECUTED`) VALUES ('mrego', 'Add new column finished_timesheets with default value FALSE to
            sum_charged_effort table.', NOW(), 'Add Column, Add Default Value, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.3.xml', 'add-new-column-finished_timesheets-to-sum_charged_effort', '2.0.5', '3:2e0afbe29878247b314daf44d13e7953', 433);

--  Changeset src/main/resources/db.changelog-1.3.xml::add-new-column-updated_from_timesheets-to-task_element::mrego::(Checksum: 3:8e277ed572a72c08c2d637c171f75faa)
--  Add new column updated_from_timesheets with default value FALSE to
--              task_element table.
ALTER TABLE `task_element` ADD `updated_from_timesheets` TINYINT(1);

ALTER TABLE `task_element` ALTER `updated_from_timesheets` SET DEFAULT 0;

UPDATE `task_element` SET `updated_from_timesheets` = '0' WHERE updated_from_timesheets IS NULL;

ALTER TABLE `task_element` MODIFY `updated_from_timesheets` TINYINT(1) NOT NULL;

INSERT INTO `DATABASECHANGELOG` (`AUTHOR`, `COMMENTS`, `DATEEXECUTED`, `DESCRIPTION`, `EXECTYPE`, `FILENAME`, `ID`, `LIQUIBASE`, `MD5SUM`, `ORDEREXECUTED`) VALUES ('mrego', 'Add new column updated_from_timesheets with default value FALSE to
            task_element table.', NOW(), 'Add Column, Add Default Value, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.3.xml', 'add-new-column-updated_from_timesheets-to-task_element', '2.0.5', '3:8e277ed572a72c08c2d637c171f75faa', 434);

--  Changeset src/main/resources/db.changelog-1.3.xml::update-status-values-in-order_table::mrego::(Checksum: 3:3a3f781e8ff867ecb6f049b95d2d36f6)
--  Updating status values in order_table
UPDATE `order_table` SET `state` = '8' WHERE state='6';

UPDATE `order_table` SET `state` = '7' WHERE state='4';

UPDATE `order_table` SET `state` = '6' WHERE state='3';

UPDATE `order_table` SET `state` = '4' WHERE state='2';

UPDATE `order_table` SET `state` = '3' WHERE state='1';

UPDATE `order_table` SET `state` = '2' WHERE state='5';

UPDATE `order_table` SET `state` = '1' WHERE state='0';

INSERT INTO `DATABASECHANGELOG` (`AUTHOR`, `COMMENTS`, `DATEEXECUTED`, `DESCRIPTION`, `EXECTYPE`, `FILENAME`, `ID`, `LIQUIBASE`, `MD5SUM`, `ORDEREXECUTED`) VALUES ('mrego', 'Updating status values in order_table', NOW(), 'Update Data (x7)', 'EXECUTED', 'src/main/resources/db.changelog-1.3.xml', 'update-status-values-in-order_table', '2.0.5', '3:3a3f781e8ff867ecb6f049b95d2d36f6', 435);

--  Changeset src/main/resources/db.changelog-1.3.xml::drop-unique-constraint-code-in-order_element::mrego::(Checksum: 3:a4fafb8fe670f6e03b1fe0aab68eeefe)
--  Drop unique constraint for code in order_element table
ALTER TABLE `order_element` DROP KEY `order_element_code_key`;

INSERT INTO `DATABASECHANGELOG` (`AUTHOR`, `COMMENTS`, `DATEEXECUTED`, `DESCRIPTION`, `EXECTYPE`, `FILENAME`, `ID`, `LIQUIBASE`, `MD5SUM`, `ORDEREXECUTED`) VALUES ('mrego', 'Drop unique constraint for code in order_element table', NOW(), 'Drop Unique Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.3.xml', 'drop-unique-constraint-code-in-order_element', '2.0.5', '3:a4fafb8fe670f6e03b1fe0aab68eeefe', 436);

--  Changeset src/main/resources/db.changelog-1.3.xml::add-seconds_planning_warning-column-to-configuration::mrego::(Checksum: 3:812805ebd72a401ecab1d002ae7eff36)
--  Add seconds_planning_warning column to configuration
ALTER TABLE `configuration` ADD `seconds_planning_warning` INT;

UPDATE `configuration` SET `seconds_planning_warning` = '30';

UPDATE `configuration` SET `seconds_planning_warning` = '30' WHERE seconds_planning_warning IS NULL;

ALTER TABLE `configuration` MODIFY `seconds_planning_warning` INT NOT NULL;

INSERT INTO `DATABASECHANGELOG` (`AUTHOR`, `COMMENTS`, `DATEEXECUTED`, `DESCRIPTION`, `EXECTYPE`, `FILENAME`, `ID`, `LIQUIBASE`, `MD5SUM`, `ORDEREXECUTED`) VALUES ('mrego', 'Add seconds_planning_warning column to configuration', NOW(), 'Add Column, Update Data, Add Not-Null Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.3.xml', 'add-seconds_planning_warning-column-to-configuration', '2.0.5', '3:812805ebd72a401ecab1d002ae7eff36', 437);

--  Release Database Lock
--  Release Database Lock
