package org.navalplanner.web.workreports;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.CriterionType;
import org.navalplanner.business.workreports.entities.WorkReportType;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.navalplanner.web.common.components.TwoWaySelector;
import org.navalplanner.web.common.entrypoints.IURLHandlerRegistry;
import org.navalplanner.web.common.entrypoints.URLHandler;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.api.Window;

/**
 * Controller for CRUD actions over a {@link WorkReportType}
 *
 * @author Manuel Rego Casasnovas <mrego@igalia.com>
 */
public class WorkReportTypeCRUDController extends GenericForwardComposer
        implements IWorkReportTypeCRUDControllerEntryPoints {

    private Window listWindow;

    private Window createWindow;

    private Window editWindow;

    private Window confirmRemove;

    private boolean confirmingRemove = false;

    private IWorkReportTypeModel workReportTypeModel;

    private OnlyOneVisible visibility;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private IWorkReportCRUDControllerEntryPoints workReportCRUD;

    private IURLHandlerRegistry URLHandlerRegistry;

    public List<WorkReportType> getWorkReportTypes() {
        return workReportTypeModel.getWorkReportTypes();
    }

    public WorkReportType getWorkReportType() {
        return workReportTypeModel.getWorkReportType();
    }

    public Set<CriterionType> getAssignedCriterionTypes() {
        WorkReportType workReportType = getWorkReportType();
        if (workReportType == null) {
            return new HashSet<CriterionType>();
        }

        Set<CriterionType> criterionTypes = workReportType.getCriterionTypes();
        if (criterionTypes == null) {
            return new HashSet<CriterionType>();
        }

        return criterionTypes;
    }

    public Set<CriterionType> getUnassignedCriterionTypes() {
        Set<CriterionType> criterionTypes = workReportTypeModel
                .getCriterionTypes();
        Set<CriterionType> assignedCriterionTypes = getAssignedCriterionTypes();
        if (assignedCriterionTypes != null) {
            criterionTypes.removeAll(assignedCriterionTypes);
        }
        return criterionTypes;
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        messagesForUser = new MessagesForUser(messagesContainer);
        comp.setVariable("controller", this, true);
        final URLHandler<IWorkReportTypeCRUDControllerEntryPoints> handler = URLHandlerRegistry
                .getRedirectorFor(IWorkReportTypeCRUDControllerEntryPoints.class);
        handler.registerListener(this, page);
        getVisibility().showOnly(listWindow);
    }

    public void cancel() {
        goToList();
    }

    public void goToList() {
        Util.reloadBindings(listWindow);
        getVisibility().showOnly(listWindow);
    }

    public void goToEditForm(WorkReportType workReportType) {
        workReportTypeModel.initEdit(workReportType);
        getVisibility().showOnly(editWindow);
        Util.reloadBindings(editWindow);
    }

    public void save() {
        try {
            Set<CriterionType> criterionTypes = getCriterionTypesSelector()
                    .getAssignedObjects();

            workReportTypeModel.setCriterionTypes(criterionTypes);
            workReportTypeModel.save();
            messagesForUser.showMessage(Level.INFO, "work report type saved");
            goToList();
        } catch (ValidationException e) {
            messagesForUser.showInvalidValues(e);
        }
    }

    private TwoWaySelector getCriterionTypesSelector() {
        if (workReportTypeModel.isEditing()) {
            return (TwoWaySelector) editWindow
                    .getFellow("criterionTypesSelector");
        } else {
            return (TwoWaySelector) createWindow
                    .getFellow("criterionTypesSelector");
        }
    }

    public void confirmRemove(WorkReportType workReportType) {
        workReportTypeModel.prepareForRemove(workReportType);
        showConfirmingWindow();
    }

    public void cancelRemove() {
        confirmingRemove = false;
        confirmRemove.setVisible(false);
        Util.reloadBindings(confirmRemove);
    }

    public boolean isConfirmingRemove() {
        return confirmingRemove;
    }

    private void hideConfirmingWindow() {
        confirmingRemove = false;
        Util.reloadBindings(confirmRemove);
    }

    private void showConfirmingWindow() {
        confirmingRemove = true;
        try {
            Util.reloadBindings(confirmRemove);
            confirmRemove.doModal();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void remove(WorkReportType workReportType) {
        workReportTypeModel.remove(workReportType);
        hideConfirmingWindow();
        Util.reloadBindings(listWindow);
        messagesForUser.showMessage(Level.INFO, "removed "
                + workReportType.getName());
    }

    public void goToEditForm() {
        getVisibility().showOnly(editWindow);
        Util.reloadBindings(editWindow);
    }

    public void goToCreateForm() {
        workReportTypeModel.prepareForCreate();
        getVisibility().showOnly(createWindow);
        Util.reloadBindings(createWindow);
    }

    private OnlyOneVisible getVisibility() {
        if (visibility == null) {
            visibility = new OnlyOneVisible(listWindow, createWindow,
                    editWindow);
        }
        return visibility;
    }

    public void goToEditNewWorkReportForm(WorkReportType workReportType) {
        workReportCRUD.goToCreateForm(workReportType);
    }

}
