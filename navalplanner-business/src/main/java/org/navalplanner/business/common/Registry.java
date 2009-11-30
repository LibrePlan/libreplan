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

package org.navalplanner.business.common;

import org.navalplanner.business.advance.daos.IAdvanceTypeDAO;
import org.navalplanner.business.resources.daos.ICriterionTypeDAO;
import org.navalplanner.business.resources.daos.IMachineDAO;
import org.navalplanner.business.users.daos.IUserDAO;
import org.navalplanner.business.workreports.daos.IWorkReportTypeDAO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A registry, AKA service locator, for objects in which dependency injection
 * (DI) is not directly supported by Spring (e.g. entities) must use this class
 * to access DAOs. For the rest of classes (e.g. services, tests, etc.), Spring
 * DI is a more convenient option. The DAOs or services are added to the
 * registry as needed.
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Javier Moran Rua <jmoran@igalia.com>
 */
public class Registry {

    private static final Registry singleton = new Registry();

    @Autowired
    private IAdvanceTypeDAO advanceTypeDao;

    @Autowired
    private ICriterionTypeDAO criterionTypeDAO;

    @Autowired

    private IUserDAO userDAO;

    @Autowired
    private IMachineDAO machineDAO;

    private IWorkReportTypeDAO workReportTypeDAO;

    private Registry() {
    }

    public static Registry getInstance() {
        return singleton;
    }

    public static IAdvanceTypeDAO getAdvanceTypeDao() {
        return getInstance().advanceTypeDao;
    }

    public static ICriterionTypeDAO getCriterionTypeDAO() {
        return getInstance().criterionTypeDAO;
    }

    public static IUserDAO getUserDAO() {
        return getInstance().userDAO;
    }

    public static IMachineDAO getMachineDAO() {
        return getInstance().machineDAO;
    }

    public static IWorkReportTypeDAO getWorkReportTypeDAO() {
        return getInstance().workReportTypeDAO;
    }
}
