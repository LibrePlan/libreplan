/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2012 WirelessGalicia, S.L.
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

package org.libreplan.web.planner.filming.progress;

import java.util.Date;

import org.joda.time.LocalDate;
import org.libreplan.business.filmingprogress.entities.FilmingProgress;
import org.libreplan.business.filmingprogress.entities.ProgressGranularityType;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.web.common.IMessagesForUser;
import org.libreplan.web.common.MessagesForUser;
import org.libreplan.web.common.OnlyOneVisible;
import org.libreplan.web.common.Util;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Auxheader;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.SimpleListModel;

/**
 * @author Susana Montes Pedreira <smontes@wirelessgalicia.com>
 *
 *         Controller for filming progress per order view
 * @param <IFilmingProgressModel>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FilmingProgressController extends GenericForwardComposer {

    private IFilmingProgressModel filmingProgressModel;

    private org.zkoss.zk.ui.Component mainComponent;

    private OnlyOneVisible onlyOneVisible;
    private Div noDataLayout;
    private org.zkoss.zk.ui.Component normalLayout;

    private IMessagesForUser messages;
    private Listbox filmingProgressZoomLevel;

    private Auxheader auxHeaderTableTitles;
    private Label totalInitialProgress;
    private Label totalCurrentEstimations;
    private Label totalRealProgress;

    private boolean fixedZoomByUser = false;

    public FilmingProgressController() {
    }

    @Override
    public void doAfterCompose(org.zkoss.zk.ui.Component comp) throws Exception {
        super.doAfterCompose(comp);
        this.mainComponent = comp;
        self.setAttribute("controller", this);
        Util.createBindingsFor(this.mainComponent);

        normalLayout = (org.zkoss.zk.ui.Component) comp.getFellow("normalLayout");
        noDataLayout = (Div) comp.getFellow("noDataLayout");
        onlyOneVisible = new OnlyOneVisible(normalLayout, noDataLayout);
        onlyOneVisible.showOnly(noDataLayout);

    }

    private void reloadNormalLayout() {
        Util.createBindingsFor(normalLayout);
    }

    public void setCurrentOrder(Order order) {
        filmingProgressModel.setCurrentOrder(order);
        if (this.mainComponent != null) {
            loadAndInitializeComponents();
        }
    }

    private Order getCurrentOrder() {
        return filmingProgressModel.getCurrentOrder();
    }

    private void loadAndInitializeComponents() {
        messages = new MessagesForUser(mainComponent.getFellow("messages"));
        if (getCurrentOrder().getBudget() == null) {
            onlyOneVisible.showOnly(noDataLayout);
        } else {
            onlyOneVisible.showOnly(normalLayout);
            createComponents();
        }
    }

    private void createComponents() {
        filmingProgressZoomLevel = (Listbox) mainComponent.getFellow("filmingProgressZoomLevel");
        setZoomFromFilmingProgress();
        reloadNormalLayout();
    }

    private void setZoomFromFilmingProgress() {
        int index = ProgressGranularityType.getDefault().ordinal();
        if (this.getFilmingProgress() != null) {
            index = this.getFilmingProgress().getProgressGranularity().ordinal();
        }
        filmingProgressZoomLevel.setSelectedIndex(index);
    }

    private FilmingProgress getFilmingProgress() {
        return this.filmingProgressModel.getCurrentFilmingProgress();
    }

    /*
     * functions to manage the datebox
     */
    public Date getStartDate() {
        if (getFilmingProgress() != null && getFilmingProgress().getStartDate() != null) {
            return getFilmingProgress().getStartDate().toDateTimeAtStartOfDay().toDate();
        }
        return null;
    }

    public void setStartDate(Date date) {
        if (getFilmingProgress() != null && date != null) {
            getFilmingProgress().setStartDate(new LocalDate(date));
        }
    }

    public Date getEndDate() {
        if (getFilmingProgress() != null && getFilmingProgress().getEndDate() != null) {
            return getFilmingProgress().getEndDate().toDateTimeAtStartOfDay().toDate();
        }
        return null;
    }

    public void setEndDate(Date date) {
        if (getFilmingProgress() != null && date != null) {
            getFilmingProgress().setEndDate(new LocalDate(date));
        }
    }

    public ListModel getZoomLevels() {
        return new SimpleListModel(ProgressGranularityType.values());
    }

}