/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2012 Igalia, S.L.
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

package org.libreplan.web.workreports;

import static org.libreplan.web.I18nHelper._;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.Hibernate;
import org.libreplan.business.common.IntegrationEntity;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.entities.EntityNameEnum;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.costcategories.daos.ITypeOfWorkHoursDAO;
import org.libreplan.business.costcategories.entities.TypeOfWorkHours;
import org.libreplan.business.labels.daos.ILabelDAO;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.labels.entities.LabelType;
import org.libreplan.business.orders.daos.IOrderElementDAO;
import org.libreplan.business.orders.daos.ISumChargedEffortDAO;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderLineGroup;
import org.libreplan.business.resources.daos.IWorkerDAO;
import org.libreplan.business.resources.entities.Resource;
import org.libreplan.business.resources.entities.Worker;
import org.libreplan.business.workreports.daos.IWorkReportDAO;
import org.libreplan.business.workreports.daos.IWorkReportTypeDAO;
import org.libreplan.business.workreports.entities.WorkReport;
import org.libreplan.business.workreports.entities.WorkReportLabelTypeAssigment;
import org.libreplan.business.workreports.entities.WorkReportLine;
import org.libreplan.business.workreports.entities.WorkReportType;
import org.libreplan.business.workreports.valueobjects.DescriptionField;
import org.libreplan.business.workreports.valueobjects.DescriptionValue;
import org.libreplan.web.common.IntegrationEntityModel;
import org.libreplan.web.common.concurrentdetection.OnConcurrentModification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zkoss.ganttz.IPredicate;

