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

package org.navalplanner.web.costcategories;

import java.util.List;

import org.navalplanner.business.costcategories.entities.CostCategory;
import org.navalplanner.web.common.IMessagesForUser;
import org.navalplanner.web.common.MessagesForUser;
import org.navalplanner.web.common.OnlyOneVisible;
import org.navalplanner.web.common.Util;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.api.Window;

/**
 * Controller for CRUD actions over a {@link CostCategory}
 *
 * @author Jacobo Aragunde Perez <jaragunde@igalia.com>
 */
@SuppressWarnings("serial")
public class CostCategoryCRUDController extends GenericForwardComposer
        implements ICostCategoryCRUDController {

    private Window listWindow;

    private Window createWindow;

    private ICostCategoryModel costCategoryModel;

    private OnlyOneVisible visibility;

    private IMessagesForUser messagesForUser;

    private Component messagesContainer;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        comp.setVariable("controller", this, true);
        messagesForUser = new MessagesForUser(messagesContainer);
        getVisibility().showOnly(listWindow);
    }

    @Override
    public void goToCreateForm() {
        //TODO
    }

    @Override
    public void goToEditForm(CostCategory costCategory) {
        //TODO
    }

    @Override
    public void goToList() {
        getVisibility().showOnly(listWindow);
        Util.reloadBindings(listWindow);
    }

    public List<CostCategory> getCostCategories() {
        return costCategoryModel.getCostCategories();
    }

    private OnlyOneVisible getVisibility() {
        return (visibility == null) ? new OnlyOneVisible(createWindow,
                listWindow)
                : visibility;
    }
}
