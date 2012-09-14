/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
 * Copyright (C) 2010-2011 Igalia, S.L.
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

package org.libreplan.business.resources.bootstrap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.libreplan.business.resources.entities.CriterionType;
import org.libreplan.business.resources.entities.PredefinedCriterionTypes;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * This class provides the CriterionTypes with their criterions that are known a
 * priori<br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 * @author Diego Pino García <dpino@igalia.com>
 */
@Component
@Scope("singleton")
public class PredefinedCriterionTypesProvider implements ICriterionTypeProvider {

    public PredefinedCriterionTypesProvider() {
    }

    @Override
    public Map<CriterionType, List<String>> getRequiredCriterions() {
        Map<CriterionType, List<String>> result = new HashMap<CriterionType, List<String>>();

        // Predefined criteria will not be available on Audiovisual Branch
        //
        // for (PredefinedCriterionTypes type :
        // PredefinedCriterionTypes.values()) {
        // result.put(CriterionType.fromPredefined(type), type.getPredefined());
        // }
        return result;
    }
}
