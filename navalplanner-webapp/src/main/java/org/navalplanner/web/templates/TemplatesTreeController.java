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

import java.util.Collections;

import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.SimpleTreeModel;
import org.zkoss.zul.SimpleTreeNode;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;

/**
 * Controller for template element tree <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class TemplatesTreeController extends GenericForwardComposer {

    public TreeitemRenderer getRenderer() {
        return new TreeitemRenderer() {

            @Override
            public void render(Treeitem item, Object data) throws Exception {
            }
        };
    }

    public TreeModel getTreeModel() {
        return new SimpleTreeModel(new SimpleTreeNode("", Collections
                .emptyList()));
    }

}
