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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.navalplanner.business.labels.daos.ILabelDAO;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.reports.dtos.HoursWorkedPerResourceDTO;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Diego Pino Garcia <dpino@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class HoursWorkedPerWorkerModel implements IHoursWorkedPerWorkerModel {

    @Autowired
    private IResourceDAO resourceDAO;

    @Autowired
    private ILabelDAO labelDAO;

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    private Set<Resource> selectedResources = new HashSet<Resource>();

    private List<Label> selectedLabels = new ArrayList<Label>();

    private List<Criterion> selectedCriterions = new ArrayList<Criterion>();

    private List<Criterion> allCriterions = new ArrayList<Criterion>();

    private static List<ResourceEnum> applicableResources = new ArrayList<ResourceEnum>();

    static {
        applicableResources.add(ResourceEnum.WORKER);
    }

    private boolean showReportMessage = false;

    @Transactional(readOnly = true)
    public JRDataSource getHoursWorkedPerWorkerReport(List<Resource> resources,
            List<Label> labels, List<Criterion> criterions, Date startingDate,
            Date endingDate) {

        final List<HoursWorkedPerResourceDTO> workingHoursPerWorkerList = resourceDAO
                .getWorkingHoursPerWorker(resources, labels,  criterions, startingDate,
                        endingDate);

        if (workingHoursPerWorkerList != null && !workingHoursPerWorkerList.isEmpty()) {
            setShowReportMessage(false);
            return new JRBeanCollectionDataSource(workingHoursPerWorkerList);
        } else {
            setShowReportMessage(true);
            return new JREmptyDataSource();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void init() {
        this.selectedResources.clear();
        this.selectedLabels.clear();
        this.selectedCriterions.clear();

        allCriterions.clear();
        loadAllCriterions();
    }

    @Override
    public Set<Resource> getResources() {
        return this.selectedResources;
    }

    @Override
    public void removeSelectedResource(Resource resource) {
        this.selectedResources.remove(resource);
    }

    @Override
    public boolean addSelectedResource(Resource resource) {
        if (this.selectedResources.contains(resource)) {
            return false;
        }
        this.selectedResources.add(resource);
        return true;
    }

    public void setShowReportMessage(boolean showReportMessage) {
        this.showReportMessage = showReportMessage;
    }

    @Override
    public boolean isShowReportMessage() {
        return showReportMessage;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Label> getAllLabels(){
        List<Label> allLabels = labelDAO.getAll();
        // initialize the labels
        for (Label label : allLabels) {
            label.getType().getName();
        }
        return allLabels;
    }

    @Override
    public void removeSelectedLabel(Label label) {
        this.selectedLabels.remove(label);
        hasChangeLabels = true;
    }

    @Override
    public boolean addSelectedLabel(Label label) {
        if (this.selectedLabels.contains(label)) {
            return false;
        }
        this.selectedLabels.add(label);
        return true;
    }

    @Override
    public List<Label> getSelectedLabels() {
        return selectedLabels;
    }

    @Override
    public List<Criterion> getCriterions() {
        return this.allCriterions;
    }

    private void loadAllCriterions() {
        List<CriterionType> listTypes = getCriterionTypes();
        for (CriterionType criterionType : listTypes) {
            if (criterionType.isEnabled()) {
                Set<Criterion> listCriterion = getDirectCriterions(criterionType);
                addCriterionWithItsType(listCriterion);
            }
        }
    }

    private static Set<Criterion> getDirectCriterions(
            CriterionType criterionType) {
        Set<Criterion> criterions = new HashSet<Criterion>();
        for (Criterion criterion : criterionType.getCriterions()) {
            if (criterion.getParent() == null) {
                criterions.add(criterion);
            }
        }
        return criterions;
    }

    private void addCriterionWithItsType(Set<Criterion> children) {
        for (Criterion criterion : children) {
            if (criterion.isActive()) {
                // Add to the list
                allCriterions.add(criterion);
                addCriterionWithItsType(criterion.getChildren());
            }
        }
    }

    private List<CriterionType> getCriterionTypes() {
        return criterionTypeDAO
                .getCriterionTypesByResources(applicableResources);
    }

    @Override
    public void removeSelectedCriterion(Criterion criterion) {
        this.selectedCriterions.remove(criterion);
    }

    @Override
    public boolean addSelectedCriterion(Criterion criterion) {
        if (this.selectedCriterions.contains(criterion)) {
            return false;
        }
        this.selectedCriterions.add(criterion);
        return true;
    }

    @Override
    public List<Criterion> getSelectedCriterions() {
        return selectedCriterions;
    }

}
