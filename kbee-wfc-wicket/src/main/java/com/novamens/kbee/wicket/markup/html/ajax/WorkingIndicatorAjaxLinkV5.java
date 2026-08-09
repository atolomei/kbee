/**
 * 
 */
package com.novamens.kbee.wicket.markup.html.ajax;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;

import com.novamens.wicket.markup.html.form.Form;


/**
 * 
 * 
 * spinning icon after submit
 *
 * @param <T>
 */
public abstract class WorkingIndicatorAjaxLinkV5<T> extends AjaxLink<T>  {

	private static final long serialVersionUID = 1L;

	private String label = "";
	String previousLabel = null;
	
	
	public WorkingIndicatorAjaxLinkV5(String id) {
		super(id);
	}
	
	/**
	 * 
	 * @param id
	 * @param model
	 */
	public WorkingIndicatorAjaxLinkV5(String id, final IModel<T> model) {
		super(id, model);
	}
	
	public WorkingIndicatorAjaxLinkV5(String id, final IModel<T> model, String label) {
		super(id, model);
		this.label=label;
	}
	
	public WorkingIndicatorAjaxLinkV5(String id, String label) {
		super(id);
		this.label=label;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	protected String getLabel() {
		return this.label;
	}
	
	protected String getWorkingLabel() {
		return "";
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
					s1 = "document.getElementById('"+component.getMarkupId()+"').innerHTML = '"+getLabel()+"';";
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
				if (getWorkingLabel()!=null)
					s = "document.getElementById('"+component.getMarkupId()+"').innerHTML = '<i class=\""+ Form.SPINNING+"\"></i> "+getWorkingLabel() +"';";
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
}
