/*
 * This file is part of ###PROJECT_NAME###
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

package org.navalplanner.web.common;

import org.navalplanner.web.common.ExceptionCatcherProxy.IExceptionHandler;
import org.springframework.dao.OptimisticLockingFailureException;

public class ConcurrentModificationDetector {

    private ConcurrentModificationDetector() {
    }

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
