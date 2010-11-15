/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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
package org.zkoss.ganttz.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.Validate;
import org.zkoss.web.servlet.http.HttpServlet;

/**
 * Servlet that allows to register custom responses. It must be declared at
 * web.xml having a load-on-startup element.
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class CallbackServlet extends HttpServlet {

    private static final String MAPPING = "/callback/";

    private static final long CLEANING_PERIOD_MILLIS = 1000 * 60 * 10; // ten
    // minutes

    private static final long EXPIRATION_TIME_MILLIS = 1000 * 60 * 30; // half
    // hour;

    private static Random random = new Random();

    private static ConcurrentMap<String, HandlerWithRegisterTime> handlersCallbacks = new ConcurrentHashMap<String, HandlerWithRegisterTime>();

    private static Timer cleaningTimer = new Timer(true);

    public interface IServletRequestHandler {
        public void handle(HttpServletRequest request,
                HttpServletResponse response) throws ServletException,
                IOException;
    }

    private static class HandlerWithRegisterTime {
        private final IServletRequestHandler handler;
        private final long creationTime;

        public HandlerWithRegisterTime(IServletRequestHandler handler) {
            Validate.notNull(handler);
            this.handler = handler;
            this.creationTime = System.currentTimeMillis();
        }

        boolean hasExpired() {
            return System.currentTimeMillis() - creationTime > EXPIRATION_TIME_MILLIS;
        }
    }

    public static String registerAndCreateURLFor(HttpServletRequest request,
            IServletRequestHandler handler) {
        return registerAndCreateURLFor(request, handler, true);
    }

    public static String registerAndCreateURLFor(HttpServletRequest request,
            IServletRequestHandler handler, boolean withContextPath) {
        // theoretically could be an infinite loop, could be improved.
        String generatedKey = "";
        HandlerWithRegisterTime toBeRegistered = new HandlerWithRegisterTime(
                handler);
        do {
            generatedKey = generateKey();
        } while (handlersCallbacks.putIfAbsent(generatedKey, toBeRegistered) != null);
        return buildURLFromKey(request, generatedKey, withContextPath);
    }

    private static synchronized String buildURLFromKey(
            HttpServletRequest request, String generatedKey,
            boolean withContextPath) {
        String contextPath = withContextPath ? request.getContextPath() : "";
        return contextPath + MAPPING + generatedKey;
    }

    private static String generateKey() {
        return "" + random.nextInt(Integer.MAX_VALUE);
    }

    private static String getId(String pathInfo) {
        if (pathInfo.startsWith("/")) {
            return pathInfo.substring(1);
        }
        return pathInfo;
    }

    private static void cleanExpired() {
        remove(findExpired());
    }

    private static void remove(List<String> expired) {
        for (String key : expired) {
            handlersCallbacks.remove(key);
        }
    }

    private static List<String> findExpired() {
        ArrayList<Entry<String, HandlerWithRegisterTime>> handlersList = new ArrayList<Entry<String, HandlerWithRegisterTime>>(
                handlersCallbacks.entrySet());
        List<String> expired = new ArrayList<String>();
        for (Entry<String, HandlerWithRegisterTime> entry : handlersList) {
            if (entry.getValue().hasExpired()) {
                expired.add(entry.getKey());
            }
        }
        return expired;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        scheduleTimer();
    }

    private void scheduleTimer() {
        cleaningTimer.schedule(cleaningTask(), CLEANING_PERIOD_MILLIS,
                CLEANING_PERIOD_MILLIS);
    }

    private TimerTask cleaningTask() {
        return new TimerTask() {
            @Override
            public void run() {
                cleanExpired();
            }
        };
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String callbackId = getId(req.getPathInfo());
        IServletRequestHandler handler = handlerFor(callbackId);
        if (handler == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            handler.handle(req, resp);
        }
    }

    private IServletRequestHandler handlerFor(String callbackId) {
        HandlerWithRegisterTime h = handlersCallbacks.get(callbackId);
        return h != null ? h.handler : null;
    }

}
