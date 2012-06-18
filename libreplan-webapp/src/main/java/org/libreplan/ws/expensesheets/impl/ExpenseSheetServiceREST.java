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

package org.libreplan.ws.expensesheets.impl;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.libreplan.business.common.daos.IIntegrationEntityDAO;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.business.expensesheet.daos.IExpenseSheetDAO;
import org.libreplan.business.expensesheet.entities.ExpenseSheet;
import org.libreplan.business.orders.daos.ISumExpensesDAO;
import org.libreplan.ws.common.api.InstanceConstraintViolationsListDTO;
import org.libreplan.ws.common.impl.GenericRESTService;
import org.libreplan.ws.common.impl.RecoverableErrorException;
import org.libreplan.ws.expensesheets.api.ExpenseSheetDTO;
import org.libreplan.ws.expensesheets.api.ExpenseSheetListDTO;
import org.libreplan.ws.expensesheets.api.IExpenseSheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * REST-based implementation of {@link IExpenseSheetService}.
 *
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
@Path("/expenses/")
@Produces("application/xml")
@Service("expenseSheetServiceREST")
public class ExpenseSheetServiceREST extends
        GenericRESTService<ExpenseSheet, ExpenseSheetDTO> implements
        IExpenseSheetService {

    @Autowired
    private ISumExpensesDAO sumExpensesDAO;

    @Autowired
    private IExpenseSheetDAO expenseSheetDAO;

    @Override
    protected void beforeSaving(ExpenseSheet entity) {
        sumExpensesDAO.updateRelatedSumExpensesWithExpenseSheetLineSet(entity
                .getExpenseSheetLines());
        entity.updateCalculatedProperties();
    }

    @Override
    @POST
    @Consumes("application/xml")
    @Transactional
    public InstanceConstraintViolationsListDTO addExpenseSheets(
            ExpenseSheetListDTO expenseSheetListDTO) {
        return save(expenseSheetListDTO.expenseSheets);
    }

    @Override
    protected IIntegrationEntityDAO<ExpenseSheet> getIntegrationEntityDAO() {
        return expenseSheetDAO;
    }

    @Override
    protected ExpenseSheetDTO toDTO(ExpenseSheet entity) {
        return ExpenseSheetConverter.toDTO(entity);
    }

    @Override
    protected ExpenseSheet toEntity(ExpenseSheetDTO entityDTO)
            throws ValidationException, RecoverableErrorException {
        return ExpenseSheetConverter.toEntity(entityDTO);
    }

    @Override
    @GET
    @Path("/{code}/")
    @Transactional(readOnly = true)
    public Response getExpenseSheet(@PathParam("code") String code) {
        return getDTOByCode(code);
    }

    @Override
    protected void updateEntity(ExpenseSheet entity, ExpenseSheetDTO entityDTO)
            throws ValidationException, RecoverableErrorException {
        ExpenseSheetConverter.updateExpenseSheet(entity, entityDTO);

    }

    @Override
    @GET
    @Transactional(readOnly = true)
    public ExpenseSheetListDTO getExpenseSheets() {
        return new ExpenseSheetListDTO(findAll());
    }
}
