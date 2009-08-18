package org.navalplanner.web.common;

import org.navalplanner.web.common.ExceptionCatcherProxy.IExceptionHandler;
import org.springframework.dao.OptimisticLockingFailureException;

public class ConcurrentModificationDetector {

    public static <T> T addAutomaticHandlingOfConcurrentModification(Class<T> interfaceClass,
            T model, final String backURL) {
        IExceptionHandler<OptimisticLockingFailureException> handler = createHandler(backURL);
        return ExceptionCatcherProxy.doCatchFor(interfaceClass).when(
                OptimisticLockingFailureException.class, handler)
                .applyTo(model);
    }

    private static IExceptionHandler<OptimisticLockingFailureException> createHandler(
            final String backURL) {
        return new IExceptionHandler<OptimisticLockingFailureException>() {

            @Override
            public void onException(OptimisticLockingFailureException exception) {
                ConcurrentModificationController.showException(exception,
                        backURL);
            }
        };
    }

}
