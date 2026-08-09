package kbee.web.dashboard;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.browser.RefreshClickEvent;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.DummyBlockPanel;


@SuppressWarnings("serial")
public abstract class DashboardWidgetBasePanel extends KBPanel {
	
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DashboardWidgetBasePanel.class.getName());
	
	private IModel<String> title;
	private IModel<User> model_session_user = null;
	
	private String  preferences_key;
	private WebMarkupContainer header, base_bottom, base_alert;
	private WebMarkupContainer icon;
	
	private boolean isViewMode  = false;
	private boolean isEdit  = false;
	private boolean isHelp  = false;
	private boolean isCollapsed  = false;
	
	protected final boolean is_root		   = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	protected final boolean is_admin     = is_root || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());

	public class BaseHeader extends Fragment {
		
		public BaseHeader() {
			super("header", "base-header-fragment", DashboardWidgetBasePanel.this);
			setOutputMarkupId(true);
		}
		
		@Override
		public void onInitialize() {
			super.onInitialize();
	
			
			Link<Void> tl = new Link<Void>("title-link") {
				@Override
				public void onClick() {
					DashboardWidgetBasePanel.this.onTitleClick();
				}
			};
			
			add(tl);
			
			tl.add(new Label("title", getTitle()).setEscapeModelStrings(false));
			
			AjaxLink<Void> collapse = new AjaxLink<Void>("collapse") {
				public boolean isVisible() {
					return isCollapsable();
				}
				@Override
				public void onClick(AjaxRequestTarget target) {
					isCollapsed = !isCollapsed;
					icon.add(new AttributeModifier("class", new Model<String>() {
						public String getObject() {
							return  isCollapsed ? "far fa-angle-up" : "far fa-angle-down"; 
						}
					}));
					DashboardWidgetBasePanel.this.setUserPreference("expanded", isCollapsed?  "no" : "yes");
					target.add(BaseHeader.this);
					DashboardWidgetBasePanel.this.onClickCollapse(target);
				}
			};

			
			icon = new WebMarkupContainer("collapse-icon");
			icon.add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					return  isCollapsed ? "far fa-angle-up" : "far fa-angle-down"; 
				}
			}));
			
			
			collapse.add(icon);
			Label cl= new Label("expand-label", new StringResourceModel("expand", this, null)) {
				public boolean isVisible() {
					return isCollapsed;
				}
			};
			collapse.add(cl);
			
			
			AjaxLink<Void> re = new AjaxLink<Void>("refresh") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					DashboardWidgetBasePanel.this.refresh(target);
				}
				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					IAjaxCallListener listener = new IAjaxCallListener() {
						@Override
						public CharSequence getSuccessHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getPrecondition(Component component) {
							return null;
						}
						@Override
						public CharSequence getFailureHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getCompleteHandler(Component component) {
							String s = null, s1=null;
							String id = component.getMarkupId();
							s1 = "document.getElementById('"+id+"').innerHTML = '"+"<i class=\"far fa-sync \"/>"+"';";
							 s ="setTimeout(function () {"+s1+"}, 350);";
							return s;
						}
						@Override
						public CharSequence getBeforeSendHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getBeforeHandler(Component component) {
							String s = null;
							String id = component.getMarkupId();
							s = "document.getElementById('"+id+"').innerHTML = '<i class=\"far fa-sync fa-spin  spinning\"></i>'";
							return s;
						}
						@Override
						public CharSequence getAfterHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getDoneHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getInitHandler(Component component) {
							return null;
						}
					};
					attributes.getAjaxCallListeners().add(listener);
				}
			};
			
			
			AjaxLink<Void> help = new AjaxLink<Void>("help") {
				public boolean isVisible() {
					return isHelp();
				}
				@Override
				public void onClick(AjaxRequestTarget target) {
					DashboardWidgetBasePanel.this.onHelp(target);
				}
				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					IAjaxCallListener listener = new IAjaxCallListener() {
						@Override
						public CharSequence getSuccessHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getPrecondition(Component component) {
							return null;
						}
						@Override
						public CharSequence getFailureHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getCompleteHandler(Component component) {
							String s = null, s1=null;
							String id = component.getMarkupId();
							s1 = "document.getElementById('"+id+"').innerHTML = '"+"<i class=\"fal fa-info-circle \"/>"+"';";
							 s ="setTimeout(function () {"+s1+"}, 350);";
							return s;
						}
						@Override
						public CharSequence getBeforeSendHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getBeforeHandler(Component component) {
							String s = null;
							String id = component.getMarkupId();
							s = "document.getElementById('"+id+"').innerHTML = '<i class=\"far fa-sync fa-spin   spinning\"></i>'";
							return s;
						}
						@Override
						public CharSequence getAfterHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getDoneHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getInitHandler(Component component) {
							return null;
						}
					};
					attributes.getAjaxCallListeners().add(listener);
				}
			};
			
			AjaxLink<Void> edit = new AjaxLink<Void>("edit") {
				public boolean isVisible() {
					return isEdit();
				}
				@Override
				public void onClick(AjaxRequestTarget target) {
					DashboardWidgetBasePanel.this.onEdit(target);
				}
				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					IAjaxCallListener listener = new IAjaxCallListener() {
						@Override
						public CharSequence getSuccessHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getPrecondition(Component component) {
							return null;
						}
						@Override
						public CharSequence getFailureHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getCompleteHandler(Component component) {
							String s = null, s1=null;
							String id = component.getMarkupId();
							s1 = "document.getElementById('"+id+"').innerHTML = '"+"<i class=\"far fa-sync \"/>"+"';";
							 s ="setTimeout(function () {"+s1+"}, 350);";
							return s;
						}
						@Override
						public CharSequence getBeforeSendHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getBeforeHandler(Component component) {
							String s = null;
							String id = component.getMarkupId();
							s = "document.getElementById('"+id+"').innerHTML = '<i class=\"far fa-edit spinning\"></i>'";
							return s;
						}
						@Override
						public CharSequence getAfterHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getDoneHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getInitHandler(Component component) {
							return null;
						}
					};
					attributes.getAjaxCallListeners().add(listener);
				}
			};
			

			
			

			// fa-duotone fa-grip-lines
			
			/**
			AjaxLink<Void> viewmode = new AjaxLink<Void>("view") {
				public boolean isVisible() {
					return isViewMode();
				}
				@Override
				public void onClick(AjaxRequestTarget target) {
					DashboardWidgetBasePanel.this.onViewMode(target);
				}
				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					IAjaxCallListener listener = new IAjaxCallListener() {
						@Override
						public CharSequence getSuccessHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getPrecondition(Component component) {
							return null;
						}
						@Override
						public CharSequence getFailureHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getCompleteHandler(Component component) {
							String s = null, s1=null;
							String id = component.getMarkupId();
							s1 = "document.getElementById('"+id+"').innerHTML = '"+"<i class=\"fa-duotone " +  (!isViewMode()  ?"fa-grip-lines" : "fa-diagram-cells")+ "\"/>"+"';";
							 s ="setTimeout(function () {"+s1+"}, 350);";
							return s;
						}
						@Override
						public CharSequence getBeforeSendHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getBeforeHandler(Component component) {
							String s = null;
							String id = component.getMarkupId();
							s = "document.getElementById('"+id+"').innerHTML = '<i class=\"fa spinning\"></i>'";
							return s;
						}
						
						@Override
						public CharSequence getAfterHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getDoneHandler(Component component) {
							return null;
						}
						@Override
						public CharSequence getInitHandler(Component component) {
							return null;
						}
					};
					attributes.getAjaxCallListeners().add(listener);
				}
			};
			*/

			add(collapse);
			add(help);
			add(edit);
			add(re);
			
			
			WebMarkupContainer menuCon = new WebMarkupContainer("menu-container");
			add(menuCon);
			menuCon.setVisible(isMenu());
			menuCon.add(getMenu());
			
		}
	} 
	
		
	
	public DashboardWidgetBasePanel(String id,  String preferences_key) {
		super(id);
		this.preferences_key = (preferences_key!=null ? 
				(preferences_key+ "-" + this.getClass().getCanonicalName()) : this.getClass().getName());
		isCollapsed  = getUserPreference("expanded", "yes").equals("no") ;
		
	}
	
	
	protected abstract void onTitleClick();


	protected Panel getMenu() {
		return new InvisiblePanel("menu");
	}
	
	
	public boolean isMenu() {
		return false;
	}

	protected List<ToolbarItem> getToolbarItems() {
		return null;
	}

	public void setViewMode(boolean v) {
		this.isViewMode=v;
	}
	
	public boolean isViewMode() {
		return isViewMode;
	}
	
	
	public boolean isEdit() {
		return isEdit;
	}

	public void setEdit(boolean isEdit) {
		this.isEdit = isEdit;
	}

	public void setHelp(boolean isHelp) {
		this.isHelp = isHelp;
	}

	public boolean isHelp() {
		return isHelp;
	}
	
	public void setHeader (WebMarkupContainer h) {
		if (!h.getId().contentEquals("header"))
			throw new IllegalArgumentException(" id must be = 'header'");
		header=h;
		if (this.isInitialized())
			addOrReplace(header);
	}
	
	public WebMarkupContainer getBottomPanel() {
		return this.base_bottom;
	}

	public WebMarkupContainer getAlertPanel() {
		return this.base_alert;
	}

	public void  setBottomPanel(WebMarkupContainer b) {
		this.base_bottom=b;
		addOrReplace(this.base_bottom);
	}

	public void setAlertPanel(WebMarkupContainer c) {
		this.base_alert=c;
		addOrReplace(this.base_alert);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);

		if (header==null) 
			header=new BaseHeader();
		
		if (base_bottom==null)
			base_bottom = new InvisiblePanel("base-bottom");

		if (base_alert==null)
			base_alert = new InvisiblePanel("base-alert");

		addOrReplace(header);
		addOrReplace(base_alert);
		addOrReplace(base_bottom);
		
		add(new AbstractAjaxTimerBehavior(java.time.Duration.ofMinutes(3)) {
			@Override
			protected void onTimer(AjaxRequestTarget target) {
				try {
  					refresh(target);
				}
				catch (Exception e) {
					logger.error(e);
				}
				finally {
					restart(target);
				}
			}
		});
		
	}

	@Override
	public void addListeners() {
		super.addListeners();
		add(new WicketEventListener<RefreshClickEvent>() {
			@Override
			public void onEvent(RefreshClickEvent event) {
				refresh(event.getRequestTarget());
			}
		});
	}

	
	abstract protected void onHelp(AjaxRequestTarget target);
	abstract protected void onClickCollapse(AjaxRequestTarget target);
	
	
	protected WebMarkupContainer getHeader() {
		return this.header;
	}
	
	
	protected void onEdit(AjaxRequestTarget target)		 {}
	protected void onViewMode(AjaxRequestTarget target, String criteria)  {}
	
	
	
	
	protected void setCollapsed( boolean b) {
		isCollapsed=b;
	}
	
	protected boolean isCollapsed() {
		return isCollapsed;
	}

	
	protected int getIntUserPreference(String key) {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null) 
			return user.getService(PreferencesService.class).getIntValue("dashboard-"+ this.preferences_key, key, 0);
		return 0;
	}
	
	protected String getUserPreference(String key, String default_value) {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null) {
			return user.getService(PreferencesService.class).getValue("dashboard-"+ this.preferences_key, key, default_value);
		}
		return default_value;
	}
	
	
	protected String getUserPreference(String key) {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null) 
			return user.getService(PreferencesService.class).getValue("dashboard-"+this.preferences_key, key);
		return null;
	}
		
	protected void setUserPreference(String key, String value) {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null) {
			user.getService(PreferencesService.class).setValue("dashboard-"+this.preferences_key, key, value);
		}	
	}
	
	protected void setIntUserPreference(String key, int value) {
		KbeeUser user = (KbeeUser) getSessionUser();
		if (user!=null) {
			user.getService(PreferencesService.class).setIntValue("dashboard-"+this.preferences_key, key, value);
		}	
	}
		
	public IModel<String> getTitle() {
		return title;
	}
	
	public void setTitle(IModel<String> title) {
		this.title=title;
	}
	
	protected void refresh(AjaxRequestTarget target) {
		getBehaviors(AbstractAjaxTimerBehavior.class).get(0).restart(target);
		target.add(this);
	}

	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	protected Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}

	protected UserProfile getSessionUserProfile() {
		return getContentDao().findUserProfileByUser(getSessionUser());
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	protected void setPreferencesKey(String s) {
		this.preferences_key=s;
	}
	
	protected String getPreferencesKey() {
		return this.preferences_key;
	}
	
	protected boolean isExpressVersion() {
		return getDomain().getDomainType()==DomainType.EXPRESS;
	}

	
	public void onDetach() {
		super.onDetach();
		if (model_session_user != null)
			model_session_user.detach();
	}
	
	protected KbeeUser getSessionUser() {
		try {
			if (model_session_user != null && model_session_user.getObject() != null)
				return (KbeeUser) model_session_user.getObject();

			User session_user = ServiceLocator.getService(SecurityService.class).getSessionUser();
			model_session_user = new ObjectModel<User>(session_user);
			return (KbeeUser) model_session_user.getObject();
		} catch (Exception e) {
				logger.error(e);
			return null;
		}
	}

	protected <R> DomRepository<R> getRepository(Class<R> objectclass) {
		DomRepository<R> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
	}
	
	protected WebMarkupContainer getHelpPanel() {
		return new DummyBlockPanel("help", new Model<String>(getClass().getName()));
	}
	

	protected  boolean isRoot() {
		return is_root;
	}



	protected boolean isAdmin() {
		return is_admin;
	}
	
	protected boolean isCollapsable() {
		return true;
	}


	
}
