package com.air.common.service.annotation;

import com.air.common.service.Operator;

/**
 * @author yh
 *
 */
public @interface RestrictionMark {

    /** 是否禁用该查询属性 */
    boolean disabled() default false;
    /**
     * 限制符号
     *
     * @see com.thinkgem.jeesite.common.service.Operator
     */
    Operator operator() default Operator.EQ;
}
