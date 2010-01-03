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
package org.navalplanner.web.orders.components;

import org.navalplanner.web.orders.OrderElementTreeController;
import org.zkoss.zk.ui.HtmlMacroComponent;

/**
 * macro component for order elements tree and similar pages<br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class TreeComponent extends HtmlMacroComponent {

    private static final String CONTROLLER_NAME = "treeController";

    public void clear() {
        OrderElementTreeController controller = (OrderElementTreeController) getVariable(
                CONTROLLER_NAME, true);
        controller.clear();
    }

    public void useController(OrderElementTreeController controller) {
        doAfterComposeOnController(controller);
        this.setVariable(CONTROLLER_NAME, controller, true);
    }

    private void doAfterComposeOnController(
            OrderElementTreeController controller) {
        try {
            controller.doAfterCompose(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
