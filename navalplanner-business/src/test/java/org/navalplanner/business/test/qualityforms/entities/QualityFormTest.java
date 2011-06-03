/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.navalplanner.business.test.qualityforms.entities;

import static org.junit.Assert.fail;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.qualityforms.daos.IQualityFormDAO;
import org.navalplanner.business.qualityforms.entities.QualityForm;
import org.navalplanner.business.qualityforms.entities.QualityFormItem;
import org.navalplanner.business.qualityforms.entities.QualityFormType;
import org.navalplanner.business.test.qualityforms.daos.AbstractQualityFormTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class QualityFormTest extends AbstractQualityFormTest {

    @Autowired
    IQualityFormDAO qualityFormDAO;

    @Test
    public void checkInvalidNameQualityForm() throws ValidationException {
        QualityForm qualityForm = createValidQualityForm();
        qualityForm.setName("");
        try {
            qualityFormDAO.save(qualityForm);
            fail("It should throw an exception");
        } catch (ValidationException e) {
            // It should throw an exception
        }

        qualityForm.setName(null);
        try {
            qualityFormDAO.save(qualityForm);
            fail("It should throw an exception");
        } catch (ValidationException e) {
            // It should throw an exception
        }
    }

    @Test
    public void checkInvalidQualityFormType() throws ValidationException {
        QualityForm qualityForm = createValidQualityForm();
        try {
            qualityForm.setQualityFormType(null);
            qualityFormDAO.save(qualityForm);
            fail("It should throw an exception");
        } catch (ValidationException e) {
            // It should throw an exception
        } catch (IllegalArgumentException e) {
            // It should throw an exception
        }
    }

    @Test
    public void checkInvalidRepeatedQualityFormItemPosition()
            throws ValidationException {
        QualityForm qualityForm = createValidQualityForm();

        QualityFormItem qualityFormItem1 = createValidQualityFormItem();
        qualityForm.addQualityFormItemOnTop(qualityFormItem1);

        QualityFormItem qualityFormItem2 = createValidQualityFormItem();
        qualityForm.addQualityFormItemOnTop(qualityFormItem2);

        qualityFormItem1.setPosition(0);
        qualityFormItem2.setPosition(0);

        try {
            qualityFormDAO.save(qualityForm);
            fail("It should throw an exception");
        } catch (ValidationException e) {
            // It should throw an exception
        }

    }

    @Test
    public void checkInvalidNotConsecutivesQualityFormItemPosition()
            throws ValidationException {
        QualityForm qualityForm = createValidQualityForm();

        QualityFormItem qualityFormItem1 = createValidQualityFormItem();
        qualityForm.addQualityFormItemOnTop(qualityFormItem1);

        QualityFormItem qualityFormItem2 = createValidQualityFormItem();
        qualityForm.addQualityFormItemOnTop(qualityFormItem2);

        qualityFormItem1.setPosition(0);
        qualityFormItem2.setPosition(2);

        try {
            qualityFormDAO.save(qualityForm);
            fail("It should throw an exception");
        } catch (ValidationException e) {
            // It should throw an exception
        }

    }

    @Test
    public void checkInvalidOutOfRangeQualityFormItemPosition()
            throws ValidationException {
        QualityForm qualityForm = createValidQualityForm();

        QualityFormItem qualityFormItem1 = createValidQualityFormItem();
        qualityForm.addQualityFormItemOnTop(qualityFormItem1);

        QualityFormItem qualityFormItem2 = createValidQualityFormItem();
        qualityForm.addQualityFormItemOnTop(qualityFormItem2);

        qualityFormItem1.setPosition(1);
        qualityFormItem2.setPosition(2);


        try {
            qualityFormDAO.save(qualityForm);
            fail("It should throw an exception");
        } catch (ValidationException e) {
            // It should throw an exception
        }

    }

    @Test
    public void checkInvalidPercentageQualityFormItemPosition()
            throws ValidationException {
        QualityForm qualityForm = createValidQualityForm();

        QualityFormItem qualityFormItem1 = createValidQualityFormItem();
        qualityForm.addQualityFormItemOnTop(qualityFormItem1);

        QualityFormItem qualityFormItem2 = createValidQualityFormItem();
        qualityForm.addQualityFormItemOnTop(qualityFormItem2);

        qualityFormItem1.setPosition(0);
        qualityFormItem1.setPercentage(new BigDecimal(1));
        qualityFormItem2.setPosition(1);
        qualityFormItem2.setPercentage(new BigDecimal(2));

        try {
            qualityFormDAO.save(qualityForm);
        } catch (ValidationException e) {
            fail("It shouldn't throw an exception");
        }

        qualityFormItem1.setPercentage(new BigDecimal(2));
        qualityFormItem2.setPercentage(new BigDecimal(1));

        try {
            qualityFormDAO.save(qualityForm);
            fail("It should throw an exception");
        } catch (ValidationException e) {
            // It should throw an exception
        }
    }

    @Test
    public void checkInvalidQualityFormItemPositionByItems()
            throws ValidationException {
        QualityForm qualityForm = createValidQualityForm();
        qualityForm.setQualityFormType(QualityFormType.BY_ITEMS);

        QualityFormItem qualityFormItem1 = createValidQualityFormItem();
        qualityForm.addQualityFormItemOnTop(qualityFormItem1);

        QualityFormItem qualityFormItem2 = createValidQualityFormItem();
        qualityForm.addQualityFormItemOnTop(qualityFormItem2);

        try {
            qualityFormDAO.save(qualityForm);
        } catch (ValidationException e) {
            fail("It shouldn't throw an exception");
        }

        // Incorrect Position
        qualityFormItem1.setPosition(2);
        qualityFormItem2.setPosition(1);

        try {
            qualityFormDAO.save(qualityForm);
            fail("It shouldn't throw an exception");
        } catch (ValidationException e) {
            // It should throw an exception
        }

        // Incorrect Percentage
        qualityFormItem1.setPosition(0);
        qualityFormItem2.setPosition(1);
        qualityFormItem1.setPercentage(new BigDecimal(100));
        qualityFormItem2.setPercentage(new BigDecimal(1));

        try {
            qualityFormDAO.save(qualityForm);
            fail("It should throw an exception");
        } catch (ValidationException e) {
            // It should throw an exception
        }

        // Incorrect Percentage
        qualityFormItem1.setPosition(0);
        qualityFormItem2.setPosition(1);
        qualityFormItem1.setPercentage(new BigDecimal(10));
        qualityFormItem2.setPercentage(new BigDecimal(1));

        try {
            qualityFormDAO.save(qualityForm);
            fail("It should throw an exception");
        } catch (ValidationException e) {
            // It should throw an exception
        }
    }

     @Test
    public void checkInvalidQualityFormItemName() throws ValidationException {
        QualityForm qualityForm = createValidQualityForm();
        QualityFormItem qualityFormItem = createValidQualityFormItem();
        qualityForm.addQualityFormItemOnTop(qualityFormItem);
        try {
            qualityFormDAO.save(qualityForm);
        } catch (ValidationException e) {
            fail("It should not throw an exception");
        }

        qualityFormItem.setName(null);
        try {
            qualityFormDAO.save(qualityForm);
            fail("It should throw an exception");
        } catch (ValidationException e) {
            // It should throw an exception
        }

        qualityFormItem.setName("");
        try {
            qualityFormDAO.save(qualityForm);
            fail("It should throw an exception");
        } catch (ValidationException e) {
            // It should throw an exception
        }
    }

    @Test
    public void checkNotNullQualityFormItemPosition()
            throws ValidationException {
        QualityForm qualityForm = createValidQualityForm();
        QualityFormItem qualityFormItem = createValidQualityFormItem();
        qualityForm.addQualityFormItemOnTop(qualityFormItem);
        qualityFormItem.setPosition(null);
        try {
            qualityFormDAO.save(qualityForm);
            fail("It should throw an exception");
        } catch (ValidationException e) {
            // It should throw an exception
        }
    }

    @Test
    public void checkNotNullQualityFormItemPercentage()
            throws ValidationException {
        QualityForm qualityForm = createValidQualityForm();
        QualityFormItem qualityFormItem = createValidQualityFormItem();
        qualityFormItem.setPercentage(null);
        qualityForm.addQualityFormItemOnTop(qualityFormItem);
        try {
            qualityFormDAO.save(qualityForm);
            fail("It should throw an exception");
        } catch (ValidationException e) {
            // It should throw an exception
        }
    }

    @Test
    public void checkIncorrectQualityFormItemPercentage()
            throws ValidationException {
        QualityForm qualityForm = createValidQualityForm();
        QualityFormItem qualityFormItem = createValidQualityFormItem();
        qualityFormItem.setPercentage(new BigDecimal(100.1));
        qualityForm.addQualityFormItemOnTop(qualityFormItem);
        try {
            qualityFormDAO.save(qualityForm);
            fail("It should throw an exception");
        } catch (ValidationException e) {
            // It should throw an exception
        }
    }

}
