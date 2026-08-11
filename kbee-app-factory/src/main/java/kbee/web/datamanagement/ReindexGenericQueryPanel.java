package kbee.web.datamanagement;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.novamens.beans.BeansService;
import com.novamens.content.command.Command;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.properties.Property;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.content.command.mt.QueueProcessorCommand;
import com.novamens.kbee.content.user.UserPropertyService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.NumberField;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.command.CommandStatusPanelV5;
import kbee.web.command.panel.CommandModel;
import kbee.web.form.EditButtonsV5;
import kbee.web.iql.KbeeIqlHelpService;
import kbee.web.user.UserQueryHistoryPanel;


@SuppressWarnings("serial")
public class ReindexGenericQueryPanel extends ObjectEditor<Domain> {
			
	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReindexGenericQueryPanel.class.getName());
    
	private static final long serialVersionUID = 1L;
	
	final boolean is_root			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_service_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
	final boolean is_factory_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId());

	private boolean is_executing;
	
	private CommandStatusPanelV5 status_panel;
	private String condition;
	
	private Long th;
	private Long batchsize;
	private boolean attachments = true;
	
	public Long getThreads() {
		return this.th;
	}
	
	
	public void setThreads(Long m) {
		this.th=m;
	}
	
	
    class IqlValidator implements IValidator<String> {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

        @Override
        public void validate(final IValidatable<String> validatable) {
            String statement = validatable.getValue();
            try {
                if (statement == null || "".equals(statement))
                    return;
                IqlService iqlservice = getDomain().getService(IqlService.class);
                ResultSet set = iqlservice.execute(statement);
                set.hasNext();
            } catch (RuntimeException e) {
                logger.error(e);
                validatable.error(new ValidationError(this));
            }
        }
    }

    
	public ReindexGenericQueryPanel(String id) {
		super(id);
		this.setOutputMarkupId(true);
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		Long MAX_THREADS;
		
		try {

			String m=getContentDao().findSystemParameterValueByKey("queue.processor.max-threads", String.valueOf(Math.round(Math.floor(Double.valueOf( Runtime.getRuntime().availableProcessors())*0.8))));
				
			MAX_THREADS = Long.valueOf(m);
			
				if (MAX_THREADS>48)					MAX_THREADS = Long.valueOf(48);
				else if (MAX_THREADS<1)				MAX_THREADS = Long.valueOf(1);
				
		} catch (Exception e) {
				logger.error(e);
				MAX_THREADS = Long.valueOf(5);	
		}
		
		batchsize	=	QueueProcessorCommand.BATCH_SIZE;
		
		

		
		setThreads(MAX_THREADS);
		
		is_executing  = false;
		
		Form<Void> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.setOutputMarkupId(true);
		
        add(new InfoDialog("help-modal"));

        //
        // Hibernate Querty
        //
        /**
         * from KbeeContent where domain.id in (10754,4950,7600,7150,5901,3157) and id > 87921722-10000000 and id <= 87921722
         * 
         */
 		final TextAreaField<String> condition = new TextAreaField<String>("condition", new PropertyModel<String>(this, "condition"), true, Field.Width.W12, null, 3, 3) {
            @Override
			public void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, new Model<String>("Hibernate Query"), () -> { return new StringResourceModel("condition.help" , ReindexGenericQueryPanel.this, null).getObject(); });
			}
			@Override
			public boolean isHelpInfo() {
				return true;
			}
            @Override
            public void onUpdate(AjaxRequestTarget target) {
                super.onUpdate(target);
                setCondition(getValue());
            }
        };
        form.add(condition);
        

        NumberField<Long> th_f= new NumberField<Long>("threads", new PropertyModel<Long>(this, "threads")) {
			@Override
        	public void onUpdate(AjaxRequestTarget target) {
				try {
						setThreads(getValue());
				} catch (Exception e) {
					logger.error(e);
				}
        	}
        };
        
        				
        NumberField<Long> bs_f= new NumberField<Long>("batchsize", new PropertyModel<Long>(this, "batchsize")) {
			@Override
        	public void onUpdate(AjaxRequestTarget target) {
				try {
						setBatchsize(getValue());
						
				} catch (Exception e) {
					logger.error(e);
				}
        	}
        };
        
        form.add(bs_f);

        
        BooleanField at_f= new BooleanField("attachments", new PropertyModel<Boolean>(this, "attachments")) {
			@Override
        	public void onUpdate(AjaxRequestTarget target) {
				setAttachment(getValue());
        	}
        };


        form.add(at_f);
        
        
        th_f.setRequired(true);
        form.add(th_f);
        
        form.add(new UserQueryHistoryPanel("user-history", getHistoryKey(), new ObjectModel<Person>(getPerson())) {
			private static final long serialVersionUID = 1L;
			@Override
            public boolean isVisible() {
                return ReindexGenericQueryPanel.this.isIQLVisible();
            }
            protected void apply(AjaxRequestTarget target, IModel<Property> model) {
                setCondition(model.getObject().getValue().toString());
                condition.setValue(model.getObject().getValue().toString());
                form.addOrReplace(condition);
                target.add(form);
            }
        });

		
		form.add(new EditButtonsV5<Domain>(this, false) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return is_root || (isKbeeDomain() &&  (is_domain_admin  || is_factory_admin || is_service_admin));
			}
			
			@Override
			public boolean isEnabled() {
				if (is_executing)
					return false;
				return is_root || (isKbeeDomain() &&  (is_domain_admin  || is_factory_admin || is_service_admin));
			}
			
			@Override
			protected IModel<String> getSubmitLabel() {
				return new Model<String>("execute");
			}
			
			@Override
			protected String getCancelClass() {
				return "btn btn-default btn-sm";
			}
			
			@Override
			protected String getSubmitClass() {
				return "btn btn-primary btn-sm";
			}
		});
		
		
		add(form);
		form.add(new InvisiblePanel("command-status"));
	}
	
		
	public void setBatchsize(Long value) {
		this.batchsize=value;
	}
	
	public long getBatchsize() {
		return this.batchsize;
	}
	
	public void setAttachment(boolean value) {
		this.attachments = value;
	}
	
	public boolean getAttachments() {
		return this.attachments;
	}

	public void update(AjaxRequestTarget target) {

		Serializable command_id = null;
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("query", getCondition());
		
		map.put("batch-size", String.valueOf(getBatchsize()));
		map.put("max-threads", String.valueOf(getThreads().intValue()));
		map.put("max-threads", String.valueOf(getThreads().intValue()));
		map.put("include-attachments", getAttachments() ? "true" : "false");
		
		// com.novamens.kbee.content.command.mt.BatchReindexCommand
		CommandService service = ServiceLocator.getService(CommandService.class);
        Command command = (Command) ServiceLocator.getService(BeansService.class).getBean("ReindexBatchCommand");
        if (command != null) {
        	command.setParameters(map);
        	service.add(command);
        	is_executing  = true;
        	command_id = command.getId();
        }
        
        
		if (command_id!=null) {
			
			saveUserHistory(getHistoryKey(), getCondition());
			
			status_panel = new CommandStatusPanelV5("command-status", new CommandModel(ServiceLocator.getService(CommandService.class).getCommand(command_id))) {
				private static final long serialVersionUID = 1L;
				@Override
	            public void onAfterExecution(AjaxRequestTarget target) {
	                	is_executing = false;
		                	try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
						}
	                	target.add(ReindexGenericQueryPanel.this);
	            }
	        };
	        ((Form<?>) get("form")).addOrReplace(status_panel);
		}

        target.add(ReindexGenericQueryPanel.this);
        
	}

	  public String getCondition() {
	        return condition;
	    }

	    public void setCondition(String condition) {
	        this.condition = condition;
	    }
	    

	public void onDetach() {
		super.onDetach();
		if (status_panel!=null)
			status_panel.detach();
	}

	Boolean is_kbee_domain;
	
	protected boolean isKbeeDomain() {
	
		if (this.is_kbee_domain == null) {
			try {
				this.is_kbee_domain = Boolean.valueOf(getDomain().getName().toLowerCase().trim().equals("kbee"));
			} 
			catch (Exception e) {
				logger.error(e);
				this.is_kbee_domain = Boolean.valueOf(false);
			}
		}
		return this.is_kbee_domain.booleanValue();
	}
	

    protected boolean isIQLVisible() {
        return true;
    }
    
    
	protected Domain getDomain() {
        return (Domain) ServiceLocator.getService(UserService.class).getDomain();
    }
	
    protected User getSessionUser() {
        return ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
    }
    
    protected InfoDialog getHelpModal() {
        return (InfoDialog) get("help-modal");
    }

    protected IModel<String> getPredicatesHelp() {
        return new Model<String>(getDomain().getService(KbeeIqlHelpService.class).getPredicatesHelp());
    }

    protected ContentDao getContentDao() {
        return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
    }

	
    protected String getHistoryKey() {
        return "reindexgenericquery";
    }

    protected Person getPerson() {
        return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
    }

    
    
    
