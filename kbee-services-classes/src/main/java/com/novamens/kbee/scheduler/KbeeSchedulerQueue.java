package com.novamens.kbee.scheduler;


import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sql.DataSource;
import javax.transaction.Synchronization;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.engine.spi.SessionImplementor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.novamens.kbee.command.RequestCommandByClassNameServiceRequest;
import com.novamens.kbee.metrics.KbeeSystemMetricsService;
import com.novamens.kbee.sql.PostgresPlatform;
import com.novamens.kbee.sql.SqlPlatform;
import com.novamens.kbee.sql.SqlPlatformFactory;
import com.novamens.scheduler.AbstractServiceRequest;
import com.novamens.scheduler.SchedulerQueue;
import com.novamens.scheduler.SchedulerException;
import com.novamens.scheduler.SchedulerService;
import com.novamens.scheduler.ServiceRequest;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.spring.transaction.TransactionSynchronization;

import kbee.util.PropertiesFactory;

/**
 * 
 * <p>Persistent queue used by the {@link Scheduler}. 
 * executors threads {@link Batch}, will execute the Services Requests taken from this Queue by the Scheduler.
 *  
 * The Scheduler adds {@link ServiceRequest}, and the multiple {@Batch} executors threads take {@link ServiceRequest} from the queue to execute them.</p>
 * <p>ServiceRequests are Serialized into the persistent Queue and de-serialized to execute them.</p>
 * 
 * @see {@link KBeeScheduleService},  {@link ServiceRequest}
 * 
 * @param <T> is a instance of a subclass of {@link AbstractServiceServiceRequest}
 * 
 * <p>TBA. This version can suffer from starvation of the low priority requests when the stream of HP is high.</p>
 * 
 */						
public class KbeeSchedulerQueue<T extends ServiceRequest> implements SchedulerQueue<T> {
			
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger("Scheduler");
	
	private static Integer  max_queue_size  	=  Integer.valueOf(30);
	private static Integer  max_min_waiting     =  Integer.valueOf(15);
	
	private int it = 0;
	
	static double HP_PROBABILITY = 0.65;
	
	String _server_id = null; 
	String _hostname = null;
	
	static final private String UNIVERSAL = "universal";
	
	
	static {
		String m = PropertiesFactory.getInstance("kbee").getProperties().getProperty("scheduler.error.size", "60");
		try {
   			Integer i = Integer.valueOf(m);
			max_queue_size = i;
		}
		catch (Exception e) {
			logger.error(e);
			max_queue_size = 60;
		}

		String min = PropertiesFactory.getInstance("kbee").getProperties().getProperty("scheduler.error.minutes.waiting", "30");
		
		try {
			Integer n = Integer.valueOf(min);
			max_min_waiting = n;
		} 
		catch (Exception e) {
			logger.error(e);
			max_min_waiting = 30;
		}
		
		String hpp = PropertiesFactory.getInstance("kbee").getProperties().getProperty("scheduler.hprob", "0.68");
		try {
			Double i = Double.valueOf(hpp);
			HP_PROBABILITY = i;
		} 
		catch (Exception e) {
			logger.error(e);
			HP_PROBABILITY = 0.68;
		}

	}

	
	private static String ID_COLUMN 		= "ID"; 								// 1

	//private static String TITLE_COLUMN 		= "TITLE";								// 2
	//private static String PRIORITY_COLUMN 	= "PRIORITY";							// 3
	//private static String OBJECTID_COLUMN 	= "OBJECTID";
	
	private static String REQUEST_COLUMN 	= "REQUEST";
	private static String EXECUTE_AFTER_COLUMN 	= "EXECUTE_AFTER";
	
	private static String COMMAND_CLASS_NAME_COLUMN 	= "COMMAND_CLASS_NAME";
	private static String COMMAND_PARAMETERS_COLUMN 	= "COMMAND_PARAMETERS";
								
	private static String HOSTNAME_COLUMN	 		= "HOSTNAME";			
	private static String APPSERVERID_COLUMN 		= "APPSERVERID";
	
	
	

	// Metrics
	private Meter hp_meter_out; 
	private Meter lp_meter_out;
	
	private Counter hp_out;
	private Counter lp_out;
	
	private Counter hp_in;
	private Counter lp_in;
	
