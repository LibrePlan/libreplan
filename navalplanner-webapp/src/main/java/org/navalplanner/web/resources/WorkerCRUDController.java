package org.navalplanner.web.resources;

import java.util.List;

import org.hibernate.validator.InvalidValue;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.api.Window;

/**
 * Controller for {@link Worker} resource <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class WorkerCRUDController extends GenericForwardComposer {

    private Window createWindow;

    private Window listWindow;

    private Window editWindow;

    private IWorkerModel workerModel;

    private OnlyOneVisible visibility;

    private IMessagesForUser messages;

    private Component messagesContainer;

    public WorkerCRUDController() {
    }

    public WorkerCRUDController(Window createWindow, Window listWindow,
            Window editWindow, IWorkerModel workerModel,
            IMessagesForUser messages) {
        this.createWindow = createWindow;
        this.listWindow = listWindow;
        this.editWindow = editWindow;
        this.workerModel = workerModel;
        this.messages = messages;
    }

    public Worker getWorker() {
        return workerModel.getWorker();
    }

    public List<Worker> getWorkers() {
        return workerModel.getWorkers();
    }

    public void save() {
        try {
            workerModel.save();
            getVisibility().showOnly(listWindow);
            Util.reloadBindings(listWindow);
            messages.showMessage(Level.INFO, "traballador gardado");
        } catch (ValidationException e) {
            for (InvalidValue invalidValue : e.getInvalidValues()) {
                messages.invalidValue(invalidValue);
            }
        }
    }

    public void cancel() {
        getVisibility().showOnly(listWindow);
    }

    public void goToEditForm(Worker worker) {
        workerModel.prepareEditFor(worker);
        getVisibility().showOnly(editWindow);
        Util.reloadBindings(editWindow);
    }

    public void goToCreateForm() {
        workerModel.prepareForCreate();
        getVisibility().showOnly(createWindow);
        Util.reloadBindings(createWindow);
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
        getVisibility().showOnly(listWindow);
        if (messagesContainer == null)
            throw new RuntimeException("messagesContainer is needed");
        messages = new MessagesForUser(messagesContainer);
    }

    private OnlyOneVisible getVisibility() {
        if (visibility == null) {
            visibility = new OnlyOneVisible(listWindow, editWindow,
                    createWindow);
        }
        return visibility;
    }

}
