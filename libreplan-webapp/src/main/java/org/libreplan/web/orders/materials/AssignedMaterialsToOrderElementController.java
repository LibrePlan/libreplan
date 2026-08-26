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

package org.libreplan.web.orders.materials;

import static org.libreplan.web.I18nHelper._t;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

import org.libreplan.business.materials.entities.Material;
import org.libreplan.business.materials.entities.MaterialAssignment;
import org.libreplan.business.materials.entities.MaterialStatusEnum;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.web.common.EnumsListitemRenderer;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.TreeModel;

/**
 * Controller for showing {@link OrderElement} assigned {@link Material}.
 *
 * @author Diego Pino García <dpino@igalia.com>
 */
public class AssignedMaterialsToOrderElementController
        extends AssignedMaterialsController<OrderElement, MaterialAssignment> {

    private IAssignedMaterialsToOrderElementModel assignedMaterialsToOrderElementModel;

    public AssignedMaterialsToOrderElementController() {
        assignedMaterialsToOrderElementModel =
                (IAssignedMaterialsToOrderElementModel) SpringUtil.getBean("assignedMaterialsToOrderElementModel");
    }

    @Override
    protected IAssignedMaterialsModel<OrderElement, MaterialAssignment> getModel() {
        return assignedMaterialsToOrderElementModel;
    }

    @Override
    protected void createAssignmentsBoxComponent(Component parent) {
        Executions.createComponents("/orders/_assignmentsBox.zul", parent, new HashMap<String, String>());
    }

    @Override
    protected void initializeEdition(OrderElement orderElement) {
        assignedMaterialsToOrderElementModel.initEdit(orderElement);
    }

    @Override
    public TreeModel getMaterialCategories() {
        return assignedMaterialsToOrderElementModel.getMaterialCategories();
    }

    @Override
    public TreeModel getAllMaterialCategories() {
        return assignedMaterialsToOrderElementModel.getAllMaterialCategories();
    }

    @Override
    public BigDecimal getTotalUnits() {
        BigDecimal result = BigDecimal.ZERO;

        final OrderElement orderElement = getOrderElement();
        if (orderElement != null) {
            result = result.add(orderElement.getTotalMaterialAssignmentUnits());
        }
        return result;
    }

    public BigDecimal getTotalPrice() {
        BigDecimal result = new BigDecimal(0);

        final OrderElement orderElement = getOrderElement();
        if (orderElement != null) {
            result = orderElement.getTotalMaterialAssignmentPrice();
        }
        return result.setScale(2, RoundingMode.HALF_UP);
    }

    private OrderElement getOrderElement() {
        return assignedMaterialsToOrderElementModel.getOrderElement();
    }

    @Override
    protected MaterialAssignment copyFrom(MaterialAssignment assignment) {
        return MaterialAssignment.create(assignment);
    }

    @Override
    protected Material getMaterial(MaterialAssignment materialAssignment) {
        return materialAssignment.getMaterial();
    }

    @Override
    protected Double getTotalPrice(MaterialAssignment materialAssignment) {
        return materialAssignment.getTotalPrice().doubleValue();
    }

    @Override
    protected BigDecimal getUnits(MaterialAssignment assignment) {
        return assignment.getUnits();
    }

    protected void setUnits(MaterialAssignment assignment, BigDecimal units) {
        assignment.setUnits(units);
    }

    /**
     * Companion to TemplateMaterialsController.getMaterialAssignmentsRenderer() - this row layout
     * has extra columns (Name, Reception date, Status) that the template variant doesn't have, so
     * it needs its own renderer rather than sharing one on the base class.
     */
    public RowRenderer getMaterialAssignmentsRenderer() {
        return (Row row, Object data, int i) -> {
            final MaterialAssignment assignment = (MaterialAssignment) data;
            row.setValue(assignment);
            row.setTooltiptext(assignment.getMaterial().getCategory().getName());

            row.appendChild(new Label(assignment.getMaterial().getCode()));

            row.appendChild(new Label(assignment.getMaterial().getDescription()));

            Datebox estimatedAvailability = new Datebox(assignment.getEstimatedAvailability());
            // ZK's default Datebox width is a fixed, narrow size that doesn't grow with the
            // "Reception date" column (14% of the grid) - fill the column instead.
            estimatedAvailability.setWidth("100%");
            estimatedAvailability.addEventListener(Events.ON_CHANGE,
                    event -> assignment.setEstimatedAvailability(estimatedAvailability.getValue()));
            row.appendChild(estimatedAvailability);

            Doublebox units = new Doublebox(assignment.getUnits().doubleValue());
            units.setStyle("text-align:right");
            units.addEventListener(Events.ON_CHANGE, event -> {
                assignment.setUnits(BigDecimal.valueOf(units.getValue()));
                updateTotalPrice(row);
            });
            row.appendChild(units);

            row.appendChild(new Label(assignment.getMaterial().getUnitType().getMeasure()));

            Decimalbox unitPrice = new Decimalbox(assignment.getUnitPrice());
            unitPrice.setStyle("text-align:right");
            unitPrice.setFormat(getMoneyFormat());
            unitPrice.addEventListener(Events.ON_CHANGE, event -> {
                assignment.setUnitPrice(unitPrice.getValue());
                updateTotalPrice(row);
            });
            row.appendChild(unitPrice);

            Decimalbox totalPrice = new Decimalbox(assignment.getTotalPrice());
            totalPrice.setStyle("text-align:right");
            totalPrice.setDisabled(true);
            totalPrice.setFormat(getMoneyFormat());
            row.appendChild(totalPrice);

            Listbox status = new Listbox();
            status.setMold("select");
            status.setModel(new SimpleListModel<>(MaterialStatusEnum.values()));
            status.setItemRenderer((item, itemData, itemIndex) -> {
                new EnumsListitemRenderer().render(item, itemData, itemIndex);
                item.setSelected(itemData == assignment.getStatus());
            });
            status.addEventListener(Events.ON_SELECT,
                    event -> assignment.setStatus((MaterialStatusEnum) status.getSelectedItem().getValue()));
            row.appendChild(status);

            Hbox operations = new Hbox();

            Button delete = new Button();
            delete.setSclass("icono");
            delete.setImage("/common/img/ico_borrar1.png");
            delete.setHoverImage("/common/img/ico_borrar.png");
            delete.setTooltiptext(_t("Delete"));
            delete.addEventListener(Events.ON_CLICK, event -> showRemoveMaterialAssignmentDlg(assignment));
            operations.appendChild(delete);

            Button split = new Button();
            split.setLabel(_t("Split"));
            split.setSclass("add-button");
            split.setTooltiptext(_t("Split assignment"));
            split.addEventListener(Events.ON_CLICK, event -> showSplitMaterialAssignmentDlg(assignment));
            operations.appendChild(split);

            row.appendChild(operations);
        };
    }

}
