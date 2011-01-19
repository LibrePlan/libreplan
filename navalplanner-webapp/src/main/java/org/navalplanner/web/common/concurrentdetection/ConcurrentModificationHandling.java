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

package org.navalplanner.web.common.concurrentdetection;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.dao.OptimisticLockingFailureException;

/**
 * Responsible of handling {@link OptimisticLockingFailureException} on Spring
 * beans marked with {@link OnConcurrentModification}
 * @author Óscar González Fernández <ogonzalez@igalia.com>
 */
@Aspect
@Order(0)
public class ConcurrentModificationHandling {

    public ConcurrentModificationHandling() {
    }

    @SuppressWarnings("unused")
    @Pointcut("@within(onConcurrentModification))")
    private void methodWithinConcurrentModificationMarkedType(
            OnConcurrentModification onConcurrentModification) {
    }

    /**
     * It intercepts the calls to public methods of Spring beans marked with
     * {@link OnConcurrentModification}. When an
     * {@link OptimisticLockingFailureException} happens the page for concurrent
     * modification is shown and the user is returned to the page specified by
     * {@link OnConcurrentModification}
     * @param jointPoint
     * @param onConcurrentModification
     *            the annotation applied to object's type
     * @return the object that would be originally returned
     */
    @Around("methodWithinConcurrentModificationMarkedType(onConcurrentModification)"
            + " && execution(public * * (..))")
    public Object whenConcurrentModification(ProceedingJoinPoint jointPoint,
            OnConcurrentModification onConcurrentModification) throws Throwable {
        try {
            return jointPoint.proceed(jointPoint.getArgs());
        } catch (OptimisticLockingFailureException e) {
            ConcurrentModificationController.showException(e,
                    onConcurrentModification.goToPage());
            throw e;
        }
    }
}
