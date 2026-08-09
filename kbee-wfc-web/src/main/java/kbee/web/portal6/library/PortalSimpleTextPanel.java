package kbee.web.portal6.library;

import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.dom.Json;
 
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.PortalViewRender;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.DummyBlockPanel;

import kbee.web.error.ErrorPanel;
import kbee.web.help.InlineHelpWebService;
import kbee.web.portal6.PortalObjectDataProviderService;
import kbee.web.portal6.editor.PortalCloseDataProviderAjaxEvent;
import kbee.web.portal6.editor.PortalCloseEditAjaxEvent;
import kbee.web.portal6.event.PortalAjaxEvent;
import kbee.web.portal6.factory.PanelPortalModel;
import kbee.web.portal6.panel.PortalErrorPanel;
import kbee.web.portal6.panel.PortalPanel;


public class PortalSimpleTextPanel extends PortalPanel<Block> implements PanelPortalModel<Block>, PortalViewRender  {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalSimpleTextPanel.class.getName());


	private boolean isEdit  = false;
	private boolean isHelp  = false;

	WebMarkupContainer help;
	WebMarkupContainer editor;
	WebMarkupContainer container;
	
	
	public PortalSimpleTextPanel(String id) {
		super(id);
	}
	
	public PortalSimpleTextPanel(String id, IModel<Block> model, Map<String, String> parameters) {
		super(id, model, parameters);
 
	}
	
	
	@Override
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<PortalCloseDataProviderAjaxEvent<Block>>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(PortalCloseDataProviderAjaxEvent<Block> event) {
				//toogleEdit(event.getRequestTarget());
				editor.setVisible(false);
				container.get("text").setVisible(true);
				event.getRequestTarget().add(container);
			}
		});
		
		
	}
	
	
	protected void refresh(AjaxRequestTarget target) {
		target.add(this);
	}
	
	protected void onEdit(AjaxRequestTarget target) {
		
		if (editor == null || editor instanceof InvisiblePanel) {
			
			try {
				WebMarkupContainer dp = getModel().getObject().getService(PortalObjectDataProviderService.class).getDataProviderEditor("data-provider");
				editor=(dp!=null?dp:new DummyBlockPanel("data-provider"));
			} catch (Exception e) {
				logger.error(e);
				editor=new PortalErrorPanel<>("data-provider", e);
			}
			container.addOrReplace(editor);
			editor.setVisible(false);
		}
		toogleEdit(target);
	}

	protected void onHelp(AjaxRequestTarget target) {
		
		toogleHelp(target);
		/**
		if (help==null || (help instanceof InvisiblePanel)) {
			help=new DummyBlockPanel("help");
			container.addOrReplace(help);
			help.setVisible(false);
		}
		toogleHelp(target);
		**/
	}
	
	
	
	protected WebMarkupContainer getHelpPanel() {
		InlineHelpWebService se = ServiceLocator.getService(InlineHelpWebService.class);
		WebMarkupContainer pa = se.getPanel("help", getLocale(), InlineHelpWebService.PORTAL_SIMPLE_TEXT);
		if (pa!=null) 
			return pa;
		return new ErrorPanel("help", new Model<String>(InlineHelpWebService.PORTAL_SIMPLE_TEXT));
	}
	
	// PORTAL_FAV_CATEGORIES
	
	
	public void toogleHelp(AjaxRequestTarget target) {
	
		if (help==null) {
			help=getHelpPanel();
			help.setVisible(false);
			container.addOrReplace(help);
		}
		
		
		if (help!=null && !(help instanceof InvisiblePanel)) {
			help.setVisible(!help.isVisible());
			container.get("text").setVisible(!container.get("text").isVisible());
			target.add(container);
		}
		
		/**
		if (help!=null && !(help instanceof InvisiblePanel)) {
			help.setVisible(!help.isVisible());
			container.get("text").setVisible(!container.get("text").isVisible());
			target.add(container);
		}
		**/
	}
	
	
	public void toogleEdit(AjaxRequestTarget target) {
		
		
		if (editor!=null && !(editor instanceof InvisiblePanel)) {
			editor.setVisible(!editor.isVisible());
			container.get("text").setVisible(!container.get("text").isVisible());
			target.add(container);
		}
	}


	public boolean isEdit() {
		return isEdit;
	}


	public void setEdit(boolean isEdit) {
		this.isEdit = isEdit;
	}

	public void setHelp(boolean isHelp) {
		this.isHelp = isHelp;
	}


	public boolean isHelp() {
		return isHelp;
	}
	

	
	@Override
	public void onInitialize() {
			super.onInitialize();
			
			setOutputMarkupId(true);
			
			 container = new WebMarkupContainer("container");
			 container.setOutputMarkupId(true);
			 add(container);
			
			setHelp(true);
			setEdit(true);
			
			
			container.addOrReplace(new InvisiblePanel("help"));
			
			//if (help==null) {
			//	help= new DummyBlockPanel("help", new Model<String>(getClass().getSimpleName()));
			//	help.setVisible(false);
			//}
			//container.addOrReplace(help);
			

			if (editor==null) {
				editor= new InvisiblePanel("data-provider");
			}
			container.addOrReplace(editor);

			
		   try {	
				Label la=new Label("title", getModel().getObject().getTitle());
				la.setEscapeModelStrings(false);
				la.setVisible(getModel().getObject().getTitle()!=null);
				add(la);
				
			} catch (Exception e) {
				addOrReplace( new Label("title", e.getClass().getName()));
				logger.error(e);
			}
			
			
			try {
						Json js=getModel().getObject().getCustomValuesJson();
						Object o=js.get("text");
						String text	 = (o!=null?o.toString():null);
						Label te=new Label("text", text);
						te.setEscapeModelStrings(false);
						te.setVisible(text!=null);
						container.add(te);
			} catch (Exception e) {
				logger.error(e);
				container.addOrReplace( new Label("text", e.getClass().getName()+" | " + e.getMessage()));
			}

			
			
			
			
			
			
			
			
			AjaxLink<Void> re = new AjaxLink<Void>("refresh") {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					PortalSimpleTextPanel.this.refresh(target);
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
							String id = component.getMarkupId();
							s1 = "document.getElementById('"+id+"').innerHTML = '"+"<i class=\"far fa-sync \"/>"+"';";
							 s ="setTimeout(function () {"+s1+"}, 350);";
							return s;
						}
						@Override
						public CharSequence getBeforeSendHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getBeforeHandler(Component component) {
							String s = null;
							String id = component.getMarkupId();
							s = "document.getElementById('"+id+"').innerHTML = '<i class=\"far fa-sync fa-spin  spinning\"></i>'";
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
			};
			
			
			
			
			
			
			AjaxLink<Void> ahelp = new AjaxLink<Void>("help-button") {
				private static final long serialVersionUID = 1L;
				
				public boolean isVisible() {
					return isHelp();
				}
				
				@Override
				public void onClick(AjaxRequestTarget target) {
					PortalSimpleTextPanel.this.onHelp(target);
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
							String id = component.getMarkupId();
							s1 = "document.getElementById('"+id+"').innerHTML = '"+"<i class=\"fal fa-info-circle \"/>"+"';";
							 s ="setTimeout(function () {"+s1+"}, 350);";
							return s;
						}
						@Override
						public CharSequence getBeforeSendHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getBeforeHandler(Component component) {
							String s = null;
							String id = component.getMarkupId();
							s = "document.getElementById('"+id+"').innerHTML = '<i class=\"far fa-sync fa-spin   spinning\"></i>'";
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
			};
			
			

			
			AjaxLink<Void> aedit = new AjaxLink<Void>("edit") {
				private static final long serialVersionUID = 1L;
				
				
				public boolean isVisible() {
					return isEdit();
				}
				@Override
				public void onClick(AjaxRequestTarget target) {
					PortalSimpleTextPanel.this.onEdit(target);
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
							String id = component.getMarkupId();
							s1 = "document.getElementById('"+id+"').innerHTML = '"+"<i class=\"far fa-edit \"/>"+"';";
							 s ="setTimeout(function () {"+s1+"}, 350);";
							return s;
						}
						@Override
						public CharSequence getBeforeSendHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getBeforeHandler(Component component) {
							String s = null;
							String id = component.getMarkupId();
							s = "document.getElementById('"+id+"').innerHTML = '<i class=\"far fa-edit spinning\"></i>'";
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
			};
			
			
		 
		
		
			add(aedit);
			add(ahelp);
			add(re);

				
				
				
			
			
	}

	
	

	@Override
	public void setPortalModel(IModel<Block> model) {
		setModel(model);
		
	}

	@Override
	public IModel<Block> getPortalModel() {
		return getModel();
	}

}
