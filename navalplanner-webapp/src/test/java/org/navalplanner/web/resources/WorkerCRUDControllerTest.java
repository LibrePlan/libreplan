package org.navalplanner.web.resources;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.navalplanner.business.resources.entities.Worker;
import org.zkoss.zul.api.Window;

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
        createWindow = createNiceMock(Window.class);
        listWindow = createNiceMock(Window.class);
        editWindow = createNiceMock(Window.class);

        WorkerCRUDController workerCRUDController = new WorkerCRUDController(
                createWindow, listWindow, editWindow, workerModel);
        return workerCRUDController;
    }

    @Test
    public void testSave() throws Exception {
        IWorkerModel workerModel = createMock(IWorkerModel.class);
        Worker workerToReturn = new Worker();

        WorkerCRUDController workerCRUDController = createControllerForModel(workerModel);
        replay(createWindow, listWindow, editWindow);
        // expectations
        expect(workerModel.createNewInstance()).andReturn(workerToReturn);
        workerModel.save(workerToReturn);
        replay(workerModel);
        // action
        workerCRUDController.goToCreateForm();
        workerCRUDController.save();
        // verify
        verify(workerModel);
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
        WorkerCRUDController workerCRUDController = createControllerForModel(workerModel);
        List<Worker> workersToReturn = new ArrayList<Worker>(Arrays.asList(
                new Worker("firstName", "surname", "nif", 4), new Worker(
                        "firstName", "surname", "nif", 4)));
        // expectations
        expect(workerModel.getWorkers()).andReturn(workersToReturn);
        expect(editWindow.setVisible(true)).andReturn(false);
        workerModel.save(workersToReturn.get(0));
        replay(createWindow, listWindow, editWindow, workerModel);
        // perform actions
        List<Worker> workers = workerCRUDController.getWorkers();
        assertEquals(workersToReturn, workers);
        workerCRUDController.goToEditForm(workers.get(0));
        workerCRUDController.save();
        // verify
        verify(workerModel, editWindow);
    }
}
