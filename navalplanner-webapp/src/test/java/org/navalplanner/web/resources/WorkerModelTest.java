package org.navalplanner.web.resources;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.navalplanner.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.navalplanner.web.WebappGlobalNames.WEBAPP_SPRING_CONFIG_FILE;
import static org.navalplanner.web.test.WebappGlobalNames.WEBAPP_SPRING_CONFIG_TEST_FILE;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.resources.daos.ICriterionDAO;
import org.navalplanner.business.resources.daos.IResourceDAO;
import org.navalplanner.business.resources.entities.Criterion;
import org.navalplanner.business.resources.entities.PredefinedCriterionTypes;
import org.navalplanner.business.resources.entities.Resource;
import org.navalplanner.business.resources.entities.Worker;
import org.navalplanner.web.resources.worker.WorkerModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Some test cases for {@link WorkerModel}. <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        WEBAPP_SPRING_CONFIG_FILE, WEBAPP_SPRING_CONFIG_TEST_FILE })
@Transactional
public class WorkerModelTest {

    @Autowired
    private IResourceDAO resourceDAO;

    @Test
    public void testWorkerValid() throws ValidationException,
            InstanceNotFoundException {

        IResourceDAO resourceDAOMock = createMock(IResourceDAO.class);
        ICriterionDAO criterionServiceMock = createMock(ICriterionDAO.class);
        Worker workerToReturn = Worker.create();
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

    @Test(expected = IllegalStateException.class)
    public void testWorkerInvalid() throws ValidationException,
            InstanceNotFoundException, IllegalStateException {

        IResourceDAO resourceDAOMock = createMock(IResourceDAO.class);
        ICriterionDAO criterionServiceMock = createMock(ICriterionDAO.class);
        final Worker workerToReturn = Worker.create();
        // expectations
        List<Criterion> criterions = new ArrayList<Criterion>();
        expect(
                criterionServiceMock
                        .findByType(PredefinedCriterionTypes.LOCATION_GROUP))
                .andReturn(criterions).anyTimes();
        expect(resourceDAOMock.find(workerToReturn.getId())).andReturn(
                workerToReturn);
        resourceDAOMock.save(workerToReturn);
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                Resource argument = (Resource) EasyMock.getCurrentArguments()[0];
                resourceDAO.save(argument);
                return null;
            }
        });
        replay(resourceDAOMock, criterionServiceMock);
        // perform actions
        WorkerModel workerModel = new WorkerModel(resourceDAOMock,
                criterionServiceMock);
        workerModel.prepareEditFor(workerToReturn);
        workerModel.save();
    }
}
