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

package org.navalplanner.business.common.daos;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.Hibernate;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StaleObjectStateException;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.BaseEntity;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.navalplanner.business.common.exceptions.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * An implementation of <code>IGenericDao</code> based on Hibernate's native
 * API. Concrete DAOs must extend directly from this class. This constraint is
 * imposed by the constructor of this class that must infer the type of the
 * entity from the declaration of the concrete DAO.
 * <p/>
 * This class autowires a <code>SessionFactory</code> bean and allows to
 * implement DAOs with Hibernate's native API. Subclasses access Hibernate's
 * <code>Session</code> by calling on <code>getSession()</code> method.
 * Operations must be implemented by catching <code>HibernateException</code>
 * and rethrowing it by using <code>convertHibernateAccessException()</code>
 * method. See source code of this class for an example.
 * @author Fernando Bellas Permuy <fbellas@udc.es>
 * @param <E>
 *            Entity class
 * @param <PK>
 *            Primary key class
 */
public class GenericDAOHibernate<E extends BaseEntity,
        PK extends Serializable> implements IGenericDAO<E, PK> {

    private Class<E> entityClass;

    @Autowired
    private SessionFactory sessionFactory;

    @SuppressWarnings("unchecked")
    public GenericDAOHibernate() {
        this.entityClass = (Class<E>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public GenericDAOHibernate(Class<E> entityClass) {
        Validate.notNull(entityClass);
        this.entityClass = entityClass;
    }

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    public Class<E> getEntityClass() {
        return entityClass;
    }

    public void save(E entity) throws ValidationException {
        save(entity, Mode.AUTOMATIC_FLUSH);
    }

    /**
     * It's necessary to save and validate later.
     *
     * Validate may retrieve the entity from DB and put it into the Session, which can eventually lead to
     * a NonUniqueObject exception. Save works here to reattach the object as well as saving.
     */
    public void save(E entity, Mode mode) throws ValidationException {
        getSession().saveOrUpdate(entity);
        if (mode == Mode.FLUSH_BEFORE_VALIDATION) {
            getSession().flush();
        }
        entity.validate();
    }

   public void saveWithoutValidating(E entity) {
       getSession().saveOrUpdate(entity);
   }

    public void reattachUnmodifiedEntity(E entity) {
        if (Hibernate.isInitialized(entity) && entity.isNewObject()) {
            return;
        }
        getSession().lock(entity, LockMode.NONE);

    }

    public E merge(E entity) {

        return entityClass.cast(getSession().merge(entity));

    }

    public void checkVersion(E entity) {

        /* Get id and version from entity. */
        Serializable id;
        Long versionValueInMemory;

        try {

            Method getIdMethod = entityClass.getMethod("getId");
            id = (Serializable) getIdMethod.invoke(entity);

            if (id == null) {
                return;
            }

            Method getVersionMethod = entityClass.getMethod("getVersion");
            versionValueInMemory = (Long) getVersionMethod.invoke(entity);

            if (versionValueInMemory == null) {
                return;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        /* Check version. */
        Long versionValueInDB = (Long) getSession().createCriteria(entityClass)
                .add(Restrictions.idEq(id)).setProjection(
                        Projections.property("version")).uniqueResult();

        if (versionValueInDB == null) {
            return;
        }

        if (!versionValueInMemory.equals(versionValueInDB)) {
            throw new StaleObjectStateException(entityClass.getName(), id);
        }

    }

    public void lock(E entity) {

        getSession().lock(entity, LockMode.UPGRADE);

    }

    public void associateToSession(E entity) {
        getSession().lock(entity, LockMode.NONE);
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public E find(PK id) throws InstanceNotFoundException {

        E entity = (E) getSession().get(entityClass, id);

        if (entity == null) {
            throw new InstanceNotFoundException(id, entityClass.getName());
        }

        return entity;
    }

    public E findExistingEntity(PK id) {

        try {
            return find(id);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean exists(final PK id) {

        return getSession().createCriteria(entityClass).add(
                Restrictions.idEq(id)).setProjection(Projections.id())
                .uniqueResult() != null;

    }

    public void remove(PK id) throws InstanceNotFoundException {
        getSession().delete(find(id));
    }

    @SuppressWarnings("unchecked")
    @Override
    @Transactional(readOnly = true)
    public <T extends E> List<T> list(Class<T> klass) {
        return getSession().createCriteria(klass).list();
    }

    @Override
    public void flush() {
        getSession().flush();
    }

    @Override
    public void reattach(E entity) {
        getSession().saveOrUpdate(entity);
    }

}
