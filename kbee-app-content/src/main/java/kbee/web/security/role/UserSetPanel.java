package kbee.web.security.role;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserService;
import com.novamens.content.web.nav.markup.DataViewNavigatorPanel;
import com.novamens.content.web.nav.markup.NavigationLabel;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.util.DisplayNameExtractor;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.error.ApplicationErrorPage;
import kbee.web.service.ApplicationSiteMapService;

public class UserSetPanel extends ModelPanel<Role> {
			
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(UserSetPanel.class.getName());
	static final int PAGE_SIZE = 60;
	
	
	private List<UserRoleAggregateModel> members;

					
	final boolean role_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_security	= role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());


	
	
	private class UserRoleAggregate {
		public UserRole  urm;
		public List<String> entity_members;
		public UserRoleAggregate(UserRole urm, List<String> entity_members) {
			this.urm=urm;
			this.entity_members=entity_members;
		}
	}
	

	
	
	private class UserRoleAggregateModel implements IModel<UserRoleAggregate> {
		private static final long serialVersionUID = 1L;
		public IModel<UserRole> model;
		public List<String> entity_members;
		public UserRoleAggregateModel(UserRoleAggregate ur) {
			this.model=new ObjectModel<UserRole>(ur.urm);
			entity_members=ur.entity_members;
		}
		
		@Override
		public void detach() {
			model.detach();
		}

		@Override
		public UserRoleAggregate getObject() {
			return new UserRoleAggregate(model.getObject(), entity_members);
		}

		@Override
		public void setObject(UserRoleAggregate a) {
			model=new ObjectModel<UserRole>(a.urm);
			entity_members=a.entity_members;
		}
		
	}
	
	

	
	public class MembersProvider extends ListDataProvider<UserRoleAggregateModel> {
		private static final long serialVersionUID = 1L;
		public MembersProvider() {
			super();
		}
		protected List<UserRoleAggregateModel> getData() {
			return getMembers();
		}
	}

	
	
	/** ------------------
	 * 
	 * 
	 * @param id
	 * @param model
	 */
	public UserSetPanel(String id, IModel<Role> model) {
		super(id, model);
	}
	

	/**
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();

		setOutputMarkupId(true);
		
		add(new DataView<UserRoleAggregateModel>("members", new MembersProvider(), PAGE_SIZE) {
			private static final long serialVersionUID = 1L;
			protected void populateItem(final Item<UserRoleAggregateModel> item) {
			
				String name = null;
				StringBuilder str = new StringBuilder();
				
				try {
				if (item.getModelObject().getObject().urm instanceof UserRole) {
					Person person = getContentDao().findUserProfileByUser((User) item.getModelObject().getObject().urm.getUser()).getPerson();	
					name = person.getLastFirstName() +(  (person.getState()!=ObjectState.ENABLED)  ? ("[ "+ person.getState().getLabel(getSessionUser().getLocale())+" ]") : "");
				}
				else
					name = DisplayNameExtractor.get(item.getModelObject().getObject());
				
				if (name==null || "".equals(name))
					name = "-";

				if (item.getModelObject().getObject().entity_members!=null)
					item.getModelObject().getObject().entity_members.sort(new Comparator<String>() {
						@Override
						public int compare(String a, String b) {
							if (a==null && b==null)
								return 0;
							if (a==null && b!=null)
								return 1;
							if (a!=null && b==null)
								return -1;
							return a.compareToIgnoreCase(b);
						}
						
					});
				
				} catch (Exception e) {
					logger.error(e);
					name=e.getClass().getSimpleName();
				}
				
				
				final String sepa="<span class=\"ago\"> | </span>";
				for(String s: item.getModelObject().getObject().entity_members) {
					if (str.length()>0)
						str.append(sepa);
					else
						str.append(" (");

					str.append("<span class=\"iql-value\"> "+s+ "</span>");
				}

				if (str.length()>0)
					str.append(" )");
				
				WebMarkupContainer me= new 	WebMarkupContainer("member");
				
				Link<Void> lnk = new Link<Void>("user-link") {
					private static final long serialVersionUID = 1L;
					@Override
					public void onClick() {
						 UserSetPanel.this.onUserClick(item.getModelObject());
					}
					@Override
					public boolean isEnabled() {
						return role_security;
					}
				};
				
				me.add(lnk);
				lnk.add(new AttributeModifier("target", "_blank"));
				lnk.add(new Label("name", name));
				me.add((new Label("entity", str.toString())).setEscapeModelStrings(false));
				
				item.add(me);
			}
		});
		
		WebMarkupContainer navigationbar = new WebMarkupContainer("navigation-bar") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return !getMembers().isEmpty();
			}
		};
		
		navigationbar.add(new DataViewNavigatorPanel<Group>("navigator", (DataView<?>) get("members")));
		navigationbar.add(new NavigationLabel("navigation-label", (DataView<?>) get("members")));
		
		add(navigationbar);
	}
	

	
	private boolean isSame(Serializable previous, Serializable current) {
		if (previous==null && current!=null)
			return false;
		if (previous!=null && current==null)
			return false;
		return previous.equals(current);
	}
	
	/**
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
	public List<UserRoleAggregateModel> getMembers() {

		if (this.members==null) {

			long start = System.currentTimeMillis();
			this.members = new ArrayList<UserRoleAggregateModel>();
			
			// IMPORTANT. This List must come sorted by User for this method to work
			List<UserRole> uroles = getContentSecurityDao().findUserRolesByRole(getModel().getObject(), "name");

			if (uroles.isEmpty()) 
				return this.members;

			
			List<String> ls = new ArrayList<String>();

			
			if (!getModel().getObject().isEntity()) {
				for (UserRole uro : uroles) {
					this.members.add(new UserRoleAggregateModel(new UserRoleAggregate(uro, ls)));
				}
				return this.members;
			}
			
			
			UserRole previous = uroles.get(0);
			UserRole current = null;
			
			for (UserRole uro : uroles) {
				current = uro;
				if (isSame(previous.getUser().getId(), current.getUser().getId())) {
					ls.add(uro.getEntity().getDisplayName());
					previous=current;
				}
				else {
					this.members.add(new UserRoleAggregateModel(new UserRoleAggregate(previous, ls)));
					ls = new ArrayList<String>();
					ls.add(uro.getEntity().getDisplayName());
					previous=current;
				}
			}
			
			this.members.add(new UserRoleAggregateModel(new UserRoleAggregate(current, ls)));
			
			logger.debug("getMemeber() time: " + String.valueOf(System.currentTimeMillis()-start)+" ms");
			
		}
		return this.members;
	}


	/**
	 * 
	 * 
	 */
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	

	/**
	 * 
	 * 
	 */
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	

	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void onUserClick(UserRoleAggregateModel model) {
		
		if (model.getObject().urm instanceof UserRole) {
			UserProfile profile=getContentDao().findUserProfileByUser((User) model.getObject().urm.getUser());
			if (profile!=null) {
				Person person = profile.getPerson();
				if (person!=null) {
					if (role_security) {
					     PageParameters pa= new PageParameters();
					     pa.add("id", person.getProfile(UserProfile.class).getUser().getId().toString());
					     setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("security-user-page", pa));
						 return;
					}
					else {
						setResponsePage(new ApplicationErrorPage(new Model<String>("Not Authorized")));
					}
				}
				else {
					logger.error("Person is null");
					setResponsePage(new ApplicationErrorPage(new Model<String>("Person is null")));
					
				}
			}
			else {
				logger.error("Profile is null");
				setResponsePage(new ApplicationErrorPage(new Model<String>("Profile is null")));
			}
		}
		else {
		
			logger.error("Principal is not User");
			setResponsePage(new ApplicationErrorPage(new Model<String>("Principal is not User")));
		}
}
	
	
	private Boolean is_domain_kbee = null;
	
	protected boolean isDomainKbee() {
		if (this.is_domain_kbee == null) {
			try {
				this.is_domain_kbee = Boolean.valueOf(getPerson().getDomain().getName().toLowerCase().trim().equals("kbee"));
			} 
			catch (Exception e) {
				this.is_domain_kbee = Boolean.valueOf(false);
			}
		}
		return this.is_domain_kbee.booleanValue();
	}
	
	protected Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}
	
	protected boolean isFreeVersion() {
		return getDomain().getDomainType()==DomainType.EXPRESS;
	}
	
	protected Domain getDomain() {
		return (Domain)ServiceLocator.getService(UserService.class).getDomain();
	}

	protected boolean isAdminSessionUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	}
	
	protected boolean isSupportSessionUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}
	
	private ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
}
