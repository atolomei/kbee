package kbee.web.content.panel;


import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.library.Library;
import com.novamens.content.library.LibraryService;
import com.novamens.content.security.ContentSystemSecurityService;

import com.novamens.content.workflow.WorkflowService;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.ProxyUtil;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Activity;

import kbee.web.command.panel.CommandAttributePanelV5;
import kbee.web.panel.AlertPanel;

/**
 * 
 * Library
 * Source
 * 
 * OId - v -Id
 * External Id
 * 
 *
 * @param <T>
 */
public class ContentLibraryPanel<T extends Content> extends ModelPanel<T> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ContentLibraryPanel.class.getName());
	
	private List<Panel> panels;

	private IModel<Site> site_model;
	private boolean isConsole = false;
	
	boolean isHistory = true;
	
	public ContentLibraryPanel(String id, IModel<T> model, IModel<Site> siteModel, boolean isConsole) {
			this(id, model, siteModel, isConsole, true);
	}
	
	
	public ContentLibraryPanel(String id, IModel<T> model, IModel<Site> siteModel, boolean isConsole, boolean isHistory) {
		super(id, model);
		setOutputMarkupId(true);
		this.site_model=siteModel;
		this.isConsole=isConsole;
		this.isHistory=isHistory;
		
	}

	public void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer history_container = new WebMarkupContainer("history-container");
		
		history_container.setVisible( isHistory && isWriteable(getModel()));
		add(history_container);
		
		HistoryPanel<T> pa=new HistoryPanel<T>("versions", getModel(), getSiteModel(), this.isConsole );
		pa.setVisible(pa.getHistory()!=null && pa.getHistory().size()>0);
		history_container.add(pa);
		
		/**
		WebMarkupContainer no_history = new WebMarkupContainer("no-history");
		no_history.setVisible(!pa.isVisible());
		history_container.add(no_history);
		**/
		
		AlertPanel<Void> no=new AlertPanel<Void>("no-history",AlertPanel.INFO,  null, 
				null, 
				getLabel("nohistory"));
		no.setIcon(AlertPanel.HELP_INFO);
		no.setVisible(!pa.isVisible());
		history_container.add(no);
		
		List<Panel> _list = getPanels();
		
		add(new ListView<Panel>("result",  _list) {
			private static final long serialVersionUID = 1L;
			protected void populateItem(ListItem<Panel> item){
				item.setOutputMarkupId(true);
				item.add(item.getModelObject());
				item.setVisible(item.getModelObject().isVisible());
			}
		});
	}
	
	public List<Panel> getPanels() {

		if (this.panels!=null)
		return this.panels;
		
	this.panels = new ArrayList<Panel>();
		
	String libraryName = "";
	
	if (getModelObject().isEnabled()) {
		List<Library> libraries = getModelObject().getDomain().getService(LibraryService.class).getLibraries(getModelObject());
		
		if (!libraries.isEmpty()) 
			libraryName = libraries.get(0).getDisplayName();
	}
	else if (getModelObject().isArchived()) { 
		libraryName = getLabelModel("archive").getObject() ;
	}
	else if (getModelObject().isRecycled()) { 
		libraryName = getLabelModel("recycle").getObject();
	}
	
	String vers= "v"+ String.valueOf(getModelObject().getVersion()).trim();
	String v=  String.valueOf(getModelObject().getId()) +" / " + String.valueOf(getModelObject().getOId()) ;
	
	OffsetDateTime date = getModelObject().getCheckinOffsetDateTime();
	
	String datelabel;
	
	try {
		if (date!=null)	
			datelabel = ServiceLocator.getService(DateTimeService.class).getDateDisplayString(date);
		else
			datelabel  = "";
		
	} catch (Exception e) {
		logger.error(e);
		datelabel=e.getClass().getName();
	}
	 
	IModel<String> kcss=new Model<String>("col-lg-3 col-md-7 col-xs-7 keyc");
	IModel<String> vcss=new Model<String>("col-lg-9 col-md-5 col-xs-5 valuec");
	
	this.panels.add(new CommandAttributePanelV5("command_item", getLabelModel("version"), 	new Model<String>(vers), kcss, vcss));
	
	String proc = getProcedure();
	if (proc!=null)
		this.panels.add(new CommandAttributePanelV5("command_item", getLabelModel("procedure"), 	new Model<String>(proc), kcss, vcss));
	
	this.panels.add(new CommandAttributePanelV5("command_item", getLabelModel("date"), 	new Model<String>(datelabel), kcss, vcss));
	this.panels.add(new CommandAttributePanelV5("command_item", getLabelModel("published-by"), 	new Model<String>(getModelObject().getLastModifiedUser().getFirstLastName() ), kcss, vcss));
	this.panels.add(new CommandAttributePanelV5("command_item", getLabelModel("library"),	new Model<String>(libraryName), kcss, vcss  ));
	this.panels.add(new CommandAttributePanelV5("command_item", getLabelModel("template"), 	new Model<String>(getModelObject().getContentTemplate().getDisplayName()), kcss, vcss));

	String s=getModelObject().getContentTypeClassificationAsString();
	this.panels.add(new CommandAttributePanelV5("command_item", getLabelModel("content-type"), 	new Model<String>(s!=null?s:""), kcss, vcss));

	
	this.panels.add(new CommandAttributePanelV5("command_item", getLabelModel("version-id"), 	new Model<String>(v), kcss, vcss));
	
	if (getModelObject().getSource()!=null)	  	  this.panels.add(new CommandAttributePanelV5("command_item", getLabelModel("source"), 			new Model<String>( getModelObject().getSource().getDisplayName() ), kcss, vcss));
	if (getModelObject().getExternalId()!=null)	  this.panels.add(new CommandAttributePanelV5("command_item", getLabelModel("externalid"), 		new Model<String>(getModelObject().getExternalId() ), kcss, vcss));
	if (getModelObject().getExternalTime()!=null) this.panels.add(new CommandAttributePanelV5("command_item", getLabelModel("externaltime"), 	 getStringDateModel( getModelObject().getExternalTime() ), kcss, vcss));

	this.panels.sort(new Comparator<Panel>() {

		@Override
		public int compare(Panel o1, Panel o2) {

			try {
			CommandAttributePanelV5 p1 = (CommandAttributePanelV5) o1;
			CommandAttributePanelV5 p2 = (CommandAttributePanelV5) o2;
			return p1.getKey().getObject().compareToIgnoreCase(p2.getKey().getObject());
			} catch (Exception e) {
				logger.error(e);
			}
			return 0;
		}
	});
	return this.panels;
}


	private String getProcedure() {

		try {
			List<IModel<Activity>> list=getActivities();
			if (list!=null && list.size()>0)
				return list.get(0).getObject().getProcess().getProcedure().getName();
			return null;
		
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}


@SuppressWarnings("unchecked")
@Override
public void onDetach() {
	
	if (this.site_model!=null)
		this.site_model.detach();
	
	ListView<Panel> lv= (ListView<Panel>) get("result");
	if (lv!=null)
		for (Panel panel: lv.getList())
			panel.detach();
	if (this.panels!=null)
		for (Panel panel: this.panels)
			panel.detach();
	super.onDetach();
}


protected IModel<Site> getSiteModel() {
	return this.site_model;
}
protected ContentDao getContentDao() {
	return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
}

protected IModel<String> getStringDateModel(OffsetDateTime dt) {
	if (dt==null)
		return new Model<String>("err");
	DateTimeService service = ServiceLocator.getService(DateTimeService.class);
	User user = getSessionUser();
	String zid = service.getMapZoneIds().get(user.getTimeZone());
	if (zid==null)
			zid=ZoneId.systemDefault().getId();
	/** YYYY-MM-dd HH:mm:ss.XXX-z */
	return new Model<String>(service.format(dt, zid, getSessionUser().getLocale(),  DateTimeService.Day_Month_Year_hh_mm_ss_zzz ));
}


protected User getSessionUser() {
	return ServiceLocator.getService(SecurityService.class).getSessionUser();
}
	
	protected String getContentClass(T content) {
		return ProxyUtil.getClassName(content).toLowerCase();
	}
						
	protected IModel<String> getLabelModel(String resourceKey) {
		return new StringResourceModel(resourceKey, this, null);
	}

	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}
	
	protected boolean isWriteable(IModel<T> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(model.getObject());
	}
	
	
	protected List<IModel<Activity>> getActivities() {
		try {
			com.novamens.workflow.Process process= getModel().getObject().getService(WorkflowService.class).getLastProcess();
			List<IModel<Activity>> list_m = new ArrayList<IModel<Activity>>();
			if (process==null)
				return list_m;
			List<Activity> list= process.getActivities();
			if (list==null)
				return list_m;
			for (Activity a: list)
				list_m.add(new ObjectModel<Activity>(a));
			return list_m;
			
		} catch (Exception e) {
			logger.error(e);
			return new ArrayList<IModel<Activity>>();
		}
		
	}

}
