package com.air.common.service.impl;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import com.air.common.entity.QueryDateRange;
import com.air.common.persistence.CommonDao;
import com.air.common.persistence.Page;
import com.air.common.service.CommonService;
import com.air.common.service.Operator;
import com.air.common.service.annotation.RestrictionMark;

/**
 * @author yh
 *
 */
@Service
public class CommonServiceImpl implements CommonService {

	@Resource
	protected CommonDao commonDao;

	@Override
	public <T> T get(Class<T> clazz, Serializable id) {
		return commonDao.get(clazz, id);
	}

	@Override
	public <T> void save(T t) {
		commonDao.save(t);
	}

	@Override
	public <T> void update(T t) {
		commonDao.update(t);
	}

	@Override
	public <T> void delete(T t) {
		commonDao.delete(t);
	}

	@Override
	public <T> void deleteAll(Collection<T> beans) {
		commonDao.deleteAll(beans);
	}

	@Override
	public <T> void saveOrUpdate(T t) {
		commonDao.saveOrUpdate(t);
	}

	@Override
	public <T> void saveOrUpdate(Collection<T> beans) {
		commonDao.saveOrUpdate(beans);
	}

	/** 列出所有对象 */
	@Override
	public <T> List<T> listAll(T clazz) {
		return commonDao.listAll(clazz);
	}

	/**
	 * 根据BaseBean中属性上的RestrictionMark注解动态生成查询条件进行相关分页查询
	 * 
	 * @param clazz
	 *            条件和返回值的class
	 * @param t
	 *            条件
	 * @param page
	 *            分页组件
	 * @param orders
	 *            排序
	 * @param <T>
	 *            条件和返回值适配的类型限制
	 * @return T的查询列表
	 */
	@Override
	public <T> List<T> list(Class<T> clazz, T t, Page page, Order... orders) {
		DetachedCriteria criteria = DetachedCriteria.forClass(clazz);
		if (t != null) {
			Field[] fields = clazz.getDeclaredFields();
			try {
				for (Field field : fields) {
					// 屏蔽掉static final类型属性
					if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
						continue;
					}

					Class<?> type = field.getType();
					if (type.isPrimitive() || type.isAssignableFrom(String.class)) {
						field.setAccessible(true);
						RestrictionMark restrictionMark = field.getAnnotation(RestrictionMark.class);

						// 如果操作符是is null或者is not null
						if (restrictionMark != null && !restrictionMark.disabled()
								&& (restrictionMark.operator() == Operator.IS_NULL
										|| restrictionMark.operator() == Operator.IS_NOT_NULL)) {
							switch (restrictionMark.operator()) {
							case IS_NOT_NULL: {
								criteria.add(Restrictions.isNotNull(field.getName()));
								break;
							}
							case IS_NULL: {
								criteria.add(Restrictions.isNull(field.getName()));
								break;
							}
							default:
								break;
							}
						}

						Object value = field.get(t);
						if (value != null) {
							if (restrictionMark != null && restrictionMark.disabled()) {
								continue;
							}

							// 如果是数字并且==0.0则直接屏蔽掉
							if (value instanceof Number && ((Number) value).doubleValue() == 0.0D) {
								continue;
							}

							setParameter(criteria, restrictionMark != null ? restrictionMark.operator() : null,
									field.getName(), value);
						}
					}
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			

			Field[] pfields = clazz.getSuperclass().getDeclaredFields();
			try {
				for (Field field : pfields) {
					// 屏蔽掉static final类型属性
					if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
						continue;
					}
					Class<?> type = field.getType();
					if (type.isAssignableFrom(QueryDateRange.class)) {
						field.setAccessible(true);
						QueryDateRange range = (QueryDateRange)field.get(t);
						if (range != null) {
							if(null!=range.getDateFiled()){//时间范围
								if(range.getStart()!=null&& range.getEnd()==null){
									criteria.add(Restrictions.ge(range.getDateFiled(), range.getStart()));
								}else if(range.getStart()==null&& range.getEnd()!=null){
									criteria.add(Restrictions.ge(range.getDateFiled(), range.getEnd()));
								}else if(range.getStart()!=null&& range.getEnd()!=null){
									criteria.add(Restrictions.between(range.getDateFiled(), range.getStart(),range.getEnd()));
								}
							}
							if(null!=range.getIdFiled()){//ids 范围
								if(range.getValues()!=null&&range.getValues().length>0){
									criteria.add(Restrictions.in(range.getIdFiled(), range.getValues()));
								}
							}
						}
					}
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			
		}

		return commonDao.listByCriteria(criteria, page, orders);
	}

	/**
	 * 设置参数
	 *
	 * @param criteria
	 *            离线查询
	 * @param operator
	 *            操作符
	 * @param fieldName
	 *            属性名称
	 * @param value
	 *            值
	 */
	private void setParameter(DetachedCriteria criteria, Operator operator, String fieldName, Object value) {
		if (operator == null) {
			criteria.add(Restrictions.eq(fieldName, value));
			return;
		}

		switch (operator) {
		case EQ: {
			criteria.add(Restrictions.eq(fieldName, value));
			return;
		}
		case GE: {
			criteria.add(Restrictions.ge(fieldName, value));
			return;
		}
		case GT: {
			criteria.add(Restrictions.gt(fieldName, value));
			return;
		}
		case LE: {
			criteria.add(Restrictions.le(fieldName, value));
			return;
		}
		case LIKE: {
			if (value instanceof String) {
				criteria.add(Restrictions.like(fieldName, value.toString(), MatchMode.ANYWHERE));
			}
			return;
		}
		case LT: {
			criteria.add(Restrictions.lt(fieldName, value));
			return;
		}
		case NE: {
			criteria.add(Restrictions.ne(fieldName, value));
		}
		default:
			break;
		}
	}

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public <T> void saveAll(Collection<T> list) {
		commonDao.saveAll(list);
		
	}

	@Override
	public <T> void updateAll(Collection<T> list) {
		commonDao.updateAll(list);
		
	}
}
