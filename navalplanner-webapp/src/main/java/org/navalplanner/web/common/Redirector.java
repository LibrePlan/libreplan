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
import org.zkoss.zk.ui.Execution;

/**
 * <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class Redirector<T> {

    private static class LinkableMetadata {
        private final Method method;

        private final Linkable annotation;

        private LinkableMetadata(Method method, Linkable annotation) {
            this.method = method;
            this.annotation = annotation;
        }
    }

    private final ExecutorRetriever executorRetriever;

    private Map<String, LinkableMetadata> metadata = new HashMap<String, LinkableMetadata>();

    private final String page;

    private final IConverterFactory converterFactory;

    public Redirector(IConverterFactory converterFactory,
            ExecutorRetriever executorRetriever,
            Class<T> klassWithLinkableMetadata) {
        this.converterFactory = converterFactory;
        this.executorRetriever = executorRetriever;
        Page pageAnnotation = klassWithLinkableMetadata
                .getAnnotation(Page.class);
        Validate.notNull(pageAnnotation, Page.class.getName()
                + " annotation required on "
                + klassWithLinkableMetadata.getName());
        this.page = pageAnnotation.value();
        for (Method method : klassWithLinkableMetadata.getMethods()) {
            Linkable linkable = method.getAnnotation(Linkable.class);
            if (linkable != null) {
                metadata.put(method.getName(), new LinkableMetadata(method,
                        linkable));
            }
        }
    }

    public void doRedirect(Object... values) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement invoker = stackTrace[2];
        String methodName = invoker.getMethodName();
        if (!metadata.containsKey(methodName)) {
            throw new RuntimeException(
                    "It's not invoked on a method in which there is linkable information. Method is: "
                            + methodName);
        }
        LinkableMetadata linkableMetadata = metadata.get(methodName);
        Class<?>[] types = linkableMetadata.method.getParameterTypes();
        int i = 0;
        String[] parameterNames = linkableMetadata.annotation.value();
        String[] associatedValues = new String[parameterNames.length];
        for (Class<?> type : types) {
            Converter<?> converterFor = converterFactory.getConverterFor(type);
            associatedValues[i] = converterFor.asStringUngeneric(values[i]);
            i++;
        }
        StringBuilder linkValue = new StringBuilder(page);
        for (int j = 0; j < parameterNames.length; j++) {
            String value = associatedValues[j];
            linkValue.append(";").append(parameterNames[j]);
            if (value != null)
                linkValue.append("=").append(value);
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
        for (Entry<String, LinkableMetadata> entry : metadata.entrySet()) {
            LinkableMetadata linkableMetadata = entry.getValue();
            Linkable annotation = linkableMetadata.annotation;
            HashSet<String> requiredParams = new HashSet<String>(Arrays
                    .asList(annotation.value()));
            if (matrixParamsNames.equals(requiredParams)) {
                Class<?>[] parameterTypes = linkableMetadata.method
                        .getParameterTypes();
                Object[] arguments = new Object[parameterTypes.length];
                for (int i = 0; i < parameterTypes.length; i++) {
                    Object argumentName = annotation.value()[i];
                    String parameterValue = matrixParams.get(argumentName);
                    Converter<?> converter = converterFactory
                            .getConverterFor(parameterTypes[i]);
                    arguments[i] = converter.asObject(parameterValue);
                }
                callMethod(controller, linkableMetadata.method, arguments);
                return;
            }
        }
    }
}
