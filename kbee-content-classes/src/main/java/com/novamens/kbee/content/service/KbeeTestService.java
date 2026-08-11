package com.novamens.kbee.content.service;

import java.util.Random;

import com.novamens.content.base.Content;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.service.TestService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;
import com.novamens.solr.indexer.query.SolrParametersQuery;
import com.novamens.workflow.Activity;

public class KbeeTestService implements TestService {

	private Domain domain = null;

	public KbeeTestService() {
	}
	
	public KbeeTestService(Domain domain) {
		 this.domain = domain;
	}
	
	public Domain getDomain() {
		return domain;
	}
	
	public Content getSample() {
		return getSample(null);
	}
	
		
	public Content getSample(ContentTemplate template) {
		SolrParametersQuery query = new SolrParametersQuery(getQueryIndex());
		
		query.getParameters().put("type", "[text, idoc]");
		query.getParameters().put("domain", String.valueOf(getDomain().getId()));
		if (template!=null)
		query.getParameters().put("template", template.getId());
		
		ResultSet resultSet = query.execute();
		
		if (!resultSet.hasNext())
			return null;
		
		long size = resultSet.size();
		
		Random rand = new Random(); 
		int upperbound = (int)size;
		int random = rand.nextInt(upperbound);
		
		resultSet.absolute(random);
		
		Content content = (Content)resultSet.next().getObject();
		
		return content;
	}
	
	public Activity getSampleActivity() {
		SolrParametersQuery query = new SolrParametersQuery(getQueryIndex());
		
		query.getParameters().put("type", "[text, idoc]");
		query.getParameters().put("inworkspace", "true");
		query.getParameters().put("domain", String.valueOf(getDomain().getId()));
		
		ResultSet resultSet = query.execute();
		
		if (!resultSet.hasNext())
			return null;
		
		long size = resultSet.size();
		
		Random rand = new Random(); 
		int upperbound = (int)size;
		int random = rand.nextInt(upperbound);
		
		resultSet.absolute(random);
		
		int test=0;
		Content content = null;
		Activity activity = null;
		while (activity==null && test<10 && resultSet.hasNext())  {
			content = (Content)resultSet.next().getObject();
			activity = content.getService(WorkflowService.class).getActivity();
			test++;
		}
		
		return activity;
	}
	
	public Content getSampleMonitor() {
		SolrParametersQuery query = new SolrParametersQuery(getQueryIndex());
		
		query.getParameters().put("type", "[text, idoc]");
		query.getParameters().put("inworkspace", "true");
		query.getParameters().put("domain", String.valueOf(getDomain().getId()));
		
		ResultSet resultSet = query.execute();
		
		if (!resultSet.hasNext())
			return null;
		
		long size = resultSet.size();
		
		Random rand = new Random(); 
		int upperbound = (int)size;
		int random = rand.nextInt(upperbound);
		
		resultSet.absolute(random);
		
		Content content = (Content)resultSet.next().getObject();
		
		return content;
	}
	
	private Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
}
