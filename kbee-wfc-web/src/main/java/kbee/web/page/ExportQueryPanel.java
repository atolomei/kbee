package kbee.web.page;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.content.user.UserService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.content.command.ExportContentsCommand;
import com.novamens.kbee.content.service.datamanagement.QuerySizeEstimator;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;

import kbee.web.command.CommandStatusPanelV5;
import kbee.web.command.panel.CommandModel;

/**
 *  1. Calcular espacio  estimado CANCEL
 *  2. Confirmar 
 *  3. Generar y enviar x mail
 *
 */
public class ExportQueryPanel extends ModelPanel<Content> {

	private static final long serialVersionUID = 1L;
	
	final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());

	
	static private int INITIALIZING 	= 1;
	static private int DATA_TOO_LARGE 	= 3;
	static private int EMAIL_DISABLED	= 4;
	static private int STARTED		 	= 5;
	
	private int status = INITIALIZING;

	private Query query;
	
	private double export_size = -1;
	
	private CommandStatusPanelV5 status_panel;
	private boolean is_executing;
	private boolean done=false;
	
	private Serializable command_id = null;
	private String filePath = null;
	
	WebMarkupContainer main;
	
	
	public ExportQueryPanel(String id, Query query) {
		super(id);
		this.query=query;
		setOutputMarkupId(true);
		is_executing  = false;
		
	}
	
	/** 
	 * Export Query
	 * 
	 * [Info]
	 * 1.a Total Contents:
	 * Files:
	 * Size:
	 * 
	 * -------------------------------------------------------------
	 * 
	 * 2. [Alert] The Query is too large.
	 * 3. [Alert] We will send an email with a link to the zip file when done, You can safely close this window.
	 * 
	 */
	@SuppressWarnings("serial")
	@Override
	public void onInitialize() {
		super.onInitialize();
	
		String smax;
		Double max;
	
		String mode = getDomain().getService(DomainSettingsService.class).get(DomainSettingsService.EMAIL_SERVICE_STATUS);
		
		boolean is_active;
		
		if (mode==null)
			is_active = true;	
		else
			is_active = mode.equals("enabled") || mode.equals("yes");
		
		
		done = false;
		
		if (!is_active) 
			setState(EMAIL_DISABLED);
		
			if (is_root) {
				smax = getContentDao().findSystemParameterValueByKey("zip.export.limit.gb.root",  "15");
				try {
					max=Double.valueOf(smax);
				} catch (java.lang.NumberFormatException e) {
					max=Double.valueOf(15);
				}
			}
			else if (is_domain_admin) {
				smax= getContentDao().findSystemParameterValueByKey("zip.export.limit.gb.admin", "10");
				try {
					max=Double.valueOf(smax);
				} catch (java.lang.NumberFormatException e) {
					max=Double.valueOf(10);
				}
			}
			else if (is_support) {
				setState(DATA_TOO_LARGE);
				max=Double.valueOf(0);
			}
			else {
				smax= getContentDao().findSystemParameterValueByKey("zip.export.limit.gb.user",   "1");
				try {
					max=Double.valueOf(smax);
				} catch (java.lang.NumberFormatException e) {
					max=Double.valueOf(1);
				}
			}
		
		if (getExportSize()>max)
				setState(DATA_TOO_LARGE);
											
		IModel<String> sttm = getLabel("too-big", String.format("%4.0f", getExportSize()), String.valueOf(max));
		IModel<String> em = getLabel("email-disabled");

		
		main = new WebMarkupContainer("main");
		main.setOutputMarkupId(true);
		add(main);
			
		
		
		main.add((new Label("alert-too-big", sttm) {
			public boolean isVisible() {
				return getState()==DATA_TOO_LARGE;
			}
		}).setEscapeModelStrings(false));
							
		
		main.add((new Label("email-disabled", em) {
			public boolean isVisible() {
				return getState()==EMAIL_DISABLED;
			}
		}).setEscapeModelStrings(false));
		
		IModel<String> exem = getLabel("executing", String.valueOf(getExportSize()));
		
		main.add((new Label("alert-executing", exem) {
			public boolean isVisible() {
				return getState()==STARTED;
			}
		}).setEscapeModelStrings(false));

		main.add (new Link<Void>("close") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() {
					onClose();
			}
			@Override
			public boolean isVisible() {
				return (getState()==STARTED || getState()==DATA_TOO_LARGE); 
			}
		});
	
		ExportContentsCommand command = null;
		
		if (getState()!=DATA_TOO_LARGE && getState()!=EMAIL_DISABLED) {
			
			CommandService service = ServiceLocator.getService(CommandService.class);
			command = new ExportContentsCommand();
			
			command.setQuery(getQuery());
			command.setDomain(ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain());
			command.setUserId(ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser().getId());
			service.add(command);
			
			setState(STARTED);
			
			if (command!=null) {
				command_id  = command.getId();
				status_panel = new CommandStatusPanelV5("command-status", new CommandModel(ServiceLocator.getService(CommandService.class).getCommand(command.getId()))) {
					private static final long serialVersionUID = 1L;
					@Override
		            public void onAfterExecution(AjaxRequestTarget target) {
		                	is_executing = false;
		                	done = true;
                			ExportContentsCommand cm = (ExportContentsCommand) ServiceLocator.getService(CommandService.class).getCommand(command_id);
                			
		                	filePath = cm.getZipPath();
		                	
		                	try {
								Thread.sleep(400);
							} catch (InterruptedException e) {
							}
		                	
		                	addLink();
		                	
		                	target.add(ExportQueryPanel.this);
		            }
		        };
		        main.addOrReplace(status_panel);
			}
		
			addLink();
		}
		else {
			main.addOrReplace(new InvisiblePanel("command-status"));
			main.addOrReplace(new InvisiblePanel("link-container"));
		}
		
			
	}
	

	
	private void addLink() {

		WebMarkupContainer mc = new WebMarkupContainer("link-container") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return true;
			}
		};
		
		main.addOrReplace(mc);
		
		if (filePath!=null) {
			
			IModel<File> filemodel = new Model<File>() {
				public File getObject() {
					try {
						File file = new File(filePath);
						return file;
					}
					catch (Exception e) {
						return null;
					}		
				}
			};
			
			File zfile = new File(filePath);
			
			String si=ServiceLocator.getService(DateTimeService.class).formatFileSize(zfile.length(), getSessionUser().getLocale(), "ago");
			Link<?> link = new DownloadLink("link",  filemodel, new Model<String>(zfile.getName())) {
				private static final long serialVersionUID = 1L;
			};
			mc.addOrReplace(link);
			
			Label name = new Label("name",new Model<String>(zfile.getName() + "  (" + si +  ")" ) );
			name.setEscapeModelStrings(false);
			link.addOrReplace(name);
			
		}
		else {
			mc.addOrReplace( new InvisiblePanel("link"));
		}
	}
	
	
	
	
	
	protected void setState(int state) {
		this.status=state;
	}
	
	protected int getState() {
		return this.status;
	}

	protected void onClose() {}
	
	public boolean isSizeGreatedThan( double gigas) {
		if (this.export_size>0) {
			return this.export_size>gigas;
		}
		return false;
	}
	
	public class InfoPanel extends Fragment {

		private static final long serialVersionUID = 1L;

		public InfoPanel(String id, String markupId, MarkupContainer markupProvider) {
			super(id, markupId, markupProvider);
		}
		
		@Override
		public void onBeforeRender() {
			super.onBeforeRender();
		}
	}
	
	public class AlertPanel extends Fragment {
		private static final long serialVersionUID = 1L;
		public AlertPanel(String id, String markupId, MarkupContainer markupProvider) {
			super(id, markupId, markupProvider);
			
		}
		
		@Override
		public void onBeforeRender() {
			super.onBeforeRender();
		}
	}
	
	protected IModel<String> getLabel(String key) {
		return new StringResourceModel(key, this, null);
	}

	protected IModel<String> getLabel(String key, String... parameter) {
		StringResourceModel model = new StringResourceModel(key, this);
		model.setParameters((Object[]) parameter);
		return model;
	}

	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	private Query getQuery() {
		return query;
	}
	
	private double getExportSize() {
		if (this.export_size>0)
			return this.export_size;
		QuerySizeEstimator qes = new QuerySizeEstimator(getQuery());
		this.export_size = qes.getTotalSpace();
		return this.export_size;
	}
}