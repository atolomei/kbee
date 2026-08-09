package com.novamens.kbee.wicket.markup.html.console.panel;


import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Query;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.markup.html.repeater.util.MemberModel;
import com.novamens.wicket.markup.html.repeater.util.Searcher;


/**
 * 
 * This is the internal panel of {@link FiltersPanel}
 * that contains pretty much everything:
 * 
 * {@code ParametersPanel}
 * {@code  FacetsPanel}
 * 
 *
 */
@SuppressWarnings("serial")
public class FiltersPanelInternalContainerPanel extends KBPanel {

	private static final long serialVersionUID = 1L;
	
	private Searcher searcher;
	private Query query;
	private String consoleName;
	private String consoleDisplayName;
	
	public FiltersPanelInternalContainerPanel(String id, Query query) {
		super(id);
		setOutputMarkupId(true);
		setQuery(query);
	}
	
	public Query getQuery() {
		return query;
	}
	
	public void setQuery(Query query) {
		this.query = query;
		if (get("facets")!=null) {
			((FacetsPanel)get("facets")).reset();
		}
	}
	
	public Searcher getSearcher() {
		return searcher;
	}

	public void setConsoleDisplayName(String s) {
		this.consoleDisplayName=s;
	}
	
	public String getConsoleDisplayName() {
		return this.consoleDisplayName;
	}
	
	public Map<String, Object> getParameters() {
		return getQuery().getParameters();
	}
	
	public void setParameters(Map<String, Object> parameters) {
		getParametersPanel().setParameters(parameters);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addParameters();
		addFacets();
	}
	
	public void  setConsoleName(String s) {
		this.consoleName=s;
	}
	
	public String getConsoleName() {
		return this.consoleName;
	}

	
	protected boolean isVisible(Facet facet) {
		return true;
	}
	
	protected void onUpdate(AjaxRequestTarget target) {
	}
	
	protected void onClose(AjaxRequestTarget target) {
	}
	
	
	/**
	 * 
	 * ParametersPanel
	 * 
	 * @return
	 */
	protected ParametersPanel getParametersPanel() {
		if (get("parameters")==null) 
			this.onInitialize();
		return (ParametersPanel)get("parameters");
	}
	
	public void recreateParametersPanel() {
		addParameters();
	}
	
	protected void addParameters() {
		addOrReplace(new ParametersPanel(getParameters(), getConsoleDisplayName()) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				getQuery().setParameters(getParameters());
				FiltersPanelInternalContainerPanel.this.onUpdate(target);
				//((FacetsPanel)AppliedFiltersPanel.this.get("facets")).refresh(target);
			};
			@Override
			protected void saveQuery(AjaxRequestTarget target, String title, Map<String, Object> parameters2) {
				FiltersPanelInternalContainerPanel.this.saveQuery(target, title, parameters2);
			}
			
			
			@Override
			protected void saveDashboardQuery(AjaxRequestTarget target, String title, Map<String, Object> parameters2) {
				FiltersPanelInternalContainerPanel.this.saveDashboardQuery(target, title, parameters2);
			}
			
		});
	}
	
	protected void saveQuery(AjaxRequestTarget target, String title, Map<String, Object> parameters2) {
	}
	
	protected void saveDashboardQuery(AjaxRequestTarget target, String title, Map<String, Object> parameters2) {
	}

	protected void addFacets() {
		addOrReplace(new FacetsPanel(new Searcher(getQuery())) {
			@Override
			public Searcher getSearcher() {
				return FiltersPanelInternalContainerPanel.this.getSearcher();
			}
			@Override
			public void onMemberSelect(AjaxRequestTarget target, MemberModel member) {
				if (member!=null) {
					getParametersPanel().setMember(member);
					getQuery().setParameters(getParametersPanel().getParameters());
					FiltersPanelInternalContainerPanel.this.onUpdate(target);
				}
			}
			@Override
			public void onMemberRemove(AjaxRequestTarget target, MemberModel member) {
				if (member!=null) {
					getParametersPanel().removeMember(member);
					getQuery().setParameters(getParametersPanel().getParameters());
					FiltersPanelInternalContainerPanel.this.onUpdate(target);
				}
			}
			@Override
			public void onMembersSelect(AjaxRequestTarget target, List<MemberModel> members) {
				if (members!=null) {
					getParametersPanel().setOrMembers(members);
					getQuery().setParameters(getParametersPanel().getParameters());
					FiltersPanelInternalContainerPanel.this.onUpdate(target);
				}
			}
			@Override
			public boolean isVisible(Facet facet) {
				return FiltersPanelInternalContainerPanel.this.isVisible(facet);
			}
		});
	}
}
