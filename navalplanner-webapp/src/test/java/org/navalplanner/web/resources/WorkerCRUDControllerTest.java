package org.navalplanner.web.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.junit.Test;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.zkoss.zul.api.Window;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.verify;

/**
 * Tests for {@link WorkerCRUDController} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class WorkerCRUDControllerTest {

    private Window createWindow;
    private Window listWindow;
    private Window editWindow;

    private WorkerCRUDController createControllerForModel(
            IWorkerModel workerModel) {
        return createControllerForModel(workerModel, null);
    }

    private WorkerCRUDController createControllerForModel(
            IWorkerModel workerModel, IMessagesForUser messages) {
        createWindow = createNiceMock(Window.class);
        listWindow = createNiceMock(Window.class);
        editWindow = createNiceMock(Window.class);

        WorkerCRUDController workerCRUDController = new WorkerCRUDController(
                createWindow, listWindow, editWindow, workerModel, messages);
        return workerCRUDController;
    }

    @Test
    public void testSave() throws Exception {
        IWorkerModel workerModel = createMock(IWorkerModel.class);
        IMessagesForUser messagesForUser = createMock(IMessagesForUser.class);
        Worker workerToReturn = new Worker();

        WorkerCRUDController workerCRUDController = createControllerForModel(
                workerModel, messagesForUser);
        replay(createWindow, listWindow, editWindow);
        // expectations
        expect(workerModel.createNewInstance()).andReturn(workerToReturn);
        workerModel.save(workerToReturn);
        messagesForUser.showMessage(same(Level.INFO),
                isA(String.class));
        replay(workerModel, messagesForUser);
        // action
        workerCRUDController.goToCreateForm();
        workerToReturn.setFirstName("first");
        workerToReturn.setSurname("blabla");
        workerToReturn.setNif("11111");
        workerToReturn.setDailyHours(2);
        workerCRUDController.save();
        // verify
        verify(workerModel, messagesForUser);
    }

    @Test
    public void testGoToSaveAndThenCancel() {
        IWorkerModel workerModel = createMock(IWorkerModel.class);
        Worker workerToReturn = new Worker();
        // expectations
        WorkerCRUDController workerCRUDController = createControllerForModel(workerModel);
        expect(workerModel.createNewInstance()).andReturn(workerToReturn);
        expect(createWindow.setVisible(true)).andReturn(false);
        expect(createWindow.setVisible(false)).andReturn(true);
        expect(listWindow.setVisible(true)).andReturn(false);
        replay(createWindow, listWindow, editWindow, workerModel);
        // actions
        workerCRUDController.goToCreateForm();
        workerCRUDController.cancel();
        // verify
        verify(workerModel);
    }

    @Test
    public void testEditWorker() throws Exception {
        IWorkerModel workerModel = createMock(IWorkerModel.class);
        IMessagesForUser messagesForUser = createMock(IMessagesForUser.class);
        WorkerCRUDController workerCRUDController = createControllerForModel(
                workerModel, messagesForUser);
        List<Worker> workersToReturn = new ArrayList<Worker>(Arrays.asList(
                new Worker("firstName", "surname", "nif", 4), new Worker(
                        "firstName", "surname", "nif", 4)));
        // expectations
        expect(workerModel.getWorkers()).andReturn(workersToReturn);
        expect(editWindow.setVisible(true)).andReturn(false);
        workerModel.save(workersToReturn.get(0));
        messagesForUser.showMessage(same(Level.INFO),
                isA(String.class));
        replay(createWindow, listWindow, editWindow, workerModel,
                messagesForUser);
        // perform actions
        List<Worker> workers = workerCRUDController.getWorkers();
        assertEquals(workersToReturn, workers);
        workerCRUDController.goToEditForm(workers.get(0));
        workerCRUDController.save();
        // verify
        verify(workerModel, editWindow, messagesForUser);
    }

    @Test
    public void testWorkerInvalid() {
        IWorkerModel workerModel = createMock(IWorkerModel.class);
        IMessagesForUser messages = createMock(IMessagesForUser.class);
        WorkerCRUDController workerCRUDController = createControllerForModel(
                workerModel, messages);
        Worker workerToReturn = new Worker();
        // expectations
        expect(workerModel.createNewInstance()).andReturn(workerToReturn);
        ClassValidator<Worker> workerValidator = new ClassValidator<Worker>(
                Worker.class);
        InvalidValue[] invalidValues = workerValidator
                .getInvalidValues(workerToReturn);
        assertFalse(invalidValues.length == 0);
        messages.invalidValue(isA(InvalidValue.class));
        expectLastCall().times(invalidValues.length);
        replay(createWindow, listWindow, editWindow, workerModel, messages);
        // perform actions
        workerCRUDController.goToCreateForm();
        workerCRUDController.save();
        // verify
        verify(messages);
    }
}
