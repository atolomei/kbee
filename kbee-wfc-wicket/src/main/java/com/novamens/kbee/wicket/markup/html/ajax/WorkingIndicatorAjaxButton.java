package com.novamens.kbee.wicket.markup.html.ajax;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.Response;


/**
 * 
 * muestra una ventana no modal que indica que el servidor esta trabajando
 *
 */
@SuppressWarnings("serial")
public class WorkingIndicatorAjaxButton extends AjaxButton implements IAjaxIndicatorAware {
	private static final long serialVersionUID = 1L;

	String dl;
	
	private AjaxIndicatorAppender indicatorAppender = new AjaxIndicatorAppender() {
		@Override
		public void afterRender(final Component component)	{
			
			final Response r = component.getResponse();
			r.write("<div class=\"working-indicator ");
			r.write(getSpanClass());
			r.write("\"");
			r.write("id=\"");
			r.write(getMarkupId());
			r.write("\">");
			r.write(getIndicatorLabel()+"</span>");
		}
	};
	
	public WorkingIndicatorAjaxButton(String id, Form<?> form) {
		super(id, form);
		add(getIndicator());
	}
	
	public String getAjaxIndicatorMarkupId() {
		return getIndicator().getMarkupId();
	}
	
	protected String getIndicatorLabel() {
		if (dl==null)																	
			dl = new StringResourceModel("working", WorkingIndicatorAjaxButton.this, null).getString();
		return dl;
	}
	
	protected AjaxIndicatorAppender getIndicator() {
		return indicatorAppender;
	}
}
