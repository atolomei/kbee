package com.novamens.wicket.markup.html.modal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxLinkV5;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingIndicatorAjaxSubmitLink;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.wicket.markup.html.panel.KBPanel;


/**
 * Modal Windows can be
 *  Full Screen or Centered 
 *
 */
@SuppressWarnings("serial")
public class Modal extends KBPanel {
	
	private static final long serialVersionUID = 1L;
																	
	private static final ResourceReference CSS_KBEE_BOOTSTRAP			 = new CssResourceReference(AbstractKbeeWebPage.class, "kbeebootstrap.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_0_360      = new CssResourceReference(AbstractKbeeWebPage.class, "kb-0-360.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_361_800    = new CssResourceReference(AbstractKbeeWebPage.class, "kb-361-800.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_801_1200   = new CssResourceReference(AbstractKbeeWebPage.class, "kb-801-1200.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1201_1600  = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1201-1600.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1601  	 = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1601.css");
															
	
	private int modal_type = MODAL_FULL_SCREEN;
	
	
	private String titlekey; //, messagekey;
	private String subtitlekey;
	
	private Object[] parameters;
	private Object[] subparameters;
	
	private Handler onCloseHandler;
	private List<Button> buttons = new ArrayList<Button>();
	private Panel body;
									
	public static Button Cancel 	= new Button("button.cancel", "btn btn-sm btn-default", ButtonType.CANCEL);
	public static Button Cancel_lg  = new Button("button.cancel", "btn btn-lgbtn-default", ButtonType.CANCEL);
	
	public static Button Delete     = new Button("button.delete", "btn btn-sm btn-danger");
	public static Button Delete_lg  = new Button("button.delete", "btn btn-lg btn-danger");
	
	public static Button Close 		= new Button("button.close", "btn btn-sm btn-default",ButtonType.CLOSE);
	public static Button Close_lg 	= new Button("button.close", "btn btn-lg btn-default",ButtonType.CLOSE);
	
	public static Button OK 		= new Button("button.ok", "btn btn-sm btn-primary",ButtonType.SUBMIT);
	public static Button OK_lg 		= new Button("button.ok", "btn btn-lg btn-primary",ButtonType.SUBMIT);
						
	public static Button Send 		= new Button("button.send", "btn btn-sm btn-primary",ButtonType.SUBMIT);
	public static Button Send_lg 	= new Button("button.send", "btn btn-lg btn-primary",ButtonType.SUBMIT);
		
	public static Button Save 		= new Button("button.save", "btn btn-sm btn-primary",ButtonType.SUBMIT);
	public static Button Save_lg 	= new Button("button.save", "btn btn-lg btn-primary",ButtonType.SUBMIT);
								
	static public final int MODAL_FULL_SCREEN   = 1;
	static public final int MODAL_CENTER  		= 2; 
	
	public class RefreshBehavior extends AbstractDefaultAjaxBehavior {
		@Override
		protected void respond(AjaxRequestTarget target) {
			if (Modal.this.get("modal-dialog:body")!=null)
			Modal.this.get("modal-dialog:body").setVisible(false);
			Modal.this.onClose(target);
		}
		@Override
		public void renderHead(final Component component, final IHeaderResponse response) {
			super.renderHead(component, response);
			StringBuilder script = new StringBuilder();
			script.append("function refreshdialog"+Modal.this.getMarkupId()+"() {\n");
			script.append(getCallbackScript());
			script.append("}\n");
			response.render(JavaScriptHeaderItem.forScript(script.toString(), "refreshdialog"+Modal.this.getMarkupId()));
		}
	}

	public enum ButtonType {
		BUTTON,
		CANCEL,
		SUBMIT,
		CLOSE
	};
	
	public static class Button implements Serializable {
		private static final long serialVersionUID = 1L;
		private String key;
		private String css;
		private boolean closeOnClick = true;
		private ButtonType type;
		public Button(String key, String css) {
			this(key, css, ButtonType.BUTTON);
		}
		public Button(String key, String css, ButtonType type) {
			this.key = key;
			this.css = css;
			this.type = type;
		}
		public Button(String key, String css, ButtonType type, boolean closeOnClick) {
			this.key = key;
			this.css = css;
			this.type = type;
			this.closeOnClick = closeOnClick;
		}
		public String key() {
			return key;
		}
		public String getCssClass() {
			return css;
		}
		public boolean isCancel() {
			return type.equals(ButtonType.CANCEL);
		}
		public boolean isSubmit() {
			return type.equals(ButtonType.SUBMIT);
		}
		public boolean isClose() {
			return type.equals(ButtonType.CLOSE);
		}
		public boolean isVisible() {
			return true;
		}
		public boolean closeOnClick() {
			return closeOnClick;
		}
	};
	
	public static class Handler implements Serializable {
		public void onClick(AjaxRequestTarget target, Button button) {
		}
	}
	
	public Modal(String id) {
		super(id);
		setOutputMarkupId(true);
	}	
	
	public Modal(String id, String titlekey, Panel body, Button... buttons) {
		this(id, titlekey, null, body, buttons);
	}
	
	
	public Modal(String id, String titlekey, String subtitlekey, Panel body, Button... buttons) {
		super(id);
		setOutputMarkupId(true);
		setTitle(titlekey);
		setSubtitle(subtitlekey);
		setBody(body);
		setButtons(buttons);
	}
	
	public void open(AjaxRequestTarget target) {
		if (get("modal-dialog")==null)
			addComponents();
		
		Modal.this.get("modal-dialog:body").setVisible(true);
		target.add(this);
		
		target.appendJavaScript("$('#"+getMarkupId()+"').modal('show')");
		target.appendJavaScript("$('#"+getMarkupId()+"').on('hide.bs.modal', function (e) { refreshdialog"+Modal.this.getMarkupId() + "();})");
	}

	
	/**
	 * 
	 * @param target
	 * @param panel
	 */
	public void open(AjaxRequestTarget target, Panel panel) {
		
		if (get("modal-dialog")==null)
			addComponents();
		
		Modal.this.get("modal-dialog:body").replaceWith(panel);
		Modal.this.get("modal-dialog:body").setVisible(true);
		
		target.add(this);

		target.appendJavaScript("$('#"+getMarkupId()+"').modal('show')");
		target.appendJavaScript("$('#"+getMarkupId()+"').on('hide.bs.modal', function (e) { refreshdialog"+Modal.this.getMarkupId() + "();})");
	}
	
	/**
	 * This can not support subtitle params
	 *  
	 * @param target
	 * @param panel
	 * @param handler
	 * @param parameter
	 */
	public void open(AjaxRequestTarget target, Panel panel, Handler handler, String... titleparameter) {
		
		setParameters(titleparameter);
		
		if (get("modal-dialog")==null)
			addComponents();
		
		setBody(panel);
		
		Modal.this.get("modal-dialog:body").replaceWith(panel);
		Modal.this.get("modal-dialog:body").setVisible(true);
		
		this.onCloseHandler = handler;
		
		target.add(this);
		target.appendJavaScript("$('#"+getMarkupId()+"').modal('show')");
		target.appendJavaScript("$('#"+getMarkupId()+"').on('hide.bs.modal', function (e) { refreshdialog"+Modal.this.getMarkupId() + "();})");

	}
	
	/**
	 * 
	 * This can not support subtitle params
	 *  
	 * @param target
	 * @param handler
	 * @param parameter
	 */
	public void open(AjaxRequestTarget target, Handler handler, String... parameter) {
	
		if (get("modal-dialog")==null)
			addComponents();
		
		Modal.this.get("modal-dialog:body").setVisible(true);
		this.onCloseHandler = handler;
		setParameters(parameter);
		target.add(this);
		target.appendJavaScript("$('#"+getMarkupId()+"').modal('show')");
		//target.appendJavaScript("$('#"+getMarkupId()+"').on('hide.bs.modal', function (e) { refreshdialog"+Modal.this.getMarkupId() + "();})");
	}
	
	public void setTitle(String titlekey) {
		this.titlekey = titlekey;
	}
	
	public void setSubtitle(String subtitlekey) {
		this.subtitlekey = subtitlekey;
	}
	
	public IModel<String> getTitle() {
		StringResourceModel model = new StringResourceModel(titlekey);
		model.setParameters(this.parameters);
		return model;
	}
	
	public IModel<String> getSubtitle() {
		StringResourceModel model = new StringResourceModel(subtitlekey);
		model.setParameters(this.subparameters);
		return model;
	}
	
	public Panel getBody() {
		return body;
	}
	
	public void setBody(Panel panel) {
		
		if (panel!=null)
			panel.setVisible(false);
		
		if (get("modal-dialog:body")!=null) 
			((WebMarkupContainer) get("modal-dialog")).replace(panel);
		
		this.body = panel;
	}
	
	public void setButtons(Button... buttons) {
		for (int b=0; b<buttons.length; b++) {
			this.buttons.add(buttons[b]);
		}		
	}
	
	public void replaceButtons(Button... buttons) {
		this.buttons.clear();
		setButtons(buttons);
	}
	
	public List<Button> getButtons() {
		return this.buttons;
	}
	
	public int getModalType() {
		return modal_type;
	}
	
	public void setModalType(int type) {
		this.modal_type=type;
	}
	
	public void setHandler(Handler handler) {
		this.onCloseHandler = handler;
	}
	
	public void onClick(AjaxRequestTarget target, Button button) {
		if (this.onCloseHandler!=null) 
			this.onCloseHandler.onClick(target, button);
	}
	
	public void onClose(AjaxRequestTarget target) {
//		if (this.onCloseHandler!=null) 
//			this.onCloseHandler.onClick(target, null);
		//target.add(getParent());
	}
	
	public void setParameters(String... parameter) {
		this.parameters = parameter;
	}
				
	public void setSubtitleParameters(String... parameter) {
		this.subparameters = parameter;
	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
		if (get("modal-dialog")==null)
			addComponents();

		if (modal_type==MODAL_FULL_SCREEN)
			((WebMarkupContainer)get("modal-dialog")).add(new AttributeModifier("class", "modal-dialog modal-dialog-full"));
		else
			((WebMarkupContainer)get("modal-dialog")).add(new AttributeModifier("class", "modal-dialog modal-dialog-center"));
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
	
	
	protected void addComponents() {
		
		WebMarkupContainer modal_dialog = new WebMarkupContainer("modal-dialog");
		
		modal_dialog.setOutputMarkupId(true);
		
		addOrReplace(modal_dialog);
		
		if (titlekey!=null)
			modal_dialog.add(new Label("title", getTitle()));
		else
			modal_dialog.add( ( (new Label("title", ""))).setVisible(false));
		
		
		if (subtitlekey!=null)
			modal_dialog.add( (new Label("subtitle", getSubtitle())));
		else
			modal_dialog.add( ( (new Label("subtitle", ""))).setVisible(false));

		if (getBody()==null) {
			setBody(new Panel("body") {
				@Override
				public boolean isVisible() {
					return false;
				}
			});
		}
			
		modal_dialog.add(getBody());
		
		getBody().setVisible(false);
		
		WebMarkupContainer modal_footer = new WebMarkupContainer("modal-footer"); 
				
		modal_dialog.add(modal_footer);
		
		modal_footer.add(new ListView<Button>("buttons", this.buttons) {
			public void populateItem(ListItem<Button> item) {
				
				final Button button = item.getModelObject();
										
				if (button.isSubmit()  && getBodyForm()!=null) {

					Form<?> form = getBodyForm();

		
					WorkingIndicatorAjaxSubmitLink buttonlink = new WorkingIndicatorAjaxSubmitLink("button", new StringResourceModel(button.key(), Modal.this, null).getString(), form) {

						@Override
						protected void onSubmit(AjaxRequestTarget target) {
							
							Editor<?> editor = getEditor();
							
							if (editor!=null) 
								editor.update(target);

							
							Modal.this.onClick(target, button);
							
							
							if (button.closeOnClick()) {
								target.appendJavaScript("$('#"+Modal.this.getMarkupId()+"').modal('hide')");
							}
						}
						
						@Override
						public String getWorkingLabel() {
							return new StringResourceModel("saving", Modal.this, null).getString();
						}

						@Override
						public String getBeforeHandler() {
							return "";
						}

						
						@Override
						protected void onError(AjaxRequestTarget target) {
							target.add(Modal.this.getBody());
						}
						
				
						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							super.updateAjaxAttributes(attributes);
							
							attributes.setEventPropagation(EventPropagation.STOP);
							
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
									return null;
								}
								@Override
								public CharSequence getBeforeSendHandler(Component component) {
									return null;
								}
								@Override
								public CharSequence getBeforeHandler(Component component) {
									return "document.getElementById('"+component.getMarkupId()+"').innerHTML = '<i class=\"far fa-sync fa-spin fa-fw \"></i> "+getWorkingLabel()+"'";
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

						@Override
						public String getAjaxIndicatorMarkupId() {
							return getId();
						}
					};

					buttonlink.add(new AttributeModifier("class", button.getCssClass()));

					if (button.isCancel())
						buttonlink.add(new AttributeModifier("data-dismiss", "modal"));
					buttonlink.add(new Label("label", new StringResourceModel(button.key(), Modal.this, null)));
					item.add(buttonlink);

				}
				else {

					if (button.isSubmit()) {

						WorkingIndicatorAjaxLinkV5<Void> buttonlink = new WorkingIndicatorAjaxLinkV5<Void> ("button") { 
							@Override
							public void onClick(AjaxRequestTarget target) {
								Modal.this.onClick(target, button);
								if (button.closeOnClick())
									target.appendJavaScript("$('#"+Modal.this.getMarkupId()+"').modal('hide')");
							}
							@Override
							public boolean isVisible() {
								return button.isVisible();
							}
							@Override
							protected String getWorkingLabel() {
								return new StringResourceModel("saving", Modal.this, null).getString();

							}
							
							
							@Override
							protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
								super.updateAjaxAttributes(attributes);
								// attributes.setEventPropagation(EventPropagation.STOP);
							}
						};
						
						buttonlink.add(new Label("label", new StringResourceModel(button.key(), Modal.this, null)));
						item.add(buttonlink);

						buttonlink.add(new AttributeModifier("class", "btn btn-primary btn-sm"));

					}
					else {	
							AbstractLink buttonlink;
		
							buttonlink = new AjaxLink<Void>("button") {
								@Override
								public void onClick(AjaxRequestTarget target) {
									Modal.this.onClick(target, button);
									if (button.closeOnClick())
										target.appendJavaScript("$('#"+Modal.this.getMarkupId()+"').modal('hide')");
								}
								@Override
								public boolean isVisible() {
									return button.isVisible();
								}
							};
							
							buttonlink.add(new AttributeModifier("class", button.getCssClass()));
							
							
							if (button.isCancel())
								buttonlink.add(new AttributeModifier("data-dismiss", "modal"));
					
							buttonlink.add(new Label("label", new StringResourceModel(button.key(), Modal.this, null)));
							item.add(buttonlink);
					}
				};
				
			}
		});

		if (getFooterCss()!=null)
			modal_footer.add(new AttributeModifier("class", getFooterCss()));
		
		add(new RefreshBehavior());
	}
	
	protected IModel<String> getFooterCss() {
		return new Model<String>("modal-footer center");
	}
	
	protected Form<?> getBodyForm() {
		boolean found = false;
		MarkupContainer parent = getBody();
		while (!found  && parent!=null) {
			Iterator<Component> childs = parent.iterator();
			parent = null;
			while (childs.hasNext()) {
				Component child = childs.next();
				if (child instanceof Form) {
					return (Form<?>)child;
				}
			}
		}
		return null;
	}
	
	protected Editor<?> getEditor() {
		boolean found = false;
		MarkupContainer parent = getBody();
		if (parent instanceof Editor) {
			return (Editor<?>)parent;
		}
		while (!found  && parent!=null) {
			Iterator<Component> childs = parent.iterator();
			parent = null;
			while (childs.hasNext()) {
				Component child = childs.next();
				if (child instanceof Editor) {
					return (Editor<?>)child;
				}
			}
		}
		return null;
	}
}
