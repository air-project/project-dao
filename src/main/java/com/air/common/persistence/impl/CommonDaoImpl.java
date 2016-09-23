package com.air.common.persistence.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.internal.CriteriaImpl;
//import org.hibernate.impl.CriteriaImpl;
import org.springframework.core.Ordered;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.air.common.entity.BaseEntity;
import com.air.common.persistence.CommonDao;
import com.air.common.persistence.Page;


/**
 * @author yh
 *
 */
@Repository
public class CommonDaoImpl extends HibernateDaoSupport implements CommonDao {

	
	/**
	 * 为了注入 hibernateTemplate
	 * @param sessionFactory
	 */
	@Resource
	protected void setCommonSessionFactory(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}

	/** 保存 */
	@Override
	public <T> void save(T t) {
		if (t instanceof BaseEntity) {
			BaseEntity entity = (BaseEntity) t;
			if (entity.getCreateTime() == null) {
				entity.setCreateTime(new Date());
			}
		}
		getHibernateTemplate().save(t);
	}

	/** 添加集合 */
	@Override
	public <T> void saveAll(Collection<T> list) {
		list.forEach(t -> save(t));
	}

	/** 添加更新 */
	@Override
	public <T> void saveOrUpdate(T t) {
		getHibernateTemplate().saveOrUpdate(t);
	}

	/** 添加更新集合 */
	@Override
	public <T> void saveOrUpdateAll(Collection<T> list) {
		list.forEach(t -> saveOrUpdate(t));
	}

	/** 合并 */
	@Override
	public <T> void marge(T t) {
		getHibernateTemplate().merge(t);
	}

	/** 删除 */
	@Override
	public <T> void delete(T t) {
		getHibernateTemplate().delete(t);
	}

	/** 删除列表中所有对象 */
	@Override
	public <T> void deleteAll(Collection<T> collection) {
		getHibernateTemplate().deleteAll(collection);
	}

	/** 更新 */
	@Override
	public <T> void update(T t) {
		if (t instanceof BaseEntity) {
			BaseEntity entity = (BaseEntity) t;
			entity.setLastUpdateTime(new Date());
		}
		getHibernateTemplate().update(t);
	}

	/** 更新集合 */
	@Override
	public <T> void updateAll(Collection<T> list) {
		list.forEach(t -> update(t));
	}

	/** hql更新数据 */
	@Override
	public void update(final String hql, final Map<String, Object> params) {
		getHibernateTemplate().execute(new HibernateCallback<Void>() {
			@Override
			public Void doInHibernate(Session session) throws HibernateException {
				Query query = session.createQuery(hql);
				query.setProperties(params);
				query.executeUpdate();
				return null;
			}
		});
	}

	/** sql更新数据 */
	@Override
	public void updateSql(final String sql, final Map<String, Object> params) {
		getHibernateTemplate().execute(new HibernateCallback<Void>() {
			@Override
			public Void doInHibernate(Session session) throws HibernateException {
				Query query = session.createQuery(sql);
				query.setProperties(params);
				query.executeUpdate();
				return null;
			}
		});
	}

	/** 取对象 */
	@Override
	public <T> T get(Class<T> clazz, Serializable id) {
		return getHibernateTemplate().get(clazz, id);
	}

	/** 列出所有对象 */
	@Override
	public <T> List<T> listAll(T clazz) {
		return getHibernateTemplate().findByExample(clazz);
	}

	/** 列出所有对象 */
	@Override
	public <T> List<?> listAll(T clazz, String orderBy) {
		StringBuilder hql = new StringBuilder();
		hql.append("from ");
		hql.append(clazz.getClass().getSimpleName());
		if (orderBy != null && !"".equals(orderBy)) {
			hql.append(" order by ");
			hql.append(orderBy);
		}
		return getHibernateTemplate().find(hql.toString());
	}

	
	
