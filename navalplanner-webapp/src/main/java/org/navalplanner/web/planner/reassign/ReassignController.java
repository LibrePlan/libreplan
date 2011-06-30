/*
 * This file is part of NavalPlan
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
package org.navalplanner.web.planner.reassign;

import static org.navalplanner.business.i18n.I18nHelper._;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.joda.time.LocalDate;
import org.navalplanner.web.planner.reassign.ReassignCommand.IConfigurationResult;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.ganttz.timetracker.ICellForDetailItemRenderer;
import org.zkoss.ganttz.timetracker.OnColumnsRowRenderer;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Window;
/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReassignController extends GenericForwardComposer {

    public static void openOn(org.zkoss.zk.ui.Component component,
            IConfigurationResult configurationResult) {
        Window result = (Window) Executions.createComponents(
                "/planner/reassign.zul", component, Collections.emptyMap());
        ReassignController controller = (ReassignController) result
                .getAttribute("controller");
        controller.showWindow(configurationResult);
    }

    private Window window;

    private Radiogroup reassigningTypeSelector;

    private Grid reassigningTypesGrid;

    private Datebox associatedDate;

    private Type currentType = Type.ALL;

    private IConfigurationResult configurationResult;

    private void showWindow(IConfigurationResult configurationResult) {
        this.configurationResult = configurationResult;
        try {
            window.setMode("modal");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        super.doAfterCompose(comp);
        this.window = (Window) comp;
        comp.setAttribute("controller", this);
        associatedDate.setVisible(currentType.needsAssociatedDate());
        fillGridWithRadios();
        listenForTypeChange();
        reassigningTypesGrid.invalidate();
    }


    private void fillGridWithRadios() {
        reassigningTypesGrid.setModel(new SimpleListModel(Type
                .values()));
        reassigningTypesGrid.setRowRenderer(OnColumnsRowRenderer.create(
                reassigningTypesRenderer(), Arrays.asList(0)));
    }

    private ICellForDetailItemRenderer<Integer, Type> reassigningTypesRenderer() {
        return new ICellForDetailItemRenderer<Integer, Type>() {
            @Override
            public org.zkoss.zk.ui.Component cellFor(Integer column, Type type) {
                Radio radio = type.createRadio();
                radio.setChecked(currentType == type);
                return radio;
            }
        };
    }

    private void listenForTypeChange() {
        reassigningTypeSelector.addEventListener(Events.ON_CHECK,
                new EventListener() {

                    @Override
                    public void onEvent(Event event) {
                        Radio selectedItem = reassigningTypeSelector
                                .getSelectedItem();
                        newTypeChoosen(Type.fromRadio(selectedItem));
                        associatedDate.setVisible(currentType
                                .needsAssociatedDate());
                    }
                });
    }


    private void newTypeChoosen(Type fromRadio) {
        this.currentType = fromRadio;
        associatedDate.setVisible(false);
    }


    public void confirm() {
        if (currentType.needsAssociatedDate()) {
            Date value = associatedDate.getValue();
            if (value == null) {
                throw new WrongValueException(associatedDate,
                        _("must be not empty"));
            }
        }
        window.setVisible(false);
        final LocalDate associatedDate = this.associatedDate.getValue() != null ? LocalDate
                .fromDateFields(this.associatedDate.getValue())
                : null;
        configurationResult.result(ReassignConfiguration.create(currentType,
                associatedDate));
    }

    public void cancel() {
        window.setVisible(false);
    }

}
