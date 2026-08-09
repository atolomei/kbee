/**
 * 
 */
package com.novamens.kbee.wicket.markup.html.ajax;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.Response;


@Deprecated
@SuppressWarnings("serial")
public abstract class WorkingIndicatorAjaxLink extends AjaxLink<Void> implements IAjaxIndicatorAware {
	private static final long serialVersionUID = 1L;

	String dl=null;
	
	private AjaxIndicatorAppender indicatorAppender = new AjaxIndicatorAppender() {
		@Override
		public void afterRender(final Component component)	{
			
			final Response r = component.getResponse();
			r.write("<span class=\"");
			r.write(getSpanClass());
			r.write("\" ");
			r.write("id=\"");
			r.write(getMarkupId());
			r.write("\">");
			r.write(getIndicatorLabel()+"</span>");
		}
	};
	
	public WorkingIndicatorAjaxLink(String id) {
		super(id);
		add(indicatorAppender);
	}
 
	
	public String getAjaxIndicatorMarkupId() {
		return indicatorAppender.getMarkupId();
	}
	
	protected String getIndicatorLabel() {
		if (dl==null)																	
			dl=new StringResourceModel("working", WorkingIndicatorAjaxLink.this, null).getString();
		return dl;
	}
}
