package com.novamens.wicket.markup.html.actions;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public abstract class SubmitenerMenuItemPanel<T> extends AbstractMenuItemPanelV5<T>  {
	private static final long serialVersionUID = 1L;
	private Form<?> form;

	public SubmitenerMenuItemPanel(String id, Form<?> form) {
		super(id);
		
		this.form = form;
		
		AbstractLink link = getNewLink("item-link");
		
		link.add(new Label("item-label", new Model<String>() {
			public String getObject() {
				return getLabel();
			}
		}));
		
		add(link);
	}

	@Override
	public void onClick() throws Exception {
	}
	
	public String getIndicatingLabel() {
		return null;
	}
	
	@Override
	public String getLabel() {
		return null;
	}

	@Override
	public String getCssClass() {
		return null;
	}
	
	@Override
	public String getBeforeClick() {
		return null;
	}
	
	public void onSubmit(AjaxRequestTarget target, Form<?> form) {
		
	}

	protected AbstractLink getNewLink(String id) {
		AjaxSubmitLink link = new AjaxSubmitLink(id, form) {
			@SuppressWarnings("unused")
			public void onSubmit(AjaxRequestTarget target, Form<?> form) {
				SubmitenerMenuItemPanel.this.onSubmit(target, form);
			}
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				AjaxCallListener myAjaxCallListener = new AjaxCallListener() {
					@Override 
						public CharSequence getBeforeHandler(Component component) { 
							return "if (typeof(tinyMCE) != \"undefined\") tinyMCE.triggerSave(true,true)";
						}
				};
				attributes.getAjaxCallListeners().add(myAjaxCallListener);
			}
		};
		return link;
	}
}
