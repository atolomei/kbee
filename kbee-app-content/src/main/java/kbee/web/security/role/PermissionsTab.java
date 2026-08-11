package kbee.web.security.role;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.security.Role;
import com.novamens.content.user.UserService;
import com.novamens.content.web.editor.markup.ObjectEditorPanel;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.security.Identifiable;
import com.novamens.security.Principal;
import com.novamens.security.User;
import com.novamens.security.acl.Permission;
import com.novamens.service.ServiceLocator;

import kbee.web.security.AclRow;
import kbee.web.security.AclRow.PermissionValue;

@SuppressWarnings("serial")
public class PermissionsTab<T extends Identifiable> extends ObjectEditorPanel<Role>  {
	private static final long serialVersionUID = 1L;
	
	private AclRow row;
	private Principal principal;
	IModel<T> model;
	
	
	public class AclRowsProvider extends SortableDataProvider<AclRow, String> {
		public Iterator<AclRow> iterator(long first, long count) {
			ArrayList<AclRow> iteration = new ArrayList<AclRow>();
			iteration.add(row);
			return iteration.iterator();
		}	
		public IModel<AclRow> model(AclRow object) {
			return new Model<AclRow>(object);
		}
		public long size() {
			return 1;
		}
	}
	
	private class PermissionFragment extends Fragment {
		public PermissionFragment(String id, final AclRow row, final Permission permission) {
			super(id, "permissionFragment", PermissionsTab.this);
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
					PermissionsTab.this.onUpdate(target);//updated = true;
					target.add(PermissionFragment.this);
				}
				@Override
				public boolean isEnabled() {
					return PermissionsTab.this.getEditor2().isEditionEnabled();
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
	
	
	/**  
	 * 
	 * @param id
	 * @param editor
	 * @param rows
	 * @param permissions
	 * @param readOnly
	 */
	public PermissionsTab(String id, IModel<T> model, Editor<Role> editor, AclRow row, List<Permission> permissions) {
		super(id);
		setEditor2(editor);
		setOutputMarkupId(true);
		this.row = row;
		this.model=model;
		DataTable<AclRow, String> table = new AjaxFallbackDefaultDataTable<AclRow, String>("rows", getColumns(permissions), new AclRowsProvider(), 30);
		add(table);
		
		Link<Void> te=new Link<Void>("template-link") {
			@Override
			public void onClick() {
				PermissionsTab.this.onClick();
			}
			
			public boolean isVisible() {
				return  PermissionsTab.this.getModel()!=null &&
						PermissionsTab.this.getModel().getObject() instanceof ProcessLauncher;
			}
		};
		
		StringResourceModel rs=new StringResourceModel("template", PermissionsTab.this, null).setParameters(model.getObject().getDisplayName());
		Label a = new Label(  "template",  rs);
		a.setEscapeModelStrings(false);
		te.add(a);
		add(te);
	}

	
	
	protected void onClick() {}
	
	
	public Principal getPrincipal() {
		return principal;
	}

	public void setPrincipal(Principal principal) {
		this.principal = principal;
	}

	public IModel<T> getModel() {
		return this.model;
	}

	
	public List<IColumn<AclRow, String>> getColumns(List<Permission> permissions) {
		
		List<IColumn<AclRow, String>> columns = new ArrayList<IColumn<AclRow, String>>();
		
		Locale locale=getSessionUser().getLocale();
		
		for (final Permission permission : permissions)  {
			AbstractColumn<AclRow, String> permissionColumn = new AbstractColumn<AclRow, String>(new Model<String>(((KbeePermission)permission).getLabel(locale))) {
				public void populateItem(
						Item<ICellPopulator<AclRow>> cellItem, 
						String componentId,
						IModel<AclRow> model){
					PermissionFragment f=new PermissionFragment(componentId, model.getObject(), permission);
					f.add(new AttributeModifier("style", "width:100%;"));
					cellItem.add(f);
					
				}
				@Override
				public String getCssClass()	{
					return "permission col-lg-2";
				}
			};
			columns.add(permissionColumn);
		}
		return columns;
	}

	public void onUpdate(AjaxRequestTarget target) {
		target.add(this);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		if (model!=null)
			 model.detach();
		
		principal = null;

		if (row!=null) 
			row.detach();
	}

	protected User getSessionUser() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();	
	}
}
