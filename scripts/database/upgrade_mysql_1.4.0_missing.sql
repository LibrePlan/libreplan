/* Missing from db.changelog-1.3.xml*/

ALTER TABLE `order_table` ADD `hours_margin` INT;

ALTER TABLE `order_table` ADD `budget_margin` INT;

/* Missing from db.changelog-1.4.xml*/

ALTER TABLE `criterion` ADD `id_cost_category` BIGINT;

ALTER TABLE `criterion` ADD CONSTRAINT `cost_category_fkey` FOREIGN KEY (`id_cost_category`) REFERENCES `cost_category` (`id`) ON UPDATE NO ACTION ON DELETE SET NULL;

ALTER TABLE `configuration` ADD `automatic_budget_enabled` BOOLEAN DEFAULT FALSE;

ALTER TABLE `configuration` ADD `automatic_budget_type_of_work_hours` BIGINT;

ALTER TABLE `configuration` ADD CONSTRAINT `automatic_budget_type_of_work_hours_fkey` FOREIGN KEY (`automatic_budget_type_of_work_hours`) REFERENCES `type_of_work_hours` (`id`) ON UPDATE NO ACTION ON DELETE NO ACTION;
