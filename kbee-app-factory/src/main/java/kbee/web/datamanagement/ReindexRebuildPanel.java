package kbee.web.datamanagement;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.command.Command;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KeyValue;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.command.CommandStatusPanelV5;
import kbee.web.command.panel.CommandModel;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class ReindexRebuildPanel extends ObjectEditor<Domain> {
	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReindexContentPanel.class.getName());

	private  Boolean is_kbee_domain;
	
	private IModel<Boolean>    custommodel = new Model<Boolean>(Boolean.valueOf(false));

	private IModel<KeyValue<String>> listmodel;

	final boolean is_root			= ServiceLocator.getService(SecurityService.class).isRoot(); 
	final boolean is_domain_admin 	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_service_admin 	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
	final boolean is_factory_admin 	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId());
	
	private boolean is_executing;
	
	private CommandStatusPanelV5 status_panel;

	public ReindexRebuildPanel(String id) {
		super(id);
		this.setOutputMarkupId(true);
	}
	
	public IModel<Boolean> getCustommodel() {
		return this.custommodel;
	}
	
	public void setCustommodel(IModel<Boolean> b) {
		this.custommodel = b;
	}
	
	public IModel<KeyValue<String>> getListmodel() {
		return this.listmodel;
	}
	
	public void setListmodel(IModel<KeyValue<String>> b) {
		this.listmodel = b;
	}
	
	public void onDetach() {
		super.onDetach();
		if (status_panel!=null)
			status_panel.detach();
	}
	
	public void onInitialize() {
		super.onInitialize();

		is_executing  = false;
		
		Form<Void> form = new Form<Void>("form", Disposition.VERTICAL);
		
		add(form);
		
		form.add(new EditButtonsV5<Domain>(this, false) {
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
		
		form.add(new InvisiblePanel("command-status"));
	}

	@SuppressWarnings({ "rawtypes" })
	public void update(AjaxRequestTarget target) {

		Serializable command_id = null;
		
		
		List<String> statements = new ArrayList<String>();
		
		String stm;
		String domaincriteria = " where domain.id="  + getDomain().getId();
		
				
		// Domain  
		stm = "from KbeeDomain";
		statements.add(stm); 
		
		// Security  

		stm = "from KbeeGroup";
		if (!isKbeeDomain()) stm += domaincriteria;
		statements.add(stm);

		stm = "from KbeeSecurityRule";
		if (!isKbeeDomain()) stm += domaincriteria;
		statements.add(stm);
		
		stm = "from KbeeAbstractRole";
		if (!isKbeeDomain()) stm += domaincriteria;
		statements.add(stm);
				

		// Model
		stm = "from KbeeDataSet";
		if (!isKbeeDomain()) stm += domaincriteria;
		statements.add(stm);
		
		stm = "from KbeeClassifier";
		if (!isKbeeDomain()) stm += domaincriteria;
		statements.add(stm);

		stm = "from KbeeAttribute";
		if (!isKbeeDomain()) stm += domaincriteria;
		statements.add(stm);
		
		stm = "from KbeeContentTemplate";
		if (!isKbeeDomain()) stm += domaincriteria;
		statements.add(stm);


		stm = "from KbeeEmailTemplate";
		if (!isKbeeDomain()) stm += domaincriteria;
		statements.add(stm);

		stm = "from KbeeLibrary";
		if (!isKbeeDomain()) stm += domaincriteria;
		statements.add(stm);
		
		
		// Members
		stm = "from KbeeDataSetMember";
		if (!isKbeeDomain()) stm += domaincriteria;
		statements.add(stm);

		// Content ---------------------------------------------------
		
		//stm = "from KbeeIDoc";
		//if (!isKbeeDomain()) stm += domaincriteria;
		//statements.add(stm);
		
		
		int currentYear = OffsetDateTime.now().getYear();
		
		
		for (int in = 2016; in <= currentYear; in++) {
			stm = "from KbeeIDoc";
			String timeClause = " lastModifiedDate < '" + String.valueOf(in+1) +"-01-01T00:00:00Z' AND lastModifiedDate >=   '" + String.valueOf(in) + "-01-01T00:00:00Z' ";
			
			if (!isKbeeDomain()) {
				stm += domaincriteria;
				stm += " AND " + timeClause ;
			}
			else {
				stm += " WHERE " + timeClause;
			}
	
			statements.add(stm);
		}
		 
		
		// -------------------------------------------------------------
		
		
		
				
				
		
		stm = "from KbeeBillboard";
		if (!isKbeeDomain()) stm += domaincriteria;
		statements.add(stm);
		
		stm = "from KbeeOrganizationalText";
		if (!isKbeeDomain()) stm += domaincriteria;
		statements.add(stm);
		
		stm = "from KbeeActivityProgressNote";
		if (!isKbeeDomain()) stm += domaincriteria;
		statements.add(stm);
		
		stm = "from KbeeLibrary";
		if (!isKbeeDomain()) stm += domaincriteria;
		statements.add(stm);
		
		stm = "from KbeeSite";
		if (!isKbeeDomain()) stm += domaincriteria;
		statements.add(stm);

		stm = "from KbeeUserListItem";
		if (!isKbeeDomain()) stm += domaincriteria;
		statements.add(stm);
		
		

		statements.forEach( item -> logger.debug(item));
		
		
		Map<String, Object> parameters = new HashMap<String, Object>();

		parameters.put("statements", statements);
		parameters.put("batch-size", "80");
		
		int processors = Runtime.getRuntime().availableProcessors();
		parameters.put("max-threads", String.valueOf((processors>1? processors-1 : 1)));
		
		CommandService service = ServiceLocator.getService(CommandService.class);
		
		// <bean id="ReindexAllBatchCommand" class="com.novamens.kbee.content.command.mt.BatchReindexCommand" scope="prototype">
		
		Command command = (Command) ServiceLocator.getService(BeansService.class).getBean("ReindexAllBatchCommand");
		if (command != null) {
			
			logger.debug(command.getClass().getName());
			
			command.setParameters(parameters);
	        service.add(command);
	        is_executing  = true;
	        command_id = command.getId();
		}
		
		if (command_id!=null) {
			status_panel = new CommandStatusPanelV5("command-status", new CommandModel(ServiceLocator.getService(CommandService.class).getCommand(command_id))) {
				@Override
	            public void onAfterExecution(AjaxRequestTarget target) {
	               	
					is_executing = false;
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
					
	               	target.add(ReindexRebuildPanel.this);
	            }
	        };
	        ((Form) get("form")).addOrReplace(status_panel);
		}
        target.add(ReindexRebuildPanel.this);
	}

	protected Domain getDomain() {
        return (Domain) ServiceLocator.getService(UserService.class).getDomain();
    }
	
    protected User getSessionUser() {
        return ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
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
