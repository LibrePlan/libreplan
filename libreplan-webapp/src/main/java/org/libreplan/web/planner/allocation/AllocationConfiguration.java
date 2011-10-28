/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011 Igalia, S.L
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

package org.libreplan.web.planner.allocation;

import java.util.Arrays;

import org.apache.commons.lang.Validate;
import org.libreplan.business.planner.entities.CalculatedValue;
import org.libreplan.web.planner.allocation.ResourceAllocationController.CalculationTypeRadio;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

/**
 *
 * @author Diego Pino García <dpino@igalia.com>
 *
 */
public class AllocationConfiguration extends HtmlMacroComponent {

    private Label lbTaskStart;

    private Label lbTaskEnd;

    private Intbox taskWorkableDays;

    private Radiogroup calculationTypeSelector;

    private Grid calculationTypesGrid;

    private FormBinder formBinder;

    @Override
    public void afterCompose() {
        super.afterCompose();
        this.setVariable("allocationConfigurationController", this, true);

        lbTaskStart = (Label) getFellowIfAny("lbTaskStart");
        lbTaskEnd = (Label) getFellowIfAny("lbTaskEnd");
        taskWorkableDays = (Intbox) getFellowIfAny("taskWorkableDays");
    }

    private void initializeCalculationTypeSelector() {
        calculationTypeSelector = (Radiogroup) getFellowIfAny("calculationTypeSelector");
        calculationTypeSelector.addEventListener(Events.ON_CHECK, new EventListener() {

            @Override
            public void onEvent(Event event) {
                taskWorkableDays.clearErrorMessage(true);
                setCalculationTypeSelected(calculationTypeSelector
                        .getSelectedItem().getValue());
            }

            private void setCalculationTypeSelected(String calculationType) {
                Validate.notNull(formBinder);
                formBinder.setCalculatedValue(getCalculatedValue(calculationType));
            }

        });
    }

    private void initializeCalculationTypesGrid() {
        calculationTypesGrid = (Grid) getFellowIfAny("calculationTypesGrid");
        calculationTypesGrid.setModel(new ListModelList(Arrays
                .asList(CalculationTypeRadio.values())));
        calculationTypesGrid.setRowRenderer(getCalculationTypesRenderer());
    }

    private RowRenderer getCalculationTypesRenderer() {
        return new RowRenderer() {

            @Override
            public void render(Row row, Object data) throws Exception {
                CalculationTypeRadio type = (CalculationTypeRadio) data;

                Radio radio = type.createRadio();
                row.appendChild(radio);

                if (formBinder != null) {
                    if (type == CalculationTypeRadio.from(formBinder
                            .getCalculatedValue())) {
                        radio.setChecked(true);
                    }
                    radio.setDisabled(formBinder.isAnyManual());
                }
            }
        };
    }

    public Intbox getTaskWorkableDays() {
        return taskWorkableDays;
    }

    public Label getTaskStart() {
        return lbTaskStart;
    }

    public Label getTaskEnd() {
        return lbTaskEnd;
    }

    public Radiogroup getCalculationTypeSelector() {
        return calculationTypeSelector;
    }

    public void initialize(FormBinder formBinder) {
        this.formBinder = formBinder;
        initializeCalculationTypeSelector();
        initializeCalculationTypesGrid();
    }

    private CalculatedValue getCalculatedValue(String enumName) {
        return CalculationTypeRadio.valueOf(enumName).getCalculatedValue();
    }

}