	/**
	 * 根据条件, 分页, 分页参数及排序选择对象
	 * 如果分页组件参与查询时,如果当前分页中的总记录数为0或被要求强制重新加载总记录时,均会从数据库中查询记录总数
	 *
	 * @param detachedCriteria
	 *            条件
	 * @param page
	 *            分页组件
	 * @param orders
	 *            排序条件
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> List<T> listByCriteria(final DetachedCriteria detachedCriteria, final Page page, final Order... orders) {
		if (page == null) {
			if (orders != null) {
				for (Order order : orders) {
					detachedCriteria.addOrder(order);
				}
			}
			return (List<T>) getHibernateTemplate().findByCriteria(detachedCriteria);
		}
		//http://stackoverflow.com/questions/38936931/classcastexception-proxy36-cannot-be-cast-to-sessionimplementor-after-hibernate
		return (List<T>) getHibernateTemplate().executeWithNativeSession(new HibernateCallback() {

			@Override
			public Object doInHibernate(Session session) throws HibernateException {
				Criteria criteria = detachedCriteria.getExecutableCriteria(session);
				if (page.getTotalCount() <= 0) {
					CriteriaImpl criteriaImpl = (CriteriaImpl) criteria;
					Projection projection = criteriaImpl.getProjection();
					Number counter = ((Number) criteriaImpl.setProjection(Projections.rowCount()).uniqueResult());
					if (counter != null) {
						page.setTotalCount(counter.intValue());
					}
					criteriaImpl.setProjection(projection);
					if (projection == null) {
						criteriaImpl.setResultTransformer(CriteriaSpecification.ROOT_ENTITY);
					}
				}
				if (StringUtils.isNotEmpty(page.getOrderBy())) {
					if ("desc".equals(page.getOrderType())) {
						criteria.addOrder(Order.desc(page.getOrderBy()));
					} else {
						criteria.addOrder(Order.asc(page.getOrderBy()));
					}
				}
				if (orders != null) {
					for (Order order : orders) {
						criteria.addOrder(order);
					}
				}
				return criteria.setFirstResult(page.getStartIndex()).setMaxResults(page.getPageSize()).list();
			}
		});
	}

	/** 根据条件, 排序选择对象 */
	@Override
	public <T> List<T> listByCriteria(DetachedCriteria criteria, Order... orders) {
		return listByCriteria(criteria, null, orders);
	}

	/** 使某个对象从session会话中被移除, 从而成为detach状态 */
	@Override
	public <T> void evict(T t) {
		getHibernateTemplate().evict(t);
	}

	/** 使一个集合从session会话中被移除, 从而成为detach状态 */
	@Override
	public <T> void evict(Collection<? extends T> oList) {
		for (T t : oList) {
			getHibernateTemplate().evict(t);
		}
	}

	/** hql查询对象 */
	@Override
	public <T> List<?> find(String hql, Object... params) {
		return getHibernateTemplate().find(hql, params);
	}

	/**
	 * @param hql
	 *            查询语句
	 * @param params
	 *            参数
	 * @return hql查询并返回List<Map<String, Object>>
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> find2Map(final String hql, final Object... params) {
		return getHibernateTemplate().execute(new HibernateCallback<List<Map<String, Object>>>() {
			@Override
			public List<Map<String, Object>> doInHibernate(Session session) throws HibernateException {
				Query query = session.createQuery(hql);
				if (params != null && params.length > 0) {
					for (int i = 0; i < params.length; i++) {
						query.setParameter(i, params[i]);
					}
				}
				query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
				return query.list();
			}
		});
	}

	/**
	 * @param hql
	 *            查询语句
	 * @param page
	 *            分页控件
	 * @param params
	 *            查询语句参数
	 * @return 使用hql分页查询数据
	 */
	@Override
	public <T> List<T> find(final String hql, final Page page, final Object... params) {
		return getHibernateTemplate().execute(new HibernateCallback<List<T>>() {
			@Override
			@SuppressWarnings("unchecked")
			public List<T> doInHibernate(Session session) throws HibernateException {
				Query queryResult = session.createQuery(hql);
				// 如果带有参数,则设置参数
				if (params != null && params.length > 0) {
					// 需要分页控件
					if (page != null) {
						String countHql = "select count(*) ";
						if (!hql.toLowerCase().startsWith("from")) {
							countHql += hql.substring(hql.toLowerCase().indexOf("from"));
						} else {
							countHql += hql;
						}
						Query queryCount = session.createQuery(countHql);
						for (int i = 0; i < params.length; i++) {
							queryResult.setParameter(i, params[i]);
							queryCount.setParameter(i, params[i]);
						}
						page.setTotalCount(((Number) queryCount.list().get(0)).intValue());
						queryResult.setMaxResults(page.getPageSize());
						queryResult.setFirstResult(page.getStartIndex());
					} else {
						// 不需要分页控件
						for (int i = 0; i < params.length; i++) {
							queryResult.setParameter(i, params[i]);
						}
					}
				}
				return queryResult.list();
			}
		});
	}

