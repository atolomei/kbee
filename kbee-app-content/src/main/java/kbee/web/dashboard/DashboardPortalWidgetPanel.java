package kbee.web.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.kbee.security.KbeeUser;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorPanel;
import kbee.web.help.InlineHelpWebService;
import kbee.web.portal6.SitesPage;
import kbee.web.service.PortalPanelService;

public class DashboardPortalWidgetPanel extends DashboardListWidgetPanel<Site> {
		
	
	/**
	 */
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DashboardPortalWidgetPanel.class.getName());
	
	int size;
	long total;
		
	public DashboardPortalWidgetPanel(String id, String preferences_key) {
		super(id, preferences_key);
		setTitle( new StringResourceModel("sites", this, null));
	
	}

	
	protected IModel<String> getItemLabelMeta(IModel<Site> modelObject) {
		StringBuilder str = new StringBuilder();
		try {

		} catch (Exception e) {
			logger.error(e);
			str.append(e.getClass().getName());
		}
		return new Model<String>(str.toString()									);
	}

	
	
	protected void onHelp(AjaxRequestTarget target) {
		super.toogleHelp(target);
	}
	
	@Override
	protected WebMarkupContainer getHelpPanel() {
		InlineHelpWebService se=ServiceLocator.getService(InlineHelpWebService.class);
		 WebMarkupContainer  pa = se.getPanel("help", getLocale(), InlineHelpWebService.HOME_PORTALS);
		 if (pa!=null)
			 return pa;
		 return new ErrorPanel("help", new Model<String>(InlineHelpWebService.HOME_PORTALS));
		
	}
	

	
	//@Override
	//protected String getListContainerCss() {
	//	return "cozy";
	//}
	
	@Override
	public void onInitialize() {

		setHelp(true);
		List<IModel<Site>> list_site = new ArrayList<IModel<Site>>();
		KbeeUser us = (KbeeUser) getSessionUser();
		us.getService(UserDashboardService.class).getMySites().forEach(item -> list_site.add( new ObjectModel<Site>(item)));
		size=list_site.size();
		total=list_site.size();
		
		setItems(list_site);
		
		
		super.onInitialize();
	}
	protected boolean isIconVisible() {
		return false;
	}
	
	
	protected Panel addVoidPanel(String id) {
		return new  DashboardSimpleInfoPanel("tabs",  new StringResourceModel("no-items", this,null), "fad fa-sitemap");	
	}
	
	protected boolean isMenuVisible() {
		return true;
	}
	
	@Override
	protected IModel<String> getViewingString() {
		if (total==0)
			return new Model<String>("");
		
		if (size==total)
			return new Model<String>("Total: <b>" + String.valueOf(size) +"</b>");
		
		return new Model<String>("<b>" + String.valueOf(size) +"</b> of <b>"+ String.valueOf(total) +"</b>");
	}

	protected IModel<String> getAllString() {
		return new StringResourceModel("sites",this, null);
	}

	@Override
	protected void onClick(IModel<Site> modelObject, int index) {
		try {
		setResponsePage( ServiceLocator.getService(PortalPanelService.class).getWebPage(modelObject.getObject()));
		} catch (Exception e) {
			logger.error(e);
			setResponsePage(new ApplicationErrorPage<>(e));
		}
			
	}
	
	@Override
	protected void onClickAll() {
		setResponsePage( new SitesPage());
	}

}
