package kbee.web.dataset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntityMember;
import com.novamens.content.model.EntitySet;
import com.novamens.content.model.UserSet;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.Role;
import com.novamens.content.security.RolesService;
import com.novamens.content.service.DataSetService;
import com.novamens.content.service.PersonService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserService;
import com.novamens.content.web.suggestion.service.UserSuggestionService;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.content.dao.Proxy;
import com.novamens.kbee.content.user.KbeeUserRole;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.markup.html.repeater.util.DataViewNavigationToolbar;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.form.AutoCompleteFieldV5;
import kbee.web.security.user.UserPage;

@SuppressWarnings("serial")
public class MemberRolesPanel extends ModelPanel<DataSetMember> {
	private static final long serialVersionUID = 1L;

	final boolean is_root			= ServiceLocator
			.getService(SecurityService.class)
			.isRoot(); 
	final boolean is_domain_admin 	= ServiceLocator
			.getService(SecurityService.class)
			.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean is_security 		= ServiceLocator
			.getService(SecurityService.class)
			.isMember(KbeeGlobalRole.SECURITY.getId());
	final boolean is_federated_security = ServiceLocator
			.getService(SecurityService.class)
			.isMember(KbeeGlobalRole.FEDERATED_SECURITY.getId());
 	final boolean is_support 		= ServiceLocator
 			.getService(SecurityService.class)
 			.isMember(KbeeGlobalRole.SUPPORT.getId());
 	
 	private class EntityRole implements Serializable	{
 		public IModel<Role> roleModel;
 		public IModel<EntityMember> entityModel;
 		
 		public EntityRole(DataSetMember entity, Role role) {
 			roleModel = new ObjectModel<Role>(role);
 			entityModel = new ObjectModel<EntityMember>((EntityMember)entity);
 		}
 	}

	private List<EntityRole> entityRoles;
	
	public class RolePanel extends Fragment  {
		private EntityRole entityRole;
		List<UserRole> users;
		private UserSelectorPanel selector;
		
		public class UserDataProvider extends SortableDataProvider<UserRole, String> {
			@Override
			public Iterator<UserRole> iterator(long first, long count) {
				List<UserRole> iteration = new ArrayList<>();
				List<UserRole> users = getUsers();
				long size = size();
				for (long i=first; i<first+count && i<size; i++) {
					iteration.add(users.get((int)i));
				}
				return iteration.iterator();
				
			}
			@Override
			public long size()  {
				return getUsers().size();
			}
			@Override
			public IModel<UserRole> model(UserRole object) {
				return new ObjectModel<UserRole>(object);
			}
			@Override
			public void detach() {
			}		
		}
		
