package com.novamens.kbee.content.command.mt;


import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrDocument;
import org.hibernate.WrongClassException;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.FileIndexerService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.IndexerDocument;
import com.novamens.indexer.service.JavaIndex;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class BatchCleanExecutor extends QueuedBatchProcessor {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(BatchCleanExecutor.class.getName());
	
	private List<SolrDocument> batch;
	private Callback<SolrDocument> callback;
	private Map<String, Object> parameters;
	
	public BatchCleanExecutor(List<SolrDocument> batch, Callback<SolrDocument> callback, Map<String, Object> parameters) {
		IncrementInstances();
		setBatch(batch);
	 	setCallback(callback);
		setParameters(parameters);
	}
	
	public void run() {
		try {
			
			
			com.novamens.hibernate.session.Session.open();
			
	  		ServiceLocator.getService(SecurityService.class).authenticate("root@kbee");

			
			int errors = 0;
			int msize=getBatch().size() / 2 + 1;			
			
			JavaIndex index = getIndex();
			
			for (SolrDocument solrdocument : getBatch()) {
				try {
					String id = solrdocument.getFieldValue("id").toString();
					IndexerDocument document = new IndexerDocument();
					document.addField("id", id);
					document.setId(id);
					Object object = null;
					try {
						object = index.getObjectBuilder().build(document);
					}
					catch (WrongClassException e) {
						logger.error(e);
					}
					if(object==null){
						index.delete(id);
						logger.debug("clean "+ toString(solrdocument));
					}
				}				
				catch(Exception e) {
					errors++;
					logger.error(e);
					if (errors>=msize)
						break;
				}
			}
			
		}	
		catch(Exception e) {
			logger.error(e);
			logger.debug(e);
		}
		finally {
			if (getIndex()!=null) {
				getIndex().commit();
			}

			for (SolrDocument doc : getBatch()) {
				try {
					getCallback().execute(doc);
				}
				catch (Exception callbackexception) {
					logger.error(callbackexception);
					callbackexception.printStackTrace();
				}
			}
			DecrementInstances();
			com.novamens.hibernate.session.Session.close();
		}
	}
	
	public List<SolrDocument> getBatch() {
		return batch;
	}
	
	public void setBatch(List<SolrDocument> batch) {
		this.batch = batch;
	}
	
	public void setCallback(Callback<SolrDocument> callback) {
		this.callback = callback;
	}
	
	public Callback<SolrDocument> getCallback() {
		return callback;
	}
	
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
	
	public Map<String, Object> getParameters() {
		return this.parameters;
	}
	
	private Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
	}
	

	
	

	
//	private Domain getDomainKbee() {
//		return getContentDao().findDomainByName("kbee");
//	}
//	
//	private Indexable reload(Object object) {
//		return (Indexable) getContentDao().reload(object);
//	}

//	private ContentDao getContentDao() {
//		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
	
//	private JavaIndex getIndex(Indexable content) {
//		JavaIndex index = content instanceof KBFile ? (JavaIndex) getDomain().getService(FileIndexerService.class).getIndex() :
//			(JavaIndex) getDomain().getService(JavaIndexerService.class).getIndex();
//		return index;
//	}
	
	private JavaIndex getIndex() {
		JavaIndex index = (JavaIndex) getDomain().getService(JavaIndexerService.class).getIndex();
		return index;
	}
	
	private JavaIndex getFileIndex() {
		JavaIndex index = (JavaIndex) getDomain().getService(FileIndexerService.class).getIndex();
		return index;
	}
	
	private String toString(SolrDocument document) {
		StringBuffer string = new StringBuffer();
		string.append("{");
		int names = 0;
		for (String fieldname : document.getFieldNames()) {
			Collection<?> values = document.getFieldValues(fieldname);
			if (names++>0)
				string.append(",");
			string.append("\""+fieldname+"\":");
			if (values.size()>1) {
				string.append("[");
			}
			int v = 0;
			for (Object value : values) {
				if (v++>0)
					string.append(",");
				String stringvalue = value.toString();
				if (stringvalue.length()>50) {
					stringvalue = stringvalue.substring(0,49)+"....";
				}
				stringvalue = stringvalue.replace("\n", "");
				string.append("\""+stringvalue+"\"");
			}
			if (values.size()>1) {
				string.append("]");
			}
		}
		string.append("}");
		return string.toString();
	}
}
 