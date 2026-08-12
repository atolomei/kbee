package com.novamens.solr.indexer.util;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.novamens.content.base.ResourceContainer;
import com.novamens.content.resource.KBFile;
import com.novamens.indexer.java.Extractor;
import com.novamens.indexer.java.FileIndexerService;
import com.novamens.indexer.query.TextQuery;
import com.novamens.indexer.service.Document;
import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.IndexProxy;
import com.novamens.indexer.service.IndexerException;
import com.novamens.indexer.service.JavaIndex;
import com.novamens.solr.indexer.service.SolrIndex;
import com.novamens.util.JXPath;

import kbee.util.PropertiesFactory;
import kbee.util.logging.Logger;

public class SolrFileTextExtractor implements Extractor {

	private static Logger logger = Logger.getLogger(SolrFileTextExtractor.class.getName());

	private JXPath jpath;
	private int maxFiles = -1;
	private long maxSize = -1;

	public SolrFileTextExtractor() {
		try {
			String value = (String)PropertiesFactory.getInstance("kbee").getProperties().getOrDefault("solr.index.maxSize", "-1");
			if (!"-1".equals(value)) {
				maxSize = Long.valueOf(value);
			}
			value = (String)PropertiesFactory.getInstance("kbee").getProperties().getOrDefault("solr.index.maxFiles", "-1");
			if (!"-1".equals(value)) {
				maxFiles = Integer.valueOf(value);
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	public SolrFileTextExtractor(String path) {
		setPath(path);
	}
	
	public void setPath(String path) {
		this.jpath = new JXPath(path);
	}
	
	public JXPath getPath() {
		return this.jpath;
	}
	
	public Object extract(Object object) throws IndexerException {
		
		StringBuffer text = null;
		
		try {
			text = new StringBuffer();
			
			Object value = jpath.evaluateAll(object);
			
			if (value!=null &&  (value instanceof List)) {
				List<?> values = (List<?>)value;
				int files = 0;
				for(Object valueobject : values) {
					value = valueobject;
					if(value instanceof KBFile) {
						files++;
						KBFile kbFile = (KBFile) value;
						if ((maxFiles<0||files<maxFiles) && indexable(object, kbFile)) {
							text.append(extractText(kbFile));
						}
					}
				}
			}
 			return text.toString();
		}
		catch (InvocationTargetException e) {
			logger.error(e);
			throw new IndexerException(e);
		} 
		catch (IllegalAccessException e) {
			logger.error(e);
			throw new IndexerException(e);
		} 
		catch (NoSuchMethodError e) {
			logger.error(e);
			return null;
		}
		catch (Exception e) {
			logger.error(e);
			throw e;
		}
	}
	
	protected boolean indexable (Object object, KBFile file) {
		if (!(object instanceof ResourceContainer)) 
			return true;
		//if (!((ResourceContainer)object).isPublic(file))
		//	return false;
		//if (file.isInPortalVersion())
		//	return  false;
		if (maxSize>0 && file.getSize()>maxSize) {
			return  false;
		}
		return true;
	}
	
	private String extractText(KBFile file) throws IndexerException {
		
		String text = null;
		SolrDocument solrdocument = null;
		
		try {
			solrdocument = findFile(file);
			if (solrdocument == null) {
				Document indexerdocument = index(file);
				if (indexerdocument!=null && indexerdocument.getFieldValue("filetext")!=null) {
					text = indexerdocument.getFieldValue("filetext").toString();
				}
			}
			else {
				try {
					text = solrdocument.get("text")!=null?solrdocument.get("text").toString():null;
				} 
				catch (Exception e) {
					logger.error(e);
					throw new IndexerException(e);
				}
			}
			return text;
		} 
		catch (Exception e) {
			StringBuilder str = new StringBuilder();
			str.append(solrdocument!=null ? ("solrdocument->"+solrdocument.toString()) : "");
			str.append(text!=null ? ("text->"+text) : "");
			logger.error(e, str.toString());
			throw (e);
		}
	}
	
	private SolrDocument findFile(KBFile file)  throws IndexerException  {
		SolrDocument document = null;
		String type = file.getClass().getSimpleName().toLowerCase();
		String satement = "id:"+type+"#" + String.valueOf(file.getId()) + " AND type:kbfile";
		TextQuery query = new TextQuery(satement);
		query.setFaceted(false);
		query.setCache(false);
		QueryResponse response = (QueryResponse)getIndex(file).execute(query);
		SolrDocumentList results = response.getResults();
		if (results.size()==1) 
			document = results.get(0);
		return document;
	}
	
	private Document index(KBFile file) throws IndexerException {
		Index index = getIndex(file);
		if (index instanceof IndexProxy) 
			index = ((IndexProxy)index).getIndex();
		if (index instanceof SolrIndex) {
			Document document = ((SolrIndex)index).buildDocument(file, false);
			((SolrIndex)index).indexDocument(document);
			return document;
		}
		return null;
	}
	
	private JavaIndex getIndex(KBFile file) {
		return (JavaIndex)file.getDomain().getService(FileIndexerService.class).getIndex();
	}
}
