package kbee.web.security;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.user.UserService;

import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Acl;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.security.acl.Permission;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.form.AutoCompleteFieldV5;
import kbee.web.security.AclRow.PermissionValue;
import kbee.web.security.service.PrincipalSuggestionService;

@SuppressWarnings("serial")
public class AclEditorTab extends ObjectEditorPanel<Acl>  {
				
	private static final long serialVersionUID = 1L;
	
	static private Logger logger = LogManager.getLogger(AclEditorTab.class);
	
	private boolean readOnly;
	private IModel<List<AclRow>> rowsModel;
	private Principal principal;
	
	public class AclRowsProvider extends SortableDataProvider<AclRow, String> {
		public Iterator<AclRow> iterator(long first, long count) {
	        List<AclRow> sorted = new ArrayList<>(getRows());

	        sorted.sort(Comparator.comparing(a -> a.getPrincipal().getName()));

	        int toIndex = (int)Math.min(first + count, sorted.size());

	        return sorted.subList((int)first, toIndex).iterator();
		}	
		public IModel<AclRow> model(AclRow object) {
			return new Model<AclRow>(object);
		}
		public long size() {
			return getRows().size();
		}
	}
	
	private class PrincipalFragment extends Fragment {
		public PrincipalFragment(String id, final AclRow row) {
			super(id, "principalFragment", AclEditorTab.this);
			setOutputMarkupId(true);
			AjaxLink<?> principalLink = new AjaxLink<Void>("principal-link") {
				public void onClick(AjaxRequestTarget target) {
					AclEditorTab.this.getParent().detach();
					AclEditorTab.this.getParent().getParent().detach();
					AclEditorTab.this.getParent().getParent().getParent().detach();
					AclEditorTab.this.getParent().getParent().getParent().getParent().detach();
					AclEditorTab.this.getPage().detach();
					AclEditorTab.this.detach();
					((GroupModal)PrincipalFragment.this.get("group-window")).open(target, new ObjectModel<Group>((Group)row.getPrincipal()));
				}
				public boolean isEnabled() {
					//boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
					//boolean role_security = role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());
					//return row.getPrincipal() instanceof Group && role_security;
					return row.getPrincipal() instanceof Group;
				}
			};
			principalLink.add((new Label("principal", new Model<String>() {
				public String getObject() {
					
					StringResourceModel gm = new StringResourceModel("group", AclEditorTab.this, null); 
					StringResourceModel um = new StringResourceModel("user", AclEditorTab.this, null);
					
					return row.getPrincipal() instanceof Group ? 
						AclEditorTab.this.getPath((Group)row.getPrincipal()) + " " + gm.getObject() :
						((User)row.getPrincipal()).getFirstLastName() + " " + um.getObject();
				}
			})).setEscapeModelStrings(false));
			add(principalLink);
			add(new GroupModal("group-window"));
		}
	}
	
	private class PermissionFragment extends Fragment {
		public PermissionFragment(String id, final AclRow row, final Permission permission) {
			super(id, "permissionFragment", AclEditorTab.this);
			setOutputMarkupId(true);
			AjaxLink<?> permissionLink = new AjaxLink<Void>("permission-link") {
				public void onClick(AjaxRequestTarget target) {
					if (row.getValue(permission)==null)
						row.setValue(permission, PermissionValue.GRANT);
					else
						if (row.getValue(permission)==PermissionValue.GRANT)
							row.setValue(permission, PermissionValue.DENIED);
						else
							row.remove(permission);
					AclEditorTab.this.onUpdate(target);//updated = true;
					target.add(PermissionFragment.this);
				}
				@Override
				public boolean isEnabled() {
					return AclEditorTab.this.getEditor().isEditionEnabled();
				}
			};
			
			Label label= new Label("value", new Model<String>() {
				public String getObject() {
					return row.getValue(permission)!=null ? row.getValue(permission).toString() : "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
				}
			});
					
			label.setEscapeModelStrings(false);
			if (row.getValue(permission)!=null)
				label.add(new AttributeModifier("class", row.getValue(permission).toString()));
			else
				label.add(new AttributeModifier("class", "not-set"));

			permissionLink.add(label);
			add(permissionLink);
		}
	}
	
	private class DeleteLinkFragment extends Fragment {
		public DeleteLinkFragment(String id, final AclRow row) {
			super(id, "deletelinkFragment", AclEditorTab.this);
			setOutputMarkupId(true);
			AjaxLink<?> deleteLink = new AjaxLink<Void>("delete-link") {
				public void onClick(AjaxRequestTarget target) {
					deleteRow(row);
					AclEditorTab.this.onUpdate(target);//updated = true;
					target.add(AclEditorTab.this);
				}
				@Override
				public boolean isVisible() {
					return AclEditorTab.this.getEditor().isEditionEnabled();
				}
			};
			add(deleteLink);
		}
	}
	
