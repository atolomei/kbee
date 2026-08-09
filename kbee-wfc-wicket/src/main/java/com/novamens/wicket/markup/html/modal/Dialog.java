package com.novamens.wicket.markup.html.modal;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;

@SuppressWarnings("serial")
public class Dialog extends Panel {

	private static final long serialVersionUID = 1L;
	
	private String titlekey, messagekey;
	private Object[] parameters;
	private Handler onCloseHandler;
	private List<Button> buttons = new ArrayList<Button>();

	public static Button Cancel  		= new Button("button.cancel", "btn btn-sm btn-default", true);
	public static Button Delete  		= new Button("button.delete", "btn btn-sm btn-danger");
	public static Button Ok 	 		= new Button("button.ok", "btn btn-sm btn-primary");
	public static Button Ok_Default 	= new Button("button.ok", "btn btn-sm btn-default");
	public static Button OkError 		= new Button("button.close", "btn btn-sm btn-default");
	

	public class RefreshBehavior extends AbstractDefaultAjaxBehavior {
		@Override
		protected void respond(AjaxRequestTarget target) {
			Dialog.this.onClose(target);
		}
		@Override
		public void renderHead(final Component component, final IHeaderResponse response) {
			super.renderHead(component, response);
			StringBuilder script = new StringBuilder();
			script.append("function refreshdialog"+Dialog.this.getMarkupId()+"() {\n");
			script.append(getCallbackScript());
			script.append("}\n");
			response.render(JavaScriptHeaderItem.forScript(script.toString(), "refreshdialog"+Dialog.this.getMarkupId()));
		}
	}
	
	public static class Button implements Serializable {
		private static final long serialVersionUID = 1L;
		private String key;
		private String css;
		private boolean cancel;
		public Button(String key, String css) {
			this(key, css, false);
		}
		public Button(String key, String css, boolean cancel) {
			this.key = key;
			this.css = css;
			this.cancel = cancel;
		}
		public String key() {
			return key;
		}
		public String getCssClass() {
			return css;
		}
		public boolean isCancel() {
			return cancel;
		}
	};
	
	public static class Handler implements Serializable {
		
		public void onClick(AjaxRequestTarget target, Button button) {
			
		}


	};
	
	public Dialog(String id, String titlekey, String messagekey, Button... buttons) {
		super(id);
		
		setOutputMarkupId(true);
		
		this.titlekey = titlekey;
		this.messagekey = messagekey;
		
		addButtons(buttons);
		
		add((new Label("title", getTitle())).setEscapeModelStrings(false));
		add((new Label("message", getMessage())).setEscapeModelStrings(false));
		
		add((new Label("text", getText()) {
			public boolean isVisible() {
				return getText()!=null;
			}
		}).setEscapeModelStrings(false));
		
		WebMarkupContainer footer = new WebMarkupContainer("footer");
		add(footer);
		
		footer.add(new AttributeModifier("class", "modal-footer " +  (this.buttons.size()>1?" center ": " right ")));
				
		footer.add(new ListView<Button>("buttons", this.buttons) {
			public void populateItem(ListItem<Button> item) {
				final Button button = item.getModelObject();
		
				WorkingIndicatorAjaxLinkV5<Void> buttonlink = new WorkingIndicatorAjaxLinkV5<Void>("button") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						Dialog.this.onClick(target, button);
						target.appendJavaScript("$('#"+Dialog.this.getMarkupId()+"').modal('hide')");
					}
					
					@Override
					protected String getWorkingLabel() {
						return "working";
					}
		
				};
				buttonlink.add(new AttributeModifier("class", button.getCssClass()));
				if (button.isCancel())
					buttonlink.add(new AttributeModifier("data-dismiss", "modal"));
				buttonlink.add(new Label("label", new StringResourceModel(button.key(), Dialog.this, null)));
				item.add(buttonlink);
			}
		});
		
		add(new RefreshBehavior());
		
		
		
	}
	
	public void open(AjaxRequestTarget target) {
		target.appendJavaScript("$('#"+getMarkupId()+"').modal('show')");
	}
	
	public void open(AjaxRequestTarget target, Handler handler, String... parameter) {
		this.onCloseHandler = handler;
		this.parameters = parameter;
		target.add(this);
		target.appendJavaScript("$('#"+getMarkupId()+"').modal('show')");
	}
	
	public IModel<String> getTitle() {
		return new StringResourceModel(titlekey, this, null);
	}
	
	public IModel<String> getMessage() {
		return new StringResourceModel(messagekey, this, null) {
			protected Object[] getParameters() {
				if (parameters!=null)
					return parameters;
				else
					return null;
			}
		};
	}
	
	public IModel<String> getText() {
		return null;
	}
	
	public void onClick(AjaxRequestTarget target, Button button) {
		if (onCloseHandler!=null) {
			onCloseHandler.onClick(target, button);
		}
		onCloseHandler = null;
	}
	
	public void onClose(AjaxRequestTarget target) {
		target.add(getParent());
	}
	
	public void setHandler(Handler handler) {
		this.onCloseHandler = handler;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		// response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP));
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	protected void addButtons(Button... buttons) {
		this.buttons.clear();
		for (int b=0; b<buttons.length; b++) {
			this.buttons.add(buttons[b]);
		}
	}
}
