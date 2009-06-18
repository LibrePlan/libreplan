package org.navalplanner.web.resources.worker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.Interval;
import org.navalplanner.business.resources.entities.PredefinedCriterionTypes;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.resources.services.CriterionService;
import org.navalplanner.business.resources.services.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for worker <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class WorkerModel implements IWorkerModel {

    private final ResourceService resourceService;
    private final ICriterionType<?>[] laboralRelatedTypes = {
            PredefinedCriterionTypes.LEAVE,
            PredefinedCriterionTypes.WORK_RELATIONSHIP };
    private Worker worker;
    private ClassValidator<Worker> workerValidator;
    private final CriterionService criterionService;

    private IMultipleCriterionActiveAssigner localizationsAssigner;

    @Autowired
    public WorkerModel(ResourceService resourceService,
            CriterionService criterionService) {
        Validate.notNull(resourceService);
        Validate.notNull(criterionService);
        this.resourceService = resourceService;
        this.workerValidator = new ClassValidator<Worker>(Worker.class);
        this.criterionService = criterionService;
    }

    @Override
    @Transactional
    public void save() throws ValidationException {
        InvalidValue[] invalidValues = workerValidator
                .getInvalidValues(getWorker());
        if (invalidValues.length > 0) {
            throw new ValidationException(invalidValues);
        }
        getLocalizationsAssigner().applyChanges();
        resourceService.saveResource(worker);
        worker = null;
        localizationsAssigner = null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Worker> getWorkers() {
        return resourceService.getWorkers();
    }

    @Override
    public Worker getWorker() {
        return worker;
    }

    @Override
    @Transactional(readOnly = true)
    public void prepareForCreate() {
        worker = new Worker();
        localizationsAssigner = new MultipleCriterionActiveAssigner(
                criterionService, worker,
                PredefinedCriterionTypes.LOCATION_GROUP);
    }

    @Override
    @Transactional(readOnly = true)
    public void prepareEditFor(Worker worker) {
        Validate.notNull(worker, "worker must be not null");
        try {
            this.worker = (Worker) resourceService.findResource(worker.getId());
            this.worker.forceLoadSatisfactions();
            localizationsAssigner = new MultipleCriterionActiveAssigner(
                    criterionService, this.worker,
                    PredefinedCriterionTypes.LOCATION_GROUP);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AddingSatisfactionResult addSatisfaction(ICriterionType<?> type,
            CriterionSatisfaction original, CriterionSatisfaction edited) {

        /* Check worker's version. */
        Worker worker = getWorker();
        resourceService.checkVersion(worker);

        /* Add criterion satisfaction. */
        edited.setResource(worker);
        boolean previouslyContained = false;
        if (previouslyContained = worker.contains(original)) {
            worker.removeCriterionSatisfaction(original);
        }
        boolean canAdd = false;
        try {
            canAdd = worker.canAddSatisfaction(type, edited);
        } catch (IllegalArgumentException e) {
            if (previouslyContained) {
                worker.addSatisfaction(type, original);
            }
            return AddingSatisfactionResult.SATISFACTION_WRONG;
        }
        if (!canAdd) {
            if (previouslyContained) {
                worker.addSatisfaction(type, original);
            }
            return AddingSatisfactionResult.DONT_COMPLY_OVERLAPPING_RESTRICTIONS;
        }
        worker.addSatisfaction(type, edited);
        return AddingSatisfactionResult.OK;
    }

    @Override
    @Transactional(readOnly = true)
    public void removeSatisfaction(CriterionSatisfaction satisfaction) {

        /* Check worker's version. */
        Worker worker = getWorker();
        resourceService.checkVersion(worker);

        /* Remove criterion satisfaction. */
        worker.removeCriterionSatisfaction(satisfaction);

    }

    @Override
    @Transactional(readOnly = true)
    public void assignCriteria(Collection<? extends Criterion> criteria) {

        /* Check worker's version. */
        Worker worker = getWorker();
        resourceService.checkVersion(worker);

        /* Assign criteria. */
        getLocalizationsAssigner().assign(criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public void unassignSatisfactions(
        Collection<? extends CriterionSatisfaction> satisfactions) {

        /* Check worker's version. */
        Worker worker = getWorker();
        resourceService.checkVersion(worker);

        /* Unassign criterion satisfactions. */
        getLocalizationsAssigner().unassign(satisfactions);

    }

    private static class NullAssigner implements
            IMultipleCriterionActiveAssigner {

        private List<CriterionSatisfaction> empty = Arrays.asList();

        private List<Criterion> emptyCriterions = Arrays.asList();

        @Override
        public void assign(Collection<? extends Criterion> criterions) {

        }

        @Override
        public List<CriterionSatisfaction> getActiveSatisfactions() {
            return empty;
        }

        @Override
        public List<Criterion> getCriterionsNotAssigned() {
            return emptyCriterions;
        }

        @Override
        public List<CriterionSatisfaction> getHistoric() {
            return empty;
        }

        @Override
        public void unassign(
                Collection<? extends CriterionSatisfaction> satisfactions) {
        }

        @Override
        public void applyChanges() {
        }

    }

    private static class MultipleCriterionActiveAssigner implements
            IMultipleCriterionActiveAssigner {
        private final Resource resource;
        private final ICriterionType<?> type;
        private final CriterionService criterionService;
        private List<CriterionSatisfaction> history;
        private List<Criterion> initialCriterionsNotAssigned;
        private Set<CriterionSatisfaction> initialActive;

        private Map<Criterion, CriterionSatisfaction> unassigned = new HashMap<Criterion, CriterionSatisfaction>();

        private Set<CriterionSatisfaction> added = new HashSet<CriterionSatisfaction>();

        public MultipleCriterionActiveAssigner(
                CriterionService criterionService, Resource resource,
                ICriterionType<?> type) {
            Validate
                    .isTrue(
                            type.allowSimultaneousCriterionsPerResource(),
                            "must allow multiple active criterions for this type to use this assignment strategy");
            this.criterionService = criterionService;
            this.resource = resource;
            this.type = type;
            this.resource.forceLoadSatisfactions();
            this.history = calculateInitialHistory();
            this.initialCriterionsNotAssigned = calculateInitialCriterionsNotAssigned();
            for (Criterion criterion : initialCriterionsNotAssigned) {
                unassigned.put(criterion, createSatisfactionFor(criterion));
            }
            this.initialActive = calculateInitialActive();
        }

        public List<CriterionSatisfaction> getHistoric() {
            return history;
        }

        private List<CriterionSatisfaction> calculateInitialHistory() {
            Collection<CriterionSatisfaction> allSatisfactions = resource
                    .getSatisfactionsFor(type);
            ArrayList<CriterionSatisfaction> result = new ArrayList<CriterionSatisfaction>();
            for (CriterionSatisfaction criterionSatisfaction : allSatisfactions) {
                if (criterionSatisfaction.isFinished()) {
                    result.add(criterionSatisfaction);
                }
            }
            return result;
        }

        private HashSet<CriterionSatisfaction> calculateInitialActive() {
            return new HashSet<CriterionSatisfaction>(resource
                    .getCurrentSatisfactionsFor(type));
        }

        private List<Criterion> calculateInitialCriterionsNotAssigned() {
            Map<Long, Criterion> allCriterions = byId(criterionService
                    .getCriterionsFor(type));
            for (Long activeId : asIds(resource.getCurrentCriterionsFor(type))) {
                allCriterions.remove(activeId);
            }
            return new ArrayList<Criterion>(allCriterions.values());
        }

        public List<CriterionSatisfaction> getActiveSatisfactions() {
            Set<CriterionSatisfaction> result = new HashSet<CriterionSatisfaction>(
                    added);
            for (CriterionSatisfaction criterionSatisfaction : initialActive) {
                if (!unassigned.containsKey(criterionSatisfaction
                        .getCriterion())) {
                    result.add(criterionSatisfaction);
                }
            }
            return new ArrayList<CriterionSatisfaction>(result);
        }

        public List<Criterion> getCriterionsNotAssigned() {
            return new ArrayList<Criterion>(unassigned.keySet());
        }

        public void unassign(
                Collection<? extends CriterionSatisfaction> satisfactions) {
            for (CriterionSatisfaction criterionSatisfaction : satisfactions) {
                unassigned.put(criterionSatisfaction.getCriterion(),
                        criterionSatisfaction);
                added.remove(criterionSatisfaction);
            }
        }

        public void assign(Collection<? extends Criterion> criterions) {
            for (Criterion criterion : criterions) {
                CriterionSatisfaction removed = unassigned.remove(criterion);
                if (!initialActive.contains(removed)) {
                    added.add(removed);
                }
            }
        }

        private CriterionSatisfaction createSatisfactionFor(Criterion criterion) {
            return new CriterionSatisfaction(new Date(), criterion, resource);
        }

        @Override
        public void applyChanges() {
            for (CriterionSatisfaction criterionSatisfaction : added) {
                resource.addSatisfaction(new CriterionWithItsType(type,
                        criterionSatisfaction.getCriterion()), Interval
                        .from(criterionSatisfaction.getStartDate()));
            }
            for (Criterion criterion : unassigned.keySet()) {
                resource.finish(new CriterionWithItsType(type, criterion));
            }
        }
    }

    @Override
    public IMultipleCriterionActiveAssigner getLocalizationsAssigner() {
        return localizationsAssigner != null ? localizationsAssigner
                : new NullAssigner();
    }

    private static List<Long> asIds(Collection<? extends Criterion> criterions) {
        List<Long> result = new ArrayList<Long>();
        for (Criterion criterion : criterions) {
            result.add(criterion.getId());
        }
        return result;
    }

    private static Map<Long, Criterion> byId(
            Collection<? extends Criterion> criterions) {
        Map<Long, Criterion> result = new HashMap<Long, Criterion>();
        for (Criterion criterion : criterions) {
            result.put(criterion.getId(), criterion);
        }
        return result;
    }

    @Override
    public boolean isCreating() {
        return worker != null && worker.getId() == null;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<ICriterionType<?>, Collection<Criterion>> getLaboralRelatedCriterions() {
        Map<ICriterionType<?>, Collection<Criterion>> result = new HashMap<ICriterionType<?>, Collection<Criterion>>();
        for (ICriterionType<?> type : laboralRelatedTypes) {
            result.put(type, criterionService.getCriterionsFor(type));
        }
        return result;
    }

    @Override
    public List<CriterionSatisfaction>
        getLaboralRelatedCriterionSatisfactions() {
        return worker.query().oneOf(laboralRelatedTypes).sortByStartDate()
                .result();
    }
}