package com.novamens.kbee.wicket.markup.html.console.panel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;

import kbee.web.console.BaseBrowser;

public class SavedQueriesSidePanel extends ConsoleSidePanel {
				
	private static final long serialVersionUID = 1L;

	private BaseBrowser<?> browser;
	
	@SuppressWarnings("unused")
	static private Logger logger = LogManager.getLogger(SavedQueriesSidePanel.class.getName());
	
	public SavedQueriesSidePanel(String id, BaseBrowser<?> browser) {
		super(id);
		this.browser=browser;
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
		if (get("saved-queries")==null) {
			add(new SavedQueriesPanel("saved-queries", this.browser.getConsoleKey(), this.browser.getQuery()));
			addCloseLink();
			addFiltersLink();
		}
	}
	
	@Override
	public void onClose(AjaxRequestTarget target) {

	}
	
	protected void addFiltersLink() {
	
		WorkingIndicatorAjaxLinkV5<Void> link = new WorkingIndicatorAjaxLinkV5<Void>("filters") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				onFilters(target);
			}
		};
		add(link);
	}
	
	protected void onFilters(AjaxRequestTarget target) {
	}


	protected void addCloseLink() {
		 WorkingIndicatorAjaxLinkV5<Void> close= new WorkingIndicatorAjaxLinkV5<Void>("close") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
		 };
		 add(close);
	}
}
