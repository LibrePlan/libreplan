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

package org.libreplan.web.resources.worker;

import static org.libreplan.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.libreplan.business.calendars.daos.IBaseCalendarDAO;
import org.libreplan.business.calendars.entities.BaseCalendar;
import org.libreplan.business.calendars.entities.CalendarData;
import org.libreplan.business.calendars.entities.ResourceCalendar;
import org.libreplan.business.common.IntegrationEntity;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.entities.Configuration;
import org.libreplan.business.common.entities.EntityNameEnum;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.planner.daos.IDayAssignmentDAO;
import org.libreplan.business.planner.daos.IResourceAllocationDAO;
import org.libreplan.business.resources.daos.ICriterionDAO;
import org.libreplan.business.resources.daos.IResourceDAO;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionSatisfaction;
import org.libreplan.business.resources.entities.CriterionWithItsType;
import org.libreplan.business.resources.entities.ICriterionType;
import org.libreplan.business.resources.entities.Interval;
import org.libreplan.business.resources.entities.PredefinedCriterionTypes;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.VirtualWorker;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.scenarios.IScenarioManager;
import org.libreplan.business.workreports.daos.IWorkReportLineDAO;
import org.libreplan.web.calendars.IBaseCalendarModel;
import org.libreplan.web.common.IntegrationEntityModel;
import org.libreplan.web.common.concurrentdetection.OnConcurrentModification;
import org.libreplan.web.resources.search.ResourcePredicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for worker <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Diego Pino García <dpino@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@OnConcurrentModification(goToPage = "/resources/worker/worker.zul")
public class WorkerModel extends IntegrationEntityModel implements IWorkerModel {

    private static final org.apache.commons.logging.Log LOG = LogFactory
            .getLog(WorkerModel.class);

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private IBaseCalendarDAO baseCalendarDAO;

    private final ICriterionType<?>[] laboralRelatedTypes = {
            PredefinedCriterionTypes.LOCATION,
            PredefinedCriterionTypes.CATEGORY, PredefinedCriterionTypes.SKILL };

    private Worker worker;

    private ResourceCalendar calendarToRemove = null;

    private final ICriterionDAO criterionDAO;

    private IMultipleCriterionActiveAssigner localizationsAssigner;

    @Autowired
    @Qualifier("subclass")
    private IBaseCalendarModel baseCalendarModel;

    @Autowired
    private IAssignedCriterionsModel assignedCriterionsModel;

    @Autowired
    private IConfigurationDAO configurationDAO;

    private List<Worker> currentWorkerList = new ArrayList<Worker>();

    @Autowired
    private IDayAssignmentDAO dayAssignmentDAO;

    @Autowired
    private IWorkReportLineDAO workReportLineDAO;

    @Autowired
    private IResourceAllocationDAO resourceAllocationDAO;

    @Autowired
    private IScenarioManager scenarioManager;

    @Autowired
    public WorkerModel(IResourceDAO resourceDAO, ICriterionDAO criterionDAO) {
        Validate.notNull(resourceDAO);
        Validate.notNull(criterionDAO);
        this.resourceDAO = resourceDAO;
        this.criterionDAO = criterionDAO;
    }

    @Override
    @Transactional
    public void save() throws ValidationException {
        removeCalendarIfNeeded();
        resourceDAO.save(worker);
        if (worker.getCalendar() != null) {
            baseCalendarModel.checkInvalidValuesCalendar(worker.getCalendar());
        }
        getLocalizationsAssigner().applyChanges();
        if(assignedCriterionsModel != null){
            assignedCriterionsModel.confirm();
        }
        localizationsAssigner = null;
    }