		public RolePanel(EntityRole entityRole) {
			super("role", "role-fragment", MemberRolesPanel.this);
			
			setOutputMarkupId(true);
			
			this.entityRole = entityRole;
			
			WebMarkupContainer members = new WebMarkupContainer("members");
			members.setOutputMarkupId(true);
			members.setVisible(false);
			
			add(new Label("name", getDisplayName()));
			
			add(new AjaxLink<Void>("expander") {
				public void onClick(AjaxRequestTarget target) {
					members.setVisible(!members.isVisible());
					target.add(RolePanel.this);
				}
			});
			get("expander").add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					return members.isVisible() ? "far fa-angle-down" : "far fa-angle-up";
				}
			}));
			
			//members.add(new Label("name", getDisplayName()));
			
			DataTable<UserRole, String> table = new DataTable<UserRole, String>("user", 
					getColumns(), 
					new UserDataProvider(), 20) {
				public boolean isVisible() {
					return !getUsers().isEmpty();
				}
			};
			
			table.addTopToolbar(new AjaxFallbackHeadersToolbar<String>(
					table, 
					(UserDataProvider)table.getDataProvider()));

			members.add(new DataViewNavigationToolbar("navigator", table) {
				public void onUpdate(AjaxRequestTarget target) {
					target.add(RolePanel.this);
				}
				@Override
				public boolean isVisible() {
					return true;
				}
			});
			
			members.add(table);
			
			members.add(new AjaxLink<Void>("add-user-button") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					selector.open(target);
					target.add(RolePanel.this);
				}
				@Override
				public boolean isVisible() {
					return modificationsEnabled();
				}
			});
			
			selector = new UserSelectorPanel(entityRole) {
				public void onUpdate(AjaxRequestTarget target) {
					setVisible(false);
					target.add(RolePanel.this);
				}
			};
			
			selector.setVisible(false);
			
			members.add(selector);
			
			add(members);
		}	
		
		public Role getRole() {
			return entityRole.roleModel.getObject();
		}
		
		public EntityMember getEntity( ) {
			return entityRole.entityModel.getObject();
		}
		
		public List<UserRole> getUsers() {
			if (this.users == null) {
				users = new ArrayList<>();
				for (UserRole userRole : getSecurityDao().findUserRolesByEntityMember(getEntity())) {
					if (userRole.getRole().equals(getRole())) {
						users.add(userRole);
					}
				};
				users.sort(new Comparator<UserRole>() {
					@Override
					public int compare(UserRole a, UserRole b) {
						try {
							return a.getPerson().getLastFirstName().toLowerCase()
								.compareTo(b.getPerson().getLastFirstName().toLowerCase());
						} 
						catch (Exception e) {
							return 0;	
						}
					}
				});
			}
			return users;
		}
		
		@Override
		public void onDetach() {
			super.onDetach();
			users = null;
			entityRole.roleModel.detach();
			entityRole.entityModel.detach();
		}
		
		protected String getDisplayName() {
			String label = "";
			if (!getEntity().getDataSet().equals(
				MemberRolesPanel.this.getModelObject().getDataSet())) {
				label = getEntity().getDisplayName();
			}
			else {
				label = getRole().getName();
			}
			return label;
		}
		
		protected List<IColumn<UserRole, String>> getColumns() {
			
			List<IColumn<UserRole, String>> columns = new ArrayList<>();
			
			if (modificationsEnabled())
			columns.add(new AbstractColumn<UserRole, String>(new Model<String>("")) {
				public void populateItem(
						Item<ICellPopulator<UserRole>> cellItem, 
						String componentId,
						IModel<UserRole> model){
					cellItem.add(new UserMenu(componentId, model) {
						protected void onUpdate(AjaxRequestTarget target) {
							target.add(RolePanel.this);
						}
					});
				}
				@Override
				public String getCssClass() {
					return "col-xs-1 col-xs-menu";
				}
			});
			
			columns.add(new AbstractColumn<UserRole, String>(getLabel("property.user")) {
				public void populateItem(
						Item<ICellPopulator<UserRole>> cellItem, 
						String componentId,
						IModel<UserRole> model){
					cellItem.add(new UserLink(componentId, model));
				}
				@Override
				public String getCssClass() {
					return "col-xs-2";
				}
			});
			
			columns.add(new PropertyColumn<UserRole, String>(getLabel("property.email"), "person.email") {
				@Override
				public String getCssClass() {
					return "col-xs-2";
				}
			});
			
//			columns.add(new PropertyColumn<UserRole, String>(getLabel("property.phone"), "person.phone") {
//				@Override
//				public String getCssClass() {
//					return "col-xs-1";
//				}
//			});
			
			for (Classifier classifier : getUserSet().getClassifiers()) {
				IModel<Classifier> classifiermodel = new ObjectModel<>(classifier);
				columns.add(new AbstractColumn<UserRole, String>(new Model<String>(classifier.getDisplayName())) {
					public void populateItem(
							Item<ICellPopulator<UserRole>> cellItem, 
							String componentId,
							IModel<UserRole> model){
						cellItem.add(new Label(componentId, () -> getClassification(model.getObject().getPerson(), classifiermodel.getObject())));
					}
					@Override
					public String getCssClass() {
						return "col-xs-2";
					}
				});
			}

			return columns;
		}
		
		private String getClassification(Person person, Classifier classifier) {
			String displayName = ""; 
			for (Classification classification :person.getService(PersonService.class).getUserMember().getClassification(classifier)) {
				displayName += classification.getDataSetMember().getDisplayName();
			};
			return displayName;
		}
	}
	
	public class UserLink extends Fragment  {
		
		public UserLink(String id, IModel<UserRole> model) {
			super(id, "user-link-fragment", MemberRolesPanel.this);
			
			Link<User> userLink = new Link<User>("user-link") {
				@Override
				public void onClick() {
					setResponsePage(new UserPage(new ObjectModel<Person>(model.getObject().getPerson())));
				}
				public boolean isEnabled() {
					return is_security || is_domain_admin || is_root || is_support; 
				}
			};
			
			userLink.add(new Label("user-name", () -> model.getObject().getPerson().getLastFirstName()));
			
			add(userLink);
		}
	}	

	public class UserMenu extends Fragment  {
		
		public UserMenu(String id, IModel<UserRole> model) {
			super(id, "user-menu-fragment", MemberRolesPanel.this);
			
			WebMarkupContainer menu = new WebMarkupContainer("menu-container") {
				@Override
				public boolean isVisible() {
					return modificationsEnabled();
				}
			};
			add(menu);
			menu.add(getMenu(model));
		}
		
		protected void onUpdate(AjaxRequestTarget target) {
			
		}
		
		private Panel getMenu(IModel<UserRole> model) {
			try {
				
				if (!modificationsEnabled()) {
					return new InvisiblePanel("menu");
				}
				
				ContextMenuPanel<UserRole> menu = new ContextMenuPanel<UserRole>(model);
				menu.setOutputMarkupId(true);
				
				menu.addItem(id ->
					new  AjaxMenuItemPanelV5<UserRole>(id) {
						@Override
						public void onClick(AjaxRequestTarget target) {
							getConfirmationDialog().open(target, 
									MemberRolesPanel.this.getLabel("remove-user.confirmation.message", getModelObject().getPerson().getDisplayName()), 
									Dialog.Delete, 
									new Dialog.Handler() {
								@Override
								public void onClick(AjaxRequestTarget target, Button button) {
									if (button.key().equals(Dialog.Delete.key())) {
										try { 
											removeRole(getModelObject());
											UserMenu.this.onUpdate(target);
										} 
										catch (Exception e) {
										}
									}
								}
							});
						}
						@Override
						public String getLabel() {
							return getLabelString("menu.delete");
						}
					}
				);
				
				return menu;
			} 
			catch (Exception e) {
				return new InvisiblePanel("menu");
			}
		}
		
		
		private void removeRole(UserRole userRole) {
			userRole.getPerson().getService(RolesService.class).remove(userRole);
		}
	}
	
	public class UserSelectorPanel extends Fragment  {
		private EntityRole entityRole;
		private IModel<User> usermodel;
		private String feedbackMessage;
		
		public UserSelectorPanel(EntityRole entityRole) {
			super("user-selector", "user-selector-fragment", MemberRolesPanel.this);
			
			setOutputMarkupId(true);
			
			this.entityRole = entityRole;
			 
			Form<User> form = new Form<User>("form", Disposition.VERTICAL);
			
			form.add(new AutoCompleteFieldV5<User>("user", new PropertyModel<User>(this, "user"), false) {
				@Override
				public int getMaxHistory() {
					return 3;
				}
				@Override
				public List<Suggestion> getSuggestions(String pattern) {
					return ServiceLocator.getService(UserSuggestionService.class).getSuggestions(pattern);
				}
				@Override
				public String getHistoryKey() {
					return "entity-user-role";
				}
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					super.onUpdate(target);
					setUser(getValue());
				}
				@Override
				protected boolean isInputEnabled() {
					return true;
				}
			});	
			
			form.add(new AjaxLink<Void>("cancel-button") {
				public void onClick(AjaxRequestTarget target) {
					UserSelectorPanel.this.onUpdate(target);
				}
			});
			
			form.add(new AjaxLink<Void>("confirm-button") {
				public void onClick(AjaxRequestTarget target) {
					if (hasRole(getUser())) {
						setFeedback("ya existe el usuario");
						target.add(UserSelectorPanel.this);
					}
					else {
						KbeeUserRole userRole = new KbeeUserRole();
						userRole.setRole(entityRole.roleModel.getObject());
						userRole.setEntity(entityRole.entityModel.getObject());
						UserProfile userProfile = getContentDao().findUserProfileByUser(getUser());
						userRole.setUserProfile(userProfile);
						userRole.setUser(getUser());
						Person person = userProfile.getPerson();
						person.getService(RolesService.class).add(userRole);
						UserSelectorPanel.this.onUpdate(target);
					}
				}
			});
			
			add(form);
			
			WebMarkupContainer feedbackpanel = new WebMarkupContainer("feedback") {
				public boolean isVisible() {
					return getFeedback()!=null;
				}
			};
			feedbackpanel.setOutputMarkupId(true);
			feedbackpanel.add(new Label("message", () -> getFeedback()));
			add(feedbackpanel);
		}
		
		public void open(AjaxRequestTarget target) {
			setVisible(true);
			setFeedback(null);
			usermodel = null;
			((Field<?>)get("form:user")).beforeRender();
			((Field<?>)get("form:user")).setValue(null);
			((AutoCompleteFieldV5<?>)get("form:user")).setStringValue(null);
			((Field<?>)get("form:user")).clearInput();
			Component user = ((Field<?>)get("form:user")).getInput();
			target.focusComponent(user);
		}
		
		public void onUpdate(AjaxRequestTarget target) {
			
		}
		
		public User getUser() {
			return usermodel!=null ? usermodel.getObject() : null;
		}
		
		public void setUser(User user) {
			usermodel = new ObjectModel<User>(user);
		}
		
		private boolean hasRole(User user) {
			for (UserRole userRole : 
				getSecurityDao().findUserRolesByEntityMember(entityRole.entityModel.getObject())) {
				if (userRole.getRole().equals(
					entityRole.roleModel.getObject()) && userRole.getUser().equals(user)) {
					return true;
				}
			};
			return false;
		}
		
		private void setFeedback(String message) {
			feedbackMessage = message;
		}
		
		private String getFeedback() {
			return feedbackMessage;
		}
	}	
	
	public MemberRolesPanel(String id, IModel<DataSetMember> model) {
		super(id, model);
		
		Assert.isInstanceOf(EntityMember.class, model.getObject());
		
		add(new ListView<EntityRole>("roles", new PropertyModel<List<EntityRole>>(this, "entityRoles")) {
			public void populateItem(final ListItem<EntityRole> item) {
				item.add(new RolePanel(item.getModelObject()));
			}
		});
		
		add(new WebMarkupContainer("emptypanel") {
			public boolean isVisible() {
				return getEntityRoles().isEmpty();
			}
		});
		
		add(new ConfirmationDialog("confirmation-dialog"));
	}
	
	public List<EntityRole> getEntityRoles() {
		if (entityRoles==null) {
			entityRoles = new ArrayList<>();
			EntityMember entity = (EntityMember)getModelObject();
			EntitySet entitySet = (EntitySet)Proxy.Unproxy(entity.getDataSet());
			for (Role role : entitySet.getService(DataSetService.class).getRoles()) {
				entityRoles.add(new EntityRole(entity, role));
			}
			for (DataSet aggregation : 
				entitySet.getService(DataSetService.class).getAggregations()) {
				for (DataSetMember aggregated : 
					aggregation.getService(DataSetService.class).getAggregatedValues(entity)) {
					for (Role role : aggregation.getService(DataSetService.class).getRoles()) {
						entityRoles.add(new EntityRole((EntityMember)Proxy.Unproxy(aggregated), role));
					}
				}
			}
		}
		return entityRoles;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		entityRoles = null;
	}
	
	private boolean modificationsEnabled() {
		return is_security || 
			is_domain_admin || 
			is_root || 
			(is_federated_security && isEntityAdmin()); 
	}
	
	private boolean isEntityAdmin() {
		return ServiceLocator.getService(UserService.class).isAdmin(getModelObject().getDataSet());
	}
	
	private UserSet getUserSet() {
		return getContentDao().getUserSet();
	}
	
	private ConfirmationDialog getConfirmationDialog() {
		return (ConfirmationDialog) get("confirmation-dialog");
	}
	
	private ContentSecurityDao getSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
}
