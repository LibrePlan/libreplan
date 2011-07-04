/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2011 ComtecSF, S.L.
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
package org.navalplanner.web.users.settings;

import org.navalplanner.business.common.exceptions.ValidationException;
import org.navalplanner.business.users.entities.Language;

/**
 * Model for UI operations related to user settings
 *
 * @author Cristina Alvarino Perez <cristina.alvarino@comtecsf.es>
 * @author Ignacio Diaz Teijido <ignacio.diaz@comtecsf.es>
 */
public interface ISettingsModel {

    void setApplicationLanguage(Language applicationLanguage);

    Language getApplicationLanguage();

    void initEditLoggedUser();

    void confirmSave() throws ValidationException;

    void setExpandCompanyPlanningViewCharts(
            boolean expandCompanyPlanningViewCharts);

    boolean isExpandResourceLoadViewCharts();

    void setExpandResourceLoadViewCharts(boolean expandResourceLoadViewCharts);

    boolean isExpandOrderPlanningViewCharts();

    void setExpandOrderPlanningViewCharts(boolean expandOrderPlanningViewCharts);

    boolean isExpandCompanyPlanningViewCharts();

    void setLastName(String lastName);

    String getLastName();

    void setFirstName(String firstName);

    String getFirstName();

    String getEmail();

    void setEmail(String email);

    void setLoginName(String loginName);

    String getLoginName();

    /**
     * Sets the password attribute to the inner {@ link User} object.
     *
     * @param password String with the <b>unencrypted</b> password.
     */
    void setPassword(String password);

    String getClearNewPassword();
}
