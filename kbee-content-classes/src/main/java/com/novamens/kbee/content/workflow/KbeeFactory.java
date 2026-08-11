package com.novamens.kbee.content.workflow;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.hibernate.id.IdentifierGeneratorHelper;
import org.hibernate.id.IntegralDataTypeHolder;
import org.hibernate.id.enhanced.AbstractOptimizer;
import org.hibernate.id.enhanced.AccessCallback;
import org.hibernate.id.enhanced.PooledLoOptimizer;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.kbee.sql.SqlPlatform;
import com.novamens.kbee.sql.SqlPlatformFactory;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.workflow.Activity;
import com.novamens.workflow.WorkflowContext;
import com.novamens.workflow.WorkflowThreadStatus;
import com.novamens.workflow.WorkflowThreadStatus.Status;
import com.novamens.workflow.Factory;
import com.novamens.workflow.Procedure;
import com.novamens.workflow.Process;
import com.novamens.workflow.Task;

public class KbeeFactory implements Factory {
			
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeFactory.class.getName());

	private JdbcTemplate jdbcTemplate;
	private SqlPlatform sqlplatform;
	private String schema;
	private AccessCallback sequencerCallback;
	private AbstractOptimizer sequencer;

	public static String Process_Bean = "com.novamens.workflow.process";
	public static String Activity_Bean = "com.novamens.workflow.activity";
	
	public Process createProcess(Procedure procedure, WorkflowContext context) {
		KbeeProcess process = (KbeeProcess)ServiceLocator.getService(BeansService.class).getBean(Process_Bean, procedure, context);
		process.setId(getNewId());
		process.setProcedure(procedure);
		return process;
	}
	
	public Activity createActivity(Task task, WorkflowContext context, User user) {
		KbeeWorkflowActivity activity = (KbeeWorkflowActivity)ServiceLocator.getService(BeansService.class).getBean(Activity_Bean, task, context, user);
		if (((KbeeContext)context).getThread()!=null) {
			activity.setThread(((KbeeContext)context).getThread());
			KbeeWorkflowActivity parentActivity = (KbeeWorkflowActivity)((KbeeContext)context).getParentActivity();
			if (parentActivity!=null) {
				activity.setParent(parentActivity);
				Content content = parentActivity.getContent();
				KbeeContext parentContext = (KbeeContext)content.getService(WorkflowService.class).getContext();
				WorkflowThreadStatus threadStatus = parentContext.getThread(((KbeeContext)context).getThread());
				((KbeeWorkflowThreadStatus)threadStatus).setStatus(Status.RUNNING);
				content.getService(WorkflowService.class).update();
			}
		}	
		return activity;
	}
	
	public Long getNewId() {
		Serializable id = getSequencer().generate(getSequencerCallback());
		return (Long)id;
	}
	
	public void setDataSource(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	public void setSchema(String schema) {
		this.schema = schema;
	}
	
	public String getSchema() {
		return this.schema;
	}
	
	private synchronized AbstractOptimizer getSequencer() {
		if (this.sequencer == null) {
			this.sequencer = new PooledLoOptimizer(Long.class, 50);
		}
		return this.sequencer;
	}
	
	private synchronized AccessCallback getSequencerCallback() {
		if (sequencerCallback!=null)
			return sequencerCallback;
		this.sequencerCallback = new AccessCallback() {
			@Override
			public IntegralDataTypeHolder getNextValue() {
				return getNewSequencerSegment();
			}
			@Override
			public String getTenantIdentifier() {
				return null;
			}
		};
		return this.sequencerCallback;
	}
	
	private IntegralDataTypeHolder getNewSequencerSegment() {
		SqlPlatform sqlplatform = getSqlPlatform(); 
		String sequencename = "workflow_sequence";
		
		if (getSchema()!=null && !getSchema().equals("")) 
			sequencename = getSchema() + "." + sequencename;
		
		IntegralDataTypeHolder value = (IntegralDataTypeHolder)this.jdbcTemplate.query(sqlplatform.nextSequenceQuery(sequencename), new ResultSetExtractor<IntegralDataTypeHolder>() {
			public IntegralDataTypeHolder extractData(ResultSet rs) throws SQLException, DataAccessException {
				rs.next();
				final IntegralDataTypeHolder value = IdentifierGeneratorHelper.getIntegralDataTypeHolder(Long.class);
				value.initialize( rs, 1 );
				return value;
			}
		});
		
		return value;
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
					logger.error(e);
					throw new RuntimeException(e);
				}
			}
		}
		return sqlplatform;
	}
}
