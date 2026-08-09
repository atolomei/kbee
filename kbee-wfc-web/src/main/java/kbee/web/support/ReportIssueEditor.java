package kbee.web.support;


import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.BrandingService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.model.ObjectModel;

import kbee.content.support.SupportTicket;
import kbee.util.PropertiesFactory;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.form.EditButtonsV5;
import kbee.web.form.FileUploadField;
import kbee.web.service.ApplicationSiteMapService;


public class ReportIssueEditor extends ObjectEditor<SupportTicket> {
		
	/**
	 * 
	 * [_Map<String, String> context_]
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReportIssueEditor.class.getName());

	final boolean is_root			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_service_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
	final boolean is_factory_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId());
	final boolean is_support	 = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());

	
	Form<Void> form;
	
	String subject;
	String text;
	Boolean sendme = Boolean.valueOf(true);
	IModel<KBFile> sshotmodel;
	Map<String, String> context = new HashMap<String, String>();
	
	
	public ReportIssueEditor(String id) {
		super(id);
	}
	
	public ReportIssueEditor(String id, IModel<SupportTicket> model) {
		super(id, model);
	}
	
	public void onDetach() {
		super.onDetach();
		if (this.sshotmodel!=null)
			this.sshotmodel.detach();
	}
	
	/**
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();

		setOutputMarkupId(true);
		setEditionEnabled(true);
		
		setModel(new ObjectModel<SupportTicket>(ServiceLocator.getService(ObjectFactoryService.class).createSupportTicket(getSessionUser(), "", "")));
		
		
		form = new Form<Void>("form", Disposition.VERTICAL);
		
								
		TextField<String> subject = new TextField<String>("subject", new PropertyModel<String>(this, "subject"), true) {
			private static final long serialVersionUID = 1L;
			@Override
            public void onUpdate(AjaxRequestTarget target) {
                super.onUpdate(target);
                // setParameters(getValue());
            }
		};
		
        TextAreaField<String> text = new TextAreaField<String>("text", new PropertyModel<String>(this, "text"), 4, 4, true) {
            private static final long serialVersionUID = 1L;
            @Override
            public void onUpdate(AjaxRequestTarget target) {
                super.onUpdate(target);
                //setParameters(getValue());
            }

            @Override
            public boolean isHelpInfo() {
                return false;
            }
        };
        
        text.setRequired(true);

        
        FileUploadField sshot = new FileUploadField("KBFile") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void onUpdate(AjaxRequestTarget target) {
				if (getValue()!=null) {
					getModelObject().setKBFile(getValue());
				}
				ReportIssueEditor.this.onUpdate(target);
			}
		};
		
		form.add(sshot);
        form.add(subject);
        form.add(text);
        // form.add(sendtome);
		add(form);
		
		
		form.add(new EditButtonsV5<SupportTicket>(this, false) {
			
			private static final long serialVersionUID = 1L;
			
			public void onEditClick(AjaxRequestTarget target) {
				super.onEditClick(target);
			}
			
			@Override
			public boolean isVisible() {
				return true;
			}
			
			@Override
			protected IModel<String> getSubmitLabel() {
				return new StringResourceModel("send", ReportIssueEditor.this, null);
			}
			
			@Override
			protected String getCancelClass() {
				return "btn btn-default btn-md";
				
			}
			@Override
			protected String getSubmitClass() {
				return "btn btn-primary btn-md";
			}
		});
		
		
		
		WebMarkupContainer m = new WebMarkupContainer("control-info");
		add(m);
		m.setVisible(is_root || is_support);
		
		
		String emailTo = ServiceLocator.getService(BrandingService.class).getSupportTicketEmailAddress();
		
		Label mode 	= new Label("mode", "email");
		Label ep 	= new Label("endpoint", emailTo);
		m.add(mode);
		m.add(ep);
		
		
		
	}
	
	
	
	/**
	 * 
	 * 
	 */
	public void update(AjaxRequestTarget target) {

			 try {
				 logger.debug(getSubject());
				 logger.debug(getText());
				 logger.debug((this.getScreenShotModel()!=null?this.getScreenShotModel().getObject().getDisplayName():""));
				 
				 getModel().getObject().setSubject(getSubject());
				 getModel().getObject().setText(getText());
				 
				 
				 Map<String, String> map = getContext();

				 
				if (map==null)
					map= new HashMap<String, String>();
				
				map.put("person-email", getPerson().getEmail());
				map.put("person-name", getPerson().getFirstLastName());
				map.put("person-username", getSessionUser().getUserName());
				map.put("server", getServerUrl());

				getModel().getObject().setState(ObjectState.ENABLED);
				getModel().getObject().setDeliveryStatus(SupportTicket.DELIVERY_STATUS_PENDING);
				getModel().getObject().setContext(map);
				 
				 getModel().getObject().getService(DOMObjectService.class).update();
				 
						
			} catch (Exception e1) {
				logger.error(e1);
				setResponsePage(new ApplicationErrorPage<Void>(e1));
			}
			
			 setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage(ApplicationSiteMapService.WorkspacePage));
	}
	
	private Map<String, String> getContext() {
		return context;
	}

	@Override
	public void cancel(AjaxRequestTarget target) {
		try {
			
			getModel().getObject().getService(DOMObjectService.class).delete();
			setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage(ApplicationSiteMapService.WorkspacePage));
			
		} catch (Exception e) {
			logger.error(e);
			setResponsePage(new ApplicationErrorPage<Void>(e));
		}
	}
	
	public void onSubmitClick(AjaxRequestTarget target) {
		setResponsePage(new ApplicationErrorPage<SupportTicket>());
	}
	

	public KBFile getScreenshot() {
		return (this.sshotmodel!=null ? this.sshotmodel.getObject() : null);
	}

	public void setScreenshot(KBFile value) {
		this.sshotmodel=new ObjectModel<KBFile>(value);
			
	}

	
	protected IModel<KBFile> getScreenShotModel() {
		return this.sshotmodel;
	}

	protected void setScreenShotModel(IModel<KBFile> value) {
		this.sshotmodel=value;
			
	}

	
	
	protected void onUpdate(AjaxRequestTarget target) {
		// TODO Auto-generated method stub
		
	}

	
	public Boolean getSendMe() {
		return this.sendme;
	}

	
	public void setSubject(Boolean b) {
		this.sendme=b;
	}

	
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	 
	 
	
	protected Domain getDomain() {
        return (Domain) ServiceLocator.getService(UserService.class).getDomain();
    }
	
	protected InfoDialog getHelpModal() {
		return (InfoDialog) get("help-modal");
	}
	
	
    protected User getSessionUser() {
        return ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
    }
    
    
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	


}
