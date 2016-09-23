package com.air.common.persistence;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.springframework.core.Ordered;

/**
 * @author yh
 *
 */
public interface CommonDao extends Ordered {
	/**
	 * 保存 
	 * @param <T> This is the type parameter
	 * @param t 实体
	 */
    <T> void save(T t);

    /** 
     * 添加集合
     * @param list 实体集合
     *  */
    <T> void saveAll(Collection<T> list);

    /** 
     * 添加更新 
     * @param t 实体
     */
    <T> void saveOrUpdate(T t);

    /** 
     * 添加更新集合 
     * @param list 实体集合
     * 
     * */
    <T> void saveOrUpdateAll(Collection<T> list);

    /** 
     * 合并
     * @param t 实体
     *  */
    <T> void marge(T t);

    /** 
     * 删除
     * @param t 实体
     *  */
    <T> void delete(T t);

    /** 
     * 删除列表中所有对象
     * @param collection 实体集合
     *  */
    <T> void deleteAll(Collection<T> collection);

    /** 
     * 更新
     * @param t 实体
     *  */
    <T> void update(T t);

    /** 
     * 更新集合
     * @param list 实体集合
     *  */
    <T> void updateAll(Collection<T> list);
    

    /**
     * hql更新数据
     * @param hql HQL
     * @param params 参数
     */
    void update(String hql, Map<String, Object> params);

    /**
     * sql更新数据
     * @param sql SQL
     * @param params 参数
     */
    void updateSql(String sql, Map<String, Object> params);

    /**
     * 取对象
     * @param clazz 对象
     * @param id 主键
     * @return 对象
     */
    <T> T get(Class<T> clazz, Serializable id);
    
    /**
     * 根据属性取对象
     * @param entityClass 对象
     * @param propertyName 属性名
     * @param value 属性值
     * @return 根据属性取对象
     */
    <T> T findUniqueByProperty(Class<T> entityClass,
			String propertyName, Object value);

    /**
     * 列出所有对象
     * @param clazz 对象
     * @return 所有对象
     */
    <T> List<T> listAll(T clazz);

    /**
     * 列出所有对象可以排序
     * @param clazz 对象
     * @param orderBy 排序
     * @return 所有对象可以排序
     */
    <T> List<?> listAll(T clazz, String orderBy);

    /**
     * 根据条件分页查询
     * @param criteria 条件
     * @param page     分页组件
     * @param orders   排序条件
     *                 根据条件分页查询
     * @return 根据条件分页查询
     */
    <T> List<T> listByCriteria(DetachedCriteria criteria, Page page, Order... orders);

    
    /**
     * 根据条件, 排序选择对象
     * @param criteria 条件
     * @param orders 排序条件
     * @return 根据条件分页查询
     */
    <T> List<T> listByCriteria(DetachedCriteria criteria, Order... orders);

    /**
     * 使某个对象从session会话中被移除成为detach状态
     * @param t 使某个对象从session会话中被移除成为detach状态
     */
    <T> void evict(T t);

    /**
     * 使一个集合从session会话中被移除成为detach状态
     * @param oList 集合
     */
    <T> void evict(Collection<? extends T> oList);

    /**
     * hql查询对象
     * @param hql HQL
     * @param params 参数
     * @return hql查询对象
     */
    <T> List<?> find(String hql, Object... params);

    /**
     * hql查询并返回List<Map<String, Object>>
     * @param hql    查询语句
     * @param params 参数
     * @return hql查询并返回List<Map<String, Object>>
     */
    List<Map<String, Object>> find2Map(String hql, Object... params);

    /**
     * 使用hql分页查询数据
     * @param hql    查询语句
     * @param page   分页控件
     * @param params 查询语句参数
     * @return 实体bean列表
     * 
     */
    <T> List<T> find(final String hql, final Page page, final Object... params);

    /**
     * 本地sql查询
     * @param sql SQL
     * @param params 参数
     * @return map封装的列表
     */
    List<Map<String, Object>> sqlQuery(final String sql, final Object... params);

    /**
     * @return 实体bean列表
     * 本地sql查询
     */
    <T> List<T> sqlQuery(final Class<T> clazz, final String sql, final Object... params);

    /**
     * @param clazz  实体bean类型
     * @param sql    sql语句
     * @param page   分页对象
     * @param params 参数集合
     * @return 本地sql分页查询
     */
    <T> List<T> sqlQuery(final Class<T> clazz, final String sql, final Page page, final Object... params);

    /**
     * @param hql    hql查询语句
     * @param params 参数列表
     * @return Number
     * 查询数量
     */
    Number numberHql(final String hql, final Object... params);

    /**
     * @param sql    sql查询语句
     * @param params 参数列表
     * @return Number
     * 查询数量
     */
    Number numberSql(final String sql, final Object... params);

    /**
     * @param hql    hql查询语句
     * @param params 参数列表
     * @return long
     * 查询数量
     */
    long countHql(final String hql, final Object... params);

    /**
     * @param sql    sql查询语句
     * @param params 参数列表
     * @return long
     * 查询数量
     */
    long countSql(final String sql, final Object... params);

    /**
     * Force this session to flush
     */
    void flush();

    /**
     * Completely clear the session
     */
    void clear();

    /**
     * @return Session
     * 打开一个数据库连接
     */
    Session openSession();

}