	private Map<Thread, Synchronization> transactions = Collections.synchronizedMap(new HashMap<Thread, Synchronization>());
											
	private LinkedBlockingQueue<T> buffer_hp = new LinkedBlockingQueue<T>();
	private LinkedBlockingQueue<T> buffer_lp = new LinkedBlockingQueue<T>();
	
	private Map<Thread, Set<Serializable>> removedbythread = Collections.synchronizedMap(new HashMap<Thread, Set<Serializable>>());	
	private Map<Thread, Set<Serializable>> enqueuedbythread = Collections.synchronizedMap(new HashMap<Thread, Set<Serializable>>());	
	
	private Set<Serializable> dequeued = Collections.synchronizedSet(new HashSet<Serializable>());
	private Serializable lasthpdequeued = (long)0, lastlpdequeued = (long)0;
	private Set<Serializable> allremoved = Collections.synchronizedSet(new HashSet<Serializable>());
	
	private int bufferSize = 320;
	
	private DataSource dataSource;
	private SessionFactory sessionFactory;
	private String tableName;
	private String schema;
	private Boolean is_postgres = null; 
	 
	public abstract class QueueSynchronization implements Synchronization {
		private String transactionId; 
		public QueueSynchronization(String txid) {
			this.transactionId = txid;
		}
		public String getTransactionId() {
			return transactionId;
		}
	}

	 public KbeeSchedulerQueue() {
	 }

	 
	/**
	 *	<p>Adds Request to the persistent Queue. Must be Thread safe</p>
	 */
	public Serializable enqueue(T request) throws SchedulerException {
	
		Connection connection = null;
		PreparedStatement statement = null;
		
		Long r_id = null;
		
		try {
			if (request.getId()!=null && (dequeued.contains(request.getId())||allremoved.contains(request.getId()))) {
				update(request);
				allremoved.remove(request.getId());
				getRemovedByThread().remove(request.getId());
				getEnqueuedByThread().add(request.getId());
				return request.getId();
			}
			
			request.setInQueueTimestamp(System.currentTimeMillis());
			connection = getConnection(getSessionFactory().getCurrentSession());
			String enqueuestatement = this.getEnqueueStatement();
			statement = connection.prepareStatement(enqueuestatement);
			ByteArrayOutputStream ostream = new ByteArrayOutputStream();	
			ObjectOutputStream oostream = new ObjectOutputStream(ostream);

			oostream.writeObject(request);
			oostream.flush();
			ostream.close();

			
			r_id = getNewOId();
			
			// id is generated by the database engine
			statement.setLong(1, r_id ); //getNewOId()
			
			statement.setBytes(2, ostream.toByteArray());
			statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			statement.setInt(4, request.getPriority());

			String des = request.getName() + (request.getDescription()!=null?( " | " + request.getDescription()) :"");
			if (des.length()>512)
				des=des.substring(0, 512);
			statement.setString(5, des); // des
			
			String name = request.getName();
			if (name==null)
				name=request.getClass().getName();
			if (name.length()>64)
				name=name.substring(0, 64);
			statement.setString(6, name);  // title
			
			String oid = request.getObjectID();
			statement.setString(7, oid!=null?oid:"");  // objectid
				

			if (request.getExecuteAfter()!=null)
				statement.setObject(8, request.getExecuteAfter());
			else
				statement.setTimestamp(8, null);
			
			statement.setObject(9, null);
			statement.setObject(10, null);
			
			statement.setString(11, request.getServerHost());
			statement.setString(12, request.getApplicationServerId());
			 
			
			statement.executeUpdate();
			
			if (request.getPriority()==SchedulerService.HIGH_PRIORITY)
				getCounterInHP().inc();
			else 
				getCounterInLP().inc();
		
			return r_id;
			
		} 
		catch (IOException e) {
			logger.error(e);			
			throw new SchedulerException(e);
		}
		catch (SQLException e) {
			logger.error(e);			
			throw new SchedulerException(e);
		}
		catch (Exception e) {
			logger.error(e);			
			throw new SchedulerException(e);
		}
		finally {
			try {
				close(null, statement, null);
			} 
			catch (IOException e) {
				logger.error(e);			
			}
		}
	}

	
	
