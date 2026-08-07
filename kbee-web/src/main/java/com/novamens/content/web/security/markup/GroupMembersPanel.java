package com.novamens.content.web.security.markup;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.kbee.wicket.markup.html.LinkCellItem;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.repeater.util.NavigationToolbar;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.service.ApplicationSiteMapService;



@SuppressWarnings("serial")
public class GroupMembersPanel extends Panel {
	private static final long serialVersionUID = 1L;
	

	static final private org.apache.logging.log4j.Logger logger = LogManager.getLogger(GroupMembersPanel.class.getName());
	
	//static final DateConverter converter = new PatternDateConverter("dd MMM yyyy hh:mm:ss z",false);
		
	private IModel<Group> model;
	private List<Principal> members;

	public class MembersProvider extends SortableDataProvider<Principal, String> {
		public Iterator<Principal> iterator(long first, long count) {
			return getMembers().subList((int)first, (int)(first+count)).iterator();
		}	
		public IModel<Principal> model(Principal object) {
			return new ObjectModel<Principal>(object);
		}
		public long size() {
			return getMembers().size();
		}
	}

	/** ----------------------------------------------------------------------------------------------
	 */

	public GroupMembersPanel(String id, IModel<Group> model) {
		super(id);

		setModel(model);
		
		DataTable<Principal, String> table = new DataTable<Principal, String>("members", getColumns(), new MembersProvider(), 30);
		table.addTopToolbar(new AjaxFallbackHeadersToolbar<String>(table, (MembersProvider)table.getDataProvider()));
		add(table);
		add(new NavigationToolbar("navigation", table, String.valueOf(table.size())));
		
	}
	
	public void setModel(IModel<Group> model) {
		this.model = model;
	}
	
	public IModel<Group> getModel() {
		return this.model;
	}
	
	@Override
	public void onDetach() {
		this.model.detach();
		this.members = null;
		super.onDetach();
	}
	
	/** ----------------------------------------------------------------------------------------------
	 */
	private List<IColumn<Principal, String>> getColumns() {
		List<IColumn<Principal, String>> columns = new ArrayList<IColumn<Principal, String>>();
		columns.add(new AbstractColumn<Principal, String>(new StringResourceModel("name",  GroupMembersPanel.this, null)) {
			@Override
			public void populateItem(Item<ICellPopulator<Principal>> cellItem, String componentId, IModel<Principal> rowModel) {
				Principal principal = rowModel.getObject();
				String name = principal instanceof KbeeUser ? ((KbeeUser)principal).getLastFirstName() : principal.getName() + new StringResourceModel("group", GroupMembersPanel.this, null).getString();

				LinkCellItem<Principal> cell = new LinkCellItem<Principal>(componentId, rowModel, new Model<String>(name)) {
					@Override
					public void onClick() {
						if (getModelObject() instanceof User) {
							UserProfile profile = getContentDao().findUserProfileByUser(((User) getModelObject()));
							// Page page = new UserPage(new ObjectModel<Person>(profile.getPerson()), new NavigationBar<Person>("navigation"), false);
							if (profile!=null) {
								
								final boolean role_admin 	= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
								final boolean role_security	= role_admin || ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
								if (role_security) {
									// TODO VER AT
									//setResponsePage(new UserStandAlonePage(new ObjectModel<Person>(person)));
									setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage("security-user-standalone-page", new ObjectModel<Person>(profile.getPerson())));
									//Page page = new UserStandAlonePage(new ObjectModel<Person>(profile.getPerson()));
									//setResponsePage(page);
								}
								else
									logger.error("Not Authorized");
							} 
							else
								logger.error("Profile is null");
						}
						else
							logger.error("Principal is not a User");
					}
					@Override
					public String target() {
						return "_blank";
					}
				};
				cellItem.add(cell);
			}
		});
		return columns;
	}
	
	private List<Principal> getMembers() {
		if (this.members==null) {
			this.members = new ArrayList<Principal>();
			this.members.addAll(((KbeeGroup)getModel().getObject()).getMembers());
			Collections.sort(this.members, new Comparator<Principal>() {
				@Override
				public int compare(Principal a, Principal b) {
					try {	
					return a.getName().compareToIgnoreCase(b.getName());
					} catch (Exception e) {
						return 0;
					}
				}
			});
		}
		return this.members;
	}

	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}	
}
