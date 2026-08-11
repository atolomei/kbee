package com.novamens.kbee.content.webapi.command;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import com.novamens.kbee.content.command.mt.Queue;
import com.novamens.kbee.content.command.mt.QueueException;
import com.novamens.kbee.content.webapi.resource.FileUrl;
import com.novamens.kbee.idoc.webapi.client.KbeeApiService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;

import kbee.api.model.ApiFile;
import kbee.api.model.ApiValue;
import kbee.api.model.IAttributeValues;
import kbee.api.model.ApiResource;
import kbee.api.model.IResultSet;

public class RemoteQueue implements Queue<ApiFile> {

	private LinkedList<ApiFile> buffer = new LinkedList<ApiFile>();
	private Set<String> dequeued = new HashSet<String>();
	
	private int bufferSize = 64;
	
	private KbeeApiService sourceserver = null; //, targetserver = null;
	private JdbcTemplate jdbcTemplate;
	private Map<String, Object> parameters;
	private IResultSet<ApiFile> files;
	private String criteria;
	
	//	CREATE TABLE kb_Move (
	//		content_id bigint NOT NULL,
	//		domain character varying(64),
	//		status int,
	//		error_message character varying(256),
	//		CONSTRAINT move_id_pk PRIMARY KEY (content_id, domain)
	//	)
	//	WITH (
	//	OIDS = FALSE
	//)
	
	//	create index move_idx on kb_move using btree (content_id);

	//	public class MoveEvent {
	//		public long contentId;
	//		public String  domain;
	//		public int status;
	//		public boolean error;
	//		public String message;
	//	}

	@Override
	public synchronized ApiFile dequeue() throws QueueException {
		if (buffer.isEmpty()) 
			fillBuffer();
		
		if (buffer.isEmpty())
			return null;
		
		ApiFile file = buffer.removeFirst();
		
		dequeued.add(file.getId());
		
		return file;
	}
	
	public void enqueue(ApiFile file) throws QueueException {
		Transaction tx = null;
		try {
			tx = beginTransaction();
			String stm = "INSERT INTO KB_MOVE (CONTENT_ID, DOMAIN, STATUS, ERROR_MESSAGE) VALUES(?, ?, ?)";
			String status = file.getControlAttributeValue("RC");
			jdbcTemplate.update(stm, new Object[] {	Long.valueOf(file.getId()), file.getDomain(), status, "" });
			tx.commit();
		}
		catch (Exception e) {
			tx.rollback();
		}
	}
	
	public synchronized void remove(ApiFile file) throws QueueException {
		Transaction tx = null;
		try {
			tx = beginTransaction();
			int rc = Integer.valueOf(file.getControlAttributeValue("RC"));
			
			//if (rc!=200) {
			//	// System.out.println(rc);
			//
			// }
		
			String stm = "INSERT INTO KB_MOVE (CONTENT_ID, DOMAIN, STATUS) VALUES(?, ?, ?)";
			jdbcTemplate.update(stm, new Object[] {	Long.valueOf(file.getId()), file.getDomain(), rc });
			tx.commit();
		}
		catch (Exception e) {
			tx.rollback();
		}		
	}
	
	public long size() throws QueueException {
		return getFiles().getSize();
	}
	
	public void setBufferSize(int size) {
		this.bufferSize = size;
	}
	
	public int getBufferSize() {
		return this.bufferSize;
	}
	
	public void setDataSource(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
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
	
	public void close() {
		
	}
	
	private ApiFile adaptForMove(ApiFile file) {
		List<IAttributeValues> attributevalues = file.getSeededAttributes();
//		if (file.getClassName().equals("DocuSign Certificate")) {
//			for (IProxy proxy : file.getRelationships()) {
//				proxy.setHRef(getTargetHref(proxy));
//			}
//		}
		for (IAttributeValues attributevalue : attributevalues) {
			for (ApiValue ivalue : attributevalue.getValues()) {
				ivalue.setHRef(null);
				ivalue.setId(null);
				if (attributevalue.getAttribute().getName().equals("Create Date")) {
					String date = ivalue.getValue();
					int i = date.indexOf("T");
					if (i>0) {
						date = date.substring(0,i);
						ivalue.setValue(date);
					}
				}
			}
		}
		for (ApiResource resource : file.getResources()) {
			resource.setControlAttribute("name", resource.getName());
			String urlvalue = resource.getControlAttributeValue("proxy-url");
			if (urlvalue!=null) {
				FileUrl url = new FileUrl(urlvalue);
				resource.setHRef(url.getPath());
				String fileid = url.getParameter("resourceid");
				if (fileid!=null)
				resource.setControlAttribute("fileid", fileid);
			}
		}
		return file;
	}

	private void fillBuffer() throws QueueException {
		if (!this.buffer.isEmpty())
			return;
		synchronized (this) {
			if (!this.buffer.isEmpty())
				return;
			int i = 0;
			while (i<getBufferSize() && getFiles().hasNext()) {
				ApiFile file = getFiles().next();
				//if (!moved(file)) {
					i++;
					file = adaptForMove(file);
					this.buffer.addLast(file);
				//}
			}
		}
	}
	
	private IResultSet<ApiFile> getFiles() {
		if (files==null) {
			String domain = (String)getParameter("source-domain");
			files =  getSourceServer().select(getSourceCriteria(), domain, 256);
		}
		return files;
	}
	
	private KbeeApiService getSourceServer() {
		if (sourceserver==null) {
			String sourceurl = (String)getParameter("source-url");
			String sourceuser = (String)getParameter("source-user");
			String sourcepassword = (String)getParameter("source-password");
			sourceserver = new KbeeApiService(sourceurl, sourceuser, sourcepassword);
		}
		return sourceserver;
	}
	
	private String getSourceCriteria() {
		if (criteria==null) {
			criteria = (String)getParameter("source-criteria");
		}
		return criteria;
	}
	
	private Transaction beginTransaction()  {
		return ServiceLocator.getService(TransactionService.class).beginTransaction(false);
	}
}
