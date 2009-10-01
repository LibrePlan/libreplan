/*
 * This file is part of ###PROJECT_NAME###
 *
 * Copyright (C) 2009 Fundación para o Fomento da Calidade Industrial e
 *                    Desenvolvemento Tecnolóxico de Galicia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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

    @Test(expected = ValidationException.class)
    public void testWorkerInvalid() throws ValidationException,
            InstanceNotFoundException {
        IResourceDAO resourceDAOMock = createMock(IResourceDAO.class);
        ICriterionDAO criterionServiceMock = createMock(ICriterionDAO.class);
        Worker workerToReturn = Worker.create();
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
