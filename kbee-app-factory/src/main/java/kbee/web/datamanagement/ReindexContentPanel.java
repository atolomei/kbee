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
import com.novamens.wicket.markup.html.form.BooleanField;
import com.novamens.wicket.markup.html.form.BooleanSwitchField;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.form.NumberField;
import com.novamens.wicket.markup.html.form.OffsetDateTimeField;

import kbee.web.command.CommandStatusPanelV5;
import kbee.web.command.panel.CommandModel;
import kbee.web.form.EditButtonsV5;

/**
 * 
 */
public class ReindexContentPanel extends ObjectEditor<Domain> {
			
	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReindexContentPanel.class.getName());

	private IModel<OffsetDateTime> frommodel;
	private IModel<OffsetDateTime> tomodel;

	private  Boolean is_kbee_domain;
	
	private IModel<String> sectionmodel = new Model<String>("All");
	private IModel<Boolean> attachmentsmodel = new Model<Boolean>(true);
	private IModel<Long> maxmodel = new Model<Long>(Long.valueOf(100000));
	private IModel<Boolean>    custommodel = new Model<Boolean>(Boolean.valueOf(false));

	private IModel<KeyValue<String>> listmodel;
	private IModel<KeyValue<String>> domainmodel;

	final boolean is_root			= ServiceLocator.getService(SecurityService.class).isRoot(); 
	final boolean is_domain_admin 	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_service_admin 	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SERVICE_ADMIN.getId());
	final boolean is_factory_admin 	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_FACTORY_MANAGER.getId());
	
	private boolean is_executing;
	
	private CommandStatusPanelV5 status_panel;

	public ReindexContentPanel(String id) {
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
		
		form.add(new BooleanSwitchField("custommodel",	custommodel) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				setCustommodel( new Model<Boolean>(getValue()));
				target.add(ReindexContentPanel.this);
			}
			
			@Override
			public boolean isBorder() {
				return true;
			}
		});

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

		// ------------------
		// cuco
		//
		WebMarkupContainer cuco= new WebMarkupContainer("custom-container") {
			private static final long serialVersionUID = 1L;
			public boolean isVisible() {
				return getCustommodel().getObject().booleanValue();
			}
		};
		
		form.add(cuco);		
		
		frommodel =  new Model<OffsetDateTime>(OffsetDateTime.now().minusDays(30));
		tomodel =  new Model<OffsetDateTime>(OffsetDateTime.now());

		domainmodel = new Model<KeyValue<String>>(getDomains().get(0));
		
		cuco.add(new ChoiceField<KeyValue<String>> ("domainmodel",  domainmodel, 	new PropertyModel<List<KeyValue<String>>>(this, "domains"), true) {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return isKbeeDomain();
			}
		});

		sectionmodel = new Model<String>(getSections().get(0));
		
		cuco.add(new ChoiceField<String> 	 ("sectionmodel",  sectionmodel, 	new PropertyModel<List<String>>(this, "sections"), true));
		cuco.add(new OffsetDateTimeField	 ("frommodel",  getSessionUser().getZoneId(), frommodel, true));
		cuco.add(new OffsetDateTimeField	 ("tomodel",  getSessionUser().getZoneId(), tomodel, true));
		cuco.add(new BooleanField 	  		 ("attachmentsmodel",	attachmentsmodel ));

		cuco.add(new NumberField<Long> 	 ("maxmodel", 	maxmodel) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				try {
					maxmodel.setObject(getValue());
				} catch (Exception e) {
					logger.error(e);
				}
			}
		});
		
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

	
	/**
	 * 
	 * 
	 */
	@SuppressWarnings({ "rawtypes", "serial" })
	public void update(AjaxRequestTarget target) {

		Serializable command_id = null;
		
		if (custommodel.getObject().booleanValue()) {
			
			Map<String, Object> map = new HashMap<String, Object>();
			
			StringBuilder str =new StringBuilder();
			str.append("from KbeeContent C where ");
			
			if (!isKbeeDomain()) {
				str.append(" C.domain.id="  + getDomain().getId().toString() + " and ");
			}
			else {
				if (!domainmodel.getObject().getValue().toLowerCase().equals("all")) {
					str.append(" C.domain.id="  + domainmodel.getObject().getValue() + " and ");
				}
			}
			
			DateTimeFormatter df =	DateTimeFormatter.ofPattern ("yyyy-MM-dd'T'HH:mm:ssXXX");
			
			ZonedDateTime z_from = frommodel.getObject().atZoneSameInstant(getSessionUser().getZoneId());
			ZonedDateTime z_to   = tomodel.getObject().truncatedTo(ChronoUnit.DAYS).plusDays(1).atZoneSameInstant(getSessionUser().getZoneId());;
			
			str.append(" C.lastModifiedDate >= '"+ df.format(z_from)+"'  and " );
			str.append(" C.lastModifiedDate <= '"+ df.format(z_to)+"' " );
		
			
			if (sectionmodel.getObject().toLowerCase().equals("all")) {
				
			}
			else if (sectionmodel.getObject().toLowerCase().equals("workspace")) {
				str.append(" and C.workspace is not null " );
			}
			else {
				str.append(" and C.workspace is null " );
			}
				
			if (maxmodel.getObject()!=null) {
				
				if (maxmodel.getObject().longValue()>0) {
					logger.debug(maxmodel.getObject().longValue());
					map.put("limit", String.valueOf(Long.valueOf(maxmodel.getObject())));
				}
				else
					map.remove("limit"); 
			}
			
			map.put("include-attachments", attachmentsmodel.getObject() ? "true" : "false");
			
			logger.debug(str.toString());
			
			map.put("query", str.toString());
			
			CommandService service = ServiceLocator.getService(CommandService.class);
	        Command command = (Command) ServiceLocator.getService(BeansService.class).getBean("ReindexBatchCommand");
	        if (command != null) {
	        	command.setParameters(map);
	        	service.add(command);
		        try {
						Thread.sleep(800);
					} catch (InterruptedException e) {
				}
	        	command_id = command.getId();
	        }
		}
		else {
			String query = listmodel.getObject().getValue();
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("query", query);
			map.put("max-threads", "3");
			CommandService service = ServiceLocator.getService(CommandService.class);
	        Command command = (Command) ServiceLocator.getService(BeansService.class).getBean("ReindexBatchCommand");
	        if (command != null) {
	        	command.setParameters(map);
	        	service.add(command);
	        	is_executing  = true;
	        	command_id = command.getId();
	        }
		}

		if (command_id!=null) {
			status_panel = new CommandStatusPanelV5("command-status", new CommandModel(ServiceLocator.getService(CommandService.class).getCommand(command_id))) {
				@Override
	            public void onAfterExecution(AjaxRequestTarget target) {
	               	is_executing = false;
	               	try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
						
						
					}
	               	target.add(ReindexContentPanel.this);
	            }
	        };
	        ((Form) get("form")).addOrReplace(status_panel);
		}
        target.add(ReindexContentPanel.this);
	}
	

	public List<String> getSections() {
		List<String> sections =new ArrayList<String>();
		sections.add("All");
		sections.add("Libraries");
		sections.add("Workspace");
		return sections;
	}
	
	List<KeyValue<String>>  lq=null;
	
	/***
	 * @return
	 */
	public List<KeyValue<String>> getQueries() {
	
		if (lq!=null)
			return lq;
		
		ZonedDateTime one_hour_earlier = ZonedDateTime.now().minusHours(1);
		ZonedDateTime two_hour_earlier = ZonedDateTime.now().minusHours(2);
		ZonedDateTime one_day_earlier = ZonedDateTime.now().minusDays(1);
		ZonedDateTime one_week_earlier = ZonedDateTime.now().minusDays(7);
		ZonedDateTime one_month_earlier = ZonedDateTime.now().minusMonths(1);
		ZonedDateTime one_year_earlier = ZonedDateTime.now().minusYears(1);
		ZonedDateTime two_year_earlier = ZonedDateTime.now().minusYears(2);
		
		DateTimeFormatter df =	DateTimeFormatter.ofPattern ("yyyy-MM-dd'T'HH:mm:ssXXX");
		logger.debug(df.format(ZonedDateTime.now()));
		
								
		String all_str  	 = "from KbeeContent" + ( !isKbeeDomain()?  (" where domain.id="+ getDomain().getId().toString() ): "");
		
		String one_hour_str  = "from KbeeContent where lastModifiedDate >= '"+ df.format(one_hour_earlier) +"' "+ ( !isKbeeDomain()?  (" and domain.id="+ getDomain().getId().toString() ): "");
		String two_hour_str  = "from KbeeContent where lastModifiedDate >= '"+ df.format(two_hour_earlier) +"' "+ ( !isKbeeDomain()?  (" and domain.id="+ getDomain().getId().toString() ): "");
		String one_day_str   = "from KbeeContent where lastModifiedDate >= '"+ df.format(one_day_earlier)  +"' "+ ( !isKbeeDomain()?  (" and domain.id="+ getDomain().getId().toString() ): "");
		String one_week_str  = "from KbeeContent where lastModifiedDate >= '"+ df.format(one_week_earlier) +"' "+ ( !isKbeeDomain()?  (" and domain.id="+ getDomain().getId().toString() ): "");
		String one_month_str = "from KbeeContent where lastModifiedDate >= '"+ df.format(one_month_earlier)+"' "+ ( !isKbeeDomain()?  (" and domain.id="+ getDomain().getId().toString() ): "");
		String one_year_str  = "from KbeeContent where lastModifiedDate >= '"+ df.format(one_year_earlier) +"' "+ ( !isKbeeDomain()?  (" and domain.id="+ getDomain().getId().toString() ): "");
		String two_year_str  = "from KbeeContent where lastModifiedDate >= '"+ df.format(two_year_earlier) +"' "+ ( !isKbeeDomain()?  (" and domain.id="+ getDomain().getId().toString() ): "");
		
		lq  =new ArrayList<KeyValue<String>>();
		
		lq.add(new KeyValue<String>("Content - All - Workspaces", "from KbeeContent where workspace is not null" + (!isKbeeDomain()?  (" and domain.id="+ getDomain().getId().toString() ): "") ));
		lq.add(new KeyValue<String>("Content - All - Libraries", "from KbeeContent where workspace is null" + ( !isKbeeDomain()?  (" and domain.id="+ getDomain().getId().toString() ): "")));
		lq.add(new KeyValue<String>("Content - All - Workspaces and Libraries", "from KbeeContent" + ( !isKbeeDomain()?  (" and domain.id="+ getDomain().getId().toString() ): "")));
		
		lq.add(new KeyValue<String>("Content - Last modified - 1 hour", one_hour_str));
		lq.add(new KeyValue<String>("Content - Last modified - 2 hours", two_hour_str));
		lq.add(new KeyValue<String>("Content - Last modified - 1 day", one_day_str));
		lq.add(new KeyValue<String>("Content - Last modified - 1 week", one_week_str));
		
		lq.add(new KeyValue<String>("Content - Last modified - 1 month", one_month_str));
		lq.add(new KeyValue<String>("Content - Last modified - 1 year", one_year_str));
		lq.add(new KeyValue<String>("Content - Last modified - 2 years", two_year_str));
		lq.add(new KeyValue<String>("Content - All", all_str));
		
		if (isKbeeDomain()) {
			
			lq.add(new KeyValue<String>("DataSetMember", "from KbeeDataSetMember"));
			
			lq.add(new KeyValue<String>("Model - DataSet", "from KbeeDataSet"));
			lq.add(new KeyValue<String>("Model - Classifier", "from KbeeClassifier"));
			lq.add(new KeyValue<String>("Model - ContentTemplate", "from KbeeContentTemplate"));
			lq.add(new KeyValue<String>("Model - Attribute", "from KbeeAttribute"));
			
			lq.add(new KeyValue<String>("Security - User", "from KbeeUser"));
			lq.add(new KeyValue<String>("Security - Group", "from KbeeGroup"));
			lq.add(new KeyValue<String>("Security - Role", "from KbeeAbstractRole"));
			lq.add(new KeyValue<String>("Security - Person", "from KbeePerson"));
		}
		
		if (logger.isDebugEnabled())
			lq.forEach(item -> logger.debug(item.value));
		
		return lq;
		
	}

	
	public List<KeyValue<String>> getDomains() {
		List<KeyValue<String>> queries =new ArrayList<KeyValue<String>>();
		queries.add(new KeyValue<String>("All", "all"));
		for (Domain domain : getContentDao().getDomains()) 
			queries.add(new KeyValue<String>(domain.getName() +" - " + domain.getOrganization()+" ", domain.getId().toString()));
		
		queries.sort(new Comparator<KeyValue<String>>() {
			@Override
			public int compare(KeyValue<String> a, KeyValue<String> b) {
				if (a.getKey().equals("All"))
					return -1;
				if (b.getKey().equals("All"))
					return 1;
				return a.getKey().toString().compareToIgnoreCase(b.getKey().toString());
			}
		});
		return queries;
	}
	
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