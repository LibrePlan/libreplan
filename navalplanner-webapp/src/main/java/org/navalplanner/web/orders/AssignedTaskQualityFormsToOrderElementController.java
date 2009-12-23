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

import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.navalplanner.business.orders.entities.OrderElement;
import org.navalplanner.business.qualityforms.entities.QualityForm;
import org.navalplanner.business.qualityforms.entities.TaskQualityForm;
import org.navalplanner.business.qualityforms.entities.TaskQualityFormItem;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.Autocomplete;
import org.navalplanner.web.common.components.bandboxsearch.BandboxSearch;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Messagebox;

/**
 * Controller for showing OrderElement assigned task quality forms
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class AssignedTaskQualityFormsToOrderElementController extends
        GenericForwardComposer {

    private static final org.apache.commons.logging.Log LOG = LogFactory
            .getLog(AssignedTaskQualityFormsToOrderElementController.class);

    private IMessagesForUser messagesForUser;

    private static final String ITEM = "item";

    private IAssignedTaskQualityFormsToOrderElementModel assignedTaskQualityFormsToOrderElementModel;

    private Autocomplete cbQualityForms;

    private Grid assignedTaskQualityForms;

    private Grid editedTaskQualityFormItems;

    private BandboxSearch bdQualityForms;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("assignedTaskQualityFormsController", this, true);
    }

    public void openWindow(IOrderElementModel orderElementModel) {
        setOrderElementModel(orderElementModel);
        openWindow(getOrderElement());
    }

    private void openWindow(OrderElement orderElement) {
        assignedTaskQualityFormsToOrderElementModel.init(orderElement);

        // Clear components
        bdQualityForms.clear();
        Util.reloadBindings(self);
        reloadTaskQualityForms();
    }

    IOrderElementModel orderElementModel;

    public void setOrderElementModel(IOrderElementModel orderElementModel) {
        this.orderElementModel = orderElementModel;
        setOrderElement(orderElementModel.getOrderElement());
        setOrderModel(orderElementModel.getOrderModel());
    }

    private void setOrderModel(IOrderModel orderModel) {
        if (assignedTaskQualityFormsToOrderElementModel != null) {
            assignedTaskQualityFormsToOrderElementModel
                    .setOrderModel(orderModel);
        }
    }

    public OrderElement getOrderElement() {
        return orderElementModel.getOrderElement();
    }

    public void setOrderElement(OrderElement orderElement) {
        if (assignedTaskQualityFormsToOrderElementModel != null) {
            assignedTaskQualityFormsToOrderElementModel
                    .setOrderElement(orderElement);
        }
    }

    /**
     * Executed on pressing Assign button Adds selected quality form to task
     * quality form list
     */
    public void onAssignTaskQualityForm() {
        QualityForm qualityForm = (QualityForm) bdQualityForms
                .getSelectedElement();
        if (qualityForm == null) {
            throw new WrongValueException(bdQualityForms,
                    _("please, select a quality form"));
        }
        if (isAssigned(qualityForm)) {
            throw new WrongValueException(bdQualityForms, _("already assigned"));
        }
        assignQualityForm(qualityForm);
        bdQualityForms.clear();
    }

    public void clear() {

    }

    private void assignQualityForm(QualityForm qualityForm) {
        assignedTaskQualityFormsToOrderElementModel
                .assignTaskQualityForm(qualityForm);
        reloadTaskQualityForms();
    }

    private boolean isAssigned(QualityForm qualityForm) {
        return assignedTaskQualityFormsToOrderElementModel
                .isAssigned(qualityForm);
    }

    public void confirmRemove(TaskQualityForm taskQualityForm) {
        try {
            int status = Messagebox.show(_(
                    "Confirm deleting {0}. Are you sure?",
                    getTaskQualityFormName(taskQualityForm)), _("Delete"),
                    Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION);
            if (Messagebox.OK == status) {
                deleteTaskQualityForm(taskQualityForm);
            }
        } catch (InterruptedException e) {
            messagesForUser.showMessage(Level.ERROR, e.getMessage());
            LOG.error(_("Error on showing removing element: ", taskQualityForm
                    .getId()), e);
        }
    }

    private String getTaskQualityFormName(TaskQualityForm taskQualityForm) {
        if (taskQualityForm == null || taskQualityForm.getQualityForm() == null) {
            return ITEM;
        }
        return taskQualityForm.getQualityForm().getName();
    }

    public void deleteTaskQualityForm(TaskQualityForm taskQualityForm) {
        assignedTaskQualityFormsToOrderElementModel
                .deleteTaskQualityForm(taskQualityForm);
        reloadTaskQualityForms();
    }

    public void editTaskQualityForm(TaskQualityForm taskQualityForm) {
        assignedTaskQualityFormsToOrderElementModel
                .setTaskQualityForm(taskQualityForm);
        Util.reloadBindings(editedTaskQualityFormItems);
    }

    public List<TaskQualityForm> getTaskQualityForms() {
        return assignedTaskQualityFormsToOrderElementModel
                .getTaskQualityForms();
    }

    public List<QualityForm> getNotAssignedQualityForms() {
        return assignedTaskQualityFormsToOrderElementModel
                .getNotAssignedQualityForms();
    }

    public List<TaskQualityFormItem> getTaskQualityFormItems() {
        return assignedTaskQualityFormsToOrderElementModel
                .getTaskQualityFormItems();
    }

    private void reloadTaskQualityForms() {
        Util.reloadBindings(bdQualityForms);
        Util.reloadBindings(assignedTaskQualityForms);
        assignedTaskQualityFormsToOrderElementModel
                .clearEditTaskQualityFormItems();
        Util.reloadBindings(editedTaskQualityFormItems);
    }

    public void close() {

    }
}
