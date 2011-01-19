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

package org.navalplanner.business.common;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdHocTransactionService implements IAdHocTransactionService {

    private static <T> T proxy(IAdHocTransactionService transactionService,
            boolean readOnly,
            Class<T> interfaceClass,
            T interfaceObject) {
        Class<?>[] interfaces = { interfaceClass };
        return interfaceClass.cast(Proxy.newProxyInstance(interfaceClass
                .getClassLoader(), interfaces, createHandler(interfaceObject,
                transactionService, readOnly)));
    }

    /**
     * Returns a new object implementing the same interface but with its calls
     * wrapped on read only transactions
     * @param transactionService
     * @param interfaceClass
     * @param interfaceObject
     * @return
     */
    public static <T> T readOnlyProxy(IAdHocTransactionService transactionService,
            Class<T> interfaceClass, T interfaceObject) {
        return proxy(transactionService, true, interfaceClass, interfaceObject);
    }

    /**
     * Returns a new object implementing the same interface but with its calls
     * wrapped on transactions
     * @param transactionService
     * @param interfaceClass
     * @param interfaceObject
     * @return
     */
    public static <T> T proxy(IAdHocTransactionService transactionService,
            Class<T> interfaceClass, T interfaceObject) {
        return proxy(transactionService, false, interfaceClass, interfaceObject);
    }

    private static InvocationHandler createHandler(final Object originalObject,
            final IAdHocTransactionService transactionService,
            final boolean readOnly) {
        return new InvocationHandler() {
            @Override
            public Object invoke(final Object proxy, final Method method,
                    final Object[] args) throws Throwable {
                IOnTransaction<Object> onTransaction = createOnTransaction(originalObject, method, args);
                try {
                    if (readOnly) {
                        return transactionService
                                .runOnReadOnlyTransaction(onTransaction);
                    } else {
                        return transactionService.runOnTransaction(onTransaction);
                    }
                } catch (RuntimeException e) {
                    throw e.getCause();
                }
            }
        };
    }

    private static IOnTransaction<Object> createOnTransaction(
            final Object originalObject, final Method method,
            final Object[] args) {
        return new IOnTransaction<Object>() {

            @Override
            public Object execute() {
                try {
                    return method.invoke(originalObject, args);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Transactional
    public <T> T runOnTransaction(IOnTransaction<T> onTransaction) {
        return onTransaction.execute();
    }

    @Override
    @Transactional(readOnly = true)
    public <T> T runOnReadOnlyTransaction(IOnTransaction<T> onTransaction) {
        return onTransaction.execute();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public <T> T runOnAnotherTransaction(IOnTransaction<T> onTransaction) {
        return onTransaction.execute();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public <T> T runOnAnotherReadOnlyTransaction(IOnTransaction<T> onTransaction) {
        return onTransaction.execute();
    }

}
