package kbee.web.datamanagement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.beans.BeansService;
import com.novamens.content.command.Command;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KeyValue;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.command.CommandStatusPanelV5;
import kbee.web.command.panel.CommandModel;
import kbee.web.error.ErrorPanel;
import kbee.web.form.EditButtonsV5;


/**
 * 
 */			
public class ReindexCleanIndexesPanel extends ObjectEditor<Domain> {
			
	private static final long serialVersionUID = 1L;

	final boolean is_root			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_service_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
	final boolean is_factory_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId());
	
	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReindexContentPanel.class.getName());

	private boolean is_executing;
	
	private IModel<KeyValue<String>> listmodel;
	
	private CommandStatusPanelV5 status_panel;
	
	/***
	 * 
	 * @param id
	 */
	public ReindexCleanIndexesPanel(String id) {
		super(id);
		this.setOutputMarkupId(true);
	}
	
	/**
	 * 
	 */
	public IModel<KeyValue<String>> getListmodel() {
		return this.listmodel;
	}
	

	/**
	 * 
	 */
	public void setListmodel(IModel<KeyValue<String>> b) {
		this.listmodel = b;
	}
	
	
	/**
	 * 
	 */
	public void onDetach() {
		super.onDetach();
		if (status_panel!=null)
			status_panel.detach();
	}
	

	/**
	 * 
	 */
	public void onInitialize() {
		super.onInitialize();

			is_executing  = false;
			
			
			
			Form<Void> form = new Form<Void>("form", Disposition.VERTICAL);

			add(form);
			form.add(new InvisiblePanel("error"));
			
			
			form.add(new InvisiblePanel("command-status"));
			listmodel = new Model<KeyValue<String>>(getQueries().get(0));
			
			form.add(new ChoiceField<KeyValue<String>>("listmodel",  listmodel, 	new PropertyModel<List<KeyValue<String>>>(this, "queries"), true));
			
			form.add(new EditButtonsV5<Domain>(this, false) {
				private static final long serialVersionUID = 1L;
				
				@Override
				protected IModel<String> getSubmitLabel() {
					return new Model<String>("execute");
				}
				
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
			});
			
	}

	
	 
	@SuppressWarnings("unchecked")
	public void update(AjaxRequestTarget target) {
		
		Serializable command_id = null;
		String query = listmodel.getObject().getValue();
		String index = listmodel.getObject().getLink();
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("statement", query);
		map.put("domain", getDomain().getId().toString());
		
		if (index!=null)
			map.put("index", index);
		
		CommandService service = ServiceLocator.getService(CommandService.class);
		Command command  =null;
		
		try {
			command = (Command) ServiceLocator.getService(BeansService.class).getBean("CleanIndexCommand");
	        // Command command = (Command) ServiceLocator.getService(BeansService.class).getBean("CleanBatchCommand");
		} catch (Exception e) {
        	((Form) get("form")).addOrReplace(new ErrorPanel("error", e));			
		}
        
        if (command != null) {
        	logger.debug(command.getClass().getName());
	        	command.setParameters(map);
	        	service.add(command);
	        	is_executing  = true;
	        	command_id = command.getId();
        }
		
        if (command_id!=null) {
			status_panel = new CommandStatusPanelV5("command-status", new CommandModel(ServiceLocator.getService(CommandService.class).getCommand(command_id))) {
				private static final long serialVersionUID = 1L;
				@Override
	            public void onAfterExecution(AjaxRequestTarget target) {
	                	is_executing = false;
	                	target.add(ReindexCleanIndexesPanel.this);
	            }
	        };
	        ((Form<Void>) get("form")).addOrReplace(status_panel);
		}
        
        target.add(ReindexCleanIndexesPanel.this);
	}


	
	/**
	 * 
	 */
	private Boolean is_kbee_domain;

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

	protected Domain getDomain() {
        return (Domain) ServiceLocator.getService(UserService.class).getDomain();
    }
	
    protected User getSessionUser() {
        return ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
    }
    
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	List<KeyValue<String>>  lq=null;
	
	/***
	 * @return
	 */
	public List<KeyValue<String>> getQueries() {
	
		if (lq!=null)
			return lq;
		
		lq  =new ArrayList<KeyValue<String>>();
		
		lq.add(new KeyValue<String>("Content (all)", "(type:idoc OR type:text)"));
		lq.add(new KeyValue<String>("DataSet Values", "type:datasetmember"));
		lq.add(new KeyValue<String>("Security (users, groups, roles, rules)", "((type:user)  OR  (type:group)  OR  (type:role)  OR  (type:rule))"));
		lq.add(new KeyValue<String>("Security (Users)", "(type:user)"));
		lq.add(new KeyValue<String>("Security (Groups)", "(type:group)"));
		lq.add(new KeyValue<String>("Security (Role)", "(type:role)"));
		lq.add(new KeyValue<String>("Site", "type:site"));
		lq.add(new KeyValue<String>("Domain", "type:domain"));
		
		lq.add(new KeyValue<String>("KBFile", "type:kbfile", "file"));
		
		lq.add(new KeyValue<String>("Billboard", "type:billboard"));
		lq.add(new KeyValue<String>("Library", "type:library"));
		lq.add(new KeyValue<String>("ListItem", "type:useritem"));
		lq.add(new KeyValue<String>("¨Person", "type:person"));
		

		
		// treeidoc
		lq.add(new KeyValue<String>("Workspaces", "inworkspace:true"));
		lq.add(new KeyValue<String>("Model", "((type:template)  OR  (type:dataset)  OR  (type:classifier)  OR  (type:attribute))"));
		
		
		lq.sort(new Comparator<KeyValue<String>>() {
			@Override
			public int compare(KeyValue<String> a, KeyValue<String> b) {
				return a.getKey().toString().compareToIgnoreCase(b.getKey().toString());
			}
		});
		
		
		
		
		
		
		if (logger.isDebugEnabled())
			lq.forEach(item -> logger.debug(item.value));
		
		return lq;
	}

}
