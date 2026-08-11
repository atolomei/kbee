package com.novamens.kbee.content.webapi.command;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.novamens.kbee.content.command.mt.Callback;
import com.novamens.kbee.content.command.mt.QueuedBatchProcessor;
import com.novamens.kbee.idoc.webapi.client.KbeeApiService;

import kbee.api.model.ApiFile;
import kbee.api.model.ApiValue;
import kbee.api.model.IAttributeValues;
import kbee.api.model.ICustomAttributeValue;
import kbee.api.service.ApiException;
import kbee.api.model.ApiResource;

public class MoveExecutor extends QueuedBatchProcessor {

	private List<ApiFile> batch;
	private Callback<ApiFile> callback;
	
	private Map<String, Object> parameters;
	private JdbcTemplate jdbcTemplate;
	
	//CREATE TABLE kb_Move (
	//	content_id bigint NOT NULL,
	//	domain character varying(64),
	//	status int,
	//	error_message character varying(256),
	//	CONSTRAINT move_id_pk PRIMARY KEY (content_id, domain)
	//)
	//WITH (
	//	OIDS = FALSE
	//)

	public class MoveEvent {
		public long contentId;
		public String  domain;
		public int status;
		public boolean error;
		public String message;
	}
	
	public MoveExecutor(List<ApiFile> batch, Callback<ApiFile> callback, Map<String, Object> parameters) {
		IncrementInstances();
		setBatch(batch);
		setCallback(callback);
		setParameters(parameters);
	}
	
	public void run() {
		try {
			com.novamens.hibernate.session.Session.open();
			KbeeApiService api = getTargetServer();
			//int f=0;
			for (ApiFile file : getBatch()) {
				try {
					//// System.out.println(String.valueOf(f++) + "/"+  file.getId());
					//if (resourceError(file) || !moved(file)) {
					if (!moved(file)) {
						//if (file.getClassName().equals("DocuSign Certificate")) {
							//// System.out.println("DocuSign Certificate");
						//}
						removeResourceError(file);
						api.update(file);
						file.setControlAttribute("RC", "200");
					}
				}
				catch(ApiException e) {
  					file.setControlAttribute("RC", String.valueOf(e.getHttpStatus()));
				}
			}
		}	
		catch(Exception e) {
			logger.debug("Batch Error", e);
		}
		finally {
			for (ApiFile file : getBatch()) {
				try {
					if (!moved(file))
					getCallback().execute(file);
				}
				catch (Exception callbackexception) {
					callbackexception.printStackTrace();
					logger.debug("File Error", callbackexception);
				}
			}
			DecrementInstances();
			com.novamens.hibernate.session.Session.close();
		}
	}
	
	public List<ApiFile> getBatch() {
		return batch;
	}
	
	public void setBatch(List<ApiFile> batch) {
		this.batch = batch;
	}
	
	public void setCallback(Callback<ApiFile> callback) {
		this.callback = callback;
	}
	
	public Callback<ApiFile> getCallback() {
		return callback;
	}
	
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
	
	public Map<String, Object> getParameters() {
		return this.parameters;
	}
	
	public Object getParameter(String value) {
		return getParameters().get(value);
	}
	
	public void setDataSource(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	private boolean moved(ApiFile file) {
		return getMove(file)!=null;
	}
	
	private void removeResourceError(ApiFile file) {
		for (IAttributeValues attribute : file.getAttributes()) {
			for (ApiValue value : attribute.getValues()) {
				if ("Resource Error".equals(value.getValue())) {
					attribute.getValues().remove(value);
					for (ApiResource resource : file.getResources()) {
						OffsetDateTime now = OffsetDateTime.now();
						String nowstr = now.toString();
						for (ICustomAttributeValue customvalue : resource.getControlAttributes()) {
							if ("lastModifedDate".equals(customvalue.getAttribute())) {
								customvalue.setValue(nowstr);
							}
						}
					}
					//file.setLastModifiedDate(OffsetDateTime.now());
					return;
				}
			}	
		}
	}
	
	private MoveEvent getMove(ApiFile file) {
		String stm = "SELECT CONTENT_ID FROM KB_MOVE WHERE CONTENT_ID = ? AND DOMAIN=?";
		try {
			MoveEvent event = this.jdbcTemplate.queryForObject(stm, new Object[] { Long.valueOf(file.getId()), file.getDomain() }, new RowMapper<MoveEvent>() {
				public MoveEvent mapRow(ResultSet rs, int i) throws SQLException, DataAccessException {
					MoveEvent event = new MoveEvent();
					event.contentId = rs.getLong("CONTENT_ID");
					return event;
				}
			});
			return event;
		}
		catch (DataAccessException e) {
			return null;
		}
	}
	
	private KbeeApiService getTargetServer() {
		String targeturl = (String)getParameter("target-url");
		String targetuser = (String)getParameter("target-user");
		String targetpassword = (String)getParameter("target-password");
		return new KbeeApiService(targeturl, targetuser, targetpassword);
	}
}
