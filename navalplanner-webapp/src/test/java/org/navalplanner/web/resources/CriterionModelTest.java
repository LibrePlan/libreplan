package org.navalplanner.web.resources;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;

import java.util.List;
import java.util.UUID;

import org.hibernate.SessionFactory;
import org.hibernate.validator.InvalidStateException;
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
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.PredefinedCriterionTypes;
import org.navalplanner.web.resources.criterion.CriterionsModel;
import org.navalplanner.web.resources.criterion.ICriterionsModel;
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
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE })
@Transactional
public class CriterionModelTest {

    @Autowired
    private ICriterionsModel criterionModel;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private IAdHocTransactionService adHocTransactionService;

    @Autowired
    private ICriterionDAO criterionDAO;

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    private Criterion criterion;

    @Test(expected = InvalidStateException.class)
    public void cantSaveCriterionWithoutName() throws Exception {
        givenValidCriterion();
        criterion.setName("");
        criterionModel.save(criterion);
        sessionFactory.getCurrentSession().flush();
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
        return CriterionType.create(name);
    }

    public static CriterionType createValidCriterionType() {
        String unique = UUID.randomUUID().toString();
        return createValidCriterionType(unique);
    }

    @Test
    public void savingCriterionIncreasesTheNumberOfCriterions()
            throws Exception {
        givenValidCriterionFor(PredefinedCriterionTypes.WORK_RELATIONSHIP);
        int initial = getCriterionsNumber(PredefinedCriterionTypes.WORK_RELATIONSHIP);
        criterionModel.save(criterion);
        criterionDAO.flush();
        assertThat(
                getCriterionsNumber(PredefinedCriterionTypes.WORK_RELATIONSHIP),
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
        List<CriterionType> found = criterionTypeDAO.findByName(transientType);
        if (!found.isEmpty())
            return found.get(0);
        criterionTypeDAO.save(transientType);
        return criterionTypeDAO.findByName(transientType).get(0);
    }

    @Test
    @NotTransactional
    public void modificationsAreSaved() throws Exception {
        adHocTransactionService.onTransaction(new IOnTransaction<Void>() {

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
                .onTransaction(new IOnTransaction<Criterion>() {

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
    }

    @Test
    public void modifyingDontAlterTheNumberOfCriterions() throws Exception {
        givenCreatedCriterionFor(PredefinedCriterionTypes.WORK_RELATIONSHIP);
        int initial = getCriterionsNumber(PredefinedCriterionTypes.WORK_RELATIONSHIP);
        String newName = UUID.randomUUID().toString() + "random";
        criterion.setName(newName);
        criterionModel.save(criterion);
        assertThat(
                getCriterionsNumber(PredefinedCriterionTypes.WORK_RELATIONSHIP),
                equalTo(initial));
    }

    private void givenCreatedCriterionFor(PredefinedCriterionTypes type) {
        givenValidCriterionFor(type);
        try {
            criterionModel.save(criterion);
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
    }

    private int getCriterionsNumber(final ICriterionType<?> type) {
        return adHocTransactionService
                .onTransaction(new IOnTransaction<Integer>() {

                    @Override
                    public Integer execute() {
                        return criterionDAO.findByType(type).size();
                    }
                }).intValue();
    }

    @Test
    public void theSameCriterionCanBeSavedTwice() throws ValidationException {
        givenValidCriterion();
        criterionModel.save(criterion);
        criterionModel.save(criterion);
    }

    @Test(expected = ValidationException.class)
    public void twoDifferentCriterionsWithSameNameAndTypeAreDetectedIfPossible()
            throws ValidationException {
        String unique = UUID.randomUUID().toString();
        Criterion criterion = givenValidCriterionFor(
                PredefinedCriterionTypes.WORK_RELATIONSHIP, unique);
        criterionModel.save(criterion);
        Criterion criterion2 = givenValidCriterionFor(
                PredefinedCriterionTypes.WORK_RELATIONSHIP, unique);
        criterionModel.save(criterion2);
    }

}
