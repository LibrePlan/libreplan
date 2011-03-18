/*
 * This file is part of NavalPlan
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

package org.zkoss.ganttz.resourceload;

import static org.zkoss.ganttz.i18n.I18nHelper._;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.Duration;
import org.joda.time.LocalDate;
import org.zkoss.ganttz.IDatesMapper;
import org.zkoss.ganttz.data.resourceload.LoadPeriod;
import org.zkoss.ganttz.data.resourceload.LoadTimeLine;
import org.zkoss.ganttz.timetracker.TimeTracker;
import org.zkoss.ganttz.timetracker.zoom.IZoomLevelChangedListener;
import org.zkoss.ganttz.timetracker.zoom.ZoomLevel;
import org.zkoss.ganttz.util.MenuBuilder;
import org.zkoss.ganttz.util.WeakReferencedListeners;
import org.zkoss.ganttz.util.MenuBuilder.ItemAction;
import org.zkoss.ganttz.util.WeakReferencedListeners.IListenerNotification;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Div;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.impl.XulElement;

/**
 * This class wraps ResourceLoad data inside an specific HTML Div component.
 * @author Lorenzo Tilve Álvaro <ltilve@igalia.com>
 */
public class ResourceLoadComponent extends XulElement {

    public static ResourceLoadComponent create(TimeTracker timeTracker,
            LoadTimeLine loadLine) {
        return new ResourceLoadComponent(timeTracker, loadLine);
    }

    private final LoadTimeLine loadLine;
    private final TimeTracker timeTracker;
    private transient IZoomLevelChangedListener zoomChangedListener;
    private WeakReferencedListeners<ISeeScheduledOfListener> scheduleListeners = WeakReferencedListeners
            .create();

    private ResourceLoadComponent(final TimeTracker timeTracker,
            final LoadTimeLine loadLine) {
        this.loadLine = loadLine;
        this.timeTracker = timeTracker;
        createChildren(loadLine, timeTracker.getMapper());
        zoomChangedListener = new IZoomLevelChangedListener() {

            @Override
            public void zoomLevelChanged(ZoomLevel detailLevel) {
                getChildren().clear();
                createChildren(loadLine, timeTracker.getMapper());
                invalidate();
            }
        };
        this.timeTracker.addZoomListener(zoomChangedListener);
    }

    private void createChildren(final LoadTimeLine loadLine, IDatesMapper mapper) {
        List<Div> divs = createDivsForPeriods(mapper, loadLine.getLoadPeriods());
        for (Div div : divs) {
            appendChild(div);
        }

        if (loadLine.getRole().isVisibleScheduled()) {
            for (Div div : divs) {
                addDoubleClickAction(div, loadLine);
                addContextMenu(divs, div, loadLine);
            }
        }
    }

    private void addDoubleClickAction(final Div div, final LoadTimeLine loadLine) {
        div.addEventListener("onDoubleClick", new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                schedule(loadLine);
            }
        });
    }

    private void addContextMenu(final List<Div> divs, final Div div,
            final LoadTimeLine loadLine) {
        div.addEventListener("onRightClick", new EventListener() {
            @Override
            public void onEvent(Event event) throws Exception {
                try {
                    getContextMenuFor(divs, div, loadLine).open(div);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void schedule(final LoadTimeLine taskLine) {

        scheduleListeners
                .fireEvent(new IListenerNotification<ISeeScheduledOfListener>() {
                    @Override
                    public void doNotify(ISeeScheduledOfListener listener) {
                        listener.seeScheduleOf(taskLine);
                    }
                });
    }

    public void addSeeScheduledOfListener(
            ISeeScheduledOfListener seeScheduledOfListener) {
        scheduleListeners.addListener(seeScheduledOfListener);
    }

    private Map<Div, Menupopup> contextMenus = new HashMap<Div, Menupopup>();

    private Menupopup getContextMenuFor(final List<Div> divs, final Div div,
            final LoadTimeLine loadLine) {
        if (contextMenus.get(div) == null) {

            MenuBuilder<Div> menuBuilder = MenuBuilder.on(getPage(), divs);
            menuBuilder.item(_("See resource allocation"),
                    "/common/img/ico_allocation.png", new ItemAction<Div>() {

                        @Override
                        public void onEvent(Div choosen, Event event) {
                            schedule(loadLine);
                        }
                    });

            Menupopup result = menuBuilder.createWithoutSettingContext();
            contextMenus.put(div, result);
            return result;

        }
        return contextMenus.get(div);
    }

    public String getResourceLoadName() {
        return loadLine.getConceptName();
    }

    public String getResourceLoadType() {
        return loadLine.getType();
    }


    private static List<Div> createDivsForPeriods(IDatesMapper datesMapper,
            List<LoadPeriod> loadPeriods) {
        List<Div> result = new ArrayList<Div>();
        for (LoadPeriod loadPeriod : loadPeriods) {
            result.add(createDivForPeriod(datesMapper, loadPeriod));
        }
        return result;
    }

    private static Div createDivForPeriod(IDatesMapper datesMapper,
            LoadPeriod loadPeriod) {
        Div result = new Div();
        result.setClass(String.format("taskassignmentinterval %s", loadPeriod
                .getLoadLevel().getCategory()));

        String load = _("Load: {0}%", loadPeriod.getLoadLevel().getPercentage())
                + ", ";
        if (loadPeriod.getLoadLevel().getPercentage() == Integer.MAX_VALUE) {
            load = "";
        }
        result.setTooltiptext(load
                + _("total work hours: {0}, assigned hours: {1}", loadPeriod
                        .getTotalResourceWorkHours(), loadPeriod
                        .getAssignedHours()));

        result.setLeft(forCSS(getStartPixels(datesMapper, loadPeriod)));
        result.setWidth(forCSS(getWidthPixels(datesMapper, loadPeriod)));
        return result;
    }

    private static int getWidthPixels(IDatesMapper datesMapper,
            LoadPeriod loadPeriod) {
        LocalDate start = loadPeriod.getStart();
        LocalDate end = loadPeriod.getEnd();
        return datesMapper.toPixels(new Duration(
                start.toDateTimeAtStartOfDay(), end.toDateTimeAtStartOfDay()));
    }

    private static String forCSS(int pixels) {
        return String.format("%dpx", pixels);
    }

    private static int getStartPixels(IDatesMapper datesMapper,
            LoadPeriod loadPeriod) {
        return datesMapper.toPixels(loadPeriod.getStart());
    }

}