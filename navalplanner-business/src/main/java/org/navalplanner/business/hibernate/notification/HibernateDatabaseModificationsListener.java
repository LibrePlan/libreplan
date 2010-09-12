/*
 * This file is part of NavalPlan
 *
 * Copyright (C) 2009-2010 Fundación para o Fomento da Calidade Industrial e
 *                         Desenvolvemento Tecnolóxico de Galicia
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
package org.navalplanner.business.hibernate.notification;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    public HibernateDatabaseModificationsListener() {
        interested = new ConcurrentHashMap<Class<?>, BlockingQueue<NotBlockingAutoUpdatedSnapshot<?>>>();
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        modificationOn(inferEntityClass(getEntityObject(event)));
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        modificationOn(inferEntityClass(getEntityObject(event)));
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        modificationOn(inferEntityClass(getEntityObject(event)));
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

    private void modificationOn(Class<?> entityClass) {
        LOG.debug("modification on " + entityClass);
        BlockingQueue<NotBlockingAutoUpdatedSnapshot<?>> queue = interested
                .get(entityClass);
        if (queue == null) {
            LOG.debug("nobody interested on modification on: " + entityClass);
            return;
        }
        LOG.debug("notifying modification on: " + entityClass + " to " + queue);
        for (NotBlockingAutoUpdatedSnapshot<?> each : queue) {
            each.reloadNeeded(executor);
        }
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
