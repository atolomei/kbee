package com.novamens.wicket.markup.html.repeater.util;


import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.navigation.paging.IPageableItems;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public class DataViewNavigationToolbar extends Panel {
	private static final long serialVersionUID = 1L;
	
	private IPageableItems table;
	
	public DataViewNavigationToolbar(String id, IPageableItems table) {
		super(id);
		setOutputMarkupId(true);
		this.table = table;
	}

	public long getFrom() {
		long pn = table.getCurrentPage(); 
		long ps = table.getItemsPerPage();
		long from = (pn*ps)+1;
		return from;
	}
	
	public long getTo() {
		long pn = table.getCurrentPage(); 
		long ps = table.getItemsPerPage();
		//long total = table.getDataProvider().size();
		long total = table.getItemCount();
		long to = (pn+1)*ps;
		if (total<to) to = total;
		return to;
	}
	
	public long getTotal() {
		long total = table.getItemCount();
		return total;
	}
	
	public long getPageCount() {
		return table.getPageCount();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		add(getLeftPanel("left-panel"));
		add(new Label("from-label", new Model<String>() {
			public String getObject() {
				return String.valueOf(getFrom());
			}
		}));
		add(new Label("to-label", new Model<String>() {
			public String getObject() {
				return String.valueOf(getTo());
			}
		}));
		add(new Label("total-label", new Model<String>() {
			public String getObject() {
				return String.valueOf(getTotal());
			}
		}));
		add(new AjaxLink<Void>("nextpage-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				long pn = table.getCurrentPage();
				if (pn<table.getPageCount()) {
					table.setCurrentPage(++pn);
				}
				onUpdate(target);
				target.add(DataViewNavigationToolbar.this);
			}
			@Override
			public boolean isEnabled() {
				return table.getCurrentPage()<table.getPageCount();
			}
		});
		add(new AjaxLink<Void>("previouspage-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				long pn = table.getCurrentPage();
				if (pn>0) {
					table.setCurrentPage(--pn);
				}
				onUpdate(target);
				target.add(DataViewNavigationToolbar.this);
			}
			@Override
			public boolean isEnabled() {
				return table.getCurrentPage()>0;
			}
		});
	}
	
	protected Component getLeftPanel(String id) {
		return (new Panel(id) {}).setVisible(false); 
	}
	
	protected void onUpdate(AjaxRequestTarget target) {
		
	}

}
