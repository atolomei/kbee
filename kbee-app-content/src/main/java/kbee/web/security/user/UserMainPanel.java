package kbee.web.security.user;


import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.PersonMember;
import com.novamens.content.model.PersonSet;
import com.novamens.content.model.UserSet;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserProfileType;
import com.novamens.content.user.UserService;
import com.novamens.dom.DomainType;
import com.novamens.kbee.security.KbeeUser;

import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.markup.html.page.PageMainTabs;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.security.audit.AuditSet;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;
import com.novamens.util.PropertiesFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;
import kbee.web.dataset.PersonMemberPanel;
import kbee.web.error.ErrorPanel;
import kbee.web.model.object.AuditTrailObjectPanel;
import kbee.web.object.ObjectStateEditor;
import kbee.web.service.PortalPanelService;
import kbee.web.util.PanelBeanResolver;

@SuppressWarnings("serial")
public class UserMainPanel extends ObjectEditor<Person> implements PageMainTabs {
			
	private static final long serialVersionUID = 1L;
																							
	private static Logger logger = Logger.getLogger(UserMainPanel.class.getName());

	final boolean is_root = 
		ServiceLocator
		.getService(SecurityService.class)
		.isRoot();
	
	final boolean is_admin 	= is_root || 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	final boolean role_security = is_root || is_admin || 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.SECURITY.getId());

	final boolean is_external = !is_root && !is_admin && 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.EXTERNAL_USER.getId());
	
	final boolean role_super = is_root || 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.SU.getId());
	
	private static String LoginExternalEnabled =
		PropertiesFactory
		.getInstance("kbee")
		.getProperties()
		.getProperty("kbee.user.login.external.enabled", "true");
	
	private static String BillingEnabled =
		PropertiesFactory
		.getInstance("kbee")
		.getProperties()
		.getProperty("kbee.user.billing.enabled", "true");
	
	private static String DigitalSignatureEnabled =
		PropertiesFactory
		.getInstance("kbee")
		.getProperties()
		.getProperty("kbee.user.signature.enabled", "true");

	private String initial_tab;
	
	private boolean isMyAccount;
	private boolean isPortalMode;
	private boolean gotoLastSelection;
	private String tab;

	private  VerticalLayout<ITab> layout;


	public class NullUser implements User {
		
		public Serializable getId() {
			return 0;
		}
		public Date getLastModifiedDate() {
			return null;
		}
		public void setLastModifiedDate(Date lastModifiedDate) {
		}
		public User getLastModifiedUser() {
			return null;
		}
		public void setLastModifiedUser(User lastModifiedUser) {
		}
		public String getDisplayName() {
			return null;
		}
		public String getLastFirstName() {
			return null;
		}
		public String getFirstLastName() {
			return null;
		}
		public String getName() {
			return null;
		}
		public String getUserName() {
			return "new person";
		}
		public String getPassword() {
			return null;
		}
		public boolean isEnabled() {
			return false;
		};
		public boolean isActive() {
			return false;
		};
		public void setEnabled(boolean enabled) {
		}
		public void setActive(boolean enabled) {
		}
		public void setGroups(Set<Group> groups) {
		}
		public Set<Group> getGroups() {
			return new HashSet<Group>();
		}
		
		public List<Group> getStandardGroups() {
			return new ArrayList<Group>();
		}
		
		public void addGroup(Group group) {
		}
		public void removeGroup(Group group) {
		}
		public boolean isMember(Group group) {
			return false;
		}
		public String getUserHash(Date date) {
			return null;
		}
		public void setLocale(String locale_str) {
		}
		public void setLocale(Locale locale) {
		}
		public Locale getLocale() {
			return null;
		}
		public boolean isCanonical() {
			return false;
		}
		@Override
		public OffsetDateTime getCreationOffsetDateTime() {
			return null;
		}
		@Override
		public void setCreationOffsetDateTime(OffsetDateTime lastModifiedOffsetDate) {
		}
		
		@Override
		public OffsetDateTime getLastModifiedOffsetDateTime() {
			return null;
		}
		@Override
		public void setLastModifiedOffsetDateTime(OffsetDateTime lastModifiedOffsetDate) {
		}
		@Override
		public String getTimeZone() {
			return null;
		}
		@Override
		public void setTimeZone(String tz) {
		}
		@Override
		public String getFirstName() {
			return null;
		}
		@Override
		public String getLasName() {
			return null;
		}
		@Override
		public void setDefaultAudit() {
		}
		@Override
		public ZoneId getZoneId() {
			return null;
		}
		@Override
		public String getLastModifiedOffsetDateTimeColloquial(String css) {
			return null;
		}
		@Override
		public String getCreationOffsetDateTimeColloquial() {
			return null;
		}
		@Override
		public AuditSet getAuditSet() {
			return AuditSet.SECURITY;
		}
		@Override
		public void setStateEnabled() {
			// TODO Auto-generated method stub
			
		}
		@Override
		public boolean isArchived() {
			// TODO Auto-generated method stub
			return false;
		}
		@Override
		public void setStateArchived() {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void setStateDeleted() {
			// TODO Auto-generated method stub
			
		}
		@Override
		public boolean isDeleted() {
			// TODO Auto-generated method stub
			return false;
		}
		@Override
		public OffsetDateTime getValidityAccessDate() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	public class UserModel implements IModel<User> {
		private IModel<UserProfile> model;
		public UserModel(IModel<UserProfile> model) {
			this.model = model;
		}
		public User getObject() {
			return model.getObject().getUser()==null ? new NullUser() : model.getObject().getUser();
		}
		public void setObject(User user) {
		}
		public void detach() {
			model.detach();
		}
	}
	
	/** ------------------------------------------
	 * 
	 * @param id
	 * @param parameters
	 */
	public UserMainPanel(String id, PageParameters parameters) {
		super(id);
		if (parameters!=null && parameters.get("person")!=null) {
			String pe =  parameters.get("person").toString();
			try {
				Person person = getContentDao().findPersonById(Long.valueOf(pe));
				IModel<Person> model = new ObjectModel<Person>(person);
				if (person==null)
					throw new IllegalArgumentException("person is null");
				setModel(model);
				this.isMyAccount = parameters.get("myaccount").toString("yes").equals("yes");
				this.isPortalMode = parameters.get("portal").toString("yes").equals("yes");
			} catch (Exception e) {
				logger.error(e);
				throw new IllegalArgumentException(e);
			}
			
		}
		
	}

	
	/** ------------------------------------------------------------------------------
	 * @param model
	 */
	public UserMainPanel(IModel<Person> model) {
		this(model, false);
	}
	

	public UserMainPanel(IModel<Person> model, boolean ismyaccount) {
		this("editor", model, ismyaccount);
	}

	public UserMainPanel(String id, IModel<Person> model, final boolean ismyaccount) {
		this(id, model, ismyaccount, false, false);
	}
	
	public UserMainPanel(String id, IModel<Person> model, boolean ismyaccount, boolean isPortalMode, boolean gotolastselection) {
		super(id, model);
		setModel(model);
		isMyAccount = ismyaccount;
		this.isPortalMode = isPortalMode;
		gotoLastSelection = gotolastselection;
	}
	
	public String getTab() {
		return tab;
	}
	
	public void setTab(String tab) {
		this.tab=tab;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		addTabs(isMyAccount, getTab(), gotoLastSelection);
		
		int sel = layout.getSelectedTab();
		
		if (sel==-1)
			sel=0;
		
		String str = (layout.getTabs().get(sel)).getTitle().getObject();
		((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
		
	}
	
	public void onCancel(AjaxRequestTarget target) {
	}
	
	@Override
	public void setEditionEnabled(boolean editionEnabled) {
		super.setEditionEnabled(editionEnabled);
		if (editionEnabled) {
			@SuppressWarnings("unchecked")
			VerticalLayout<ITab> editor = (VerticalLayout<ITab>)get("tabs");
			if (editor!=null)
				editor.setSelectedTab(0);		
		}
	}
	
	@Override
	public void setInitialTab(String a) {
		initial_tab=a;
		try {
			if (layout!=null)
				layout.setSelectedTab(a);
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}

	@Override
	public String getInitialTab() {
		return initial_tab;
	}

	/**
	 * @param ismyaccount
	 * @param seltab
	 * @param gotolastselection
	 * @return
	 */
	protected void addTabs(final boolean ismyaccount, final String seltab, final boolean gotolastselection) {
		
		try {
		
			List<ITab> tabs = new ArrayList<ITab>();
			
			tabs.add(new AbstractTabKB(getLabel("editor.person"), "person") {
				@Override
				public Panel getPanel(String panelId) {
					return new PersonFormEditor(panelId, 
						new ObjectModel<DataSetMember>(getMember()), 
						isNew(),
						!role_security &&	
							ismyaccount && 
							!getUserProfile().isEditPersonEnabled());
				}
			});
					
			tabs.add(new AbstractTabKB(getLabel("editor.user"), "account") {
				@Override
				public Panel getPanel(String panelId) {
					return new UserEditor(panelId, 
						getProfileModel(), 
						ismyaccount, 
						isPortalMode);
				}
			});
					
			if ((ismyaccount && getUserProfile().isChangePasswordEnabled()) || role_security) {
				tabs.add(new AbstractTabKB(getLabel("editor.password"), "password") {
					@Override
					public Panel getPanel(String panelId) {
						return new UserPasswordEditor(panelId, 
							getProfileModel(), 
							ismyaccount);
					}
				});
			}
			
			if (!ismyaccount && "true".equals(DigitalSignatureEnabled)) {
				tabs.add(new AbstractTabKB(getLabel("sms"), "sms") {
					@Override
					public Panel getPanel(String panelId) {
						return new PersonSMSEditor(panelId, getModel(), ismyaccount);
					}
				});
			}
					
			if ("true".equals(LoginExternalEnabled)) {
				tabs.add(new AbstractTabKB(getLabel("editor.externalLogin"), "externalLogin") {
					@Override
					public Panel getPanel(String panelId) {
						return new UserExternalLoginEditor(panelId, 
							getProfileModel());
					}
				});
			}		
					
			if (!ismyaccount && "true".equals(DigitalSignatureEnabled)) {
				tabs.add(new AbstractTabKB(getLabel("editor.signatures"), "signature") {
					@Override
					public Panel getPanel(String panelId) {
						try {
							return new UserSignaturesEditor(panelId, 
								getProfileModel());
						} 
						catch (Exception e) {
							logger.error(e);
							return new ErrorPanel(panelId, e);
						}
					}
				});
			}
					
					
			for (PersonSet personset : getPersonSets()) {
				IModel<PersonSet> datasetmodel = new ObjectModel<PersonSet>(personset);
				tabs.add(new AbstractTabKB(new Model<String>("DataSet " + personset.getDisplayName())) {
					@Override
					public Panel getPanel(String panelId) {
						return new PersonMemberPanel(panelId, getPersonModel(), datasetmodel);
					}
				});
			}
			
			if (!is_external && (role_security || !ismyaccount || isWorkflowUser())) {
				tabs.add(new AbstractTabKB(getLabel("editor.roles"), "roles") {
					@Override
					public Panel getPanel(String panelId) {
						return new PanelBeanResolver(
							"userroles-editor", 
							panelId).getPanel();
					}
				});
			}			
			
			if (!is_external && (role_security || !ismyaccount || isWorkflowUser())) {
				tabs.add(new AbstractTabKB(getLabel("editor.emailrules"), "alerts") { // era -> "emailalerts"
					@Override
					public Panel getPanel(String panelId) {
						return new UserEMailRulesPanel(panelId, 
							getProfileModel(), 
							ismyaccount);
					}
				});
			}	
						
			if (!is_external && !ismyaccount) {
				tabs.add(new AbstractTabKB(getLabel("editor.workflow"), "workflow") {
					@Override
					public Panel getPanel(String panelId) {
						return new UserWorkflowEditor(panelId, 
							getProfileModel(), 
							false);
					}
				});
			}	
			
			if (!ismyaccount) {
				tabs.add(new AbstractTabKB(getLabel("status"), "status") {
					@Override
					public Panel getPanel(String panelId) {
						return new ObjectStateEditor<User>(panelId, 
							getUserModel(), 
							ismyaccount || isPortalMode || (!role_security));
					}
				});
			}	
					
			if (!ismyaccount && "true".equals(BillingEnabled)) {
				tabs.add(new AbstractTabKB(getLabel("billing"), "billing") {
					@Override
					public Panel getPanel(String panelId) {
						try {
							return new UserBillingEditor(panelId, 
								getProfileModel(), 
								ismyaccount);
						} 
						catch (Exception e) {
							logger.error(e);
							return new ErrorPanel(panelId, e);
						}
					}
				});
			}		
					
			if (!is_external) {
				tabs.add(new AbstractTabKB(getLabel("editor.activity"), "activity") {
					@Override
					public Panel getPanel(String panelId) {
						return new UserActivityPanel(panelId, getModel());
					}
				});
			}	
					
			if (!is_external  && (!ismyaccount || is_root || role_security)) {
				tabs.add(new AbstractTabKB(getLabel("editor.audit"), "audit") {
					@Override
					public Panel getPanel(String panelId) {
						return new AuditTrailObjectPanel<User>(panelId, getUserModel());
					}
				});
			}
					
			if (is_root) {
				tabs.add(new AbstractTabKB(getLabel("editor.groups"), "groups") {
					@Override
					public Panel getPanel(String panelId) {
						return new UserGroupsEditor(panelId, getUserModel(), false);
					}
				});
			}
					
			layout = new VerticalLayout<ITab>("tabs", "user-main-panel",tabs, VerticalLayout.VERTICAL) {
				@Override
				protected void onAjaxUpdate(AjaxRequestTarget target) {
					String str = this.getTabs().get(this.getSelectedTab()).getTitle().getObject();
					((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
					((KbeeUser) getSessionUser()).getService(PreferencesService.class).setIntValue(UserMainPanel.class.getName(), "selectedtab", getSelectedTab());
				}
			};
			
					
			layout.setMenuItemFactory(getMenuItems());
			layout.setSections(VerticalLayout.COLS_9X3);
					
			layout.setTitle(new StringResourceModel("sections", this, null));
			add(layout);
		
		} 
		catch (Exception e) {
			logger.error(e);
			addOrReplace(new ErrorPanel("sections",e));
		}
	}
	
	protected void onClose(AjaxRequestTarget target) {
	}

//	protected ContentDao getContentDao() {
//		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}

	protected PersonMember getMember() { 
		Person person = getModel().getObject();
		if (getModel().getObject() instanceof DataSetMember) {
			return (PersonMember) getModel().getObject();
		}
//		List<DataSetMember> members = getContentDao().findMembersByEntity(person);
		for (DataSetMember member : getContentDao().findMembersByEntity(person)) {
			if (member.getDataSet() instanceof UserSet) {
				member =  (PersonMember) getContentDao().reload(member);
				return (PersonMember)member;
			}
		}
		return null;
	}

	protected boolean isFreeVersion() {
		return getModel().getObject().getDomain().getDomainType()==DomainType.EXPRESS;
	}
	
	protected boolean isRootUser() {
		return ServiceLocator.getService(SecurityService.class).isRoot();
	}
	
	protected UserProfile getUserProfile() {
		return getModel().getObject().getProfile(UserProfile.class);
	}
	
	protected boolean isWorkflowUser() {
		return is_root || is_admin ||
			UserProfileType.WORKFLOW_PARTICIPANT.equals(getUserProfile().getType());
	}
	
	private IModel<UserProfile> getProfileModel() {
		return new ObjectModel<UserProfile>(getModel().getObject().getProfile(UserProfile.class));
	}

	private IModel<User> getUserModel() {
		return new UserModel(new ObjectModel<UserProfile>(getModel().getObject().getProfile(UserProfile.class)));
	}
	
	private IModel<Person> getPersonModel() {
		return new ObjectModel<Person>(getUserProfile().getPerson());
	}

//	private List<Classifier> getClassifiers(Person person) {
//		Assert.isInstanceOf(PersonMember.class, person);
//		return (((PersonMember)person).getDataSet()).getClassifiers();
//	}
	
	protected KbeeUser getSessionUser() {
		try {
			User session_user = ServiceLocator.getService(SecurityService.class).getSessionUser();
				return (KbeeUser) session_user;
		} 
		catch (Exception e) {
			logger.error(
				" {} | {} | {} | {}", "getSessionUser() gave the error", 
				e.getClass().getName(), 
				Thread.currentThread().getStackTrace()[1].getMethodName(), 
				e.getMessage());
			return null;
		}
	}
	
	/**
	 * [VER AT]
	 * 
	 * @param profile
	 * @return
	 */
//	private String getClass(Profile profile) {
//		String classname = profile.getClass().getSimpleName().toLowerCase();
//		int i = classname.indexOf("_");
//		if (i>0) 
//			classname = classname.substring(0, i);
//		return classname;
//	}
	
	/**
	 * Library
	 * Archive
	 * RecycleBin
	 * 
	 * @return
	 */
	protected List<MenuItemFactory<Panel>> getMenuItems() {
		
		List<MenuItemFactory<Panel>> list = new ArrayList<MenuItemFactory<Panel>>();
				
		
		
        list.add(new MenuItemFactory<Panel>() {
            @Override
            public AbstractMenuItemPanelV5<Panel> getItem(String id) {
                return new AjaxMenuItemPanelV5<Panel>(id) {
                    public void onClick(AjaxRequestTarget target) {
                        // ServiceLocator.getService(SecurityContentMgmtService.class).enable(UserMainPanel.getModel().getObject().get);
                        
                    }
                    public String getLabel() {
                        return "Enable";
                    }
                    @Override
                    public boolean isVisible() {
                    	try {
                    		return   getUser() != null && 
                    				!getUser().isEnabled() && 
                    				!getUser().getName().startsWith("root@"); 

                    	}
                    	catch (Exception e) {
                    		logger.error(e);
                    		return false;
                    	}
                    }
                    public User getUser() {
                    	return null;
                        // return (getModel().getObject().getProfile(UserProfile.class)).getUser();
                    }
                    @Override
                    public String getWorkingLabel() {
                        return "working";
                    }
                    @Override
                    public boolean isEnabled() {
                        //if (is_support && !is_root)
                         //   return false;
                        return true;
                    }
                };
            }
        });
		
		list.add(new MenuItemFactory<Panel>() {
			@Override
			public AbstractMenuItemPanelV5<Panel> getItem(String id) {
				return new MenuItemPanelV5<Panel>(id) {
					@Override
					public void onClick() {
                        try {
                        	ServiceLocator.getService(UserService.class)
                        		.impersonate(getUserProfile().getUser());
                            WebPage page = ServiceLocator
                            	.getService(PortalPanelService.class)
                            	.getStartPage(getUserProfile());
                            page.getSession()
                            	.setLocale(getUserProfile().getUser().getLocale());
                            setResponsePage(page);
                        } 
                        catch (Exception e) {
                            logger.error(e);
                        }
					}
					@Override
					public String getLabel() {
						return new StringResourceModel("impersonate", UserMainPanel.this, null).getObject();
					}
					@Override 
					public boolean isVisible() {
						return role_super;
					}
					@Override 
					public boolean isEnabled() {
						return role_super && !(getUserProfile().getUser().getUserName().startsWith("root@"));
					}
				};	
			}	
		});

		return list;
	}
	
	private List<PersonSet> getPersonSets() {
		List<PersonSet> datasets = new ArrayList<PersonSet>();
		for (DataSet dataset : getContentDao().getDataSets(getDomain())) {
			if (dataset instanceof PersonSet) {
				datasets.add((PersonSet)dataset);
			}
		}
		return datasets;
	}
}