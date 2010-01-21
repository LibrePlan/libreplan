/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
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
package org.navalplanner.business.templates.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.Min;
import org.hibernate.validator.Valid;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.navalplanner.business.advance.entities.AdvanceAssignmentTemplate;
import org.navalplanner.business.advance.entities.DirectAdvanceAssignment;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.labels.entities.Label;
import org.navalplanner.business.materials.entities.MaterialAssignment;
import org.navalplanner.business.materials.entities.MaterialAssignmentTemplate;
import org.navalplanner.business.orders.entities.InfoComponent;
import org.navalplanner.business.orders.entities.Order;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLineGroup;
import org.navalplanner.business.orders.entities.SchedulingState;
import org.navalplanner.business.orders.entities.SchedulingState.ITypeChangedListener;
import org.navalplanner.business.orders.entities.SchedulingState.Type;
import org.navalplanner.business.qualityforms.entities.QualityForm;
import org.navalplanner.business.trees.ITreeNode;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public abstract class OrderElementTemplate extends BaseEntity implements
        ITreeNode<OrderElementTemplate> {

    private static final Log LOG = LogFactory
            .getLog(OrderElementTemplate.class);

    public static <T extends OrderElementTemplate> T create(T beingBuilt,
            OrderElement origin) {
        InfoComponent infoComponentCopied = origin.getInfoComponent().copy();
        Order order = origin.getOrder();
        Days fromBeginningToStart = daysBetween(order.getInitDate(), origin
                .getInitDate());
        Days fromBeginningToEnd = daysBetween(order.getInitDate(), origin
                .getDeadline());
        beingBuilt.materialAssignments = copyMaterialAssignmentsFrom(beingBuilt, origin
                .getMaterialAssignments());
        beingBuilt.labels = new HashSet<Label>(origin.getLabels());
        beingBuilt.qualityForms = origin.getQualityForms();
        beingBuilt.advanceAssignmentTemplates = copyDirectAdvanceAssignments(
                beingBuilt, origin.getDirectAdvanceAssignments());
        beingBuilt.infoComponent = infoComponentCopied;
        assignDates(beingBuilt, fromBeginningToStart, fromBeginningToEnd);
        return create(beingBuilt);
    }

    private static Set<AdvanceAssignmentTemplate> copyDirectAdvanceAssignments(
            OrderElementTemplate beingBuilt,
            Set<DirectAdvanceAssignment> directAdvanceAssignments) {
        Set<AdvanceAssignmentTemplate> result = new HashSet<AdvanceAssignmentTemplate>();
        for (DirectAdvanceAssignment each : directAdvanceAssignments) {
            result.add(AdvanceAssignmentTemplate.convert(beingBuilt, each));
        }
        return result;
    }

    public static <T extends OrderElementTemplate> T createNew(T beingBuilt) {
        beingBuilt.infoComponent = new InfoComponent();
        assignDates(beingBuilt, null, null);
        return create(beingBuilt);
    }

    private static Set<MaterialAssignmentTemplate> copyMaterialAssignmentsFrom(OrderElementTemplate beingBuilt,
            Collection<? extends MaterialAssignment> assignments) {
        Set<MaterialAssignmentTemplate> result = new HashSet<MaterialAssignmentTemplate>();
        for (MaterialAssignment each : assignments) {
            result.add(MaterialAssignmentTemplate.copyFrom(each, beingBuilt));
        }
        return result;
    }

    private static void assignDates(OrderElementTemplate beingBuilt,
            Days fromBeginningToStart, Days fromBeginningToEnd) {
        Validate.isTrue(isNullOrPositive(fromBeginningToStart));
        Validate.isTrue(isNullOrPositive(fromBeginningToEnd));
        beingBuilt.startAsDaysFromBeginning = daysToInteger(fromBeginningToStart);
        beingBuilt.deadlineAsDaysFromBeginning = daysToInteger(fromBeginningToEnd);
    }

    private static Days daysBetween(Date start, Date end) {
        if (start == null || end == null) {
            return null;
        }
        return Days.daysBetween(asDateTime(start), asDateTime(end));
    }

    private static DateTime asDateTime(Date date) {
        return new DateTime(date);
    }

    private static boolean isNullOrPositive(Days days) {
        return days == null || days.getDays() >= 0;
    }

    private static Integer daysToInteger(Days days) {
        return days != null ? days.getDays() : null;
    }

    protected <T extends OrderElement> T setupElementParts(T orderElement) {
        setupInfoComponent(orderElement);
        setupDates(orderElement);
        setupMaterialAssignments(orderElement);
        setupLabels(orderElement);
        setupQualityForms(orderElement);
        setupAdvances(orderElement);
        return orderElement;
    }

    private void setupInfoComponent(OrderElement orderElement) {
        orderElement.setCode(getCode());
        orderElement.setName(getName());
        orderElement.setDescription(getDescription());
    }

    private <T> void setupDates(OrderElement orderElement) {
        Date orderInitDate = orderElement.getOrder().getInitDate();
        if (getStartAsDaysFromBeginning() != null) {
            orderElement.setInitDate(plusDays(orderInitDate,
                    getStartAsDaysFromBeginning()));
        }
        if (getDeadlineAsDaysFromBeginning() != null) {
            orderElement.setDeadline(plusDays(orderInitDate,
                    getDeadlineAsDaysFromBeginning()));
        }
    }

    private Date plusDays(Date date, Integer days) {
        LocalDate localDate = new LocalDate(date);
        return localDate.plusDays(days).toDateTimeAtStartOfDay().toDate();
    }

    private void setupMaterialAssignments(OrderElement orderElement) {
        for (MaterialAssignmentTemplate each : materialAssignments) {
            orderElement.addMaterialAssignment(each
                    .createAssignment(orderElement));
        }
    }

    private void setupLabels(OrderElement orderElement) {
        for (Label each : getLabels()) {
            orderElement.addLabel(each);
        }
    }

    private void setupQualityForms(OrderElement orderElement) {
        for (QualityForm each : qualityForms) {
            orderElement.addTaskQualityForm(each);
        }
    }

    private void setupAdvances(OrderElement orderElement) {
        for (AdvanceAssignmentTemplate each : advanceAssignmentTemplates) {
            try {
                orderElement.addAdvanceAssignment(each
                        .createAdvanceAssignment(orderElement));
            } catch (Exception e) {
                String errorMessage = "error adding advance assignment to newly instantiated orderElement. Ignoring it";
                LOG.warn(errorMessage, e);
            }
        }
    }

    public abstract OrderElement createElement();

    public abstract OrderElement createElement(OrderLineGroup parent);

    private SchedulingState.Type schedulingStateType;

    private InfoComponent infoComponent;

    private Integer startAsDaysFromBeginning;

    private Integer deadlineAsDaysFromBeginning;

    private OrderLineGroupTemplate parent;

    private Set<MaterialAssignmentTemplate> materialAssignments = new HashSet<MaterialAssignmentTemplate>();

    private Set<Label> labels = new HashSet<Label>();

    private Set<QualityForm> qualityForms = new HashSet<QualityForm>();

    private Set<AdvanceAssignmentTemplate> advanceAssignmentTemplates = new HashSet<AdvanceAssignmentTemplate>();

    private SchedulingState schedulingState;

    public SchedulingState getSchedulingState() {
        if (schedulingState == null) {
            schedulingState = SchedulingState.createSchedulingState(
                    getSchedulingStateType(),
                    getChildrenStates(), new ITypeChangedListener() {
                        @Override
                        public void typeChanged(Type newType) {
                            schedulingStateType = newType;
                        }
                    });
        }
        return schedulingState;
    }

    private List<SchedulingState> getChildrenStates() {
        List<SchedulingState> result = new ArrayList<SchedulingState>();
        for (OrderElementTemplate each : getChildren()) {
            result.add(each.getSchedulingState());
        }
        return result;
    }

    public SchedulingState.Type getSchedulingStateType() {
        if (schedulingStateType == null) {
            schedulingStateType = Type.NO_SCHEDULED;
        }
        return schedulingStateType;
    }

    public OrderLineGroupTemplate getParent() {
        return parent;
    }

    protected void setParent(OrderLineGroupTemplate parent) {
        this.parent = parent;
    }

    @Valid
    private InfoComponent getInfoComponent() {
        if (infoComponent == null) {
            infoComponent = new InfoComponent();
        }
        return infoComponent;
    }

    /**
     * @return a description of the type or template this object is
     */
    public abstract String getType();

    public abstract List<OrderElementTemplate> getChildrenTemplates();

    @Min(0)
    public Integer getDeadlineAsDaysFromBeginning() {
        return deadlineAsDaysFromBeginning;
    }

    @Min(0)
    public Integer getStartAsDaysFromBeginning() {
        return startAsDaysFromBeginning;
    }

    public void setStartAsDaysFromBeginning(Integer days) {
        this.startAsDaysFromBeginning = days;
    }

    public void setDeadlineAsDaysFromBeginning(Integer days) {
        this.deadlineAsDaysFromBeginning = days;
    }

    public String getCode() {
        return getInfoComponent().getCode();
    }

    public void setCode(String code) {
        getInfoComponent().setCode(code);
    }

    public String getDescription() {
        return getInfoComponent().getDescription();
    }

    public void setDescription(String description) {
        getInfoComponent().setDescription(description);
    }

    public String getName() {
        return getInfoComponent().getName();
    }

    public void setName(String name) {
        getInfoComponent().setName(name);
    }

    @Override
    public OrderElementTemplate getThis() {
        return this;
    }

    protected void copyTo(OrderElementTemplate result) {
        result.setCode(getCode());
        result.setName(getName());
        result.setDescription(getDescription());
        result.setDeadlineAsDaysFromBeginning(getDeadlineAsDaysFromBeginning());
        result.setStartAsDaysFromBeginning(getStartAsDaysFromBeginning());
    }

    @Valid
    public Set<MaterialAssignmentTemplate> getMaterialAssignments() {
        return Collections.unmodifiableSet(materialAssignments);
    }

    public void addMaterialAssignment(
            MaterialAssignmentTemplate materialAssignment) {
        Validate.notNull(materialAssignment);
        materialAssignments.add(materialAssignment);
    }

    public void removeMaterialAssignment(
            MaterialAssignmentTemplate materialAssignment) {
        materialAssignments.remove(materialAssignment);
    }

    public BigDecimal getTotalMaterialAssigmentPrice() {
        BigDecimal result = BigDecimal.ZERO;
        for (MaterialAssignmentTemplate each : materialAssignments) {
            result = result.add(each.getTotalPrice());
        }
        return result;
    }

    public double getTotalMaterialAssigmentUnits() {
        double result = 0;
        for (MaterialAssignmentTemplate each : materialAssignments) {
            if (each.getUnits() != null) {
                result += each.getUnits();
            }
        }
        return result;
    }

    public abstract boolean isLeaf();

    @Valid
    public Set<Label> getLabels() {
        return Collections.unmodifiableSet(labels);
    }

    public void addLabel(Label label){
        Validate.notNull(label);
        this.labels.add(label);
    }

    public void removeLabel(Label label) {
        this.labels.remove(label);
    }

    public Set<QualityForm> getQualityForms() {
        return Collections.unmodifiableSet(qualityForms);
    }

    public void addQualityForm(QualityForm qualityForm) {
        qualityForms.add(qualityForm);
    }

    public void removeQualityForm(QualityForm qualityForm) {
        qualityForms.remove(qualityForm);
    }

    @Valid
    public Set<AdvanceAssignmentTemplate> getAdvanceAssignmentTemplates() {
        return Collections.unmodifiableSet(advanceAssignmentTemplates);
    }
}
