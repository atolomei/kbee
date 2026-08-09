package kbee.web.workflow;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.StringValue;

import com.novamens.beans.BeansService;
import com.novamens.content.form.EFormData;
import com.novamens.content.workflow.WorkflowDao;
import com.novamens.kbee.content.support.Tip;
import com.novamens.kbee.content.workflow.KbeeWorkflowActivity;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.service.ServiceLocator;
import com.novamens.site.logging.SiteStatInEvent;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Activity;

import kbee.web.page.ErrorPageEvent;
import kbee.web.page.KbeeWebPage;

public class ResolutionLetterViewPage extends KbeeWebPage<Void> {

	
	private static final long serialVersionUID = 1L;
	
	
	private static final ResourceReference ICONS_CSS = new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/css/icons/icomoon/styles.css");
	
	private static final ResourceReference COMPONENTS_CSS = new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/css/components.css");
	private static final ResourceReference CORE_CSS = new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/css/core.css");
	private static final ResourceReference APP_JS = new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/js/core/app.js");
	
	private static final ResourceReference BOOTSTRAP_JS = new JavaScriptResourceReference(Form.class, Form.BOOTSTRAP_JS);
	private static final ResourceReference BOOTSTRAP_CSS = new CssResourceReference(Form.class, Form.BOOTSTRAP);
	
