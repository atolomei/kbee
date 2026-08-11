package kbee.web.datamanagement;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.service.DomainService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.logging.usage.LogUsageServiceRequest;
import com.novamens.kbee.scheduler.CronServiceRequestWrapper;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.SchedulerService;
import com.novamens.scheduler.ServiceRequest;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.InfoDialog;

import kbee.web.command.CommandStatusPanelV5;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorPanel;
import kbee.web.form.EditButtonsV5;


/**
 * 
 * <p>
 * In order for a ServiceRequest to be executed from this panel, it must have a default Constructor
 * List of Requests
 * Parameters
 * </p>
 *
 */			
public class SchedulerRequestExecutePanel extends ObjectEditor<Domain> {
			
	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SchedulerRequestExecutePanel.class.getName());

    private static  List<Class<? extends ServiceRequest>> 			_list;
    private static   Map<Class<? extends ServiceRequest>, String> 	_map;
    
    /**
    static {
    	_map.put(LogUsageServiceRequest.class, PARENT_PATH);
    }**/
    
    
	final boolean is_root			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_service_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
	final boolean is_factory_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId());

	
	private boolean is_executing;
	
	private CommandStatusPanelV5 status_panel;
	
	private  Boolean is_kbee_domain;
	
	private IModel<Class<? extends ServiceRequest>> sv_model;
	private Map<Class<? extends ServiceRequest>, String> map;
	private List<Class<? extends ServiceRequest>> list = null;
	private Form<Void> form;
	
    private String parameters;
    
	/**
	 * @param id
	 */
	public SchedulerRequestExecutePanel(String id) {
		super(id);
		setOutputMarkupId(true);
	}
	

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String condition) {
        this.parameters = condition;
    }

    
    
    
	public List<Class<? extends ServiceRequest>> getAllServiceRequestClasses() {
		
		if (list!=null)
			return list;
	
		this.list = new ArrayList<Class<? extends ServiceRequest>>();
		this.map = new HashMap<Class<? extends ServiceRequest>, String>();
	
		 if (_list!=null) {
			 this.list.addAll(_list);
			 this.map.putAll(_map);
			 return list;
		}
		

		 _list = ServiceLocator.getService(SchedulerService.class).getAllServiceRequestClasses();
		 		 
		_map  = new HashMap<Class<? extends ServiceRequest>, String>();

		_list.forEach(item ->   
			{ 
				if (!Modifier.isAbstract(item.getModifiers())) { 
						_map.put(item, item.getName());
				}
			} 
		);
		
		this.list.addAll(_list);
		this.map.putAll(_map);
		
		return list;
		
	}
	
		
	public IModel<Class<? extends ServiceRequest>> getServiceRequestModel() {
		return this.sv_model;
	}
	
	public void setServiceRequestModel(IModel<Class<? extends ServiceRequest>> b) {
		this.sv_model = b;
	}
	
	public void onDetach() {
		super.onDetach();
		if (status_panel!=null)
			status_panel.detach();
	}

	
	
	/**
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();

		is_executing  = false;
		
		form = new Form<Void>("form", Disposition.VERTICAL);
		
		
		form.add(new InvisiblePanel("sent"));
		
		try {

			
				sv_model = new Model<Class<? extends ServiceRequest>>(getAllServiceRequestClasses().get(0));
				
				org.apache.wicket.model.util.ListModel<Class<? extends ServiceRequest>> list = new org.apache.wicket.model.util.ListModel<Class<? extends ServiceRequest>>(getAllServiceRequestClasses());
				
				
				
				ChoiceField< Class<? extends ServiceRequest>> servicerequests = new ChoiceField< Class<? extends ServiceRequest>>("sv_model",sv_model, list, true) {
					private static final long serialVersionUID = 1L;
		
					
					@Override			
					public void  onUpdate(AjaxRequestTarget target) {
						super.onUpdate(target);
						StringBuilder str = new StringBuilder();
						
						if (getValue()!=null) {
							 
							str.append(getValue().getName());
							
							 if (map.containsKey(getValue())) {
								 str.append(" (CronJob)");
							 }
						}
						
						//((StaticField<String>) form.get("description")).setValue(str.toString());
						target.add(SchedulerRequestExecutePanel.this);
					}
					
					
					@Override			
					protected String getDisplayValue(Class<? extends ServiceRequest> value) {
						if (value!=null) {
							 if (map.containsKey(value)) {
								 return value.getName() + " (CronJob)";
							 }
							 else
								 return value.getName();
						}
						return "";
					}
				};
				
				form.add(servicerequests);
				
		} catch (Exception e) {
			logger.error(e);
			form.add( new ErrorPanel("sv_model", e));
			
		}
		

		/***
		form.add((new StaticField<String>("description", new Model<String>() {
			private static final long serialVersionUID = 1L;
			public String getObject() {
				return ((ChoiceField< Class<? extends ServiceRequest>>) form.get("sv_model")).getValue().getName();
				
				if (sv_model!=null && sv_model.getObject()!=null) {
					StringBuilder str = new StringBuilder();
					 str.append(sv_model.getObject().getName());
					 if (map.containsKey(sv_model.getObject())) {
						 str.append(" (CronJob)");
					 }
					 return str.toString();
				}
				return "";
			}
		})));
			
									**/
		
        final TextAreaField<String> param = new TextAreaField<String>("parameters", new PropertyModel<String>(this, "parameters"), 3, 3) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onUpdate(AjaxRequestTarget target) {
                super.onUpdate(target);
                setParameters(getValue());
            }

            @Override
            public boolean isHelpInfo(){
                return true;
            }

            @Override
            public boolean isRequired() {
                return false;
            }

            @Override
            public void onHelp(AjaxRequestTarget target) {
                getHelpModal().open(target,  new StringResourceModel("parameters.help",SchedulerRequestExecutePanel.this, null) );
            }
        };

		form.add(param);
		add(form);
		
		
		form.add(new EditButtonsV5<Domain>(this, false) {
			
			private static final long serialVersionUID = 1L;
			
			public void onEditClick(AjaxRequestTarget target) {
				super.onEditClick(target);
				 Label se=new Label("sent", "<span>" + "" + "</span>");
				 se.setEscapeModelStrings(false);
				 form.replace(se);
			}
				
			@Override
			public boolean isVisible() {
				return isKbeeDomain() &&  (is_domain_admin  || is_factory_admin || is_service_admin);
			}
			
			@Override
			public boolean isEnabled() {
				if (is_executing)
					return false;
				return isKbeeDomain() &&  (is_domain_admin  || is_factory_admin || is_service_admin);
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
		
		
		
		add(new InfoDialog("help-modal"));
		form.add(new InvisiblePanel("command-status"));
	}

	
	/**
	 * 
	 * 
	 */
	
	public void update(AjaxRequestTarget target) {

		if (sv_model==null || sv_model.getObject()==null)
			return;
		
		Map<String, String> x_map = new HashMap<String, String>();
			if (getParameters()!=null) {
				String arr[] = getParameters().split("\n");
				for (String s:arr) {
					String kv[] = s.split("=");
					if (kv.length>1) {
						StringBuilder t=new StringBuilder();
						for( int n=1; n<kv.length;n++) {
							if (kv[n]!=null)
								t.append(kv[n].replace("\r", ""));
						}
						x_map.put(kv[0], t.toString());
					}
				}
			}
		
			 try {
			
				 ServiceRequest serv = (ServiceRequest) sv_model.getObject().newInstance();
				 serv.setParameters(x_map);
				 
				 logger.debug(serv.getClass().getName());
				 logger.debug(x_map.toString());
				 
				 Serializable r_id;

				 Label se;
				 try {
				 if (serv instanceof AbstractCronJobRequest) 
					 r_id = getDomain().getService(DomainService.class).enqueueRequest(new CronServiceRequestWrapper((AbstractCronJobRequest) serv));
				 else
					 r_id = getDomain().getService(DomainService.class).enqueueRequest(serv);
				 
				 se=new Label("sent", "<span>" + serv.toString() + " <br />  Request id -> " + (r_id!=null?r_id.toString():"null") + " </span>");
				 
				 } catch (Exception e) {
					 se=new Label("sent", "<span>" + e.getClass().getName() + " | " + e.getMessage() + " </span>");
				 }
				 
				 se.setEscapeModelStrings(false);
				 form.replace(se);
				
			} catch (Exception e1) {
				logger.error(e1);
				setResponsePage(new ApplicationErrorPage<Void>(e1));
			}
			

		target.add(SchedulerRequestExecutePanel.this);
        
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



}
