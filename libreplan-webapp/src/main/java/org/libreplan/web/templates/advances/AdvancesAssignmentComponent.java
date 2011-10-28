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
package org.libreplan.web.templates.advances;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.libreplan.business.advance.entities.AdvanceAssignmentTemplate;
import org.libreplan.business.templates.entities.OrderElementTemplate;
import org.libreplan.web.templates.IOrderTemplatesModel;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class AdvancesAssignmentComponent extends HtmlMacroComponent {

    private OrderElementTemplate template;

    private ListitemRenderer advancesRenderer = new ListitemRenderer() {

        @Override
        public void render(Listitem item, Object data) {
            AdvanceAssignmentTemplate assignment = (AdvanceAssignmentTemplate) data;
            append(item, createTypeLabel(assignment));
            append(item, createMaxValueLabel(assignment));
            append(item, createReportGlobalCheckbox(assignment));
            append(item, createOperations(assignment));
        }

        private Hbox createOperations(AdvanceAssignmentTemplate assignment) {
            return new Hbox();
        }

        private <C extends Component> C append(Listitem item, C component) {
            Listcell cell = new Listcell();
            cell.appendChild(component);
            item.appendChild(cell);
            return component;
        }

        private Label createTypeLabel(AdvanceAssignmentTemplate assignment) {
            Label result = new Label();
            result.setValue(assignment.getAdvanceType().getUnitName());
            return result;
        }

        private Label createMaxValueLabel(AdvanceAssignmentTemplate assignment) {
            return new Label(assignment.getMaxValue().toPlainString());
        }

        private Checkbox createReportGlobalCheckbox(
                AdvanceAssignmentTemplate assignment) {
            Checkbox result = new Checkbox();
            result.setChecked(assignment.isReportGlobalAdvance());
            result.setDisabled(true);
            return result;
        }

    };

    public void useModel(IOrderTemplatesModel model) {
        useModel(model, model.getTemplate());
    }

    public void useModel(IOrderTemplatesModel model,
            OrderElementTemplate template) {
        this.template = template;
    }

    public List<AdvanceAssignmentTemplate> getAdvanceAssignments() {
        if (template == null) {
            return Collections.emptyList();
        }
        return new ArrayList<AdvanceAssignmentTemplate>(template
                .getAdvanceAssignmentTemplates());
    }

    public ListitemRenderer getAdvancesRenderer() {
        return advancesRenderer;
    }

}
