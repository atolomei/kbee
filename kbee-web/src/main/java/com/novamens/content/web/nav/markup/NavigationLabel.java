package com.novamens.content.web.nav.markup;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;


@SuppressWarnings("serial")
public class NavigationLabel extends Panel {
	private static final long serialVersionUID = 1L;
	
	private DataView<?> view;

	public NavigationLabel(String id, DataView<?> view) {
		super(id);
		this.view = view;
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("total")==null) {
			addLabels();
		}
	}
	
	public DataView<?> getDataView() {
		return view;	
	}
	
	protected void addLabels() {
		add(new Label("from", new Model<String>() {
			public String getObject() {
				DataView<?> dataview = getDataView();
				long currentPage = dataview.getCurrentPage();
				long pageSize = dataview.getItemsPerPage();
				long from = currentPage*pageSize+1;
				return String.valueOf(from);
			}
		}));
		add(new Label("to", new Model<String>() {
			public String getObject() {
				DataView<?> dataview = getDataView();
				long currentPage = dataview.getCurrentPage();
				long pageSize = dataview.getItemsPerPage();
				long from = currentPage*pageSize+1;
				long total = dataview.getDataProvider().size();
				long to = from+pageSize-1;
				if (to>total) to = total;
				return String.valueOf(to);
			}
		}));
		add(new Label("total", new Model<String>() {
			public String getObject() {
				DataView<?> dataview = getDataView();
				long total = dataview.getDataProvider().size();
				return String.valueOf(total);
			}
		}));
	}
}
