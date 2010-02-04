/*
 * This file is part of NavalPlan
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

package org.navalplanner.business.resources.entities;


import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.navalplanner.business.common.Registry;

/**
 * This class models a worker.
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */
public class Worker extends Resource {

    public static Worker create() {
        return create(new Worker());
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
        return getFirstName()+" "+getSurname();
    }

    @Override
    public String getShortDescription() {
        return getNif() + " :: " + getDescription();
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
        return firstName + " " + surname;
    }

    @NotEmpty(message="worker's NIF not specified")
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

    @AssertTrue(message = "Worker with the same first name, surname and nif previously existed")
    public boolean checkConstraintUniqueFirstNameSurnameNif() {

        if (!areFirstNameSurnameNifSpecified()) {
            return true;
        }

        /* Check the constraint. */
        List<Worker> list = Registry.getWorkerDAO()
                .findByFirstNameSecondNameAndNifAnotherTransaction(firstName,
                        surname, nif);

        if (isNewObject()) {
            return list.isEmpty();
        } else {
            if (list.isEmpty()) {
                return true;
            } else {
                return list.get(0).getId().equals(getId());
            }
        }

    }

   private boolean areFirstNameSurnameNifSpecified() {

       return !StringUtils.isBlank(firstName) &&
           !StringUtils.isBlank(surname) &&
           !StringUtils.isBlank(nif);

   }

   @Override
   protected boolean isCriterionSatisfactionOfCorrectType(
      CriterionSatisfaction c) {

       return super.isCriterionSatisfactionOfCorrectType(c) ||
           c.getResourceType().equals(ResourceEnum.WORKER);

   }

}
