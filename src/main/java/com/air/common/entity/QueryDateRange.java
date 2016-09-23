package com.air.common.entity;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

import lombok.Data;

/**
 * @author yh
 *
 */
@Data
public class QueryDateRange {
	
	private String dateFiled;

	private Date start;

	private Date end;
	
	private String idFiled;
	
	private Object[] values;
	
	private Object clazz;
	
	public void dateRange(String dateFiled,Date start,Date end){
		this.dateFiled=dateFiled;
		this.start=start;
		this.end=end;
	}
	
	public void dateRange(String dateFiled,String start,String end){
		this.dateFiled=dateFiled;
		try {
			this.start=DateUtils.parseDate(start,"yyyy-MM-dd HH:mm:ss");
			this.end=DateUtils.parseDate(end,"yyyy-MM-dd HH:mm:ss");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void idsRange(String idFiled,Object type,String ids){
		if(ids!=null){
			idsRange(idFiled,type,ids.split(","));
		}
	}
	public void idsRange(String idFiled,Object type,String[] ids){
		this.idFiled=idFiled;
		this.clazz=type;
		if(ids!=null&&ids.length>0){
			if(clazz!=null && clazz instanceof java.lang.String){
				values=ids;
			}else if(clazz!=null &&  clazz instanceof java.lang.Long){
				values=new Long[ids.length];
				for(int i=0;i<ids.length;i++){
					values[i]=Long.parseLong(ids[i]);
				}
			}else if(clazz!=null && clazz instanceof java.lang.Integer){
				values=new Integer[ids.length];
				for(int i=0;i<ids.length;i++){
					values[i]=Integer.parseInt(ids[i]);
				}
			}
		}
	}

	
	
	
	@SuppressWarnings("unused")
	private void setDateFiled(String dateFiled) {
	}

	@SuppressWarnings("unused")
	private void setStart(Date start) {
	}

	@SuppressWarnings("unused")
	private void setEnd(Date end) {
	}

	@SuppressWarnings("unused")
	private void setValues(Object[] values) {
	}
	@SuppressWarnings("unused")
	private void setClazz(Object clazz) {
	}
	@SuppressWarnings("unused")
	private void setIdFiled(String idsFiled) {
	}
	
}
