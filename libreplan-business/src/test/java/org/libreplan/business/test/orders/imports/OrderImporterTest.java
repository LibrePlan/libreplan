/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplan.business.test.orders.imports;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.orders.imports.OrderDTO;
import org.libreplan.business.orders.imports.OrderImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Tests for {@link OrderImport}. <br />
 *
 * @author Alba Carro Pérez <alba.carro@gmail.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
public class OrderImporterTest {

    @Autowired
    private OrderImporter orderImporter;

    @Test
    public void testCreatingImportDataFromMPPFile() {

        String filename = System.getProperty("user.dir")
                + "/../scripts/mpxj-import/T1R1FCT2R2.mpp";

        InputStream file;

        try {
            file = new FileInputStream(filename);

            OrderDTO importData = orderImporter.getImportData(file, filename);

            assertEquals(importData.name, "T1R1FCT2R2");

            assertEquals(importData.tasks.size(), 2);

            assertEquals(importData.tasks.get(0).name, "Tarea1");

            assertEquals(importData.tasks.get(1).name, "Tarea2");

            assertEquals(importData.tasks.get(0).children.size(), 0);

            assertEquals(importData.tasks.get(1).children.size(), 0);

        } catch (FileNotFoundException e) {
            assertTrue(false);
        }
    }

    @Test
    public void testCreatingImportDataFromPlannerFile() {

        String filename = System.getProperty("user.dir")
                + "/../scripts/mpxj-import/T1R1FCT2R2.planner";
        InputStream file;

        try {
            file = new FileInputStream(filename);

            OrderDTO importData = orderImporter.getImportData(file, filename);

            assertEquals(importData.tasks.size(), 2);

            assertEquals(importData.tasks.get(0).name, "Tarea1");

            assertEquals(importData.tasks.get(1).name, "Tarea2");

            assertEquals(importData.tasks.get(0).children.size(), 0);

            assertEquals(importData.tasks.get(1).children.size(), 0);

        } catch (FileNotFoundException e) {
            assertTrue(false);
        }
    }

}
