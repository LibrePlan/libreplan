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

package org.libreplan.business.advance.bootstrap;

import org.libreplan.business.IDataBootstrap;
import org.libreplan.business.advance.daos.IAdvanceTypeDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Scope("singleton")
public class DefaultAdvanceTypesBootstrapListener implements IDataBootstrap {

    @Autowired
    private IAdvanceTypeDAO advanceTypeDAO;

    @Transactional
    @Override
    public void loadRequiredData() {
        for (PredefinedAdvancedTypes predefinedType : PredefinedAdvancedTypes.values()) {
            if (!advanceTypeDAO.existsNameAdvanceType(predefinedType.getTypeName())) {
                advanceTypeDAO.save(predefinedType.createType());
            }
        }
    }

}
