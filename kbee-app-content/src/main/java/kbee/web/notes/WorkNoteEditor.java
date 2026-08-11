package kbee.web.notes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.googlecode.wicket.jquery.ui.markup.html.link.AjaxSubmitLink;
import com.novamens.beans.BeansService;
import com.novamens.content.base.Resource;
import com.novamens.content.dao.ContentDao;

import com.novamens.content.notes.Billboard;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.Role;
import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.notes.KbeeBillboard;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.scheduler.CronExpressionJ8;
import com.novamens.security.Principal;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.BooleanSwitchField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.CronField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.model.ListModel;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.OffsetDateTimeField;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.enoti.ReceiversEditor;
import kbee.web.enoti.RoleReceiversEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.Buttons;
import kbee.web.form.FileUploadField;
import kbee.web.panel.AlertPanel;
//import kbee.web.form.TextEditorField;
import kbee.web.resource.ResourceLink2;
import kbee.wicket.froala.FroalaField;
//import kbee.wicket.tinymce.settings.TinyMCESettings;

/**
 * NOTE. These are new Alerts (Regular and Billboard)
 *
 */
@SuppressWarnings("serial")
public class WorkNoteEditor extends DomainObjectEditor<Billboard> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(WorkNoteEditor.class.getName());
	
	/**
	static private String FORMATS = "style_formats : ["
			+ "{title : 'Highlight inline'		, inline : 'span',     classes : 'highlight-inline'},"
			+ "{title : 'Parameter'				, inline : 'span',     classes : 'parameter'},"
			+ "{title : 'Table row 1', selector : 'tr', classes : 'tablerow1'}]";
			
	**/


	final boolean is_support = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot(); 
	final boolean is_admin = is_root || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());

	private boolean isnew = true;
	private boolean isPreviewVisible = false;
	
	private CronExpressionJ8 cronExpression = null;

	private String iconCss;
	
	private List<IModel<Principal>> m_receivers;
	private List<IModel<Role>> m_role_receivers;
	private Boolean frequency = Boolean.valueOf(false);
	
	
	public String getIconCss() {
		return this.iconCss;
	}
	
	public void setIconCss( String s) {
		this.iconCss=s;
	}
	
	
	/** --------------------------------------------------------------------------------------------------------------
	 * 
	 * 
	 * 
	 *
	 */
	public class BillboardAlertFragment extends Fragment {
		
		Map<String, Object> parameters;
		
		public BillboardAlertFragment (String id) {
			super(id, "billboard-alert-fragment", WorkNoteEditor.this);
		}
		
		public Map<String, Object> getParameters() {
			if (parameters==null)
				parameters =  new HashMap<String, Object>();
			return parameters;
		}
		
		public void setParameters(Map<String, Object> parameters) {
			this.parameters=parameters;
		}
		
		@SuppressWarnings("unchecked")
		public void onBeforeRender() {
			super.onBeforeRender();

			getParameters().put("title", ((TextField<String>) WorkNoteEditor.this.get("form:title")).getValue());
			
			String s = ((ChoiceField<String>) WorkNoteEditor.this.get("form:icon")).getValue();
			
			 if (getIconCss()!=null) { 
				 s=getIconCss();
			 }
			
			if (s!=null) {
					getParameters().put("icon", KbeeBillboard.getFontAwesomeIcon(s));
					getParameters().put("icon-color", "success");
			}
			
			getParameters().put("text", ((FroalaField) WorkNoteEditor.this.get("form:text")).getValue());
		}
		
		public void onInitialize() {
			super.onInitialize();
			
			Label title= new Label ("xtitle", new Model<String>() {
				public String getObject() {
					return (String) getParameters().get("title");
				}			
			}) {
				public boolean isVisible() {
					return ((String) getParameters().get("title")!=null);
				}
			};
			add(title);
			
			Label text= new Label ("text", new Model<String>() {
				public String getObject() {
					return (String) getParameters().get("text");
				}
				
			}) {
				public boolean isVisible() {
					return ((String) getParameters().get("text")!=null);
				}
			};
			text.setEscapeModelStrings(false);
			
			add(text);
			WebMarkupContainer icon= new WebMarkupContainer ("icon") {
				public boolean isVisible() {
					return (getIconCss()!=null || (String) getParameters().get("icon")!=null);
				}
			};
			icon.add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					
					if (getIconCss()!=null)
						return getIconCss();
					
					if ((String) getParameters().get("icon") != null) {
						return  (String) getParameters().get("icon") + (
								(String) getParameters().get("icon-color")!=null ?
							    (" "+ (String) getParameters().get("icon-color")) : "" ) ;
					}
					return "";
				}
			}));
			add(icon);
			
			
			WebMarkupContainer wc = new WebMarkupContainer("attachment-container");
			add(wc);
			if (getModel().getObject().getFile()!=null) {
				Link<Resource> ofile = new ResourceLink2("openfile", new ObjectModel<Resource>(getModel().getObject().getFile()));
				wc.add(ofile);
				ofile.add(new Label("file", getModel().getObject().getFile().getName()));
			}
			else
				wc.setVisible(false);
		}
	}
	
	/** -----------------------------------------------------------------
	 * 
	 * 
	 * 
	 * 
	 * @param id
	 * @param model
	 * @param isnew
	 * 
	 */
	public WorkNoteEditor(String id, IModel<Billboard> model, boolean isnew) {
		super(id, model);
		setEditionEnabled(true);
		this.isnew=isnew;
	}

	
	Form<?> form;
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		setEditionEnabled(true);
		
		form = new Form<Void>("form", Disposition.VERTICAL);

		if (getModel().getObject().getCronExpression()!=null) {
			setFrequency(Boolean.valueOf(true));	
			this.setCronExpression(getModel().getObject().getCronExpression());
		}
		else {
			setFrequency(Boolean.valueOf(false));
			this.setCronExpression(new CronExpressionJ8("0 15 6 5 * *"));
		}
		
		CronField c = new CronField("cronExpression") {
			public boolean isVisible() {
					return  WorkNoteEditor.this.getModel().getObject().isBillboard() && !isDomainKbee() && getFrequency();
			}
			public String getStyleStr() {
				return "margin-top: -44px;  margin-left: 15px;";
			}
			@Override
			public boolean isPlaceholderLabel() {
					return false;
			}
		};
		
	   c.setOutputMarkupId(true);
       c.setBorder(false);
       
		WebMarkupContainer kfi_c = new  WebMarkupContainer("file-container") {
	    	   public boolean isVisible() {
	    		   	return WorkNoteEditor.this.getModelObject().isBillboard();
	    	   }
	       };
	       
	     kfi_c.setOutputMarkupId(true);
		form.add(kfi_c);
		

		FileUploadField kfi = new FileUploadField("file") {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				getModelObject().setFile(getValue());
				setUpdatedPart("add file " + (getValue()!=null? getValue().getDisplayName():"null"));
				target.add(WorkNoteEditor.this.form.get("file-container"));
			}
			
			@Override
			public void onRemove(AjaxRequestTarget target) {
				getModelObject().setFile(null);
				setUpdatedPart("remove file");
				target.add(WorkNoteEditor.this.form.get("file-container"));
			}
		};
		
		 kfi_c.add(kfi);

		 
		 
		
		WebMarkupContainer ic = new WebMarkupContainer("image-container") {
			  public boolean isVisible() {
	    		   	return WorkNoteEditor.this.getModelObject().isBillboard();
	    	   }
		};
		
		add(ic);
	    ic.setOutputMarkupId(true);
		form.add(ic);

		FileUploadField kic = new FileUploadField("sideImage") {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				getModelObject().setSideImage(getValue());
				setUpdatedPart("add image " + (getValue()!=null? getValue().getDisplayName():"null"));
				target.add(WorkNoteEditor.this.form.get("image-container"));
			}
			
			@Override
			public void onRemove(AjaxRequestTarget target) {
				getModelObject().setSideImage(getValue());
				setUpdatedPart("remove image");
				target.add(WorkNoteEditor.this.form.get("image-container"));
			}
		};
		
		ic.add(kic);

		 
       WebMarkupContainer wm = new  WebMarkupContainer("frequency-container") {
    	   public boolean isVisible() {
    		   return !isDomainKbee() && getModel().getObject().isBillboard();
    	   }
       };
       
       wm.setOutputMarkupId(true);
       wm.add(c);
               
       BooleanSwitchField isf=new BooleanSwitchField("frequent", new PropertyModel<>(this, "frequency")) {    	      
    	   @Override
    	   public void onUpdate(AjaxRequestTarget target) {
    		   setFrequency(!getFrequency());
    		   target.add(WorkNoteEditor.this.form.get("frequency-container"));
    	   }
       };       
       
       isf.setVisible(getModel().getObject().isBillboard() && (isnew || getFrequency()));
       
       isf.setBorder(true);
       form.add(isf);
       form.add(wm);
        
	   WebMarkupContainer al=new WebMarkupContainer("kbee-warn");
	   al.setVisible(isDomainKbee());
	   form.add(al);

		Label xtitle = new Label("formtitle", new StringResourceModel( getModel().getObject().isBillboard() ?  getLabel("billboard").getObject() : getLabel("alert").getObject(), this, null));
		form.add(xtitle);
		
		form.add(new AjaxSubmitLink("preview-link") {
			@Override
			public boolean isVisible() {
				return WorkNoteEditor.this.getModel()!=null &&
					WorkNoteEditor.this.getModel().getObject()!=null &&
					WorkNoteEditor.this.getModel().getObject().isBillboard();						
			}
			@Override
			public void onSubmit(AjaxRequestTarget target) {
				if (!isPreviewVisible())
					setPreviewVisible(true);
				else
					setPreviewVisible(false);
				target.add(WorkNoteEditor.this.form.get("billboard-preview-container"));
			}
			
			
			/**
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				AjaxCallListener myAjaxCallListener = new AjaxCallListener() {
					@Override 
					public CharSequence getBeforeHandler(Component component) { 
						return "tinyMCE.triggerSave(true,true)";
					}
				};
				attributes.getAjaxCallListeners().add(myAjaxCallListener);
			}**/
		});
		
		
		
		if (!isnew) {
		
			StringResourceModel m=new StringResourceModel( (getFrequency() ? "was-sent-frequent":"was-sent"), WorkNoteEditor.this, null);
			AlertPanel<Void> pa=new AlertPanel<Void>("was-sent", AlertPanel.WARNING, null, null, m);
			pa.setIcon("fa-duotone fa-bullhorn");
			form.add(pa);
		}
		else
			form.add( new InvisiblePanel("was-sent"));
		
		
		BillboardAlertFragment panel = new BillboardAlertFragment("billboard-preview") {
			public boolean isVisible() {
				return isPreviewVisible();
			}
		};

		WebMarkupContainer bcp=new WebMarkupContainer("billboard-preview-container");
		bcp.setOutputMarkupId(true);
		bcp.add(panel);
		form.add(bcp);
				
		
		
		/**
		form.add(new TextEditorField("text", new PropertyModel<String>(getModel(), "text")) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
			}
			@Override
			protected String getStyleFormats() {
				return FORMATS ;
			}
			@Override
			protected TinyMCESettings.Theme getTheme() {
				return TinyMCESettings.Theme.simple;
			}
		});
		**/

					
		form.add(new FroalaField("text", new PropertyModel<String>(getModel(), "text")) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
			}
		});

		ChoiceField<String> ch = new ChoiceField<String>("icon", new PropertyModel<String>(new Model<Panel>(this), "icon"), new ListModel<String>(new Model<Panel>(this), "icons", true)) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
				setIconCss(KbeeBillboard.getFontAwesomeIcon(getValue()));
				TextField<String> t=(TextField<String>) WorkNoteEditor.this.form.get("iconcss");
				t.setValue(getIconCss());
				target.add(WorkNoteEditor.this);
			}
		};
		ch.setVisible(isBillboard());
		form.add(ch);
		
		TextField<String> iconCss = new TextField<String>("iconcss", new PropertyModel<String>(new Model<Panel>(this), "iconCss"));
		iconCss.setVisible(isBillboard());
		form.add(iconCss);
		
		form.add(new TextField<String>("title", true));
		
		setXreceivers(getModel().getObject().getReceivers());

		ReceiversEditor<Billboard> receivers = new ReceiversEditor<Billboard>("receivers") {
			@Override
			public boolean isEnabled() {
				return WorkNoteEditor.this.isnew || getFrequency();
			}
			@Override
			protected IModel<Collection<Principal>> getPropertyModel() {
				return new PropertyModel<Collection<Principal>>(WorkNoteEditor.this, "xreceivers");
			}
		};
		
		setRreceivers(getModel().getObject().getRoleReceivers());
		
		RoleReceiversEditor<Billboard> rolreceivers = new RoleReceiversEditor<Billboard>("roleReceivers") {
			@Override
			public boolean isEnabled() {
				return WorkNoteEditor.this.isnew || getFrequency();
			}
			@Override
			protected IModel<Collection<Role>> getPropertyModel() {
				return new PropertyModel<Collection<Role>>(WorkNoteEditor.this, "rreceivers");
			}
		};
		
		receivers.setVisible(!isDomainKbee());
		form.add(receivers);
		
		rolreceivers.setVisible(!isDomainKbee());
		form.add(rolreceivers);

		OffsetDateTimeField sp = new OffsetDateTimeField("startpub", getDomainZoneId(), true);
		sp.setVisible(isBillboard());
		form.add(sp);
		
		OffsetDateTimeField ep = new OffsetDateTimeField("endpub", getDomainZoneId());
		ep.setVisible(isBillboard());
		form.add(ep);
		
		BooleanField bo = new BooleanField("isemail", new PropertyModel<Boolean>(this, "isEmail"));
		bo.setVisible(isAlert());
		form.add(bo);
		add(form);
		
		add(new Buttons<Billboard>(this) {
			@Override
			protected String getCss() {
				return "btn-sm btn-default";
			}
			@Override
			protected String getSubmitCss() {
				return "btn-sm btn-primary";
			}
			
			//@Override
			//protected String getBeforeSubmitHandler() {
			//	return "tinyMCE.triggerSave(true,true);";
			//}
			
			@Override
			public boolean isVisible() {
				if (isReadOnly())
					return false;
				if (!isEditionEnabled())
					return false;
				if (is_admin)
					return true;
				if (is_support)
					return false;
				return true;
			}
		});
	}

	public Boolean getFrequency() {
		return frequency;
	}

	public void setFrequency(Boolean frequency) {
		this.frequency = frequency;
	}

	
	

	
	/**
	 *  Save update -> 
	 *  
	 *  If it is a one time -> no edition
	 *  if is a recurrent -> edit text for [next time]
	 */
	@Override
	public void update(AjaxRequestTarget target) {
		try {

			if (!getUpdatedParts().isEmpty()) {

				if (this.m_receivers!=null) {
					List<Principal> list = new ArrayList<Principal>();
					for(IModel<Principal> p: this.m_receivers)
						list.add(p.getObject());
					getModel().getObject().setReceivers(list);
				}
					
				if (this.m_role_receivers!=null) {
					List<Role> list = new ArrayList<Role>();
					for(IModel<Role> p: this.m_role_receivers)
						list.add(p.getObject());
					((KbeeBillboard)getModel().getObject()).setRoleReceivers(list);
				}
					
				if (!getFrequency()) 
					getModel().getObject().setCronExpression(null);
					
				if (getIconCss()!=null)
					((com.novamens.kbee.content.notes.KbeeBillboard) getModel().getObject()).setGlyphicon(getIconCss());
					
				
				String text1 = ((FroalaField) form.get("text")).getModel().getObject();
				getModel().getObject().setText(text1!=null?text1.trim():"");
					
				/**--
				  if it is new, send notification
				  else we assume the notification was sent already.
				 --**/
					
				getModel().getObject().setTimeZone(getSessionUser().getTimeZone());
					
				((com.novamens.kbee.content.notes.KbeeBillboard) getModel().getObject()).sendNotification(this.isnew);
						
				if (getModel().getObject().getEndpub()!=null)
						((com.novamens.kbee.content.notes.KbeeBillboard) getModel().getObject()).setEndpub(getModel().getObject().getEndpub().plusDays(1));
				
				if (isDomainKbee() && this.isnew) 
					sendToAllUsers();
				else
					getDomain().getService(DomainSettingsService.class).save(getModel().getObject());
						
				onUpdate(target);
					
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
	
//	private ContentSecurityDao getSecurityDao() {
//		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
//	}

	private void sendToAllUsers() {
		
		for (Domain domain: getContentDao().getDomains(ObjectState.ENABLED)) {
			
			Billboard note = null;
			try {
				note = domain.getService(DomainSettingsService.class).createBillboard(getModel().getObject().getTitle(), getModel().getObject().getText());
				((KbeeBillboard) note).setGlyphicon(getModel().getObject().getGlyphicon());
				((KbeeBillboard) note).setEmail(getModel().getObject().isEmail());
				((KbeeBillboard) note).setCreationOffsetDateTime(OffsetDateTime.now());
				((KbeeBillboard) note).setDomain(domain);
				((KbeeBillboard) note).setEndpub(getModel().getObject().getEndpub());
				((KbeeBillboard) note).setStartpub(getModel().getObject().getStartpub());
				((KbeeBillboard) note).setAlert(getModel().getObject().isAlert());
	
				for (Group gr: getContentSecurityDao().getCanonicalGroups(domain)) {
					if (gr.getName().equals(KbeeGlobalRole.USER.getId())) {
							List<Principal> list = new ArrayList<Principal>();
							list.add(gr);
							((KbeeBillboard) note).setReceivers(list);
							if (note.getEndpub()!=null)
								((com.novamens.kbee.content.notes.KbeeBillboard) note).setEndpub(note.getEndpub().plusDays(1));
							logger.debug("Sending Billboard "+ note.getDisplayName() +" | to Domain -> " + domain.getName());
							getDomain().getService(DomainSettingsService.class).save(note);
							break;
						}
					}
			} catch (Exception e) {
				logger.error(e, (note!=null ? ((KbeeBillboard) note).toString() : "null"));
			}
			
		}
	}

	@Override
	public void cancel(AjaxRequestTarget target) {
		super.cancel(target);
		if (this.isnew)
			getDomain().getService(DomainSettingsService.class).remove(getModel().getObject());
		 onCancel(target); 
	}

	public void setXreceivers(List<Principal> list) {
		m_receivers = new ArrayList<IModel<Principal>>();
		for (Principal p: list) {
			m_receivers.add(new ObjectModel<Principal>(p));
		}
	}
	
	public List<Principal> getXreceivers() {
		List<Principal> receivers = new ArrayList<Principal>();
		if (this.m_receivers!=null) {
			for (IModel<Principal> model : this.m_receivers) {
				receivers.add(model.getObject());
			}
		}
		return receivers;
	}
	
	public void setRreceivers(List<Role> list) {
		m_role_receivers = new ArrayList<IModel<Role>>();
		for (Role p: list) {
			m_role_receivers.add(new ObjectModel<Role>(p));
		}
	}
	
	public List<Role> getRreceivers() {
		List<Role> receivers = new ArrayList<Role>();
		if (this.m_role_receivers!=null) {
			for (IModel<Role> model : this.m_role_receivers) {
				receivers.add(model.getObject());
			}
		}
		return receivers;
	}
	

	public boolean isPreviewVisible() {
		return isPreviewVisible;
	}

	public void setPreviewVisible(boolean isPreviewVisible) {
		this.isPreviewVisible = isPreviewVisible;
	}

	public void onUpdate(AjaxRequestTarget target) {}
	public void onCancel(AjaxRequestTarget target) {}
	
	public boolean isBillboard() {
		return getModelObject().isBillboard();
	}
	
	public void setIsEmail(boolean b) {
		((KbeeBillboard) getModelObject()).setEmail(b);
	}

	public void setEmail(boolean b) {
		((KbeeBillboard) getModelObject()).setEmail(b);
	}
	
	public boolean isAlert() {
		return getModelObject().isAlert();
	}

	public boolean isEmail() {
		return getModelObject().isEmail();
	}
	
	public void setIcon(String icon) {
		((KbeeBillboard) getModelObject()).setGlyphicon(icon);
	}

	public String getIcon() {
		return ((KbeeBillboard) getModelObject()).getGlyphicon();
	}
	
	public List<String> getIcons() {
		return  KbeeBillboard.getIconList();
	}
	
	public CronExpressionJ8 getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(CronExpressionJ8 cronExpression) {
		this.cronExpression = cronExpression;
	}
	
	public void onDetach() {
		super.onDetach();
		if (m_receivers!=null) 
			for( IModel<Principal> m: m_receivers) m.detach();
		if (m_role_receivers!=null) 
			for( IModel<Role> m: m_role_receivers) m.detach();
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected IModel<String> getLabel(String key) {
		return new StringResourceModel(key, this, null);
	}

}