	/**  
	 * 
	 * @param id
	 * @param editor
	 * @param rows
	 * @param permissions
	 * @param readOnly
	 */
	public AclEditorTab(String id, 
			Editor<Acl> editor, 
			IModel<List<AclRow>> rowsModel, 
			List<Permission> permissions, 
			boolean readOnly) {
		super(id);
		
		setReadOnly(readOnly);
		setEditor(editor);
		setOutputMarkupId(true);
		this.rowsModel = rowsModel;
		DataTable<AclRow, String> table = new AjaxFallbackDefaultDataTable<AclRow, String>("rows", 
				getColumns(permissions), 
				new AclRowsProvider(), 
				20);
		add(table);
	
		AutoCompleteFieldV5<Principal> principalfield = new AutoCompleteFieldV5<Principal>("group", new PropertyModel<Principal>(this, "principal")) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				if (getValue()!=null)
				addPrincipal(getValue());
				setSuggestion(null);
				setStringValue(null); 
				AclEditorTab.this.onUpdate(target);
				target.add(AclEditorTab.this);
				setSuggestion(null);
			};
			@Override
			public boolean isVisible() {
				return AclEditorTab.this.getEditor().isEditionEnabled() && 
					!AclEditorTab.this.get("selector").isEnabled();
			}
			@Override
			public List<Suggestion> getSuggestions(String pattern) {
				return ServiceLocator.getService(PrincipalSuggestionService.class).getSuggestions(pattern); 
			}
			@Override 
			public String getHistoryKey() {
				return "acl-principal"; 
			}
		};
		add(principalfield); 
		
		add(new WebMarkupContainer("selector-button") {
			public void onInitialize() {
				super.onInitialize();
				add(new AjaxLink<>("button") {
					public void onClick(AjaxRequestTarget target) {
						AclEditorTab.this.get("selector").setVisible(true);
						target.add(AclEditorTab.this);
					}
				});
			}
			public boolean isVisible() {
				return AclEditorTab.this.get("selector").isEnabled() && 
					AclEditorTab.this.getEditor().isEditionEnabled();
			}
		});
		
		add(new PrincipalSelector("selector") {
			protected void onSelect(AjaxRequestTarget target, Principal principal) {
				addPrincipal(principal);
				AclEditorTab.this.onUpdate(target);
				target.add(AclEditorTab.this);
				super.onSelect(target, principal);
			}
			protected void onClose(AjaxRequestTarget target) {
				target.add(AclEditorTab.this);
				super.onClose(target);
			}
		});
	}

	public Principal getPrincipal() {
		return principal;
	}

	public void setPrincipal(Principal principal) {
		this.principal = principal;
	}

	public void setReadOnly(boolean value) {
		readOnly = value;
	}

	public boolean isReadOnly() {
		return readOnly;
	}
	
	public boolean isUsersEnabled() {
		return true;
	}
	
	public List<AclRow> getRows() {
		return rowsModel.getObject();
	}
	
	public List<IColumn<AclRow, String>> getColumns(List<Permission> permissions) {
		
		List<IColumn<AclRow, String>> columns = new ArrayList<IColumn<AclRow, String>>();
		
		IModel<String> principalLabel = isUsersEnabled()
				? getLabel("column.principal")
				: getLabel("column.group");		
		AbstractColumn<AclRow, String> principalColumn = new AbstractColumn<AclRow, String>(principalLabel) {
			public void populateItem(
					Item<ICellPopulator<AclRow>> cellItem, 
					String componentId,
					IModel<AclRow> model){
				cellItem.add(new PrincipalFragment(componentId, model.getObject()));
			}
			@Override
			public String getCssClass()	{
				return "principal col-lg-4";
			}
		};
		columns.add(principalColumn);
		
		Locale locale=getSessionUser().getLocale();
		
		for (final Permission permission : permissions)  {
			AbstractColumn<AclRow, String> permissionColumn = new AbstractColumn<AclRow, String>(new Model<String>(((KbeePermission)permission).getLabel(locale))) {
				public void populateItem(
						Item<ICellPopulator<AclRow>> cellItem, 
						String componentId,
						IModel<AclRow> model){
					cellItem.add(new PermissionFragment(componentId, model.getObject(), permission));
				}
				@Override
				public String getCssClass()	{
					return "permission col-lg-2";
				}
			};
			columns.add(permissionColumn);
		}
		
		if (!isReadOnly()) {
				AbstractColumn<AclRow, String> actionColumn = new AbstractColumn<AclRow, String>(new StringResourceModel("row", AclEditorTab.this, null)) {
					public void populateItem(
							Item<ICellPopulator<AclRow>> cellItem, 
							String componentId,
							IModel<AclRow> model){
						cellItem.add(new DeleteLinkFragment(componentId, model.getObject()));
					}
					@Override
					public String getCssClass()	{
						return "permission col-lg-1";
					}
				};
				columns.add(actionColumn);
		}
		return columns;
	}

	public void addPrincipal(Group group) {
		for (AclRow row : getRows()) {
			if (row.getPrincipal().getId().equals(group.getId()))
				return;
		}
		getRows().add(new AclRow(group));
	}
	
	public void addPrincipal(Principal principal) {
		for (AclRow row : getRows()) {
			if (row.getPrincipal().getId().equals(principal.getId()))
				return;
		}
		getRows().add(new AclRow(principal));
	}
	
	public void deleteRow(AclRow row1) {
		for (AclRow row2 : getRows()) {
			if (row2.getPrincipal().getId().equals(row1.getPrincipal().getId())) {
				getRows().remove(row2);
				break;
			}	
		}
	}

	public boolean usersEnabled() {
		return true;
	}

	public void onUpdate(AjaxRequestTarget target) {
		target.add(this);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		principal = null;
		for (AclRow row : getRows()) {
			row.detach();
		}
	}

	protected Panel getMenu() {
		ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					AclEditorTab.this.get("selector").setVisible(true);
					target.add(AclEditorTab.this);
				}	
				@Override
				public String getLabel() {
					return AclEditorTab.this.getLabel("menu.principal-selector").getObject();
				}
				@Override
				public boolean isEnabled() {
					return true;
				}
		});
		
		return menu;
	}	
	
	private User getSessionUser() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();	
	}

	private String getPath(Group group) {
		String path = group.getName();
		try {
			for (Group parent : group.getGroups()) {
				try {
					String parentpath = getPath(parent);
					path = parentpath + "/" + path;
				} catch (Exception e) {
					logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
					path = "err" + "/" + path;
				}
			}
		} catch (Exception e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return path;
	}
}