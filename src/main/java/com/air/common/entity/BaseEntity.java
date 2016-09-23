package com.air.common.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import lombok.Data;

/**
 * @author yh
 *
 */
@MappedSuperclass
@Data
public class BaseEntity implements Serializable {

	private static final long serialVersionUID = -1479028874914453979L;

	protected static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/** 主键 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	
	@Column(name="create_user_id")
	private long createUserId; // 创建者
	
	/** 创建人 */
	@Column(name="create_user")
	private String createUser;

	/** 创建时间 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="create_date")
	private Date createTime;


	@Column(name="last_update_user_id")
	private long lastUpdateUserId; // 更新者
	
	/** 更新人 */
	@Column(name="last_update_user")
	private String lastUpdateUser;

	/** 更新时间 */
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="last_update_date")
	private Date lastUpdateTime;
	
	@Transient
	private QueryDateRange dateRange;
 
	public String getCreateTimeInfo() {
		return createTime != null ? sdf.format(createTime) : "";
	}

	public String getLastUpdateTimeInfo() {
		return lastUpdateTime != null ? sdf.format(lastUpdateTime) : "";
	}

	@Override
	public String toString() {
		return ReflectionToStringBuilder.reflectionToString(this);
	}

	
}