	/**
	 * <p>removes and returns next request from the queue. If the persistent queue is empty, returns null</p>
	 * 
	 */
	public synchronized T dequeue() throws SchedulerException {
	
		double dice=Math.random();
	
		/* High priority, if empty, try low priority */
		if (dice<=HP_PROBABILITY) {
			
			if (buffer_hp.isEmpty()) 
				fillBuffer(SchedulerService.HIGH_PRIORITY);
			
			if (!buffer_hp.isEmpty()) {
				T request = buffer_hp.poll();
				dequeued.add(request.getId());
				lasthpdequeued = request.getId();
				getCounterOutHP().inc();
				getMeterOutHP().mark();
				return request;
			}
			
			if (buffer_lp.isEmpty()) 
				fillBuffer(SchedulerService.LOW_PRIORITY);
			
			if (!buffer_lp.isEmpty()) {
				T request = buffer_lp.poll();
				dequeued.add(request.getId());
				lastlpdequeued = request.getId();
				getCounterOutLP().inc();
				getMeterOutLP().mark();
				return request;
			}
			
			return null;
		}
		

		/* low priority, if empty, try high priority */
		
		if (buffer_lp.isEmpty()) 
			fillBuffer(SchedulerService.LOW_PRIORITY);
			
		if (!buffer_lp.isEmpty()) {
			T request = buffer_lp.poll();
			dequeued.add(request.getId());
			lastlpdequeued = request.getId();
			getCounterOutLP().inc();
			getMeterOutLP().mark();
			return request;
		}
			
		if (buffer_hp.isEmpty()) 
			fillBuffer(SchedulerService.HIGH_PRIORITY);
		
		if (!buffer_hp.isEmpty()) {
			T request = buffer_hp.poll();
			dequeued.add(request.getId());
			lasthpdequeued = request.getId();
			getCounterOutHP().inc();
			getMeterOutHP().mark();
			return request;
		}
			
		return null;
	}
	
	/**
	 * <p>Removes element from the queue after it was executed</p> 
	 */
	public synchronized void remove(T request) throws SchedulerException {
		addRemoved(request.getId());
		addTransactionSynchronization();
	}
	
	/**
	 * 
	 */
	public boolean isEmpty() throws SchedulerException {
		
		if (!this.buffer_lp.isEmpty())
			return false;
		
		if (!this.buffer_hp.isEmpty())
			return false;
		 
		if (this.buffer_hp.isEmpty()) {
			fillBuffer(SchedulerService.HIGH_PRIORITY);
		}	
		
		if (!this.buffer_hp.isEmpty())
			return false;
		
		if (this.buffer_lp.isEmpty()) { 
			fillBuffer(SchedulerService.LOW_PRIORITY);
		}	
		
		return this.buffer_lp.isEmpty();
	}
	
