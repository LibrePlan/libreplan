package org.navalplanner.web.common.entrypoints;

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
import org.navalplanner.web.common.converters.Converter;
import org.navalplanner.web.common.converters.IConverterFactory;
import org.zkoss.zk.ui.Execution;

/**
 * <br />
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
public class URLHandler<T> {

    private static final Log LOG = LogFactory.getLog(URLHandler.class);

    private static class EntryPointMetadata {
        private final Method method;

        private final EntryPoint annotation;

        private EntryPointMetadata(Method method, EntryPoint annotation) {
            this.method = method;
            this.annotation = annotation;
        }
    }

    private final ExecutorRetriever executorRetriever;

    private Map<String, EntryPointMetadata> metadata = new HashMap<String, EntryPointMetadata>();

    private final String page;

    private final IConverterFactory converterFactory;

    public URLHandler(IConverterFactory converterFactory,
            ExecutorRetriever executorRetriever,
            Class<T> interfaceDefiningEntryPoints) {
        Validate.isTrue(interfaceDefiningEntryPoints.isInterface());
        this.converterFactory = converterFactory;
        this.executorRetriever = executorRetriever;
        EntryPoints entryPoints = interfaceDefiningEntryPoints
                .getAnnotation(EntryPoints.class);
        Validate.notNull(entryPoints, EntryPoints.class.getName()
                + " annotation required on "
                + interfaceDefiningEntryPoints.getName());
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

    public <S extends T> void applyIfMatches(S controller) {
        Execution current = executorRetriever.getCurrent();
        Map<String, String> matrixParams = MatrixParameters
                .extract((HttpServletRequest) current.getNativeRequest());
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
                return;
            }
        }
    }

    private Object[] retrieveArguments(Map<String, String> matrixParams,
            EntryPoint linkToStateAnnotation, Class<?>[] parameterTypes) {
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