	/**
	 * 本地sql查询
	 *
	 * @return map封装的列表
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> sqlQuery(final String sql, final Object... params) {
		return getHibernateTemplate().execute(new HibernateCallback<List<Map<String, Object>>>() {
			@Override
			public List<Map<String, Object>> doInHibernate(Session session) throws HibernateException {
				SQLQuery query = session.createSQLQuery(sql);
				if (params != null && params.length > 0) {
					for (int i = 0; i < params.length; i++) {
						query.setParameter(i, params[i]);
					}
				}
				query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
				return query.list();
			}
		});
	}

	/**
	 * 本地sql查询
	 *
	 * @return 实体bean列表
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> List<T> sqlQuery(final Class<T> clazz, final String sql, final Object... params) {
		return getHibernateTemplate().execute(new HibernateCallback<List<T>>() {
			@Override
			public List<T> doInHibernate(Session session) throws HibernateException {
				SQLQuery query = session.createSQLQuery(sql);
				if (params != null && params.length > 0) {
					for (int i = 0; i < params.length; i++) {
						query.setParameter(i, params[i]);
					}
				}
				query.addEntity(clazz);
				return query.list();
			}
		});
	}

	/**
	 * 本地sql查询
	 *
	 * @param clazz
	 *            实体bean的类型
	 * @param sql
	 *            sql查询语句
	 * @param page
	 *            分页组件
	 * @param params
	 *            参数列表
	 * @return 实体bean列表
	 */
	@Override
	public <T> List<T> sqlQuery(final Class<T> clazz, final String sql, final Page page, final Object... params) {
		return getHibernateTemplate().execute(new HibernateCallback<List<T>>() {
			@Override
			@SuppressWarnings("unchecked")
			public List<T> doInHibernate(Session session) throws HibernateException {
				SQLQuery queryResult = session.createSQLQuery(sql);
				// 如果带有参数,则设置参数
				if (params != null && params.length > 0) {
					// 需要分页控件
					if (page != null) {
						String countSql = "select count(*) ";
						if (!sql.toLowerCase().startsWith("from")) {
							countSql += sql.substring(sql.toLowerCase().indexOf("from"));
						} else {
							countSql += sql;
						}
						SQLQuery queryCount = session.createSQLQuery(countSql);
						for (int i = 0; i < params.length; i++) {
							queryResult.setParameter(i, params[i]);
							queryCount.setParameter(i, params[i]);
						}
						page.setTotalCount(((Number) queryCount.list().get(0)).intValue());
						queryResult.setMaxResults(page.getPageSize());
						queryResult.setFirstResult(page.getStartIndex());
					} else {
						// 不需要分页控件
						for (int i = 0; i < params.length; i++) {
							queryResult.setParameter(i, params[i]);
						}
					}
				}
				queryResult.addEntity(clazz);
				return queryResult.list();
			}
		});
	}

	/**
	 * 查询数量
	 *
	 * @param hql
	 *            hql查询语句
	 * @param params
	 *            参数列表
	 * @return Number
	 */
	@Override
	public Number numberHql(final String hql, final Object... params) {
		return getHibernateTemplate().execute(new HibernateCallback<Number>() {
			@Override
			public Number doInHibernate(Session session) throws HibernateException {
				Query queryCount = session.createQuery(hql);
				if (params != null) {
					for (int i = 0; i < params.length; i++) {
						queryCount.setParameter(i, params[i]);
					}
				}
				return ((Number) queryCount.uniqueResult());
			}
		});
	}

	/**
	 * 查询数量
	 *
	 * @param sql
	 *            sql查询语句
	 * @param params
	 *            参数列表
	 * @return Number
	 */
	@Override
	public Number numberSql(final String sql, final Object... params) {
		return getHibernateTemplate().execute(new HibernateCallback<Number>() {
			@Override
			public Number doInHibernate(Session session) throws HibernateException {
				Query queryCount = session.createSQLQuery(sql);
				if (params != null) {
					for (int i = 0; i < params.length; i++) {
						queryCount.setParameter(i, params[i]);
					}
				}
				return ((Number) queryCount.uniqueResult());
			}
		});
	}

	/**
	 * 查询数量
	 *
	 * @param hql
	 *            hql查询语句
	 * @param params
	 *            参数列表
	 * @return Long
	 */
	@Override
	public long countHql(final String hql, final Object... params) {
		return numberHql(hql, params).longValue();
	}

	/**
	 * 查询数量
	 *
	 * @param sql
	 *            sql查询语句
	 * @param params
	 *            参数列表
	 * @return Long
	 */
	@Override
	public long countSql(final String sql, final Object... params) {
		return numberSql(sql, params).longValue();
	}

	/** Force this session to flush */
	@Override
	public void flush() {
		getHibernateTemplate().flush();
	}

	/** Completely clear the session */
	@Override
	public void clear() {
		getHibernateTemplate().clear();
	}

	/**
	 * 打开一个新的数据库连接
	 *
	 * @return Session
	 */
	@Override
	public Session openSession() {
		return getSessionFactory().openSession();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T findUniqueByProperty(Class<T> entityClass, String propertyName, Object value) {
		Assert.hasText(propertyName);
		return (T) createCriteria(entityClass, Restrictions.eq(propertyName, value)).uniqueResult();
	}

	/**
	 * 创建Criteria对象带属性比较
	 * 
	 * @param <T>
	 * @param entityClass
	 * @param criterions
	 * @return
	 */
	private <T> Criteria createCriteria(Class<T> entityClass, Criterion... criterions) {
		Criteria criteria = currentSession().createCriteria(entityClass);
		for (Criterion c : criterions) {
			criteria.add(c);
		}
		return criteria;
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

}
