package kbee.web.page;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.markup.html.event.ClickH1Event;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.util.DummyBlockPanel;


@SuppressWarnings("serial")
public class PageContentHeaderPanel<T> extends KBPanel {

	private static final long serialVersionUID = 1L;
																									
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PageContentHeaderPanel.class.getName());
	
	private IModel<T> model;
	private WebMarkupContainer titlepanel;
	private WebMarkupContainer messagepanel;
	private WebMarkupContainer default_toolspanel;
	
	private IModel<String> title;
	private IModel<String> sub_line;
	private IModel<String> message;
	
	private  WebMarkupContainer hcx;
	private  WebMarkupContainer hc_internal;
	
	private  Panel menu;
	private  Panel breadcrumb;
	private  Panel search;
	private  Panel actions;
	private  Panel navigator;
	private  Panel avatar;
	private  WebMarkupContainer icon;
	
	private Panel toolbar;
	
	private  String icon_css = null;
	private  boolean is_section_home = false;
	
	/**
	 * 
	 *
	 */
	public class DefaultToolsPanel extends Fragment {
		
		private static final long serialVersionUID = 1L;
		public DefaultToolsPanel(String id) {
			super(id, "default-tools-panel", PageContentHeaderPanel.this);
		}
		
		@Override
		public void onInitialize() {
			super.onInitialize();
			
			Panel s=getSearchPanel();
			Panel m=getMenuPanel();
			Panel n_tor=getSearchNavigatorPanel();
			Panel a=getActionsPanel();
			
			if (a!=null) 	 addOrReplace(a);		else addOrReplace(new InvisiblePanel("actions"));
			if (s!=null) 	 addOrReplace(s); 		else addOrReplace(new InvisiblePanel("search"));
			if (m!=null) 	 addOrReplace(m); 		else addOrReplace(new InvisiblePanel("menu-panel"));
			if (n_tor!=null) addOrReplace(n_tor); 	else addOrReplace(new InvisiblePanel("navigation"));
		}
	}
	
	/**
	 * 
	 *
	 */
	public class DefaultTitlePanel extends Fragment {
		private static final long serialVersionUID = 1L;
		public DefaultTitlePanel(String id) {
			super(id, "default-title-panel", PageContentHeaderPanel.this);
			if (sub_line!=null) {
				add( (new Label("subline", sub_line)).setEscapeModelStrings(false));
			}
			else {
				add((new Label("subline", "")).setVisible(false));
			}
			Link<Void> h1link = new Link<Void>("h1link") {
				@Override
				public void onClick() {
					fire(new  ClickH1Event<Void>());
				}
			};
			
			Label title =new Label("title", getTitle());
			title.setEscapeModelStrings(false);
			h1link.add(title);
			add(h1link);
		}
	}
	
	/**
	 *
	 */
	public class DefaultMessagePanel extends Fragment {
		private static final long serialVersionUID = 1L;
		public DefaultMessagePanel(String id) {
			super(id, "default-message-panel", PageContentHeaderPanel.this);
			add(new Label("message", getMessage()).setEscapeModelStrings(false));
		}
	}

	/**
	 * 
	 */
	public PageContentHeaderPanel() {
		this("page-content-header", null);
	}
	
	public PageContentHeaderPanel(IModel<T> model) {
		this("page-content-header", model);
	}		
	
	public PageContentHeaderPanel(String id, IModel<T> model) {
		super(id, model);
		setModel(model);
		this.hcx = new WebMarkupContainer("header-container");
		this.hc_internal =  new WebMarkupContainer("header-internal-container");
		this.icon =   new WebMarkupContainer("icon");
		this.icon.setVisible(false);
		this.hc_internal.add(icon);
		this.hcx.add(hc_internal);
		add(this.hcx);
	}
	
	public void setToolsPanel(Panel panel) {
		if (!panel.getId().equals("tools")) 
			throw new IllegalArgumentException("must have id = 'tools'");
		default_toolspanel=panel;
		if (PageContentHeaderPanel.this.isInitialized())
			PageContentHeaderPanel.this.hcx.addOrReplace(panel);
	}
	
	public void setSubLine(IModel<String> s) {
		this.sub_line=s;
	}
	
	public void setSubLine(String s) {
		setSubLine(new Model<String>(s));
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (model!=null)
			model.detach();
	}

	public void setToolbarPanel(Panel panel) {
		if (!panel.getId().equals("toolbar")) 
			throw new KbeeRuntimeException("must have id = 'toolbar'");
		this.toolbar=panel;
		if (PageContentHeaderPanel.this.isInitialized()) {
			try {
				hcx.addOrReplace(this.toolbar);
			} 
			catch (Exception e) {
				logger.error(e);
			}
		}
	}
	
	public void setAvatarPanel(Panel panel) {
		if (!panel.getId().equals("avatar")) 
			throw new KbeeRuntimeException("must have id = 'avatar'");
		this.avatar=panel;
		if (PageContentHeaderPanel.this.isInitialized()) {
			try {
				hc_internal.addOrReplace(this.avatar);
			} 
			catch (Exception e) {
				logger.error(e);
			}
		}
	}
	
	/**
	 * 
	 */				
	public void setMessagePanel(Panel panel) {

		if (!panel.getId().equals("message")) 
			throw new KbeeRuntimeException("must have id ='message'");
		this.messagepanel=panel;
		if (PageContentHeaderPanel.this.isInitialized()) {
			try {
				hc_internal.addOrReplace(this.messagepanel);
			} 
			catch (Exception e) {
				logger.error(e);
			}
		}
	}
	

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		super.setOutputMarkupId(true);

		if (icon_css!=null) {
			icon.setVisible(true);
			icon.add( new AttributeModifier("class", icon_css));
		}
		
		if (titlepanel==null)
			titlepanel = new DefaultTitlePanel("titlepanel");
		 
		if (getMessage()!=null)
			messagepanel = new DefaultMessagePanel("messagepanel");
		
		if (messagepanel==null)
			messagepanel = new InvisiblePanel("messagepanel");
		
		hc_internal.addOrReplace(titlepanel);
		hc_internal.addOrReplace(messagepanel);
		
		if (this.avatar!=null) 
			hc_internal.addOrReplace(this.avatar);
		else 	
			hc_internal.addOrReplace(new InvisiblePanel("avatar"));
		
		Panel b=getBreadcrumbPanel();
		if (b!=null) hcx.addOrReplace(b); else hcx.addOrReplace(new InvisiblePanel("breadcrumb"));
		
		 if (default_toolspanel==null)
			 default_toolspanel = new DefaultToolsPanel("tools");
		 
		 hcx.addOrReplace(default_toolspanel);
		 
		 if (toolbar==null) {
			 DummyBlockPanel p=new DummyBlockPanel("toolbar");
			 p.setStyle("height:25px;");
			 toolbar = p;
		 }
		 
		 hcx.addOrReplace(toolbar);
		 

		 /**
		Panel s=getSearchPanel();
		Panel m=getMenuPanel();
		Panel n_tor=getSearchNavigatorPanel();
		
		if (s!=null) 	 hcx.addOrReplace(s); 		else hcx.addOrReplace(new InvisiblePanel("search"));
		if (m!=null) 	 hcx.addOrReplace(m); 		else hcx.addOrReplace(new InvisiblePanel("menu-panel"));
		if (n_tor!=null) hcx.addOrReplace(n_tor); 	else hcx.addOrReplace(new InvisiblePanel("navigation"));
		**/
		
	}
	
	

	public void setSectionHome( boolean b) {
		this.is_section_home=b;
	}
	
	public void onBeforeRender() {
		super.onBeforeRender();

		if (this.avatar!=null) {
			hcx.add(new AttributeModifier("class", "isavatar console-page-header page-header internal-page-header"));
		}	
		else {
			if (is_section_home)
				hcx.add(new AttributeModifier("class", "section-header console-page-header page-header"));
			else
				hcx.add(new AttributeModifier("class", "internal-page-header console-page-header page-header"));
		}
	}
	
	public IModel<T> getModel() {
	return this.model;
	}

	public void setModel(IModel<T> model) {
		this.model=model;
	}
	
	public void setTitle(String s) {
		this.title=new Model<String>(s);
	}
	
	public void setTitle(IModel<String> title) {
		this.title=title;
	}
	
	public void setTitle(Panel panel) {
		this.titlepanel = panel;
	}

	public void setBreadcrumbPanel(Panel panel) {
		if (!panel.getId().equals("breadcrumb")) 
			throw new KbeeRuntimeException("breadcrumb must have id = breadcrumb");
		if (PageContentHeaderPanel.this.isInitialized()) 
			hcx.addOrReplace(panel);
		breadcrumb=panel;
	}
	
	public void setSearchPanel(Panel panel) {
		if (!panel.getId().equals("search")) 
			throw new KbeeRuntimeException("search must have id = search");
		
		if (PageContentHeaderPanel.this.isInitialized()) {
			if (default_toolspanel!=null  && default_toolspanel.get("search")!=null) {
				default_toolspanel.addOrReplace(panel);
			}
			hcx.addOrReplace(panel);
		}
		search=panel;
	}
	
	public void setIcon(String css) {
		icon_css = css;
	}
	
	public void setMenuPanel(Panel panel) {
		if (!panel.getId().equals("menu-panel")) 
			throw new KbeeRuntimeException("must have id = menu-panel");
		menu=panel;
		if (PageContentHeaderPanel.this.isInitialized()) {
			if (default_toolspanel!=null  && default_toolspanel.get("menu-panel")!=null) {
				default_toolspanel.addOrReplace(panel);
			}
		}
	}
	
	public void setActionsPanel(Panel panel) {
		if (!panel.getId().equals("actions")) 
			throw new KbeeRuntimeException("must have id = actions");
		actions=panel;
		
		if (PageContentHeaderPanel.this.isInitialized() && default_toolspanel!=null  && default_toolspanel.get("actions")!=null) {
			default_toolspanel.addOrReplace(panel);
		}
	}
	
	public void setSearchNavigatorPanel(Panel panel) {
		if (!panel.getId().equals("navigation")) 
			throw new IllegalArgumentException("must have id = navigation");
		navigator=panel;
		if (PageContentHeaderPanel.this.isInitialized() && default_toolspanel!=null  && default_toolspanel.get( "navigation")!=null) {
			default_toolspanel.addOrReplace(panel);
		}
	}
	
	public void setMessage( IModel<String> message) {
		this.message = message;
	}
	
	protected IModel<String> getMessage() {
		return this.message;
	}
	
	protected Panel getSearchNavigatorPanel() {
		return navigator;  
	}
	
	protected Panel getSearchPanel() {
		return search;  
	}
	
	protected Panel getMenuPanel() {
		return menu; 
	}
	
	protected Panel getActionsPanel() {
		return actions; 
	}
	
	protected Panel getBreadcrumbPanel() {
		return breadcrumb;
	}
	
	protected IModel<String> getTitle() {
		if (title==null)
			return new Model<String>(getClass().getSimpleName()+ " -> untitled");
		return this.title;
	}
}