protected void saveUserHistory(String key, String text) {
		
		if (text==null)
			return;
		
		try {
			List<Property> list = ((KbeeUser) getSessionUser()).getService(UserPropertyService.class).getPropertiesSet(key, 30);
			for (Property p:list) {
					if (p.getValue().toString().toLowerCase().trim().equals(text.toLowerCase().trim()))
							return;
			}
			DateTimeFormatter dt= DateTimeFormatter.ISO_DATE_TIME;
			((KbeeUser) getSessionUser()).getService(UserPropertyService.class).setProperty(key+"-"+dt.format(OffsetDateTime.now()), key, text);
		} catch (Exception e) {
			logger.error(e);
		}
	}

/**
 

 from KbeeContent where domain.id in (10754,4950,7600,7150,5901,3157) and id > 87921722-10000000 and id <= 87921722
 from KbeeContent
 
 from KBFileImpl where lastModifiedDate >= '2020-09-02T20:45:20Z'
 
 from KbeeContent where workspace is not null
 from KbeeContent where workspace is null
 from KbeeContent where lastModifiedDate >= '2020-09-02T20:45:20Z'
 from KbeeContent C where C.lastModifiedDate >= '2020-08-03T00:00:00-03:00' and C.lastModifiedDate <= '2020-09-03T00:00:00-03:00'
 from KbeeDomain
 from KbeeDataSetMember
 from KbeeDataSet
 from KbeeAttribute
 from KbeeClassifier
 from KbeeContentTemplate
 from KbeePerson 
 from KbeeUserLabel
 from KbeeBillboard
 from KbeeTreeIDoc
 from KbeeOrganizationalText
 
 --------------------------
 from KbeeGroup
 from KbeeUser
 from KbeeRole
 from KbeeSecurityRule
 from KbeeENotiRule
 --------------------------
 
  inworkspace:true
  type:idoc or type:text 
 
 */

	

}
