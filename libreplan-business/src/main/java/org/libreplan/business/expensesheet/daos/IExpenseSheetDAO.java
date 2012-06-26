/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 WirelessGalicia, S.L.
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
package org.libreplan.business.expensesheet.daos;

import java.util.List;

import org.libreplan.business.common.daos.IIntegrationEntityDAO;
import org.libreplan.business.expensesheet.entities.ExpenseSheet;
import org.libreplan.business.resources.entities.Resource;

/**
 * Interface for ExpenseSheet DAO
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 *
 */
public interface IExpenseSheetDAO extends IIntegrationEntityDAO<ExpenseSheet> {

    List<ExpenseSheet> getAll();

    List<ExpenseSheet> getPersonalExpenseSheetsByResource(Resource resource);

}