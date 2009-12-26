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

package org.navalplanner.web.orders;

import static org.navalplanner.web.I18nHelper._;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.orders.entities.HoursGroup;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.orders.entities.OrderLine;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.resources.entities.CriterionWithItsType;
import org.navalplanner.business.resources.entities.ResourceEnum;
import org.navalplanner.business.workreports.entities.WorkReportLine;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.NewDataSortableGrid;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Vbox;

/**
 * Controller for showing OrderElement assigned labels
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class AssignedCriterionRequirementToOrderElementController extends
        GenericForwardComposer {

    private static final org.apache.commons.logging.Log LOG = LogFactory
            .getLog(OrderCRUDController.class);

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private IAssignedCriterionRequirementToOrderElementModel assignedCriterionRequirementToOrderElementModel;

    private Vbox vboxCriterionRequirementsAndHoursGroups;

    private Listbox hoursGroupsInOrderLineGroup;

    List<ResourceEnum> listResourceTypes = new ArrayList<ResourceEnum>();

    private NewDataSortableGrid listingRequirements;

    private NewDataSortableGrid listHoursGroups;

    private Intbox orderElementTotalHours;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        messagesForUser = new MessagesForUser(messagesContainer);
        comp.setVariable("assignedCriterionRequirementController", this, true);
        vboxCriterionRequirementsAndHoursGroups = (Vbox) comp;

        // init the resorcesType
        listResourceTypes.add(ResourceEnum.MACHINE);
        listResourceTypes.add(ResourceEnum.WORKER);
    }

    public OrderElement getOrderElement() {
        return assignedCriterionRequirementToOrderElementModel.getOrderElement();
    }

    public IOrderModel getOrderModel() {
        return assignedCriterionRequirementToOrderElementModel.getOrderModel();
    }

    public Set<CriterionType> getCriterionTypes() {
        return assignedCriterionRequirementToOrderElementModel.getTypes();
    }

    public void setOrderElement(OrderElement orderElement) {
        assignedCriterionRequirementToOrderElementModel.setOrderElement(orderElement);
    }

    public void openWindow(IOrderElementModel orderElementModel) {
        assignedCriterionRequirementToOrderElementModel.setOrderModel(orderElementModel
                .getOrderModel());
        openWindow(orderElementModel.getOrderElement());
    }

    public void openWindow(OrderElement orderElement) {
        assignedCriterionRequirementToOrderElementModel.init(orderElement);
        Util.reloadBindings(vboxCriterionRequirementsAndHoursGroups);
    }

    public boolean close() {
        if (showInvalidValues()) {
            return false;
        }
        assignedCriterionRequirementToOrderElementModel.confirm();
        return true;
    }

    public List<CriterionRequirementWrapper> criterionRequirementWrappers() {
         return assignedCriterionRequirementToOrderElementModel.
getCriterionRequirementWrappers();
    }

    public List<CriterionWithItsType> getCriterionWithItsTypes(){
        return assignedCriterionRequirementToOrderElementModel.getCriterionWithItsTypes();
    }

    public List<CriterionWithItsType> getCriterionWithItsTypesWorker() {
        List<CriterionWithItsType> result = new ArrayList<CriterionWithItsType>();
        for (CriterionWithItsType criterionAndType : assignedCriterionRequirementToOrderElementModel
                .getCriterionWithItsTypes()) {
            if (!criterionAndType.getCriterion().getType().getResource()
                    .equals(ResourceEnum.MACHINE)) {
                result.add(criterionAndType);
            }
        }
        return result;
    }

    public List<CriterionWithItsType> getCriterionWithItsTypesMachine() {
        List<CriterionWithItsType> result = new ArrayList<CriterionWithItsType>();
        for (CriterionWithItsType criterionAndType : assignedCriterionRequirementToOrderElementModel
                .getCriterionWithItsTypes()) {
            if (!criterionAndType.getCriterion().getType().getResource()
                    .equals(ResourceEnum.WORKER)) {
                result.add(criterionAndType);
            }
        }
        return result;
    }

    public List<ResourceEnum> getResourceTypes() {
        return listResourceTypes;
    }

    public void addCriterionRequirementWrapper() {
        assignedCriterionRequirementToOrderElementModel
                .assignCriterionRequirementWrapper();
        reload();
    }

    public void remove(CriterionRequirementWrapper requirement){
        assignedCriterionRequirementToOrderElementModel.
deleteCriterionRequirementWrapper(requirement);
        reload();
    }

    public void invalidate(CriterionRequirementWrapper requirement){
        assignedCriterionRequirementToOrderElementModel.
setValidCriterionRequirementWrapper(requirement, false);
        reload();
    }

    public void validate(CriterionRequirementWrapper requirement){
        assignedCriterionRequirementToOrderElementModel.
setValidCriterionRequirementWrapper(requirement, true);
        reload();
    }

    public void selectCriterionAndType(Listitem item,Bandbox bandbox,
            CriterionRequirementWrapper requirementWrapper) {
        if(item != null){
            CriterionWithItsType newCriterionAndType = (CriterionWithItsType) item
                    .getValue();
            try {
                bandbox.close();
                bandbox.setValue(newCriterionAndType.getNameAndType());
                assignedCriterionRequirementToOrderElementModel
                        .changeCriterionAndType(requirementWrapper,
                                newCriterionAndType);
            } catch (IllegalStateException e) {
                showInvalidConstraint(bandbox, e);
                requirementWrapper.setCriterionWithItsType(null);
            }
            Util.reloadBindings(listHoursGroups);
        }else{
            bandbox.setValue("");
        }
    }

    public void selectResourceType(Combobox combobox)
            throws InterruptedException {
        HoursGroupWrapper hoursGroupWrapper = (HoursGroupWrapper) ((Row) combobox
                .getParent()).getValue();

        try {
            int status = Messagebox
                    .show(
                            _("Are you sure of changing the resource type? You will lose the criterions with different resource type."),
                            "Question", Messagebox.OK | Messagebox.CANCEL,
                            Messagebox.QUESTION);

            if (Messagebox.OK == status) {
                ResourceEnum resource = (ResourceEnum) combobox
                        .getSelectedItem().getValue();
                hoursGroupWrapper.assignResourceType(resource);
                assignedCriterionRequirementToOrderElementModel
                        .updateCriterionsWithDiferentResourceType(hoursGroupWrapper);
            }
        } catch (InterruptedException e) {
            messagesForUser.showMessage(Level.ERROR, e.getMessage());
            LOG.error(_("Error on showing removing element: ",
                    hoursGroupWrapper.getHoursGroup().getId()), e);
        }
        Util.reloadBindings(listHoursGroups);
    }

    public void reload() {
        Util.reloadBindings(listingRequirements);
        Util.reloadBindings(orderElementTotalHours);
        if (isReadOnly()) {
            Util.reloadBindings(hoursGroupsInOrderLineGroup);
        } else {
            Util.reloadBindings(listHoursGroups);
        }
    }

    private boolean showInvalidValues() {
        CriterionRequirementWrapper invalidWrapper = assignedCriterionRequirementToOrderElementModel
                .validateWrappers(criterionRequirementWrappers());
        if (invalidWrapper != null) {
            showInvalidValues(invalidWrapper);
            return true;
        }

        CriterionRequirementWrapper invalidHoursGroupWrapper = assignedCriterionRequirementToOrderElementModel
                .validateHoursGroupWrappers();
        if (invalidHoursGroupWrapper != null) {
            showInvalidValuesInHoursGroups(invalidHoursGroupWrapper);
            return true;
        }
        return false;
    }

    // Show invalid values inside listhoursGroup.
    private void showInvalidValuesInHoursGroups(
            CriterionRequirementWrapper requirementWrapper) {
        if (listHoursGroups != null) {
            List<Row> listRowsHoursGroup = (List<Row>) ((Rows) listHoursGroups
                    .getRows())
                    .getChildren();
            for (Row row : listRowsHoursGroup) {
                Rows listRequirementRows = getRequirementRows(row);
                Row requirementRow = findRowOfCriterionRequirementWrapper(
                        listRequirementRows, requirementWrapper);
                showInvalidValue(requirementRow, requirementWrapper);
            }
        }
    }
    /**
     * Validates {@link CriterionRequirementWrapper} data constraints
     * @param invalidValue
     */
    private void showInvalidValues(
            CriterionRequirementWrapper requirementWrapper) {
        if(listingRequirements != null){
            // Find which listItem contains CriterionSatisfaction inside listBox
            Row row = findRowOfCriterionRequirementWrapper(listingRequirements
                    .getRows(), requirementWrapper);
            showInvalidValue(row, requirementWrapper);
        }
    }

    private void showInvalidValue(Row row,
            CriterionRequirementWrapper requirementWrapper) {
        if (row != null) {
            Bandbox bandType = getBandType(requirementWrapper, row);
            bandType.setValue(null);
            throw new WrongValueException(bandType,
                    _("The criterion and its type cannot be null"));
        }
    }

    /**
     * Locates which {@link row} is bound to {@link WorkReportLine} in rows
     * @param Rows
     * @param CriterionRequirementWrapper
     * @return
     */

    private Row findRowOfCriterionRequirementWrapper(Rows rows,
            CriterionRequirementWrapper requirementWrapper) {
        List<Row> listRows = (List<Row>) rows.getChildren();
        for (Row row : listRows) {
            if (requirementWrapper.equals(row.getValue())) {
                return row;
            }
        }
        return null;
    }

     /**
     * Locates {@link Bandbox} criterion requirement in {@link row}
     *
     * @param row
     * @return Bandbox
     */
    private Bandbox getBandType(CriterionRequirementWrapper wrapper, Row row) {
        if (wrapper.isNewDirectAndItsHoursGroupIsMachine()) {
            return (Bandbox) ((Hbox) row.getChildren().get(0)).getChildren()
                    .get(1);
        }
        if (wrapper.isNewException()) {
            return (Bandbox) ((Hbox) row.getChildren().get(0)).getChildren()
                    .get(2);
        }
        return (Bandbox)((Hbox) row.getChildren().get(0))
                .getChildren().get(0);
    }

    private Rows getRequirementRows(Row row) {
        Panel panel = (Panel) row.getFirstChild().getFirstChild();
        NewDataSortableGrid grid = (NewDataSortableGrid) panel.getFirstChild()
                .getFirstChild();
        return grid.getRows();
    }

    private HoursGroupWrapper getHoursGroupOfRequirementWrapper(
            Row rowRequirement) {
        NewDataSortableGrid grid = (NewDataSortableGrid) rowRequirement
                .getParent().getParent();
        Panel panel = (Panel) grid.getParent().getParent();
        return (HoursGroupWrapper) ((Row) panel.getParent().getParent())
                .getValue();
    }
    /*
     * Operations to manage OrderElement's hoursGroups and to assign criterion
     * requirements to this hoursGroups.
     */

    public boolean isReadOnly() {
        return !isEditableHoursGroup();
    }

    public boolean isEditableHoursGroup() {
        if (getOrderElement() != null) {
            if (getOrderElement() instanceof OrderLine)
                return true;
        }
        return false;
    }

    public List<HoursGroupWrapper> getHoursGroupWrappers() {
            return assignedCriterionRequirementToOrderElementModel
                .getHoursGroupsWrappers();
    }
    /**
     * Adds a new {@link HoursGroup} to the current {@link OrderElement} The
     * {@link OrderElement} should be a {@link OrderLine}
     */
    public void addHoursGroup() {
        assignedCriterionRequirementToOrderElementModel
                .addNewHoursGroupWrapper();
        Util.reloadBindings(listHoursGroups);
    }

    /**
     * Deletes the selected {@link HoursGroup} for the current
     * {@link OrderElement} The {@link OrderElement} should be a
     * {@link OrderLine}
     */
    public void deleteHoursGroups(Component self) throws InterruptedException {
        if (getHoursGroupWrappers().size() < 2) {
            Messagebox.show(_("At least one HoursGroup is needed"), _("Error"), Messagebox.OK,
                    Messagebox.ERROR);
            return;
        }else{
            HoursGroupWrapper hoursGroupWrapper = getHoursGroupWrapper(self);
            if (hoursGroupWrapper != null) {
                assignedCriterionRequirementToOrderElementModel
                        .deleteHoursGroupWrapper(hoursGroupWrapper);
                Util.reloadBindings(listHoursGroups);
            }
        }
    }

    public void addCriterionToHoursGroup(Component self) {
        HoursGroupWrapper hoursGroupWrapper = getHoursGroupWrapper(self);
        if (hoursGroupWrapper != null) {
            assignedCriterionRequirementToOrderElementModel
                    .addCriterionToHoursGroupWrapper(hoursGroupWrapper);
            Util.reloadBindings(listHoursGroups);
        }
    }

    public void addExceptionToHoursGroups(Component self) {
        HoursGroupWrapper hoursGroupWrapper = getHoursGroupWrapper(self);
        if (hoursGroupWrapper != null) {
            assignedCriterionRequirementToOrderElementModel
                    .addExceptionToHoursGroupWrapper(hoursGroupWrapper);
            Util.reloadBindings(listHoursGroups);
        }
    }

    public void removeCriterionToHoursGroup(Component self){
        try {
            Row row = (Row) self.getParent().getParent();
            CriterionRequirementWrapper requirementWrapper = (CriterionRequirementWrapper) row.getValue();
            HoursGroupWrapper hoursGroupWrapper = getHoursGroupOfRequirementWrapper(row);

            assignedCriterionRequirementToOrderElementModel
                    .deleteCriterionToHoursGroup(hoursGroupWrapper,
                            requirementWrapper);
            Util.reloadBindings(listHoursGroups);
        } catch (Exception e) {
        }
    }

    public void selectCriterionToHoursGroup(Listitem item, Bandbox bandbox,
            CriterionRequirementWrapper requirementWrapper) {
        if (item != null) {

            Row row = (Row) bandbox.getParent().getParent();
            CriterionWithItsType criterionAndType = (CriterionWithItsType) item
                    .getValue();
            HoursGroupWrapper hoursGroupWrapper = getHoursGroupOfRequirementWrapper(row);

            bandbox.close();
            bandbox.setValue(criterionAndType.getNameAndType());

            try{
                assignedCriterionRequirementToOrderElementModel.selectCriterionToHoursGroup(hoursGroupWrapper,
                            requirementWrapper, criterionAndType);
            }catch(IllegalStateException e){
                requirementWrapper.setCriterionWithItsType(null);
                showInvalidConstraint(bandbox, e);
            }
            Util.reloadBindings(listHoursGroups);
        } else {
            bandbox.setValue("");
        }
    }

    private void showInvalidConstraint(Bandbox bandbox, IllegalStateException e) {
        bandbox.setValue("");
        throw new WrongValueException(bandbox, _(e.getMessage()));
    }

    private HoursGroupWrapper getHoursGroupWrapper(Component self) {
        try {
            return ((HoursGroupWrapper) (((Row) (self.getParent().getParent()))
                    .getValue()));
        } catch (Exception e) {
            return null;
        }
    }

    /*
     * Operations to manage the data hoursGroup, for example validate the
     * percentage and its number of hours or set the fixed percentage
     */

    public void changeTotalHours() {
        recalculateHoursGroup();
    }

    public Constraint validateTotalHours() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                if (value == null) {
                    value = new Integer(0);
                    orderElementTotalHours.setValue((Integer) value);
                }
                try {
                    if (getOrderElement() instanceof OrderLine) {
                        ((OrderLine) getOrderElement())
                            .setWorkHours((Integer) value);
                    }
                } catch (IllegalArgumentException e) {
                    throw new WrongValueException(comp, _(e.getMessage()));
                }
            }
        };
    }

    public Constraint validatePercentage() {
        return new Constraint() {
            @Override
            public void validate(Component comp, Object value)
                    throws WrongValueException {
                HoursGroupWrapper hoursGroupWrapper = (HoursGroupWrapper) ((Row) comp
                        .getParent()).getValue();
                try {
                    hoursGroupWrapper.setPercentage((BigDecimal) value);
                } catch (IllegalArgumentException e) {
                    throw new WrongValueException(comp, _(e.getMessage()));
                }
            }
        };
    }

    public void recalculateHoursGroup() {
        ((OrderLine) assignedCriterionRequirementToOrderElementModel
                .getOrderElement()).recalculateHoursGroups();
        Util.reloadBindings(listHoursGroups);
        Util.reloadBindings(orderElementTotalHours);
    }

