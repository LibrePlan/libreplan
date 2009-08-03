package org.navalplanner.business.common.daos;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StaleObjectStateException;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.navalplanner.business.common.exceptions.InstanceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

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
public class GenericDAOHibernate<E, PK extends Serializable> implements
        IGenericDAO<E, PK> {

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

    protected DataAccessException convertHibernateAccessException(
            HibernateException e) {

        return SessionFactoryUtils.convertHibernateAccessException(e);

    }

    public void save(E entity) {

        try {
            getSession().saveOrUpdate(entity);
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        }

    }

    public void reattachUnmodifiedEntity(E entity) {

        try {
            getSession().lock(entity, LockMode.NONE);
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        }

    }

    public E merge(E entity) {

        try {
            return entityClass.cast(getSession().merge(entity));
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        }

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
        try {

            Long versionValueInDB = (Long)
                getSession().createCriteria(entityClass).
                add(Restrictions.idEq(id)).
                setProjection(Projections.property("version")).
                uniqueResult();

            if (versionValueInDB == null) {
                return;
            }

            if (!versionValueInMemory.equals(versionValueInDB)) {
                throw new StaleObjectStateException(entityClass.getName(), id);
            }

        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        }

    }

    public void lock(E entity) {

        try {
            getSession().lock(entity, LockMode.UPGRADE);
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        }

    }

    @SuppressWarnings("unchecked")
    public E find(PK id) throws InstanceNotFoundException {

        try {

            E entity = (E) getSession().get(entityClass, id);

            if (entity == null) {
                throw new InstanceNotFoundException(id, entityClass.getName());
            }

            return entity;

        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        }

    }

    public E findExistingEntity(PK id) {

        try {
            return find(id);
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean exists(final PK id) {

        try {

            return getSession().createCriteria(entityClass).
                add(Restrictions.idEq(id)).
                setProjection(Projections.id()).
                uniqueResult() != null;

        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        }

    }

    public void remove(PK id) throws InstanceNotFoundException {

        try {
            getSession().delete(find(id));
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends E> List<T> list(Class<T> klass) {

        try {
            return getSession().createCriteria(klass).list();
        } catch (HibernateException e) {
            throw convertHibernateAccessException(e);
        }

    }

}
