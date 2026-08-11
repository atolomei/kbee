package com.novamens.kbee.content.service.datamanagement;



import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.CronExpressionJ8;
import com.novamens.service.ServiceLocator;


/**
 * <p>This Request can be used for operations that do not affect SolR indexes, otherwise the caller
 * must take care of reindexing SolR to reflect changes executed on the DB directly.
 * </p>
 * 
 * 
 * 
 * 
 * <p>Example.  Liquibase record: 
 *  
 *  {@code 
 * 	<changeSet author="atolomei" id="CronJob Delete idle trx 8">
		<sql><![CDATA[
			delete from kb_cronjob where name='Cancel Idle Transaction';
			insert into  kb_cronjob (id, lastmodifieduser, name, description, cronexpression, clazz, parameter) 
			values 
			(
			(select nextval('objectid_sequence')), 
			(select id from users where username='root@kbee'), 
			'Cancel Idle Transaction',  
		    'Cancel TRX that have been idle for more than 2.5h',
			'38 50 * * * *', 
			'com.novamens.kbee.content.service.datamanagement.SQLCronJobRequest', 
			'SELECT pg_terminate_backend(pid) from (select pid from pg_stat_activity where pid <> pg_backend_pid() and  state  like ''idle in transaction%''   and now()- xact_start > ''150 minute''\:\:interval) AS ACT');

		]]>
		</sql>
	</changeSet>
	}

 * </p>
 * 
 *
 */
public class SQLCronJobRequest extends AbstractCronJobRequest {
				
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SQLCronJobRequest.class.getName());
	
	private String sql;
	private String reindex;
	
	private boolean clean_hibernate_cache = false;
	
	public SQLCronJobRequest() {
		setName("Execute SQL");
	}
		
	
	public AbstractCronJobRequest clone() 	{
		try {
			SQLCronJobRequest clone = (SQLCronJobRequest)this.getClass().newInstance();
			super.onClone(clone);
			clone.sql=sql;
			clone.reindex=reindex;
			return clone;
		}
		catch (InstantiationException | IllegalAccessException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}
	
	
	
	public SQLCronJobRequest(String sql, String expression) {
		this.sql=sql;
		setCronExpression(expression);
	}
	
	public void setCronExpression(String expression) {
		super.setCronExpression(new CronExpressionJ8(expression));
	}
	
	
	/**
	 * {@code param} is loaded by the CronJobDao
	 * {@see com.novamens.kbee.scheduler.CronJobDao}
	 */
	@Override
	public void execute() {
		try {

			if (getParameters()!=null && getParameters().containsKey("param")) {
				String s= (String) getParameters().get("param");
				if (s!=null)
					setSql(s);
			}
			
			logger.debug("Starting SQL Request -> " + getSql());

			if (getSql()!=null) {
					if (getSql().toLowerCase().trim().startsWith("select "))
						getContentDao().executeSelectNativeQuery(getSql());
					else	 
						getContentDao().executeUpdateNativeQuery(getSql());
				}
				
			
			if (getReindex()!=null) {
				logger.error("Sorry Reindex is not implemented.");
				
				// if (isCleanHibernate()) 
				//	 getContentDao().cleanHibernateCache();
				// if (getReindex()!=null) {
				//	 ReindexCommand cmd = new ReindexCommand(getReindex());
				//	 cmd.setDoNotSu(true);
				//	 CommandService service = ServiceLocator.getService(CommandService.class);
				//	 service.add(cmd);
				// }
			}
			
		} catch (Exception e) {
			/**
			 * SQL Exceptions that prevent the Trx from committing must be propagated 
			 * to the Scheduler 
			 */
			logger.error(e);
			throw(e);
		}
		finally {
			if (isCleanHibernate()) 
				getContentDao().cleanHibernateCache();
			logger.debug("done.");
		}
	}
	
	public boolean isCleanHibernate() {
		return this.clean_hibernate_cache;
	}
	
	public String getSql() {
		return this.sql;
	}
	
	
	public void setSql(String sql) {
		this.sql=sql;
	}
	
	
	public String getReindex() {
		return this.reindex;
	}
	
	
	public void setReindex(String r) {
		this.reindex=r;
	}
	
	@Override
	public String toString() {
		return super.toString() + "| " + getSql(); 
	}

	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	
}
