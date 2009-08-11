package org.navalplanner.web.resources;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;

import java.util.Collection;
import java.util.UUID;

import org.hibernate.SessionFactory;
import org.hibernate.validator.InvalidStateException;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.IAdHocTransactionService;
import org.navalplanner.business.common.IOnTransaction;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.resources.entities.ICriterion;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.PredefinedCriterionTypes;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.resources.services.IResourceService;
import org.navalplanner.web.resources.criterion.CriterionsModel;
import org.navalplanner.web.resources.criterion.ICriterionsModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for {@link CriterionsModel}. <br />
 *
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
    private IResourceService resourceService;

    @Test(expected = InvalidStateException.class)
    public void testCantSaveCriterionWithoutNameAndType() throws Exception {
        Criterion criterion = createValidCriterion("valido");
        criterion.setName("");
        criterionModel.save(criterion);
        sessionFactory.getCurrentSession().flush();
    }

    public static Criterion createValidCriterion() {
        return createValidCriterion(UUID.randomUUID().toString());
    }

    public static Criterion createValidCriterion(String name) {
        CriterionType criterionType = createValidCriterionType();

        return Criterion.withNameAndType(name, criterionType);
    }

    public static CriterionType createValidCriterionType(String name) {
        return new CriterionType(name);
    }

    public static CriterionType createValidCriterionType() {
        String unique = UUID.randomUUID().toString();
        return createValidCriterionType(unique);
    }

    @Test
    public void testAddCriterion() throws Exception {
        String unique = UUID.randomUUID().toString();
        Criterion criterion = PredefinedCriterionTypes.WORK_RELATIONSHIP
                .createCriterion(unique);
        criterionModel.save(criterion);
    }

    @Test
    @NotTransactional
    public void testEditingCriterion() throws Exception {
        String unique = UUID.randomUUID().toString();
        final Criterion criterion = PredefinedCriterionTypes.WORK_RELATIONSHIP
                .createCriterion(unique);
        int initial = getCriterionsNumber(PredefinedCriterionTypes.WORK_RELATIONSHIP);
        criterionModel.save(criterion);
        assertThat(
                "after saving one more",
                getCriterionsNumber(PredefinedCriterionTypes.WORK_RELATIONSHIP),
                equalTo(initial + 1));
        criterion.setActive(false);
        String newName = UUID.randomUUID().toString() + "random";
        criterion.setName(newName);
        criterionModel.save(criterion);
        assertThat(
                "after editing there are the same",
                getCriterionsNumber(PredefinedCriterionTypes.WORK_RELATIONSHIP),
                equalTo(initial + 1));
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

        adHocTransactionService.onTransaction(new IOnTransaction<Void>() {

            @Override
            public Void execute() {
                if (criterion.getId() != null) {
                    try {
                        criterionDAO.remove(criterion.getId());
                    } catch (InstanceNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    criterionDAO.removeByNameAndType(criterion);
                }
                return null;
            }
        });
    }

    private int getCriterionsNumber(final ICriterionType<?> type) {
        int size = adHocTransactionService
                .onTransaction(new IOnTransaction<Integer>() {

                    @Override
                    public Integer execute() {
                        return criterionDAO.findByType(type).size();
                    }
                });
        return size;
    }

    @Test
    public void testSaveSameCriterionTwice() throws ValidationException {
        String unique = UUID.randomUUID().toString();
        Criterion criterion = PredefinedCriterionTypes.WORK_RELATIONSHIP
                .createCriterion(unique);
        criterionModel.save(criterion);
        criterionModel.save(criterion);
    }

    @Test
    public void testCreateIfNotExists() throws ValidationException {
        String unique = UUID.randomUUID().toString();
        Criterion criterion = PredefinedCriterionTypes.WORK_RELATIONSHIP
                .createCriterion(unique);
        if (!(criterionDAO.exists(criterion.getId()) || criterionDAO
                .existsByNameAndType(criterion)))
            criterionModel.save(criterion);
        assertTrue(criterionDAO.exists(criterion.getId())
                || criterionDAO.existsByNameAndType(criterion));
        if (!(criterionDAO.exists(criterion.getId()) || criterionDAO
                .existsByNameAndType(criterion)))
            criterionModel.save(PredefinedCriterionTypes.WORK_RELATIONSHIP
                    .createCriterion(unique));
    }

    @Test(expected = ValidationException.class)
    @NotTransactional
    public void twoDifferentCriterionsWithSameNameAndTypeAreDetectedIfPossible()
            throws ValidationException {
        String unique = UUID.randomUUID().toString();
        Criterion criterion = PredefinedCriterionTypes.WORK_RELATIONSHIP
                .createCriterion(unique);
        criterionModel.save(criterion);
        Criterion criterion2 = PredefinedCriterionTypes.WORK_RELATIONSHIP
                .createCriterion(unique);
        criterionModel.save(criterion2);
    }

    public static class ResourceTest extends Resource {

        @Override
        public int getDailyCapacity() {
            return 0;
        }

        @Override
        public String getDescription() {
            return "";
        }

    }

    @Test
    @NotTransactional
    public void testCriterionIsEquivalentOnDetachedAndProxifiedCriterion()
            throws Exception {
        final Worker worker1 = new Worker("worker-1", "worker-2-surname",
                "11111111A", 8);
        resourceService.saveResource(worker1);
        Criterion criterion = createValidCriterion();
        criterionModel.save(criterion);
        createTypeThatMatches(criterion);
        worker1.addSatisfaction(new CriterionWithItsType(criterion.getType(),
                criterion));
        resourceService.saveResource(worker1);
        Resource workerReloaded = adHocTransactionService
                .onTransaction(new IOnTransaction<Resource>() {

                    @Override
                    public Resource execute() {
                        try {
                            Resource result = resourceService
                                    .findResource(worker1.getId());
                            forceLoadSatisfactions(result);
                            return result;
                        } catch (InstanceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        Collection<CriterionSatisfaction> satisfactionsFor = workerReloaded
                .getSatisfactionsFor(criterion.getType());
        Criterion reloadedCriterion = satisfactionsFor.iterator().next()
                .getCriterion();
        Assume.assumeTrue(!reloadedCriterion.getClass().equals(
                criterion.getClass()));
        assertTrue(reloadedCriterion.isEquivalent(criterion));
    }

    private void forceLoadSatisfactions(Resource resource) {
        for (CriterionSatisfaction criterionSatisfaction : resource
                .getAllSatisfactions()) {
            criterionSatisfaction.getCriterion().getName();
            criterionSatisfaction.getCriterion().getType().getName();
        }
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
            public boolean allowSimultaneousCriterionsPerResource() {
                return allowSimultaneousCriterionsPerResource;
            }

            @Override
            public boolean allowHierarchy() {
                return false;
            }

            @Override
            public boolean contains(ICriterion c) {
                return criterion.isEquivalent(c);
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
            public boolean allowAdding() {
                return false;
            }

            @Override
            public boolean allowEditing() {
                return false;
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
        };
    }

}
