/*
 * This file is part of LibrePlan
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

package org.libreplan.business.test.qualityforms.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.qualityforms.daos.IQualityFormDAO;
import org.libreplan.business.qualityforms.entities.QualityForm;
import org.libreplan.business.qualityforms.entities.QualityFormItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test for {@QualityDAO}
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class QualityFormDAOTest extends AbstractQualityFormTest {

    @Autowired
    IQualityFormDAO qualityFormDAO;

    @Test
    public void testInSpringContainer() {
        assertNotNull(qualityFormDAO);
    }

    @Test
    public void testSaveQualityForm() {
        QualityForm qualityForm = createValidQualityForm();
        qualityFormDAO.save(qualityForm);
        assertTrue(qualityForm.getId() != null);
    }

    @Test
    public void testRemoveQualityForm() throws InstanceNotFoundException {
        QualityForm qualityForm = createValidQualityForm();
        qualityFormDAO.save(qualityForm);
        qualityFormDAO.remove(qualityForm.getId());
        assertFalse(qualityFormDAO.exists(qualityForm.getId()));
    }

    @Test
    public void testListQualityForm() {
        int previous = qualityFormDAO.list(QualityForm.class).size();
        QualityForm qualityForm = createValidQualityForm();
        qualityFormDAO.save(qualityForm);
        List<QualityForm> list = qualityFormDAO.list(QualityForm.class);
        assertEquals(previous + 1, list.size());
    }

    @Test
    public void testSaveQualityFormItems() {
        QualityForm qualityForm = createValidQualityForm();
        QualityFormItem qualityFormItem = createValidQualityFormItem();
        qualityForm.addQualityFormItemOnTop(qualityFormItem);
        qualityFormDAO.save(qualityForm);

        assertTrue(qualityForm.getId() != null);
        assertEquals(1, qualityForm.getQualityFormItems().size());
    }

    @Test
    public void testSaveAndRemoveQualityFormItem()
            throws InstanceNotFoundException {
        QualityForm qualityForm = createValidQualityForm();
        QualityFormItem qualityFormItem = createValidQualityFormItem();
        qualityForm.addQualityFormItemOnTop(qualityFormItem);
        qualityFormDAO.save(qualityForm);

        assertTrue(qualityForm.getId() != null);
        assertEquals(1, qualityForm.getQualityFormItems().size());

        qualityForm.removeQualityFormItem(qualityFormItem);
        assertEquals(0, qualityForm.getQualityFormItems().size());
    }
}
