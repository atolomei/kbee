package com.novamens.kbee.logging.usage;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Domain;
import com.novamens.logging.usage.UsageStat;
import com.novamens.service.ServiceLocator;


/***
 * 
 *
 */
public class KbeeUsageStatService implements UsageStatService {
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeUsageStatService.class.getName());


	private SessionFactory sessionFactory; 	// must be assigned by Spring

	
	public KbeeUsageStatService() {
	}


	public void setDataSource(DataSource dataSource) {
	}

	
	public void nonTrxSave(UsageStat stat) {
		sessionFactory.getCurrentSession().save(stat);
	}

	
	@Override
	@Transactional
	public void save(UsageStat stat) {
		sessionFactory.getCurrentSession().save(stat);
	}


	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	
	/**
	 * Domain kbee contains totals
	 */
	@Override
	public List<UsageStat> getGlobalUsageStat() {
		Domain kb = getContentDao().findDomainByName("kbee");
		if (kb==null)
			return new ArrayList<UsageStat>();
		
		String hql = "FROM KbeeUsageStat K where K.domain_id = " + 	kb.getId().toString() + " order by ts desc";
		Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setCacheable(true);
		query.setCacheRegion("metrics");
		@SuppressWarnings("unchecked")
		List<UsageStat> results = (List<UsageStat>)query.list();
		return results;
	}
	
	
	//
	// In the table domain_id is bigint 
	//
	@Override
	public List<UsageStat> getUsageStat(Serializable domain_id) {
		String hql = "FROM KbeeUsageStat K where K.domain_id = " + domain_id.toString() + " order by ts desc";
		Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setCacheable(true);
		query.setCacheRegion("metrics");
		@SuppressWarnings("unchecked")
		List<UsageStat> results = (List<UsageStat>)query.list();
		return results;
	}


	/**
	  In the table domain_id is bigint 
	*/
	@Override
	public List<UsageStat> getUsageStat(Serializable domain_id, int max_elements) {
		String hql = "FROM KbeeUsageStat K where K.domain_id = " + domain_id.toString() + " order by ts desc";
		Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setCacheable(true);
		query.setMaxResults(max_elements);
		query.setCacheRegion("metrics");
		@SuppressWarnings("unchecked")
		List<UsageStat> results = (List<UsageStat>)query.list();
		return results;
	}

	/**
	 * 
	 * -- Mean processing time last hour
		select sum(event_processing_time)/count(*) from api_logevent 
		where event_time >
		and   event_time >
		
		select date_trunc('hour', event_time) D, count(*)                            from api_logevent where event_time >= start and event_time < end group by D order by D
		select date_trunc('hour', event_time) D, sum(event_processing_time)/count(*) from api_logevent where event_time >= start and event_time < end group by D order by D
		
		select date_trunc('hour', event_time) D, sum(event_processing_time)/count(*) from api_logevent where event_time >(now() - INTERVAL '6 month')::timestamp group by D order by D
		

		TOTAL 
		select date_trunc('hour', event_time) D,  count(*) from api_logevent where event_time >(now() - INTERVAL '7 day')::timestamp group by D order by D

		TOTAL OK 
		select date_trunc('hour', event_time) D,  count(*) from api_logevent where event_status=200 and event_time >(now() - INTERVAL '7 day')::timestamp group by D order by D

		TOTAL ERROR 
		select date_trunc('hour', event_time) D,  count(*) from api_logevent where event_status!=200 and event_time >(now() - INTERVAL '7 day')::timestamp group by D order by D

		TOTAL BOUNCED 
		select date_trunc('hour', event_time) D,  count(*) from api_logevent where event_status=429 and event_time >(now() - INTERVAL '7 day')::timestamp group by D order by D

		TOTAL DELETE 
		select date_trunc('hour', event_time) D,  count(*) from api_logevent where event_method like 'DELE%' and event_time >(now() - INTERVAL '7 day')::timestamp group by D order by D
		
		TOTAL POST 
		select date_trunc('hour', event_time) D,  count(*) from api_logevent where event_method like 'POST%' and event_time >(now() - INTERVAL '7 day')::timestamp group by D order by D
		
		MEAN PROCESSING TIME POST
		select date_trunc('hour', event_time) D,  sum(event_processing_time)/count(*) from api_logevent where event_method like 'POST%' and event_time >(now() - INTERVAL '7 day')::timestamp group by D order by D
		
		MEAN PROCESSING TIME DELETE
		select date_trunc('hour', event_time) D,  sum(event_processing_time)/count(*) from api_logevent where event_method like 'DELETE%' and event_time >(now() - INTERVAL '7 day')::timestamp group by D order by D

		MEAN PROCESSING TIME ALL
		select date_trunc('hour', event_time) D,  sum(event_processing_time)/count(*) from api_logevent where event_time >(now() - INTERVAL '7 day')::timestamp group by D order by D
        
		----------

		from] - [to]
		
		------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		Date Hour |  Total | total mean req processing time (msecs) | #Post | #Delete | POST mean processing time (msecs) |  DELETE mean processing time (msecs) | total bounced (Error 429) 
		------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
		12           33223   120                                       33       44              333                                3322                             211 (1%)
		
		. Roles
		. Email Notifications
		. Alta de binary ??
		-----------------------------
		 Advanced Search Contents
		-----------------------------
		
	 * 
	 */
	
	
	public class APIUsageStat {
		
		public java.sql.Timestamp ts;
		public int total;
		public double mean_time_total;
		
		public int total_post;
		public double mean_time_post;
		
		public int total_delete;
		public double mean_time_delete;
		
		public int total_bounced;
		
		public String toString() {
			StringBuilder str = new StringBuilder();
			
			if (ts!=null)
				str.append("Ts. "+ts.toString());
			
			if (str.length()>0) str.append(" | ");
			str.append("Total Requests. "+String.format("%6d",total));
							
			if (str.length()>0) str.append(" | ");
			str.append("Total Post. "+String.format("%6d", total_post));

			if (str.length()>0) str.append(" | ");
			str.append("Total Delete. "+String.format("%6d",total_delete));

			if (str.length()>0) str.append(" | ");
			str.append("Total Bounced. "+String.format("%6d",total_bounced));

			if (str.length()>0) str.append(" | ");
			str.append("Mean Time (ms). "+String.format("%8.2f", this.mean_time_total));
			
			if (str.length()>0) str.append(" | ");
			str.append("Mean Time POST (ms). "+String.format("%8.2f",this.mean_time_post));

			if (str.length()>0) str.append(" | ");
			str.append("Mean Time DELETE (ms). "+String.format("%8.2f",this.mean_time_delete));

			return str.toString();
		}
	}
	
	@Override
	@Transactional
	public void saveApiUsage(String from, String to) throws ContentMgmtException {
		nonTrxSaveApiUsage(from,to);
	}

	
	@Override
	public void nonTrxSaveApiUsage(String from, String to) throws ContentMgmtException {

		Map<java.sql.Timestamp, APIUsageStat> map = new HashMap<java.sql.Timestamp, APIUsageStat>();
							
		try {
				
			try {					
				String sql_total = "select date_trunc('hour', event_time) D,  count(*) from api_logevent where event_time >='" + from + "' and event_time <'"+ to +"' group by D order by D";
				logger.debug(sql_total);
				NativeQuery<?> query_total = sessionFactory.getCurrentSession().createNativeQuery(sql_total);
				List<Object[]> list = (List<Object[]>) query_total.list();
						for (Iterator<Object[]> iterator = list.iterator(); iterator.hasNext();) {
						    Object[] e = iterator.next();
						    java.sql.Timestamp ts = (java.sql.Timestamp)  e[0];
						    java.math.BigInteger co = (java.math.BigInteger)e[1];
						    APIUsageStat us;
						    if (map.containsKey(ts))
						    	us = map.get(ts);
						    else {
						    	us = new APIUsageStat();
						    	us.ts = ts;
						    	
						    	map.put(us.ts, us);
						    }
						    us.total=co.intValue();
						    logger.debug(us.toString());
					}
			} catch (Exception e) {
				logger.error(e.getClass().getName()+ " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + e.getMessage()!=null? (" | " +e.getMessage()):"");
				throw new ContentMgmtException(e);
			}
				
			try {
					String sql_total_post = "select date_trunc('hour', event_time) D,  count(*) from api_logevent where event_method like 'POST%' and event_time >='" + from + "' and event_time <'"+ to +"' group by D order by D";
					NativeQuery<?> query_total_post = sessionFactory.getCurrentSession().createNativeQuery(sql_total_post);
					logger.debug(sql_total_post);
					List<Object[]> list = (List<Object[]>) query_total_post.list();
							
					for (Iterator<Object[]> iterator = list.iterator(); iterator.hasNext();) {
					    Object[] e = iterator.next();
					    java.sql.Timestamp ts = (java.sql.Timestamp)  e[0];
					    java.math.BigInteger co = (java.math.BigInteger)e[1];
					    APIUsageStat us;
					    if (map.containsKey(ts))
					    	us = map.get(ts);
					    else {
					    	us = new APIUsageStat();
					    	us.ts = ts;
					    	
					    	map.put(us.ts, us);
					    }
					    us.total_post=co.intValue();
					    logger.debug(us.toString());
					}

			} catch (Exception e) {
				logger.error(e);
				throw new ContentMgmtException(e);
			}
					
				
			try {
				String sql_total_delete = "select date_trunc('hour', event_time) D,  count(*) from api_logevent where event_method like 'DELETE%' and event_time >='" + from + "' and event_time <'"+ to +"' group by D order by D";
				NativeQuery<?> query_total_delete = sessionFactory.getCurrentSession().createNativeQuery(sql_total_delete);
				logger.debug(sql_total_delete);
				List<Object[]> list = (List<Object[]>) query_total_delete.list();
						for (Iterator<Object[]> iterator = list.iterator(); iterator.hasNext();) {
						    Object[] e = iterator.next();
						    java.sql.Timestamp ts = (java.sql.Timestamp)  e[0];
						    java.math.BigInteger co = (java.math.BigInteger)e[1];
						    APIUsageStat us;
						    if (map.containsKey(ts))
						    	us = map.get(ts);
						    else {
						    	us = new APIUsageStat();
						    	us.ts = ts;
						    	
						    	map.put(us.ts, us);
						    }
						    us.total_delete=co.intValue();
						    logger.debug(us.toString());
					}
				
			} catch (Exception e) {
				logger.error(e.getClass().getName()+ " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + e.getMessage()!=null? (" | " +e.getMessage()):"");
				throw new ContentMgmtException(e);

			}
			
			try {
				String sql_total_bounced = "select date_trunc('hour', event_time) D,  count(*) from api_logevent where event_status=429 and event_time >='" + from + "' and event_time <'"+ to +"' group by D order by D";
				NativeQuery<?> query_total_bounced = sessionFactory.getCurrentSession().createNativeQuery(sql_total_bounced);
				logger.debug(sql_total_bounced );
				List<Object[]> list = (List<Object[]>) query_total_bounced .list();
						for (Iterator<Object[]> iterator = list.iterator(); iterator.hasNext();) {
						    Object[] e = iterator.next();
						    java.sql.Timestamp ts = (java.sql.Timestamp)  e[0];
						    java.math.BigInteger co = (java.math.BigInteger)e[1];
						    APIUsageStat us;
						    if (map.containsKey(ts))
						    	us = map.get(ts);
						    else {
						    	us = new APIUsageStat();
						    	us.ts = ts;
						    	
						    	map.put(us.ts, us);
						    }
						    us.total_bounced=co.intValue();
						    logger.debug(us.toString());
						}
						
			} catch (Exception e) {
				logger.error(e.getClass().getName()+ " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + e.getMessage()!=null? (" | " +e.getMessage()):"");
				throw new ContentMgmtException(e);
			}

			try {
				String sql_mean_total = "select date_trunc('hour', event_time) D,  sum(event_processing_time)/count(*) from api_logevent where event_time >='" + from + "' and event_time <'"+ to +"' group by D order by D";
				NativeQuery<?> query_mean_total = sessionFactory.getCurrentSession().createNativeQuery(sql_mean_total);
				
				logger.debug(sql_mean_total);
				
				List<Object[]> list = (List<Object[]>) query_mean_total.list();
						for (Iterator<Object[]> iterator = list.iterator(); iterator.hasNext();) {
						    Object[] e = iterator.next();
						    java.sql.Timestamp ts = (java.sql.Timestamp)  e[0];
						    java.math.BigDecimal co = (java.math.BigDecimal)e[1];
						    APIUsageStat us;
						    if (map.containsKey(ts))
						    	us = map.get(ts);
						    else {
						    	us = new APIUsageStat();
						    	us.ts = ts;
						    	
						    	map.put(us.ts, us);
						    }
						    us.mean_time_total=co.intValue();
						    logger.debug(us.toString());
						}
				
			} catch (Exception e) {
				logger.error(e.getClass().getName()+ " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + e.getMessage()!=null? (" | " +e.getMessage()):"");
				throw new ContentMgmtException(e);
			}

			
			try {
				String sql_mean_delete = "select date_trunc('hour', event_time) D,  sum(event_processing_time)/count(*) from api_logevent where event_method like 'DELETE%' and event_time >='" + from + "' and event_time <'"+ to +"' group by D order by D";
				NativeQuery<?> query_mean_delete = sessionFactory.getCurrentSession().createNativeQuery(sql_mean_delete);
				
				logger.debug(sql_mean_delete);
				List<Object[]> list = (List<Object[]>) query_mean_delete.list();
						for (Iterator<Object[]> iterator = list.iterator(); iterator.hasNext();) {
						    Object[] e = iterator.next();
						    java.sql.Timestamp ts = (java.sql.Timestamp)  e[0];
						    java.math.BigDecimal co = (java.math.BigDecimal)e[1];
						    APIUsageStat us;
						    if (map.containsKey(ts))
						    	us = map.get(ts);
						    else {
						    	us = new APIUsageStat();
						    	us.ts = ts;
						    	
						    	map.put(us.ts, us);
						    }
						    us.mean_time_delete=co.intValue();
						    logger.debug(us.toString());
						}
			} catch (Exception e) {
				logger.error(e.getClass().getName()+ " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + e.getMessage()!=null? (" | " +e.getMessage()):"");
				throw new ContentMgmtException(e);
			}
			
						
			try {
				String sql_mean_post = "select date_trunc('hour', event_time) D,  sum(event_processing_time)/count(*) from api_logevent where  event_method like 'POST%' and event_time >='" + from + "' and event_time <'"+ to +"' group by D order by D";
				NativeQuery<?> query_mean_post = sessionFactory.getCurrentSession().createNativeQuery(sql_mean_post);

				logger.debug(sql_mean_post);
				List<Object[]> list = (List<Object[]>) query_mean_post.list();
						for (Iterator<Object[]> iterator = list.iterator(); iterator.hasNext();) {
						    Object[] e = iterator.next();
						    java.sql.Timestamp ts = (java.sql.Timestamp)  e[0];
						    java.math.BigDecimal co = (java.math.BigDecimal)e[1];
						    APIUsageStat us;
						    if (map.containsKey(ts))
						    	us = map.get(ts);
						    else {
						    	us = new APIUsageStat();
						    	us.ts = ts;
						    	
						    	map.put(us.ts, us);
						    }
						    us.mean_time_post=co.intValue();
						    logger.debug(us.toString());
						}
			} catch (Exception e) {
				logger.error(e);
				throw new ContentMgmtException(e);
			}
			

			List<APIUsageStat> list = new ArrayList<APIUsageStat>();
			
			for (Entry<java.sql.Timestamp, APIUsageStat> entry: map.entrySet()) {
				list.add(entry.getValue());
			}
			
			
			list.sort(new Comparator<APIUsageStat>() {
				@Override
				public int compare(APIUsageStat o1, APIUsageStat o2) {
					try {
						return o1.ts.compareTo(o2.ts);
						
					} catch (Exception e) {
						return 0;	
					}
				}
			});
			

			if (logger.isDebugEnabled()) {
				logger.debug( "--------------------------------------");
				logger.debug( "Final list: ");
				list.forEach(item -> logger.debug(item.toString()));
			}
			
			list.forEach(item -> saveItem(item));
			
		} catch (Exception e) {
			logger.error(e);
			throw new ContentMgmtException(e);
		}
		
	}

	
	@SuppressWarnings("rawtypes")
	private void saveItem(APIUsageStat item) {
		
		if (item.ts==null)
			return;
		try {
			String sql="select * from kb_api_usage_stat where ts='"+item.ts+"'";
					NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(sql);
					List results = query.list();
					if (!results.isEmpty()) 
						return;
			
		} catch (Exception e) {
			logger.error(e.getClass().getName()+ " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + e.getMessage()!=null? (" | " +e.getMessage()):"");
			throw (e);
		}
		
		try {
			String sql="insert into kb_api_usage_stat (ts,  total, mean_time_total, "
					                                   + " total_post, mean_time_post, totdel, "
					                                   + " meantimedel, total_bounced) values ("+
					"'"+item.ts.toString()+"' ,"+
					String.valueOf(item.total)+","+
					String.valueOf(item.mean_time_total)+","+
					String.valueOf(item.total_post)+","+
					String.valueOf(item.mean_time_post)+","+
					String.valueOf(item.total_delete)+","+
					String.valueOf(item.mean_time_delete)+","+
					String.valueOf(item.total_bounced)+") ";
			
			logger.debug(sql);
			NativeQuery<?> query = sessionFactory.getCurrentSession().createNativeQuery(sql);
			query.executeUpdate();
			
		} catch (Exception e) {
			logger.error(e);
			throw (e);
		}
	}
	
	
	@Override
	@Transactional
	public void delete(Serializable domain_id) {
		Query<?> query = sessionFactory.getCurrentSession().createQuery("Delete from KbeeStatUsage K where K.domain_id=" + domain_id.toString());
		query.executeUpdate();
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
