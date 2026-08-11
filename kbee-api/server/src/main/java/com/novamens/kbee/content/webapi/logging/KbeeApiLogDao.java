package com.novamens.kbee.content.webapi.logging;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.kbee.sql.SqlPlatform;
import com.novamens.kbee.sql.SqlPlatformFactory;

public class KbeeApiLogDao implements ApiLogDao {
	private JdbcTemplate jdbcTemplate;
	private SqlPlatform sqlplatform;
	private String schema;
	private long lastid = 0;
		
	private DataSource dataSource;
	
	private static String FILE_EVENT_TABLE_NAME = "api_logevent";
	private static String SOAP_EVENT_TABLE_NAME = "api_soapevent";
	private static String SEQUENCE_NAME = "api_sequence";

	//a
	private static String INSERT_STATEMENT = "insert into "
			+ "api_logevent(event_id, event_domain, event_filesource, event_file, event_time, event_source, event_user, event_transaction, event_uri, event_method, event_request, event_status, event_response, event_processing_time, event_retry, event_retrynumber, event_contentclass) "
			+ "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	public void append(ApiLogEvent event) {
		if (event.getId()==null)
		((AbstractApiLogEvent)event).setId(getNewId());
		
		
		int value = jdbcTemplate.update(getInsertStatement(event), new Object[] { 
			event.getId(),
			event.getDomain(),
			event.getFileSource(),
			event.getFile(),
			getSqlPlatform().getTimestampValue(event.getTime()),
			event.getSource(),
			event.getUser(),
			event.getTransaction(),
			event.getUri(),
			event.getMethod(),
			event.getRequest(),
			event.getStatus().value(),
			event.getResponse(),
			event.getProcessingTime(),
			event.getRetry(),
			event.getRetryNumber(),
			event.getContentClass()
		});
		
