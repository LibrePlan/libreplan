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

package org.navalplanner.web.resources.criterion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.resources.worker.IWorkerCRUDControllerEntryPoints;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.api.Button;
import org.zkoss.zul.api.Listbox;
import org.zkoss.zul.api.Listitem;
import org.zkoss.zul.api.Window;

public class CriterionWorkersController extends GenericForwardComposer {

    private final ICriterionsModel criterionsModel;

    private Window workersWindow;

    private Listbox list;

    private Button saveListButton;

    private Button cancelListButton;

    private IWorkerCRUDControllerEntryPoints workerCRUD;

    public void showList(Event event) {
        loadDataToList();
        try {
            workersWindow.doModal();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Worker> getAllWorkers() {
        return criterionsModel.getAllWorkers();
    }

    public CriterionWorkersController(ICriterionsModel criterionsModel) {
        this.criterionsModel = criterionsModel;
    }

    public List<Worker> getWorkersForCurrentCriterion() {
        return criterionsModel
                .getResourcesSatisfyingCurrentCriterionOfType(Worker.class);
    }

    public boolean isChangeAssignmentsDisabled() {
        return criterionsModel.isChangeAssignmentsDisabled();
    }

    public void goToEditPage(Resource resource) {
        workerCRUD.goToEditForm((Worker) resource);
    }

    @Override
    public void doAfterCompose(final Component comp) throws Exception {
        super.doAfterCompose(comp);
        list = (Listbox) workersWindow.getFellow("list");
        loadDataToList();
        saveListButton = (Button) workersWindow.getFellow("saveList");
        cancelListButton = (Button) workersWindow.getFellow("cancelList");
        saveListButton.addEventListener("onClick", new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                Collection<? extends Listitem> items = (Collection<? extends Listitem>) list
                        .getItems();
                List<Worker> selectedWorkers = new ArrayList<Worker>();
                List<Worker> unSelectedWorkers = new ArrayList<Worker>();
                for (Listitem listitem : items) {
                    if (listitem.isSelected()) {
                        selectedWorkers.add((Worker) listitem.getValue());
                    } else {
                        unSelectedWorkers.add((Worker) listitem.getValue());
                    }
                }
                criterionsModel.activateAll(selectedWorkers);
                criterionsModel.deactivateAll(unSelectedWorkers);
                workersWindow.setVisible(false);
                Util.reloadBindings(comp);
            }
        });
        cancelListButton.addEventListener("onClick", new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                workersWindow.setVisible(false);
            }
        });
    }

    private void loadDataToList() {
        List<Worker> allWorkers = getAllWorkers();
        final HashSet<Long> workersForCurrentCriterionIds = new HashSet<Long>(
                asIds(getWorkersForCurrentCriterion()));
        list.setModel(new ListModelList(allWorkers));
        list.setItemRenderer(new ListitemRenderer() {

            @Override
            public void render(org.zkoss.zul.Listitem item, Object data)
                    throws Exception {
                Resource r = (Resource) data;
                item.setValue(data);
                item.setSelected(workersForCurrentCriterionIds.contains(r
                        .getId()));
                Listcell cell = new Listcell();
                cell.setParent(item);
                Worker worker = (Worker) data;
                cell.setLabel(worker.getSurname() + ", "
                        + worker.getFirstName());
            }
        });
    }

    private static Set<Long> asIds(Collection<? extends Resource> resources) {
        HashSet<Long> result = new HashSet<Long>();
        for (Resource resource : resources) {
            result.add(resource.getId());
        }
        return result;
    }

}
