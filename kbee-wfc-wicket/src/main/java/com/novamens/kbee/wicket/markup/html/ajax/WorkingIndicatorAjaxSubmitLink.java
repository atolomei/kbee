/**
 * 
 */
package com.novamens.kbee.wicket.markup.html.ajax;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;

/**
 * 
 * 
 *
 */
public abstract class WorkingIndicatorAjaxSubmitLink extends AjaxSubmitLink implements IAjaxIndicatorAware  {

	private static final long serialVersionUID = 1L;

	private String label;
	
	
	public WorkingIndicatorAjaxSubmitLink(String id, String label, final Form<?> form) {
		super(id, form);
		this.label=label;
	}
	
	protected String getLabel() {
		return label;
	}
	
	protected String getWorkingLabel() {
		return getLabel();
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
				if (getWorkingLabel()!=null) {
				String id = component.getMarkupId();
					s1 = "document.getElementById('"+id+"').innerHTML = '"+getLabel()+"';";
					s ="setTimeout(function () {"+s1+"}, 520);";
				}
				return s;
			}
			@Override
			public CharSequence getBeforeSendHandler(Component component) {
				return null;
			}
			@Override
			public CharSequence getBeforeHandler(Component component) {
				String s = null;
				if (getWorkingLabel()!=null) {
					s = "document.getElementById('"+component.getMarkupId()+"').innerHTML = '<i class=\"fa fa-sync fa-spin\"></i> "+getWorkingLabel()+"';";
				}
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

	public String getBeforeHandler() {
		return null;
	}
}
