package com.novamens.kbee.wicket.markup.html.ajax;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.Response;

/**
 * Este link muestra una ventana no modal que indica que el servidor esta trabajando
 */
@SuppressWarnings("serial")
public abstract class WorkingAjaxLink<T> extends AjaxLink<T> implements IAjaxIndicatorAware {
	private static final long serialVersionUID = 1L;

	private String indicatingLabel = new ResourceModel("working").getObject();
	
	private AjaxIndicatorAppender indicatorAppender = new AjaxIndicatorAppender() {
		@Override
		public void afterRender(final Component component)	{
			Response r = component.getResponse();
			r.write("<div class=\"");
			r.write(getIndicatorClass());
			r.write("\"");
			r.write("id=\"");
			r.write(getMarkupId());
			r.write("\">");
			r.write(getIndicatingLabel()+"</div>");
		}
	};
	
	public WorkingAjaxLink(String id, final IModel<T> model) {
		super(id, model);
		add(indicatorAppender);
		this.setOutputMarkupId(true);
	}
	
	public WorkingAjaxLink(String id, final IModel<T> model, String indicatinglabel) {
		super(id, model);
		add(indicatorAppender);
		this.indicatingLabel=indicatinglabel;
		this.setOutputMarkupId(true);
	}
	
	public WorkingAjaxLink(String id) {
		super(id);
		add(indicatorAppender);
		this.setOutputMarkupId(true);
	}

	public WorkingAjaxLink(String id, String indicatinglabel) {
		super(id);
		add(indicatorAppender);
		this.indicatingLabel=indicatinglabel;
	}
	
	public String getAjaxIndicatorMarkupId() {
		return indicatorAppender.getMarkupId();
	}

	public void setIndicatingLabel(String str) {
		indicatingLabel=str;
	}
	
	public String getIndicatingLabel() {
		return indicatingLabel;
	}
	
	public String getIndicatorClass() {
		return "working-indicator";
	}
	
	public String getBeforeClick() {
		return null;
	}
	
	public MarkupContainer setDefaultModel(IModel<?> model) {
		return super.setDefaultModel(model);
	}
	
	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		IAjaxCallListener listener = new IAjaxCallListener() {
			public CharSequence getSuccessHandler(Component component) {
				return null;
			}
			public CharSequence getPrecondition(Component component) {
				return null;
			}
			public CharSequence getFailureHandler(Component component) {
				return null;
			}
			public CharSequence getCompleteHandler(Component component) {
				return null;
			}
			public CharSequence getBeforeSendHandler(Component component) {
				return null;
			}
			public CharSequence getBeforeHandler(Component component) {
				return getBeforeClick();
			}
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
}
 