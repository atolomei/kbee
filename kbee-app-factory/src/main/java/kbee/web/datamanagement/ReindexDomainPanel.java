package kbee.web.datamanagement;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
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
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.BooleanSwitchField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.NumberField;
import com.novamens.wicket.markup.html.form.OffsetDateTimeField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.command.CommandStatusPanelV5;
import kbee.web.command.panel.CommandModel;
import kbee.web.form.EditButtonsV5;

public class ReindexDomainPanel extends ObjectEditor<Domain> {

	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReindexContentPanel.class.getName());

	private IModel<OffsetDateTime> frommodel;
	private IModel<OffsetDateTime> tomodel;
	
	private IModel<String> sectionmodel = new Model<String>("All");
	private IModel<Boolean> attachmentsmodel = new Model<Boolean>(false);
	private IModel<Integer> maxmodel = new Model<Integer>(Integer.valueOf(100000));
	private IModel<Boolean>    custommodel = new Model<Boolean>(Boolean.valueOf(false));

	private IModel<KeyValue<String>> listmodel;
	private IModel<KeyValue<String>> domainmodel;

	final boolean is_root			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_service_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
	final boolean is_factory_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId());
	
	
	private boolean is_executing;
	
	private CommandStatusPanelV5 status_panel;
	
	
	public ReindexDomainPanel(String id) {
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

	
	/**
	 * 
	 */
	public void onInitialize() {
		super.onInitialize();

		is_executing  = false;
		
		Form<Void> form = new Form<Void>("form", Disposition.VERTICAL);
	 

		// ------------------
		// list domains
		//
		WebMarkupContainer lico= new WebMarkupContainer("list-container") {
			private static final long serialVersionUID = 1L;
			public boolean isVisible() {
				return !getCustommodel().getObject().booleanValue();
			}
		};
		
		listmodel = new Model<KeyValue<String>>(getQueries().get(0));
		lico.add(new ChoiceField<KeyValue<String>>("listmodel",  listmodel, 	new PropertyModel<List<KeyValue<String>>>(this, "queries"), true));
		form.add(lico);
		
		add(form);
		
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
		
		form.add(new InvisiblePanel("command-status"));
	}
	
	
	public void update(AjaxRequestTarget target) {
	

		Serializable command_id = null;
		
		String query = listmodel.getObject().getValue();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("query", query);
		CommandService service = ServiceLocator.getService(CommandService.class);
        Command command = (Command) ServiceLocator.getService(BeansService.class).getBean("ReindexBatchCommand");
        if (command != null) {
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
	                	target.add(ReindexDomainPanel.this);
	            }
	        };
	        ((Form) get("form")).addOrReplace(status_panel);
		}
        target.add(ReindexDomainPanel.this);
	}
	

	 
	
	List<KeyValue<String>>  lq=null;
	
	/***
	 * @return
	 */
	public List<KeyValue<String>> getQueries() {
	
		if (lq!=null)
			return lq;
		
		/**
		ZonedDateTime one_hour_earlier = ZonedDateTime.now().minusHours(1);
		ZonedDateTime two_hour_earlier = ZonedDateTime.now().minusHours(2);
		ZonedDateTime one_day_earlier = ZonedDateTime.now().minusDays(1);
		ZonedDateTime one_week_earlier = ZonedDateTime.now().minusDays(7);
		ZonedDateTime one_month_earlier = ZonedDateTime.now().minusMonths(1);
		ZonedDateTime one_year_earlier = ZonedDateTime.now().minusYears(1);
		ZonedDateTime two_year_earlier = ZonedDateTime.now().minusYears(2);
		
		DateTimeFormatter df =	DateTimeFormatter.ofPattern ("yyyy-MM-dd'T'HH:mm:ssXXX");
		logger.debug(df.format(ZonedDateTime.now()));
		**/
		/**
		String all_str  	 = "from KbeeContent";
		String one_hour_str  = "from KbeeContent where lastModifiedDate >= '"+ df.format(one_hour_earlier)+"'";
		String two_hour_str  = "from KbeeContent where lastModifiedDate >= '"+ df.format(two_hour_earlier)+"'";
		String one_day_str   = "from KbeeContent where lastModifiedDate >= '"+ df.format(one_day_earlier)+"'";
		String one_week_str  = "from KbeeContent where lastModifiedDate >= '"+ df.format(one_week_earlier)+"'";
		String one_month_str = "from KbeeContent where lastModifiedDate >= '"+ df.format(one_month_earlier)+"'";
		String one_year_str  = "from KbeeContent where lastModifiedDate >= '"+ df.format(one_year_earlier)+"'";
		String two_year_str  = "from KbeeContent where lastModifiedDate >= '"+ df.format(two_year_earlier)+"'";
		**/
		
		lq  =new ArrayList<KeyValue<String>>();
		
		
		if (isKbeeDomain())
			lq.add(new KeyValue<String>("Model - Domains",            "from KbeeDomain"));
		lq.add(new KeyValue<String>("Model - DataSet Values",      "from KbeeDataSetMember" +" " +((isKbeeDomain()? " where domain.id="+getDomain().getId().toString():"")) ));
		lq.add(new KeyValue<String>("Model - Information Model",   "info-model"));
		
		if (isKbeeDomain())
			lq.add(new KeyValue<String>("Domain - All Domains", 		 "domain-all"));
		
		if (isKbeeDomain()) {
			for (Domain d:getContentDao().getDomains())
				lq.add(new KeyValue<String>("Domain - " + d.getName(), "domain-"+String.valueOf(d.getId())));
		}
		
		// lq.add(new KV<String>("Attachments - All",    "from KbeeContent where workspace is not null"));
		
		if (logger.isDebugEnabled())
			lq.forEach(item -> logger.debug(item.value));
		
		return lq;
		
	}

	private void indexDomains() {
		
	}

	private void indexModel() {
		
		
	}
	
	
	private void indexDomain(Domain domain) {
		
	}
	
	
	
	/**
	 * 
	 * 
	 * @return

	public List<KV<String>> getDomains() {
		List<KV<String>> queries =new ArrayList<KV<String>>();
		queries.add(new KV<String>("All", "all"));
		for (Domain domain : getContentDao().getDomains()) 
			queries.add(new KV<String>(domain.getName() +" - " + domain.getOrganization()+" ", domain.getId().toString()));
		
		queries.sort(new Comparator<KV<String>>() {
			@Override
			public int compare(KV<String> a, KV<String> b) {
				if (a.getKey().equals("All"))
					return -1;
				if (b.getKey().equals("All"))
					return 1;
				return a.getKey().toString().compareToIgnoreCase(b.getKey().toString());
			}
		});
		return queries;
	}
	 */

	
	
	public  IModel<KeyValue<String>> getDomainmodel() {
		return domainmodel;
	}

	public void setDomainmodel( IModel<KeyValue<String>> domainmodel) {
		this.domainmodel = domainmodel;
	}


	public IModel<OffsetDateTime> getFrommodel() {
		return frommodel;
	}


	public void setFrommodel(IModel<OffsetDateTime> frommodel) {
		this.frommodel = frommodel;
	}


	public IModel<OffsetDateTime> getTomodel() {
		return tomodel;
	}


	public void setTomodel(IModel<OffsetDateTime> tomodel) {
		this.tomodel = tomodel;
	}


	public IModel<String> getSectionmodel() {
		return sectionmodel;
	}


	public void setSectionmodel(IModel<String> sectionmodel) {
		this.sectionmodel = sectionmodel;
	}


	public IModel<Boolean> getAttachmentsmodel() {
		return attachmentsmodel;
	}


	public void setAttachmentsmodel(IModel<Boolean> attachmentsmodel) {
		this.attachmentsmodel = attachmentsmodel;
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


}
