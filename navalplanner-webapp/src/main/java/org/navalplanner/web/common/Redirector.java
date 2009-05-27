package org.navalplanner.web.common;

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
import org.zkoss.zk.ui.Execution;

/**
 * <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class Redirector<T> {

    private static final Log LOG = LogFactory.getLog(Redirector.class);

    private static class LinkMetadata {
        private final Method method;

        private final LinkToState annotation;

        private LinkMetadata(Method method, LinkToState annotation) {
            this.method = method;
            this.annotation = annotation;
        }
    }

    private final ExecutorRetriever executorRetriever;

    private Map<String, LinkMetadata> metadata = new HashMap<String, LinkMetadata>();

    private final String page;

    private final IConverterFactory converterFactory;

    public Redirector(IConverterFactory converterFactory,
            ExecutorRetriever executorRetriever, Class<T> interfaceDefiningLinks) {
        Validate.isTrue(interfaceDefiningLinks.isInterface());
        this.converterFactory = converterFactory;
        this.executorRetriever = executorRetriever;
        LinksDefiner linkDefiner = interfaceDefiningLinks
                .getAnnotation(LinksDefiner.class);
        Validate
                .notNull(linkDefiner, LinksDefiner.class.getName()
                        + " annotation required on "
                        + interfaceDefiningLinks.getName());
        this.page = linkDefiner.page();
        for (Method method : interfaceDefiningLinks.getMethods()) {
            LinkToState linkToState = method.getAnnotation(LinkToState.class);
            if (linkToState != null) {
                metadata.put(method.getName(), new LinkMetadata(method,
                        linkToState));
            }
        }
    }

    public void doRedirect(String methodName, Object... values) {
        if (!metadata.containsKey(methodName)) {
            LOG.error("Method " + methodName
                    + "doesn't represent a state(It doesn't have a "
                    + LinkToState.class.getSimpleName()
                    + " annotation). Nothing will be done");
            return;
        }
        LinkMetadata linkableMetadata = metadata.get(methodName);
        Class<?>[] types = linkableMetadata.method.getParameterTypes();
        String[] parameterNames = linkableMetadata.annotation.value();
        String[] stringRepresentations = new String[parameterNames.length];
        for (int i = 0; i < types.length; i++) {
            Class<?> type = types[i];
            Converter<?> converterFor = converterFactory.getConverterFor(type);
            stringRepresentations[i] = converterFor
                    .asStringUngeneric(values[i]);
        }
        StringBuilder linkValue = new StringBuilder(page);
        for (int i = 0; i < parameterNames.length; i++) {
            linkValue.append(";").append(parameterNames[i]);
            if (stringRepresentations[i] != null)
                linkValue.append("=").append(stringRepresentations[i]);
        }
        executorRetriever.getCurrent().sendRedirect(linkValue.toString());
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

    public <S extends T> void applyTo(S controller) {
        Execution current = executorRetriever.getCurrent();
        Map<String, String> matrixParams = MatrixParameters
                .extract((HttpServletRequest) current.getNativeRequest());
        Set<String> matrixParamsNames = matrixParams.keySet();
        for (Entry<String, LinkMetadata> entry : metadata.entrySet()) {
            LinkMetadata linkMetadata = entry.getValue();
            LinkToState linkToStateAnnotation = linkMetadata.annotation;
            HashSet<String> requiredParams = new HashSet<String>(Arrays
                    .asList(linkToStateAnnotation.value()));
            if (matrixParamsNames.equals(requiredParams)) {
                Object[] arguments = retrieveArguments(matrixParams,
                        linkToStateAnnotation, linkMetadata.method
                                .getParameterTypes());
                callMethod(controller, linkMetadata.method, arguments);
                return;
            }
        }
    }

    private Object[] retrieveArguments(Map<String, String> matrixParams,
            LinkToState linkToStateAnnotation, Class<?>[] parameterTypes) {
        Object[] result = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Object argumentName = linkToStateAnnotation.value()[i];
            String parameterValue = matrixParams.get(argumentName);
            Converter<?> converter = converterFactory
                    .getConverterFor(parameterTypes[i]);
            result[i] = converter.asObject(parameterValue);
        }
        return result;
    }
}
