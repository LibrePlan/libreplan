/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2016 LibrePlan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.libreplan.web.orders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.orders.daos.IHoursGroupDAO;
import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderFile;
import org.libreplan.business.orders.entities.OrderLine;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.business.users.entities.User;
import org.libreplan.web.orders.files.IOrderFileModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


/**
 * Tests for {@link OrderFile}.
 *
 * @author Vova Perebykivskyi <vova@libreplan-enterprise.com>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        BUSINESS_SPRING_CONFIG_FILE,

        WEBAPP_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_TEST_FILE,

        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })

/**
 * CRUD test
 * 1. Add row to files table
 * 2. Read it
 * 3. Update it
 * 4. Delete it
 *
 * Negative test
 * 1. Create row with null field value and try to save it
 */
public class OrderFilesTest {

    @Autowired
    private IOrderFileModel orderFileModel;

    @Autowired
    private IOrderElementModel orderElementModel;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private IHoursGroupDAO hoursGroupDAO;

    @Test
    @Transactional
    public void testCreate() {

        int sizeBefore = orderFileModel.getAll().size();

        createEntities();

        int sizeWithNewRow = orderFileModel.getAll().size();
        assertEquals(sizeBefore + 1, sizeWithNewRow);


        removeEntities();
    }

    @Test
    @Transactional
    public void testRead() {
        createEntities();

        OrderFile orderFile = null;
        try {
            orderFile = orderFileModel.findByParent(orderElementDAO.findUniqueByCode("1a1k1k1k")).get(0);
            assertEquals(orderFile.getName(), "Index");
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        }

        removeEntities();
    }

    @Test
    @Transactional
    public void testUpdate() {
        createEntities();

        OrderFile orderFile = null;
        try {
            orderFile = orderFileModel.findByParent(orderElementDAO.findUniqueByCode("1a1k1k1k")).get(0);
            orderFile.setName("yii2");
            orderFileModel.confirmSave();
            assertTrue(orderFile.getName().equals("yii2"));
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        }

        removeEntities();
    }

    @Test
    @Transactional
    public void testDelete() {
        createEntities();

        int sizeBefore = orderFileModel.getAll().size();

        removeEntities();

        int sizeAfter = orderFileModel.getAll().size();
        assertEquals(sizeBefore - 1, sizeAfter);
    }

    @Transactional
    @Test(expected = DataIntegrityViolationException.class)
    public void testCreateNotValidRow() {
        createUser();
        createOrderElement();

        // Make OrderFile with null value
        createOrderFile(true);
    }

    private void createEntities() {
        createUser();
        createOrderElement();
        createOrderFile(false);
    }

    private void removeEntities() {
        removeOrderFile();
        removeUser();
        removeOrderElement();
    }

    private void createUser() {
        User user = User.create("harry-potter", "somePassword", "harry-potter@hogwarts.uk");
        userDAO.save(user);
    }

    private void createOrderElement() {
        OrderLine orderLine = OrderLine.create();
        orderLine.setName("OrderLineB");
        orderLine.setCode("1a1k1k1k");

        HoursGroup hoursGroup = HoursGroup.create(orderLine);
        hoursGroup.setWorkingHours(0);
        hoursGroup.setCode("hoursGroupName1");
        orderLine.addHoursGroup(hoursGroup);

        // Save is inside method
        orderElementModel.setCurrent(orderLine, new OrderModel());
    }


    private void createOrderFile(boolean nameIsNull) {
        orderFileModel.createNewFileObject();

        if ( !nameIsNull )
            orderFileModel.setFileName("Index");
        else
            orderFileModel.setFileName(null);

        orderFileModel.setFileType("html");
        orderFileModel.setUploadDate(new Date());

        User user = null;
        OrderElement orderElement = null;
        try {
            user = userDAO.findByLoginName("harry-potter");
            orderElement = orderElementDAO.findUniqueByCode("1a1k1k1k");
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        }

        orderFileModel.setUploader(user);
        orderFileModel.setParent(orderElement);

        orderFileModel.confirmSave();
    }

    private void removeOrderFile() {
        OrderFile orderFileToDelete = orderFileModel.getAll().get(0);
        orderFileModel.delete(orderFileToDelete);
    }

    private void removeUser() {
        User user;
        try {
            user = userDAO.findByLoginName("harry-potter");
            userDAO.remove(user);
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void removeOrderElement() {
        OrderElement orderElement = null;
        HoursGroup hoursGroup = null;
        try {
            orderElement = orderElementDAO.findUniqueByCode("1a1k1k1k");
            hoursGroup = hoursGroupDAO.findByCode("hoursGroupName1");
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        }

        OrderElement orderElementToDelete = orderElement;
        HoursGroup hoursGroupToDelete = hoursGroup;

        orderElementToDelete.getHoursGroups().remove(0);
        hoursGroupToDelete.setParentOrderLine(null);

        try {
            orderElementDAO.remove(orderElement.getId());
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        }
    }
}
