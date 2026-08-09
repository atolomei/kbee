package com.novamens.kbee.wicket.markup.html.console.panel;

import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.Site;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.modal.Modal;

public class SaveQueryModal extends Modal {
	
	private static final long serialVersionUID = 1L;

	private String console;
	
	private IModel<Site> site_model;
	
	public SaveQueryModal(String id, String console, IModel<Site> mo) {
		super(id);
	
		this.console=console;
		setOutputMarkupId(true);
		setTitle("modal.savequery.title");
		setParameters((console!=null) ? console : "");
		site_model=mo;
		setButtons(Modal.Cancel, new Button("modal.savequery.submit", "btn btn-sm btn-primary", ButtonType.SUBMIT));
		setModalType(Modal.MODAL_CENTER); 
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (site_model!=null)
			site_model.detach();
		
	}
	
	/**
	 * 
	 * @param target
	 * @param title
	 * @param parameters
	 * @param handler
	 */
	
	public void open(AjaxRequestTarget target, String title, boolean isdashboard, Map<String, Object> parameters, Handler handler) {
		open(target, title, null, isdashboard, parameters, handler);
	}

	public void open(AjaxRequestTarget target, String title, String browser, boolean isdashboard, Map<String, Object> parameters, Handler handler) {
		
		setBody(new SaveQueryPanel("body", getConsole(), site_model, isdashboard));	
		((SaveQueryPanel)getBody()).onBeforeRender();
		
		Field<?> titlefield = (Field<?>)get("modal-dialog:body:queryform:title");
		
		if (titlefield!=null)
			titlefield.clearInput();
		
		((SaveQueryPanel)getBody()).setTitle(title);
		((SaveQueryPanel)getBody()).setBrowser(browser);
		((SaveQueryPanel)getBody()).setParameters(parameters);
		
		if (titlefield!=null) {
			target.focusComponent(titlefield.getInput());
			titlefield.getInput().add(new AttributeModifier("value", title));
		}
		
		super.open(target, handler);
	}
	
	protected String getConsole() {
		return this.console;
	}
}