/**
 * Model for UI operations related to {@link WorkReport}.
 *
 * @author Diego Pino García <dpino@igalia.com>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@OnConcurrentModification(goToPage = "/workreports/workReport.zul")
public class WorkReportModel extends IntegrationEntityModel implements
        IWorkReportModel {

    @Autowired
    private IWorkReportTypeDAO workReportTypeDAO;

    @Autowired
    private IWorkReportDAO workReportDAO;

    @Autowired
    private IOrderElementDAO orderElementDAO;

    @Autowired
    private IWorkerDAO workerDAO;

    @Autowired
    private ILabelDAO labelDAO;

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Autowired
    private ITypeOfWorkHoursDAO typeOfWorkHoursDAO;

    @Autowired
    private ISumChargedEffortDAO sumChargedEffortDAO;

    private WorkReportType workReportType;

    private WorkReport workReport;

    private boolean editing = false;

    private boolean listingQuery = false;

    private static final Map<LabelType, List<Label>> mapLabelTypes = new HashMap<LabelType, List<Label>>();

    private List<WorkReportDTO> listWorkReportDTOs = new ArrayList<WorkReportDTO>();

    private List<WorkReportLine> listWorkReportLine = new ArrayList<WorkReportLine>();

    private Set<WorkReportLine> deletedWorkReportLinesSet = new HashSet<WorkReportLine>();

    @Override
    public WorkReport getWorkReport() {
        return workReport;
    }

    @Override
    public WorkReportType getWorkReportType() {
        return this.workReportType;
    }

    @Override
    @Transactional(readOnly = true)
    public void initCreate(WorkReportType workReportType) {
        editing = false;
        forceLoadWorkReportTypeFromDB(workReportType);
        workReport = WorkReport.create(this.workReportType);
        workReport.setCodeAutogenerated(configurationDAO.getConfiguration()
                .getGenerateCodeForWorkReport());
        if (!workReport.isCodeAutogenerated()) {
            workReport.setCode("");
        }else{
            setDefaultCode();
        }
        loadMaps();
        deletedWorkReportLinesSet = new HashSet<WorkReportLine>();
    }

    @Override
    @Transactional(readOnly = true)
    public void initEdit(WorkReport workReport) {
        editing = true;
        Validate.notNull(workReport);
        this.workReport = getFromDB(workReport);
        forceLoadWorkReportTypeFromDB(workReport.getWorkReportType());
        loadMaps();
        initOldCodes();
        deletedWorkReportLinesSet = new HashSet<WorkReportLine>();
    }

    @Transactional(readOnly = true)
    private WorkReport getFromDB(WorkReport workReport) {
        return getFromDB(workReport.getId());
    }

    @Transactional(readOnly = true)
    private WorkReport getFromDB(Long id) {
        try {
            WorkReport result = workReportDAO.find(id);
            forceLoadEntities(result);
            return result;
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load entities that will be needed in the conversation
     *
     * @param workReport
     */
    private void forceLoadEntities(WorkReport workReport) {
        // Load WorkReportType
        workReport.getWorkReportType().getName();
        if (workReport.getResource() != null) {
            workReport.getResource().getShortDescription();
        }
        if (workReport.getOrderElement() != null) {
            workReport.getOrderElement().getCode();
        }

        // Load Labels
        for (Label label : workReport.getLabels()) {
            label.getName();
            label.getType().getName();
        }

        // Load DescriptionValues
        for (DescriptionValue descriptionValue : workReport
                .getDescriptionValues()) {
            descriptionValue.getFieldName();
        }

        // Load WorkReportLines
        for (WorkReportLine workReportLine : workReport.getWorkReportLines()) {
            //Load pricipal data
            forceLoadPrincipalDataWorkReportLines(workReportLine);

            // Load Labels
            for (Label label : workReportLine.getLabels()) {
                label.getName();
                label.getType().getName();
            }

            // Load DescriptionValues
            for (DescriptionValue descriptionValue : workReportLine
                    .getDescriptionValues()) {
                descriptionValue.getFieldName();
            }
        }
    }

    private void forceLoadPrincipalDataWorkReportLines(WorkReportLine line) {
        line.getEffort().getHours();
        line.getResource().getShortDescription();
        line.getTypeOfWorkHours().getName();
        initalizeOrderElement(line.getOrderElement());
    }

    private void initalizeOrderElement(OrderElement orderElement) {
        Hibernate.initialize(orderElement);
        Hibernate.initialize(orderElement.getChildren());
        initalizeOrder(orderElement);
    }

    private void initalizeOrder(OrderElement orderElement) {
        OrderLineGroup parent = orderElement.getParent();
        while (parent != null) {
            Hibernate.initialize(parent);
            parent = parent.getParent();
        }
    }

    private void forceLoadWorkReportTypeFromDB(WorkReportType workReportType) {
        this.workReportType = getWorkReportTypeFromDB(workReportType.getId());
        forceLoadCollections(this.workReportType);
    }

    @Transactional(readOnly = true)
    private WorkReportType getWorkReportTypeFromDB(Long id) {
        try {
            WorkReportType result = workReportTypeDAO.find(id);
            return result;
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void forceLoadCollections(WorkReportType workReportType) {
        for (DescriptionField line : workReportType.getLineFields()) {
            line.getFieldName();
        }

        for (DescriptionField head : workReportType.getHeadingFields()) {
            head.getFieldName();
        }

        for (WorkReportLabelTypeAssigment assignedLabel : workReportType
                .getWorkReportLabelTypeAssigments()) {
            assignedLabel.getDefaultLabel().getName();
            assignedLabel.getLabelType().getName();
        }
    }

    @Override
    @Transactional
    public void confirmSave() throws ValidationException {
        sumChargedEffortDAO.updateRelatedSumChargedEffortWithDeletedWorkReportLineSet(deletedWorkReportLinesSet);
        sumChargedEffortDAO
                .updateRelatedSumChargedEffortWithWorkReportLineSet(workReport
                        .getWorkReportLines());

        workReportDAO.save(workReport);
    }

    @Override
    @Transactional
    public void generateWorkReportLinesIfIsNecessary() {
        if (workReport.isCodeAutogenerated()) {
            generateWorkReportLineCodes();
        }
    }

    private void generateWorkReportLineCodes() {
        workReport.generateWorkReportLineCodes(getNumberOfDigitsCode());
    }

    @Override
    @Transactional
    public OrderElement findOrderElement(String orderCode)
            throws InstanceNotFoundException {
        OrderElement result =  orderElementDAO.findUniqueByCode(orderCode);
        initializeChildren(result);
        return result;
    }

    private void initializeChildren(OrderElement order) {
        for (OrderElement each: order.getAllChildren()) {
            Hibernate.initialize(each);
        }
    }

    @Override
    @Transactional
    public Worker findWorker(String nif) throws InstanceNotFoundException {
        return workerDAO.findUniqueByNif(nif);
    }

    @Override
    @Transactional
    public Worker asWorker(Resource resource) throws InstanceNotFoundException {
        return workerDAO.find(resource.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkReportDTO> getWorkReportDTOs() {
        // load the work reports DTOs
        listWorkReportDTOs.clear();
        for (WorkReport workReport : getAllWorkReports()) {
            WorkReportDTO workReportDTO = new WorkReportDTO(workReport);
            listWorkReportDTOs.add(workReportDTO);
        }
        return listWorkReportDTOs;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkReportDTO> getFilterWorkReportDTOs(IPredicate predicate) {
        List<WorkReportDTO> resultDTOs = new ArrayList<WorkReportDTO>();
        for (WorkReportDTO workReportDTO : listWorkReportDTOs) {
            if (predicate.accepts(workReportDTO)) {
                resultDTOs.add(workReportDTO);
            }
        }
        return resultDTOs;
    }

    private List<WorkReport> getAllWorkReports() {
        List<WorkReport> result = new ArrayList<WorkReport>();
        for (WorkReport each : workReportDAO
                .allWorkReportsWithAssociatedOrdersUnproxied()) {
            each.getWorkReportType().getName();
            if (each.getResource() != null) {
                each.getResource().getShortDescription();
            }
            if (each.getOrderElement() != null) {
                each.getOrderElement().getName();
                each.getOrderElement().getOrder();
            }
            result.add(each);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkReportLine> getAllWorkReportLines() {
        listWorkReportLine.clear();
        for (WorkReport workReport : getAllWorkReports()) {
            for (WorkReportLine workReportLine : workReport
                    .getWorkReportLines()) {
                forceLoadPrincipalDataWorkReportLines(workReportLine);
                listWorkReportLine.add(workReportLine);
            }
        }
        return listWorkReportLine;
    }

    @Override
    public List<WorkReportLine> getFilterWorkReportLines(IPredicate predicate) {
        List<WorkReportLine> result = new ArrayList<WorkReportLine>();
        for (WorkReportLine workReportLine : listWorkReportLine) {
            if (predicate.accepts(workReportLine)) {
                result.add(workReportLine);
            }
        }
        return result;
    }

    @Override
    public boolean isEditing() {
        return editing;
    }

    @Override
    public boolean isListingQuery() {
        return this.listingQuery;
    }

    @Override
    public void setListingQuery(boolean listingQuery) {
        this.listingQuery = listingQuery;
    }

    @Override
    public WorkReportLine addWorkReportLine() {
        if (workReport != null) {
            WorkReportLine workReportLine = WorkReportLine.create(workReport);
            workReportLine.setCode("");

            // Adding default date
            workReportLine.setDate(new Date());
            workReport.addWorkReportLine(workReportLine);
            return workReportLine;
        }
        return null;
    }

    @Override
    @Transactional
    public void remove(WorkReport workReport) {
        //before deleting the report, update OrderElement.SumChargedHours
        try {
            workReportDAO.reattach(workReport);
            sumChargedEffortDAO
                    .updateRelatedSumChargedEffortWithDeletedWorkReportLineSet(workReport
                            .getWorkReportLines());
            workReportDAO.remove(workReport.getId());
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeWorkReportLine(WorkReportLine workReportLine) {
        deletedWorkReportLinesSet.add(workReportLine);
        workReport.removeWorkReportLine(workReportLine);
    }

    @Override
    public List<WorkReportLine> getWorkReportLines() {
        List<WorkReportLine> result = new ArrayList<WorkReportLine>();
        if (getWorkReport() != null) {
            result.addAll(workReport.getWorkReportLines());
        }
        return result;
    }

    /* Operations to manage the Description Fields and the assigned labels */

    @Override
    public List<Object> getFieldsAndLabelsLineByDefault() {
        if ((getWorkReport() != null)) {
            return sort(getWorkReportType().getLineFieldsAndLabels());
        }
        return new ArrayList<Object>();
    }

    @Override
    public List<Object> getFieldsAndLabelsHeading() {
        List<Object> result = new ArrayList<Object>();
        if (getWorkReport() != null) {
            result.addAll(getWorkReport().getDescriptionValues());
            result.addAll(getWorkReport().getLabels());
            return sort(result);
        }
        return result;
    }

    @Override
    public List<Object> getFieldsAndLabelsLine(WorkReportLine workReportLine) {
        List<Object> result = new ArrayList<Object>();
        if ((getWorkReport() != null) && (workReportLine != null)) {
            result.addAll(workReportLine.getDescriptionValues());
            result.addAll(workReportLine.getLabels());
            return sort(result);
        }
        return result;
    }

    @Override
    public Map<LabelType, List<Label>> getMapAssignedLabelTypes() {
        return this.mapLabelTypes;
    }

    @Override
    public void changeLabelInWorkReportLine(Label oldLabel, Label newLabel,
            WorkReportLine line) {
        if (line != null) {
            line.getLabels().remove(oldLabel);
            line.getLabels().add(newLabel);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void changeLabelInWorkReport(Label oldLabel, Label newLabel) {
        if (getWorkReport() != null) {
            getWorkReport().getLabels().remove(oldLabel);
            getWorkReport().getLabels().add(newLabel);
        }
    }

    private void loadMaps() {
        loadLabelsByAssignedType();
    }

    private void loadLabelsByAssignedType() {
        mapLabelTypes.clear();
        //get the all assigned label types.
        for (LabelType labelType : getAssignedLabelTypes()) {
            List<Label> labels = new ArrayList<Label>(labelDAO
                    .findByType(labelType));
            mapLabelTypes.put(labelType, labels);
        }
    }

    private DescriptionField getDescriptionFieldByName(String name){
        for (DescriptionField descriptionField : getWorkReportType()
                .getDescriptionFields()) {
            if(descriptionField.getFieldName().equals(name)){
                return descriptionField;
            }
        }
        return null;
    }

    private Integer getAssignedLabelIndex(Label label){
        for (WorkReportLabelTypeAssigment labelTypeAssigment : getWorkReportType()
                .getWorkReportLabelTypeAssigments()) {
            if(labelTypeAssigment.getLabelType().equals(label.getType())){
                return labelTypeAssigment.getPositionNumber();
            }
        }
        return null;
    }

    private List<Object> sort(List<Object> list) {
        List<Object> result = new ArrayList<Object>(list);
        if (list != null) {
            for (Object object : list) {
                Integer index = getIndex(object);
                if ((index != null) && ((index >= 0) && (index < list.size()))) {
                    result.set(getIndex(object), object);
                }
            }
        }
        return result;
    }

    private List<LabelType> getAssignedLabelTypes() {
        List<LabelType> result = new ArrayList<LabelType>();
        for (WorkReportLabelTypeAssigment labelTypeAssigment : getWorkReportType()
                .getWorkReportLabelTypeAssigments()) {
            result.add(labelTypeAssigment.getLabelType());
        }
        return result;
    }

    private Integer getIndex(Object object) {
        if (object instanceof DescriptionValue) {
            DescriptionField descriptionField = getDescriptionFieldByName(((DescriptionValue) object)
                    .getFieldName());
            return descriptionField.getPositionNumber();
        }
        if (object instanceof Label) {
            return getAssignedLabelIndex((Label) object);
        }
        if (object instanceof DescriptionField) {
            return ((DescriptionField) object).getPositionNumber();
        }
        if (object instanceof WorkReportLabelTypeAssigment) {
            return ((WorkReportLabelTypeAssigment) object).getPositionNumber();
        }
        return null;
    }

    @Override
    public Integer getLength(DescriptionValue descriptionValue) {
        DescriptionField descriptionField = getDescriptionFieldByName(descriptionValue
                .getFieldName());
        return descriptionField.getLength();
    }

    /**
     * Set the selected default work report type to filter the work reports
     */
    public final String SHOW_ALL_TYPES = _("Show all");

    private final WorkReportType defaultType = WorkReportType.create(
            SHOW_ALL_TYPES, "");

    @Override
    public WorkReportType getDefaultType() {
        return defaultType;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkReportType> getWorkReportTypes() {
        List<WorkReportType> result = workReportTypeDAO
                .list(WorkReportType.class);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderElement> getOrderElements() {
        return orderElementDAO.getAll();
    }

    @Override
    public EntityNameEnum getEntityName() {
        return EntityNameEnum.WORK_REPORT;
    }

    @Override
    public Set<IntegrationEntity> getChildren() {
        return (Set<IntegrationEntity>) (workReport != null ? workReport
                .getWorkReportLines() : new HashSet<IntegrationEntity>());
    }

    @Override
    public IntegrationEntity getCurrentEntity() {
        return this.workReport;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TypeOfWorkHours> getAllHoursType() {
        return typeOfWorkHoursDAO.hoursTypeByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Worker> getBoundWorkers() {
        return workerDAO.getBound();
    }

}
