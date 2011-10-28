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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionSatisfaction;
import org.libreplan.business.resources.entities.CriterionWithItsType;
import org.libreplan.business.resources.entities.ICriterionType;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.Level;
import org.libreplan.web.common.Util;
import org.libreplan.web.resources.worker.IWorkerModel.AddingSatisfactionResult;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Listbox;

import static org.libreplan.web.I18nHelper._;

/**
 * Subcontroller for {@link Worker} resource <br />
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class WorkRelationshipsController extends GenericForwardComposer {

    private IWorkerModel workerModel;

    private WorkerCRUDController workerCRUDController;

    private CriterionSatisfaction satisfactionEdited = CriterionSatisfaction.create();

    private List<Criterion> workCriterions;

    private Listbox selectedWorkCriterion;

    private HashMap<Criterion, CriterionWithItsType> fromCriterionToType;

    private boolean editing;

    private Component containerComponent;

    private CriterionSatisfaction originalSatisfaction;

    private final IMessagesForUser messagesForUser;

    public WorkRelationshipsController(IWorkerModel workerModel,
            WorkerCRUDController workerCRUDController,
            IMessagesForUser messagesForUser) {
        this.workerModel = workerModel;
        this.workerCRUDController = workerCRUDController;
        this.messagesForUser = messagesForUser;
        this.workCriterions = new ArrayList<Criterion>();
        Map<ICriterionType<?>, Collection<Criterion>> map = workerModel
                .getLaboralRelatedCriterions();
        this.fromCriterionToType = new HashMap<Criterion, CriterionWithItsType>();
        for (Entry<ICriterionType<?>, Collection<Criterion>> entry : map
                .entrySet()) {
            this.workCriterions.addAll(entry.getValue());
            for (Criterion criterion : entry.getValue()) {
                this.fromCriterionToType.put(criterion,
                        new CriterionWithItsType(entry.getKey(), criterion));
            }
        }
    }

    public List<CriterionSatisfaction> getCriterionSatisfactions() {
        if (getWorker() == null) {
            return new ArrayList<CriterionSatisfaction>();
        } else {
            return workerModel
                    .getLaboralRelatedCriterionSatisfactions();
        }
    }

    public void deleteCriterionSatisfaction(CriterionSatisfaction satisfaction)
            throws InstanceNotFoundException {
        workerModel.removeSatisfaction(satisfaction);
        this.workerCRUDController.goToEditForm();
    }

    public void prepareForCreate() {
        this.satisfactionEdited = CriterionSatisfaction.create();
        this.originalSatisfaction = this.satisfactionEdited;
        Util.reloadBindings(containerComponent);
        editing = false;
    }

    public void prepareForEdit(CriterionSatisfaction criterionSatisfaction) {
        this.satisfactionEdited = criterionSatisfaction.copy();
        this.originalSatisfaction = criterionSatisfaction;
        Util.reloadBindings(containerComponent);
        this.satisfactionEdited.setCriterion(select(this.satisfactionEdited
                .getCriterion()));
        // the criterion retrieved is used instead of the original one, so the
        // call fromCriterionToType.get(criterion) works
        editing = true;
    }

    private Criterion select(Criterion criterion) {
        int i = 0;
        for (Criterion c : workCriterions) {
            if (c.isEquivalent(criterion)) {
                selectedWorkCriterion.setSelectedIndex(i);
                return c;
            }
            i++;
        }
        throw new RuntimeException(_("Couldn't find criterion {0}", criterion));
    }

    public void saveCriterionSatisfaction() {
        Criterion choosenCriterion = getChoosenCriterion();
        CriterionWithItsType criterionWithItsType = fromCriterionToType
                .get(choosenCriterion);
        satisfactionEdited.setCriterion(choosenCriterion);
        AddingSatisfactionResult addSatisfaction = workerModel.addSatisfaction(
                criterionWithItsType.getType(), originalSatisfaction,
                satisfactionEdited);
        switch (addSatisfaction) {
        case OK:
            messagesForUser.showMessage(Level.INFO, _("Time period saved"));
            this.workerCRUDController.goToEditForm();
            break;
        case SATISFACTION_WRONG:
            messagesForUser
                    .showMessage(Level.WARNING,
                            _("Time period contains non valid data. Ending data must be older than starting date"));
            break;
        case DONT_COMPLY_OVERLAPPING_RESTRICTIONS:
            messagesForUser
                    .showMessage(Level.WARNING,
                            _("Could not save time period. Time period overlaps with another non-compatible time period"));
            this.workerCRUDController.goToEditForm();
            break;
        default:
            throw new RuntimeException(_("Unexpected: {0}", addSatisfaction));
        }
    }

    private Criterion getChoosenCriterion() {
        Criterion criterion;
        if (editing) {
            criterion = satisfactionEdited.getCriterion();
        } else {
            criterion = (Criterion) this.selectedWorkCriterion
                    .getSelectedItemApi().getValue();
        }
        return criterion;
    }

    private Worker getWorker() {
        return this.workerModel.getWorker();
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        this.containerComponent = comp;
        this.selectedWorkCriterion.setSelectedIndex(0);
    }

    public CriterionSatisfaction getEditRelationship() {
        return this.satisfactionEdited;
    }

    public Collection<Criterion> getWorkCriterions() {
        return this.workCriterions;
    }

    public boolean isEditing() {
        return editing;
    }

}
