package kbee.web.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;


import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.user.UserProfile;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.kbee.wicket.markup.html.behaviour.AjustableHeightBehavior;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;


@SuppressWarnings("serial")
public class GroupPanel extends Panel {
	private static final long serialVersionUID = 1L;

	private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(GroupPanel.class.getName());

	@SuppressWarnings("unused")
	private int windowHeight;
	
	private boolean b_adjustHeight;

	private IModel<Group> model;
	private List<IModel<Principal>> members;
	
	public class MembersProvider extends SortableDataProvider<Person, String> {
		public Iterator<Person> iterator(long first, long count) {
			ArrayList<Person> iteration = new ArrayList<Person>();
			Iterator<IModel<Principal>> iterator = getMembers().listIterator((int)first);
			int i = 0;
			while (i++<count) {
				Principal principal = iterator.next().getObject();
				if (principal instanceof User) {
					UserProfile profile = getContentDao().findUserProfileByUser((User)principal);
					if (profile!=null) {
						iteration.add(profile.getPerson());
					}
				}
				else {
					logger.error("Not person: "+ principal.getName() + "  " + principal.getClass().getName());
				}
			}
			return iteration.iterator();
		}	
		
		public IModel<Person> model(Person object) {
			return new ObjectModel<Person>(object);
		}
		public long size() {
			return getMembers().size();
		}
	}

	public GroupPanel(String id) {
		this(id, null, 0, true);
	}
	
	public GroupPanel(String id, IModel<Group> model) {
		this(id, model, 0, true);
	}
	
	public GroupPanel(String id, IModel<Group> model, int windowHeight, boolean b_adjustHeight) {
		super(id);
		
		setOutputMarkupId(true);
		
		setModel(model);

		this.windowHeight=windowHeight;
		this.b_adjustHeight=b_adjustHeight;
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();

		if (get("members-container")!=null)
				return;
		
		DataTable<Person, String> table = new DataTable<Person, String>("members", getColumns(), new MembersProvider(), 20);
		table.addTopToolbar(new AjaxFallbackHeadersToolbar<String>(table, (MembersProvider)table.getDataProvider()));

		WebMarkupContainer container = new WebMarkupContainer("members-container");
		WebMarkupContainer tablecontainer = new WebMarkupContainer("table-container");
		tablecontainer.add(table);
		if (b_adjustHeight)
			tablecontainer.add(new AjustableHeightBehavior(196));
		container.add(tablecontainer);
		container.add(new com.novamens.wicket.markup.html.repeater.util.NavigationToolbar("navigation", table, false));
		if (b_adjustHeight)
			container.add(new AjustableHeightBehavior(156));
		add(container);
	}

	
	public void onClose(AjaxRequestTarget target) {
		
	}
	

	
	public List<IColumn<Person, String>> getColumns() {
		
		List<IColumn<Person, String>> columns = new ArrayList<IColumn<Person, String>>();
		
		// Name -------------------------------------------------------------------------
		//
		columns.add(new PropertyColumn<Person, String>(getLabelModel("person"), "LastFirstName"));
		
		
		// EMail -------------------------------------------------------------------------
		//
		// columns.add(new PropertyColumn<Person, String>(getLabelModel("username"), "UserName"));

		return columns;
	}
	
	/** ------------------------------------------------------------------------------------------
	 */
	public IModel<Group> getModel() {
		return model;
	}
	
	
	public void setModel(IModel<Group> model) {
		this.model = model;
	}

	
	public void onDetach() {
		super.onDetach();
		if (model!=null)
			model.detach();
	}

	
	protected IModel<String> getLabelModel(String resourceKey) {
		return new StringResourceModel(resourceKey, GroupPanel.this, null);
	}

	
	protected String getLabel(String resourceKey) {
		return ((new StringResourceModel(resourceKey, GroupPanel.this, null)).getString());
	}
	
	
	public List<IModel<Principal>> getMembers() {
		if (this.members==null) {
			this.members = new ArrayList<IModel<Principal>>();
			for (Principal principal : ((KbeeGroup)getModel().getObject()).getMembers()) {
				this.members.add(new ObjectModel<Principal>(principal));
			}
			
			Collections.sort(this.members, new Comparator<IModel<Principal>>() {
				@Override
				public int compare(IModel<Principal> a, IModel<Principal> b) {
					try {	
						if (a instanceof User && b instanceof User) {
								return   ((User) a).getLastFirstName().compareToIgnoreCase(((User) b).getLastFirstName());
						}
						
						else if (a instanceof User && b instanceof Group) {
							return   ((User) a).getLastFirstName().compareToIgnoreCase(((Group) b).getName());
						}
						
						
						else if (a instanceof Group && b instanceof User) {
							return   ((Group) a).getName().compareToIgnoreCase(((User) b).getLastFirstName());
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


	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
