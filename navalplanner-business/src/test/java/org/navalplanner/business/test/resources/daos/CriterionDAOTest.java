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

package org.navalplanner.business.test.resources.daos;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.ICriterion;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test cases for CriterionDAO <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
@Transactional
public class CriterionDAOTest {

    @Autowired
    private ICriterionDAO criterionDAO;

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    private Criterion criterion;

    @Test
    public void testInSpringContainer() {
        assertNotNull(criterionDAO);
    }

    public static Criterion createValidCriterion() {
        return createValidCriterion(UUID.randomUUID().toString(),"");
    }

    public static Criterion createValidCriterion(String name,String description) {
        CriterionType criterionType = CriterionTypeDAOTest
                .createValidCriterionType();
        return Criterion.withNameAndType(name, criterionType);
    }

    private CriterionType ensureTypeExists(CriterionType criterionType) {
        if (criterionTypeDAO.existsOtherCriterionTypeByName(criterionType)) {
            try {
                return criterionType = criterionTypeDAO
                        .findUniqueByName(criterionType);
            } catch (InstanceNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            criterionTypeDAO.save(criterionType);
            return criterionType;
        }

    }

    @Test(expected = DataIntegrityViolationException.class)
    public void aCriterionRelatedToATransientTypeCannotBeSaved() {
        givenACriterionWithATransientCriterionType();
        criterionDAO.save(criterion);
    }

    private void givenACriterionWithATransientCriterionType() {
        this.criterion = createValidCriterion();
    }

    @Test
    public void afterSavingACriterionItExists() {
        givenACriterionWithAnExistentType();
        criterionDAO.save(criterion);
        assertTrue(criterionDAO.exists(criterion.getId()));
    }

    @Test
    public void afterRemovingTheCriterionNoLongerExists()
            throws InstanceNotFoundException {
        givenACriterionWithAnExistentType();
        criterionDAO.save(criterion);
        criterionDAO.remove(criterion.getId());
        assertFalse(criterionDAO.exists(criterion.getId()));
    }

    private Criterion givenACriterionWithAnExistentType() {
        this.criterion = createValidCriterion();
        CriterionType type = ensureTypeExists(CriterionTypeDAOTest
                .createValidCriterionType());
        this.criterion.setType(type);
        return this.criterion;
    }

    private Criterion givenUniquelyNamedCriterion() {
        return givenACriterionWithAnExistentType();
    }

    @Test
    public void listReturnsTheNewlyCreatedCriterions() {
        int previous = criterionDAO.list(Criterion.class).size();
        givenASavedCriterionWithAnExistentType();
        givenASavedCriterionWithAnExistentType();
        List<Criterion> list = criterionDAO.list(Criterion.class);
        assertEquals(previous + 2, list.size());
    }

    private Criterion givenASavedCriterionWithAnExistentType() {
        Criterion c = givenACriterionWithAnExistentType();
        criterionDAO.save(c);
        return c;
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void schemaEnsuresCannotExistTwoDifferentCriterionsWithSameNameAndType()
            throws ValidationException {
        Criterion c = givenASavedCriterionWithAnExistentType();
        Criterion repeated = anotherCriterionWithSameNameAndType(c);
        criterionDAO.save(repeated);
        criterionDAO.flush();
    }

    private Criterion anotherCriterionWithSameNameAndType(Criterion c) {
        return Criterion.create(c.getName(), c.getType());
    }

    @Test
    public void findByTypeOnlyReturnsTheCriterionsMatchedByType() {
        givenASavedCriterionWithAnExistentType();
        // saving another
        givenASavedCriterionWithAnExistentType();
        ICriterionType<Criterion> type = createTypeThatMatches(criterion);
        Collection<Criterion> criterions = criterionDAO.findByType(type);
        assertEquals(1, criterions.size());
        assertTrue(criterions.contains(criterion));
    }

    @Test
    @NotTransactional
    public void thereIsOtherWithSameNameAndTypeWorksIsolatedFromCurrentTransaction() {
        transactionService.runOnTransaction(new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                Criterion saved = givenASavedCriterionWithAnExistentType();
                assertFalse(criterionDAO.thereIsOtherWithSameNameAndType(saved));
                return null;
            }
        });
    }

    @Test
    @NotTransactional
    public void thereIsNoOtherIfItsTheSame() {
        Criterion c = transactionService
                .runOnTransaction(new IOnTransaction<Criterion>() {

            @Override
            public Criterion execute() {
                return givenASavedCriterionWithAnExistentType();
            }
        });
        assertFalse(criterionDAO.thereIsOtherWithSameNameAndType(c));
    }

    @Test
    @NotTransactional
    public void ifItsDifferentThereIsOther() {
        Criterion c = transactionService
                .runOnTransaction(new IOnTransaction<Criterion>() {

                    @Override
                    public Criterion execute() {
                        return givenASavedCriterionWithAnExistentType();
                    }
                });
        Criterion copy = Criterion.create(c.getName(), c.getType());
        assertTrue(criterionDAO.thereIsOtherWithSameNameAndType(copy));
    }

    @Test
    public void noOtherIfTheCriterionDoesntExist() {
        Criterion criterion = givenUniquelyNamedCriterion();
        assertFalse(criterionDAO.thereIsOtherWithSameNameAndType(criterion));
    }

    private static ICriterionType<Criterion> createTypeThatMatches(
            final Criterion criterion) {
        return createTypeThatMatches(false, criterion);
    }

    private static ICriterionType<Criterion> createTypeThatMatches(
            final boolean allowSimultaneousCriterionsPerResource,
            final Criterion criterion) {
        return new ICriterionType<Criterion>() {

            @Override
            public boolean isAllowSimultaneousCriterionsPerResource() {
                return allowSimultaneousCriterionsPerResource;
            }

            @Override
            public boolean allowHierarchy() {
                return false;
            }

            @Override
            public boolean contains(ICriterion c) {
                return c instanceof Criterion
                        && criterion.isEquivalent((Criterion) c);
            }

            @Override
            public Criterion createCriterion(String name) {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public boolean criterionCanBeRelatedTo(
                    Class<? extends Resource> klass) {
                return true;
            }

            @Override
            public Criterion createCriterionWithoutNameYet() {
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public boolean isImmutable() {
                return false;
            }
        };
    }

}
