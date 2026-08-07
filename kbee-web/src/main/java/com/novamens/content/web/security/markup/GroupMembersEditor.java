package com.novamens.content.web.security.markup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.content.web.nav.markup.DataViewNavigatorPanel;
import com.novamens.content.web.nav.markup.NavigationLabel;
import com.novamens.content.web.suggestion.service.UserSuggestionService;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.security.KbeePrincipal;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.kbee.wicket.util.DisplayNameExtractor;
import com.novamens.logging.SecurityUpdateEvent;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.AutoCompleteFieldV5;
import kbee.web.form.EditButtonsV5;
import kbee.web.service.ApplicationSiteMapService;


@SuppressWarnings("serial")
public class GroupMembersEditor extends ObjectEditor<Group> {
	private static final long serialVersionUID = 1L;
	
	static final int PAGE_SIZE = 40;
	
	static private Logger trx_logger = LogManager.getLogger("TxLogger");	

	static Logger logger = LogManager.getLogger(GroupMembersEditor.class.getName());
	
	private List<IModel<Principal>> members;
	private List<IModel<Principal>> deleted;
	private Set<Serializable> adds = new HashSet<Serializable>(); 
	private User member;
	
	final boolean role_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_security	= role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());

	public class MembersProvider extends ListDataProvider<IModel<Principal>> {
		public MembersProvider() {
			super();
		}
		protected List<IModel<Principal>> getData() {
			return getMembers();
		}
	}

	
	 
	public GroupMembersEditor(String id, IModel<Group> model) {
		super(id, model);

		setOutputMarkupId(true);
		
		setEditionEnabled(false);
		
		final Form<?> form = new Form<Void>("gform", Disposition.VERTICAL);
		
		form.setOutputMarkupId(true);
		
		form.add(new DataView<IModel<Principal>>("members", new MembersProvider(), PAGE_SIZE) {
			protected void populateItem(final Item<IModel<Principal>> item) {
				
				String name = null;
				if (item.getModelObject().getObject() instanceof User) {
					Person person = getContentDao().findUserProfileByUser((User) item.getModelObject().getObject()).getPerson();	
					name = person.getLastFirstName();
				}
				else
					name = DisplayNameExtractor.get(item.getModelObject().getObject());
				
				if (name==null || "".equals(name))
					name = item.getModelObject().getObject().getName();
				
				Link<Void> lnk = new Link<Void>("user-link") {
					@Override
					public void onClick() {
							GroupMembersEditor.this.onUserClick(item.getModel());
					}
				};
				
				lnk.add(new AttributeModifier("target", "_blank"));
				
				
				
				lnk.add(new Label("member", name));
				
				item.add(new AjaxLink<Void>("delete-link") {
					@Override
					public void onClick(AjaxRequestTarget target) {
						removeMember((User)item.getModelObject().getObject());
						target.add(form);
					}
					@Override
					public boolean isVisible() {
						return role_security && isEditionEnabled();
					}
					
					
				});
				item.add(lnk);
			}
		});
		
		WebMarkupContainer navigationbar = new WebMarkupContainer("navigation-bar") {
			@Override
			public boolean isVisible() {
				return !getMembers().isEmpty();
			}
		};
		navigationbar.add(new DataViewNavigatorPanel<Group>("navigator", (DataView<?>)form.get("members")));
		navigationbar.add(new NavigationLabel("navigation-label", (DataView<?>)form.get("members")));
		form.add(navigationbar);

		WebMarkupContainer selector = new WebMarkupContainer("memberselector") {
			public boolean isVisible() {
				return isEditionEnabled();
			}
		};
		
		selector.add(new AutoCompleteFieldV5<User>("member", new PropertyModel<User>(this, "member")) {
			@Override
			public List<Suggestion> getSuggestions(String pattern) {
				return ServiceLocator.getService(UserSuggestionService.class).getSuggestions(pattern);
			}
			
			@Override
			public String getHistoryKey() {
				return GroupMembersEditor.this.getClass().getSimpleName()+"-member";
			}
			
			@Override 
			public void onUpdate(AjaxRequestTarget target) {
				super.onUpdate(target);
				addMember(getValue());
				setSuggestion(null);
				setStringValue(null);
				target.add(GroupMembersEditor.this);
			}
		});
		
		form.add(selector);
		
		add(form);
		
		add(new EditButtonsV5<Group>(this)  {

			@Override
			public boolean isVisible() {
				return isRoot()  && isEditionEnabled();
			}
			
			@Override
			public boolean isEnabled()  {
				return isRoot();
			}
		});
	
	}
	
	protected void onUserClick(IModel<IModel<Principal>> model) {
		
			if (model.getObject().getObject() instanceof User) {
				UserProfile profile=getContentDao().findUserProfileByUser((User) model.getObject().getObject());
				if (profile!=null) {
					Person person = profile.getPerson();
					if (person!=null) {
						final boolean role_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
						final boolean role_security	= role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
						if (role_security) {
							// TODO VER AT
							//setResponsePage(new UserStandAlonePage(new ObjectModel<Person>(person)));
							setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("security-user-standalone-page", new ObjectModel<Person>(person)));
						}
						else {
						
							// abrir ventana Not authorized
							//
							logger.info("Not Authorized");
						}
					}
					else
						logger.error("Person is null");
				}
				else
					logger.error("Profile is null");
			}
			else
				logger.error("Principal is not User");
	}

	public void setMember(User member) {
		this.member = member;
	}
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			for (IModel<Principal> model : members) {
				if (adds.contains(String.valueOf(((KbeePrincipal)model.getObject()).getId()))) {
					getModelObject().addMember(model.getObject());
					trx_logger.info(new SecurityUpdateEvent((com.novamens.security.Principal)model.getObject(), "Add " + getModelObject().getName() + " Group"));
				}
			}
			for (IModel<Principal> model : deleted) {
				getModelObject().removeMember(model.getObject());
				trx_logger.info(new SecurityUpdateEvent((com.novamens.security.Principal)model.getObject(), "Remove " + getModelObject().getName() + " Group"));
			}
			ServiceLocator.getService(SecurityContentMgmtService.class).update(getModelObject());
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
	
	@Override
	public void cancel(AjaxRequestTarget target) {
		this.members = null;
		getMembers();
		adds.clear();
		deleted.clear();
		super.cancel(target);
		target.add(this);
	}
	
	public User getMember() {
		return member;
	}
	
	public Form<?> getForm() {
		return (Form<?>)get("gform");
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		this.member = null;
		if (members!=null)
		for (IModel<Principal> model : members) {
			model.detach();
		}
		if (deleted!=null)
		for (IModel<Principal> model : deleted) {
			model.detach();
		}
	}
	
	protected void addMember(User member) {
		
 		for (IModel<Principal> model : members) {
			if (model.getObject().equals(member)) {
				return;
			}
		}
 		
		this.adds.add(String.valueOf(member.getId()));
		
		this.members.add(new ObjectModel<Principal>(member));
		
		for (IModel<Principal> model : deleted) {
			if (model.getObject().equals(member)) {
				deleted.remove(model);
				break;
			}
		}
	}
	
	protected void removeMember(User member) {
		if (adds.contains(String.valueOf(member.getId())))
			adds.remove(String.valueOf(member.getId()));
 		for (IModel<Principal> model : members) {
			if (model.getObject().equals(member)) {
				members.remove(model);
				deleted.add(model);
				break;
			}
		}
	}
	
	public List<IModel<Principal>> getMembers() {
		if (this.members==null) {
			this.members = new ArrayList<IModel<Principal>>();
			this.deleted = new ArrayList<IModel<Principal>>();
			for (Principal principal : ((KbeeGroup)getModel().getObject()).getMembers()) {
				this.members.add(new ObjectModel<Principal>(principal));
			}
			
			Collections.sort(this.members, new Comparator<IModel<Principal>>() {
				@Override
				public int compare(IModel<Principal> a, IModel<Principal> b) {
					try {
						if (a.getObject() instanceof User && b.getObject() instanceof User) {
							Person a_person = getContentDao().findUserProfileByUser((User) a.getObject()).getPerson();
							String a_name = a_person.getLastFirstName();
							if (a_name==null || a_name.equals("")) a_name = a.getObject().getName();
							Person b_person = getContentDao().findUserProfileByUser((User) b.getObject()).getPerson();
							String b_name = b_person.getLastFirstName(); 
							if (b_name==null || b_name.equals("")) b_name = b.getObject().getName();
							return a_name.compareToIgnoreCase(b_name);
						}
						else
							return a.getObject().getName().compareToIgnoreCase(b.getObject().getName());
					} 
					catch (Exception e) {
						return 0;
					}
				}
			});
		}
		return this.members;
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	 
	protected boolean isSupportUser() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}

	/**  
	 * Session User
	 */
	protected boolean isRoot() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(getSessionUser());
	}
	
	/**
	 * Session USer
	 * @return
	 */
	protected User getSessionUser() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();	
		
	}
}
