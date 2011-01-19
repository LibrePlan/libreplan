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

package org.navalplanner.web.common.entrypoints;

import static org.navalplanner.web.I18nHelper._;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.navalplanner.web.common.converters.IConverter;
import org.navalplanner.web.common.converters.IConverterFactory;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.BookmarkEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

/**
 * <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class URLHandler<T> {

    private static final String MANUALLY_SET_PARAMS = "PARAMS";

    private static final String FLAG_ATTRIBUTE = URLHandler.class.getName()
            + "_";

    private static final Log LOG = LogFactory.getLog(URLHandler.class);

    private static class EntryPointMetadata {
        private final Method method;

        private final EntryPoint annotation;

        private EntryPointMetadata(Method method, EntryPoint annotation) {
            this.method = method;
            this.annotation = annotation;
        }
    }

    private final IExecutorRetriever executorRetriever;

    private Map<String, EntryPointMetadata> metadata = new HashMap<String, EntryPointMetadata>();

    private final String page;

    private final IConverterFactory converterFactory;

    public static void setupEntryPointsForThisRequest(
            HttpServletRequest request, Map<String, String> entryPoints) {
        request.setAttribute(MANUALLY_SET_PARAMS, entryPoints);
    }

    public URLHandler(IConverterFactory converterFactory,
            IExecutorRetriever executorRetriever,
            Class<T> interfaceDefiningEntryPoints) {
        Validate.isTrue(interfaceDefiningEntryPoints.isInterface());
        this.converterFactory = converterFactory;
        this.executorRetriever = executorRetriever;
        EntryPoints entryPoints = interfaceDefiningEntryPoints
                .getAnnotation(EntryPoints.class);
        Validate.notNull(entryPoints,
                _("{0} annotation required on {1}", EntryPoints.class.getName(),
                    interfaceDefiningEntryPoints.getName()));
        this.page = entryPoints.page();
        for (Method method : interfaceDefiningEntryPoints.getMethods()) {
            EntryPoint entryPoint = method.getAnnotation(EntryPoint.class);
            if (entryPoint != null) {
                metadata.put(method.getName(), new EntryPointMetadata(method,
                        entryPoint));
            }
        }
    }

    public void doTransition(String methodName, Object... values) {
        if (isFlagedInThisRequest()) {
            return;
        }
        flagAlreadyExecutedInThisRequest();
        if (!metadata.containsKey(methodName)) {
            LOG.error("Method " + methodName
                    + "doesn't represent a state(It doesn't have a "
                    + EntryPoint.class.getSimpleName()
                    + " annotation). Nothing will be done");
            return;
        }
        EntryPointMetadata linkableMetadata = metadata.get(methodName);
        Class<?>[] types = linkableMetadata.method.getParameterTypes();
        String[] parameterNames = linkableMetadata.annotation.value();
        String[] stringRepresentations = new String[parameterNames.length];
        for (int i = 0; i < types.length; i++) {
            Class<?> type = types[i];
            IConverter<?> converterFor = converterFactory.getConverterFor(type);
            stringRepresentations[i] = converterFor
                    .asStringUngeneric(values[i]);
        }
        String fragment = getFragment(parameterNames, stringRepresentations);
        String requestPath = executorRetriever.getCurrent().getDesktop()
                .getRequestPath();
        if (requestPath.contains(page)) {
            doBookmark(fragment);
        } else {
            sendRedirect(fragment);
        }
    }

    private boolean isFlagedInThisRequest() {
        return getRequest().getAttribute(FLAG_ATTRIBUTE) == this;
    }

    private void flagAlreadyExecutedInThisRequest() {
        getRequest().setAttribute(FLAG_ATTRIBUTE, this);
    }

    private void doBookmark(String fragment) {
        executorRetriever.getCurrent().getDesktop().setBookmark(
                stripHash(fragment));
    }

    private String stripHash(String fragment) {
        if (fragment.startsWith("#")) {
            return fragment.substring(1);
        }
        return fragment;
    }

    private void sendRedirect(String fragment) {
        StringBuilder linkValue = new StringBuilder(page).append(fragment);
        executorRetriever.getCurrent().sendRedirect(linkValue.toString());
    }

    private String getFragment(String[] parameterNames,
            String[] stringRepresentations) {
        StringBuilder result = new StringBuilder();
        if (parameterNames.length > 0) {
            result.append("#");
        }
        for (int i = 0; i < parameterNames.length; i++) {
            result.append(parameterNames[i]);
            if (stringRepresentations[i] != null) {
                result.append("=").append(stringRepresentations[i]);
            }
            if (i < parameterNames.length - 1) {
                result.append(";");
            }
        }
        return result.toString();
    }

    private static void callMethod(Object target, Method superclassMethod,
            Object[] params) {
        try {
            Method method = target.getClass().getMethod(
                    superclassMethod.getName(),
                    superclassMethod.getParameterTypes());
            method.invoke(target, params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <S extends T> boolean applyIfMatches(S controller) {
        HttpServletRequest request = getRequest();
        if (request.getAttribute(MANUALLY_SET_PARAMS) != null) {
            return applyIfMatches(controller, (Map<String, String>) request
                    .getAttribute(MANUALLY_SET_PARAMS));
        }
        return applyIfMatches(controller, request.getRequestURI());
    }

    private HttpServletRequest getRequest() {
        Execution current = executorRetriever.getCurrent();
        HttpServletRequest request = (HttpServletRequest) current
                .getNativeRequest();
        return request;
    }

    public <S extends T> boolean applyIfMatches(S controller, String fragment) {
        if (isFlagedInThisRequest()) {
            return false;
        }
        String string = insertSemicolonIfNeeded(fragment);
        Map<String, String> matrixParams = MatrixParameters.extract(string);
        return applyIfMatches(controller, matrixParams);
    }

    private <S> boolean applyIfMatches(S controller,
            Map<String, String> matrixParams) {
        flagAlreadyExecutedInThisRequest();
        Set<String> matrixParamsNames = matrixParams.keySet();
        for (Entry<String, EntryPointMetadata> entry : metadata.entrySet()) {
            EntryPointMetadata entryPointMetadata = entry.getValue();
            EntryPoint entryPointAnnotation = entryPointMetadata.annotation;
            HashSet<String> requiredParams = new HashSet<String>(Arrays
                    .asList(entryPointAnnotation.value()));
            if (matrixParamsNames.equals(requiredParams)) {
                Object[] arguments = retrieveArguments(matrixParams,
                        entryPointAnnotation, entryPointMetadata.method
                                .getParameterTypes());
                callMethod(controller, entryPointMetadata.method, arguments);
                return true;
            }
        }
        return false;
    }

    public <S extends T> void registerListener(final S controller, Page page) {
        page.addEventListener("onBookmarkChange", new EventListener() {

            @Override
            public void onEvent(Event event) throws Exception {
                BookmarkEvent bookmarkEvent = (BookmarkEvent) event;
                String bookmark = bookmarkEvent.getBookmark();
                applyIfMatches(controller, bookmark);
            }
        });
    }

    private String insertSemicolonIfNeeded(String uri) {
        if (!uri.startsWith(";")) {
            return ";" + uri;
        }
        return uri;
    }

    private Object[] retrieveArguments(Map<String, String> matrixParams,
            EntryPoint linkToStateAnnotation, Class<?>[] parameterTypes) {
        Object[] result = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Object argumentName = linkToStateAnnotation.value()[i];
            String parameterValue = matrixParams.get(argumentName);
            IConverter<?> converter = converterFactory
                    .getConverterFor(parameterTypes[i]);
            result[i] = converter.asObject(parameterValue);
        }
        return result;
    }
}
