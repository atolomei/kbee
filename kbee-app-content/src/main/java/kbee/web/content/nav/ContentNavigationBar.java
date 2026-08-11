package kbee.web.content.nav;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Fragment;

import org.apache.wicket.model.IModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.document.IDoc;
import com.novamens.content.service.HelpService;
import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.content.user.UserService;
import com.novamens.content.web.content.markup.ContentPanel;
import com.novamens.content.web.nav.markup.NavBarContentMenu;
import com.novamens.content.web.workflow.markup.TaskPanel;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.repeater.util.Searcher;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.PropertiesFactory;
import kbee.web.event.wicket.FullScreenEvent;
import kbee.web.event.wicket.PreviewClickEvent2;
import kbee.web.idoc.IDocPageV6;
import kbee.web.nav.DonePage;
import kbee.web.nav.NavigationPanel;
import kbee.web.nav.NavigatorPanel;
import kbee.web.nav.RefreshParentBehavior;

/**
 * Bootstrap based. Navigation Bar
 *
 * @param <T>
 */
@SuppressWarnings("serial")
public class ContentNavigationBar<T extends Content> extends NavigationPanel<T>  {
	private static final long serialVersionUID = 1L;

	private Boolean is_readonly;
	private boolean isCloseVisible = true; 
														
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ContentNavigationBar.class.getName());
	
	static String onNavigateBehavior = PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.content.web.onNavigate", "navigate");
	
	private IModel<T> model;
	private Editor<T> editor;

	private String isportal = null;

	private static AtomicBoolean IS_HELP_VISIBLE= null;

	public static boolean isHelpEnabled() {
		if (IS_HELP_VISIBLE!=null)
			return 	IS_HELP_VISIBLE.get();
			IS_HELP_VISIBLE = new AtomicBoolean(((ContentDao) (ServiceLocator.getService(BeansService.class).getBean("contentDao"))).findSystemParameterValueByKey("help.enabled", "no").toLowerCase().trim().equals("yes"));
		return 	IS_HELP_VISIBLE.get();
	}
	
	
	public class HelpFragment extends Fragment {
		public HelpFragment(String id) {
			super(id, "help-fragment", ContentNavigationBar.this);
			
			Link<Void> link = new Link<Void>("help-link") {
				@Override
				public void onClick() {
					Page page = getPage();
					if (page instanceof AbstractKbeeWebPage) {
						String page_key = ((AbstractKbeeWebPage) page).getPageHelpKey();
						String section_key = ((AbstractKbeeWebPage) page).getPageInternalSectionHelpKey();
						String url = ServiceLocator.getService(HelpService.class).getHelpUrl(page_key+ (section_key!=null? ("-" + section_key):""));
						setResponsePage(new RedirectPage(url));
					}
				}
			};
			link.add(new AttributeModifier("target", "_blank"));
			link.setVisible(isHelpEnabled());
			add(link);
		}
	}
	
	public ContentNavigationBar(IModel<T> model) {
		this("navigation", model);
	}
	
	Searcher searcher = null;
	long index = 0;
	
	public ContentNavigationBar(String id, IModel<T> model, Searcher searcher, long index) {
		super(id);
		setOutputMarkupId(true);
		
		this.searcher=searcher;
		this.index=index;
		
		setModel(model);
		add(newNavigator(searcher, index));
	}
	
	public ContentNavigationBar(String id, IModel<T> model) {
		super(id);
		setOutputMarkupId(true);
		setModel(model);
		
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		if (get("navigator")==null)
			add(new InvisiblePanel("navigator"));

		
		Link<T> v6l=new Link<T>("detailv6", getModel()) {
			@Override
			public void onClick() {
				if (getModel().getObject() instanceof IDoc) {
					//Searcher searcher = ContentNavigationBar.this.searcher;
					//IDocPageV6 page=new IDocPageV6( new ObjectModel<IDoc>((IDoc) getModel().getObject()), searcher, ContentNavigationBar.this.index );
					IDocPageV6 page=new IDocPageV6( new ObjectModel<IDoc>((IDoc) getModel().getObject()));
					setResponsePage(page);
					
				}
				else {
					logger.debug("not idoc");
				}
			}
		};
		
		add(v6l);
				
		add(newCloseLink());
		add(newFullWidthLink().setVisible(false));
		add(newToolsPanel());
		add(versionTagPanel());
		add(newInfoPanel());
		add(newPreviewLink());
		add(newHelpPanel());
		
	}
	
	public void setCloseVisible(boolean b) {
		this.isCloseVisible=b;
	}
	
	public void navigate() {
		Page donePage = new DonePage();
		donePage.add(new RefreshParentBehavior());
		setResponsePage(donePage);
	};
	
	public void onStartWorkflow() {
	};
	
	public void onNavigate(T object) {
	};
	
	public void onReturn() {
	};
	
	public void setEditor(Editor<?> editor) {
		
	};
	
	public boolean isFromContentBase() {
		return false;
	};
	
	public void setModel(IModel<T> model) {
		this.model = model;
	}
	
	public IModel<T> getModel() {
		return model;
	}
	
	public long getIndex() {
		if (get("navigator")!=null && get("navigator") instanceof NavigatorPanel) {
			return ((NavigatorPanel<?>)get("navigator")).getIndex();
		}
		return -1;
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
	}
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		StringBuilder script = new StringBuilder();
		script.append("function closewindow() {\n");
		script.append("	if (window.opener && window.opener.refresh) { window.opener.refresh(); };\n");
		script.append("	var agent = navigator.userAgent;\n");
		script.append("	if (agent.indexOf('Edge') > 0 || agent.indexOf('Trident') > 0) {\n");
		script.append("		window.open('', '_self', '');\n");
		script.append("	}\n");
		script.append("	window.close();\n");
		script.append("}\n");
		response.render(JavaScriptHeaderItem.forScript(script.toString(), "closewindow"));
	}

	@Override
	public void onDetach() {
		if (get("navigator")!=null)
			get("navigator").detach();
		if (this.model!=null)
			this.model.detach();
		super.onDetach();
	}

	public void setReadOnly(boolean b) {
		this.is_readonly = Boolean.valueOf(b);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean isReadOnly() {
		
		if (this.is_readonly != null)
			return this.is_readonly.booleanValue();
		try {
			if (getModel().getObject().isExternal()) {
				return true;
			}
		} catch (Exception e) {
			logger.error(e);
		}
		
		this.is_readonly = Boolean.valueOf(false);
		return this.is_readonly;
	}

	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	
	protected Component newHelpPanel()  {
		return  isHelpEnabled() ? new HelpFragment("help")  : new InvisiblePanel("help");
	}
	
	
	protected void onReturn(AjaxRequestTarget target)  {
	}
	
	
	protected Component newCloseLink()  {
		WebMarkupContainer cl= new WebMarkupContainer("close") {
			@Override
			public boolean isVisible() {
				return isCloseVisible();
			}
		};
		Link<?> link = new Link<Void>("close-link")	{
			public void onClick() {
			}
		};
		link.add(new AttributeModifier("onclick", "closewindow();"));
		cl.add(link);
		return cl;
	}
	
	
 	protected boolean isCloseVisible() {
		return this.isCloseVisible;
	}

	protected Component newPreviewLink()  {
 		return new Link<Void>("preview-link") {
			@Override
			public void onClick() {
				fire(new PreviewClickEvent2<T>(ContentNavigationBar.this.getModel()));
			}
			@Override
			public boolean isVisible() {
				return isPortalEnabled();
			}
			@Override
			public boolean isEnabled() {
				return true;
			}
 		};
	}

 	
 	protected boolean isPortalEnabled() {
		if (this.isportal==null) {
			this.isportal = ServiceLocator.getService(UserService.class).getDomain().getService(DomainSettingsService.class).get(DomainSettingsService.PORTAL);
			if (this.isportal==null)
				this.isportal="no";
		}
		return this.isportal.equals("yes") || isportal.equals("true");
	}

	
	protected Component newNavigator(Searcher searcher, long index)  {
		return new NavigatorPanel<T>("navigator", searcher, (int)index) {
			public void onNavigate(T object) {
				ContentNavigationBar.this.onNavigate(object);
			}
		};
	}
	
 	protected Component newInfoPanel()  {
 		return new AjaxLink<Void>("info-link") {
 			public void onClick(AjaxRequestTarget target) {
 				Editor<T> editor = getEditor();
 				
 				if (((ContentPanel<T>)editor).isRightPanelVisible()) {
 					((ContentPanel<T>)editor).setRightPanelVisible(false);
 					((KbeeUser) getSessionUser()).getService(PreferencesService.class).setValue(TaskPanel.class.getSimpleName(), "one-panel", "yes");
 					target.add((ContentPanel<T>)editor);
 				}
 				else {
 					((KbeeUser) getSessionUser()).getService(PreferencesService.class).setValue(TaskPanel.class.getSimpleName(), "one-panel", "no");
 					((ContentPanel<T>)editor).showInfoPanel(target);
 				}
 				target.add(ContentNavigationBar.this);
 			}
 		};
 	}
	
	protected Component newToolsPanel()  {
		NavBarContentMenu<T> m = new NavBarContentMenu<T>("tools") {
			public IModel<T> getModel() {
				return ContentNavigationBar.this.getModel();
			}
			@Override
			public boolean isReadOnly() {
				return ContentNavigationBar.this.isReadOnly();
			}
		};
		return m;
	}

	
 	protected Component newFullWidthLink()  {
 		return new AjaxLink<Object>("fullwidth-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				fire(new FullScreenEvent(target));
			}
		};
	}
	
	protected Component versionTagPanel() {
		WebMarkupContainer tag = new WebMarkupContainer("version-tag") {
			public boolean isVisible() {
				return !ContentNavigationBar.this.getModel().getObject().isHeadVersion();
			}
		};
		return tag;
	}

	
	
	protected Editor<T> getEditor() {
		if (this.editor==null) {
			this.editor = getEditor(getPage().iterator());
		}
		return this.editor;
	}
	
	@SuppressWarnings("unchecked")
	protected Editor<T> getEditor(Iterator<Component> components) {
		while (components.hasNext()) {
			Component component = components.next();
			if (component instanceof Editor<?> && component.isVisible()) {
				return (Editor<T>)component;
			}
			else {
				if (component instanceof WebMarkupContainer) {
					Editor<T> editor = getEditor(((WebMarkupContainer)component).iterator());
					if (editor!=null) {
						return editor;
					}
				}
			}
		}
		return null;
	}

	

		

	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
 }

