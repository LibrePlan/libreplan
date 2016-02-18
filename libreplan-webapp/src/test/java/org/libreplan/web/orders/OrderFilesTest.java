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


import org.hibernate.PropertyValueException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;

import org.hibernate.Transaction;
import org.junit.Test;
import org.junit.runner.RunWith;


import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderFile;
import org.libreplan.business.orders.entities.OrderLine;
import org.libreplan.business.users.entities.User;
import org.libreplan.web.orders.files.IOrderFileModel;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;


/**
 * Tests for {@link OrderFile}.
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 11.01.2016.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
public class OrderFilesTest {

    @Autowired
    private IOrderFileModel orderFileModel;

    @Autowired
    private IOrderElementModel orderElementModel;

    @Autowired
    private SessionFactory sessionFactory;

    private Session newSession;

    private Transaction transaction;

    /**
     * A new session is opened because there was a lot of errors with {@link Transaction}, {@link Session}
     *
     * CRUD test
     * 1. Add row to files table
     * 2. Add row and read it
     * 3. Add row and update added row
     * 4. Add row and delete it
     *
     * Negative test
     * 1. Create row with null field value and try to save it
     */
    @Test
    public void testRead() {
        // This initialize method is for all test methods
        initialize();

        // Make OrderFile
        orderFileModel.createNewFileObject();
        orderFileModel.setFileName("noname");
        orderFileModel.setFileType(".htaccess");
        orderFileModel.setUploadDate(new Date());
        orderFileModel.setUploader( (User) newSession.createCriteria(User.class).list().get(0) );
        orderFileModel.setParent( (OrderElement) newSession.createCriteria(OrderElement.class).list().get(0) );

        newSession.save(orderFileModel.getOrderFile());
        transactionBeginCommit();

        List<OrderFile> file = orderFileModel.getAll();
        assertEquals(file.get(0).getName(), "noname");

        // Clean data
        OrderFile orderFile = (OrderFile) newSession.createCriteria(OrderFile.class).list().get(0);
        newSession.delete(orderFile);
        transactionBeginCommit();

        newSession.close();
    }

    @Test
    public void testCreate(){
        newSession = sessionFactory.openSession();

        int sizeBefore = orderFileModel.getAll().size();

        // Make OrderFile
        orderFileModel.createNewFileObject();
        orderFileModel.setFileName("Index");
        orderFileModel.setFileType("html");
        orderFileModel.setUploadDate(new Date());
        orderFileModel.setUploader( (User) newSession.createCriteria(User.class).list().get(0) );
        orderFileModel.setParent( (OrderElement) newSession.createCriteria(OrderElement.class).list().get(0) );

        newSession.save(orderFileModel.getOrderFile());
        transactionBeginCommit();

        int sizeWithNewRow = orderFileModel.getAll().size();

        assertEquals(sizeBefore + 1, sizeWithNewRow);

        // Clean data
        OrderFile orderFile = (OrderFile) newSession.createCriteria(OrderFile.class).list().get(0);
        newSession.delete(orderFile);
        transactionBeginCommit();

        newSession.close();
    }

    @Test
    public void testDelete(){
        newSession = sessionFactory.openSession();

        int sizeBefore = orderFileModel.getAll().size();

        // Make OrderFile
        orderFileModel.createNewFileObject();
        orderFileModel.setFileName("Main");
        orderFileModel.setFileType("java");
        orderFileModel.setUploadDate(new Date());
        orderFileModel.setUploader( (User) newSession.createCriteria(User.class).list().get(0) );
        orderFileModel.setParent( (OrderElement) newSession.createCriteria(OrderElement.class).list().get(0) );

        newSession.save(orderFileModel.getOrderFile());
        transactionBeginCommit();

        newSession.delete(orderFileModel.getOrderFile());
        transactionBeginCommit();

        int sizeAfter = orderFileModel.getAll().size();

        assertEquals(sizeBefore, sizeAfter);

        newSession.close();
    }

    @Test(expected = PropertyValueException.class)
    public void testCreateNotValidRow(){
        newSession = sessionFactory.openSession();

        // Make OrderFile
        orderFileModel.createNewFileObject();
        orderFileModel.setFileName(null);
        orderFileModel.setFileType("html");
        orderFileModel.setUploadDate(new Date());
        orderFileModel.setUploader( (User) newSession.createCriteria(User.class).list().get(0) );
        orderFileModel.setParent( (OrderElement) newSession.createCriteria(OrderElement.class).list().get(0) );

        newSession.save(orderFileModel.getOrderFile());
        transactionBeginCommit();

        newSession.close();
    }

    @Test
    public void testUpdate(){
        newSession = sessionFactory.openSession();

        // Make OrderFile
        orderFileModel.createNewFileObject();
        orderFileModel.setFileName("yii");
        orderFileModel.setFileType("bat");
        orderFileModel.setUploadDate(new Date());
        orderFileModel.setUploader( (User) newSession.createCriteria(User.class).list().get(0) );
        orderFileModel.setParent( (OrderElement) newSession.createCriteria(OrderElement.class).list().get(0) );

        newSession.save(orderFileModel.getOrderFile());
        transactionBeginCommit();

        // Get saved OrderFile and update it
        OrderFile orderFile = (OrderFile) newSession.createCriteria(OrderFile.class).list().get(0);
        orderFile.setName("yii2");

        newSession.save(orderFile);
        transactionBeginCommit();

        // Get updated OrderFile and check for equality
        OrderFile newOrderFile = (OrderFile) newSession.createCriteria(OrderFile.class).list().get(0);
        assertTrue( newOrderFile.getName().equals("yii2") );

        /**
         * It is necessary to delete user and order element because it make influence on some other tests
         */
        OrderFile orderFileToDelete = (OrderFile) newSession.createCriteria(OrderFile.class).list().get(0);
        User userToDelete = (User) newSession.createCriteria(User.class).list().get(0);
        OrderElement orderElementToDelete = (OrderElement) newSession.createCriteria(OrderElement.class).list().get(0);

        newSession.delete(orderFileToDelete);
        newSession.delete(userToDelete);
        newSession.delete(orderElementToDelete);
        transactionBeginCommit();

        newSession.close();
    }


    public void initialize(){
        newSession = sessionFactory.openSession();
        createUser();
        createOrderElement();
    }

    private void createUser(){
        User user = User.create("harry-potter", "somePassword", "harry-potter@hogwarts.uk");
        newSession.save(user);
        transactionBeginCommit();
    }

    private void createOrderElement(){
        OrderLine result = OrderLine.create();
        result.setName("OrderLineB");
        result.setCode("1a1k1k1k");
        HoursGroup hoursGroup = HoursGroup.create(result);
        hoursGroup.setWorkingHours(0);
        hoursGroup.setCode("hoursGroupName1");
        result.addHoursGroup(hoursGroup);

        OrderElement orderElement = result;
        orderElementModel.setCurrent(orderElement, new OrderModel());
        newSession.save(orderElement);
        transactionBeginCommit();
    }

    private void transactionBeginCommit(){
        transaction = newSession.beginTransaction();
        transaction.commit();
    }
}