	/**
	 *  libera todos los recursos del thread
	 */
	public void dispose()  {
		Set<Serializable> removed = removedbythread.get(Thread.currentThread());
		if (removed!=null) {
			for (Serializable id : removed) {
				allremoved.remove(id);
				dequeued.remove(id);
			}
		}
		removedbythread.remove(Thread.currentThread());
		Set<Serializable> enqueued = enqueuedbythread.get(Thread.currentThread());
		if (enqueued!=null) {
			for (Serializable id : enqueued) {
				dequeued.remove(id);
			}
		}
		enqueuedbythread.remove(Thread.currentThread());
		synchronized(Thread.currentThread()) {	
			Thread.currentThread().notifyAll();
		}
		transactions.remove(Thread.currentThread());
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.clearSynchronization();
		}	
	}
	
	/**
	 * <p>Wipes off the persistent queue. All the messages in the queue are lost</p>
	 */
	@Override					
	public synchronized void restartQueue() throws SchedulerException {
		try {
			_server_id= ServiceLocator.getService(ApplicationServerService.class).getApplicationServerId();
			_hostname = ServiceLocator.getService(ApplicationServerService.class).getServerHost();
			this.buffer_lp.clear();
			this.buffer_hp.clear();
			this.dequeued.clear();
			this.transactions.clear();
			this.dequeued.clear();
			this.allremoved.clear();
			lastlpdequeued = 0;
			lasthpdequeued = 0;
			//this.dequeuedbythread.clear();
			this.removedbythread.clear();
		} 
		catch (Exception sqle) {
			logger.error(sqle);			
			throw new SchedulerException(sqle);
		}
	}
	
	
	/**
	 */
	public synchronized void resetQueue() throws SchedulerException {
		restartQueue();
	}

	
	/**
	 * <p>Removes phantom requests from the queue. 
	 * A Phantom request is a request that the scheduler has tried to execute 3 times without success.</p>
	 */
	public synchronized void cleanPhantomRequests() throws SchedulerException {
		
		Connection connection = null;
		PreparedStatement statement = null;
		
		try {
			connection = this.getDataSource().getConnection();
			String sql = this.getDeleteBrokenStatement();
			statement = connection.prepareStatement(sql);
			int delete = statement.executeUpdate();
			logger.debug("Query deletes " + delete + " rows.");
			connection.commit();
		} 
		catch (SQLException sqle) {
			logger.error(sqle);			
			throw new SchedulerException(sqle);
		} 
		finally {
			try {
				close(connection, statement,null);
			} 
			catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	/**
	 * <p>Size of the persistent queue on disk</p>
	 */
	public int getSize() throws SchedulerException {
		
		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.getDataSource().getConnection();
			String sql = this.getQueueSizeStatement();
			statement = connection.prepareStatement(sql);
			resultSet = statement.executeQuery();
			if (resultSet.next()) 
				return resultSet.getInt(1);
			return -1;
		} 
		catch (SQLException sqle) {
			logger.error(sqle);			
			throw new SchedulerException(sqle);
			
		} 
		finally {
			
			try {
				close(connection, statement,null);
			} 
			catch (IOException e) {
				logger.error(e);			
			}
		}
	}

	/**
	 * 
	 * <p>Size of the persistent queue on disk</p>
	 * 
	 */
	@Override
	public String getQueueStatus() throws SchedulerException {
		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.getDataSource().getConnection();
			String sql = this.getCheckQueueStatement();
			statement = connection.prepareStatement(sql);
			resultSet = statement.executeQuery();
			
			if (resultSet.next()) { 
				int blocked=resultSet.getInt(1);
				 if (blocked<max_queue_size)
					 return "ok";
				 else
					 return String.valueOf(blocked) + " requests waiting for too long ("+String.valueOf(max_min_waiting)+"+ min)";
			}
			return "error";
		} 
		catch (SQLException sqle) {
			logger.error(sqle);			
			throw new SchedulerException(sqle);
		} 
		finally {
			
			try {
				close(connection, statement,null);
			} catch (IOException e) {
				logger.error(e);			
				
			}
		}
	}
	
	/**
	 * <p>Size of the persistent queue on disk that gave 3 errors</p>
	 */
	public int getErrorSize() throws SchedulerException {
		ResultSet resultSet = null;
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = this.getDataSource().getConnection();
			String sql = this.getQueueErrorSizeStatement();
			statement = connection.prepareStatement(sql);
			resultSet = statement.executeQuery();
			if (resultSet.next()) 
				return resultSet.getInt(1);
			return -1;
		} 
		catch (SQLException sqle) {
			logger.debug(sqle);
			throw new SchedulerException(sqle);
		} 
		finally {
			try {
				close(connection, statement,null);
			} 
			catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	/**
	 * @param request
	 * @throws IOException
	 */
	public void update(T request) throws SchedulerException {
		Connection connection = null;
		PreparedStatement statement = null;
		try {
			connection = getConnection(getSessionFactory().getCurrentSession());
			String sql = this.getUpdateStatement();
			
			statement = connection.prepareStatement(sql);
			String errorMessage =  request.getErrorMessage();
			
			if (errorMessage.length()>512)
				errorMessage = errorMessage.substring(0, 512);
			statement.setString(1, errorMessage);
			
			statement.setLong(2, (Long)request.getId());
			statement.executeUpdate();
		} 
		catch (SQLException sqle) {
			logger.debug(sqle);
			throw new SchedulerException(sqle);
		} 
		finally {
			try {
				close(null, statement,null);
			} 
			catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public DataSource getDataSource() {
		return this.dataSource;
	}
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}
	
	public void setBufferSize(int size) {
		this.bufferSize = size;
	}
	
	public int getBufferSize() {
		return this.bufferSize;
	}
	
	public void setTableName(String name) {
		this.tableName = name;
	}
	
	public String getTableName() {
		return this.tableName;
	}
	
	public String getSchema() {
		if(this.schema!=null && !"".equals(this.schema.trim()))
			return this.schema.trim()+".";
		return "";
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	protected Long getNewOId() {
		SqlPlatform sqlplatform = getSqlPlatform(); 
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		Long value = (Long)jdbcTemplate.query(sqlplatform.nextSequenceQuery( getSchema()+"scheduler_sequence"), new ResultSetExtractor<Long>() {
			public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
				if (rs.next())
				return rs.getLong(1);
				return null;
			}
		}); 
		return value;
	}
			
	private boolean isPostgres() {
		if (is_postgres==null)
			is_postgres =  Boolean.valueOf( getSqlPlatform() instanceof PostgresPlatform );
		return is_postgres.booleanValue();
		
	}
	private SqlPlatform getSqlPlatform() {
		Connection connection = null;
		try {
			connection = getDataSource().getConnection();
			return SqlPlatformFactory.getPlatformFor(connection.getMetaData());
		}
		catch (SQLException e) {
			logger.error(e);
			throw new KbeeRuntimeException(e);
		}
		finally {
			if (connection!=null) {
				try {
					connection.close();
				}				
				catch (SQLException e) {
					logger.error(e);
					throw new KbeeRuntimeException(e);
				}
			}
		}
	}

	public Counter getCounterInHP() {
		if (hp_in==null)
			hp_in = ServiceLocator.getService(KbeeSystemMetricsService.class).getMetrics().counter(MetricRegistry.name(KbeeSchedulerQueue.class, "requests_in", "hp"));
		return hp_in;
	}

	public Counter getCounterInLP() {
		if (lp_in==null)
			lp_in = ServiceLocator.getService(KbeeSystemMetricsService.class).getMetrics().counter(MetricRegistry.name(KbeeSchedulerQueue.class, "requests_in", "lp"));
		return lp_in;
	}

	public Counter getCounterOutLP() {
		if (lp_out==null)
			lp_out = ServiceLocator.getService(KbeeSystemMetricsService.class).getMetrics().counter(MetricRegistry.name(KbeeSchedulerQueue.class, "requests_out", "lp"));
		return lp_out;
	}

	public Counter getCounterOutHP() {
		if (hp_out==null)
			hp_out = ServiceLocator.getService(KbeeSystemMetricsService.class).getMetrics().counter(MetricRegistry.name(KbeeSchedulerQueue.class, "requests_out", "hp"));
		return hp_out;
	}
			
	public Meter getMeterOutHP() {
		if (hp_meter_out==null)
			hp_meter_out = ServiceLocator.getService(KbeeSystemMetricsService.class).getMetrics().meter(MetricRegistry.name(KbeeSchedulerQueue.class, "meter_requests_out", "hp"));
		return hp_meter_out;
	}

	public Meter getMeterOutLP() {
		if (lp_meter_out==null)
			lp_meter_out = ServiceLocator.getService(KbeeSystemMetricsService.class).getMetrics().meter(MetricRegistry.name(KbeeSchedulerQueue.class, "meter_requests_out", "lp"));
		return lp_meter_out;
	}

	@Override
	public Map<String, String> getConfigurableParameters() {
		Map<String, String> map = new HashMap<String, String>();
		try {
			map.put("scheduler.error.minutes.waiting", 	String.valueOf(max_min_waiting) );
			map.put("scheduler.hprob", 					  String.valueOf(HP_PROBABILITY) );
			map.put("scheduler.error.size",   		String.valueOf(max_queue_size) );
		} catch (Exception e) {
			logger.error(e);
		}
		return map;
	}
	
	private Connection getConnection(Session session) {
		return ((SessionImplementor)session).connection();
	}
	
	private String getEnqueueStatement() {
		return "INSERT INTO " + getSchema()+getTableName() + "(ID, REQUEST, TIME, PRIORITY, ERROR_COUNT, ERROR_MESSAGE, DESCRIPTION, TITLE, OBJECTID, EXECUTE_AFTER, COMMAND_CLASS_NAME, COMMAND_PARAMETERS, HOSTNAME, APPSERVERID) VALUES (?, ?, ?, ?, 0, NULL, ?, ?, ?, ?, ?, ?, ?, ?)";
	}

	private String getCheckQueueStatement() {
		if (isPostgres())
			return "SELECT count(*) from "+ getSchema()+"scheduler  WHERE error_count=0 AND time < NOW() - INTERVAL '  "+String.valueOf(max_min_waiting)+" minutes'";
		else
			return "SELECT COUNT(*) FROM  " + getSchema()+"Scheduler WHERE error_count=0 AND time < SYSDATE - INTERVAL '"+String.valueOf(max_min_waiting)+"' MINUTE";
	}
	
	private String getGetHPStatement() { 
		if (isPostgres()) 
			return "SELECT ID, REQUEST, TIME, PRIORITY, TITLE, OBJECTID, EXECUTE_AFTER, COMMAND_CLASS_NAME, COMMAND_PARAMETERS, HOSTNAME, APPSERVERID FROM " + getSchema()+getTableName() + " WHERE ERROR_COUNT<3 AND PRIORITY<=1  " + " AND (appserverid='"+ _server_id  +"' OR appserverid='"+  UNIVERSAL +"' ) AND (execute_after is null or execute_after<= ?) AND ID>? ORDER BY PRIORITY, TIME ASC LIMIT 800";
		else
			return "SELECT ID, REQUEST, TIME, PRIORITY, TITLE, OBJECTID, EXECUTE_AFTER, COMMAND_CLASS_NAME, COMMAND_PARAMETERS, HOSTNAME, APPSERVERID FROM " + getSchema()+getTableName() + " WHERE ERROR_COUNT<3 AND PRIORITY<=1  " + " AND (appserverid='"+ _server_id  +"' OR appserverid='"+  UNIVERSAL +"' ) AND (execute_after is null or execute_after<= ?) AND ID>? ORDER BY PRIORITY, TIME ASC ";
	}
	
	private String getGetLPStatement() {
		if (isPostgres())
			return "SELECT ID, REQUEST, TIME, PRIORITY, TITLE, OBJECTID, EXECUTE_AFTER, COMMAND_CLASS_NAME, COMMAND_PARAMETERS, HOSTNAME, APPSERVERID  FROM " + getSchema()+getTableName() + " WHERE ERROR_COUNT<3 AND PRIORITY=2 " + " AND (appserverid='"+ _server_id  +"' OR appserverid='"+  UNIVERSAL +"' ) AND (execute_after is null or execute_after<= ?) AND ID>? ORDER BY TIME ASC LIMIT 800";
		else
			return "SELECT ID, REQUEST, TIME, PRIORITY, TITLE, OBJECTID, EXECUTE_AFTER, COMMAND_CLASS_NAME, COMMAND_PARAMETERS, HOSTNAME, APPSERVERID  FROM " + getSchema()+getTableName() + " WHERE ERROR_COUNT<3 AND PRIORITY=2 "+ "  AND (appserverid='"+ _server_id  +"' OR appserverid='"+  UNIVERSAL +"' ) AND appserverid='"+  _server_id +"' AND (execute_after is null or execute_after<= ?) AND ID>? ORDER BY TIME ASC ";
	}
						
	private String getDeleteBrokenStatement() {
		return "DELETE FROM " + getSchema() + getTableName() + " WHERE ERROR_COUNT>=3"; 
	}
	
	private String getQueueErrorSizeStatement() {
		return "SELECT COUNT(*) FROM " + getSchema()+getTableName() + " WHERE ERROR_COUNT>=3"; 
	}

	private String getQueueSizeStatement() {
		return "SELECT COUNT(*) FROM " + getSchema()+getTableName() + " WHERE ERROR_COUNT<3"; 
	}

	private String getUpdateStatement() {
		return "UPDATE " + getSchema() + getTableName() + " SET ERROR_COUNT=ERROR_COUNT+1, ERROR_MESSAGE=? WHERE ID=?";
	}
	
	private String getRemoveStatement(Set<Serializable> removed) {
		StringBuilder statement = new StringBuilder(); 
		statement.append("DELETE FROM " + getSchema() + getTableName() + " WHERE ID IN ( ");
		boolean is_first=true;
		for (Serializable removedId : removed) {
			if (!is_first)
				statement.append(", ");	
			statement.append("'" + removedId + "'");
			is_first = false;
		}
		statement.append(")");
		return statement.toString();
	}
	
	/**
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private synchronized void fillBuffer(int priority) throws SchedulerException {
	
		if (priority==SchedulerService.HIGH_PRIORITY && !this.buffer_hp.isEmpty())
			return;

		if (priority==SchedulerService.LOW_PRIORITY && !this.buffer_lp.isEmpty())
			return;

		if(_server_id==null) {
			_server_id=  ServiceLocator.getService(ApplicationServerService.class).getApplicationServerId();
			_hostname = ServiceLocator.getService(ApplicationServerService.class).getServerHost();
		}
		
		T request = null;
			
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			connection = this.getDataSource().getConnection();
			
			String getstatement = (priority==SchedulerService.HIGH_PRIORITY ? this.getGetHPStatement() : this.getGetLPStatement());
			
			statement = connection.prepareStatement(getstatement);
			
			statement.setObject(1, OffsetDateTime.now() );
			Long limitid = dequeued.isEmpty() ? 0 : (priority==SchedulerService.HIGH_PRIORITY ? (long)lasthpdequeued : (long)lastlpdequeued);
			
			it++; if (it%100==0) limitid=(long)0; if (it>1000) it=0;
			
			statement.setLong(2, limitid);

			resultSet = statement.executeQuery();
			int i = 0;
			synchronized(this.allremoved) {
				while (resultSet.next() && i<getBufferSize()) {
					long requestId = resultSet.getInt(ID_COLUMN);
					if (!this.allremoved.contains(requestId) && !this.dequeued.contains(requestId))	{
						
						byte bytes[] = resultSet.getBytes(REQUEST_COLUMN);
						
						ByteArrayInputStream istream = null;
						ObjectInputStream  oistream = null;
						
						try {
							
							String hostname 	= resultSet.getString(HOSTNAME_COLUMN);							
							String appserverid  = resultSet.getString(APPSERVERID_COLUMN);
										
							boolean is_right_source = (((hostname==null) || (hostname.equals(hostname))) && 
									((appserverid==null)  || _server_id.equals(appserverid) || appserverid.equals(UNIVERSAL)));

							if (is_right_source) {
								if (bytes!=null && bytes.length>0) {
									istream = new ByteArrayInputStream(bytes);
									oistream = new ObjectInputStream(istream);
									request = (T) oistream.readObject();
								}
								else { 
									String classname = resultSet.getString(COMMAND_CLASS_NAME_COLUMN);
									String command_par = resultSet.getString(COMMAND_PARAMETERS_COLUMN);
									RequestCommandByClassNameServiceRequest re=new RequestCommandByClassNameServiceRequest(classname, command_par);
									request = (T) (re);
									logger.debug(RequestCommandByClassNameServiceRequest.class.getName() + " -> " + classname + " | " + command_par);
								}
										
								AbstractServiceRequest a_request = ((AbstractServiceRequest)request); 
									
								a_request.setId(resultSet.getLong(ID_COLUMN));
			
								// --	
								// a_request.setPriority(resultSet.getInt(PRIORITY_COLUMN));
								// a_request.setName(resultSet.getString(TITLE_COLUMN));
								// a_request.setObjectID(resultSet.getString(OBJECTID_COLUMN));
								// ---
								// Timestamp is saved and restored in UTC
										
								Timestamp ts = resultSet.getTimestamp(EXECUTE_AFTER_COLUMN);
										
								if(ts!=null) {
									OffsetDateTime dt=OffsetDateTime.ofInstant(Instant.ofEpochMilli(ts.getTime()), ZoneOffset.UTC);
									a_request.setExecuteAfter(dt);
								}
										
								if (priority==SchedulerService.HIGH_PRIORITY) { 
									this.buffer_hp.add(request);
								}	
								else {
									this.buffer_lp.add(request);
								}	
								logger.debug("Adding to Buffer: " + request.toString());
								i++;
							}
							else {
								if (hostname!=null) {
									if (!_hostname.equals(hostname)) 
										logger.error("request(hostname) -> " +hostname +" | hostname -> " + _hostname);
								}
								if (appserverid!=null) {
									if (!_server_id.equals(appserverid))
										logger.error("request(application_server_id) is incorrect ->  received : " +appserverid +" |  expected: application_server_id -> " + _server_id);
								}
						    }
						} 
						finally {
							if (istream!=null)
								istream.close();
							if (oistream!=null)
								oistream.close();
						}
					}	
				}
			}
		}
		catch (IOException e) {
			logger.error(e);
			throw new SchedulerException(e);
		}
		catch (ClassNotFoundException e) {
			logger.error(e);
			throw new SchedulerException(e);
		}
		catch (SQLException e) {
			logger.error(e);
			throw new SchedulerException(e);
		}
		catch (RuntimeException e) {
			logger.error(e);
			throw new SchedulerException(e);
		}
		finally {
			try {
				close(connection, statement, resultSet);
			} 
			catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	private void addRemoved(Serializable requestId) {
		getRemovedByThread().add(requestId);
		this.allremoved.add(requestId);
	}
	
	private Set<Serializable> getRemovedByThread() {
		Set<Serializable> removed = removedbythread.get(Thread.currentThread());
		if (removed == null) {
			removed = new HashSet<Serializable>();
			removedbythread.put(Thread.currentThread(), removed);
		}
		return removed;
	}
	
//	private void addDequeued(Serializable requestId) {
//		dequeued.add(requestId);
//	}
	
	private Set<Serializable> getEnqueuedByThread()  {
		Set<Serializable> enqueued = enqueuedbythread.get(Thread.currentThread());
		if (enqueued == null) {
			enqueued = new HashSet<Serializable>();
			enqueuedbythread.put(Thread.currentThread(), enqueued);
		}
		return enqueued;
	}
	
	/**
	 * @throws IOException
	 */
	private void doRemove() throws SchedulerException {

		Set<Serializable> removed = this.removedbythread.get(Thread.currentThread());
		
		if (removed==null || removed.isEmpty()) 
			return;

		Connection connection = null;
		PreparedStatement statement = null;
		
		try {
			connection = getConnection(getSessionFactory().getCurrentSession());
			String sql = this.getRemoveStatement(removed);
			statement = connection.prepareStatement(sql);
			statement.executeUpdate();
		}
		catch (SQLException e) {
			logger.error(e);
			throw new SchedulerException(e);
		}
		catch (RuntimeException e) {
			logger.error(e);
			throw new SchedulerException(e);
		}
		finally {
			try {
				close(null, statement, null);
			} 
			catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	/**
	 * @param connection
	 * @param statement
	 * @param resultset
	 * @throws IOException
	 */
	private void close(Connection connection, PreparedStatement statement, ResultSet resultset) throws IOException {
		try {
			if (statement 	!= null) statement.close();
			if (resultset 	!= null) resultset.close();
			if (connection 	!= null) connection.close();
		} 
		catch (SQLException e) {
			logger.error(e);
			throw new IOException(e);
		}
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void addTransactionSynchronization() {
		
		Synchronization synchronization = transactions.get(Thread.currentThread());
		Transaction transaction = getSessionFactory().getCurrentSession().getTransaction();

		if (synchronization == null || !((QueueSynchronization)synchronization).getTransactionId().equals(String.valueOf(transaction.hashCode()))) {
			synchronization = new QueueSynchronization(String.valueOf(transaction.hashCode())) {
				@Override
				public void beforeCompletion() {
					try {
						doRemove();
						transactions.remove(Thread.currentThread());
					}
					catch (SchedulerException e) {
						logger.error(e);
						throw new KbeeRuntimeException(e);
					}
				}
				@Override
				public void afterCompletion(int status) {
					synchronized(allremoved) {
						if (status == TransactionSynchronization.STATUS_COMMITTED) {
//							Set<Serializable> removed = removedbythread.get(Thread.currentThread());
//							if (removed!=null) {
//								for (Serializable id : removed) {
//									allremoved.remove(id);
//								}
//							}
//							removedbythread.remove(Thread.currentThread());
//							Set<Serializable> dequeued = dequeuedbythread.get(Thread.currentThread());
//							if (dequeued!=null) {
//								for (Serializable id : removed) {
//									dequeued.remove(id);
//								}
//							}
//							dequeuedbythread.remove(Thread.currentThread());
//							synchronized(Thread.currentThread()) {	
//								Thread.currentThread().notifyAll();
//							}
//							transactions.remove(Thread.currentThread());
							dispose();
						}
						else {
							Set<Serializable> removed = removedbythread.get(Thread.currentThread());
							if (removed!=null) {
								for (Serializable id : removed) {
									allremoved.remove(id);
								}
								removed.clear();
							}
						}
					}	
				}
			};
			transaction.registerSynchronization(synchronization);
			transactions.put(Thread.currentThread(), synchronization);
		}
	}
}