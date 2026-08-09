package com.novamens.wicket.markup.html.repeater.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DownloadLink;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;


/**
 * 
 * Used to Navigate Audit Trail
 * 
 * AuditTrailObjectPanel
 * 
 * 
 * 
 *
 */
@SuppressWarnings("serial")
public class NavigationToolbar extends Panel {
	
	static private final File null_file = new File("nullfile");
	
	private  int frompage = 0; 
	private int topage=0;
	
	private boolean is_download = false;
	
	private DataTable<?,?> table;
	
	private String totalstr = null;

	private int max_page = 6;
	
	public NavigationToolbar(String id, DataTable<?,?> table) {
		this(id, table, null, false);
	}
	
	public NavigationToolbar(String id, DataTable<?,?> table, boolean isdownload) {
		this(id, table, null,  isdownload);
	}
	
	public NavigationToolbar(String id, DataTable<?,?> table, String totalstr) {
		this(id, table, totalstr,  false);
	}
	
	public NavigationToolbar(String id, DataTable<?,?> table, String totalstr, boolean  isdownload) {
		super(id);

		setOutputMarkupId(true);
		this.totalstr = totalstr;
		this.table = table;
		this.is_download=isdownload;
	}
	
	protected File getFile() {
		return null;
	};
	
	protected void addDownloadLink () {
	
		DownloadLink link = new DownloadLink("download",  null_file, getDownloadFilename()) {
			@Override
			public boolean isVisible() {
				return isDownload();
			}
			@Override
			public boolean isEnabled() {
				return isDownload();
			}
			@Override
			public void onClick() {
				File file = getFile();
				if (file!=null) {
					super.setModelObject(file);
					super.onClick();
				}
			}
		};

		link.setDeleteAfterDownload(true);
		add(link);
	}
	
	protected String getDownloadFilename() {
		return null;
	}

	public void onBeforeRender() {
		super.onBeforeRender();
		
		if (get("total-container")!=null)
			return;
		
		addDownloadLink();
		
		WebMarkupContainer cont = new WebMarkupContainer("total-container");
		cont.setVisible(this.totalstr!=null);
		
		add(cont);
		cont.add((new Label("total", this.totalstr)).setVisible(this.totalstr!=null));
		
		this.topage = (int) this.table.getPageCount();

		if (this.topage > this.max_page) 
			this.topage = this.max_page;

		add(new AjaxLink<Void>("prev-page") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				int current = (int)NavigationToolbar.this.table.getCurrentPage();
				current--;
				if (current-frompage<2 && frompage>0)
					frompage--;
				NavigationToolbar.this.table.setCurrentPage(current);
				target.add(NavigationToolbar.this.table);
				target.add(NavigationToolbar.this);
			}
			@Override
			public boolean isEnabled() {
				return NavigationToolbar.this.table.getCurrentPage()>0;
			}
		});
		
		
		add(new ListView<String>("page", new PropertyModel<List<String>>(this, "pages")) {
			
			public void populateItem(final ListItem<String> item) {
				AjaxLink<?> pagelink = new AjaxLink<String>("link") {
					public void onClick(AjaxRequestTarget target) {
						NavigationToolbar.this.table.setCurrentPage(Integer.valueOf(item.getModelObject())-1);
						target.add(NavigationToolbar.this.table);
						target.add(NavigationToolbar.this);
					}
				};
				pagelink.add(new Label("number", item.getModelObject()));
				item.add(pagelink);
				item.add(new AttributeModifier("class", new Model<String>() {
					public String getObject() {
						if (item.getModelObject().equals(String.valueOf(NavigationToolbar.this.table.getCurrentPage()+1))) {
							return "page-item active";
						}
						else 
							return "page-item";
					}
				}));
			}
		});
		
		add(new AjaxLink<Void>("next-page") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				int current = (int)NavigationToolbar.this.table.getCurrentPage();
				current++;
				if (current-frompage>2 && frompage<NavigationToolbar.this.table.getPageCount()-5)
					frompage++;
				NavigationToolbar.this.table.setCurrentPage(current);
				target.add(NavigationToolbar.this.table);
				target.add(NavigationToolbar.this);
			}
			@Override
			public boolean isEnabled() {
				return topage<NavigationToolbar.this.table.getPageCount();
			}
		});
	}
	
	public void setDownload(boolean b) {
		this.is_download=b;
	}
	
	public boolean isDownload() {
		return this.is_download;
	}
	
	public List<String> getPages() {
		List<String> pages = new ArrayList<String>();
		for (int page = frompage; page<table.getPageCount() && page-frompage<5; page++) {
			pages.add(String.valueOf(page+1));
			topage = page;
		}
		return pages;
	}
	
	@Override
	public boolean isVisible() {
		return this.totalstr!=null || table.getPageCount() > 1 || this.isDownload(); 
	}

}
