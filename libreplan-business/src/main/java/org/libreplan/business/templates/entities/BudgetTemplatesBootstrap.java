/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 Igalia, S.L.
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
package org.libreplan.business.templates.entities;

import org.libreplan.business.templates.daos.IOrderElementTemplateDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the default {@link BudgetTemplate} for LibrePlan Audiovisual.
 *
 * @author Manuel Rego Casasnovas <rego@igalia.com>
 */
@Component
@Scope("singleton")
public class BudgetTemplatesBootstrap implements IBudgetTemplatesBootstrap {

    @Autowired
    private IOrderElementTemplateDAO orderElementTemplateDAO;

    @Override
    @Transactional
    public void loadRequiredData() {
        if (orderElementTemplateDAO.list(BudgetTemplate.class).isEmpty()) {
            for (PredefinedBudgetTemplates budgetTemplate : PredefinedBudgetTemplates
                    .values()) {
                createAndSaveBudgetTemplate(budgetTemplate);
            }
        }
    }

    private void createAndSaveBudgetTemplate(
            PredefinedBudgetTemplates predefinedBudgetTemplate) {
        BudgetTemplate budgetTemplate = predefinedBudgetTemplate
                .getBudgetTemplate();
        orderElementTemplateDAO.save(budgetTemplate);
    }

}
