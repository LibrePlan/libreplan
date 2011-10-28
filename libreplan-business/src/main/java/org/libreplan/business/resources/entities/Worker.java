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

package org.libreplan.business.resources.entities;


import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.libreplan.business.common.Registry;
import org.libreplan.business.common.exceptions.InstanceNotFoundException;

/**
 * This class models a worker.
 *
 * Note: this class has a natural ordering that is inconsistent with equals.
 *
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class Worker extends Resource {

    public static Worker create() {
        return create(new Worker());
    }

    public static Worker create(String code) {
        return create(new Worker(), code);
    }

    public static Worker create(String firstName, String surname,
        String nif) {

        return create(new Worker(firstName, surname, nif));

    }

    public static Worker createUnvalidated(String code, String firstName,
        String surname, String nif) {

        Worker worker = create(new Worker(), code);

        worker.firstName = firstName;
        worker.surname = surname;
        worker.nif = nif;

        return worker;

    }

    public void updateUnvalidated(String firstName, String surname, String nif) {

        if (!StringUtils.isBlank(firstName)) {
            this.firstName = firstName;
        }

        if (!StringUtils.isBlank(surname)) {
            this.surname = surname;
        }

        if (!StringUtils.isBlank(nif)) {
            this.nif = nif;
        }

    }

    private final static ResourceEnum type = ResourceEnum.WORKER;

    private String firstName;

    private String surname;

    private String nif;

    /**
     * Constructor for hibernate. Do not use!
     */
    public Worker() {

    }

    private Worker(String firstName, String surname, String nif) {
        this.firstName = firstName;
        this.surname = surname;
        this.nif = nif;
    }

    public String getDescription() {
        return getSurname() + "," + getFirstName();
    }

    @Override
    public String getShortDescription() {
        return getDescription() + " (" + getNif() + ")";
    }

    @NotEmpty(message="worker's first name not specified")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @NotEmpty(message="worker's surname not specified")
    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return getSurname() + ", " + getFirstName();
    }

    @NotEmpty(message="Worker ID cannot be empty")
    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public boolean isVirtual() {
        return false;
    }

    public boolean isReal() {
        return !isVirtual();
    }

    @AssertTrue(message = "ID already used. It has to be be unique")
    public boolean checkConstraintUniqueFiscalCode() {
        if (!areFirstNameSurnameNifSpecified()) {
            return true;
        }

        try {
        /* Check the constraint. */
            Worker worker = Registry.getWorkerDAO()
                    .findByNifAnotherTransaction(nif);
            if (isNewObject()) {
                return false;
            } else {
                return worker.getId().equals(getId());
            }
        } catch (InstanceNotFoundException e) {
            return true;
        }
    }

    protected boolean areFirstNameSurnameNifSpecified() {

       return !StringUtils.isBlank(firstName) &&
           !StringUtils.isBlank(surname) &&
           !StringUtils.isBlank(nif);

   }

   @Override
   protected boolean isCriterionSatisfactionOfCorrectType(
      CriterionSatisfaction c) {
        return c.getResourceType().equals(ResourceEnum.WORKER);

   }

    @Override
    public ResourceEnum getType() {
        return type;
    }

    @Override
    public String getHumanId() {
        if (firstName == null) {
            return surname;
        }
        if (surname == null) {
            return firstName;
        }
        return firstName + " " + surname;
    }

}
