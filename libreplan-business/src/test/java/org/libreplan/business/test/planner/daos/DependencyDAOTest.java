/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
 * Copyright (C) 2014-2026 Jeroen Baten <jeroen@libreplan.dev>
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

package org.libreplan.business.test.planner.daos;

import static org.junit.Assert.assertTrue;
import static org.libreplan.business.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_FILE;
import static org.libreplan.business.test.BusinessGlobalNames.BUSINESS_SPRING_CONFIG_TEST_FILE;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.planner.daos.IDependencyDAO;
import org.libreplan.business.planner.daos.ITaskElementDAO;
import org.libreplan.business.planner.entities.Dependency;
import org.libreplan.business.planner.entities.TaskMilestone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { BUSINESS_SPRING_CONFIG_FILE,
        BUSINESS_SPRING_CONFIG_TEST_FILE })
/**
 * Characterization tests for {@link DependencyDAO}, written ahead of the
 * Hibernate Criteria -> JPA Criteria API migration (Jakarta EE / Hibernate 6)
 * to prove behavior is unchanged after the rewrite.
 *
 * Dependency.create(origin, destination, type) requires both a non-null origin and
 * destination (enforced by its constructor), so a genuinely "unattached" (null
 * origin/destination) Dependency can't be constructed through the public API. Only the
 * "nothing unattached" branch is characterized here.
 */
public class DependencyDAOTest {

    @Autowired
    private IDependencyDAO dependencyDAO;

    @Autowired
    private ITaskElementDAO taskElementDAO;

    @Test
    @Transactional
    public void testDeleteUnattachedDependenciesDoesNotRemoveAttachedOnes() throws InstanceNotFoundException {
        TaskMilestone origin = TaskMilestone.create(new Date());
        taskElementDAO.save(origin);
        TaskMilestone destination = TaskMilestone.create(new Date());
        taskElementDAO.save(destination);

        Dependency dependency = Dependency.create(origin, destination, Dependency.Type.END_START);
        dependencyDAO.save(dependency);
        dependencyDAO.flush();

        dependencyDAO.deleteUnattachedDependencies();

        assertTrue(dependencyDAO.exists(dependency.getId()));
    }

}
