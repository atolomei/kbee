package com.novamens.content.web.nav.markup;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.data.DataView;

import com.novamens.kbee.wicket.markup.html.ajax.WorkingAjaxLink;

@SuppressWarnings("serial")
public class DataViewNavigatorPanel<T> extends Panel {
	private static final long serialVersionUID = 1L;
	
	private DataView<?> view;

	public DataViewNavigatorPanel(String id, DataView<?> view) {
		super(id);
		this.view = view;
		add(newPreviousLink());
		add(newNextLink());
	}
	
	public DataView<?> getDataView() {
		return view;	
	}
	
	protected AjaxLink<?> newNextLink() {
		return new AjaxLink<Void>("next-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				getDataView().setCurrentPage(getDataView().getCurrentPage()+1);
				target.add(getDataView().getParent());
			}
			@Override
			public boolean isEnabled() {
				return getDataView().getCurrentPage()<getDataView().getPageCount()-1;
			}
		};
	}
	
	protected AjaxLink<?> newPreviousLink() {
		return new WorkingAjaxLink<Void>("previous-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				getDataView().setCurrentPage(getDataView().getCurrentPage()-1);
				target.add(getDataView().getParent());
			}
			@Override
			public boolean isEnabled() {
				return getDataView().getCurrentPage()>0;
			}
		};
	}
	
	protected void onNavigate(T object) {
	}
}
