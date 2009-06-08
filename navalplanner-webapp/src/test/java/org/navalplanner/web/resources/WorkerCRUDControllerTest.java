package org.navalplanner.web.resources;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.Level;
import org.navalplanner.web.resources.worker.IWorkerCRUDControllerEntryPoints;
import org.navalplanner.web.resources.worker.IWorkerModel;
import org.navalplanner.web.resources.worker.WorkerCRUDController;
import org.zkoss.zul.api.Window;

/**
 * Tests for {@link WorkerCRUDController} <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class WorkerCRUDControllerTest {

    private Window createWindow;
    private Window listWindow;
    private Window editWindow;
    private Window workRelationshipsWindow;
    private Window addWorkRelationshipWindow;
    private Window editWorkRelationshipWindow;

    private WorkerCRUDController createControllerForModel(
            IWorkerModel workerModel) {
        return createControllerForModel(workerModel, null);
    }

    private WorkerCRUDController createControllerForModel(
            IWorkerModel workerModel, IMessagesForUser messages) {
        createWindow = createNiceMock(Window.class);
        listWindow = createNiceMock(Window.class);
        editWindow = createNiceMock(Window.class);
        workRelationshipsWindow = createNiceMock(Window.class);
        addWorkRelationshipWindow = createNiceMock(Window.class);
        editWorkRelationshipWindow = createNiceMock(Window.class);
        WorkerCRUDController workerCRUDController = new WorkerCRUDController(
                createWindow, listWindow, editWindow, workRelationshipsWindow,
                addWorkRelationshipWindow, editWorkRelationshipWindow,
                workerModel, messages, createNiceMock(IWorkerCRUDControllerEntryPoints.class));
        return workerCRUDController;

    }

    @Test
    public void testSave() throws Exception {
        IWorkerModel workerModel = createMock(IWorkerModel.class);
        IMessagesForUser messagesForUser = createMock(IMessagesForUser.class);
        Worker workerToReturn = new Worker();

        WorkerCRUDController workerCRUDController = createControllerForModel(
                workerModel, messagesForUser);
        replay(createWindow, listWindow, editWindow, workRelationshipsWindow,
                addWorkRelationshipWindow, editWorkRelationshipWindow);
        // expectations
        workerModel.prepareForCreate();
        expect(workerModel.getWorker()).andReturn(workerToReturn).anyTimes();
        workerModel.save();
        messagesForUser.showMessage(same(Level.INFO), isA(String.class));
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
        workerModel.prepareForCreate();
        expect(workerModel.getWorker()).andReturn(workerToReturn).anyTimes();
        expect(createWindow.setVisible(true)).andReturn(false);
        expect(createWindow.setVisible(false)).andReturn(true);
        expect(listWindow.setVisible(true)).andReturn(false);
        expect(workRelationshipsWindow.setVisible(true)).andReturn(false);
        expect(addWorkRelationshipWindow.setVisible(true)).andReturn(false);
        replay(createWindow, listWindow, editWindow, workRelationshipsWindow,
                addWorkRelationshipWindow, editWorkRelationshipWindow,
                workerModel);
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
        workerModel.prepareEditFor(workersToReturn.get(0));
        expect(editWindow.setVisible(true)).andReturn(false);
        expect(workerModel.getWorker()).andReturn(workersToReturn.get(0))
                .anyTimes();
        workerModel.save();
        messagesForUser.showMessage(same(Level.INFO), isA(String.class));
        replay(createWindow, listWindow, editWindow, workRelationshipsWindow,
                addWorkRelationshipWindow, editWorkRelationshipWindow,
                workerModel, messagesForUser);
        // perform actions
        List<Worker> workers = workerCRUDController.getWorkers();
        assertEquals(workersToReturn, workers);
        workerCRUDController.goToEditForm(workers.get(0));
        workerCRUDController.save();
        // verify
        verify(workerModel, editWindow, messagesForUser);
    }

}
