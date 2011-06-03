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

package org.navalplanner.web.common;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.easymock.EasyMock;
import org.junit.Test;
import org.navalplanner.web.common.ExceptionCatcherProxy.IExceptionHandler;

public class ExceptionCatcherProxyTest {

    private final class Dummy implements IDummy {
        @Override
        public void doFoo() {
            throw new IllegalArgumentException("bla");
        }

        @Override
        public void doBar() {
            throw new IllegalStateException("bla");
        }

        @Override
        public String getHello() {
            throw new RuntimeException("bla");
        }
    }

    public interface IDummy {
        void doFoo();

        String getHello();

        void doBar();

    }

    private IDummy dummy;

    public ExceptionCatcherProxyTest() {
        dummy = new Dummy();
    }

    @Test
    public void anAutomaticExceptionCatcherWrapsAnObjectImplementingAnInterface() {
        IDummy dummyMock = EasyMock.createMock(IDummy.class);
        dummyMock.doBar();
        dummyMock.doFoo();
        expect(dummyMock.getHello()).andReturn("hi");
        IDummy proxified = ExceptionCatcherProxy.doCatchFor(IDummy.class)
                .applyTo(dummyMock);
        assertNotNull(proxified);
        EasyMock.replay(dummyMock);
        proxified.doBar();
        proxified.doFoo();
        assertThat(proxified.getHello(), equalTo("hi"));
        verify(dummyMock);
    }

    @Test
    public void theSameExceptionsAreThrownIfNotHandlersRegistered() {
        IDummy proxified = ExceptionCatcherProxy.doCatchFor(IDummy.class)
                .applyTo(dummy);

        try {
            proxified.doBar();
            fail("must throw IllegalStateException");
        } catch (IllegalStateException e) {
            // ok
        }

        try {
            proxified.doFoo();
            fail("must throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // ok
        }

    }

    @Test
    public void itLetsToSpecifyWhatToDoForSomeExceptions() {
        final boolean[] onExceptionCalled = { false };
        IDummy proxified = ExceptionCatcherProxy.doCatchFor(IDummy.class).when(
                IllegalArgumentException.class,
                new IExceptionHandler<IllegalArgumentException>() {

                    @Override
                    public void onException(IllegalArgumentException exception) {
                        onExceptionCalled[0] = true;
                    }
                }).applyTo(dummy);
        proxified.doFoo();
        assertTrue(onExceptionCalled[0]);
    }

    @Test
    public void theMostSpecificHandlerIsUsed() {
        final boolean[] runtimeExceptionCalled = { false };
        IDummy proxified = ExceptionCatcherProxy.doCatchFor(IDummy.class).when(
                RuntimeException.class,
                new IExceptionHandler<RuntimeException>() {

                    @Override
                    public void onException(RuntimeException exception) {
                        runtimeExceptionCalled[0] = true;
                    }
                }).when(IllegalArgumentException.class,
                new IExceptionHandler<IllegalArgumentException>() {

                    @Override
                    public void onException(IllegalArgumentException exception) {
                        // do nothing

                    }
                }).applyTo(dummy);
        proxified.doFoo();
        assertFalse(runtimeExceptionCalled[0]);

    }

    @Test
    public void ifTheExceptionHandlerDontThrowExceptionReturnsNull() {
        IDummy dummyProxified = ExceptionCatcherProxy.doCatchFor(IDummy.class)
                .when(RuntimeException.class,
                        new IExceptionHandler<RuntimeException>() {

                            @Override
                            public void onException(RuntimeException exception) {
                                // do nothing
                            }
                        }).applyTo(dummy);
        try {
            dummy.getHello();
            fail("it throws error");
        } catch (RuntimeException e) {
            // ok
        }
        assertNull(dummyProxified.getHello());

    }

}
