package org.navalplanner.web.resources;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import org.junit.Test;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.resources.services.ResourceService;

/**
 * Some test cases for {@link WorkerModel}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class WorkerModelTest {

    @Test
    public void testWorkerValid() throws ValidationException,
            InstanceNotFoundException {
        ResourceService resourceServiceMock = createMock(ResourceService.class);
        WorkerModel workerModel = new WorkerModel(resourceServiceMock);
        Worker workerToReturn = new Worker();
        workerToReturn.setDailyHours(2);
        workerToReturn.setFirstName("firstName");
        workerToReturn.setSurname("surname");
        workerToReturn.setNif("232344243");
        // expectations
        expect(resourceServiceMock.findResource(workerToReturn.getId()))
                .andReturn(workerToReturn);
        resourceServiceMock.saveResource(workerToReturn);
        replay(resourceServiceMock);
        // perform actions
        workerModel.prepareEditFor(workerToReturn);
        workerModel.save();
    }

    @Test(expected = ValidationException.class)
    public void testWorkerInvalid() throws ValidationException,
            InstanceNotFoundException {
        ResourceService resourceServiceMock = createMock(ResourceService.class);
        WorkerModel workerModel = new WorkerModel(resourceServiceMock);
        Worker workerToReturn = new Worker();
        // expectations
        expect(resourceServiceMock.findResource(workerToReturn.getId()))
                .andReturn(workerToReturn);
        replay(resourceServiceMock);
        // perform actions
        workerModel.prepareEditFor(workerToReturn);
        workerModel.save();
    }

}
