package com.novamens.wicket.markup.html.modal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;

@SuppressWarnings("serial")
public class Dialog2 extends Panel {
	private static final long serialVersionUID = 1L;
	
	private static final ResourceReference CSS_KBEE_BOOTSTRAP			 = new CssResourceReference(AbstractKbeeWebPage.class, "kbeebootstrap.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_0_360      = new CssResourceReference(AbstractKbeeWebPage.class, "kb-0-360.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_361_800    = new CssResourceReference(AbstractKbeeWebPage.class, "kb-361-800.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_801_1200   = new CssResourceReference(AbstractKbeeWebPage.class, "kb-801-1200.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1201_1600  = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1201-1600.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1601  	 = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1601.css");


	private String titlekey, messagekey;
	private Object[] parameters;
	private Handler onCloseHandler;
	private List<Button> buttons = new ArrayList<Button>();

	public static Button Cancel = new Button("button.cancel", "btn btn-sm btn-default", true);
	public static Button Delete = new Button("button.delete", "btn btn-sm btn-danger");
	public static Button Ok 	= new Button("button.ok",     "btn btn-sm btn-primary");
	public static Button Close 	= new Button("button.close",  "btn btn-sm btn-default");
							
	public class RefreshBehavior extends AbstractDefaultAjaxBehavior {
		@Override
		protected void respond(AjaxRequestTarget target) {
			Dialog2.this.onClose(target);
		}
		@Override
		public void renderHead(final Component component, final IHeaderResponse response) {
			super.renderHead(component, response);
			StringBuilder script = new StringBuilder();
			script.append("function refreshdialog"+Dialog2.this.getMarkupId()+"() {\n");
			script.append(getCallbackScript());
			script.append("}\n");
			response.render(JavaScriptHeaderItem.forScript(script.toString(), "refreshdialog"+Dialog2.this.getMarkupId()));
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
	
	public Dialog2(String id, String titlekey, String messagekey, Button... buttons) {
		super(id);
		
		setOutputMarkupId(true);
		
		this.titlekey = titlekey;
		this.messagekey = messagekey;
		
		addButtons(buttons);
		
		add(new Label("title", getTitle()));
		
		WebMarkupContainer body = new WebMarkupContainer("modal-body");
		body.setOutputMarkupId(true);
		body.add((new Label("message", getMessage())).setEscapeModelStrings(false));
		add(body);
		
		
		WebMarkupContainer footer = new WebMarkupContainer("footer");
		add(footer);
		
		footer.add(new AttributeModifier("class", "modal-footer " +  (this.buttons.size()>1?" center ": " right ")));
		
		footer.add(new ListView<Button>("buttons", this.buttons) {
			public void populateItem(ListItem<Button> item) {
				final Button button = item.getModelObject();
				AjaxLink<Void> buttonlink = new AjaxLink<Void>("button") {
					public void onClick(AjaxRequestTarget target) {
						Dialog2.this.onClick(target, button);
						target.appendJavaScript("$('#"+Dialog2.this.getMarkupId()+"').modal('hide')");
					}
				};
				buttonlink.add(new AttributeModifier("class", button.getCssClass()));
				if (button.isCancel())
					buttonlink.add(new AttributeModifier("data-dismiss", "modal"));
				buttonlink.add(new Label("label", new StringResourceModel(button.key(), Dialog2.this, null)));
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
	
	public void onClick(AjaxRequestTarget target, Button button) {
		if (onCloseHandler!=null) {
			onCloseHandler.onClick(target, button);
		}
	}
	
	public void onClose(AjaxRequestTarget target) {
		target.add(getParent());
	}
	
	public void setHandler(Handler handler) {
		this.onCloseHandler = handler;
	}
	
	public List<Button> getButtons() {
		return this.buttons;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_0_360));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_361_800 ));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_801_1200));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1201_1600));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1601));


		
	}
	
	protected void addButtons(Button... buttons) {
		this.buttons.clear();
		for (int b=0; b<buttons.length; b++) {
			this.buttons.add(buttons[b]);
		}
	}
}
