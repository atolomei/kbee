package kbee.web.nav;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.base.Resource;
import com.novamens.content.entity.Person;
import com.novamens.content.service.domain.DomainSettingsService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserProfileType;
import com.novamens.content.user.UserService;

import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.services.BrandingWebService;
import com.novamens.kbee.wicket.util.GenericPhoto;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailSize;
import com.novamens.util.PropertiesFactory;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.WebMarkupContainerMenuItemPanel;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.TextCleaner;

import kbee.web.entity.UserHeaderPanel;
import kbee.web.event.wicket.ShowTipOfTheDayEvent;
import kbee.web.resource.ResourceThumbnailImage;
import kbee.web.service.ApplicationSiteMapService;

@SuppressWarnings("serial")
public class NavBarUserMenu extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	static Logger logger = LogManager.getLogger(NavBarUserMenu.class.getName());

	static final int WIDTH = 32;
	
	final boolean is_root = ServiceLocator
			.getService(SecurityService.class)
			.isRoot();
	
	final boolean is_admin  = ServiceLocator
			.getService(SecurityService.class)
			.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	
	final boolean is_useradmin  = is_admin || is_root || ServiceLocator
			.getService(UserService.class)
			.isUserAdmin();
	
	final boolean is_security  = is_admin || is_root || ServiceLocator
			.getService(SecurityService.class)
			.isMember(KbeeGlobalRole.SECURITY.getId());
	
	private static String DigitalSignatureEnabled =
			PropertiesFactory
				.getInstance("kbee")
				.getProperties()
				.getProperty("kbee.user.signature.enabled", "true");

	private static String MenuTarget =
			PropertiesFactory
				.getInstance("kbee")
				.getProperties()
				.getProperty("kbee.user.behavior.target", null);
	
	public static final ResourceReference EMPTY_PHOTO = 
		new PackageResourceReference(UserHeaderPanel.class, "NoPicture.gif");

	private boolean only_user_account;
	
	private boolean newTab;

	IModel<User> sessionUser = null;
	IModel<Person> sessionPerson = null;
	
	
	
	public NavBarUserMenu(String id) {
		this(id, false);
	}
	
	public NavBarUserMenu(String id, boolean only_user_account) {
		this(id, only_user_account, false);
	}
	
	public NavBarUserMenu(String id, boolean only_user_account, boolean newtab) {
		super(id);
		this.only_user_account=only_user_account;
		this.newTab=newtab;
		setOutputMarkupId(true);
	}
	
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (sessionUser!=null)
			sessionUser.detach();
		if (sessionPerson!=null)
			sessionPerson .detach();
	}

	/**
	 * @author 
	 */
	private class UserInfoFragment extends Fragment {
		
		public UserInfoFragment(String id) {
			super(id, "userInfoFragment", NavBarUserMenu.this);
		}
		
		public void onInitialize() {
			super.onInitialize();
			Link<Void> li= new Link<Void>("user-link") {
				@Override
				public void onClick() {
					setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("user-myaccount-page"));	
				}
			};
			
			add(li);
			
			WebMarkupContainer wp 		= new WebMarkupContainer("phoneContainer");
			WebMarkupContainer wemail 	= new WebMarkupContainer("emailContainer");

			wp.setVisible(getPerson().getPhone()!=null);
			wemail.setVisible(getPerson().getEmail()!=null);
			
			li.add(wemail);
			li.add(wp);
			
			li.add(new Label("username", getSessionUser().getUserName()));
		
			wemail.add(new Label("email", getPerson().getEmail()));
			wp.add(new Label("phone", getPerson().getPhone()!=null?getPerson().getPhone():""));
			
		}
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);
		menu.setPopper(false);
		
		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				UserInfoFragment d = new UserInfoFragment("panel");
				WebMarkupContainerMenuItemPanel<Void> w=new WebMarkupContainerMenuItemPanel<Void>(id, null, d);
				return w;
				}
			});

		
		menu.addItem(id ->
			new MenuItemPanelV5<Void>(id) {
				public void onClick() {
					setResponsePage( NavBarUserMenu.this.getAccountPage());
				}
				@Override 
				public String getLabel() {
					return getLabelString("mainmenu.myaccount");
				}
				@Override 
				public String getTarget() {
					return isNewTab() ?"_blank" : "";
				}
			});
		
		if (!this.only_user_account) {
			if ("true".equals(DigitalSignatureEnabled)) {
				menu.addItem(new MenuItemFactory<Void>() {
					@Override
					public AbstractMenuItemPanelV5<Void> getItem(String id) {
						return new MenuItemPanelV5<Void>(id) {
							public void onClick() {
								setResponsePage( new RedirectPage( getServerUrl() + "/myaccount?tab=sms"));
							}
							@Override 
							public String getLabel() {
								return (new ResourceModel("phone")).getObject();
							}
							@Override 
							public String getTarget() {
								return isNewTab() ?"_blank" : "";
							}
						};
					}
				});
				
				menu.addItem(new MenuItemFactory<Void>() {
					@Override
					public AbstractMenuItemPanelV5<Void> getItem(String id) {
						return new MenuItemPanelV5<Void>(id) {
							public void onClick() {
								setResponsePage( new RedirectPage( getServerUrl() + "/myaccount?tab=signature"));
							}
							@Override 
							public String getLabel() {
								return (new ResourceModel("my-app")).getObject();
							}
							@Override 
							public String getTarget() {
								return isNewTab() ?"_blank" : "";
							}
						};
					}
				});
			}
			
			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new AjaxMenuItemPanelV5<Void>(id) {
						@Override
						public void onClick(AjaxRequestTarget target) {
							setResponsePage( new RedirectPage( getServerUrl() + "/myaccount?tab=password"));
						}
						@Override
						public String getLabel() {
							return (new ResourceModel("password")).getObject();
						}
					};
				}

			});

			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new AjaxMenuItemPanelV5<Void>(id) {
						@Override
						public void onClick(AjaxRequestTarget target) {
							setResponsePage( new RedirectPage( getServerUrl() + "/myaccount?tab=roles"));
						}
						@Override
						public String getLabel() {
							return (new ResourceModel("my-roles")).getObject();
						}
					};
				}

			});

			menu.addItem(new MenuItemFactory<Void>() {
				@Override
				public AbstractMenuItemPanelV5<Void> getItem(String id) {
					return new AjaxMenuItemPanelV5<Void>(id) {
						@Override
						public void onClick(AjaxRequestTarget target) {
							setResponsePage( new RedirectPage( getServerUrl() + "/myaccount?tab=emailalerts"));
							
						}
						@Override
						public String getLabel() {
							return (new ResourceModel("my-email-alerts")).getObject();
						}
					};
				}

			});
		
		}
		
		else {
			
			menu.addItem(id ->
				new MenuItemPanelV5<Void>(id) {
					@Override
					public void onClick() {
						setResponsePage( new RedirectPage("/myhome"));
					}
					@Override
					public String getLabel() {
						//return getLabelString("workspace-dashobard");
						return getLabelString("user-workspace");
					}
					@Override 
					public String getTarget() {
						return MenuTarget;
					}
					@Override
					public boolean isVisible() {
						return is_root || is_admin || isWorkflowUser();
					}
				});
			
			
			menu.addItem(id ->
				new MenuItemPanelV5<Void>(id) {
					@Override
					public void onClick() {
						setResponsePage( new RedirectPage("/security/users"));
					}
					@Override
					public String getLabel() {
						return getLabelString("users");
					}
					@Override 
					public String getTarget() {
						return MenuTarget;
					}
					@Override
					public boolean isVisible() {
						return is_root || is_admin || is_security || is_useradmin;
					}
				});
		}


		
		if (!only_user_account) {
			
				menu.addItem(new MenuItemFactory<Void>() {
					@Override
					public AbstractMenuItemPanelV5<Void> getItem(String id) {
						return new AjaxMenuItemPanelV5<Void>(id) {
							@Override
							public void onClick(AjaxRequestTarget target) {
								fire(new ShowTipOfTheDayEvent(target));
							}
							@Override
							public String getLabel() {
								return (new ResourceModel("mainmenu.showmetip")).getObject();
							}
							
							@Override
							public boolean isVisible() {
								return ("true").equals(((KbeeUser) getSessionUser()).getDomain().getService(DomainSettingsService.class).get(DomainSettingsService.TIP_OF_THE_DAY));
							}
						};
					}
				});
				

				/*
				menu.addItem(new MenuItemFactory<Void>() {
					@Override
					public MenuItemPanel<Void> getItem(String id) {
						return new AjaxMenuItemPanelV5<Void>(id) {
						 	@Override
							public void onClick(AjaxRequestTarget target) {
						 		((KbeeUser)getUser()).getService(UserSelfService.class).resetPreferences();
						 		((KbeeUser)getUser()).getService(UserSelfService.class).sessionFlush();
						 		
						 		((KbeeUser)getUser()).getService(UserSelfService.class).reindex();
						 		
								RemoveOrphansCommand ro = new RemoveOrphansCommand(((KbeeUser) getUser()).getId()); 
								ro.execute();
							}
							@Override
							public String getLabel() {
								return (new ResourceModel("mainmenu.myaccount.resetpreferences")).getObject();
							}
							@Override 
							public String getWorkingLabel() {
								return (new ResourceModel("working")).getObject();
								
							}
						};
					}
				});
				*/
				
				 
		
		}
		
		
		
		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new SeparatorMenuItemPanelV5<Void>(id) {
					@Override
					public boolean isVisible() {
						return true;
					}
					@Override
					public String getCssClass() {
						return "divider";
					}
				};
			}
		});
		
		
		menu.addItem(new MenuItemFactory<Void>() {
			@Override
			public AbstractMenuItemPanelV5<Void> getItem(String id) {
				return new AjaxMenuItemPanelV5<Void>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						ServiceLocator.getService(UserService.class).logout();
						
						WebPage page = new RedirectPage("/logout");
						getPage().setResponsePage(page);
					}
					@Override
					public String getLabel() {
						return (new ResourceModel("mainmenu.exit")).getObject();
					}
				};
			}
		});

		

		Link<Void> link = new Link<Void>("user-link") {
			public void onClick() {
				 setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage("user-myaccount-page"));
			}	
		};
		
		Label un = new Label("user-name", new Model<String>() {
			public String getObject() {
				User user = getSessionUser();
				
				if (user==null)
					return 	"guest";
				
				if (user.getFirstName()!=null && user.getFirstName().length()>0)
					return user.getFirstLastName();
				
				return user.getLasName()!=null?user.getLasName():user.getId().toString();
				
			}
		});
		
		un.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				if ((getPerson()!=null)) {
					String s = getPerson().getWorkPosition();
					if (s!=null && s.length()>0)
							return "username" + (getSessionUser().getName().startsWith("root@") ? " rootuser":"");
				}
				return "username-standalone";
			}
			
		}));
		
		link.add(un);
		
		link.add(getPhoto());
	
		link.add((new Label("work-position", new Model<String>() {
			public String getObject() {
					if (getPerson()!=null) {
						String wp = getPerson().getWorkPosition();
						if (wp!=null)
							return TextCleaner.truncate(wp, 18);
						return "";
					}
					return "";
				}}) {
			@Override
			public boolean isVisible() {
				if (getPerson()!=null) 
						return getPerson().getWorkPosition()!=null;
				return false;
			}
		}));

		
		add(link);
		add(menu);

		
		
		
		//String style = null;
		/**
		if (getPerson()!=null && getPerson().getPhoto()!=null) {
 			KBFile imagefile = getPerson().getPhoto();
			if (imagefile!=null & imagefile.getWidth()>0 && imagefile.getHeight()>0) {
					double delta_x     = 0;
					double delta_y     = 0;
					double porcent_x   = 0;
					double porcent_y   = 0;
					int normalized_w, normalized_h;
					if (imagefile.getHeight()>WIDTH) {
						normalized_h = WIDTH;
						normalized_w = WIDTH*imagefile.getWidth()/imagefile.getHeight();
					}
						else {
							normalized_h = imagefile.getHeight();
							normalized_w = imagefile.getWidth();
						}
					
						if (normalized_w>WIDTH) {
							delta_x = -1 * (normalized_w-WIDTH)/2;
							porcent_x = 100 *  delta_x / ((double) normalized_w); 
							
						} else {
							delta_x = (-normalized_w+WIDTH)/2;
							porcent_x = 100 *  delta_x / ((double) normalized_w); 
						}
						
						if (normalized_h>WIDTH) {
							delta_y = -1 * (normalized_h-WIDTH)/2;
							porcent_y = 100 *  delta_y / ((double) normalized_h); 
							
						} else {
							delta_y = (-1 * normalized_h+WIDTH)/2;
							porcent_y = 100 *  delta_y / ((double) normalized_h); 
						}
	
		 				style = "max-height:"+String.valueOf(WIDTH)+"px; float:left;" + (porcent_x!=0? "margin-left:"+String.valueOf(porcent_x)+"%; ":"") +
								(porcent_y!=0? "margin-top:"+String.valueOf(porcent_y)+"%;":"");
	
						ph.add(new AttributeModifier("style", style));	   		
				}
		}
		*/

		
		
	}
	
	
	
	public User getSessionUser() {
		
		if (sessionUser!=null)
			return sessionUser.getObject();
		
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		
		if (profile!=null) {
			sessionUser = new ObjectModel<User>(profile.getUser());
			return sessionUser.getObject();
		}
		else
			return null;
	}

	
	public boolean isNewTab() {
		return newTab;
	}

	public void setNewTab(boolean newTab) {
		this.newTab = newTab;
	}


	/**
	public ResourceReference getPhoto() {
		
		
		Person person = getPerson();
		ResourceReference photoreference;
		
		if (person!=null && person.getPhoto()!=null) {
			photoreference = new WebThumbnailReference(person.getPhoto(), ThumbnailSize.MINI);
     	}
		else {
			photoreference = EMPTY_PHOTO;
		}
		return photoreference;
	}
	**/
	
	
	protected Image getPhoto() {
		try {
			Person person = getPerson();
			
			if (person!=null && person.getPhoto()!=null) 
				return  new ResourceThumbnailImage<>("user-photo", new ObjectModel<Resource>((Resource) person.getPhoto()) , ThumbnailSize.MINI);
							
			return new Image("user-photo", ServiceLocator.getService(BrandingWebService.class).getUserAvatarResourceReference(person));
			
			} catch (Exception e) {
			logger.error(e);
			return new GenericPhoto("user-photo");
		}
	}

	
	
	
	
	
	
	
	
	

	public Person getPerson() {
		
		if (sessionPerson!=null)
			return sessionPerson.getObject();
		
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		
		if (profile!=null && profile.getEntity()!=null && profile.getEntity() instanceof Person) {
			sessionPerson = new ObjectModel<Person>((Person)profile.getEntity());
			return sessionPerson.getObject();
		}
		else {
			return null;
		}
	}
	
	
	/*
	 * public void fireScanAll(Event event) {
	 * 
	 * // logger.debug("Fire Scan All " + event.getClass().getSimpleName());
	 * 
	 * for (WicketEventListener<Event> listener :
	 * getPage().getBehaviors(WicketEventListener.class)) { if
	 * (listener.handle(event)) { listener.onEvent(event); } }
	 * 
	 * fire(event, getPage().iterator(), false); }
	 */
	
