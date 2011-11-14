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

package org.libreplan.web.resources;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.libreplan.web.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;
import static org.libreplan.web.test.WebappGlobalNames.WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.resources.daos.ICriterionDAO;
import org.libreplan.business.resources.daos.ICriterionTypeDAO;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionType;
import org.libreplan.business.resources.entities.ICriterionType;
import org.libreplan.business.resources.entities.PredefinedCriterionTypes;
import org.libreplan.web.resources.criterion.CriterionsModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for {@link CriterionsModel}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_FILE,
        WEBAPP_SPRING_SECURITY_CONFIG_TEST_FILE })
@Transactional
public class CriterionModelTest {

    @Autowired
    private IAdHocTransactionService adHocTransactionService;

    @Autowired
    private ICriterionDAO criterionDAO;

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Autowired
    private IAdHocTransactionService transactionService;

    private Criterion criterion;

    @Test(expected = ValidationException.class)
    public void cantSaveCriterionWithoutName() {
        givenValidCriterion();
        criterion.setName("");
        criterionDAO.save(criterion);
    }

    private Criterion givenValidCriterion() {
        criterion = createValidCriterion("valido");
        criterionTypeDAO.save(criterion.getType());
        return criterion;
    }

    public static Criterion createValidCriterion() {
        return createValidCriterion(UUID.randomUUID().toString());
    }

    public static Criterion createValidCriterion(String name) {
        CriterionType criterionType = createValidCriterionType();

        return Criterion.withNameAndType(name, criterionType);
    }

    public static CriterionType createValidCriterionType(String name) {
        return CriterionType.create(name,"");
    }

    public static CriterionType createValidCriterionType() {
        String unique = UUID.randomUUID().toString();
        return createValidCriterionType(unique);
    }

    @Test
    public void savingCriterionIncreasesTheNumberOfCriterions()
            {
        givenValidCriterionFor(PredefinedCriterionTypes.CATEGORY);
        int initial = getCriterionsNumber(PredefinedCriterionTypes.CATEGORY);
        criterionDAO.save(criterion);
        criterionDAO.flush();
        assertThat(getCriterionsNumber(PredefinedCriterionTypes.CATEGORY),
                equalTo(initial + 1));
    }

    private Criterion givenValidCriterionFor(PredefinedCriterionTypes type) {
        return givenValidCriterionFor(type, UUID.randomUUID().toString());
    }

    private Criterion givenValidCriterionFor(PredefinedCriterionTypes type,
            String name) {
        this.criterion = type.createCriterion(name);
        this.criterion
                .setType(ensureExists(CriterionType.asCriterionType(type)));
        return this.criterion;
    }

    private CriterionType ensureExists(CriterionType transientType) {
        CriterionType found = criterionTypeDAO.findByName(transientType
                .getName());
        if (found != null) {
            return found;
        }
        criterionTypeDAO.save(transientType);
        return criterionTypeDAO.findByName(transientType.getName());
    }

    /*@Test
    @NotTransactional
    public void modificationsAreSaved() {
        adHocTransactionService.runOnTransaction(new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                givenCreatedCriterionFor(PredefinedCriterionTypes.WORK_RELATIONSHIP);
                criterion.getType().dontPoseAsTransientObjectAnymore();
                return null;
            }
        });
        String newName = UUID.randomUUID().toString() + "random";
        criterion.setName(newName);
        criterionModel.save(criterion);
        Criterion retrieved = adHocTransactionService
                .runOnTransaction(new IOnTransaction<Criterion>() {

                    @Override
                    public Criterion execute() {
                        try {
                            return criterionDAO.find(criterion);
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        assertThat(retrieved.getName(), equalTo(newName));
    }*/

    @Test
    public void modifyingDontAlterTheNumberOfCriterions() {
        givenCreatedCriterionFor(PredefinedCriterionTypes.CATEGORY);
        int initial = getCriterionsNumber(PredefinedCriterionTypes.CATEGORY);
        String newName = UUID.randomUUID().toString() + "random";
        criterion.setName(newName);
        criterionDAO.save(criterion);
        assertThat(getCriterionsNumber(PredefinedCriterionTypes.CATEGORY),
                equalTo(initial));
    }

    private void givenCreatedCriterionFor(PredefinedCriterionTypes type) {
        givenValidCriterionFor(type);
        try {
            criterionDAO.save(criterion);
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
    }

    private int getCriterionsNumber(final ICriterionType<?> type) {
        return adHocTransactionService.runOnTransaction(
                new IOnTransaction<Integer>() {

                    @Override
                    public Integer execute() {
                        return criterionDAO.findByType(type).size();
                    }
                }).intValue();
    }

    @Test
    public void theSameCriterionCanBeSavedTwice() throws ValidationException {
        givenValidCriterion();
        criterionDAO.save(criterion);
        criterionDAO.save(criterion);
    }

    @NotTransactional
    public void twoDifferentCriterionsWithSameNameAndTypeAreDetectedIfPossible()
            throws ValidationException {
        final String unique = UUID.randomUUID().toString();
        transactionService.runOnTransaction(new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                Criterion criterion = givenValidCriterionFor(
                        PredefinedCriterionTypes.CATEGORY, unique);
                try {
                    criterionDAO.save(criterion);
                } catch (ValidationException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });
        transactionService.runOnTransaction(new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                try {
                    Criterion criterion2 = givenValidCriterionFor(
                            PredefinedCriterionTypes.CATEGORY, unique);
                    criterionDAO.save(criterion2);
                    fail("must send "
                            + ValidationException.class.getSimpleName());
                } catch (ValidationException e) {
                    // ok
                }
                return null;
            }
        });
    }

}
