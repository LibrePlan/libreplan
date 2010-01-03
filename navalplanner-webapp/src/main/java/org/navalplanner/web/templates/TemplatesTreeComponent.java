/*
 * This file is part of ###PROJECT_NAME###
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
package org.navalplanner.web.templates;

import static org.navalplanner.web.I18nHelper._;

import org.navalplanner.web.orders.components.TreeComponent;

/**
 * Tree component for templates <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class TemplatesTreeComponent extends TreeComponent {

    public String getAddElementLabel() {
        return _("New Template element");
    }

    public boolean isCreateTemplateEnabled() {
        return true;
    }

    public String getRemoveElementLabel() {
        return _("Delete Template element");
    }

    public String getHoursTooltip() {
        return _("Total Template hours");
    }

    public String getOperationsTooltip() {
        return _("Click on the icons to execute operation in the template");
    }

}
