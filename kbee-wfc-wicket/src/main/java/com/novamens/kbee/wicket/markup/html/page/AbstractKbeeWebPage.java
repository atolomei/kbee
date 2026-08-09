package com.novamens.kbee.wicket.markup.html.page;


import java.util.Iterator;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Session;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.MetaDataHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.event.Event;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.service.ServiceLocator;

import kbee.util.PropertiesFactory;

/**
 * This class doesn't know domain, content or any other domain class.
 */
@SuppressWarnings("serial")
public abstract class AbstractKbeeWebPage extends WebPage  {
 					
	private static final long serialVersionUID = -8491705098363328007L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AbstractKbeeWebPage.class.getName());
	private static kbee.util.logging.Logger dlogger = kbee.util.logging.Logger.getLogger("helpkey");
	
	static private IModel<String> model_xtitle;
	static private String xtitle;
	static private IModel<String> xdescription;
	static private String xfavicon; 
	static private String xlanguage;
	static private String xrobots;
	static private String xrating ;
	static private String xkeywords="digital documents, eforms workflow, digital signature, content management";
	private static final String XUA_Compatible =  PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.portal.xuacompatible", "IE=Edge");
 
	static {
		xdescription =  new Model<String>(PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.content.web.description", ""));
		xfavicon 	 =  PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.content.web.favicon", "/images/favicon.gif");
		xlanguage 	 =  PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.content.web.language", "English");
		xrobots 	 =  PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.content.web.robots", "NOINDEX, NOFOLLOW");
		xrating 	 =  PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.content.web.rating", "General");
		xtitle 		 =  PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.content.web.title", "");
		model_xtitle= new Model<String>(xtitle);
	}
	
	private static final String DEFAULT_LATO_FONTS="https://fonts.googleapis.com/css2?family=Lato:ital,wght@0,400;0,900;1,400&display=swap";
	
	/**
	 * 	<link rel="preconnect" href="https://fonts.gstatic.com">
	 *	<link href="https://fonts.googleapis.com/css2?family=Lato:ital,wght@0,400;0,900;1,400&display=swap" rel="stylesheet">
	 * 
	
	 *
	 *
	 *
	 *		<link rel="preconnect" href="https://fonts.googleapis.com">
			<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
			<link href="https://fonts.googleapis.com/css2?family=Montserrat:ital,wght@0,100..900;1,100..900&family=Noto+Sans:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">

	 *
	 *
	 *
	 *
	 *
	 */
	//private static final ResourceReference XCSS = new CssResourceReference(AbstractKbeeWebPage.class, "kbee2.css"); // este lo reemplazaremos por kbeebootstrap.css
	
	private IModel<String> description 	= xdescription;
	private IModel<String> title   		= model_xtitle;
	
	private String keywords    			= xkeywords;
	private String language    			= xlanguage;

	private String xuacompatible; // = "IE=8"; // IE=edge
 	private String robots 		 = xrobots;
 	private String fonts 		 = null;
 	private String favicon 		 = null;
 	

 	//private ResourceReference rcss = XCSS;
 	private ResourceReference rcss;
 	
 	private WebMarkupContainer wfont;
 	private WebMarkupContainer wcss;
 	private WebMarkupContainer fvicon;
 	private WebMarkupContainer vp;
 	private WebMarkupContainer desc;
 	private WebMarkupContainer lang;
 	private WebMarkupContainer kw;

	private boolean initialized = false;

	private String sectionHelpKey = null;
	
	public AbstractKbeeWebPage() {}

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Component addOrReplace(final WicketEventListener<?> newListener) {
		for (Behavior b: this.getBehaviors()) {
			if (b instanceof WicketEventListener) {
				if (  ((WicketEventListener) b).handles(newListener.getEventClass() )) {
						remove(b);
						add(newListener);
						return this;
				}
			}
		}
		add(newListener);
		return this;
	}
	
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		if (getPageXUACompatible()!=null) {
			MetaDataHeaderItem headerItem = new MetaDataHeaderItem("meta");
			headerItem.addTagAttribute("http-equiv", "X-UA-Compatible");
			headerItem.addTagAttribute("content", getPageXUACompatible());
			
			response.render(new PriorityHeaderItem(headerItem));
			
			headerItem = new MetaDataHeaderItem("meta");
			headerItem.addTagAttribute("http-equiv", "Content-Security-Policy");
			headerItem.addTagAttribute("content", "img-src *");

			response.render(new PriorityHeaderItem(headerItem));
		}
		
		ResourceReference cssResource = getCssResource();
		
		
		if (!hasLateralMenu()) {
			response.render(OnDomReadyHeaderItem.forScript("$('body').removeClass('sidebar-xs');"));
			response.render(OnDomReadyHeaderItem.forScript("$('body').addClass('nosidebar');"));
		}
		
		
		
		/**
		// kbee-portal.css
		// kbeebootstrap.css
		 */
		
		if (cssResource != null) 
			response.render(CssHeaderItem.forReference(cssResource));
	}
	
	 
	 
	
	protected boolean hasLateralMenu() {
		return true;
	}



	@Override
	public void onInitialize() {
		super.onInitialize();
		
		try {
		dlogger.debug(getPageHelpKey());
		
		
		
		this.wfont = new WebMarkupContainer("google-font");
		this.wfont.add(new AttributeModifier("rel", "stylesheet"));
		this.wfont.add(new AttributeModifier("href", getPageFonts()));
		this.wfont.setVisible(getPageFonts()!=null);
		add(this.wfont);
		
		
		this.fvicon = new WebMarkupContainer("favicon");
		this.fvicon.add(new AttributeModifier("rel", "icon"));
		this.fvicon.add(new AttributeModifier("type",  "image/x-icon"));
		this.fvicon.add(new AttributeModifier("href", getFavicon()));
		add(this.fvicon);

		this.lang = new WebMarkupContainer("language");
		this.lang.add(new AttributeModifier("name", "language"));
		this.lang.add(new AttributeModifier("language", 
			new Model<String>() {
				@Override
				public String getObject() {
					return getPageLanguage();
				}
		}));

		
		 //WebMarkupContainer bodyContainer = new WebMarkupContainer("body");
	     //bodyContainer.setTransparentResolver(true);
	     //add(bodyContainer);
	     // bodyContainer.add(new StyleAttributeModifier("class", new PropertyModel<String>(this, "bodyClass")));
	       
		
		add(this.lang);

		this.kw = new WebMarkupContainer("keywords");
		this.kw.add(new AttributeModifier("name", "keywords"));
		this.kw.add(new AttributeModifier("content", 
			new Model<String>() {
				@Override
				public String getObject() {
					return getPageKeywords();
				}
		}));
		add(this.kw);


		this.desc = new WebMarkupContainer("header-description");
		this.desc.add(new AttributeModifier("name", "description"));
		this.desc.add(new AttributeModifier("content", getPageDescription()));
		this.desc.add(new AttributeModifier("name", "viewport"));
		this.desc.add(new AttributeModifier("content", 
			new Model<String>() {
				@Override
				public String getObject() {
					//return "width=device-width, initial-scale=1.0, minimum-scale=1.0, user-scalable=yes";
					return   "width=device-width, initial-scale=1, shrink-to-fit=no";
					
				}
		}));
		add(this.desc);

		WebMarkupContainer rating = new WebMarkupContainer("rating");
		rating.add(new AttributeModifier("name", "rating"));
		rating.add(new AttributeModifier("content", xrating));
		add(rating);

		WebMarkupContainer wrobots = new WebMarkupContainer("robots");
		wrobots.add(new AttributeModifier("name", "robots"));
		wrobots.add(new AttributeModifier("content", 
				new Model<String>() {
					@Override
					public String getObject() {
						return getPageRobots();
					}
			}));
		
		add(wrobots);


		setPageKeywords(keywords);
		
		this.wcss = new WebMarkupContainer("css");
		this.wcss.setVisible(false);
		add(this.wcss);

		this.vp = new WebMarkupContainer("viewport");
		add(vp);

		if (XUA_Compatible!=null)
			setPageXUACompatible(XUA_Compatible);
		} catch (Exception e) {
			logger.error("--------------- AbstractKbeeWebPage -> onInitialize() ---------------");
			logger.error(e);
			throw(e);
		}
		
	}

	
	 Label htitle;
	 
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
		if (!Session.exists()) {
			logger.error("---------------------------------------------------------------------");
			logger.error(" Session does not exist !!!");
			logger.error("---------------------------------------------------------------------");
		}
		
		ServiceLocator.getService(SystemMetricsService.class).getMeterWebPages().mark();
		
		if (!this.initialized) {
			if (htitle==null) {
				htitle = new Label("header-title", getPageTitle()); 
				add(htitle);
			}
			this.initialized=true;
		}
	}
	
	
	public void setFonts( String fonts) {
		this.fonts=fonts;
	}
	
	public String getFonts() { 
		return fonts!=null? fonts : DEFAULT_LATO_FONTS;
	}
	
	public String getPageHelpKey() {
		return this.getClass().getSimpleName();
	}
	
	public void setPageInternalSectionHelpKey(String str) {
		this.sectionHelpKey=str;
		
	}
	
	public String getPageInternalSectionHelpKey() {
		return sectionHelpKey;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}

	protected void setPageTitle(IModel<String> title) { 
		this.title=title;
		htitle = new Label("header-title", title); 
		addOrReplace(htitle);
	}
	
	protected void setPageTitle(String title) {
		setPageTitle(new Model<String>(title));
	}
	
	protected IModel<String> getPageTitle() 				{return title;}

	protected void setPageFonts(String s) 					{fonts=s;}
	protected String getPageFonts() 						{return fonts;}
	
	protected void setPageRobots(String s) 					{robots=s;}
	protected String getPageRobots() 						{return robots;}

	protected void setPageXUACompatible(String uax) 		{this.xuacompatible=uax;}
	protected String getPageXUACompatible() 				{return xuacompatible;}

	protected void setPageLanguage(String language) 		{this.language=language;}
	protected String getPageLanguage() 						{return language;}

	protected void setPageDescription(IModel<String> desc)	{this.description=desc;}
	protected IModel<String> getPageDescription() 			{return description;}
	
					
	protected void setFavicon(String desc)					{this.favicon=desc;}
	protected String getFavicon() 							{return favicon!=null? favicon : xfavicon;}

	
	protected void setPageKeywords(String desc) 			{this.keywords=desc;}
	protected String getPageKeywords() 						{return keywords;}
	
	protected void setCss(ResourceReference rcss) 			{this.rcss=rcss;}
	protected ResourceReference getCssResource()			{return rcss;}

	protected IModel<String> getLabel(String key) {
		return new StringResourceModel(key, this, null);
	}
	
	protected String getLabelString(String key) {
		return getLabel(key).getObject();
	}

	protected IModel<String> getLabel(String key, String... parameter) {
		StringResourceModel model = new StringResourceModel(key, this);
		model.setParameters((Object[]) parameter);
		return model;
	}

	protected String getServerUrl() {
		String protocol =((WebRequest)RequestCycle.get().getRequest()).getUrl().getProtocol();
		String host =((WebRequest)RequestCycle.get().getRequest()).getUrl().getHost();
		Integer iport =((WebRequest)RequestCycle.get().getRequest()).getUrl().getPort(); 
		String port = (iport.equals(80) || iport.equals(443) ? "":  ( ":" + iport.toString()) );
		return protocol +"://" + host + port;
	}
	
	@SuppressWarnings("unchecked")
	public void fireScanAll(Event event) {
		
		logger.debug("Fire Scan All " + event.getClass().getSimpleName());
		
		for (WicketEventListener<Event> listener : getPage().getBehaviors(WicketEventListener.class)) {
			if (listener.handle(event)) {
				listener.onEvent(event);
			}
		}
		
		fire(event, getPage().iterator(), false);
	}

	
	/**
	 * Scans Page and all its components
	 * The first Component that listens to this event will handle it
	 * 
	 **/
	@SuppressWarnings("unchecked")
	public void fire(Event event) {
		
		logger.debug("Fire " + event.getClass().getSimpleName());
		
		boolean handled=false;
		for (WicketEventListener<Event> listener : getPage().getBehaviors(WicketEventListener.class)) {
			if (listener.handle(event)) {
				listener.onEvent(event);
					handled = true;
					break;
				}
			}
		if (!handled) 
			fire(event, getPage().iterator());
	}
	
	protected boolean fire(Event event, Iterator<Component> components) {
		return fire(event, components, true);
	}
	
	@SuppressWarnings("unchecked")
	protected boolean fire(Event event, Iterator<Component> components, boolean stop_first_hit) {
		boolean handled = false;
		while (components.hasNext()) {
			Component component = components.next();
			for (WicketEventListener<Event> listener : component.getBehaviors(WicketEventListener.class)) {
				if (listener.handle(event)) {
					listener.onEvent(event);
					if (stop_first_hit) {
						handled = true;
						break;
					}
				}
			}
			if (!handled) {
				if (component instanceof MarkupContainer) {
					handled = fire (event, ((MarkupContainer)component).iterator(), stop_first_hit);
				}
			}
			else {
				break;
			}
		}
		return handled;
	}

	
}
