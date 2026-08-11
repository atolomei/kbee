package kbee.web.dataset;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.user.UserLabel;
import com.novamens.content.user.UserLabelsService;
import com.novamens.dom.Domain;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.model.ObjectModel;

@Deprecated
public class LabelsPanel extends ObjectEditorPanel<com.novamens.content.user.UserLabel> {
			
	private static final long serialVersionUID = 1L;

	static Logger logger = LogManager.getLogger(LabelsPanel.class.getName());

	private static final int LIST_LABEL 		= 1;
	private static final int LIST_EDITING_LABEL = 2;
	private static final int ADDING_LABEL 		= 3;

	private List<IModel<UserLabel>> labels;
	
	private int state = LIST_LABEL;
	
	final boolean isroot  = ServiceLocator.getService(SecurityService.class).isRoot();
	
	final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model = role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	final boolean role_dataset_members_read = role_model || role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_READ.getId());
	final boolean role_dataset_members = role_model || role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());
	
	
	public class LabelModel implements IModel<UserLabel> {
		
		private static final long serialVersionUID = 1L;
		
		private UserLabel label;
		private IModel<UserLabel> model;
		private String title, color;
		
		public LabelModel(UserLabel label) {
			model = new ObjectModel<UserLabel>(label);
			title = label.getLabel();
			color = label.getCss();
			this.label = label;
		}
		public UserLabel getObject() {
			if (label==null) {
				label = model.getObject();
				label.setLabel(title);
				label.setCss(color);
			}	
			return label;
		}
		public void setObject(UserLabel label) {
		}
		
		public void detach() {
			if (label!=null) {
				title = label.getLabel();
				color = label.getCss();
				model.detach();
				this.label = null;
			}
		}
	}
	

	public class LabelView extends Fragment {
					
		private static final long serialVersionUID = 1L;
		
		private boolean edit_enabled = false;
		
		private IModel<UserLabel> model;
		private int index;
		public LabelView(String id, String markupid, IModel<UserLabel> model) {
			super(id, markupid, LabelsPanel.this);
			setModel(model);
			
			setOutputMarkupId(true);

			WebMarkupContainer ec = new WebMarkupContainer("edit-container");
			ec.setOutputMarkupId(true);
			add(ec);
			
			ec.add(new AttributeModifier("class", new Model<String>() {
				private static final long serialVersionUID = 1L;
				@Override
				public String getObject() {
					return (LabelView.this.isEditEnabled() ? "edit-bck" : "view-bck" );
				}
			}));
			
			WebMarkupContainer icon = new WebMarkupContainer("icon") {
				private static final long serialVersionUID = 1L;
				@Override
				public boolean isVisible() {
					return true;
				}
			};
			
			icon.add(new AttributeModifier("class", "far fa-tag " + model.getObject().getCss()));
			ec.add(icon);
			
			Label label = new Label("label", model.getObject().getLabel());
			ec.add(label);
			
			Panel menuPanel = getMenu();
			
			WebMarkupContainer menulink = new WebMarkupContainer("menulink") {
				private static final long serialVersionUID = 1L;
				public boolean isVisible() {
					return true;
				}
			};
			
			ec.add(new LabelEditor("editor", getModel()) {
				 
				private static final long serialVersionUID = 1L;
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					int index = LabelsPanel.this.getIndex(getModel());
					if (index>=0 && index< getLabels().size()) {
						getLabels().get(index).detach();
					}
					setState(LabelsPanel.LIST_LABEL);
					LabelView.this.setEditEnabled(false);
					LabelView.this.get("edit-container:editor").setVisible(false);
					target.add(LabelsPanel.this.get("label-list"));
				}
				
				@Override
				public void onCancel(AjaxRequestTarget target) {
					setState(LabelsPanel.LIST_LABEL);
					LabelView.this.setEditEnabled(false);
					LabelView.this.get("edit-container:editor").setVisible(false);
					target.add(LabelView.this.get("edit-container"));
				}
			});
			
			(ec.get("editor")).setVisible(false);
			
			WebMarkupContainer ddown = new WebMarkupContainer("dropdown") {
				private static final long serialVersionUID = 1L;
				public boolean isVisible() {
					return  !LabelView.this.isEditEnabled();
				}
			};
			
			ec.add(ddown);
			ddown.add(menulink);
			ddown.add(menuPanel);
		}
		
		private void edit(AjaxRequestTarget target) {
			LabelView.this.get("edit-container:editor").setVisible(true);
			LabelView.this.setEditEnabled(true);
			setState(LabelsPanel.LIST_EDITING_LABEL);
			target.add(LabelView.this.get("edit-container"));
		}
		
		public void setModel(IModel<UserLabel> model) {
			this.model = model;
		}
		
		public IModel<UserLabel> getModel() {
			return model;
		}
		
		public UserLabel getResource() {
			return getModel().getObject();
		}
		public int getIndex() {
			return index;
		}
		@Override
		public void onDetach() {
			model.detach();
			super.onDetach();
		}
		
		public boolean isEditEnabled() {
			return this.edit_enabled;
		}
		
		public void setEditEnabled(boolean en) {
			this.edit_enabled=en;
		}
		
		
		protected void setIndex(int index) {
			this.index = index;
		}
		
		@SuppressWarnings("serial")
		protected Panel getMenu() {
			ContextMenuPanel<UserLabel> menu = new ContextMenuPanel<UserLabel>(getModel());
			menu.addItem(new MenuItemFactory<UserLabel>() {
				@Override
				public AbstractMenuItemPanelV5<UserLabel> getItem(String id) {
					return new AjaxMenuItemPanelV5<UserLabel>(id) {
						@Override
						public void onClick(AjaxRequestTarget target) {
							edit(target);
						}
						@Override
						public String getLabel() {	
							return LabelsPanel.this.getStringLabel("menu.edit");
						}
						
						
												
						@Override
						public boolean isEnabled() {
							if (isSupportUser() && !isRoot())
								return false;
							
							return role_dataset_members;
						}
						
					};
				}
			});
			
			menu.addItem(new MenuItemFactory<UserLabel>() {
				@Override
				public AbstractMenuItemPanelV5<UserLabel> getItem(String id) {
					return new SeparatorMenuItemPanelV5<UserLabel>(id) {
						@Override
						public String getCssClass() {
							return "divider";
						}
						@Override
						public boolean isVisible() {
								return isroot;
							 // return role_dataset_members;
						}
					};
				}
			});

			menu.addItem(new MenuItemFactory<UserLabel>() {
				@Override
				public AbstractMenuItemPanelV5<UserLabel> getItem(String id) {
					return new AjaxMenuItemPanelV5<UserLabel>(id) {
						@Override
						public void onClick(AjaxRequestTarget target) {
							((Dialog) LabelsPanel.this.get("remove-dialog")).open(target, new Dialog.Handler() {
								@Override
								public void onClick(AjaxRequestTarget target, Button button) {
									if (button.key().equals(Dialog.Delete.key())) {
										delete(LabelView.this.getModel());
										target.add(LabelsPanel.this.get("label-list"));
									}
								}
							}, getModelObject().getLabel());
						}
						@Override
						public String getLabel() {	
							return LabelsPanel.this.getStringLabel("menu.delete");
						}
						
						@Override
						public boolean isVisible() {
								return isroot;
							 // return role_dataset_members;
						}
						
						@Override
						public boolean isEnabled() {
							
							if (isSupportUser() && !isRoot())
								return false;
							
							return isroot;
							//return role_dataset_members;
						}
						
					};
				}
			});
			return menu;
		}
	}

	/** 
	 * Constructor
	 */ 

	public LabelsPanel(String id) {
		super(id);
		setOutputMarkupId(true);
		this.labels = new ArrayList<IModel<UserLabel>>();
		for (UserLabel label: getRoot().getService(UserLabelsService.class).getLabels()) 
			this.labels.add(new ObjectModel<UserLabel>(label));
		add(new Dialog("remove-dialog", "dialog.delete.title", "dialog.delete.message", Dialog.Cancel, Dialog.Delete));
	}

 
	@Override
	public void onDetach() {
		if (getLabels()!=null && getLabels().size()>0) {
			for(IModel<UserLabel> md: getLabels()) {
				md.detach();
			}
		}
		super.onDetach();
	}
	
 
	@Override
	public void onInitialize() {
		super.onInitialize();
		if (get("label-list")==null)
			addLabelList();
	}


	private void addLabelList() {
		 
		WebMarkupContainer list = new WebMarkupContainer("label-list") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return LabelsPanel.this.getState()!=LabelsPanel.ADDING_LABEL;
			}
		};
		add(list);
		list.setOutputMarkupId(true);
		list.add(new ListView<IModel<UserLabel>>("label-element", 
				 new PropertyModel<List<IModel<UserLabel>>>(this, "labels")) {
					private static final long serialVersionUID = 1L;
						protected void populateItem(ListItem<IModel<UserLabel>> item) {
							item.add(new LabelView("label-view", "label-view-fragment", item.getModelObject()));
							item.add(new AttributeModifier("class", new Model<String>() {
								 	private static final long serialVersionUID = 1L;
									@Override	
									public String getObject() {
										return "media bck";
									}
								})); 
				}
		 });
		
		Panel panel = new Panel("new-label-editor") {
			private static final long serialVersionUID = 1L;
			public boolean isVisible() {
				return false;
			}
		};
		
		
		/**
		 * Only Root can add labels by the moment.
		 * labels will be converted into DataSets, then they will have the same treatment as the other DataSets.
		 */
		add(new AjaxLink<Object>("add-label") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				LabelsPanel.this.onAddLabel(target);	
			}
			
			@Override
			public boolean isVisible() {

				
				if (!isroot)
					return false;
				
				//if (!role_dataset_members)
				//	return false;
				
				return LabelsPanel.this.getState()!=LabelsPanel.ADDING_LABEL;
			}
			
			@Override
			public boolean isEnabled() {

				if (isSupportUser() && !isRoot())
					return false;
				
				return isroot;
				// return role_dataset_members;
			}
		});
		
		add(panel);
		
	 }
	
	protected boolean isSupportUser() {
		return ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	}
	

	protected boolean isRoot() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(getUser());
	}
	
	
	protected void onAddLabel(AjaxRequestTarget target) {

		UserLabel label = getRoot().getService(UserLabelsService.class).create("New Label", UserLabel.CSS[getLabels().size() % UserLabel.CSS.length].toLowerCase());
		IModel<UserLabel> lmodel = new ObjectModel<UserLabel>(label);
		this.getLabels().add(lmodel);
		
		LabelEditor editor = new LabelEditor("new-label-editor", lmodel) {
				private static final long serialVersionUID = 1L;
				@Override
				public boolean isVisible() {
					return getState()==LabelsPanel.ADDING_LABEL; 
				}
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					int index = getIndex(getModel());
					if (index>=0 && index<getLabels().size()) { 
						getLabels().get(index).detach();
						getLabels().get(index).getObject();
					}
					setState(LabelsPanel.LIST_LABEL);
					target.add(LabelsPanel.this);
				}
				
				/** 
				 * If it is a new Label, it is deleted onCancel.
				 */
				@Override
				public void onCancel(AjaxRequestTarget target) {
					if (isNewLabel())
						delete(getModel());
					target.add(LabelsPanel.this);
					setState(LabelsPanel.LIST_LABEL);
				}
			};
		
		editor.setTitlePanel(new StringResourceModel("newlabel", this, null).getString());
		editor.setIsNewLabel(true);
		
		addOrReplace(editor);
		
		setState(LabelsPanel.ADDING_LABEL);
		target.add(LabelsPanel.this);
		
	}

	protected void delete(IModel<UserLabel> model) {
		int index = getIndex(model);
		if (index>=0) {
			this.getLabels().remove(index);
			UserLabelsService labelsService = getRoot().getService(UserLabelsService.class);
			List<UserLabel> userlabels = labelsService.getLabels();
			for (UserLabel label: userlabels) {
					if (label.getId().toString().equals(model.getObject().getId().toString())) {
						labelsService.delete(label);
						break;
					}
				}
		}
	}
	
	protected String getStringLabel(String resourceKey) {
	return ((new StringResourceModel(resourceKey, this, null)).getString());
	}

	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}


	protected int getState() {
		return state;
	}
	

	protected void setState(int state) {
		this.state=state;
	}


	protected List<IModel<UserLabel>> getLabels() {
		return labels;
	}

	private KbeeUser getRoot() {
		Domain domain = getUser().getDomain();
		KbeeUser root = (KbeeUser)ServiceLocator.getService(SecurityService.class).findUserByUsername("root@"+domain.getName());
		return root;
	}
	
	private int getIndex(IModel<UserLabel> model) {
		try {
			int index = 0;
			UserLabel resource = model.getObject();
			for (IModel<UserLabel> resourcemodel : labels) {
				if (resource.getId().equals(resourcemodel.getObject().getId()))
					break;
				else
					index++;
			}
			return index;
			
		} catch (RuntimeException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			return -1;
		}
	}




}
