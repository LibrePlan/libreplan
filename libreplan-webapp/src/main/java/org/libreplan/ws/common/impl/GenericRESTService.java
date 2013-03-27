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

package org.libreplan.ws.common.impl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.libreplan.business.common.IAdHocTransactionService;
import org.libreplan.business.common.IOnTransaction;
import org.libreplan.business.common.IntegrationEntity;
import org.libreplan.business.common.daos.IIntegrationEntityDAO;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;
import org.libreplan.business.common.exceptions.ValidationException;
import org.libreplan.ws.common.api.InstanceConstraintViolationsDTO;
import org.libreplan.ws.common.api.InstanceConstraintViolationsListDTO;
import org.libreplan.ws.common.api.IntegrationEntityDTO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class provides generic support for implementing REST services
 * as subclasses of this. </code>.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 */
public abstract class GenericRESTService<E extends IntegrationEntity,
    DTO extends IntegrationEntityDTO> {

    @Autowired
    protected IAdHocTransactionService transactionService;

    /**
     * It retrieves all entities.
     */
    protected List<DTO> findAll() {
        return toDTO(getIntegrationEntityDAO().findAll());
    }

    /**
     * It saves (inserts or updates) a list of entities. Each entity is
     * saved in a separate transaction.
     */
    protected InstanceConstraintViolationsListDTO save(
        List<? extends DTO> entityDTOs) {

        List<InstanceConstraintViolationsDTO> instanceConstraintViolationsList =
            new ArrayList<InstanceConstraintViolationsDTO>();
        long numItem = 1;

        for (DTO entityDTO : entityDTOs) {

            InstanceConstraintViolationsDTO instanceConstraintViolationsDTO =
                null;

            try {
                insertOrUpdate(entityDTO);
            } catch (ValidationException e) {
                instanceConstraintViolationsDTO =
                    ConstraintViolationConverter.toDTO(
                        Util.generateInstanceConstraintViolationsDTOId(
                            numItem, entityDTO), e);
            } catch (RecoverableErrorException e) {
                instanceConstraintViolationsDTO =
                    ConstraintViolationConverter.toDTO(
                        Util.generateInstanceConstraintViolationsDTOId(
                            numItem, entityDTO), e);
            } catch (RuntimeException e) {
                instanceConstraintViolationsDTO =
                    ConstraintViolationConverter.toDTO(
                        Util.generateInstanceConstraintViolationsDTOId(
                            numItem, entityDTO), e);
            }

            if (instanceConstraintViolationsDTO != null) {
                instanceConstraintViolationsList.add(
                    instanceConstraintViolationsDTO);
            }

            numItem++;

        }

        return new InstanceConstraintViolationsListDTO(
            instanceConstraintViolationsList);

    }

    /**
     * It saves (inserts or updates) an entity DTO by using a new transaction.
     *
     * @throws ValidationException if validations are not passed
     * @throws RecoverableErrorException if a recoverable error occurs
     */
    protected void insertOrUpdate(final DTO entityDTO)
        throws ValidationException, RecoverableErrorException {
        /*
         * NOTE: ValidationException and RecoverableErrorException are runtime
         * exceptions. In consequence, if any of them occurs, transaction is
         * automatically rolled back.
         */

        IOnTransaction<Void> save = new IOnTransaction<Void>() {

            @Override
            public Void execute() {

                E entity = null;
                IIntegrationEntityDAO<E> entityDAO =
                    getIntegrationEntityDAO();

                /* Insert or update? */
                try {
                    entity = entityDAO.findByCode(entityDTO.code);
                    updateEntity(entity, entityDTO);
                } catch (InstanceNotFoundException e) {
                    entity = toEntity(entityDTO);
                }

                /*
                 * Validate and save (insert or update) the entity.
                 */
                entity.validate();
                beforeSaving(entity);
                entityDAO.saveWithoutValidating(entity);
                afterSaving(entity);

                return null;

            }

        };

        transactionService.runOnAnotherTransaction(save);

    }

    /**
     * It allows to add operations that must be done before saving.
     *
     * Default implementation is empty.
     */
    protected void beforeSaving(E entity) {

    }

    /**
     * It allows to add operations that must be done after saving.
     *
     * Default implementation is empty.
     */
    protected void afterSaving(E entity) {

    }

    /**
     * It creates an entity from a DTO.
     *
     * @throws ValidationException if it is not possible to create the entity
     *         because some very important constraint is violated
     * @throws RecoverableErrorException if a recoverable
     *            error occurs
     */
    protected abstract E toEntity(DTO entityDTO)
        throws ValidationException, RecoverableErrorException;

    /**
     * It creates a DTO from an entity.
     */
    protected abstract DTO toDTO(E entity);

    /**
     * It must return the DAO for the entity "E".
     */
    protected abstract IIntegrationEntityDAO<E>
        getIntegrationEntityDAO();

    /**
     * It must update the entity from the DTO.
     *
     * @throws ValidationException if updating is not possible because some
     *         very important constraint is violated
     * @throws RecoverableErrorException if a recoverable error occurs
     */
    protected abstract void updateEntity(E entity, DTO entityDTO)
        throws ValidationException, RecoverableErrorException;

    /**
     * It returns a list of DTOs from a list of entities.
     */
    protected List<DTO> toDTO(List<E> entities) {

        List<DTO> dtos = new ArrayList<DTO>();

        for (E entity : entities) {
            dtos.add(toDTO(entity));
        }

        return dtos;

    }

    /**
     * Returns a DTO searching by code. This will be useful for all REST
     * services of IntegrationEntities
     *
     * @param code
     *            this is the code for the element which will be searched
     * @return DTO which represents the IntegrationEntity with this code
     * @throws InstanceNotFoundException
     *             If entity with this code is not found
     */
    protected DTO findByCode(String code) throws InstanceNotFoundException {
        return toDTO(getIntegrationEntityDAO().findByCode(code));
    }

    /**
     * Wraps within a {@link Response} object the DTO searching the entity by
     * code.
     *
     * If entity is not found returns 404 HTTP status code (NOT_FOUND).
     *
     * @param code
     *            this is the code for the element which will be searched
     * @return The {@link Response} with DTO if OK or 404 if NOT_FOUND
     */
    protected Response getDTOByCode(String code) {
        try {
            return Response.ok(findByCode(code)).build();
        } catch (InstanceNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

}