	private static final ResourceReference KBEE_BOOTSTRAP_CSS = new CssResourceReference(AbstractKbeeWebPage.class, "kbeebootstrap.css");
	
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_0_360      = new CssResourceReference(AbstractKbeeWebPage.class, "kb-0-360.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_361_800    = new CssResourceReference(AbstractKbeeWebPage.class, "kb-361-800.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_801_1200   = new CssResourceReference(AbstractKbeeWebPage.class, "kb-801-1200.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1201_1600  = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1201-1600.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1601  	 = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1601.css");

	private static final ResourceReference AW = new CssResourceReference(Form.class, Form.FONTAWESOME);
	private static final ResourceReference CSS_KBEE_LIMITLESS = new CssResourceReference(AbstractKbeeWebPage.class, "kbee-limitless.css");
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger( EFormViewerPage.class.getName());

	static private Logger siteStatslogger = LogManager.getLogger("SiteStats");
	
	static final String TIP_CATEGORY = Tip.GENERAL;
	
	private String name;
	private long start, end;
	private boolean is_log_visit = false;
	private Panel footer = null;
	private boolean footer_is_null = false;
	private boolean has_footer = false;
	private  boolean visit_logged = false;
	
	private String initial_tab;
	
	private IModel<Activity> activitymodel;
		
	private WebMarkupContainer left_panel;
	private WebMarkupContainer right_panel;
	
	
	public ResolutionLetterViewPage(PageParameters param) {
		super();

		this.activitymodel 	= new ObjectModel<Activity>(getActivity(param));
		setName(activitymodel.getObject().getResolutionTitle()!=null?activitymodel.getObject().getResolutionTitle():" Letter");
		addListeners();
	}
	

	/**
	 * @param activitymodel
	 * @param datamodel
	 */
	public ResolutionLetterViewPage(IModel<Activity> activitymodel) {
		this.activitymodel=activitymodel;
		setParameters();
		setName(activitymodel.getObject().getResolutionTitle()!=null?activitymodel.getObject().getResolutionTitle():" Letter");
		addListeners();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		activitymodel.getObject().getId().toString();
		
		WebMarkupContainer left_panel = new WebMarkupContainer("left-panel");
		WebMarkupContainer right_panel = new WebMarkupContainer("right-panel");
		add(left_panel);
		
		
		Label t=new Label("title", getName());
		left_panel.add(t);
		
		left_panel.add(new ResolutionPreviewPanel("letter", new Model<String>(this.activitymodel.getObject().getResolution()), false));
		add(right_panel);
		right_panel.add(new   AuditActivityInfoPanel("activity-panel", this.activitymodel));
	}

	@Override
	public void onDetach() {
		super.onDetach();
		
		if (this.activitymodel!=null)
			this.activitymodel.detach();
	}
	
	/**
	 */
	@Override
	protected ResourceReference getCssResource() {
		return KBEE_BOOTSTRAP_CSS;
	}
	
	
	protected boolean hasPermissions() {
		return false;
	}

	
	
	protected void addListeners() {
		
		
		add(new WicketEventListener<ErrorPageEvent>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(ErrorPageEvent event) {
				//AbstractApplicationPage.this.setInfoTopPanel(new PageErrorPanel("info-panel", event.getThrowable()));
				//AbstractApplicationPage.this.refreshInfoArea(event.getRequestTarget());
			}
		});
		
		/**
		add(new WicketEventListener<ErrorEvent>() {
			@Override
			public void onEvent(ErrorEvent event) {
				logger.error(event.getThrowable());
				IModel<String> titlemodel = new Model<String>("Error");
				IModel<String> messagemodel = new Model<String>("<h3>"+event.getThrowable().getClass().getSimpleName() +"</h3><br/><p> " + event.getThrowable().getMessage()+"</p>");
				((ErrorDialog) getErrorDialog()).open(event.getRequestTarget(), titlemodel, messagemodel);
			}
		});
		**/
	}	
	
 
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(ICONS_CSS));
		
		response.render(CssHeaderItem.forReference(COMPONENTS_CSS));
		
		response.render(JavaScriptHeaderItem.forReference(APP_JS));
		response.render(CssHeaderItem.forReference(CORE_CSS));
		
		response.render(CssHeaderItem.forReference(BOOTSTRAP_CSS));
		response.render(JavaScriptHeaderItem.forReference(BOOTSTRAP_JS));

		
		response.render(CssHeaderItem.forReference(AW));
		
		response.render(CssHeaderItem.forReference(KBEE_BOOTSTRAP_CSS));
		response.render(CssHeaderItem.forReference(CSS_KBEE_LIMITLESS));
		
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_0_360));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_361_800));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_801_1200));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1201_1600));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1601));
	}
	

	protected String getTipCategory() {
		return TIP_CATEGORY;
	}
	
	protected void setName(String name) {this.name=name;}

	protected String getName() {return this.name;}
	 
	protected String getPageType() {return "page";}  				// con | det | det-version | task 	 
	protected String getContentTitle() {return null;} 				// content title or user title, ...

	protected String getStatsPageTitle() {return getName();} 		// for console page, it is the name of the console 
	private Long getStatsPageId() {return Long.valueOf(0);}         // for console page, it is the name of the console
														
	protected String getObjectId() {return null;}    				// for user, domain, ...
	protected String getContentId() {return null;}	 				// for content
	protected Serializable getContentOId() {return null;}	 		// for content
	protected Serializable getCId() {return null;}	 				// for content
	
	/**
	 * page_type = "Con"
	 * Site id = Console id ?
	 * Site Name = nombre de la consola
	 * 
	 * Content ID 
	 * Content OID
	 * Version 
	 */
	protected void logVisit() {
		
		// --------------------------------------------------------------------------
		// Agregar la info al log de Stat
		//

		if (isVisitLogged())
			return;
		
		
		try {

			SiteStatInEvent stat = new SiteStatInEvent();

			// stat.domain_id = Long.valueOf(getDomain().getId().toString());
			
			stat.sessionId = WebSession.get().getId();
			
			stat.page_type = getPageType();
			
			stat.site_id = Long.valueOf(getApplicationMenuSection().getId()); // Section (security, tasks)
			stat.site_title = getApplicationMenuSection().getKey();       // Section

			// for console page, it is the name of the console
			//
			stat.page_id =  getStatsPageId();
			stat.page_title = getStatsPageTitle();
			
			//stat.user_id = Long.valueOf(getSessionUser().getId().toString());
			//stat.user_name = getSessionUser().getFirstLastName();
			stat.timestamp = OffsetDateTime.now();

			stat.user_agent = ((WebRequest) getRequest()).getHeader("User-Agent");
			stat.sessionId = WebSession.get().getId();
			
			stat.render_milisecs = Long.valueOf(end - start);

			 stat.content_title = getContentTitle(); 	// content title or user/domain/dataset title
			 stat.contentId  =  getContentId(); 			// for Content
			 
			 stat.content_long_id = getCId()!=null ? (Long) getCId() : Long.valueOf(0);
			 stat.OId  =  getContentOId()!=null ? getContentOId().toString() : null; 			
			 stat.objectId  =  getObjectId();  			// for User, Domain, DataSet, etc.
			 
			//stat.content_version = getContentVersion() !=null ? getContentVersion() : Integer.valueOf(0);

			// Para que se logue en la Base de Datos
			// El logger de la Clase debe grabar en
			// el Appender "SiteStats"
			siteStatslogger.info(stat);
		} 
		catch (Exception e) {
			logger.error(e);
		}
		finally {
			this.setVisitLogged(true);
			
		}
	}
	
	
	protected void setVisitLogged(boolean b) {
		this.visit_logged = b;
	}
	protected boolean isVisitLogged() {
		return this.visit_logged;
	}
	
	private WorkflowDao getWorkflowDao() {
        return (WorkflowDao) ServiceLocator.getService(BeansService.class).getBean("WorkflowDao");
    }
	
	
	private void setParameters() {
		
		if (this.activitymodel!=null && this.activitymodel.getObject()!=null)
			getPageParameters().add("activityid", this.activitymodel.getObject().getId().toString());
		
		//if (this.datamodel!=null && this.datamodel.getObject()!=null)
		//	getPageParameters().add("ename", this.datamodel.getObject().getForm().getName());
		
	}
	
	
	private Activity getActivity(PageParameters parameters) {
		StringValue a_id = parameters.get("activityid");
		if (a_id!=null) {
			Activity a = getWorkflowDao().findActivityById(Long.valueOf(a_id.toString()));
			return a;
		}
		
		return null;
		
	}
	

}
