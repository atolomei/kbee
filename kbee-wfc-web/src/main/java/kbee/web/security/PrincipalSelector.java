package kbee.web.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.DefaultNestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.model.Classification;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntityMember;
import com.novamens.content.model.EntitySet;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.security.Role;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.dao.Proxy;
import com.novamens.kbee.content.model.KbeeEntityMember;
import com.novamens.kbee.content.repository.MemberRepository;
import com.novamens.kbee.content.security.KbeeEntityRole;
import com.novamens.security.Principal;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.model.ObjectModel;

@SuppressWarnings("serial")
public class PrincipalSelector extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	private List<IModel<DataSet>> hierarchy = null;
	private AbstractTree<PrincipalNode> treeview;
	int alevel = 0;
	
	public interface PrincipalNode extends Serializable {
		public String getDisplayName();
		public Iterator<PrincipalNode> getChilds();
		public boolean isPrincipal();
		public Principal getPrincipal();
		public boolean hasChilds();
		public String getCss();
	}	 
	
	public class PrincipalTreeProvider implements ITreeProvider<PrincipalNode> {
		private static final long serialVersionUID = 1L;

		public PrincipalTreeProvider() {
		}

		public Iterator<PrincipalNode> getRoots() {
			List<PrincipalNode> roots = new ArrayList<>();
			List<DataSetMember> members = isSecurityAdmin()
				? getMemberRepository().findAll(getDataSet(0)) 
				: getFederatedRoots();
			for (DataSetMember member : members) {
				roots.add(new EntityNode((EntityMember)member, 0));
			}
			return roots.iterator();
		}
		
		public Iterator<PrincipalNode> getChildren(PrincipalNode node) {
	 		return node.getChilds();
		}
		
		public boolean hasChildren(PrincipalNode node) {
			return node.hasChilds();
		}
		
		@Override
		public IModel<PrincipalNode> model(PrincipalNode node) {
			return new Model<PrincipalNode>(node);
		}
		
		public void detach() {
		}
		
		public List<DataSetMember> getFederatedRoots() {
			List<DataSetMember> administered = new ArrayList<>();
			for (int level = 0; level<getHierarchy().size(); level++) {
				List<DataSetMember> members = getAdministered(level);
				if (!members.isEmpty()) {
					alevel = level;
					for (int l = level-1; level>0; level--) {
						members = filter(members, l);
					}
					administered = members;
					break;
				}
			}
			return administered;
		}
	}

	public boolean isSecurityAdmin() {
		boolean is_root = ServiceLocator
			.getService(SecurityService.class)
			.isRoot();
		if (is_root) return true;
		boolean is_admin = ServiceLocator
			.getService(SecurityService.class)
			.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());;
		if (is_admin) return true;
		boolean is_security = ServiceLocator
			.getService(SecurityService.class)
			.isMember(KbeeGlobalRole.SECURITY.getId());;
		return is_security;
	}
	

	
	public class RoleNode implements PrincipalNode {
		IModel<EntityMember> entitymodel;
		IModel<Role> rolemodel;
		public RoleNode(Role role, EntityMember entity) {
			rolemodel = new ObjectModel<Role>(role);
			entitymodel = new ObjectModel<EntityMember>(entity);
		}
		public String getDisplayName() {
			return getRole().getDisplayName();
		}
		public Iterator<PrincipalNode> getChilds() {
			return null;
		}
		public Role getRole() {
			return rolemodel.getObject();
		}
		public EntityMember getEntity() {
			return entitymodel.getObject();
		}
		public Principal getPrincipal() {
			return ((KbeeEntityMember)entitymodel.getObject()).getGroup(getRole());
		}
		public boolean isPrincipal() {
			return true;
		}
		public boolean hasChilds() {
			return false;
		}
		public String getCss() {
			return "fa fa-users";
		}
	}	
	
	public class EntityNode implements PrincipalNode {
		IModel<EntityMember> model;
		int level;
		public EntityNode(EntityMember entity, int level) {
			model = new ObjectModel<>(entity);
			this.level=level;
		}
		public String getDisplayName() {
			return getEntity().getDisplayName();
		}
		public Iterator<PrincipalNode> getChilds() {
			List<PrincipalNode> roles = new ArrayList<>();
			List<PrincipalNode> entities = new ArrayList<>();
			List<PrincipalNode> childs = new ArrayList<>();
			//int i = alevel;
			if (getLevel()<getMaxLevel()-1) {
				List<DataSetMember> members = isSecurityAdmin() || getLevel()>=alevel
					? getMemberRepository().findAggregationValues(getEntity(), getDataSet(getLevel()+1))
					: getAdministered(getEntity(), getLevel()+1);		
				for (DataSetMember member : members) {
					EntityMember entity = (EntityMember)Proxy.Unproxy(member);
					if (entity.getState().equals(ObjectState.ENABLED)) {
						entities.add(new EntityNode(entity, getLevel()+1));
					}
				}
			}	
			for (Role role : PrincipalSelector.this.getRoles(getDataSet(getLevel()))) {
				if (!role.isDefault()) {
					roles.add(new RoleNode(role, getEntity()));
				}
			}
			childs.addAll(sort(roles));
			childs.addAll(sort(entities));
			return childs.iterator();
		}
		public EntityMember getEntity() {
			return model.getObject();
		}
		public List<Role> getRoles() {
			return PrincipalSelector.this.getRoles(getDataSet(getLevel()));
		}
		public int getLevel() {
			return level;
		}
		public void setLevel(int level) {
			this.level = level;
		}
		public boolean isPrincipal() {
			return true;
		}
		public Principal getPrincipal() {
			if (getEntity().getGroup()!=null) {
				return getEntity().getGroup();
			}
			PrincipalNode node = getChilds().next();
			Principal principal = node.getPrincipal();
			return principal;
			
		}
		public boolean hasChilds() {
			if (getLevel()>=getMaxLevel()-1)
				return false;
			List<DataSetMember> childs = getMemberRepository().findAggregationValues(getEntity(), getDataSet(getLevel()+1));
			if (childs.size()>0)
				return true;
			return getRoles().size()>1;
		}
		public String getCss() {          
			return "fa fa-building";
		}
		private List<PrincipalNode> sort(List<PrincipalNode> nodes) {
			Collections.sort(nodes, new Comparator<PrincipalNode>() {
				@Override
				public int compare(PrincipalNode a, PrincipalNode b) {
					return a.getDisplayName().compareTo(b.getDisplayName());
				}
			});
			return nodes;
		};
	}
	
	public class TreeLabel extends Fragment {
		
		public TreeLabel(String id, PrincipalNode node) {
			super(id, "tree-label", PrincipalSelector.this);
			WebMarkupContainer i = new WebMarkupContainer("icon");
			i.add(new AttributeModifier("class", node.getCss()));
			add(i);
			add(new Label("label", new Model<String>(node.getDisplayName())));
		}
	}	
			


	public PrincipalSelector(String id) {
		super(id);
	}
	
	public boolean isEnabled() { 
		return !getHierarchy().isEmpty();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		setVisible(false);
		
		ITreeProvider<PrincipalNode> provider = new PrincipalTreeProvider();
		   
		treeview = new DefaultNestedTree<PrincipalNode>("tree", provider) {
			@Override
			protected Component newContentComponent(String id, IModel<PrincipalNode> model)	{
				return new Folder<>(id, this, model) {
					protected Component newLabelComponent(String id, IModel<PrincipalNode> model)	{
						return new TreeLabel(id, getModelObject());
					}
					protected boolean isClickable() {
						return getModelObject().isPrincipal();
					}
					@Override
					protected void onClick(Optional<AjaxRequestTarget> targetOptional) {
						PrincipalNode node = getModelObject();
						onSelect(targetOptional.get(), node);
						if (node.isPrincipal()) {
							if (node.getPrincipal()==null) {
								if (node instanceof RoleNode) {
									RoleNode roleNode = (RoleNode)node;
									Group group = ServiceLocator
										.getService(ContentSystemSecurityService.class)
										.getGroup(roleNode.getEntity(), roleNode.getRole());
									onSelect(targetOptional.get(), group);
								}	
							}
							else {
								onSelect(targetOptional.get(), node.getPrincipal());
							}
						}	
					}
					@Override
					protected String getStyleClass() {
						String styleClass;
						PrincipalNode node = getModelObject();
						if (treeview.getState(node) == State.EXPANDED)					{
							styleClass = getOpenStyleClass();
						}
						else{
							styleClass = getClosedStyleClass();
						}
						styleClass = ".entity-principal-node";
						return styleClass;
					}
				};
			}
		};
			
		add(new AjaxLink<Void>("close") {
			public void onClick(AjaxRequestTarget target) {
				PrincipalSelector.this.setVisible(false);
				onClose(target);
			}
		});
			
		add(treeview);
	}
	
	protected void onSelect(AjaxRequestTarget target, Principal principal) {
		PrincipalSelector.this.setVisible(false);
		onClose(target);
	}
	
	protected void onSelect(AjaxRequestTarget target, PrincipalNode principal) {
	}
	
	protected void onClose(AjaxRequestTarget target) {
	}
	
	private int getMaxLevel() {
		return getHierarchy().size();
	}
	
	private MemberRepository getMemberRepository() {
		return ((MemberRepository)getRepository(DataSetMember.class));
	}
	
	private DataSet getDataSet(int level) {
		return getHierarchy().get(level).getObject();
	}
	
	private List<DataSetMember> getAdministered(EntityMember parent, int level) {
		List<DataSetMember> administered = new ArrayList<>();
		for (DataSetMember member : getAdministered(level)) {
			for (Classification classification : member.getClassification()) {
				if (classification!=null && 
					classification.getDataSetMember().equals(parent)) {
					administered.add(member);
				}		
			}	
		}
		return administered;
	}
	
	private List<DataSetMember> getAdministered(int level) {
		List<DataSetMember> administered = new ArrayList<>();
		DataSet dataSet = getDataSet(level);
		for (UserRole role : getSessionUserRoles()) {
			if (role.getRole().isEntity()) {
				KbeeEntityRole entityRole = (KbeeEntityRole)Proxy.Unproxy(role.getRole());
				if (entityRole.getManagedEntities().contains(dataSet)) {
					administered.add(role.getEntity());
				}
			}
		}
		return administered;
	}
	
	private List<DataSetMember> filter(List<DataSetMember> members, int uplevel) {
		List<DataSetMember> filtered = new ArrayList<>();
		DataSet dataSet = getDataSet(uplevel);
		for (DataSetMember member : members) {
			for (Classification classification : member.getClassification()) {
				if (classification!=null && 
					dataSet.equals(classification.getDataSetMember().getDataSet())) {
					if (!filtered.contains(classification.getDataSetMember())) {
						filtered.add(classification.getDataSetMember());
					}
				}
			}
		}
		return filtered;
	}
	
	private List<UserRole> getSessionUserRoles() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getRoles();
	}
	
	private List<Role> getRoles(DataSet dataset) {
		EntitySet entitySet = (EntitySet)Proxy.Unproxy(dataset);
		return getSecurityDao().getRolesByEntitySet(entitySet);
	}
	
	private List<IModel<DataSet>> getHierarchy() {
		if (hierarchy==null) {
			hierarchy = new ArrayList<>();
			for (String datasetname : getHierarchyNames()) {
				DataSet dataset = getContentDao().findDataSetByAlias(datasetname.trim(), getDomain().getId());
				hierarchy.add(new ObjectModel<DataSet>(dataset));
			}
		}
		return hierarchy;
	}
	
	// organization.hierarchy.domain
	private List<String> getHierarchyNames() {
		List<String> names = new ArrayList<>();
		//String namesvalue = ServiceLocator.getService(SystemParameterService.class).getParameter("organization.hierarchy."+getDomain().getName(), null);
		String namesvalue = "empresa, area, departamento";
		names = namesvalue!=null ? Arrays.asList(namesvalue.split(",")) : new ArrayList<>();
		return names;
	}
	
	private ContentSecurityDao getSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
}
 