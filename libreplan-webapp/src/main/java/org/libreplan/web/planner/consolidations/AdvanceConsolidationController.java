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

package org.libreplan.web.planner.consolidations;

import static org.libreplan.web.I18nHelper._t;

import java.util.List;

import org.libreplan.business.planner.entities.Task;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.web.common.Util;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.ganttz.TaskComponent;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Window;

/**
 * Controller for {@link Advance} consolidation view.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgailicia.com>
 */
@org.springframework.stereotype.Component("advanceConsolidationController")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AdvanceConsolidationController extends GenericForwardComposer {

    private IAdvanceConsolidationModel advanceConsolidationModel;

    private Grid advancesGrid;

    private Window window;

    private IContextWithPlannerTask<TaskElement> context;

    public AdvanceConsolidationController() {
        if ( advanceConsolidationModel == null ) {
            advanceConsolidationModel = (IAdvanceConsolidationModel) SpringUtil.getBean("advanceConsolidationModel");
        }
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        window = (Window) comp;
    }

    public void showWindow(IContextWithPlannerTask<TaskElement> context, Task task, PlanningState planningState) {

        this.context = context;
        advanceConsolidationModel.initAdvancesFor(task, context, planningState);

        try {
            Util.reloadBindings(window);
            window.doModal();
        } catch (SuspendNotAllowedException e) {
            throw new RuntimeException(e);
        }
    }

    public void cancel() {
        advanceConsolidationModel.cancel();
        close();
    }

    public void accept() {
        advanceConsolidationModel.accept();

        if (context.getRelativeTo() instanceof TaskComponent) {
            ((TaskComponent) context.getRelativeTo()).updateProperties();
            context.getRelativeTo().invalidate();
        }

        close();
    }

    private void close() {
        window.setVisible(false);
    }

    public String getInfoAdvance() {
        String infoAdvanceAssignment = advanceConsolidationModel.getInfoAdvanceAssignment();

        return infoAdvanceAssignment.isEmpty()
                ? _t("Progress measurements")
                : _t("Progress measurements") + ": " + infoAdvanceAssignment;
    }

    public ListModel<AdvanceConsolidationDTO> getAdvances() {
        return new ListModelList<>(advanceConsolidationModel.getConsolidationDTOs());
    }

    /**
     * The rows used to be declared with a ZUML "each" template (self="@{each=...}") - under this
     * app's AnnotateBinder/ZK 10 stack that only ever clones the row's FIRST child for each
     * iteration. Building rows programmatically via RowRenderer sidesteps that "each" bug entirely
     * (same fix pattern as elsewhere in this sweep, e.g. WorkerCRUDController.getWorkersRenderer()).
     */
    public RowRenderer getAdvancesRenderer() {
        return (Row row, Object data, int i) -> {
            final AdvanceConsolidationDTO dto = (AdvanceConsolidationDTO) data;
            row.setValue(dto);

            Label units = new Label(dto.getLabelUnits());
            units.setVisible(isUnitType());
            row.appendChild(units);

            row.appendChild(new Label(dto.getLabelPercentage()));
            row.appendChild(new Label(Util.formatDate(dto.getDate())));

            Checkbox consolidated = new Checkbox();
            consolidated.setChecked(dto.isConsolidated());
            consolidated.setDisabled(Boolean.TRUE.equals(dto.isCanNotBeConsolidated()));
            consolidated.addEventListener(Events.ON_CHECK, event -> {
                dto.setConsolidated(consolidated.isChecked());
                reloadAdvanceGrid();
            });
            row.appendChild(consolidated);
        };
    }

    public void reloadAdvanceGrid() {
        advanceConsolidationModel.initLastConsolidatedDate();
        advanceConsolidationModel.setReadOnlyConsolidations();
        Util.reloadBindings(advancesGrid);
    }

    public boolean isVisibleAdvances() {
        return advanceConsolidationModel.isVisibleAdvances();
    }

    public boolean isVisibleMessages() {
        return advanceConsolidationModel.isVisibleMessages();
    }

    public String getInfoMessages() {
        return advanceConsolidationModel.infoMessages();
    }

    public String getReadOnlySclass() {
        return advanceConsolidationModel.hasLimitingResourceAllocation() ? "readonly" : "";
    }

    public boolean isUnitType() {
        return advanceConsolidationModel.isUnitType();
    }

}
