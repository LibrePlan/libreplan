/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2010 Igalia S.L.
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

package org.navalplanner.web.resources;

import static org.navalplanner.web.I18nHelper._;

import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.ResourceType;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.bandboxsearch.BandboxMultipleSearch;
import org.navalplanner.web.common.components.finders.FilterPair;
import org.navalplanner.web.resources.search.ResourcePredicate;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.api.Window;

/**
 * Class to abstract some common code from WorkerCRUDController and MachineCRUDController.
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
public abstract class ResourceCRUDController extends GenericForwardComposer {

    protected Window listWindow;

    protected Window editWindow;

    private Grid listing;

    private Datebox filterStartDate;

    private Datebox filterFinishDate;

    private Listbox filterResourceType;

    private Textbox txtfilter;

    private BandboxMultipleSearch bdFilters;

    private final String ALL_TYPES_OF_RESOURCE = _("ALL");

    /*
     * Initialization of the filter components
     */

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        initFilterComponent();
    }

    private void initFilterComponent() {
        this.filterFinishDate = (Datebox) listWindow
                .getFellowIfAny("filterFinishDate");
        this.filterStartDate = (Datebox) listWindow
                .getFellowIfAny("filterStartDate");
        this.filterResourceType = (Listbox) listWindow
                .getFellowIfAny("filterResourceType");
        this.bdFilters = (BandboxMultipleSearch) listWindow
                .getFellowIfAny("bdFilters");
        this.txtfilter = (Textbox) listWindow.getFellowIfAny("txtfilter");
        this.listing = (Grid) listWindow.getFellowIfAny("listing");
        clearFilterDates();
        setupFilterResourceTypeListbox();
    }

    private void clearFilterDates() {
        filterStartDate.setValue(null);
        filterFinishDate.setValue(null);
    }

    private void setupFilterResourceTypeListbox() {
        Listitem item = new Listitem();
        item.setParent(filterResourceType);
        item.setValue(ALL_TYPES_OF_RESOURCE);
        item.appendChild(new Listcell(ALL_TYPES_OF_RESOURCE));
        filterResourceType.appendChild(item);
        for(ResourceType resourceType :
            ResourceType.getResourceTypeList()) {
            item = new Listitem();
            item.setParent(filterResourceType);
            item.setValue(resourceType);
            item.appendChild(new Listcell(resourceType.toString()));
            filterResourceType.appendChild(item);
        }
        filterResourceType.setSelectedIndex(0);
    }

    /*
     * Operations to filter the resources by multiple filters
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
        ResourcePredicate predicate = createPredicate();
        if (predicate != null) {
            filterByPredicate(predicate);
        } else {
            showAllResources();
        }
    }

    private ResourcePredicate createPredicate() {
        List<FilterPair> listFilters = (List<FilterPair>) bdFilters
                .getSelectedElements();

        String personalFilter = txtfilter.getValue();
        // Get the dates filter
        LocalDate startDate = null;
        LocalDate finishDate = null;
        if (filterStartDate.getValue() != null) {
            startDate = LocalDate.fromDateFields(filterStartDate
                .getValue());
        }
        if (filterFinishDate.getValue() != null) {
            finishDate = LocalDate.fromDateFields(filterFinishDate
                .getValue());
        }

        final Listitem item = filterResourceType.getSelectedItem();
        ResourceType resourceType = null;
        if (item != null) {
            if (!(item.getValue() == ALL_TYPES_OF_RESOURCE)) {
                resourceType = (ResourceType) item.getValue();
            }
        }
        if (listFilters.isEmpty()
                && (personalFilter == null || personalFilter.isEmpty())
                && startDate == null && finishDate == null
                && resourceType == null) {
            return null;
        }
        return new ResourcePredicate(listFilters, personalFilter, startDate,
                finishDate, resourceType);
    }

    public void showAllResources() {
        listing.setModel(new SimpleListModel(getAllResourcesFromModel()
                .toArray()));
        listing.invalidate();
    }

    private void filterByPredicate(ResourcePredicate predicate) {
        List<? extends Resource> filteredResources = getFilteredResourcesFromModel(predicate);
        listing.setModel(new SimpleListModel(filteredResources.toArray()));
        listing.invalidate();
    }

    protected abstract List<? extends Resource> getAllResourcesFromModel();
    protected abstract List<? extends Resource> getFilteredResourcesFromModel(ResourcePredicate predicate);

    /*
     * Operations related to common attributes
     */

    public Set<ResourceType> getResourceTypeOptionList() {
        return ResourceType.getResourceTypeList();
    }

    public Object getResourceType() {
        final Resource resource = getResource();
        return (resource != null) ? resource.getResourceType()
                : ResourceType.NON_LIMITING_RESOURCE;         // Default option
    }

    public void setResourceType(ResourceType option) {
        final Resource resource = getResource();
        if (resource != null) {
            resource.setResourceType(option);
        }
    }

    public void onCheckGenerateCode(Event e) {
        CheckEvent ce = (CheckEvent) e;
        if (ce.isChecked()) {
            // we have to auto-generate the code if it's unsaved
            try {
                setCodeAutogenerated(ce.isChecked());
            } catch (ConcurrentModificationException err) {
                showMessage(Level.ERROR, err.getMessage());
            }
            Util.reloadBindings(editWindow);
        }
    }

    protected abstract Resource getResource();
    protected abstract void setCodeAutogenerated(Boolean codeAutogenerated);
    protected abstract void showMessage(Level level, String message);

}
