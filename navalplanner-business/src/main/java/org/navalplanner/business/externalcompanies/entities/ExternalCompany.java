/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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

package org.navalplanner.business.externalcompanies.entities;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.AssertTrue;
import org.hibernate.validator.NotEmpty;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.common.Registry;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.externalcompanies.daos.IExternalCompanyDAO;
import org.navalplanner.business.users.entities.User;

/**
 * Entity ExternalCompany
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
public class ExternalCompany extends BaseEntity {

    private String name;

    private String nif;

    private Boolean client = false;

    private Boolean subcontractor = false;

    private User companyUser;

    private Boolean interactsWithApplications = false;

    private String appURI;

    private String ourCompanyLogin;

    private String ourCompanyPassword;

    protected ExternalCompany() {}

    public static ExternalCompany create() {
        return (ExternalCompany) create(new ExternalCompany());
    }

    protected ExternalCompany(String name, String nif) {
        this.name = name;
        this.nif = nif;
    }

    public static ExternalCompany create(String name, String nif) {
        return (ExternalCompany) create(new ExternalCompany(name,nif));
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotEmpty
    public String getName() {
        return name;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    @NotEmpty
    public String getNif() {
        return nif;
    }

    public void setClient(boolean client) {
        this.client = client;
    }

    public boolean isClient() {
        return client;
    }

    public void setSubcontractor(boolean subcontractor) {
        this.subcontractor = subcontractor;
    }

    public boolean isSubcontractor() {
        return subcontractor;
    }

    public void setCompanyUser(User companyUser) {
        this.companyUser = companyUser;
    }

    public User getCompanyUser() {
        return companyUser;
    }

    public void setInteractsWithApplications(boolean interactsWithApplications) {
        this.interactsWithApplications = interactsWithApplications;
    }

    public boolean getInteractsWithApplications() {
        return interactsWithApplications;
    }

    public void setAppURI(String appURI) {
        this.appURI = StringUtils.trim(appURI);
    }

    public String getAppURI() {
        return appURI;
    }

    public void setOurCompanyLogin(String ourCompanyLogin) {
        this.ourCompanyLogin = ourCompanyLogin;
    }

    public String getOurCompanyLogin() {
        return ourCompanyLogin;
    }

    public void setOurCompanyPassword(String ourCompanyPassword) {
        this.ourCompanyPassword = ourCompanyPassword;
    }

    public String getOurCompanyPassword() {
        return ourCompanyPassword;
    }

    @AssertTrue(message="company name has to be unique. It is already used")
    public boolean checkConstraintUniqueName() {
        IExternalCompanyDAO dao = Registry.getExternalCompanyDAO();

        if (isNewObject()) {
            return !dao.existsByNameInAnotherTransaction(name);
        } else {
            try {
                ExternalCompany company =
                    dao.findUniqueByNameInAnotherTransaction(name);
                return company.getId().equals(getId());
            } catch (InstanceNotFoundException e) {
                return true;
            }
        }
    }

    @AssertTrue(message="Company ID already used. It has to be be unique")
    public boolean checkConstraintUniqueNif() {
        IExternalCompanyDAO dao = Registry.getExternalCompanyDAO();

        if (isNewObject()) {
            return !dao.existsByNifInAnotherTransaction(nif);
        } else {
            try {
                ExternalCompany company =
                    dao.findUniqueByNifInAnotherTransaction(nif);
                return company.getId().equals(getId());
            } catch (InstanceNotFoundException e) {
                return true;
            }
        }
    }

    @AssertTrue(message = "interaction fields are empty and company is marked as interact with applications")
    public boolean checkConstraintInteractionFieldsNotEmptyIfNeeded() {
        if (!interactsWithApplications) {
            return true;
        }

        return !StringUtils.isEmpty(appURI)
                && !StringUtils.isEmpty(ourCompanyLogin)
                && !StringUtils.isEmpty(ourCompanyPassword);
    }

}