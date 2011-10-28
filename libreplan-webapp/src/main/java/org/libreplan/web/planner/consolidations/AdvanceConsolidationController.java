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

import static org.libreplan.web.I18nHelper._;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.web.common.Util;
import org.libreplan.web.planner.order.PlanningStateCreator.PlanningState;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.ganttz.TaskComponent;
import org.zkoss.ganttz.extensions.IContextWithPlannerTask;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.SuspendNotAllowedException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Window;

/**
 * Controller for {@link Advance} consolidation view.
 * @author Susana Montes Pedreira <smontes@wirelessgailicia.com>
 */
@org.springframework.stereotype.Component("advanceConsolidationController")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AdvanceConsolidationController extends GenericForwardComposer {

    private static final Log LOG = LogFactory
            .getLog(AdvanceConsolidationController.class);

    private IAdvanceConsolidationModel advanceConsolidationModel;

    private Grid advancesGrid;

    private Window window;

    private IContextWithPlannerTask<TaskElement> context;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        window = (Window) comp;
    }

    public void showWindow(IContextWithPlannerTask<TaskElement> context,
            org.libreplan.business.planner.entities.Task task,
            PlanningState planningState) {

        this.context = context;
        advanceConsolidationModel.initAdvancesFor(task, context, planningState);

        try {
            Util.reloadBindings(window);
            window.doModal();
        } catch (SuspendNotAllowedException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
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
            ((TaskComponent) context.getRelativeTo()).invalidate();
        }
        close();
    }

    private void close() {
        window.setVisible(false);
    }

    public String getInfoAdvance() {
        String infoAdvanceAssignment = advanceConsolidationModel
                .getInfoAdvanceAssignment();
        if (infoAdvanceAssignment.isEmpty()) {
            return _("Progress measurements");
        }

        return _("Progress measurements: ") + infoAdvanceAssignment;
    }

    public List<AdvanceConsolidationDTO> getAdvances() {
        return advanceConsolidationModel.getConsolidationDTOs();
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

    public String infoMessages() {
        return advanceConsolidationModel.infoMessages();
    }

    public String getReadOnlySclass() {
        if (advanceConsolidationModel.hasLimitingResourceAllocation()) {
            return "readonly";
        }
        return "";
    }

    public boolean isUnitType() {
        return advanceConsolidationModel.isUnitType();
    }

}
