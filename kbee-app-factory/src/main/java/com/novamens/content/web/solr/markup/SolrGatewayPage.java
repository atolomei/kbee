package com.novamens.content.web.solr.markup;

import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.content.web.nav.markup.GlobalNavigationBar;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.IndexerDocument;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.web.console.Console;
import kbee.web.console.ConsolePage;
import kbee.web.page.ApplicationMenuSection;

@SuppressWarnings("serial")
public class SolrGatewayPage extends ConsolePage<IndexerDocument> {
	private static final long serialVersionUID = 1L;
	
	public SolrGatewayPage() {
		super();
		SolrGatewayQuery query = new SolrGatewayQuery(getQueryIndex());
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("domain","windsor");
		query.setParameters(parameters);
		setQuery(query);
	}
	
	@Override
	public Console<IndexerDocument> newConsole(Query query) {
		return new SolrConsole("solr", query);
	}

	@Override
	public Page getConsolePage(Query query, long index) {
		SolrGatewayPage page = new SolrGatewayPage();
		page.setQuery(query);
		return page;
	}
	
	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.REPORTS;
	}
	
	@Override
	public boolean hasPermissions() {
		boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		boolean role_support = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
		if (role_support || role_admin) 
			return true;
		return false;
	}
	
	protected Index getQueryIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	@Override
	protected Panel newNavigationPanel() {
																				
		return new GlobalNavigationBar<IndexerDocument>("navigation",  "Solr") {
		//return new GlobalNavigationBar<IndexerDocument>("navigation", null, 0, "Solr") {
			@Override
			protected void onSearch(AjaxRequestTarget target, String statement) {
				getQuery().getParameters().put("statement", statement);
				getQuery().getParameters().put("sort", "relevance");
				setResponsePage(getConsolePage(getQuery(), 0));
			}
		};	
	}
}