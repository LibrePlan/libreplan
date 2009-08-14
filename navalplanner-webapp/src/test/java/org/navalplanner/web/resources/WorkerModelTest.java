package org.navalplanner.web.resources;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.PredefinedCriterionTypes;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.resources.worker.WorkerModel;

/**
 * Some test cases for {@link WorkerModel}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class WorkerModelTest {

    @Test
    public void testWorkerValid() throws ValidationException,
            InstanceNotFoundException {
        IResourceDAO resourceDAOMock = createMock(IResourceDAO.class);
        ICriterionDAO criterionServiceMock = createMock(ICriterionDAO.class);
        Worker workerToReturn = new Worker();
        workerToReturn.setDailyHours(2);
        workerToReturn.setFirstName("firstName");
        workerToReturn.setSurname("surname");
        workerToReturn.setNif("232344243");
        // expectations
        List<Criterion> criterions = new ArrayList<Criterion>();
        expect(
                criterionServiceMock
                        .findByType(PredefinedCriterionTypes.LOCATION_GROUP))
                .andReturn(criterions).anyTimes();
        expect(resourceDAOMock.find(workerToReturn.getId()))
                .andReturn(workerToReturn);
        resourceDAOMock.save(workerToReturn);
        workerToReturn.checkNotOverlaps();
        replay(resourceDAOMock, criterionServiceMock);
        // perform actions
        WorkerModel workerModel = new WorkerModel(resourceDAOMock,
                criterionServiceMock);

        workerModel.prepareEditFor(workerToReturn);
        workerModel.save();
    }

    @Test(expected = ValidationException.class)
    public void testWorkerInvalid() throws ValidationException,
            InstanceNotFoundException {
        IResourceDAO resourceDAOMock = createMock(IResourceDAO.class);
        ICriterionDAO criterionServiceMock = createMock(ICriterionDAO.class);
        Worker workerToReturn = new Worker();
        // expectations
        List<Criterion> criterions = new ArrayList<Criterion>();
        expect(
                criterionServiceMock
                        .findByType(PredefinedCriterionTypes.LOCATION_GROUP))
                .andReturn(criterions).anyTimes();
        expect(resourceDAOMock.find(workerToReturn.getId()))
                .andReturn(workerToReturn);
        replay(resourceDAOMock, criterionServiceMock);
        // perform actions
        WorkerModel workerModel = new WorkerModel(resourceDAOMock,
                criterionServiceMock);
        workerModel.prepareEditFor(workerToReturn);
        workerModel.save();
    }

}
