package com.novamens.kbee.content.relationshipsbycriteria;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.relationshipsbycriteria.RelationshipByCriteriaTemplate;
import com.novamens.content.relationshipsbycriteria.RelationshipsByCriteriaService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.query.ValueFilter;
import com.novamens.indexer.service.Index;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;

public class KbeeRelationshipsByCriteriaService implements RelationshipsByCriteriaService {

	private Content content  = null;
	
	public KbeeRelationshipsByCriteriaService() {
	}

	public KbeeRelationshipsByCriteriaService(Content content) {
		 this.content = content;
	}

	@Override
	public List<Content> getRelated() {
		
		List<Content> related = new ArrayList<Content>();
		
		if (getContent()==null)
			return related;
		
		if (!getContent().getContentTemplate().acceptsRelationshipsByCriteria())
			return related;
		
		for (RelationshipByCriteriaTemplate relationship : getContentDao().getRelationshipsByCriteria(getContent().getDomain())) {
			if (relationship.includes(getContent())) {
				related.addAll(getRelated(relationship));
			}
		}
		
		return related;
	}
	
	public Map<RelationshipByCriteriaTemplate, List<Content>> getRelatedTemplates() {
		Map<RelationshipByCriteriaTemplate, List<Content>> map = new HashMap<RelationshipByCriteriaTemplate, List<Content>>();
		
		if (getContent()==null)
			return map;
		
		if (!getContent().getContentTemplate().acceptsRelationshipsByCriteria())
			return map;
		
		for (RelationshipByCriteriaTemplate relationship : getContentDao().getRelationshipsByCriteria(getContent().getDomain())) {
			if (relationship.includes(getContent())) {
				map.put(relationship, getRelated(relationship));
			}
		}
		
		return map;
	}
	
	public Content getContent() {
		return content;
	}
	
	private List<Content> getRelated(RelationshipByCriteriaTemplate relation) {
		List<Content> related = new ArrayList<>();
		SolrParametersQuery query = new SolrParametersQuery(getQueryIndex());
		ValueFilter filter = new ValueFilter("template", String.valueOf(((KbeeRelationshipByCriteriaTemplate)relation).getSourceTemplate().getId()));
		query.setParameter("template", filter);
		ResultSet sources = query.execute();
		while (sources.hasNext()) {
			Content source = (Content)((SearchResult)sources.next()).getObject();
			if (relation.related(source, getContent())) {
				related.add(source);
			}
		}
		return related;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	private Index getQueryIndex() {
		return getContent().getDomain().getService(JavaIndexerService.class).getIndex();
	}
}