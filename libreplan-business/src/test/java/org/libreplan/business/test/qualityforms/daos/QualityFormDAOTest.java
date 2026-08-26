/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.qualityforms.daos.IQualityFormDAO;
import org.libreplan.business.qualityforms.entities.QualityForm;
import org.libreplan.business.qualityforms.entities.QualityFormItem;
import org.libreplan.business.qualityforms.entities.QualityFormType;
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
public class QualityFormDAOTest extends AbstractQualityFormTest {

    @Autowired
    IQualityFormDAO qualityFormDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    @Test
    @Transactional
    public void testInSpringContainer() {
        assertNotNull(qualityFormDAO);
    }

    @Test
    @Transactional
    public void testSaveQualityForm() {
        QualityForm qualityForm = createValidQualityForm();
        qualityFormDAO.save(qualityForm);
        assertTrue(qualityForm.getId() != null);
    }

    @Test
    @Transactional
    public void testRemoveQualityForm() throws InstanceNotFoundException {
        QualityForm qualityForm = createValidQualityForm();
        qualityFormDAO.save(qualityForm);
        qualityFormDAO.remove(qualityForm.getId());
        assertFalse(qualityFormDAO.exists(qualityForm.getId()));
    }

    @Test
    @Transactional
    public void testListQualityForm() {
        int previous = qualityFormDAO.list(QualityForm.class).size();
        QualityForm qualityForm = createValidQualityForm();
        qualityFormDAO.save(qualityForm);
        List<QualityForm> list = qualityFormDAO.list(QualityForm.class);
        assertEquals(previous + 1, list.size());
    }

    @Test
    @Transactional
    public void testSaveQualityFormItems() {
        QualityForm qualityForm = createValidQualityForm();
        QualityFormItem qualityFormItem = createValidQualityFormItem();
        qualityForm.addQualityFormItemOnTop(qualityFormItem);
        qualityFormDAO.save(qualityForm);

        assertTrue(qualityForm.getId() != null);
        assertEquals(1, qualityForm.getQualityFormItems().size());
    }

    @Test
    @Transactional
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

    /*
     * Characterization tests added for the Hibernate Criteria -> JPA Criteria API migration
     * (Jakarta EE / Hibernate 6). findByNameAndType/getAllByType/isUnique/findUniqueByName/
     * existsOtherWorkReportTypeByName had no test coverage before. All these methods are
     * @Transactional(REQUIRES_NEW), so (like elsewhere in this migration) they can't see data
     * saved earlier in the same @Transactional test method - test methods here run without
     * @Transactional and use IAdHocTransactionService to genuinely commit each step.
     */

    @Test
    public void testFindByNameAndTypeRequiresBothToMatch() {
        final String name = "name-" + UUID.randomUUID();

        transactionService.runOnAnotherTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                QualityForm qualityForm = QualityForm.create(name, UUID.randomUUID().toString());
                qualityFormDAO.save(qualityForm);
                return null;
            }
        });

        assertNotNull(qualityFormDAO.findByNameAndType(name, QualityFormType.getDefault()));
        assertNull(qualityFormDAO.findByNameAndType(name,
                QualityFormType.getDefault() == QualityFormType.BY_ITEMS
                        ? QualityFormType.BY_PERCENTAGE : QualityFormType.BY_ITEMS));
        assertNull(qualityFormDAO.findByNameAndType("does-not-exist-" + UUID.randomUUID(),
                QualityFormType.getDefault()));
    }

    @Test
    public void testGetAllByTypeOnlyReturnsMatchingType() {
        final String name1 = "name-" + UUID.randomUUID();
        final String name2 = "name-" + UUID.randomUUID();

        transactionService.runOnAnotherTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                QualityForm byPercentage = QualityForm.create(name1, UUID.randomUUID().toString());
                byPercentage.setQualityFormType(QualityFormType.BY_PERCENTAGE);
                qualityFormDAO.save(byPercentage);

                QualityForm byItems = QualityForm.create(name2, UUID.randomUUID().toString());
                byItems.setQualityFormType(QualityFormType.BY_ITEMS);
                qualityFormDAO.save(byItems);
                return null;
            }
        });

        List<QualityForm> result = qualityFormDAO.getAllByType(QualityFormType.BY_PERCENTAGE);
        boolean found = false;
        for (QualityForm qf : result) {
            assertEquals(QualityFormType.BY_PERCENTAGE, qf.getQualityFormType());
            if (qf.getName().equals(name1)) {
                found = true;
            }
            assertFalse(qf.getName().equals(name2));
        }
        assertTrue(found);
    }

    @Test(expected = InstanceNotFoundException.class)
    public void testFindUniqueByNameThrowsWhenNotFound() throws InstanceNotFoundException {
        qualityFormDAO.findUniqueByName("does-not-exist-" + UUID.randomUUID());
    }

    @Test
    public void testFindUniqueByNameReturnsMatch() throws InstanceNotFoundException {
        final String name = "name-" + UUID.randomUUID();

        transactionService.runOnAnotherTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                QualityForm qualityForm = QualityForm.create(name, UUID.randomUUID().toString());
                qualityFormDAO.save(qualityForm);
                return null;
            }
        });

        assertEquals(name, qualityFormDAO.findUniqueByName(name).getName());
    }

    @Test
    public void testExistsOtherWorkReportTypeByNameFalseWhenNotFound() {
        QualityForm notSaved = QualityForm.create("does-not-exist-" + UUID.randomUUID(), "desc");
        assertFalse(qualityFormDAO.existsOtherWorkReportTypeByName(notSaved));
    }

    @Test
    public void testIsUniqueTrueWhenNameNotUsedByAnything() {
        QualityForm notSaved = QualityForm.create("unused-" + UUID.randomUUID(), "desc");
        assertTrue(qualityFormDAO.isUnique(notSaved));
    }

    @Test
    public void testIsUniqueTrueWhenOnlyMatchIsItself() {
        final String name = "name-" + UUID.randomUUID();

        QualityForm qualityForm = transactionService.runOnAnotherTransaction(new IOnTransaction<QualityForm>() {
            @Override
            public QualityForm execute() {
                QualityForm result = QualityForm.create(name, UUID.randomUUID().toString());
                qualityFormDAO.save(result);
                return result;
            }
        });

        assertTrue(qualityFormDAO.isUnique(qualityForm));
    }

    @Test
    public void testIsUniqueFalseWhenNameUsedByAnotherQualityForm() {
        final String name = "name-" + UUID.randomUUID();

        transactionService.runOnAnotherTransaction(new IOnTransaction<Void>() {
            @Override
            public Void execute() {
                QualityForm existing = QualityForm.create(name, UUID.randomUUID().toString());
                qualityFormDAO.save(existing);
                return null;
            }
        });

        QualityForm another = QualityForm.create(name, "different description");
        assertFalse(qualityFormDAO.isUnique(another));
    }

}