//	@SuppressWarnings("unchecked")
//	protected boolean fire(Event event, Iterator<Component> components, boolean stop_first_hit) {
//		boolean handled = false;
//		while (components.hasNext()) {
//			Component component = components.next();
//			for (WicketEventListener<Event> listener : component.getBehaviors(WicketEventListener.class)) {
//				if (listener.handle(event)) {
//					listener.onEvent(event);
//					if (stop_first_hit) {
//						handled = true;
//						break;
//					}
//				}
//			}
//			if (!handled) {
//				if (component instanceof MarkupContainer) {
//					handled = fire (event, ((MarkupContainer)component).iterator(), stop_first_hit);
//				}
//			}
//			else {
//				break;
//			}
//		}
//		return handled;
//	}
//
//	/**
//	 * Scans Page and all its components
//	 * The first Component that listens to this event will handle it
//	 * 
//	 **/
//	@SuppressWarnings("unchecked")
//	public void fire(Event event) {
//		
//		// logger.debug("Fire " + event.getClass().getSimpleName());
//		
//		boolean handled=false;
//		for (WicketEventListener<Event> listener : getPage().getBehaviors(WicketEventListener.class)) {
//			if (listener.handle(event)) {
//				listener.onEvent(event);
//					handled = true;
//					break;
//				}
//			}
//		if (!handled) 
//			fire(event, getPage().iterator());
//	}
//	
//	protected boolean fire(Event event, Iterator<Component> components) {
//		return fire(event, components, true);
//	}


	protected boolean isWorkflowUser() {
		return is_admin || 
			UserProfileType.WORKFLOW_PARTICIPANT.equals(
				ServiceLocator.getService(UserService.class).getSessionUserProfile().getType());
	}

	protected WebPage getAccountPage() {
		return ServiceLocator.getService(ApplicationSiteMapService.class).getPage("user-myaccount-page");
		
	}

	protected String getServerUrl() {
		String protocol =((WebRequest)RequestCycle.get().getRequest()).getUrl().getProtocol();
		String host =((WebRequest)RequestCycle.get().getRequest()).getUrl().getHost();
		Integer iport =((WebRequest)RequestCycle.get().getRequest()).getUrl().getPort(); 
		String port = (iport.equals(80) || iport.equals(443) ? "":  ( ":" + iport.toString()) );
		return protocol +"://" + host + port;
	}
	
	
}
