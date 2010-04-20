/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.planner.allocation;

import static org.navalplanner.business.i18n.I18nHelper._;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;
import org.navalplanner.business.orders.daos.IHoursGroupDAO;
import org.navalplanner.business.orders.entities.AggregatedHoursGroup;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.TaskSource;
import org.navalplanner.business.planner.daos.ITaskSourceDAO;
import org.navalplanner.business.planner.entities.Task;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.web.common.components.NewAllocationSelector.AllocationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides logical operations for limiting resource assignations in @{Task}
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LimitingResourceAllocationModel implements ILimitingResourceAllocationModel {

    @Autowired
    private IHoursGroupDAO hoursGroupDAO;

    @Autowired
    private ITaskSourceDAO taskSourceDAO;

    @Autowired
    private ICriterionDAO criterionDAO;

    private Task task;

    private List<LimitingResourceAllocationRow> resourceAllocations = new ArrayList<LimitingResourceAllocationRow>();

    @Override
    public void init(Task task) {
        this.task = task;
        this.resourceAllocations = new ArrayList<LimitingResourceAllocationRow>();
    }

    @Override
    public Integer getOrderHours() {
        if (task == null) {
            return 0;
        }
        return AggregatedHoursGroup.sum(task.getAggregatedByCriterions());
    }

    public class LimitingResourceAllocationRow {

        private static final int DEFAULT_PRIORITY = 5;

        private AllocationType type = AllocationType.GENERIC;

        private int hours = 0;

        private int priority = DEFAULT_PRIORITY;

        public LimitingResourceAllocationRow() {

        }

        public LimitingResourceAllocationRow(AllocationType type, int hours) {
            this(type, hours, DEFAULT_PRIORITY);
        }

        public LimitingResourceAllocationRow(AllocationType type, int hours, int priority) {
            this.type = type;
            this.hours = hours;
            this.priority = priority;
        }

        public String getAllocationType() {
            return type.toString();
        }

        public String getAllocation() {
            if (AllocationType.GENERIC.equals(type)) {
                return _("Criteria");
            }
            if (AllocationType.SPECIFIC.equals(type)) {
                return _("Resource");
            }
            return "";
        }

        public int getHours() {
            return hours;
        }

        public void setHours(int hours) {
            this.hours = hours;
        }

        public int getPriority() {
            return priority;
        }

        public String getPriorityStr() {
            return (new Integer(priority)).toString();
        }

        public void setPriorityStr(String priority) {
            this.priority = toNumber(priority);
        }

        private int toNumber(String str) {
            if (NumberUtils.isNumber(str)) {
                int result = NumberUtils.toInt(str);
                return (result >= 1 && result <= 10) ? result : 1;
            }
            return 1;
        }

    };

    private void addSpecificResourceAllocation(Resource resource) {
        LimitingResourceAllocationRow resourceAllocation = new LimitingResourceAllocationRow(
                AllocationType.SPECIFIC, getSumHoursGroups());
        resourceAllocations.add(resourceAllocation);
    }

    private int getSumHoursGroups() {
        return task.getTaskSource().getTotalHours();
    }

    private void addGenericResourceAllocation(Resource resource) {
        LimitingResourceAllocationRow resourceAllocation = new LimitingResourceAllocationRow(
                AllocationType.GENERIC, getSumHoursGroups());
        resourceAllocations.add(resourceAllocation);
    }

    @Override
    public void addGeneric(Set<Criterion> criterions,
            Collection<? extends Resource> resources) {
        if (resources.size() >= 1) {
            addGenericResourceAllocation(getFirstChild(resources));
        }
    }

    @Override
    public void addSpecific(Collection<? extends Resource> resources) {
        if (resources.size() >= 1) {
            addSpecificResourceAllocation(getFirstChild(resources));
        }
    }

    public Resource getFirstChild(Collection<? extends Resource> collection) {
        return collection.iterator().next();
    }

    @Override
    public List<LimitingResourceAllocationRow> getResourceAllocations() {
        return Collections.unmodifiableList(resourceAllocations);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AggregatedHoursGroup> getHoursAggregatedByCriteria() {
        reattachTaskSource();
        List<AggregatedHoursGroup> result = task.getTaskSource()
                .getAggregatedByCriterions();
        ensuringAccesedPropertiesAreLoaded(result);
        return result;
    }

    private void ensuringAccesedPropertiesAreLoaded(
            List<AggregatedHoursGroup> result) {
        for (AggregatedHoursGroup each : result) {
            each.getCriterionsJoinedByComma();
            each.getHours();
        }
    }

    /**
     * Re-attach {@link TaskSource}
     */
    private void reattachTaskSource() {
        TaskSource taskSource = task.getTaskSource();
        taskSourceDAO.reattach(taskSource);
        Set<HoursGroup> hoursGroups = taskSource.getHoursGroups();
        for (HoursGroup hoursGroup : hoursGroups) {
            reattachHoursGroup(hoursGroup);
        }
    }

    private void reattachHoursGroup(HoursGroup hoursGroup) {
        hoursGroupDAO.reattachUnmodifiedEntity(hoursGroup);
        hoursGroup.getPercentage();
        reattachCriteria(hoursGroup.getValidCriterions());
    }

    private void reattachCriteria(Set<Criterion> criterions) {
        for (Criterion criterion : criterions) {
            reattachCriterion(criterion);
        }
    }

    private void reattachCriterion(Criterion criterion) {
        criterionDAO.reattachUnmodifiedEntity(criterion);
        criterion.getName();
        reattachCriterionType(criterion.getType());
    }

    private void reattachCriterionType(CriterionType criterionType) {
        criterionType.getName();
    }

    @Override
    public void removeAllResourceAllocations() {
        resourceAllocations.clear();
    }

}
