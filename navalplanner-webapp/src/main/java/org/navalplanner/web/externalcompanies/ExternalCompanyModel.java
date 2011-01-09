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

package org.navalplanner.web.externalcompanies;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.externalcompanies.daos.IExternalCompanyDAO;
import org.navalplanner.business.externalcompanies.entities.ExternalCompany;
import org.navalplanner.business.users.entities.User;
import org.navalplanner.web.common.concurrentdetection.OnConcurrentModification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Model for UI operations related to {@link ExternalCompany}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@OnConcurrentModification(goToPage = "/externalcompanies/externalcompanies.zul")
public class ExternalCompanyModel implements IExternalCompanyModel {

    @Autowired
    private IExternalCompanyDAO externalCompanyDAO;

    private ExternalCompany externalCompany;

    @Override
    @Transactional(readOnly = true)
    public List<ExternalCompany> getCompanies() {
        List<ExternalCompany> list = externalCompanyDAO.list(ExternalCompany.class);
        for(ExternalCompany company : list) {
            forceLoadEntities(company);
        }
        return list;
    }

    @Override
    public ExternalCompany getCompany() {
        return externalCompany;
    }

    @Override
    public void initCreate() {
        externalCompany = ExternalCompany.create();
    }

    @Override
    @Transactional
    public void confirmSave() {
        externalCompanyDAO.save(externalCompany);
    }

    @Override
    @Transactional(readOnly = true)
    public void initEdit(ExternalCompany company) {
        Validate.notNull(company);
        externalCompany = getFromDB(company);
    }

    @Transactional(readOnly = true)
    private ExternalCompany getFromDB(ExternalCompany company) {
        return getFromDB(company.getId());
    }

    @Transactional(readOnly = true)
    private ExternalCompany getFromDB(Long id) {
        try {
            ExternalCompany result = externalCompanyDAO.find(id);
            forceLoadEntities(result);
            return result;
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load entities that will be needed in the conversation
     *
     * @param company
     */
    private void forceLoadEntities(ExternalCompany company) {
        company.getName();
        if(company.getCompanyUser() != null) {
            company.getCompanyUser().getLoginName();
        }
    }

    @Override
    public void setCompanyUser(User companyUser) {
        externalCompany.setCompanyUser(companyUser);
    }

    @Override
    @Transactional
    public boolean deleteCompany(ExternalCompany company) {
        try {
            externalCompanyDAO.remove(company.getId());
        } catch (InstanceNotFoundException e) {
            return false;
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAlreadyInUse(ExternalCompany company) {
        return externalCompanyDAO.isAlreadyInUse(company);
    }
}
