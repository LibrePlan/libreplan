/*
 * This file is part of LibrePlan
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
package org.libreplan.business.hibernate.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.transaction.Status;
import javax.transaction.Synchronization;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Transaction;
import org.hibernate.event.AbstractEvent;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.proxy.HibernateProxy;

/**
 * @author Óscar González Fernández
 *
 */
public class HibernateDatabaseModificationsListener implements
        PostInsertEventListener, PostUpdateEventListener,
        PostDeleteEventListener, ISnapshotRefresherService {

    private static final Log LOG = LogFactory
            .getLog(HibernateDatabaseModificationsListener.class);

    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    private final ConcurrentMap<Class<?>, BlockingQueue<NotBlockingAutoUpdatedSnapshot<?>>> interested;

    private ConcurrentMap<Transaction, Dispatcher> pending = new ConcurrentHashMap<Transaction, Dispatcher>();

    private Set<NotBlockingAutoUpdatedSnapshot<?>> snapshotsInterestedOn(
            Class<?> entityClass) {
        List<Class<?>> list = new ArrayList<Class<?>>(1);
        list.add(entityClass);
        return snapshotsInterestedOn(list);
    }

    private Set<NotBlockingAutoUpdatedSnapshot<?>> snapshotsInterestedOn(
            Collection<? extends Class<?>> classesList) {
        Set<NotBlockingAutoUpdatedSnapshot<?>> result = new HashSet<NotBlockingAutoUpdatedSnapshot<?>>();
        for (Class<?> each : new HashSet<Class<?>>(classesList)) {
            BlockingQueue<NotBlockingAutoUpdatedSnapshot<?>> queue = interested
                    .get(each);
            if (queue != null) {
                result.addAll(queue);
            }
        }
        return result;
    }

    private final class Dispatcher implements Synchronization {

        private BlockingQueue<Class<?>> classes = new LinkedBlockingQueue<Class<?>>();
        private final Transaction transaction;

        public Dispatcher(Transaction transaction, Class<?> entityClass) {
            classes.offer(entityClass);
            this.transaction = transaction;
        }

        public void add(Class<?> entityClass) {
            classes.offer(entityClass);
        }

        @Override
        public void beforeCompletion() {
        }

        @Override
        public void afterCompletion(int status) {
            LOG.info("transaction completed with status: " + status);
            pending.remove(transaction);
            if (isProbablySucessful(status)) {
                List<Class<?>> list = new ArrayList<Class<?>>();
                classes.drainTo(list);
                LOG.info(list.size() + " modification events recorded");
                Set<NotBlockingAutoUpdatedSnapshot<?>> toDispatch = snapshotsInterestedOn(list);
                LOG.info("dispatching "
                        + toDispatch
                        + " snapshots to reload due to transaction successful completion");
                dispatch(toDispatch);
            }
        }

        private boolean isProbablySucessful(int status) {
            return status != Status.STATUS_ROLLEDBACK
                    && status != Status.STATUS_ROLLING_BACK;
        }

    }

    public HibernateDatabaseModificationsListener() {
        interested = new ConcurrentHashMap<Class<?>, BlockingQueue<NotBlockingAutoUpdatedSnapshot<?>>>();
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        modificationOn(inferTransaction(event),
                inferEntityClass(getEntityObject(event)));
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        modificationOn(inferTransaction(event),
                inferEntityClass(getEntityObject(event)));
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        modificationOn(inferTransaction(event),
                inferEntityClass(getEntityObject(event)));
    }

    private Transaction inferTransaction(AbstractEvent event) {
        return event.getSession().getTransaction();
    }

    private Object getEntityObject(PostInsertEvent event) {
        return event.getEntity();
    }

    private static Object getEntityObject(PostDeleteEvent event) {
        return event.getEntity();
    }

    private static Object getEntityObject(PostUpdateEvent event) {
        return event.getEntity();
    }

    private static Class<?> inferEntityClass(Object entity) {
        if (entity instanceof HibernateProxy) {
            HibernateProxy proxy = (HibernateProxy) entity;
            return proxy.getHibernateLazyInitializer().getPersistentClass();
        }
        return entity.getClass();
    }

    void modificationOn(Transaction transaction, Class<?> entityClass) {
        if (transaction == null) {
            dispatch(snapshotsInterestedOn(entityClass));
            return;
        }
        Dispatcher newDispatcher = new Dispatcher(transaction, entityClass);
        Dispatcher previous = null;
        previous = pending.putIfAbsent(transaction, newDispatcher);

        boolean dispatcherAlreadyExisted = previous != null;
        if (dispatcherAlreadyExisted) {
            previous.add(entityClass);
        } else {
            transaction.registerSynchronization(newDispatcher);
        }
    }

    private void dispatch(Set<NotBlockingAutoUpdatedSnapshot<?>> toBeDispatched) {
        for (NotBlockingAutoUpdatedSnapshot<?> each : toBeDispatched) {
            dispatch(each);
        }
    }

    private void dispatch(NotBlockingAutoUpdatedSnapshot<?> each) {
        each.reloadNeeded(executor);
    }

    @Override
    public <T> IAutoUpdatedSnapshot<T> takeSnapshot(String name,
            Callable<T> callable, ReloadOn reloadOn) {
        final NotBlockingAutoUpdatedSnapshot<T> result;
        result = new NotBlockingAutoUpdatedSnapshot<T>(name, callable);
        for (Class<?> each : reloadOn.getClassesOnWhichToReload()) {
            interested.putIfAbsent(each, emptyQueue());
            BlockingQueue<NotBlockingAutoUpdatedSnapshot<?>> queue = interested
                    .get(each);
            boolean success = queue.add(result);
            assert success : "the type of queue used must not have restricted capacity";
        }
        result.ensureFirstLoad(executor);
        return result;
    }

    private BlockingQueue<NotBlockingAutoUpdatedSnapshot<?>> emptyQueue() {
        return new LinkedBlockingQueue<NotBlockingAutoUpdatedSnapshot<?>>();
    }

}
