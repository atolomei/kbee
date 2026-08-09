package kbee.web.nav;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.library.Library;
import com.novamens.content.library.LibraryService;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.ContentService;
import com.novamens.content.service.HelpService;
import com.novamens.content.service.UrlService;
import com.novamens.content.user.UserService;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.content.workflow.WorkflowDomainService;
import com.novamens.content.workflow.WorkflowService;
import com.novamens.dom.Domain;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.panel.KBPanel;

import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorPage;


/**
 * Bootstrap based. Navigation Bar
 * 
 * @param <T>
 */
@SuppressWarnings("serial")
public class ContentNavigationBarV6<T extends Content> extends KBPanel  {
	private static final long serialVersionUID = 1L;

	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ContentNavigationBarV6.class.getName());
	
	private IModel<T> model;
	private Boolean readOnly = null;
	
	private static AtomicBoolean IS_HELP_VISIBLE=null;
	
	public static boolean isHelpEnabled() {
		if (IS_HELP_VISIBLE!=null)
			return 	IS_HELP_VISIBLE.get();
			IS_HELP_VISIBLE = new AtomicBoolean(((ContentDao) (ServiceLocator.getService(BeansService.class).getBean("contentDao"))).findSystemParameterValueByKey("help.enabled", "no").toLowerCase().trim().equals("yes"));
		return 	IS_HELP_VISIBLE.get();
	}
	
	/**--------------------------
	 * 
	 * 
	 * 
	 */
	public class HelpFragment extends Fragment {
		public HelpFragment(String id) {
			super(id, "help-fragment", ContentNavigationBarV6.this);
			
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
			link.setVisible(isHelpEnabled());
			link.add(new AttributeModifier("target", "_blank"));
			add(link);
		}
	}
	
	/**--------------------------
	 * 
	 * 
	 * 
	 * 
	 */
	public ContentNavigationBarV6(IModel<T> model) {
		this("navigation", model);
	}
		
	public ContentNavigationBarV6(String id, IModel<T> model) {
		super(id);
		setOutputMarkupId(true);
		setModel(model);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		add(newHelpPanel());
		
		ContextMenuPanel<Panel> menu = new ContextMenuPanel<Panel>("menu", new Model<Panel>(this));
		
		getMenuItems().forEach(item -> menu.addItem((item)));
		
		
		menu.add(new AttributeModifier("class", "dropdown-menu"));
		menu.setOutputMarkupId(true);
		
		add(menu);
		
		
	}
	
	public void setModel(IModel<T> model) {
		this.model = model;
	}
	
	public IModel<T> getModel() {
		return model;
	}
	
	
	public T getModelObject() {
		return model.getObject();
	}
	

	@Override
	public void onDetach() {
		if (this.model!=null)
			this.model.detach();
		super.onDetach();
	}

	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected Component newHelpPanel()  {
		return  isHelpEnabled() ? new HelpFragment("help") : new InvisiblePanel("help");
	}
	
	protected void onReturn(AjaxRequestTarget target)  {
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	
	private List<MenuItemFactory<Panel>> getMenuItems() {
		
	
	List<MenuItemFactory<Panel>> list = new ArrayList<MenuItemFactory<Panel>>();
		
		int index = 0;
		
		for (ProcessLauncher p:getLaunchers()) {
		
			final int p_i= index++;
			list.add(new MenuItemFactory<Panel>() {
				@Override
				public AbstractMenuItemPanelV5<Panel> getItem(String id) {
					return new MenuItemPanelV5<Panel>(id) {
						@Override
						public void onClick() {
							try {
								ProcessLauncher launcher = getLaunchers().get(p_i);
								Content content = ContentNavigationBarV6.this.getModel().getObject().getService(ContentService.class).checkout();
								content.getService(WorkflowService.class).startProcess(launcher.getProcedure());
								setResponsePage(  new RedirectPage(content.getService(UrlService.class).getTaskUrl()));
							} 
							catch (Exception e) {
								logger.error(e.getClass().getName() + "| Checkout in ContentBaseConsole contextual menu" );
								setResponsePage( new ApplicationErrorPage<>(e));
							}
						}
						@Override
						public String getLabel() {	
							return  ContentNavigationBarV6.this.getLabel("checkout").getObject()+ " - " + getLaunchers().get(p_i).getDisplayName();
						}
						@Override 
						public boolean isVisible() {
							if (isReadOnly())
								return false;
							
							if (ContentNavigationBarV6.this.getModelObject().isArchived())
								return false;
							
							if (ContentNavigationBarV6.this.getModelObject().isRecycled())
								return false;
							
							return true;
						}
						@Override 
						public boolean isEnabled() {
							
							if (isReadOnly() || getDomain()==null)
								return false;
							
							return ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(ContentNavigationBarV6.this.getModelObject()) &&
								ContentNavigationBarV6.this.getModelObject().isHeadVersion() 	&&
								!ContentNavigationBarV6.this.getModelObject().isLocked() 		&&
								!ContentNavigationBarV6.this.getModelObject().isRecycled() 		&&
								!ContentNavigationBarV6.this.getModelObject().isArchived();
						}
					};	
				}	
			});
		}
		
		
		return list;
	
	
	}
	
	private List<ProcessLauncher> getLaunchers() {
		if (getDomain()==null)
			return  new ArrayList<ProcessLauncher>();
		return getDomain().getService(WorkflowDomainService.class)==null ? new ArrayList<ProcessLauncher>() :
			getDomain().getService(WorkflowDomainService.class).getContextLaunchers(ContentNavigationBarV6.this.getModelObject());
	}

	public boolean isReadOnly() {
		if (this.readOnly!=null)
			return this.readOnly.booleanValue();
		//  -------------------------------
		//
		// External Files are read-only
		//
		if (getModel().getObject().isExternal()) {
			this.readOnly=Boolean.valueOf(true);
			return this.readOnly.booleanValue();
		}

		//  -------------------------------
		//
		// user does not have permission to write 
		//
		if (!isWriteable( getModel() )) {
			this.readOnly=Boolean.valueOf(true);
			return this.readOnly.booleanValue();
		}

		//
		// if  (getDomain().getService(WorkflowDomainService.class)!=null    && 
		//	 getLaunchers().size()>0 										&& 
		//	 getLaunchers().get(0).executeable())
		//	 return true;
		//  return false;
		//

		// -------------------------------
		//
		// Archived can only be moved to the Library
		//
		if (getModelObject().isArchived()) { 
				this.readOnly=Boolean.valueOf(true);
				return this.readOnly.booleanValue();
		}

		
		//  -------------------------------
		//
		// Recycled can be Restored
		// 
		if (getModelObject().isRecycled()) { 
			this.readOnly=Boolean.valueOf(false);
			return this.readOnly.booleanValue();
		}

		// If at least one of the Libraries of the file is not ReadOnly 
		// 
		if (getModelObject().isEnabled()) {
			List<Library> libraries = getModelObject().getDomain().getService(LibraryService.class).getLibraries(getModelObject());
			if (!libraries.isEmpty()) { 
				for (Library li: libraries)
					if (!li.isReadOnly()) {
						this.readOnly=Boolean.valueOf(false);
						return this.readOnly.booleanValue();
					}
			}
		}
		
		this.readOnly = Boolean.valueOf(true);
		return readOnly.booleanValue();
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}	

	protected boolean isWriteable(IModel<T> model) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isWriteable(model.getObject());
	}
	
 }


