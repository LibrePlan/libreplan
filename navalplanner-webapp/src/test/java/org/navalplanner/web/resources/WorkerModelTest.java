package org.navalplanner.web.resources;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.PredefinedCriterionTypes;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.business.resources.services.CriterionService;
import org.navalplanner.business.resources.services.ResourceService;
import org.navalplanner.web.resources.worker.WorkerModel;

/**
 * Some test cases for {@link WorkerModel}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class WorkerModelTest {

    @Test
    public void testWorkerValid() throws ValidationException,
            InstanceNotFoundException {
        ResourceService resourceServiceMock = createMock(ResourceService.class);
        CriterionService criterionServiceMock = createMock(CriterionService.class);
        Worker workerToReturn = new Worker();
        workerToReturn.setDailyHours(2);
        workerToReturn.setFirstName("firstName");
        workerToReturn.setSurname("surname");
        workerToReturn.setNif("232344243");
        // expectations
        Collection<Criterion> criterions = new ArrayList<Criterion>();
        expect(
                criterionServiceMock
                        .getCriterionsFor(PredefinedCriterionTypes.LOCATION_GROUP))
                .andReturn(criterions).anyTimes();
        expect(resourceServiceMock.findResource(workerToReturn.getId()))
                .andReturn(workerToReturn);
        resourceServiceMock.saveResource(workerToReturn);
        replay(resourceServiceMock, criterionServiceMock);
        // perform actions
        WorkerModel workerModel = new WorkerModel(resourceServiceMock,
                criterionServiceMock);

        workerModel.prepareEditFor(workerToReturn);
        workerModel.save();
    }

    @Test(expected = ValidationException.class)
    public void testWorkerInvalid() throws ValidationException,
            InstanceNotFoundException {
        ResourceService resourceServiceMock = createMock(ResourceService.class);
        CriterionService criterionServiceMock = createMock(CriterionService.class);
        Worker workerToReturn = new Worker();
        // expectations
        Collection<Criterion> criterions = new ArrayList<Criterion>();
        expect(
                criterionServiceMock
                        .getCriterionsFor(PredefinedCriterionTypes.LOCATION_GROUP))
                .andReturn(criterions).anyTimes();
        expect(resourceServiceMock.findResource(workerToReturn.getId()))
                .andReturn(workerToReturn);
        replay(resourceServiceMock, criterionServiceMock);
        // perform actions
        WorkerModel workerModel = new WorkerModel(resourceServiceMock,
                criterionServiceMock);
        workerModel.prepareEditFor(workerToReturn);
        workerModel.save();
    }

}
