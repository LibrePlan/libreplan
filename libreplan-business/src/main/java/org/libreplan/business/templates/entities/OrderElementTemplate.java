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
package org.libreplan.business.templates.entities;

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
import org.hibernate.NonUniqueResultException;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.Min;
import org.hibernate.validator.Valid;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.libreplan.business.advance.entities.AdvanceAssignmentTemplate;
import org.libreplan.business.advance.entities.DirectAdvanceAssignment;
import org.libreplan.business.common.BaseEntity;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.labels.entities.Label;
import org.libreplan.business.materials.entities.MaterialAssignment;
import org.libreplan.business.materials.entities.MaterialAssignmentTemplate;
import org.libreplan.business.orders.entities.HoursGroup;
import org.libreplan.business.orders.entities.ICriterionRequirable;
import org.libreplan.business.orders.entities.InfoComponent;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.orders.entities.OrderElement;
import org.libreplan.business.orders.entities.OrderLineGroup;
import org.libreplan.business.orders.entities.SchedulingState;
import org.libreplan.business.orders.entities.SchedulingState.ITypeChangedListener;
import org.libreplan.business.orders.entities.SchedulingState.Type;
import org.libreplan.business.qualityforms.entities.QualityForm;
import org.libreplan.business.requirements.entities.CriterionRequirement;
import org.libreplan.business.requirements.entities.DirectCriterionRequirement;
import org.libreplan.business.requirements.entities.IndirectCriterionRequirement;
import org.libreplan.business.templates.daos.IOrderElementTemplateDAO;
import org.libreplan.business.trees.ITreeNode;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public abstract class OrderElementTemplate extends BaseEntity implements
        ICriterionRequirable, ITreeNode<OrderElementTemplate> {

    private static final Log LOG = LogFactory
            .getLog(OrderElementTemplate.class);

    private SchedulingState.Type schedulingStateType;

    private InfoComponent infoComponent;

    private Integer startAsDaysFromBeginning;

    private Integer deadlineAsDaysFromBeginning;

    private OrderLineGroupTemplate parent;

    private Set<CriterionRequirement> criterionRequirements = new HashSet<CriterionRequirement>();

    private Set<MaterialAssignmentTemplate> materialAssignments = new HashSet<MaterialAssignmentTemplate>();

    private Set<Label> labels = new HashSet<Label>();

    private Set<QualityForm> qualityForms = new HashSet<QualityForm>();

    private Set<AdvanceAssignmentTemplate> advanceAssignmentTemplates = new HashSet<AdvanceAssignmentTemplate>();

    private SchedulingState schedulingState;

    private OrderElement origin;

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
        beingBuilt.criterionRequirements = copyDirectCriterionRequirements(
                beingBuilt, origin.getDirectCriterionRequirement());
        beingBuilt.labels = new HashSet<Label>(origin.getLabels());
        beingBuilt.qualityForms = origin.getQualityForms();
        beingBuilt.advanceAssignmentTemplates = copyDirectAdvanceAssignments(
                beingBuilt, origin.getDirectAdvanceAssignments());
        beingBuilt.infoComponent = infoComponentCopied;
        beingBuilt.schedulingStateType = origin.getSchedulingStateType();
        assignDates(beingBuilt, fromBeginningToStart, fromBeginningToEnd);
        beingBuilt.origin = origin;
        return create(beingBuilt);
    }

    /**
     *
     * Copy only {@link DirectCriterionRequirement}
     *
     * @param beingBuilt
     * @param criterionRequirements
     * @return
     */
    private static Set<CriterionRequirement> copyDirectCriterionRequirements(OrderElementTemplate beingBuilt,
            Collection<DirectCriterionRequirement> criterionRequirements) {
        Set<CriterionRequirement> result = new HashSet<CriterionRequirement>();

        for (DirectCriterionRequirement each: criterionRequirements) {
            final DirectCriterionRequirement directCriterionRequirement = (DirectCriterionRequirement) each;
            DirectCriterionRequirement newDirectCriterionRequirement = DirectCriterionRequirement
                    .copyFrom(directCriterionRequirement, beingBuilt);

            result.add(newDirectCriterionRequirement);
        }
        return result;
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
        setupCriterionRequirements(orderElement);
        setupMaterialAssignments(orderElement);
        setupLabels(orderElement);
        setupQualityForms(orderElement);
        setupAdvances(orderElement);
        return orderElement;
    }

    protected <T extends OrderElement> T setupSchedulingStateType(T orderElement) {
        orderElement.initializeType(schedulingStateType);
        return orderElement;
    }

    protected <T extends OrderElement> T setupVersioningInfo(
            OrderLineGroup parent, T orderElement) {
        orderElement.useSchedulingDataFor(parent.getCurrentOrderVersion());
        return orderElement;
    }

    private void setupInfoComponent(OrderElement orderElement) {
        // orderElement.setCode(getCode());
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

    private void setupCriterionRequirements(OrderElement orderElement) {
        for (DirectCriterionRequirement each : getDirectCriterionRequirements()) {
            if (orderElement.canAddCriterionRequirement(each)) {
                orderElement.addCriterionRequirement(DirectCriterionRequirement
                        .copyFrom(each, orderElement));
            }
        }
    }

    private void setupMaterialAssignments(OrderElement orderElement) {
        for (MaterialAssignmentTemplate each : materialAssignments) {
            orderElement.addMaterialAssignment(each
                    .createAssignment(orderElement));
        }
    }

    private void setupLabels(OrderElement orderElement) {
        for (Label each : getLabels()) {
            if (orderElement.checkAncestorsNoOtherLabelRepeated(each)) {
                orderElement.addLabel(each);
            }
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

    public abstract OrderElement createElement(OrderLineGroup parent);

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

    public BigDecimal getTotalMaterialAssigmentUnits() {
        BigDecimal result = BigDecimal.ZERO;
        for (MaterialAssignmentTemplate each : materialAssignments) {
            if (each.getUnits() != null) {
                result = result.add(each.getUnits());
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

    public boolean isRoot() {
        return getParent() == null;
    }

    @AssertTrue(message = "template name is already being used")
    public boolean checkConstraintUniqueRootTemplateName() {
        if (getParent() != null) {
            return true;
        }

        IOrderElementTemplateDAO orderElementTemplateDAO = Registry
                .getOrderElementTemplateDAO();
        if (isNewObject()) {
            return !orderElementTemplateDAO
                    .existsRootByNameAnotherTransaction(this);
        } else {
            try {
                OrderElementTemplate template = orderElementTemplateDAO
                        .findUniqueRootByName(getName());
                return template.getId().equals(getId());
            } catch (InstanceNotFoundException e) {
                return true;
            }
            catch (NonUniqueResultException e) {
                return false;
            } catch (HibernateOptimisticLockingFailureException e) {
                return true;
            }
        }
    }

    public List<OrderElementTemplate> getAllChildren() {
        List<OrderElementTemplate> children = getChildrenTemplates();
        List<OrderElementTemplate> result = new ArrayList<OrderElementTemplate>();
        for (OrderElementTemplate orderElement : children) {
            result.add(orderElement);
            result.addAll(orderElement.getAllChildren());
        }
        return result;
    }

    @Valid
    @Override
    public Set<CriterionRequirement> getCriterionRequirements() {
        return Collections.unmodifiableSet(criterionRequirements);
    }

    public abstract List<HoursGroup> getHoursGroups();

    public abstract Integer getWorkHours();

    /**
     * Operations for manipulating CriterionRequirement
     */

    protected CriterionRequirementTemplateHandler criterionRequirementHandler =
        CriterionRequirementTemplateHandler.getInstance();

    public void setValidCriterionRequirement(IndirectCriterionRequirement requirement, boolean valid){
        requirement.setValid(valid);
        criterionRequirementHandler.propagateValidCriterionRequirement(this,
                requirement.getParent(), valid);
    }

    public void removeDirectCriterionRequirement(DirectCriterionRequirement criterionRequirement){
        criterionRequirementHandler.propagateRemoveCriterionRequirement(this,
                criterionRequirement);
        removeCriterionRequirement(criterionRequirement);

    }

    @Override
    public void removeCriterionRequirement(CriterionRequirement requirement) {
        criterionRequirements.remove(requirement);
        if (requirement instanceof IndirectCriterionRequirement) {
            ((IndirectCriterionRequirement)requirement).getParent().
                    getChildren().remove((IndirectCriterionRequirement)requirement);
        }
    }

    @Override
    public void addCriterionRequirement(
            CriterionRequirement criterionRequirement) {
        criterionRequirementHandler.addCriterionRequirement(this,
                criterionRequirement);
    }

    public void addDirectCriterionRequirement(
            CriterionRequirement criterionRequirement) {
        criterionRequirementHandler.addDirectCriterionRequirement(this, criterionRequirement);
    }

    public void addIndirectCriterionRequirement(
            IndirectCriterionRequirement criterionRequirement) {
        criterionRequirementHandler.addIndirectCriterionRequirement(this,
                criterionRequirement);
    }

    protected void basicAddCriterionRequirement(
            CriterionRequirement criterionRequirement) {
            criterionRequirement.setOrderElementTemplate(this);
            this.criterionRequirements.add(criterionRequirement);
    }

    public void updateCriterionRequirements() {
        criterionRequirementHandler.updateMyCriterionRequirements(this);
        criterionRequirementHandler.propagateUpdateCriterionRequirements(this);
    }

    public boolean canAddCriterionRequirement(
            DirectCriterionRequirement newRequirement) {
        return criterionRequirementHandler.canAddCriterionRequirement(this,
                newRequirement);
    }

    protected Set<IndirectCriterionRequirement> getIndirectCriterionRequirement() {
        return criterionRequirementHandler.getIndirectCriterionRequirement(criterionRequirements);
    }

    public Set<DirectCriterionRequirement> getDirectCriterionRequirements() {
        return criterionRequirementHandler
                .getDirectCriterionRequirement(criterionRequirements);
    }

    public Order getOrder() {
        return (parent != null) ? parent.getOrder() : null;
    }

    public OrderElement getOrigin() {
        return origin;
    }

    public void setOrigin(OrderElement origin) {
        this.origin = origin;
    }

    public abstract BigDecimal getBudget();

    public abstract boolean isOrderTemplate();

}
