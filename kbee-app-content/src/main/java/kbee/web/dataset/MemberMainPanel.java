package kbee.web.dataset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.entity.Person;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.EntityMember;
import com.novamens.content.model.LabelMember;
import com.novamens.content.model.PersonMember;
import com.novamens.content.model.SecuredMember;
import com.novamens.content.security.EntityRole;
import com.novamens.content.security.Role;
import com.novamens.content.service.DataSetService;
import com.novamens.content.service.DomainService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.security.KbeeUser;

import com.novamens.kbee.wicket.markup.html.console.panel.VerticalLayout;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.markup.html.page.PageMainTabs;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.MenuItemWithModelPanel;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.tabs.AbstractTabKB;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;
import kbee.web.console.SimpleMenuPanel;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.model.object.AuditTrailObjectPanel;
import kbee.web.object.ObjectStateEditor;
import kbee.web.panel.AlertPanel;
import kbee.web.security.SecuredMemberAclEditor;
import kbee.web.security.user.PersonFormEditor;

@SuppressWarnings("serial")
public class MemberMainPanel extends ObjectEditor<DataSetMember> implements PageMainTabs {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(MemberMainPanel.class.getName());
	
	final static boolean is_root = 
		ServiceLocator
		.getService(SecurityService.class)
		.isRoot();
	final boolean role_admin = 
		ServiceLocator.
		getService(SecurityService.class)
		.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final static boolean role_security = is_root || 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.SECURITY.getId());
	
	private IModel<DataSetMember> aggregatormodel;
	private String initial_tab;
	private List<IModel<DataSet>> entitiessets;
	
	/**
	 * @param model
	 * @param aggregatormodel
	 * @param isNew
	 */
	public MemberMainPanel(IModel<DataSetMember> model, IModel<DataSetMember> aggregatormodel, boolean isNew) {
		super("editor", model);
		
		setModel(model);
 		setIsNew(isNew);
 		setAggregatorModel(aggregatormodel);
 		
		List<ITab> tabs = new ArrayList<ITab>();
		
		DataSetMember member = getModelObject();
		
		tabs.add(new AbstractTabKB(getLabel("editor.info"), "info") {
			@Override
			public Panel getPanel(String panelId) {
				if (getModel().getObject() instanceof LabelMember) {
					return new LabelMemberEditor(panelId, new ObjectModel<LabelMember>( (LabelMember) getModel().getObject()), isNew()) {
						@Override
						public void onUpdate(AjaxRequestTarget target) {
							((MemberHeaderPanel)MemberMainPanel.this.get("member-panel")).onUpdate(target);
						}
						@Override
						public void onCancel(AjaxRequestTarget target) {
							MemberMainPanel.this.onCancel(target);
						}
						@Override
						public void setEditionEnabled(boolean value) {
							MemberMainPanel.this.setEditionEnabled(value);
						}
						@Override
						public boolean isEditionEnabled() {
							return MemberMainPanel.this.isEditionEnabled();
						}
					};
				}
				else 
				if (getModel().getObject() instanceof PersonMember) {
					return new PersonFormEditor(panelId, getModel(), isNew());
				}
				else
					return new MemberFormEditor(panelId, getModel(), isNew());
			}
		});
		
		if (member instanceof PersonMember) {
			tabs.add(new AbstractTabKB(getLabel("editor.account"), "account") {
				@Override
				public Panel getPanel(String panelId) {
					return new PersonAccountEditor(panelId, getModel());
				}
			});
		}
		
		
		for (DataSet aggregation : getAggregations()) {
			IModel<DataSet> templatemodel = new ObjectModel<DataSet>(aggregation); 
			tabs.add(new AbstractTabKB(new Model<String>(templatemodel.getObject().getDisplayName() +" <span class=\"ago\">("+getLabel("built-in").getObject()+")</span>"), aggregation.getAlias()!=null?aggregation.getAlias():aggregation.getId().toString()) {
				@Override
				public Panel getPanel(String panelId) {
					return new AggregationConsole(panelId, templatemodel, getModel());
				}
			});
		}

		tabs.add(new AbstractTabKB(getLabel("editor.state"), "status") {
			@Override
			public Panel getPanel(String panelId) {
				final boolean role_model = role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
				final boolean role_dataset_members = role_model || role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());
				boolean read_only =  !role_dataset_members || getModel().getObject().getDataSet().getDataSetType()==DataSetType.EXTERNAL;
 				read_only = read_only || (MemberMainPanel.this.getModel().getObject().getDataSet().isReadonly() && !isRoot());
 				return new ObjectStateEditor<DataSetMember>(panelId, getModel(), read_only);
			}
		});

		
		if (getModel().getObject() instanceof EntityMember) {
			tabs.add(new AbstractTabKB(getLabel("editor.rules"), "rules") {
				@Override
				public Panel getPanel(String panelId) {
					return new EntityRulesPanel(panelId, getModel());
				}
			});	
		}
		
		

		
		tabs.add(new AbstractTabKB(getLabel("editor.externalid"), "externalid") {
			@Override
			public Panel getPanel(String panelId) {
				return new MemberExternalIdEditor(panelId, getModel());
			}
		});
		
		tabs.add(new AbstractTabKB(getLabel("editor.notes"), "notes") {
			@Override
			public Panel getPanel(String panelId) {
				boolean is_read_only = MemberMainPanel.this.getModel().getObject().getDataSet().isReadonly();
				return new MemberNotesEditorPanel(panelId, getModel(), isNew(), is_read_only);
			}
		});
		
		if (getModel().getObject() instanceof EntityMember) {
			tabs.add(new AbstractTabKB(getLabel("editor.roles"), "roles") {
				@Override
				public Panel getPanel(String panelId) {
					return new MemberRolesPanel(panelId, getModel());
				}
			});
		}
		
		if (member instanceof SecuredMember) {
			tabs.add(new AbstractTabKB(getLabel("editor.permissions"), "permissions") {
				@Override
				public Panel getPanel(String panelId) {
					return new SecuredMemberAclEditor(panelId, getModel());
				}
			});
		}
		
		
		tabs.add(new AbstractTabKB(getLabel("editor.audit"), "audit") {
			@Override
			public Panel getPanel(String panelId) {
				return new AuditTrailObjectPanel<DataSetMember>(panelId, getModel());
			}
		});
		
		
		VerticalLayout<ITab> editor = new VerticalLayout<ITab>("tabs", this.getClass().getName(), tabs);
		
		List <MenuItemFactory<Classifier>>  menuitems =  new ArrayList<MenuItemFactory<Classifier>>();
		
		for (Classifier cla: getContentDao().getClassifiers(getDomain())) {
			final Serializable cla_id=cla.getId();
			if (cla.getDataSet()!=null && cla.hasHome() && cla.getDataSet().equals( getModel().getObject().getDataSet()) ) {
				menuitems.add(
		       		 new MenuItemFactory<Classifier>() {
	    				@Override
	    				public AbstractMenuItemPanelV5<Classifier> getItem(String id) {
			    					
			    					return new MenuItemWithModelPanel<Classifier>(id, getClassifierModel(cla_id)) {
			    						@Override
			    						public void onDetach() {
			    							super.onDetach();
			    							getModel().detach();
			    						}
			    						
			    						@Override
			    						public String getLabel() {
			    							return   getModel().getObject().getDisplayName();
			    						}
			    						
			    						
			    						public void onClick() {
			    							try {
			    								
			    								Classifier da=getModel().getObject();
			    								DataSetMember dm = MemberMainPanel.this.getModel().getObject();
			    								if (dm instanceof EntityMember) {
			    									// setResponsePage(new DashboardEntityPage( new ObjectModel<EntityMember> ( (EntityMember) dm),  new ObjectModel<Classifier>(da)));
			    									setResponsePage(new RedirectPage("/entityhome/"+String.valueOf( ( (EntityMember) dm).getId())+"/"+String.valueOf(da.getId())));
			    								}
			    								else 
			    									throw new IllegalArgumentException( dm.getDisplayName() + " must be " + EntityMember.class.getName());

			    								// Entity page: 
			    								// WebPage page=ServiceLocator.getService(PortalPanelService.class).getWebPage(getModel().getObject());
			    								// setResponsePage(page);
			    								// entityhome/${entity_id}/${classifier_id}
			    								//setResponsePage(page);
			    								
			    							} catch (Exception e) {
			    								logger.error(e);
			    								setResponsePage(new ApplicationErrorPage<>(e));
			    							}
			    						}
			    					};
			    			}
			       		 });
			}
		}

		editor.setTitle(new StringResourceModel("sections", this, null));
		
        if (menuitems.size()>0) {
	        SimpleMenuPanel<DataSetMember,Classifier> panel = new SimpleMenuPanel<DataSetMember, Classifier>("header-bottom-panel", getModel(), menuitems);
		    panel.setTitle(getLabel("entity-mini-site"));
		    editor.setHeaderBottomPanel(panel);
        }
        
        if (getModel().getObject().getState()!=ObjectState.ENABLED) {
        	StringResourceModel user_l = new StringResourceModel("item-deleted", this, null);
        	StringResourceModel user_title = new StringResourceModel("deleted", this, null);
        	AlertPanel<DataSetMember> alert= new AlertPanel<DataSetMember>("content-top-panel", AlertPanel.DANGER, getModel(), user_title, user_l);
        	alert.add(new AttributeModifier("style", "margin-bottom:30px; float:left; width:100%;"));
        	
        	alert.setIcon(AlertPanel.ATTENTION);
        	editor.setContentTopPanel(alert);
        }
        else if (getUserProfile()!=null) {
        	
        	
        	User user =  getUserProfile().getUser();
        	IModel<String> text = role_security ? 
        		getLabel("user-link", user.getId().toString(), user.getFirstLastName()) :
        		getLabel("user-linked", user.getFirstLastName());	
        	AlertPanel<DataSetMember> alert = new AlertPanel<DataSetMember>("content-top-panel", 
        		AlertPanel.INFO, 
        		getModel(), 
        		getLabel("link-user-title"), 
        		text);
        	alert.add(new AttributeModifier("style", "margin-bottom:30px; float:left; width:100%;"));
        	alert.setIcon("fa-duotone fa-user-check");
        	editor.setContentTopPanel(alert);
        }
        
        editor.setSections(VerticalLayout.COLS_9X3);
        
		add(editor);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		@SuppressWarnings("unchecked")
		VerticalLayout<ITab> tabs = (VerticalLayout<ITab>) get("tabs");
		tabs.setTitle(new StringResourceModel("sections", this, null));
		
		int sel = tabs.getSelectedTab();
		if (sel==-1)
			sel=0;
		
		String str =  (tabs.getTabs().get(sel)).getTitle().getObject();
		((AbstractKbeeWebPage) getPage()).setPageInternalSectionHelpKey(str);
	}

	@Override
	public void setEditionEnabled(boolean editionEnabled) {
		super.setEditionEnabled(editionEnabled);
	}
	
	public void onCancel(AjaxRequestTarget target) {
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void setInitialTab(String a) {
			try {
				initial_tab=a;
				((VerticalLayout<ITab>) get("tabs")).setSelectedTab(a);
			} 
			catch (Exception e) {
				logger.error(e);
			}
	}

	@Override
	public String getInitialTab() {
		return initial_tab;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (entitiessets!=null)
			entitiessets.forEach(item-> item.detach());
		
		if (aggregatormodel!=null)
			aggregatormodel.detach();
		
	}
	
	public List<IModel<DataSet>> getDataSets() {
		if (entitiessets==null) {
			List<DataSet> list = getDomain().getService(DomainService.class).getEntitySets();
			for (DataSet ds : list) 
				if (hasRole(ds)) {
					entitiessets.add( new ObjectModel<DataSet>(ds));
				}	
		}
		return entitiessets;
	}
	
	protected boolean isFreeVersion() {
		return getDomain().getDomainType()==DomainType.EXPRESS;
	}
	
	
	protected List<DataSet> getAggregations() {
		List<DataSet> aggregations = new ArrayList<DataSet>();
		aggregations = getModelObject().getDataSet().getService(DataSetService.class).getAggregations();
		return aggregations;
	}
	
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	protected boolean isRoot() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(getSessionUser());
	}
	
	private KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}
	
	protected void setAggregatorModel(IModel<DataSetMember> model) {
		this.aggregatormodel = model;
	}
	
	protected IModel<DataSetMember> getAggregatorModel() {
		return this.aggregatormodel;
	}
	
	protected IModel<Classifier> getClassifierModel(Serializable cla_id) {
		return new ObjectModel<Classifier>( (Classifier) getContentDao().findModelObjectById(Classifier.class, cla_id));
	}
	
	// El usuario tiene algun rol en entidades del dataset
	private boolean hasRole(DataSet ds) {
		if (ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId())) {
			return true;
		};
		UserProfile usersessionprofile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		for (UserRole userrole : usersessionprofile.getRoles()) {
			Role role = userrole.getRole();
			if (role.isEntity()) {
				EntityRole entityrole = (EntityRole)getContentDao().unproxy(role); 
				if (entityrole.getClassifier().getDataSet().equals(ds) && entityrole.getClassifier().hasHome()) {
					return true;
				}
			}
		}
		return false;
	}

	private UserProfile getUserProfile() {
		if (getModel().getObject() instanceof PersonMember) {
			Person person = ((PersonMember)getModelObject());
			UserProfile profile = person.getProfile(UserProfile.class);
			return profile;
		}
		else
			return null;
	}
}
