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
package org.libreplan.web.templates.materials;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

import org.libreplan.business.materials.entities.Material;
import org.libreplan.business.materials.entities.MaterialAssignmentTemplate;
import org.libreplan.business.templates.entities.OrderElementTemplate;
import org.libreplan.web.orders.materials.AssignedMaterialsController;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Button;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.TreeModel;

import static org.libreplan.web.I18nHelper._t;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class TemplateMaterialsController extends
        AssignedMaterialsController<OrderElementTemplate, MaterialAssignmentTemplate> {

    private IAssignedMaterialsToOrderElementTemplateModel assignedMaterialsToOrderElementTemplateModel;

    public TemplateMaterialsController(){
        assignedMaterialsToOrderElementTemplateModel =
                (IAssignedMaterialsToOrderElementTemplateModel) SpringUtil
                        .getBean("assignedMaterialsToOrderElementTemplateModel");
    }

    @Override
    protected MaterialAssignmentTemplate copyFrom(MaterialAssignmentTemplate assignment) {
        return MaterialAssignmentTemplate.copyFrom(assignment);
    }

    @Override
    protected void createAssignmentsBoxComponent(Component parent) {
        Executions.createComponents("/templates/_materialAssignmentsBox.zul", parent, new HashMap<String, String>());
    }

    /**
     * The grid's rows used to be declared with a ZUML "each" template (self="@{each=...}") -
     * under this app's AnnotateBinder/ZK 10 stack that only ever clones the row's FIRST child for
     * each iteration. Building rows programmatically via RowRenderer sidesteps that "each" bug
     * entirely (same fix pattern as elsewhere in this sweep, e.g.
     * WorkerCRUDController.getWorkersRenderer()). The nested unit-type listbox's per-row selection
     * logic (getRenderer()/UnitTypeListRenderer, on the base class) was already correct - it
     * decides the selected item during rendering rather than post-hoc, so it's reused as-is here.
     */
    public RowRenderer getMaterialAssignmentsRenderer() {
        return (Row row, Object data, int i) -> {
            final MaterialAssignmentTemplate assignment = (MaterialAssignmentTemplate) data;
            row.setValue(assignment);

            Textbox code = new Textbox(assignment.getMaterial().getCode());
            code.setConstraint("no empty:" + _t("cannot be empty"));
            code.setReadonly(true);
            row.appendChild(code);

            Doublebox units = new Doublebox(getUnits(assignment).doubleValue());
            units.addEventListener(Events.ON_CHANGE, event -> {
                setUnits(assignment, BigDecimal.valueOf(units.getValue()));
                updateTotalPrice(row);
            });
            row.appendChild(units);

            Listbox unitType = new Listbox();
            unitType.setMold("select");
            unitType.setModel(new org.zkoss.zul.ListModelList<>(getUnitTypes()));
            unitType.setItemRenderer(getRenderer());
            unitType.addEventListener(Events.ON_SELECT, event -> selectUnitType(unitType));
            unitType.setDisabled(true);
            row.appendChild(unitType);

            Decimalbox unitPrice = new Decimalbox(assignment.getUnitPrice());
            unitPrice.setFormat(getMoneyFormat());
            unitPrice.addEventListener(Events.ON_CHANGE, event -> {
                assignment.setUnitPrice(unitPrice.getValue());
                updateTotalPrice(row);
            });
            row.appendChild(unitPrice);

            Decimalbox totalPrice = new Decimalbox(assignment.getTotalPrice());
            totalPrice.setDisabled(true);
            totalPrice.setFormat(getMoneyFormat());
            row.appendChild(totalPrice);

            row.appendChild(new Label(assignment.getMaterial().getCategory().getName()));

            Hbox hbox = new Hbox();
            Button delete = new Button();
            delete.setSclass("icono");
            delete.setImage("/common/img/ico_borrar1.png");
            delete.setHoverImage("/common/img/ico_borrar.png");
            delete.setTooltiptext(_t("Delete"));
            delete.addEventListener(Events.ON_CLICK, event -> showRemoveMaterialAssignmentDlg(assignment));
            hbox.appendChild(delete);

            Button split = new Button();
            split.setLabel(_t("Split"));
            split.setSclass("add-button");
            split.setTooltiptext(_t("Split assignment"));
            split.addEventListener(Events.ON_CLICK, event -> showSplitMaterialAssignmentDlg(assignment));
            hbox.appendChild(split);

            row.appendChild(hbox);
        };
    }

    @Override
    public TreeModel getAllMaterialCategories() {
        return assignedMaterialsToOrderElementTemplateModel.getAllMaterialCategories();
    }

    @Override
    protected Material getMaterial(MaterialAssignmentTemplate materialAssignment) {
        return materialAssignment.getMaterial();
    }

    @Override
    public TreeModel getMaterialCategories() {
        return assignedMaterialsToOrderElementTemplateModel.getMaterialCategories();
    }

    @Override
    protected IAssignedMaterialsToOrderElementTemplateModel getModel() {
        return assignedMaterialsToOrderElementTemplateModel;
    }

    @Override
    public BigDecimal getTotalPrice() {
        OrderElementTemplate template = assignedMaterialsToOrderElementTemplateModel.getTemplate();

        return template == null
                ? BigDecimal.ZERO
                : template.getTotalMaterialAssignmentPrice().setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    protected Double getTotalPrice(MaterialAssignmentTemplate materialAssignment) {
        return materialAssignment.getTotalPrice().doubleValue();
    }

    @Override
    public BigDecimal getTotalUnits() {
        OrderElementTemplate template = assignedMaterialsToOrderElementTemplateModel.getTemplate();
        return template == null ? BigDecimal.ZERO : template.getTotalMaterialAssignmentUnits();
    }

    @Override
    protected BigDecimal getUnits(MaterialAssignmentTemplate assignment) {
        return assignment.getUnits() == null ? BigDecimal.ZERO : assignment.getUnits();
    }

    @Override
    protected void initializeEdition(OrderElementTemplate template) {
        assignedMaterialsToOrderElementTemplateModel.initEdit(template);
    }

    @Override
    protected void setUnits(MaterialAssignmentTemplate assignment, BigDecimal units) {
        assignment.setUnits(units);
    }

}
