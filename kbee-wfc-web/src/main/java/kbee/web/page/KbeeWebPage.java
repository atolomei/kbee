package kbee.web.page;


import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.service.BrandingService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.util.DummyBlockPanel;

import kbee.util.PropertiesFactory;
import kbee.web.service.ApplicationSiteMapService;

/**
 * 
 * <p>This is the first class that knows about {@link Content}, {@link Domain}, Session {@link User}, 
 * and other* Domain Model Objects.</p>
 *
 * @param <T>
 */
public class KbeeWebPage<T> extends AbstractKbeeWebPage {
													
	private static final String XUA_Compatible = PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.portal.xuacompatible", "IE=Edge");

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeWebPage.class.getName());
	private static kbee.util.logging.Logger dlogger = kbee.util.logging.Logger.getLogger("helpkey");
	
	private static final long serialVersionUID = 1L;
	
	static Panel stfooter = null;

	/** 
	 * ConsoleFooterPanel
	 */
	private IModel<T> model;

	private String sectionHelpKey = null;
	
	
	public KbeeWebPage() {
		this(null);
	}

	public KbeeWebPage (IModel<T> model) {
		setModel(model);
		setPageFonts(getFonts());
		setPageXUACompatible(XUA_Compatible);
		setFavicon(getApplicationFavIcon());
		
	}
	
	
	static private String _fav = null;
	private String getApplicationFavIcon() {

		if (_fav!=null)
			return _fav;
	
		synchronized (this) {
			_fav= ServiceLocator.getService(BrandingService.class).getFavIconStr();
		}
		return _fav;
		
	}

	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	}
	
	public String hasPermissionsReason() {
		dlogger.debug("permissions.reason."+this.getClass().getSimpleName().toLowerCase());
		return getContentDao().findSystemParameterValueByKey("permissions.reason."+this.getClass().getSimpleName(), "System Parameter not found: <b>permissions.reason."+this.getClass().getSimpleName().toLowerCase() + "</b>");
	}
	
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.HOME;
	}
	
	public IModel<T> getModel() {
		return model;
	}

	public void setModel(IModel<T> model) {
		this.model = model;
	}

	public T getModelObject() {
		return model.getObject();
	}

	@Override
	public void onDetach() {
		
		if (this.model!=null)
			this.model.detach();
		
		if (get("footer")!=null)
			get("footer").detach();
		
		super.onDetach();
	}

	@Override
	public String getPageHelpKey() {
		return  getApplicationMenuSection().getKey() + "-" + this.getClass().getSimpleName().toLowerCase();
	}
	
	@Override
	public void setPageInternalSectionHelpKey(String str) {
		this.sectionHelpKey=str;
	}
	
	@Override
	public String getPageInternalSectionHelpKey() {
		return sectionHelpKey;
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	protected boolean isExpressVersion() {
		try {
			return getDomain().getDomainType()==DomainType.EXPRESS;
		}
		 catch (Exception e) {
			 logger.error(e);
			 return false;
		 }
	}

	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	
	/**
	 * getMainTopbar()
	 * getMainLaternalMenu()
	 * 
	 */
	protected Component getMainTopbar() {
		return ServiceLocator.getService(ApplicationSiteMapService.class).getMainTopBar();	
	}
	
	protected Component getMainLaternalMenu() {
		return ServiceLocator.getService(ApplicationSiteMapService.class).getMainLateralMenu(getApplicationMenuSection().getKey());
	}
	
	protected Component getMainLaternalMenu(String appKey) {
		logger.debug(appKey);
		try {
			return ServiceLocator.getService(ApplicationSiteMapService.class).getMainLateralMenu(appKey);
		} 
		catch (Exception e) {
			logger.error(e);
			return new DummyBlockPanel("menu");
		}
	}
}