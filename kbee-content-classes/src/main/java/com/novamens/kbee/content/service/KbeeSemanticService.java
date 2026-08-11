package com.novamens.kbee.content.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classification;
import com.novamens.content.model.ContentId;
import com.novamens.content.model.DataSetType;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.questionanswer.Question;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.query.DomainQuery2;
import com.novamens.service.ServiceLocator;

public class KbeeSemanticService implements SemanticService {

	static Logger logger = LogManager.getLogger(KbeeSemanticService.class.getName());
	static final private int MAX = 30;
	
	private Map<Long, Classification> map = null;
	private Content content = null;
	private List<Content> semantic_related = null;
	
	public KbeeSemanticService() {
	}
	
	public KbeeSemanticService(Content content) {
		 this.content = content;
	}
	
	private Index getQueryIndex() {
		return content.getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	/**
	 * Query con los Classifiers
	 * Devuelve 3
	 */
	
	@Transactional
	public List<Content> generateSemanticRelated() {
		return calculateSemanticRelated();
	}
	
	@Override
	@Transactional
	public void removeSemanticRelated() {
		content.getService(PropertyService.class).removeProperty("semanticrelated");
	}
	
	@Override
	public List<Content> generateSemanticRelatedNoTrx() {
		return calculateSemanticRelated();
	}
	
	public List<Content> getSemanticRelated() {
	
		if (semantic_related!=null)
			return  semantic_related;
			
		String  semstr = (String) content.getService(PropertyService.class).getProperty("semanticrelated");
		if (semstr!=null) {
			semantic_related = new ArrayList<Content>();
			String arr[] = semstr.split("#");
			for (String s:arr) {
				try {
					ContentId conid = new ContentId(s);
					Content content = getContentDao().findContentById(conid);
					if (content!=null)
						semantic_related.add(content);
				} catch(Exception e) {
					logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
				}
			}
			return semantic_related;
		}
		return null;
	}
	
	private void saveSemanticRelated(String srel) {
		logger.debug("Saving " + content.getTitle() + ": " + srel.toString());
		content.getService(PropertyService.class).setProperty("semanticrelated", srel.toString());
	}
		
	/**
	 *
	 * @return
	 * 
	 */
	private List<Content> calculateSemanticRelated() {
	
		Domain domain = content.getDomain();
		
		SQuery sq = new SQuery(domain, getQueryIndex());
		
		ResultSet rs = null;
		List<Content> list = new ArrayList<Content>();
		StringBuilder str = new StringBuilder();
		
		try {
			rs= sq.execute();
			if (rs==null) 
				return null;
			// 	cargo list con MAX resultados de la Query
			//
	 		
			int counter = 0;
			Long cid = (Long) content.getId();
			while(rs.hasNext() && counter<MAX) {
				Content nextco = ((Content) rs.next().getObject());
				Long yid = (Long) nextco.getId();
				if (cid!=yid)
						list.add(nextco);
				counter++;
			} 		
		} catch (RuntimeException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
		} finally {
				if (rs!=null) 
					rs.close();
		}

		// Map es un map con los tags de content
		//
		if (map==null) {
				map = new HashMap<Long, Classification>();
				for (Classification clasi: content.getClassification()) {
					if (clasi.getClassifier().isSemantic()) {
						map.put( (Long) clasi.getDataSetMember().getId(), clasi);
					}
				}
		}

		// Ordeno list por distancia semántica (los más proximos primero)
		//
		Collections.sort(list, new Comparator<Content>() {
			@Override
			public int compare(Content c1, Content c2) {
				try {
				if (content.getSemanticDistance(c1)<content.getSemanticDistance(c2))
						return 1;
				return -1;
				} catch (Exception e) {
					return 0;
				}
			}
		});
		
		// genero la lista con los 4 más cercanos semánticamente
		//
		int max = list.size()>4?4:list.size();
		List<Content> res = list.subList(0, max);

		int n=0;
		for (Content content: res) {
			if (n>0)
				str.append("#");
			ContentId conid=new ContentId(content);
			str.append(conid.toString());
			n++;
		}
		saveSemanticRelated(str.toString());
		return res;
	}
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	/** -----------------------------------------------------------------------
  	 *
	 * Question -> Question
	 * Document-> todo menos question
	 * Noticia -> todo menos question
	 *
	 */
	private class SQuery extends DomainQuery2 {
		private static final long serialVersionUID = 4013501196399167505L;
		public SQuery(Domain domain, Index index) {
			super(index);
			
			StringBuilder str = new StringBuilder();
			boolean hastags = false;
			
			for (Classification clasi: content.getClassification()) {
				
				if (!(clasi.getClassifier().getDataSetType()==DataSetType.DATE ||clasi.getClassifier().getDataSetType()==DataSetType.BOOLEAN)) {
					str.append(clasi.getStrValue()+" ");
					if (clasi.getClassifier().isSemantic())  
						hastags =true;
				}
			}
			
			if (str.length()==0 || !hastags) {
				getParameters().put("text", content.getTitle() +" "+str.toString());
			}
			else
				getParameters().put("text", content.getTitle()+" "+str.toString());

			if (content instanceof Question)
				getParameters().put("type", "[question]");
			else
				getParameters().put("type", "[idoc, text]");
				
			getParameters().put("sort", "relevance");
			getParameters().put("head", "true");
			getParameters().put("state", String.valueOf(ObjectState.ENABLED.getId()));
			getParameters().put("domain", String.valueOf(domain.getId()));
			getParameters().put("ascending", "false");
		}
	}

	@Override
	public boolean hasSemanticRelatedCalculated() {
		return getSemanticRelated()!=null;
	}
	
	
}
