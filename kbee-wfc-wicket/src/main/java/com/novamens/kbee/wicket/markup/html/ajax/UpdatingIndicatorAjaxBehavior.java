package com.novamens.kbee.wicket.markup.html.ajax;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.Response;

@SuppressWarnings("serial")
public abstract class UpdatingIndicatorAjaxBehavior extends AjaxFormComponentUpdatingBehavior implements IAjaxIndicatorAware {
	private static final long serialVersionUID = 1L;

	private String dl;
	
	private AjaxIndicatorAppender indicatorAppender = new AjaxIndicatorAppender() {
		@Override
		public void afterRender(final Component component)	{
			final Response r = component.getResponse();
			r.write("<span style=\"display:none;\" class=\"");
			r.write(getSpanClass());
			r.write("\" ");
			r.write("id=\"");
			r.write(getMarkupId());
			r.write("\">");
			r.write(getIndicatorLabel()+"</span>");
		}
	};
	
	public UpdatingIndicatorAjaxBehavior(String event) {
		super(event);
	}
	
	public String getAjaxIndicatorMarkupId() {
		return indicatorAppender.getMarkupId();
	}
	
	@Override
	protected void onBind() {
		super.onBind();
		getComponent().add(indicatorAppender);
	}
	
	protected String getIndicatorLabel() {
		if (dl==null)																						
			dl=new ResourceModel("working").getObject();
		return dl;
	}
}
