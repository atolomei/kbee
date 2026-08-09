package com.novamens.kbee.wicket.markup.html.console.browser;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxLink;

import com.novamens.kbee.wicket.markup.html.console.data.DataViewPanel;

import kbee.web.console.BaseBrowser;

@SuppressWarnings("serial")
public class NextButton extends ToolbarItem {
	private static final long serialVersionUID = 1L;

	public NextButton(BaseBrowser<?> browser, Align align) {
		super(browser, align);
	}
	
	public boolean isEnabled() {
		DataViewPanel<?> dataview = getBrowser().getPanel(DataViewPanel.class);
		long currentPage = dataview.getCurrentPage();
		long size = dataview.getSearcher().size();
		long ps = dataview.getPageSize();
		long pages = ps>0 ? size / ps : 0;
		if ((size % ps) > 0) pages++;
		return currentPage<pages-1;
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("link")==null) {
			addLink();
		}
	}
	
	protected void addLink() {
		
		add(new AjaxLink<Void>("link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				DataViewPanel<?> dataview = getBrowser().getPanel(DataViewPanel.class);
				dataview.setCurrentPage(dataview.getCurrentPage()+1);
				target.add(getBrowser());
			}
			@Override
			public boolean isEnabled() {
				return NextButton.this.isEnabled();
			}
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				IAjaxCallListener listener = new IAjaxCallListener() {
					@Override
					public CharSequence getSuccessHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getPrecondition(Component component) {
						return null;
					}
					@Override
					public CharSequence getFailureHandler(Component component) {
						return null;
					}
					
					@Override
					public CharSequence getCompleteHandler(Component component) {
						String s = null, s1=null;
						String id = component.getMarkupId();
						
						if (NextButton.this.getBrowser().getPanel(DataViewPanel.class).isLastPage()) 
							s1 = "document.getElementById('"+id+"').innerHTML = '"+"<em><i class=\"far fa-chevron-right\"/></em>"+"';";
						else
						
						s1 = "document.getElementById('"+id+"').innerHTML = '"+"<i class=\"far fa-chevron-right\"/>"+"';";
						 s ="setTimeout(function () {"+s1+"}, 350);";
						return s;
					}
					@Override
					public CharSequence getBeforeSendHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getBeforeHandler(Component component) {
						String s = null;
						String id = component.getMarkupId();
						s = "document.getElementById('"+id+"').innerHTML = '<i class=\"far fa-sync fa-spin fa-fw\"></i>'";
						return s;
					}
					@Override
					public CharSequence getAfterHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getDoneHandler(Component component) {
						return null;
					}
					@Override
					public CharSequence getInitHandler(Component component) {
						return null;
					}
				};
				attributes.getAjaxCallListeners().add(listener);
			}

			
			
			
			
			
			
			
			
			
			
			
			
			
			
		});
	}
	
	
	
	
	
	
	
	
	
	

}
