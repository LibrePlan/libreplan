--  *********************************************************************
--  Update Database Script - LibrePlan 1.3.1
--  *********************************************************************
--  Change Log: src/main/resources/db.changelog.xml
--  Ran at: 10/15/12 1:48 PM
--  Against: libreplan@localhost@jdbc:mysql://localhost/libreplandev
--  Liquibase version: 2.0.5
--  *********************************************************************

--  Lock Database
--  Changeset src/main/resources/db.changelog-1.3.xml::change-column-notes-in-task_element-to-text-in-mysql::jaragunde::(Checksum: 3:004388b31a54d5b8f0f97f21724c3a5b)
--  Change column notes in task_element to TEXT in MySQL.
ALTER TABLE task_element MODIFY notes TEXT;

INSERT INTO `DATABASECHANGELOG` (`AUTHOR`, `COMMENTS`, `DATEEXECUTED`, `DESCRIPTION`, `EXECTYPE`, `FILENAME`, `ID`, `LIQUIBASE`, `MD5SUM`, `ORDEREXECUTED`) VALUES ('jaragunde', 'Change column notes in task_element to TEXT in MySQL.', NOW(), 'Custom SQL', 'EXECUTED', 'src/main/resources/db.changelog-1.3.xml', 'change-column-notes-in-task_element-to-text-in-mysql', '2.0.5', '3:004388b31a54d5b8f0f97f21724c3a5b', 425);

--  Changeset src/main/resources/db.changelog-1.3.xml::update-work_report_type-name-to-personal-timehseets::mrego::(Checksum: 3:b6bca02ba95171a212ead3704760b41d)
--  Update work_report_type name from "Monthly timesheets" to "Personal
--              timehsheets"
UPDATE `work_report_type` SET `name` = 'Personal timesheets' WHERE name='Monthly timesheets';

INSERT INTO `DATABASECHANGELOG` (`AUTHOR`, `COMMENTS`, `DATEEXECUTED`, `DESCRIPTION`, `EXECTYPE`, `FILENAME`, `ID`, `LIQUIBASE`, `MD5SUM`, `ORDEREXECUTED`) VALUES ('mrego', 'Update work_report_type name from "Monthly timesheets" to "Personal
            timehsheets"', NOW(), 'Update Data', 'EXECUTED', 'src/main/resources/db.changelog-1.3.xml', 'update-work_report_type-name-to-personal-timehseets', '2.0.5', '3:b6bca02ba95171a212ead3704760b41d', 426);

--  Changeset src/main/resources/db.changelog-1.3.xml::add-personal_timesheets_periodicity-column-to-configuration::mrego::(Checksum: 3:bcb66fc0ee64a06c7aeee7df6a8c64d1)
--  Add personal_timesheets_periodicity column to configuration
ALTER TABLE `configuration` ADD `personal_timesheets_periodicity` INT;

UPDATE `configuration` SET `personal_timesheets_periodicity` = '0';

INSERT INTO `DATABASECHANGELOG` (`AUTHOR`, `COMMENTS`, `DATEEXECUTED`, `DESCRIPTION`, `EXECTYPE`, `FILENAME`, `ID`, `LIQUIBASE`, `MD5SUM`, `ORDEREXECUTED`) VALUES ('mrego', 'Add personal_timesheets_periodicity column to configuration', NOW(), 'Add Column, Update Data', 'EXECUTED', 'src/main/resources/db.changelog-1.3.xml', 'add-personal_timesheets_periodicity-column-to-configuration', '2.0.5', '3:bcb66fc0ee64a06c7aeee7df6a8c64d1', 427);

--  Changeset src/main/resources/db.changelog-1.3.xml::rename-column-from-monthly-to-personal-in-configuration::mrego::(Checksum: 3:21f778d292e09ef087005888af1f7f15)
--  Rename column monthly_timesheets_type_of_work_hours to
--              personal_timesheets_type_of_work_hours in configuration table
ALTER TABLE `configuration` DROP FOREIGN KEY `configuration_type_of_work_hours_fkey`;

ALTER TABLE `configuration` CHANGE `monthly_timesheets_type_of_work_hours` `personal_timesheets_type_of_work_hours` BIGINT;

ALTER TABLE `configuration` ADD CONSTRAINT `configuration_type_of_work_hours_fkey` FOREIGN KEY (`personal_timesheets_type_of_work_hours`) REFERENCES `type_of_work_hours` (`id`);

INSERT INTO `DATABASECHANGELOG` (`AUTHOR`, `COMMENTS`, `DATEEXECUTED`, `DESCRIPTION`, `EXECTYPE`, `FILENAME`, `ID`, `LIQUIBASE`, `MD5SUM`, `ORDEREXECUTED`) VALUES ('mrego', 'Rename column monthly_timesheets_type_of_work_hours to
            personal_timesheets_type_of_work_hours in configuration table', NOW(), 'Drop Foreign Key Constraint, Rename Column, Add Foreign Key Constraint', 'EXECUTED', 'src/main/resources/db.changelog-1.3.xml', 'rename-column-from-monthly-to-personal-in-configuration', '2.0.5', '3:21f778d292e09ef087005888af1f7f15', 428);

--  Changeset src/main/resources/db.changelog-1.3.xml::add-max_users-and-max_resources-columns-to-configuration::mrego::(Checksum: 3:e098fa833f01deb6447f715e6c3dd5ff)
--  Add max_users and max_resources columns to configuration
ALTER TABLE `configuration` ADD `max_users` INT;

ALTER TABLE `configuration` ADD `max_resources` INT;

UPDATE `configuration` SET `max_users` = '0';

UPDATE `configuration` SET `max_resources` = '0';

INSERT INTO `DATABASECHANGELOG` (`AUTHOR`, `COMMENTS`, `DATEEXECUTED`, `DESCRIPTION`, `EXECTYPE`, `FILENAME`, `ID`, `LIQUIBASE`, `MD5SUM`, `ORDEREXECUTED`) VALUES ('mrego', 'Add max_users and max_resources columns to configuration', NOW(), 'Add Column (x2), Update Data (x2)', 'EXECUTED', 'src/main/resources/db.changelog-1.3.xml', 'add-max_users-and-max_resources-columns-to-configuration', '2.0.5', '3:e098fa833f01deb6447f715e6c3dd5ff', 429);

--  Release Database Lock
--  Release Database Lock
