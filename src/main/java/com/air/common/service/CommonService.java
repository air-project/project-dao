package com.air.common.service;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.hibernate.criterion.Order;
import org.springframework.core.Ordered;

import com.air.common.persistence.Page;

/**
 * @author yh
 *
 */
public interface CommonService  extends Ordered {
    /** 取对象 */
    <T> T get(Class<T> clazz, Serializable id);

    /** 保存 */
    <T> void save(T t);
    
    /** 保存 */
    <T> void saveAll(Collection<T> list);

    /** 更新 */
    <T> void update(T t);
    
    /** 更新 */
    <T> void updateAll(Collection<T> list);

    /** 删除 */
    <T> void delete(T t);

    /** 删除所有 */
    <T> void deleteAll(Collection<T> list);

    /** 保存或者更新 */
    <T> void saveOrUpdate(T t);

    /** 保存或者更新所有 */
    <T> void saveOrUpdate(Collection<T> list);

    /** 列出所有对象 */
    <T> List<T> listAll(T clazz);

    /**
     * 根据BaseBean中属性上的RestrictionMark注解动态生成查询条件进行相关分页查询
     *
     * @param clazz  条件和返回值的class
     * @param t      条件
     * @param page   分页组件
     * @param orders 排序
     * @param <T>    条件和返回值适配的类型限制
     * @return T的查询列表
     */
    <T> List<T> list(Class<T> clazz, T t, Page page, Order... orders);
}
