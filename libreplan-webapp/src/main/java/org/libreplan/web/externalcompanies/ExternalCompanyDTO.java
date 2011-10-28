/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2010-2011 Wireless Galicia, S.L.
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

package org.libreplan.web.externalcompanies;

import org.libreplan.business.externalcompanies.entities.ExternalCompany;

/**
 * DTO for ExternalCompany
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

public class ExternalCompanyDTO {

    private ExternalCompany company;

    public ExternalCompany getCompany() {
        return company;
    }

    public ExternalCompanyDTO(ExternalCompany company) {
        this.company = company;
    }

    public String getName() {
        return company.getName();
    }

    public String getNif() {
        return company.getNif();
    }

    public Boolean getClient() {
        return company.isClient();
    }

    public Boolean getSubcontractor() {
        return company.isSubcontractor();
    }

    public String getCompanyUser() {
        return (company.getCompanyUser() != null) ? company.getCompanyUser()
                .getLoginName() : "---";
    }
}