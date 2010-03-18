/*
 * This file is part of NavalPlan
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
package org.navalplanner.web.planner.order;

import java.util.HashMap;
import java.util.Map;

import org.navalplanner.business.orders.entities.Order;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;

/**
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 *
 */
public class PlanningStateTracker {

    public static class SessionStore {
        private Map<Long, PlanningStateTracker> planningStatesByOrder = new HashMap<Long, PlanningStateTracker>();

        public PlanningStateTracker retrieve(Order order) {
            return planningStatesByOrder.get(order.getId());
        }

        public void add(PlanningStateTracker planningStateTracker) {
            planningStatesByOrder.put(planningStateTracker.originalOrder
                    .getId(), planningStateTracker);
        }

        public void clear(Order order) {
            planningStatesByOrder.remove(order.getId());
        }

    }

    private static final String TRACKER_ATTRIBUTE = "tracker";

    public PlanningStateTracker(Order order, PlanningState planningState) {
        this.originalOrder = order;
        this.planningState = planningState;
    }

    public static PlanningStateTracker retrieve(Order order) {
        SessionStore tracker = getSessionStore();
        return tracker.retrieve(order);
    }

    public static void cancel(Order order) {
        getSessionStore().clear(order);
    }

    public static void storeInSession(Order order, PlanningState planningState){
        SessionStore store = getSessionStore();
        store.add(new PlanningStateTracker(order, planningState));
    }

    private static SessionStore getSessionStore() {
        Execution execution = Executions.getCurrent();
        Session session = execution.getDesktop().getSession();
        Object storeOnSession = session.getAttribute(TRACKER_ATTRIBUTE);
        if (storeOnSession != null) {
            return (SessionStore) storeOnSession;
        }
        SessionStore result = new SessionStore();
        session.setAttribute(TRACKER_ATTRIBUTE, result);
        return result;
    }

    private final Order originalOrder;

    private final PlanningState planningState;

    public Order getOriginalOrder() {
        return originalOrder;
    }

    public PlanningState getPlanningState() {
        return planningState;
    }

}