		// // System.out.println(value);
		
		
		
	}
	
	@Transactional
	public void update(ApiLogEvent event) {
		jdbcTemplate.update(getUpdateStatement(event), new Object[] { 
				event.getRetry(),
				event.getRetryNumber(),
				event.getDomain(),
				event.getContentClass(),
				getSqlPlatform().getBooleanValue(event.isClosed()),
				//event.isClosed(),
				event.getId()
			});
	}
	
	public List<ApiLogEvent> getEvents(String[] statuses, int limit) {
		List<ApiLogEvent> events = this.jdbcTemplate.query(getEventsStatement(statuses, limit), new RowMapper<ApiLogEvent>() {
			public ApiLogEvent mapRow(ResultSet rs, int i) throws SQLException, DataAccessException {
				return KbeeApiLogDao.this.mapRow(rs);
			}
		});
		return events;
	}
	
	public List<ApiLogEvent> getEvents(String statement) {
		List<ApiLogEvent> events = this.jdbcTemplate.query(statement, new RowMapper<ApiLogEvent>() {
			public ApiLogEvent mapRow(ResultSet rs, int i) throws SQLException, DataAccessException {
				return KbeeApiLogDao.this.mapRow(rs);
			}
		});
		return events;
	}
	
	public long setClose(String criteria, boolean value) {
		return jdbcTemplate.update(getSetCloseStatement(criteria, value));
	}
	
	public synchronized Long getNewId() {
		
		String sequencename = SEQUENCE_NAME;
		
		if (getSchema()!=null && !getSchema().equals("")) 
			sequencename = getSchema() + "." + sequencename;
		
		if (lastid==0 || (lastid+1)%10==0) { 
			SqlPlatform sqlplatform = getSqlPlatform(); 
			Long value = (Long)this.jdbcTemplate.query(sqlplatform.nextSequenceQuery(sequencename), new ResultSetExtractor<Long>() {
				public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
					if (rs.next())
					return rs.getLong(1);
					return null;
				}
			});
			lastid = value;
		}
		else {
			lastid++;
		}
		
		return lastid;
	}
	
	public void setDataSource(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	public DataSource getDataSource() {
		return this.dataSource;
	}
	
	public void setSchema(String schema) {
		this.schema = schema;
	}
	
	public String getSchema() {
		return this.schema;
	}
	
	public ApiLogEvent mapRow(ResultSet rs) throws SQLException, DataAccessException {
		try {
			String method = rs.getString("EVENT_METHOD");
			
			if (rs.getLong("EVENT_RETRY")>0)
				return null;
			
			if ("POST".equals(method)) {
				if (rs.getString("EVENT_FILE")!=null) {
					FileUpdateEvent event = new FileUpdateEvent();
					event.setId(rs.getLong("EVENT_ID"));
					event.setRequest(rs.getString("EVENT_REQUEST"));
					event.setMethod(method);
					event.setUser(rs.getString("EVENT_USER"));
					event.setDomain(rs.getString("EVENT_DOMAIN"));
					event.setFileSource(rs.getString("EVENT_FILESOURCE"));
					event.setFile(rs.getString("EVENT_FILE"));
					event.setResponse(rs.getString("EVENT_RESPONSE"));
					event.setClosed(rs.getBoolean("EVENT_CLOSED"));
					
					Timestamp timestamp = rs.getTimestamp("EVENT_TIME");
					OffsetDateTime time = OffsetDateTime.ofInstant(Instant.ofEpochMilli(timestamp.getTime()), ZoneId.of("UTC"));
					event.setTime(time);
					
					event.setRetryNumber(rs.getInt("EVENT_RETRYNUMBER"));
					return event;
				}
				else {
					if ("user".equals(rs.getString("EVENT_CONTENTCLASS"))) {
						UserUpdateEvent event = new UserUpdateEvent(rs.getString("EVENT_URI"));
						event.setId(rs.getLong("EVENT_ID"));
						event.setRequest(rs.getString("EVENT_REQUEST"));
						event.setMethod(method);
						event.setUser(rs.getString("EVENT_USER"));
						event.setDomain(rs.getString("EVENT_DOMAIN"));
						event.setFileSource(rs.getString("EVENT_FILESOURCE"));
						event.setResponse(rs.getString("EVENT_RESPONSE"));
						event.setClosed(rs.getBoolean("EVENT_CLOSED"));
						
						Timestamp timestamp = rs.getTimestamp("EVENT_TIME");
						OffsetDateTime time = OffsetDateTime.ofInstant(Instant.ofEpochMilli(timestamp.getTime()), ZoneId.of("UTC"));
						event.setTime(time);
						
						event.setRetryNumber(rs.getInt("EVENT_RETRYNUMBER"));
						return event;
					}
					else {
						return null;
					}
				}
			}
			if ("DELETE".equals(method)) {
				if (rs.getString("EVENT_FILE")!=null) {
					FileDeleteEvent event = new FileDeleteEvent(rs.getString("EVENT_URI"));
					event.setId(rs.getLong("EVENT_ID"));
					event.setMethod(method);
					event.setUser(rs.getString("EVENT_USER"));
					event.setDomain(rs.getString("EVENT_DOMAIN"));
					event.setFileSource(rs.getString("EVENT_FILESOURCE"));
					event.setFile(rs.getString("EVENT_FILE"));
					event.setResponse(rs.getString("EVENT_RESPONSE"));
					event.setClosed(rs.getBoolean("EVENT_CLOSED"));
					
					Timestamp timestamp = rs.getTimestamp("EVENT_TIME");
					OffsetDateTime time = OffsetDateTime.ofInstant(Instant.ofEpochMilli(timestamp.getTime()), ZoneId.of("UTC"));
					event.setTime(time);
					
					event.setRetryNumber(rs.getInt("EVENT_RETRYNUMBER"));
					return event;
				}
				else
					return null;
			}
		}
		catch (SQLException e) {
			
		}
		return null;		
	}
	
	private String getEventsStatement(String[] statuses, int limit) {
		String stm = "SELECT * FROM API_LOGEVENT WHERE ";
		if (statuses.length>0) stm += "(";
		int s=0;
		for (String status : statuses) {
			if (s++>0) stm+= " OR ";
			stm += "EVENT_STATUS=" + String.valueOf(status);
		}
		if (statuses.length>0) stm += ")";
		stm += " AND EVENT_RETRY IS NULL ";
		stm += "ORDER BY EVENT_TIME ASC LIMIT " + String.valueOf(limit);
		return stm;
	}
	
	private String getInsertStatement(ApiLogEvent event) {
		String statement = INSERT_STATEMENT;
		if (getSchema()!=null && !getSchema().equals("")) {
			String tablename = event instanceof FileEvent || event instanceof UserEvent || event instanceof ValueEvent ? FILE_EVENT_TABLE_NAME : SOAP_EVENT_TABLE_NAME;
			statement = statement.replace(FILE_EVENT_TABLE_NAME, getSchema()+"."+tablename);
		}
		else {
			if (event instanceof SoapEvent) {
				String tablename = event instanceof FileEvent || event instanceof UserEvent ? FILE_EVENT_TABLE_NAME : SOAP_EVENT_TABLE_NAME;
				statement = statement.replace(FILE_EVENT_TABLE_NAME, tablename);
			}
		}
		return statement;
	}
	
	private String getUpdateStatement(ApiLogEvent event) {
		String statement = "UPDATE API_LOGEVENT SET ";
		statement += "EVENT_RETRY=?, ";
		statement += "EVENT_RETRYNUMBER=?, ";
		statement += "EVENT_DOMAIN=?, ";
		statement += "EVENT_CONTENTCLASS=?, ";
		statement += "EVENT_CLOSED=? ";
		statement += "WHERE EVENT_ID=?";
		return statement;
	}
	
	private String getSetCloseStatement(String criteria, boolean value) {
		String statement = "UPDATE API_LOGEVENT SET EVENT_CLOSED = "+(value?"true":"false")+ " WHERE "+criteria;
		return statement;
	}
	
	private SqlPlatform getSqlPlatform() {
		
		if (sqlplatform!=null) 
			return sqlplatform;
		
		Connection connection = null;
		try {
			connection = this.jdbcTemplate.getDataSource().getConnection();
			sqlplatform = SqlPlatformFactory.getPlatformFor(connection.getMetaData());
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			if (connection!=null) {
				try {
					connection.close();
				}				
				catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		return sqlplatform;
	}
}
