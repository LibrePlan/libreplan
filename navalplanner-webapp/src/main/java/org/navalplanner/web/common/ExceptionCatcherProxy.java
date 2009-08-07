package org.navalplanner.web.common;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.Validate;

public class ExceptionCatcherProxy<T> {

    public interface IExceptionHandler<T extends Exception> {
        public void onException(T exception);
    }

    public static <T> ExceptionCatcherProxy<T> doCatchFor(
            Class<T> interfaceKlass) {
        return new ExceptionCatcherProxy<T>(interfaceKlass);
    }

    private static class RegisteredHandler<T extends Exception> {
        private final Class<T> exceptionClass;

        private final IExceptionHandler<T> handler;

        RegisteredHandler(Class<T> exceptionClass, IExceptionHandler<T> handler) {
            this.exceptionClass = exceptionClass;
            this.handler = handler;
        }

        void invoke(Exception e) {
            handler.onException(exceptionClass.cast(e));
        }

        boolean isAppyable(Throwable cause) {
            return exceptionClass.isInstance(cause);
        }

        public boolean isMoreSpecificThan(RegisteredHandler<?> r) {
            return r.exceptionClass.isAssignableFrom(this.exceptionClass);
        }

    }

    private List<RegisteredHandler<?>> handlers = new LinkedList<RegisteredHandler<?>>();
    private final Class<T> interfaceKlass;

    private ExceptionCatcherProxy(Class<T> interfaceKlass) {
        this.interfaceKlass = interfaceKlass;
        Validate.isTrue(interfaceKlass.isInterface());
    }

    public T applyTo(final T instance) {
        return interfaceKlass.cast(Proxy.newProxyInstance(instance.getClass()
                .getClassLoader(), new Class[] { interfaceKlass },
                new InvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method method,
                            Object[] args) throws Throwable {
                        Object result;
                        try {
                            result = method.invoke(instance, args);
                            return result;
                        } catch (InvocationTargetException e) {
                            Throwable cause = e.getCause();
                            if (!handled(cause)) {
                                throw cause;
                            }
                            // we don't know what would be the result, so we
                            // return null
                            return null;
                        } finally {
                        }
                    }

                }));
    }

    private boolean handled(Throwable cause) {
        if (!(cause instanceof Exception)) {
            return false;
        }
        Exception exception = (Exception) cause;
        for (RegisteredHandler<?> registeredHandler : handlers) {
            if (registeredHandler.isAppyable(cause)) {
                registeredHandler.invoke(exception);
                return true;
            }
        }
        return false;
    }

    public <E extends Exception> ExceptionCatcherProxy<T> when(
            Class<E> exception, IExceptionHandler<E> handler) {
        RegisteredHandler<E> registered = new RegisteredHandler<E>(exception,
                handler);
        insertAtRightPosition(registered);
        return this;
    }

    private void insertAtRightPosition(RegisteredHandler<?> handler) {
        int i = 0;
        for (RegisteredHandler<?> r : handlers) {
            if (handler.isMoreSpecificThan(r)) {
                break;
            }
            i++;
        }
        handlers.add(i, handler);
    }

}
