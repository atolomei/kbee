package kbee.web.emailtemplate;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.email.EmailTemplate;
import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.enoti.ENotiRuleDao;
import com.novamens.content.entity.Person;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.email.EmailBuilder;
import com.novamens.email.EmailData;
import com.novamens.email.EmailService;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.Identifiable;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.InfoDialog;

import kbee.content.support.SupportTicket;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.iql.KbeeIqlHelpService;

@Deprecated 
public class EmailTemplateTestEditor extends ObjectEditor<EmailTemplate> {
			
	private static final long serialVersionUID = 1L;
												
	static kbee.util.logging.Logger logger =  kbee.util.logging.Logger.getLogger(EmailTemplateTestEditor.class.getName());
	
	Form<?> form;
	
	public EmailTemplateTestEditor(IModel<EmailTemplate> model) {
		this("editor", model);
	}

	public EmailTemplateTestEditor(String id, IModel<EmailTemplate> model) {
		super(id, model);
		setOutputMarkupId(true);
		
	}

	
	public void onDetach() {
		super.onDetach();
		try {
			
			x_map = null;
			
			if (getModel()!=null)
				getModel().detach();
		} catch (Exception e) {
			logger.error(e);
		}
	}

	
	private String parameters;
	
    public String getParameters() {
        return parameters;
    }

    public void setParameters(String condition) {
        this.parameters = condition;
    }


	@Override
	public void onInitialize() {
		super.onInitialize();

		
		/**
		logger.debug("-------------------------------------------");
		for (Class<? extends com.novamens.service.EmailBuilder>  c:  ServiceLocator.getService(EmailService.class).getAllEmailBuilderClasses()) {
			logger.debug(c.getName());
		}
		logger.debug("-------------------------------------------");
		Map<String, Class<? extends com.novamens.service.EmailBuilder>> classes = ServiceLocator.getService(EmailService.class).getEmailBuilderKeyClassMap();
		for (Entry<String, Class<? extends com.novamens.service.EmailBuilder>> entry: classes.entrySet()) {
			logger.debug(entry.getKey()+" -> " + entry.getValue());
			try {
				com.novamens.service.EmailBuilder e=entry.getValue().newInstance();
				logger.debug(e.getArea());
				logger.debug(e.getKey());
				logger.debug(e.getParameters()!=null?e.getParameters().toString():"");
			} catch (InstantiationException | IllegalAccessException e) {
					logger.error(e);
			}
		}
		logger.debug("-------------------------------------------");
		**/
		
		setEditionEnabled(true);
		
		// ---------------------------
		//
		// person
		// rule
		// receiver
		// 
		// content
		// sender
		//
		// to, text
		//
		// ---------------------------
		
		
		form = new Form<Void>("form", Disposition.VERTICAL);
		
		// form.add(new StaticField<String>("description", new Model<String>(getModel().getObject().getDescription())));
		
		StringBuilder sa = new StringBuilder();
		
		try {
				com.novamens.email.EmailBuilder builder = getBuilder(getModel().getObject(), new HashMap<String, Object>());
				
				sa.append("<h3>Parameters required by the Email Builder </h3>");
				if (builder!=null) {
					for (Entry<String, Object> e: builder.getBuilderObjects().entrySet()) {
						sa.append(e.getKey());
						sa.append(" <br /> ");
					}
				}
				Label de = new Label("description", sa.toString());
				de.setEscapeModelStrings(false);
				form.add(de);
				
		} catch (Exception e) {
			Label de = new Label("description", e.getClass().getSimpleName() + " " + e.getMessage());
			form.add(de);
		}
		
		
		TextAreaField<String> tx= new TextAreaField<String>("parameters", new PropertyModel<String>(this, "parameters"), 3, 40) {
			
			private static final long serialVersionUID = 1L;
			
            @Override
            public void onUpdate(AjaxRequestTarget target) {
                super.onUpdate(target);
                setParameters(getValue());
            }

			@Override
			public boolean isHelpInfo() {
				return false;
			}
			
			@Override
			public void onHelp(AjaxRequestTarget target) {
				//getHelpModal().open(target, () -> { return "Email Template Marcos"; },	new Model<String>(getEmailTemplateMacros()));
			}
		};
		tx.setRequired(false);
		form.add(tx);

		add(form);
		
		add(new InvisiblePanel("email"));
		Label err 	= new Label("error", "");		
		addOrReplace(err);
		
		
		add(new EditButtonsV5<EmailTemplate>(this) {

			private static final long serialVersionUID = 1L;
			
			public void onEditClick(AjaxRequestTarget target) {
				super.onEditClick(target);
				EmailTemplateTestEditor.this.addOrReplace(new InvisiblePanel("email"));
				form.setVisible(true);
				Label err 	= new Label("error", "");		
				EmailTemplateTestEditor.this.addOrReplace(err);
				
			}
		
			protected IModel<String> getSubmitLabel() {
					return new Model<String>("Send");
			}
			@Override
			protected String getCancelClass() {
				return "btn btn-default btn-sm";
			}
			@Override
			protected String getSubmitClass() {
				return "btn btn-primary btn-sm";
			}
			protected String getEditClass() {
				return "btn btn-primary btn-sm";
			}
		});	
		
		add(new InfoDialog("help-modal"));
	}

