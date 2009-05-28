package org.navalplanner.web.workorders;

import java.util.List;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.workorders.entities.ProjectWork;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.api.Window;

/**
 * Controller for CRUD actions <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class ProjectWorkCRUDController extends GenericForwardComposer {

    private IProjectWorkModel projectWorkModel;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    private Component editWindow;

    private Component createWindow;

    private Component listWindow;

    private OnlyOneVisible cachedOnlyOneVisible;

    private Window confirmRemove;

    public List<ProjectWork> getProjects() {
        return projectWorkModel.getProjects();
    }

    private OnlyOneVisible getVisibility() {
        if (cachedOnlyOneVisible == null) {
            cachedOnlyOneVisible = new OnlyOneVisible(listWindow, editWindow,
                    createWindow);
        }
        return cachedOnlyOneVisible;
    }

    public ProjectWork getProject() {
        return projectWorkModel.getProject();
    }

    public void save() {
        try {
            projectWorkModel.save();
            messagesForUser.showMessage(Level.INFO, "proxecto gardado");
            goToList();
        } catch (ValidationException e) {
            messagesForUser.showInvalidValues(e);
        }
    }

    private void goToList() {
        Util.reloadBindings(listWindow);
        getVisibility().showOnly(listWindow);
    }

    public void cancel() {
        goToList();
    }

    public void confirmRemove(ProjectWork project) {
        projectWorkModel.prepareForRemove(project);
        showConfirmingWindow();
    }

    public void cancelRemove() {
        confirmingRemove = false;
        confirmRemove.setVisible(false);
        Util.reloadBindings(confirmRemove);
    }

    private boolean confirmingRemove = false;

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

    public void goToEditForm(ProjectWork project) {
        projectWorkModel.prepareEditFor(project);
        getVisibility().showOnly(editWindow);
        Util.reloadBindings(editWindow);
    }

    public void remove(ProjectWork projectWork) {
        projectWorkModel.remove(projectWork);
        hideConfirmingWindow();
        Util.reloadBindings(listWindow);
        messagesForUser.showMessage(Level.INFO, "removed "
                + projectWork.getName());
    }

    public void goToCreateForm() {
        projectWorkModel.prepareForCreate();
        getVisibility().showOnly(createWindow);
        Util.reloadBindings(createWindow);
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        messagesForUser = new MessagesForUser(messagesContainer);
        comp.setVariable("controller", this, true);
        getVisibility().showOnly(listWindow);
    }

}
