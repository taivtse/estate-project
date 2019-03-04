package com.laptrinhjavaweb.dao.impl;

import com.laptrinhjavaweb.dao.GenericDao;
import com.laptrinhjavaweb.orm.criteria.Criteria;
import com.laptrinhjavaweb.orm.criteria.criterion.Criterion;
import com.laptrinhjavaweb.orm.session.Session;
import com.laptrinhjavaweb.orm.session.SessionFactory;
import com.laptrinhjavaweb.paging.Pageable;

import javax.inject.Inject;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class AbstractDao<T, ID> implements GenericDao<T, ID> {
    @Inject
    private SessionFactory sessionFactory;
    private Class<T> entityClass;

    public AbstractDao() {
        // set persistenceClass = T
        Type type = getClass().getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) type;
        entityClass = (Class) parameterizedType.getActualTypeArguments()[0];
    }

    protected Session getSession() {
        return this.sessionFactory.openSession();
    }

    @Override
    public List<T> findAll() {
        List<T> entityList;
        Session session = this.getSession();
        entityList = session.createCriteria(this.entityClass).list();
        session.close();
        return entityList;
    }

    @Override
    public List<T> findByProperties(Pageable pageable, List<Criterion> criterionList) {
        Session session = this.getSession();
        Criteria criteria = session.createCriteria(this.entityClass);

        if (pageable != null) {
//            set start position offset
            if (pageable.getOffset() != null && pageable.getOffset() >= 0) {
                criteria.setFirstResult(pageable.getOffset());
            }

//            set limit row
            if (pageable.getLimit() != null && pageable.getLimit() >= 0) {
                criteria.setMaxResults(pageable.getLimit());
            }

//            set sorter
            if (pageable.getSorter() != null
                    && !pageable.getSorter().getPropertyName().isEmpty()
                    && !pageable.getSorter().getDirection().isEmpty()) {
                criteria.addOrder(pageable.getSorter().getOrder());
            }
        }

//        set properties search
        if (criterionList != null) {
            criterionList.forEach(criterion -> criteria.add(criterion));
        }

        session.close();
        return criteria.list();
    }

    @Override
    public T findOneById(ID id) {
        Session session = this.getSession();
        T entity = session.get(this.entityClass, id);
        session.close();
        return entity;
    }

}
