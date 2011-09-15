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

package org.navalplanner.web.reports;

import static org.navalplanner.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jasperreports.engine.JRDataSource;

import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.reports.dtos.LabelFilterType;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.Autocomplete;
import org.navalplanner.web.common.components.bandboxsearch.BandboxSearch;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Radio;

/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class HoursWorkedPerWorkerController extends NavalplannerReportController {

    private static final String REPORT_NAME = "hoursWorkedPerWorkerReport";

    private IHoursWorkedPerWorkerModel hoursWorkedPerWorkerModel;

    private Listbox lbResources;

    private Listbox lbLabels;

    private Listbox lbCriterions;

    private Datebox startingDate;

    private Datebox endingDate;

    private Autocomplete filterResource;

    private BandboxSearch bdLabels;

    private BandboxSearch bdCriterions;

    private ResourceListRenderer resourceListRenderer = new ResourceListRenderer();

    private Radio filterByWorkReports;

    private Radio filterByOrderElements;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
        hoursWorkedPerWorkerModel.init();
    }

    public Set<Resource> getResources() {
        return hoursWorkedPerWorkerModel.getResources();
    }

    @Override
    protected String getReportName() {
        return REPORT_NAME;
    }

    @Override
    protected JRDataSource getDataSource() {
        return hoursWorkedPerWorkerModel.getHoursWorkedPerWorkerReport(
                getSelectedResources(), getSelectedLabels(),
                getSelectedFilterLabels(),
                getSelectedCriterions(),
                getStartingDate(), getEndingDate());
    }

    private LabelFilterType getSelectedFilterLabels() {
        if (filterByWorkReports.isChecked())
            return LabelFilterType.WORK_REPORT;
        if (filterByOrderElements.isChecked())
            return LabelFilterType.ORDER_ELEMENT;
        return LabelFilterType.BOTH;
    }

    private List<Resource> getSelectedResources() {
        List<Resource> result = new ArrayList<Resource>();

        final List<Listitem> listItems = lbResources.getItems();
        for (Listitem each : listItems) {
            result.add((Resource) each.getValue());
        }
        return result;
    }

    private Date getStartingDate() {
        if (startingDate.getValue() == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startingDate.getValue());
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE), 0, 0, 0);
        return calendar.getTime();
    }

    private Date getEndingDate() {
        if (endingDate.getValue() == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endingDate.getValue());
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE), 23, 59, 59);
        return calendar.getTime();
    }

    @Override
    protected Map<String, Object> getParameters() {
        Map<String, Object> result = super.getParameters();

        result.put("startingDate", getStartingDate());
        result.put("endingDate", getEndingDate());
        result.put("showNote", hoursWorkedPerWorkerModel.isShowReportMessage());
        return result;
    }

    public void onAddResource() {
        Resource resource = getSelectedCurrentResource();
        if (resource != null) {
            boolean result = hoursWorkedPerWorkerModel
                    .addSelectedResource(resource);
            if (!result) {
                throw new WrongValueException(filterResource,
                        _("This resource has already been added."));
            } else {
                Util.reloadBindings(lbResources);
            }
        }
    }

    public void onRemoveResource(Resource resource) {
        hoursWorkedPerWorkerModel.removeSelectedResource(resource);
        Util.reloadBindings(lbResources);
    }

    private Resource getSelectedCurrentResource() {
        Comboitem itemSelected = filterResource.getSelectedItem();
        if ((itemSelected != null)
                && (((Resource) itemSelected.getValue()) != null)) {
            return (Resource) itemSelected.getValue();
        }
        return null;
    }

    public ResourceListRenderer getRenderer() {
        return resourceListRenderer;
    }

    /**
     * ListitemRenderer for a @{Resource} element
     * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
     */
    public class ResourceListRenderer implements ListitemRenderer {

        @Override
        public void render(Listitem item, Object data) throws Exception {
            final Resource resource = (Resource) data;
            item.setValue(resource);

            appendType(item);
            appendLimiting(item);
            appendName(item);
            appendCode(item);
            appendDeleteButton(item);
        }
    }

    private void appendType(final Listitem item) {
        Resource resource = (Resource) item.getValue();
        org.zkoss.zul.Label typeLabel = new org.zkoss.zul.Label(
                getType(resource));

        Listcell typeResourceCell = new Listcell();
        typeResourceCell.appendChild(typeLabel);
        item.appendChild(typeResourceCell);
    }

    private void appendLimiting(final Listitem item) {
        final Resource resource = (Resource) item.getValue();
        final Checkbox limitingCheckbox = new Checkbox();
        limitingCheckbox.setChecked(resource.isLimitingResource());
        limitingCheckbox.setDisabled(true);

        Listcell limitingResourceCell = new Listcell();
        limitingResourceCell.appendChild(limitingCheckbox);
        item.appendChild(limitingResourceCell);
    }

    private void appendName(final Listitem item) {
        Resource resource = (Resource) item.getValue();
        org.zkoss.zul.Label nameLabel = new org.zkoss.zul.Label(
                getName(resource));

        Listcell nameResourceCell = new Listcell();
        nameResourceCell.appendChild(nameLabel);
        item.appendChild(nameResourceCell);
    }

    private void appendCode(Listitem item) {
        Resource resource = (Resource) item.getValue();
        org.zkoss.zul.Label codeLabel = new org.zkoss.zul.Label(resource
                .getCode());

        Listcell codeResourceCell = new Listcell();
        codeResourceCell.appendChild(codeLabel);
        item.appendChild(codeResourceCell);
    }

    private void appendDeleteButton(final Listitem item) {
        Button delete = new Button("", "/common/img/ico_borrar1.png");
        delete.setHoverImage("/common/img/ico_borrar.png");
        delete.setSclass("icono");
        delete.setTooltiptext(_("Delete"));
        delete.addEventListener(Events.ON_CLICK, new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                onRemoveResource((Resource) item.getValue());
            }
        });

        Listcell deleteResourceCell = new Listcell();
        deleteResourceCell.appendChild(delete);
        item.appendChild(deleteResourceCell);
    }

    private String getName(Resource resource) {
        if ((resource instanceof Worker) && (((Worker) resource).isReal())) {
            return ((Worker) resource).getShortDescription();
        }
        return resource.getName();
    }

    private String getType(Resource resource) {
        if (resource instanceof Worker) {
            if (((Worker) resource).isReal()) {
                return _("Worker");
            } else {
                return _("Virtual worker");
            }
        }
        return "Machine";
    }

    public List<Label> getAllLabels() {
        return hoursWorkedPerWorkerModel.getAllLabels();
    }

    public void onSelectLabel() {
        Label label = (Label) bdLabels.getSelectedElement();
        if (label == null) {
            throw new WrongValueException(bdLabels, _("please, select a label"));
        }
        boolean result = hoursWorkedPerWorkerModel.addSelectedLabel(label);
        if (!result) {
            throw new WrongValueException(bdLabels,
                    _("This label has already been added."));
        } else {
            Util.reloadBindings(lbLabels);
        }
        bdLabels.clear();
    }

    public void onRemoveLabel(Label label) {
        hoursWorkedPerWorkerModel.removeSelectedLabel(label);
        Util.reloadBindings(lbLabels);
    }

    public List<Label> getSelectedLabels() {
        return hoursWorkedPerWorkerModel.getSelectedLabels();
    }

    public List<Criterion> getAllCriterions() {
        return hoursWorkedPerWorkerModel.getCriterions();
    }

    public void onSelectCriterion() {
        Criterion criterion = (Criterion) bdCriterions.getSelectedElement();
        if (criterion == null) {
            throw new WrongValueException(bdCriterions,
                    _("please, select a Criterion"));
        }
        boolean result = hoursWorkedPerWorkerModel
                .addSelectedCriterion(criterion);
        if (!result) {
            throw new WrongValueException(bdCriterions,
                    _("This Criterion has already been added."));
        } else {
            Util.reloadBindings(lbCriterions);
        }
    }

    public void onRemoveCriterion(Criterion criterion) {
        hoursWorkedPerWorkerModel.removeSelectedCriterion(criterion);
        Util.reloadBindings(lbCriterions);
    }

    public List<Criterion> getSelectedCriterions() {
        return hoursWorkedPerWorkerModel.getSelectedCriterions();
    }

}