	public void onClose(AjaxRequestTarget target) {
		
	}
	
	@Override
	public void cancel(AjaxRequestTarget target) {
		onCancel(target);
	}

	
	Map<String, Object> x_map;
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {

			
			form.setVisible(false);
			
			x_map = new HashMap<String, Object>();
							
			Person person = getContentDao().findUserProfileByUser(getSessionUser()).getPerson();
			
			if (getParameters()!=null) {
				String arr[] = getParameters().split("\n");
				for (String s:arr) {
					String kv[] = s.split("=");
					if (kv.length>1) {
						StringBuilder t=new StringBuilder();
						for( int n=1; n<kv.length;n++) {
							if (kv[n]!=null)
								t.append(kv[n].replace("\r", "").trim());
						}
						x_map.put(kv[0], t.toString());
					}
				}
			}

			
			// Support Ticket  ------------------------------------------
			//
			SupportTicket st = getSupportTicket();
			
			x_map.put("support-ticket-submitter", person.getId().toString());
			x_map.put("support-ticket-receiver", person.getEmail()); // this just a email address
			
			try {
				
				
				
				if (x_map.containsKey("support-ticket"))  {
					String pid= (String) x_map.get("support-ticket");
					SupportTicket ca=getContentDao().findSupportTicket(Long.valueOf(pid));
					if ((ca==null) ||  (!ca.getDomain().getId().equals(getDomain().getId())))
						x_map.remove("support-ticket");
				}
			} catch (Exception e) {
				logger.error(e);
				x_map.remove("support-ticket");
			}
			
			
			// Person, Receiver, Sender ------------------------------------------
			
			try {
				if (x_map.containsKey("person"))  {
					String pid= (String) x_map.get("person");
					Person ca=getContentDao().findPersonById(Long.valueOf(pid));
					if ((ca==null) ||  (!ca.getDomain().getId().equals(getDomain().getId())))
						x_map.remove("person");
				}
			} catch (Exception e) {
				logger.error(e);
				x_map.remove("person");
			}
			

			
			
			
			// Content ---------------------------------------------------------
			
			try {
				if (x_map.containsKey("content"))  {
					String pid= (String) x_map.get("content");
					Content ca=getContentDao().findContentById(Long.valueOf(pid));
					if ((ca==null) ||  (!ca.getDomain().getId().equals(getDomain().getId())))
						x_map.remove("content");
				}
			} catch (Exception e) {
				logger.error(e);
				x_map.remove("content");
			}
			
			if (!x_map.containsKey("content")) {
				Content cot = getTestContent();
				if (cot!=null)
					x_map.put("content", cot.getId().toString());				
			}

			
			
			
			
			// Rule ---------------------------------------------------------

			
			
			try {
				if (x_map.containsKey("rule"))  {
					String pid= (String) x_map.get("rule");
					ENotiRule ca=getENotiRuleDao().findENotiRuleById(Long.valueOf(pid));
					if ((ca==null) ||  (!ca.getDomain().getId().equals(getDomain().getId())))
						x_map.remove("rule");
				}
			} catch (Exception e) {
				logger.error(e);
				x_map.remove("rule");
			}
			
			if (!x_map.containsKey("rule")) {
				ENotiRule rut = getTestRule();
				if (rut !=null)
					x_map.put("rule", rut .getId().toString());				
			}
			
			// "alert-rule-publish-user"
			x_map.put("rulekey", getModel().getObject().getKey());


			x_map.put("notification_receiver", person.getId().toString());
			x_map.put("previous_task_person", person.getId().toString());
			x_map.put("task_assigned_to", person.getId().toString());
			x_map.put("task_trigered_by", person.getId().toString());
			x_map.put("task_executer", person.getId().toString());
			x_map.put("notification_receiver", person.getId().toString());
			x_map.put("previous_task_person", person.getId().toString());
			x_map.put("to", person.getEmail());
			x_map.put("domain", getDomain().getName());
			
			
			if (!x_map.containsKey("support-ticket")) 			{
				if (st!=null)
					x_map.put("support-ticket", st.getId().toString());
			}
			
			
			if (!x_map.containsKey("displayname")) 				{x_map.put("displayname", person.getFirstLastName());}
			if (!x_map.containsKey("person")) 					{x_map.put("person", person.getId().toString());}
			if (!x_map.containsKey("sender")) 					{x_map.put("sender", person.getId().toString());}
			if (!x_map.containsKey("text")) 					{x_map.put("text", "Upload your electronic documents into the system with a simple drag-and-drop or by browsing your computer files. Financial statements and reports can be shared with regulatory agencies, owners and other third-parties easily and securely. ");}
			if (!x_map.containsKey("filename")) 				{x_map.put("filename", "dbexport.zip");}
			if (!x_map.containsKey("language")) 				{x_map.put("language", getSessionUser().getLocale().getLanguage());}
			if (!x_map.containsKey("service_monitor_email")) 	{x_map.put("service_monitor_email", person.getEmail());}
			if (!x_map.containsKey("service_monitor_name")) 	{x_map.put("service_monitor_name", person.getFirstLastName());}
			if (!x_map.containsKey("publisher")) 				{x_map.put("publisher",  person.getId().toString());}
			if (!x_map.containsKey("subscriber")) 				{x_map.put("subscriber", person.getId().toString());}
			if (!x_map.containsKey("file")) 					{x_map.put("file",  null);}
			if (!x_map.containsKey("tasktrigeredby")) 			{x_map.put("tasktrigeredby",  person.getId().toString());}
			if (!x_map.containsKey("taskassignedto")) 			{x_map.put("taskassignedto", person.getId().toString());}
			if (!x_map.containsKey("notificationreceiver")) 	{x_map.put("notificationreceiver",  person.getId().toString());}
			if (!x_map.containsKey("previoustaskperson")) 		{x_map.put("previoustaskperson", person.getId().toString());}
			if (!x_map.containsKey("receiver")) 	   			{x_map.put("receiver", person.getId().toString());}
			if (!x_map.containsKey("taskexecuter")) 			{x_map.put("taskexecuter", person.getId().toString());}
			if (!x_map.containsKey("reportschedulename")) 	   	{x_map.put("reportschedulename", "Report Name");}
		 
			if (!x_map.containsKey("alertkey")) 	  		 	{x_map.put("alertkey",  EmailBuilder.MAX_TIME_RUNNING);}
			
			try {
				
				com.novamens.email.EmailBuilder builder = getBuilder(getModel().getObject(), x_map);
				 
				 if (builder!=null) {
					 	builder.setLanguage(getModel().getObject().getLanguage());
					 	EmailData data = builder.build();
					 	addOrReplace(new EmailFragment("email", data, builder.getBuilderObjects()));
						Label err 	= new Label("error", ""); EmailTemplateTestEditor.this.addOrReplace(err);
				 }
				 else {
						addOrReplace(new InvisiblePanel("email"));
						Label err 	= new Label("error", 
								"builder is null for key -> " + getModel().getObject().getKey() +" <br />Hint: check KbeeEmailTemplate.getEmailBuilderKeyClassMap() " 
								); EmailTemplateTestEditor.this.addOrReplace(err);
				 }
				} catch (Exception e) {
					logger.error(e);
					addOrReplace(new InvisiblePanel("email"));
					Label err 	= new Label("error", e.getClass().getName()+" "+ e.getMessage());	EmailTemplateTestEditor.this.addOrReplace(err);
				}
	
				super.reset();
				target.add(EmailTemplateTestEditor.this);
	
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
	
	
	
	private ENotiRule getTestRule() {
		List<ENotiRule> list = getENotiRuleDao().getENotiRules(getDomain());
		
		if (list!=null && list.size()>0) {
			long ra=System.currentTimeMillis()% ((long) list.size());
		
			for (ENotiRule c:list) {
				if (c.getState()==ObjectState.ENABLED) {
					
					if (getModel().getObject().getKey().endsWith("-user")) {
						if (!c.isSystem() && c.getOwner()!=null && c.getOwner().equals(getSessionUser()))
							return c;
					}
					else {
						if (c.isSystem())
							return c;
					}
				}	
			}
			return list.get((int)ra);

		}
		return null;
	}


	
	private SupportTicket getSupportTicket() {
		List<SupportTicket> list =getContentDao().getSupportTickets(getDomain());
		if (list.size()>0)
			return list.get(0);
		
		try {
		SupportTicket st = ServiceLocator.getService(ObjectFactoryService.class).createSupportTicket(getSessionUser(), "Contact us", "Please I need assistance");
		return st;
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
		
	}
	
	private Content getTestContent() {
		Content content =null;
		List<Content> list = getContentDao().getWorkspaceContents(getSessionUser(), true, 100);
		if (list!=null && list.size()>0) {
			for (Content c:list) {
				if (c.getState()==ObjectState.ENABLED && c.getContentClassification()!=null && c.getContentClassification().size()>0)
					return c;
			}
			long ra=System.currentTimeMillis()% ((long) list.size());
			content=list.get((int)ra);
			return content;
		}
		else {
			List<Content> libs = getContentDao().getContents(getDomain(), ObjectState.ENABLED, 100);
			if (libs!=null && libs.size()>0) {
				for (Content c:libs) {
					if (c.getContentClassification()!=null && c.getContentClassification().size()>0)
						return c;
				}
				long ra=System.currentTimeMillis()% ((long) list.size());
				content=list.get((int)ra);
				return content;
			}
		}
		return content;
	}
	
	
	public class EmailFragment extends Fragment {
		
		private static final long serialVersionUID = 1L;
		public EmailFragment(String id, EmailData data, Map<String, Object> pa) {
			super(id, "email-fragment", EmailTemplateTestEditor.this);
		 	
			Label e1	= new Label("from",  data.from);		addOrReplace(e1);
			Label e2 	= new Label("to",    data.to);		    addOrReplace(e2);
			Label e3 	= new Label("subject",  data.subject);	addOrReplace(e3);
			
			Label e4 	= new Label("text", (data.msg !=null?data.msg.replace("\n", "<br />"):""));		
			e4.setEscapeModelStrings(false);
			addOrReplace(e4);
			
			
		 	StringBuilder str = new StringBuilder();
		 	for (Entry<String, Object> e: pa.entrySet()) {
		 		if (str.length()>0)
					str.append("<br />");
		 		
		 		str.append(e.getKey()+" -> ");

		 		if (e.getValue() !=null) {
		 		
		 		if (e.getValue() instanceof Identifiable)
		 			str.append(((Identifiable) e.getValue()).getDisplayName());
		 		else
		 			str.append(e.getValue().toString());
		 		}
		 		else
		 			str.append("[null]");
		 	}

		 	
			Label e5 	= new Label("param", (pa!=null? str.toString():""));		
			e5.setEscapeModelStrings(false);
			addOrReplace(e5);
		}
	}

	
	
	/**
	 * 
	 * @param template
	 * @param map
	 * @return
	 * @throws ReflectiveOperationException 
	 * 
	 */

	
	
	protected  com.novamens.email.EmailBuilder getBuilder(EmailTemplate template, Map<String, Object> map) throws ReflectiveOperationException {
		
		
		Map<String, Class<? extends com.novamens.email.EmailBuilder>> classes = ServiceLocator.getService(EmailService.class).getEmailBuilderKeyClassMap();
		
		String key = template.getKey();
		
		if (key.equals("alert-rule-publish-domain")) {
			key ="alert-rule-publish-user";
		}
			
		if (key.equals("alert-rule-publish-user")) {
			key ="alert-rule-publish-user";
		}
		
		
		
		
		
		if (classes.containsKey(key)) {
			
			Class<? extends com.novamens.email.EmailBuilder> c=classes.get(key);
			
			com.novamens.email.EmailBuilder b;
			try {
				if (c!=null) {
					b = c.newInstance();
					b.setParameters(map);
					return b;
				}
				else {
					logger.debug("Class<? extends com.novamens.service.EmailBuilder> c=classes.get(key); is null -> " + key);
					return null;
				}
			} catch (InstantiationException | IllegalAccessException e) {
					logger.error(e);
					throw(e);
			}
		}
		logger.debug(template.getKey() + " > key not found");
		return null;

	}
	
	  protected User getSessionUser() {
	        return ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	    }
	  
	public IModel<String> getText(String key) {
		return new StringResourceModel(key, this, null);
	}
	
	
	protected void onCancel(AjaxRequestTarget target) {
		setEditionEnabled(false);
		target.add(this);
	}

	protected void onAfterSubmit(AjaxRequestTarget target) {
		setEditionEnabled(false);
		target.add(this);
	}
	
	protected Domain getDomain() {
		return (Domain)ServiceLocator.getService(UserService.class).getDomain();
	}

	protected void onUpdate(AjaxRequestTarget target) {
	}
	
	protected InfoDialog getHelpModal() {
		return (InfoDialog) get("help-modal");
	}
	
	protected IModel<String> getPredicatesHelp() {
		return new Model<String>(getDomain().getService(KbeeIqlHelpService.class).getPredicatesHelp());
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected ENotiRuleDao getENotiRuleDao() {
		return (ENotiRuleDao)ServiceLocator.getService(BeansService.class).getBean("enotiRuleDao");
	}
}
