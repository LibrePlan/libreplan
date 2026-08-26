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
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.libreplan.business.resources.entities.Criterion;
import org.libreplan.business.resources.entities.CriterionSatisfaction;
import org.libreplan.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

/**
 * Subcontroller for assigning localizations.
 * <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public class LocalizationsController extends GenericForwardComposer {

    private IWorkerModel workerModel;

    private Listbox activeSatisfactions;

    private Listbox criterionsNotAssigned;

    private Button unassignButton;

    private Button assignButton;

    LocalizationsController(IWorkerModel workerModel) {
        Validate.notNull(workerModel);
        this.workerModel = workerModel;
    }

    public List<CriterionSatisfaction> getLocalizationsHistory() {
        return workerModel.getLocalizationsAssigner().getHistoric();
    }

    public List<CriterionSatisfaction> getActiveSatisfactions() {
        return workerModel.getLocalizationsAssigner().getActiveSatisfactions();
    }

    public ListitemRenderer getActiveSatisfactionsRenderer() {
        return (Listitem item, Object data, int index) -> {
            final CriterionSatisfaction satisfaction = (CriterionSatisfaction) data;
            item.setValue(satisfaction);
            item.appendChild(new Listcell(satisfaction.getCriterion().getName()));
            item.appendChild(new Listcell(Util.formatDate(satisfaction.getStartDate())));
        };
    }

    public List<Criterion> getCriterionsNotAssigned() {
        return workerModel.getLocalizationsAssigner().getCriterionsNotAssigned();
    }

    public ListitemRenderer getCriterionsNotAssignedRenderer() {
        return (Listitem item, Object data, int index) -> {
            final Criterion criterion = (Criterion) data;
            item.setValue(criterion);
            item.appendChild(new Listcell(criterion.getName()));
        };
    }

    public RowRenderer getLocalizationsHistoryRenderer() {
        return (Row row, Object data, int index) -> {
            final CriterionSatisfaction satisfaction = (CriterionSatisfaction) data;
            row.setValue(satisfaction);
            row.appendChild(new Label(satisfaction.getCriterion().getName()));
            row.appendChild(new Label(Util.formatDate(satisfaction.getStartDate())));
            row.appendChild(new Label(satisfaction.getEndDate() != null ? Util.formatDate(satisfaction.getEndDate()) : ""));
        };
    }

    private void reloadLists() {
        Util.reloadBindings(activeSatisfactions, criterionsNotAssigned);
    }

    private static <T> List<T> extractValuesOf(Collection<? extends Listitem> items, Class<T> klass) {
        ArrayList<T> result = new ArrayList<>();
        for (Listitem listitem : items) {
            result.add(klass.cast(listitem.getValue()));
        }

        return result;
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        unassignButton.addEventListener("onClick", new EventListener() {

            @Override
            public void onEvent(Event event) {
                workerModel.unassignSatisfactions(
                        extractValuesOf(activeSatisfactions.getSelectedItems(), CriterionSatisfaction.class));

                reloadLists();
            }

        });

        assignButton.addEventListener("onClick", new EventListener() {

            @Override
            public void onEvent(Event event) {
                Set<Listitem> selectedItems = criterionsNotAssigned.getSelectedItems();
                workerModel.assignCriteria(extractValuesOf(selectedItems, Criterion.class));
                reloadLists();
            }
        });
    }

}