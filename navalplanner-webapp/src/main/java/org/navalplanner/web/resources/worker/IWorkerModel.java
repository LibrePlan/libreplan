/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.web.resources.worker;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.navalplanner.business.calendars.entities.BaseCalendar;
import org.navalplanner.business.calendars.entities.ResourceCalendar;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionSatisfaction;
import org.navalplanner.business.resources.entities.ICriterion;
import org.navalplanner.business.resources.entities.ICriterionType;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.common.IIntegrationEntityModel;
import org.navalplanner.web.resources.search.ResourcePredicate;

/**
 * This interface contains the operations to create/edit a worker. The
 * creation/edition process of a worker is conversational. <br/>
 *
 * <strong>Conversation state</strong>: the <code>Worker</code> instance and
 * the associated <code>CriterionSatisfaction</code> instances. Some of the
 * <code>CriterionSatisfaction</code> instances represent temporal work
 * relationships (e.g. paternity leave) and others represent locations. <br/>
 *
 * <strong>Non conversational steps</strong>: <code>getWorkers</code> (to return
 * all workers) and <code>getLaboralRelatedCriterions</code></li> (to return
 * all <code>Criterion</code> instances representing temporal work
 * relationships). <br/>
 *
 * <strong>Conversation protocol:</strong>
 * <ul>
 * <li>
 * Initial conversation step: <code>prepareForCreate</code> (to create
 * a worker) or (exclusive) <code>prepareEditFor</code> (to edit an existing
 * worker).
 * </li>
 * <li>
 * Intermediate conversation steps: <code>getWorker</code> (to return the
 * worker being edited/created), <code>getLocalizationsAssigner</code> (to
 * assist in the location tab), <code>isCreating</code> (to check if the worker
 * is being created or edited),
 * <code>getLaboralRelatedCriterionSatisfactions</code> (to return all the
 * temporal work relationships), <code>addSatisfaction</code> (to add a
 * temporal work relationship), <code>removeSatisfaction</code> (to remove a
 * temporal work relationship) (note: to modify an existing temporal work
 * relationship, it is necessary to remove it and add a new one),
 * <code>assignCriteria</code> (to add locations), and
 * <code>unassignSatisfactions</code> (to remove locations).
 * </li>
 * <li>
 * Final conversational step: <code>save</code> (to save the worker being
 * edited/created).
 * </li>
 * </ul>
 *
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public interface IWorkerModel extends IIntegrationEntityModel {

    void save() throws ValidationException;

    List<Worker> getWorkers();

    List<Worker> getRealWorkers();

    List<Worker> getVirtualWorkers();

    Worker getWorker();

    void prepareForCreate();

    void prepareForCreate(boolean virtual);

    void prepareEditFor(Worker worker);

    IMultipleCriterionActiveAssigner getLocalizationsAssigner();

    boolean isCreating();

    Map<ICriterionType<?>, Collection<Criterion>> getLaboralRelatedCriterions();

    List<CriterionSatisfaction> getLaboralRelatedCriterionSatisfactions();

    public enum AddingSatisfactionResult {
        OK, SATISFACTION_WRONG, DONT_COMPLY_OVERLAPPING_RESTRICTIONS;
    }

    AddingSatisfactionResult addSatisfaction(ICriterionType<?> type,
            CriterionSatisfaction originalSatisfaction,
            CriterionSatisfaction edited);

    void removeSatisfaction(CriterionSatisfaction satisfaction);

    public void assignCriteria(Collection<? extends Criterion> criteria);

    void unassignSatisfactions(
            Collection<? extends CriterionSatisfaction> satisfactions);

    void setWorker(Worker worker);

    Set<Resource> getSetOfResourcesSatisfying(ICriterion criterion);

    List<BaseCalendar> getBaseCalendars();

    void setCalendar(ResourceCalendar resourceCalendar);

    ResourceCalendar getCalendar();

    IAssignedCriterionsModel getAssignedCriterionsModel();

    BaseCalendar getDefaultCalendar();

    Integer getCapacity();

    void setCapacity(Integer capacity);

    public List<Worker> getFilteredWorker(ResourcePredicate predicate);

    public List<Worker> getAllCurrentWorkers();

    boolean canRemove(Worker worker);

    void confirmRemove(Worker worker) throws InstanceNotFoundException;

}