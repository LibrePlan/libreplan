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

package org.libreplan.web.planner.company;

import static org.libreplan.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.libreplan.business.common.entities.ProgressType;
import org.libreplan.business.planner.entities.TaskElement;
import org.libreplan.business.users.entities.UserRole;
import org.libreplan.web.common.components.bandboxsearch.BandboxMultipleSearch;
import org.libreplan.web.common.components.finders.FilterPair;
import org.libreplan.web.planner.TaskGroupPredicate;
import org.libreplan.web.planner.tabs.MultipleTabsPlannerController;
import org.libreplan.web.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.zkoss.ganttz.IPredicate;
import org.zkoss.ganttz.Planner;
import org.zkoss.ganttz.extensions.ICommandOnTask;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.web.servlet.dsp.action.Page;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Vbox;

/**
 * Controller for company planning view. Representation of company orders in the
 * planner.
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
@org.springframework.stereotype.Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CompanyPlanningController implements Composer {

    @Autowired
    private ICompanyPlanningModel model;

    private List<ICommandOnTask<TaskElement>> additional = new ArrayList<ICommandOnTask<TaskElement>>();

    private Planner planner;

    private Vbox orderFilter;
    private Datebox filterStartDate;
    private Datebox filterFinishDate;
    private BandboxMultipleSearch bdFilters;
    private Checkbox checkIncludeOrderElements;

    private ICommandOnTask<TaskElement> doubleClickCommand;

    private Map<String, String[]> parameters;

    private MultipleTabsPlannerController tabsController;

    public CompanyPlanningController() {
    }

    private Combobox cbProgressTypes;
    private Button btnShowAdvances;

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) {
        planner = (Planner) comp;
        String zoomLevelParameter = null;
        if ((parameters != null) && (parameters.get("zoom") != null)
                && !(parameters.isEmpty())) {
            zoomLevelParameter = parameters.get("zoom")[0];
        }
        if (zoomLevelParameter != null) {
            planner.setInitialZoomLevel(ZoomLevel
                    .getFromString(zoomLevelParameter));
        }
        planner.setAreContainersExpandedByDefault(Planner
                .guessContainersExpandedByDefault(parameters));

        initializeListboxProgressTypes();

        planner.setAreShownAdvancesByDefault(Planner
                .guessShowAdvancesByDefault(parameters));

        planner.setAreShownReportedHoursByDefault(Planner
                .guessShowReportedHoursByDefault(parameters));

        orderFilter = (Vbox) planner.getFellow("orderFilter");
        // Configuration of the order filter
        Component filterComponent = Executions.createComponents(
                "/orders/_orderFilter.zul", orderFilter,
                new HashMap<String, String>());
        filterComponent.setVariable("orderFilterController", this, true);
        filterStartDate = (Datebox) filterComponent
                .getFellow("filterStartDate");
        filterFinishDate = (Datebox) filterComponent
                .getFellow("filterFinishDate");
        bdFilters = (BandboxMultipleSearch) filterComponent
                .getFellow("bdFilters");
        bdFilters.setFinder("taskGroupsMultipleFiltersFinder");
        checkIncludeOrderElements = (Checkbox) filterComponent
                .getFellow("checkIncludeOrderElements");
        filterComponent.setVisible(true);

        checkCreationPermissions();

    }

    /**
     * Checks the creation permissions of the current user and enables/disables
     * the create buttons accordingly.
     */
    private void checkCreationPermissions() {
        if (!SecurityUtils.isUserInRole(UserRole.ROLE_CREATE_ORDER)) {
            Button createOrderButton = (Button) planner.getPage().getFellow(
                    "createOrderButton");
            if (createOrderButton != null) {
                createOrderButton.setDisabled(true);
            }
        }
    }

    private void initializeListboxProgressTypes() {
        if (cbProgressTypes == null) {
            cbProgressTypes = (Combobox) planner.getFellow("cbProgressTypes");
        }
        if (btnShowAdvances == null) {
            btnShowAdvances = (Button) planner.getFellow("showAdvances");
        }

        cbProgressTypes.setModel(new ListModelList(ProgressType.getAll()));
        cbProgressTypes.setItemRenderer(new ProgressTypeRenderer());

        // Update completion of tasks on selecting new progress type
        cbProgressTypes.addEventListener(Events.ON_SELECT, new EventListener() {

            @Override
            public void onEvent(Event event) {
                planner.forcedShowAdvances();
                planner.updateCompletion(getSelectedProgressType().toString());
            }

            private ProgressType getSelectedProgressType() {
                return (ProgressType) cbProgressTypes.getSelectedItem().getValue();
            }

        });

        cbProgressTypes.setVisible(true);

        ProgressType progressType = getProgressTypeFromConfiguration();
        if (progressType != null) {
            planner.updateCompletion(progressType.toString());
        }

    }

    private class ProgressTypeRenderer implements ComboitemRenderer {

        @Override
        public void render(Comboitem item, Object data) {
            ProgressType progressType = (ProgressType) data;
            item.setValue(progressType);
            item.setLabel(_(progressType.getValue()));

            ProgressType configuredProgressType = getProgressTypeFromConfiguration();
            if ((configuredProgressType != null)
                    && configuredProgressType.equals(progressType)) {
                cbProgressTypes.setSelectedItem(item);
            }
        }
    }

    public ProgressType getProgressTypeFromConfiguration() {
        return model.getProgressTypeFromConfiguration();
    }

    public void setConfigurationForPlanner() {
        // Added predicate
        model
                .setConfigurationToPlanner(planner, additional,
                doubleClickCommand, createPredicate());
        model.setTabsController(tabsController);
        planner.updateSelectedZoomLevel();
        planner.invalidate();
    }

    public void setAdditional(List<ICommandOnTask<TaskElement>> additional) {
        Validate.notNull(additional);
        Validate.noNullElements(additional);
        this.additional = additional;
    }

    public void setDoubleClickCommand(
            ICommandOnTask<TaskElement> doubleClickCommand) {
        this.doubleClickCommand = doubleClickCommand;
    }

    public void setURLParameters(Map<String, String[]> parameters) {
        this.parameters = parameters;
    }

    /**
     * Operations to filter the tasks by multiple filters
     */

    public Constraint checkConstraintFinishDate() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                Date finishDate = (Date) value;
                if ((finishDate != null)
                        && (filterStartDate.getValue() != null)
                        && (finishDate.compareTo(filterStartDate.getValue()) < 0)) {
                    filterFinishDate.setValue(null);
                    throw new WrongValueException(comp,
                            _("must be greater than start date"));
                }
            }
        };
    }

    public Constraint checkConstraintStartDate() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                Date startDate = (Date) value;
                if ((startDate != null)
                        && (filterFinishDate.getValue() != null)
                        && (startDate.compareTo(filterFinishDate.getValue()) > 0)) {
                    filterStartDate.setValue(null);
                    throw new WrongValueException(comp,
                            _("must be lower than finish date"));
                }
            }
        };
    }

    public void onApplyFilter() {
        filterByPredicate(createPredicate());
    }

    private IPredicate createPredicate() {
        List<FilterPair> listFilters = (List<FilterPair>) bdFilters
                .getSelectedElements();
        Date startDate = filterStartDate.getValue();
        Date finishDate = filterFinishDate.getValue();
        Boolean includeOrderElements = checkIncludeOrderElements.isChecked();

        if (listFilters.isEmpty() && startDate == null && finishDate == null) {
            return model.getDefaultPredicate(includeOrderElements);
        }
        return new TaskGroupPredicate(listFilters, startDate, finishDate,
                includeOrderElements);
    }

    private void filterByPredicate(IPredicate predicate) {
        // Recalculate predicate
        model.setConfigurationToPlanner(planner, additional,
                doubleClickCommand, predicate);
        planner.updateSelectedZoomLevel();
        planner.invalidate();
    }

    public void setTabsController(MultipleTabsPlannerController tabsController) {
        this.tabsController = tabsController;
    }

}