/*Operations to return the agrouped list of hours Group */

    /**
     * Returns a {@link List} of {@link HoursGroup}. If the current element is
     * an {@link OrderLine} this method just returns the {@link HoursGroup} of
     * this {@link OrderLine}. Otherwise, this method gets all the
     * {@link HoursGroup} of all the children {@link OrderElement}, and
     * aggregates them if they have the same {@link Criterion}.
     * @return The {@link HoursGroup} list of the current {@link OrderElement}
     */
    public List<HoursGroup> getHoursGroups() {

        if ((getOrderElement() == null)
                || (assignedCriterionRequirementToOrderElementModel == null)) {
            return new ArrayList<HoursGroup>();
        }

        // Creates a map in order to join HoursGroup with the same
        // Criterions.
        Map<Map<ResourceEnum, Set<Criterion>>, HoursGroup> map = new HashMap<Map<ResourceEnum, Set<Criterion>>, HoursGroup>();

        List<HoursGroup> hoursGroups = getOrderElement().getHoursGroups();
        for (HoursGroup hoursGroup : hoursGroups) {
            Map<ResourceEnum, Set<Criterion>> key = getKeyFor(hoursGroup);

            HoursGroup hoursGroupAggregation = map.get(key);
            if (hoursGroupAggregation == null) {
                // This is not a real HoursGroup element, it's just an
                // aggregation that join HoursGroup with the same Criterions
                hoursGroupAggregation = new HoursGroup();
                hoursGroupAggregation.setWorkingHours(hoursGroup
                        .getWorkingHours());
                hoursGroupAggregation.setCriterionRequirements(hoursGroup
                        .getCriterionRequirements());
                hoursGroupAggregation.setResourceType(hoursGroup
                        .getResourceType());
            } else {
                Integer newHours = hoursGroupAggregation.getWorkingHours()
                        + hoursGroup.getWorkingHours();
                hoursGroupAggregation.setWorkingHours(newHours);
            }

            map.put(key, hoursGroupAggregation);
        }
        return new ArrayList<HoursGroup>(map.values());
    }

    private Map<ResourceEnum, Set<Criterion>> getKeyFor(HoursGroup hoursGroup) {
        Map<ResourceEnum, Set<Criterion>> keys = new HashMap<ResourceEnum, Set<Criterion>>();
        ResourceEnum resourceType = hoursGroup.getResourceType();
        Set<Criterion> criterions = getKeyCriterionsFor(hoursGroup);
        keys.put(resourceType, criterions);
        return keys;
    }

    private Set<Criterion> getKeyCriterionsFor(HoursGroup hoursGroup) {
        Set<Criterion> key = new HashSet<Criterion>();
        for (Criterion criterion : hoursGroup.getValidCriterions()) {
            if (criterion != null) {
                key.add(criterion);
            }
        }
        return key;
    }

    private transient ListitemRenderer renderer = new HoursGroupListitemRender();

    public ListitemRenderer getRenderer() {
            return renderer;
    }

   public class HoursGroupListitemRender implements ListitemRenderer{

        @Override
        public void render(Listitem item, Object data) throws Exception {
            final HoursGroup hoursGroup = (HoursGroup) data;

            // Criterion Requirements hours Group
            Listcell cellCriterionRequirements = new Listcell();
            cellCriterionRequirements.setParent(item);
            cellCriterionRequirements.appendChild( appendRequirements(hoursGroup));

            // Type hours Group
            Listcell cellType = new Listcell();
            cellType.setParent(item);
            cellType.appendChild(appendType(hoursGroup));

            // Working hours
            Listcell cellWorkingHours = new Listcell();
            cellWorkingHours.setParent(item);
            cellWorkingHours.appendChild(appendWorkingHours(hoursGroup));
        }
    }

   private Label appendRequirements(final HoursGroup hoursGroup) {
       Label requirementsLabel = new Label();
       requirementsLabel.setMultiline(true);
       requirementsLabel.setValue(getLabelRequirements(hoursGroup));
       return requirementsLabel;
   }

   private Label appendType(final HoursGroup hoursGroup) {
       Label type = new Label();
       type.setValue(hoursGroup.getResourceType().toString());
       return type;
   }

   private Label appendWorkingHours(final HoursGroup hoursGroup) {
        Listheader list = new Listheader();
       Label workingHoursLabel = new Label();
       workingHoursLabel
               .setValue(String.valueOf(hoursGroup.getWorkingHours()));
       return workingHoursLabel;
   }

    private String getLabelRequirements(HoursGroup hoursGroup) {
        String label = "";
        for (Criterion criterion : hoursGroup.getValidCriterions()) {
            if (!label.equals("")) {
                label = label.concat(", ");
            }
            label = label.concat(criterion.getName());
        }
        if (!label.equals("")) {
            label = label.concat(".");
        }
        return label;
    }

    public boolean isCodeAutogenerated() {
        return assignedCriterionRequirementToOrderElementModel
                .isCodeAutogenerated();
    }

}