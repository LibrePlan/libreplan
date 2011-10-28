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

package org.libreplan.web.common;

import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.common.entities.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;

/**
 * Controller for enable/disable the autocomplete login.
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 */

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LoginController extends GenericForwardComposer {

    private final String autocompletLoginValue = "admin";

    @Autowired
    private IConfigurationDAO configurationDAO;

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("loginController", this, true);
    }

    /**
     * It returns the login value in function of the property autocompleteLogin.
     */
    public String getLoginValue() {
        Configuration configuration = configurationDAO
                .getConfigurationWithReadOnlyTransaction();
        return ((configuration.isAutocompleteLogin()) && (!configuration
                .getChangedDefaultAdminPassword())) ? this.autocompletLoginValue
                : null;
    }

}