    private void removeCalendarIfNeeded() {
        if (calendarToRemove != null) {
            try {
                resourceDAO.reattach(worker);
                baseCalendarDAO.remove(calendarToRemove.getId());
                calendarToRemove = null;
            } catch (InstanceNotFoundException e) {
                LOG.error("Couldn't remove calendar");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Worker> getWorkers() {
        return resourceDAO.getWorkers();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Worker> getRealWorkers() {
        currentWorkerList = resourceDAO.getRealWorkers();
        return currentWorkerList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Worker> getVirtualWorkers() {
        List<Worker> list = resourceDAO.getVirtualWorkers();
        for (Worker each : list) {
            each.getCalendar().getCapacity();
        }
        currentWorkerList = list;
        return currentWorkerList;
    }

    @Override
    public Worker getWorker() {
        return worker;
    }

    @Override
    @Transactional(readOnly = true)
    public void prepareForCreate() {
        prepareForCreate(false);
    }

    @Override
    @Transactional(readOnly = true)
    public void prepareForCreate(boolean virtual) {

        if (virtual) {
            worker = VirtualWorker.create("");
            setCapacity(1);
        } else {
            worker = Worker.create("");
        }

        worker.setCodeAutogenerated(configurationDAO.getConfiguration()
                .getGenerateCodeForResources());
        if (worker.isCodeAutogenerated()) {
            setDefaultCode();
        }
        localizationsAssigner = new MultipleCriterionActiveAssigner(
                criterionDAO, worker, PredefinedCriterionTypes.LOCATION);
    }

    @Override
    @Transactional(readOnly = true)
    public void prepareEditFor(Worker worker) {
        Validate.notNull(worker, _("Worker must be not-null"));
        try {
            this.worker = (Worker) resourceDAO.find(worker.getId());
            forceLoadSatisfactions(this.worker);
            forceLoadCalendar(this.worker);
            localizationsAssigner = new MultipleCriterionActiveAssigner(
                    criterionDAO, this.worker,
                    PredefinedCriterionTypes.LOCATION);
            initOldCodes();
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void forceLoadSatisfactions(Resource resource) {
        for (CriterionSatisfaction criterionSatisfaction : resource
                .getAllSatisfactions()) {
            criterionSatisfaction.getCriterion().getName();
            criterionSatisfaction.getCriterion().getType().getName();
        }
    }

    private void forceLoadCalendar(Worker worker) {
        if (worker.getCalendar() != null) {
            forceLoadCalendar(worker.getCalendar());
        }
    }

    private void forceLoadCalendar(BaseCalendar baseCalendar) {
        for (CalendarData calendarData : baseCalendar.getCalendarDataVersions()) {
            calendarData.getHoursPerDay().size();
            if (calendarData.getParent() != null) {
                forceLoadCalendar(calendarData.getParent());
            }
        }
        baseCalendar.getExceptions().size();
    }

    @Override
    @Transactional(readOnly = true)
    public void assignCriteria(Collection<? extends Criterion> criteria) {

        resourceDAO.checkVersion(getWorker());

        /* Assign criteria. */
        getLocalizationsAssigner().assign(criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public void unassignSatisfactions(
            Collection<? extends CriterionSatisfaction> satisfactions) {

        resourceDAO.checkVersion(getWorker());

        /* Unassign criterion satisfactions. */
        getLocalizationsAssigner().unassign(satisfactions);

    }

    @Override
    @Transactional(readOnly = true)
    public AddingSatisfactionResult addSatisfaction(ICriterionType<?> type,
            CriterionSatisfaction original, CriterionSatisfaction edited) {

        /* Check worker's version. */
        Worker worker = getWorker();
        resourceDAO.checkVersion(worker);

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

     @Transactional(readOnly = true)
    public void removeSatisfaction(CriterionSatisfaction satisfaction) {

        /* Check worker's version. */
        resourceDAO.checkVersion(worker);

        /* Remove criterion satisfaction. */
        worker.removeCriterionSatisfaction(satisfaction);

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
        private final ICriterionDAO criterionDAO;
        private List<CriterionSatisfaction> history;
        private List<Criterion> initialCriterionsNotAssigned;
        private Set<CriterionSatisfaction> initialActive;

        private Map<Criterion, CriterionSatisfaction> unassigned = new HashMap<Criterion, CriterionSatisfaction>();

        private Set<CriterionSatisfaction> added = new HashSet<CriterionSatisfaction>();

        public MultipleCriterionActiveAssigner(
                ICriterionDAO criterionDAO,
                Resource resource,
                ICriterionType<?> type) {
            Validate
                    .isTrue(
                            type.isAllowSimultaneousCriterionsPerResource(),
                            _("You must allow multiple active criteria for this type to use this assignment strategy"));
            this.criterionDAO = criterionDAO;
            this.resource = resource;
            this.type = type;
            forceLoadSatisfactions(this.resource);
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
            Map<Long, Criterion> allCriterions = byId(criterionDAO
                    .findByType(type));
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
            return CriterionSatisfaction.create(new LocalDate(), criterion,
                    resource);
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
            result.put(type, criterionDAO.findByType(type));
        }
        return result;
    }

    @Override
    public List<CriterionSatisfaction> getLaboralRelatedCriterionSatisfactions() {
        return worker.query().oneOf(laboralRelatedTypes).result();
    }

    @Override
    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BaseCalendar> getBaseCalendars() {
        return baseCalendarDAO.getBaseCalendars();
    }

    @Override
    public void setCalendar(ResourceCalendar resourceCalendar) {
        if (worker != null) {
            worker.setCalendar(resourceCalendar);
        }
    }

    @Override
    public ResourceCalendar getCalendar() {
        if (worker != null) {
            return worker.getCalendar();
        }
        return null;
    }

    @Override
    public Integer getCapacity() {
        if (getCalendar() != null) {
            return getCalendar().getCapacity();
        }
        return null;
    }

    @Override
    public void setCapacity(Integer capacity) {
        if (getCalendar() != null) {
            getCalendar().setCapacity(capacity);
        }
    }

    public IAssignedCriterionsModel getAssignedCriterionsModel() {
        return assignedCriterionsModel;
    }

    @Override
    @Transactional(readOnly = true)
    public BaseCalendar getDefaultCalendar() {
        Configuration configuration = configurationDAO.getConfiguration();
        if (configuration == null) {
            return null;
        }
        BaseCalendar defaultCalendar = configuration.getDefaultCalendar();
        forceLoadCalendar(defaultCalendar);
        return defaultCalendar;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Worker> getFilteredWorker(ResourcePredicate predicate) {
        List<Worker> filteredResourceList = new ArrayList<Worker>();
        for (Worker worker : currentWorkerList) {
            resourceDAO.reattach(worker);
            if (predicate.accepts(worker)) {
                filteredResourceList.add(worker);
            }
        }
        return filteredResourceList;
    }

    public List<Worker> getAllCurrentWorkers() {
        return currentWorkerList;
    }

    @Override
    @Transactional(readOnly=true)
    public boolean canRemove(Worker worker) {
        List<Resource> resourcesList = new ArrayList<Resource>();
        resourcesList.add(worker);
        return dayAssignmentDAO.findByResources(resourcesList).isEmpty()
                && workReportLineDAO.findByResources(resourcesList).isEmpty()
                && resourceAllocationDAO.findAllocationsRelatedToAnyOf(
                        scenarioManager.getCurrent(), resourcesList).isEmpty();
    }

    @Override
    @Transactional
    public void confirmRemove(Worker worker) throws InstanceNotFoundException {
        resourceDAO.remove(worker.getId());
    }

    public EntityNameEnum getEntityName() {
        return EntityNameEnum.WORKER;
    }

    public Set<IntegrationEntity> getChildren() {
        return new HashSet<IntegrationEntity>();
    }

    public IntegrationEntity getCurrentEntity() {
        return this.worker;
    }

    @Override
    public void removeCalendar() {
        calendarToRemove = worker.getCalendar();
        worker.setCalendar(null);
    }

}
