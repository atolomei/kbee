package com.novamens.kbee.wicket.markup.html.console.browser;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.query.SavedQuery;
import com.novamens.kbee.wicket.markup.html.console.panel.BookmarksPanel;
import com.novamens.kbee.wicket.markup.html.console.panel.DownloadMenuItemPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;

import kbee.web.console.BaseBrowser;


/**
 * 
 * 
 * BookmarksPanel
 * 
 *
 */
@SuppressWarnings("serial")
public class SavedQueriesButton extends ToolbarItem {
	private static final long serialVersionUID = 1L;

	Panel book_panel = null;
	IModel<Site> sitemodel = null;
	
	public SavedQueriesButton(BaseBrowser<?> browser, Align align) {
		super(browser, align);
	}
	
	public SavedQueriesButton(BaseBrowser<?> browser, IModel<Site> model, Align align) {
		super(browser, align);
		this.sitemodel = model;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		
		AjaxLink<Void> link = new AjaxLink<Void>("link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				SavedQueriesButton.this.onClick(target);
			}
			@Override
			public boolean isEnabled() {
				return SavedQueriesButton.this.isEnabled();
			}
			@Override
			public boolean isVisible() {
				return SavedQueriesButton.this.isVisible();
			}
		};
		
		add(link);
		
		link.add(new AttributeModifier("title", getLabel("label")));

		add(new InvisiblePanel("bookmarks"));
	}
	
	public IModel<Site> getSiteModel() {
		return sitemodel;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	
	public void onClick(AjaxRequestTarget target) {
		
		if (book_panel==null) {
			book_panel = new BookmarksPanel("bookmarks", getBrowser().getQuery(), getConsoleKey(), getSiteModel(), getBrowser().isMyListsEnabled()) {
				@Override
				protected DownloadMenuItemPanel<SavedQuery> getGridExportSavedQueryMenuItem(String id, IModel<SavedQuery> model) {
					return getBrowser(). getGridExportSavedQueryMenuItem(id,  model);
				}
				protected void close(AjaxRequestTarget target) {
					book_panel.setVisible(false);
					target.add(SavedQueriesButton.this);
				}
			};
			addOrReplace(book_panel);
			target.add(this);		
		}
		else {
			book_panel .setVisible(!book_panel.isVisible());
			target.add(this);
		}
	}
	
	protected String getConsoleKey() {
		return getBrowser().getConsoleKey();
	}
	
	protected IModel<String> getLabel(String key) {
		return new StringResourceModel(key, this, null);
	}
}
