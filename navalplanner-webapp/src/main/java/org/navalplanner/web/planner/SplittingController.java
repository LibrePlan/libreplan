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

package org.navalplanner.web.planner;

import java.util.List;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Label;
import org.zkoss.zul.SimpleListModel;
import org.zkoss.zul.Window;
import org.zkoss.zul.api.Grid;

import static org.navalplanner.web.I18nHelper._;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SplittingController extends GenericForwardComposer {

    private IActionOnOk curentAction;
    private Window window;

    private Grid sharesListing;

    private Label totalHoursLabel;
    private List<ShareBean> sharesList;
    private Integer totalHours;

    public interface IActionOnOk {
        public void doOkAction(ShareBean[] shares);
    }

    public void show(List<ShareBean> initialSharesList, Integer totalHours,
            IActionOnOk ok) {
        this.sharesList = initialSharesList;
        this.totalHours = totalHours;
        this.curentAction = ok;
        this.totalHoursLabel.setValue(totalHours + "");
        this.sharesListing.setModel(new SimpleListModel(initialSharesList));
        showWindow();
    }

    public void onClick$splitOk() {
        checkSumIsEqualToTotal();
        Clients.closeErrorBox(totalHoursLabel);
        hideWindow();
        curentAction.doOkAction(this.sharesList.toArray(new ShareBean[0]));
    }

    private void checkSumIsEqualToTotal() {
        int sum = ShareBean.sum(sharesList);
        if (sum != totalHours) {
            throw new WrongValueException(totalHoursLabel,
                    _("The sum is not equal: {0}", sum));
        }
    }

    public void onClick$splitCancel() {
        hideWindow();
    }

    private void showWindow() {
        try {
            window.setMode("modal");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void hideWindow() {
        window.setVisible(false);
    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        super.doAfterCompose(comp);
        window = (Window) comp;

    }

}
