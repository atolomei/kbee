package kbee.web.eform;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.DefaultNestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.Folder;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IFormModelUpdateListener;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceFolder;
import com.novamens.content.base.ResourceNode;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.form.EFormContentData;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EResourceModel;
import com.novamens.content.form.ResourceAdded;
import com.novamens.content.form.ResourceRemoved;
import com.novamens.content.form.ResourceUpdated;
import com.novamens.content.form.ResourcesRemoved;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.FileService;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.event.EventService;
import com.novamens.kbee.content.form.KbeeEResourceSystemV3;
import com.novamens.kbee.content.resource.KbeeResourceNode;
import com.novamens.kbee.wicket.markup.html.console.event.SelectionEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.kbee.wicket.markup.html.console.tree.TreeProvider;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.markup.html.tree.TreeNodeSelection;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.metrics.domain.DomainMetricsService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.ScriptMenuItemPanel;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.form.CheckField;
import com.novamens.wicket.markup.html.form.DraggableBehavior;
import com.novamens.wicket.markup.html.form.DroppableBehavior;
import com.novamens.wicket.markup.html.form.Field.Width;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.markup.html.repeater.util.DataViewNavigationToolbar;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.AjaxBCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.util.logging.Logger;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.print.PrintMenuItemPanel;
import kbee.web.resource.FolderViewPanel;
import kbee.web.resource.ResourceNodeModel;
import kbee.web.resource.ResourceViewPanel;
import kbee.web.resource.ResourcesPanel;
import kbee.web.uploader.FileUploadedEvent;
import kbee.web.uploader.UploadBehavior;
import kbee.web.uploader.UploadTusBehavior;

@SuppressWarnings("serial")
public class EResourceSystemPanelV3 extends EFieldPanel<KbeeEResourceSystemV3> implements 
	ResourcesPanel, 
	IFormModelUpdateListener, 
	EventListener {
	
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(EResourceSystemPanelV3.class.getName());

	private List<IModel<ResourceNode>> resources = new ArrayList<>();
	private List<IModel<ResourceNode>> selection = new ArrayList<>();
	private ViewMode viewmode = ViewMode.ICON;
	private IModel<Content> contentmodel;
	private IModel<ResourceNode> foldermodel;
	private IModel<ResourceNode> selectionfoldermodel;
	private boolean selectAll = false;
	private boolean updated = false;
	private boolean selectionVisible = false;
	
	final long QUOTA = getDomain()!=null?getDomain().getQuota():0;
	final double DQUOTA = Double.valueOf(QUOTA).doubleValue();
	private static final double GB = 1000000000.0;
	private Sort sort = Sort.TITLE;
	
	static final int ITEMS_PER_PAGE = 20;
	
	private enum Layout {
		TREE,
		FLAT
	};
	
	private Layout layout = Layout.TREE;
	
	private enum Sort {
		TITLE,
		DATE_ASC,
		DATE_DESC
	};

	private class ResourceComparator implements Comparator<IModel<ResourceNode>> {
		@Override
		public int compare(IModel<ResourceNode> modela, IModel<ResourceNode> modelb) {
			try {
				Resource a = modela.getObject().getResource();
				Resource b = modelb.getObject().getResource();
				if (sort==Sort.TITLE)
					return a!=null 
						? a.getTitle().toLowerCase().compareTo(b.getTitle().toLowerCase()) 
						: (b!=null ? 1 : 0);
				else if (sort==Sort.DATE_DESC)
					return a!=null 
						? b.getLastModifiedOffsetDateTime().compareTo(a.getLastModifiedOffsetDateTime()) 
						: (b!=null ? 1 : 0);
				else
					return b!=null 
					? a.getLastModifiedOffsetDateTime().compareTo(b.getLastModifiedOffsetDateTime()) 
					:
						(a!=null ? 1 : 0);
			}
			catch (Exception e) {
				logger.error(e);
				return 0;
			}
		}
	}
	
	// wrapper serializable del resource node
	public class ResourceTreeNode implements Serializable, IDetachable {
		private IModel<ResourceNode> model;
		private String displayName;
		ResourceTreeNode(IModel<ResourceNode> model) {
			this.model = model;
			displayName = model.getObject().getTitle();
		}
		public ResourceNode getObject() {
			return model.getObject();
		}
		public IModel<ResourceNode> getModel() {
			return model;
		}
		public String getDisplayName() {
			return displayName;
		}
		public void detach() {
			model.detach();
		}
	}
	
	public class ResourceTreeProvider extends TreeProvider<ResourceTreeNode> {
		public Iterator<ResourceTreeNode> getRoots() {
			List<ResourceTreeNode> roots = new ArrayList<>();
			for (IModel<ResourceNode> model : getResources()) {
				ResourceNode node = model.getObject();
				if (node.getResource() instanceof ResourceFolder && node.getFolder()==null) {
					roots.add(getTreeNode(model));
				}
			}
			return roots.iterator();
		}
		public Iterator<ResourceTreeNode> getChildren(ResourceTreeNode node) {
			List<ResourceTreeNode> childs = new ArrayList<>();
			for (IModel<ResourceNode> model : getChilds((ResourceFolder)node.getObject().getResource())) {
				ResourceNode childnode = model.getObject();
				if (childnode.getResource() instanceof ResourceFolder) {
					childs.add(getTreeNode(model));
				}
			}
			return childs.iterator();
		}
		public boolean hasChildren(ResourceTreeNode node) {
			return getChildren(node).hasNext();
		}
		@Override
		public IModel<ResourceTreeNode> model(ResourceTreeNode object) {
			return new Model<ResourceTreeNode>(object);
		}
	}
	
	public class TreeFragment extends Fragment {
		ResourceTreeNode selected = null;
		AbstractTree<ResourceTreeNode> treeview;
		public TreeFragment(String id) {
			super(id, "tree-fragment", EResourceSystemPanelV3.this);
			
			setOutputMarkupId(true);
			
			this.treeview = new DefaultNestedTree<ResourceTreeNode>("tree", new ResourceTreeProvider()) {
				@Override
				protected Component newContentComponent(String id, IModel<ResourceTreeNode> node) {
					Folder<ResourceTreeNode> folder = new Folder<>(id, this, node) {
						protected Component newLabelComponent(String id, IModel<ResourceTreeNode> model) {
							return new Label(id, model.getObject().getDisplayName());
						}
						@Override
						protected void onClick(Optional<AjaxRequestTarget> targetOptional) {
							onSelect(targetOptional.get(), getModel());
							setFolder(selected.getModel());
				            fireScanAll(new TreeNodeSelection<ResourceTreeNode>(targetOptional.get(), getModel()));
						}
						@Override
						protected boolean isClickable() {
							return true;
						}
						@Override
						protected boolean isSelected() {
							return getFolder()!=null && getFolder().equals(getModelObject().getObject());
						}
						@Override
						 protected String getStyleClass() {
							String styleClass;
							ResourceTreeNode node = getModelObject();
							if (treeview.getState(node) == State.EXPANDED)	{
								styleClass = getOpenStyleClass();
							}
							else {
								styleClass = getClosedStyleClass();
							}
							if (isSelected()) {
								styleClass += " " + getSelectedStyleClass();
							}
							return styleClass;
						}
					};
					folder.add(new DroppableBehavior() {
						protected void onDrop(AjaxRequestTarget target, String id) {
							@SuppressWarnings("unchecked")
							ResourceTreeNode node = ((Folder<ResourceTreeNode>)getComponent()).getModelObject();
							IModel<ResourceNode> droppedmodel = EResourceSystemPanelV3.this.getResource(id);
							if (droppedmodel!=null && isFileNode(droppedmodel)) {
								move(droppedmodel.getObject(), (ResourceFolder)node.getObject().getResource());
							}
							refresh(target);
						}
					});
					return folder;
				}
			};
			
			add(treeview);
			
			add(new WicketEventListener<TreeNodeSelection<ResourceTreeNode>>() {
				@Override
				public void onEvent(TreeNodeSelection<ResourceTreeNode> event) {
					onSelect(event.getRequestTarget(), event.getModel());
				}
			});
		}	
		
		public void onSelect(AjaxRequestTarget target, IModel<ResourceTreeNode> model) {
			ResourceTreeNode node = model!=null ? model.getObject() : null;
			if (selected!=null) 
				treeview.updateNode(selected, target);
            selected = node;
			if (selected!=null) 
				treeview.updateNode(selected, target);
		}
	}
	
	/**---------------------------------------------------------------
	 */
	public class ToolbarFragment extends Fragment {
		public ToolbarFragment(String id) {
			super(id, "toolbar-fragment", EResourceSystemPanelV3.this);
			
			setOutputMarkupId(true);
			
			WebMarkupContainer actions = new WebMarkupContainer("menu-actions-container");
			actions.setOutputMarkupId(true);
			add(actions);
			actions.setVisible(isSelectionEnabled());
			actions.add(getActionsMenu());
			
			WebMarkupContainer menuCon = new WebMarkupContainer("menu-container");
			add(menuCon);
			menuCon.setVisible(isMenu());
			menuCon.add(getMenu());
	
			setBreadcrumb();
			
			add(new WicketEventListener<TreeNodeSelection<ResourceTreeNode>>() {
				@Override
				public void onEvent(TreeNodeSelection<ResourceTreeNode> event) {
					setBreadcrumb();
				}
			});
			
			
			WebMarkupContainer selectAll = new WebMarkupContainer("selectAll-container") {
				public boolean isVisible() {
					return !isReadOnly() && isSelectionEnabled();
				}
			};
			selectAll.setOutputMarkupId(true);
			selectAll.add(new CheckField("selectAll", new PropertyModel<Boolean>(EResourceSystemPanelV3.this, "selectAll")) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					if (isSelectAll())
						unselectAll();
					else
						selectAll();
					refresh(target);
				}
			});
			add(selectAll);
			
			DropDownChoice<Sort> sortchoices = new DropDownChoice<Sort>("sort", new PropertyModel<Sort>(this, "sort"), getSortChoices());
			sortchoices.setChoiceRenderer(new ChoiceRenderer<Sort>() {
				public String getIdValue(Sort value, int index) {
					return value.toString(); 
				};
				public String getDisplayValue(Sort value) {
					return EResourceSystemPanelV3.this.getLabelString(value.toString().toLowerCase());
				};
			});
			sortchoices.add(new AjaxFormComponentUpdatingBehavior("change") {
				protected void onUpdate(AjaxRequestTarget target) {
					refresh(target);
				}
			});
			add(sortchoices);
			
			AjaxLink<Void> b_new_folder;
			AjaxLink<Void> b_tree;
			
			b_new_folder  = new AjaxLink<Void>("new-folder-button") {
				public void onClick(AjaxRequestTarget target) {
					addFolder();			
					refresh(target);
				}
				@Override
				public boolean isEnabled() {
					if (isReadOnly())
						return false;
					return  layout==Layout.TREE;
				}
				@Override
				public boolean isVisible() {
					return !isReadOnly();
				}
			};
			
			b_new_folder.add( new AttributeModifier("title", getLabel("menu.newfolder")));
			add(b_new_folder);
			
			Link<Void> b_upload_folder;
			b_upload_folder  = new Link<Void>("upload-folder-button") {
				public void onClick() {
				}
				@Override
				public boolean isEnabled() {
					return true;
				}
				@Override
				public boolean isVisible() {
					return !isReadOnly();
				}
			};
			b_upload_folder.add(new AttributeModifier("onclick",
				    "var input=document.querySelector('#uppy-dashboard input[webkitdirectory]');" +
				    "if(input){input.click();}" +
				    "return false;"));
			add(b_upload_folder);

			
			b_tree  = new AjaxLink<Void>("tree-view-button") {
				public void onClick(AjaxRequestTarget target) {
					if (layout==Layout.TREE)
						layout=Layout.FLAT;
					else 
						layout=Layout.TREE;
							
					refresh(target);
				}
			};
			
			b_tree.add(new AttributeModifier("class",  new Model<String>() {
				public String getObject() {
					return layout==Layout.TREE ?"selected" : "";
				}
			}));
			
			b_tree.add( new AttributeModifier("title", "Folders and their content / Flat view with all files from all folders"));
			add(b_tree);
		}	
		public void setSort(Sort sort) {
			EResourceSystemPanelV3.this.sort = sort;
		}
		
		public Sort getSort() {
			return EResourceSystemPanelV3.this.sort;
		}
		

		
		@Override
		public void onInitialize() {
			super.onInitialize();
			add(new WicketEventListener<SelectionEvent>() {
				@Override
				public void onEvent(SelectionEvent event) {
					event.getRequestTarget().add(ToolbarFragment.this.get("menu-actions-container"));
					if (!isSelectAll()) {
						((CheckField)ToolbarFragment.this.get("selectAll-container:selectAll")).setValue(false);
						event.getRequestTarget().add(ToolbarFragment.this.get("selectAll-container"));
					}
				}
			});
		}
		
		@Override
		public void onBeforeRender() {
			super.onBeforeRender();
			addOrReplace(new DataViewNavigationToolbar("navigator", getDataView()) {
				public void onUpdate(AjaxRequestTarget target) {
					refresh(target);
				}
			});
		}
		
		public List<Sort> getSortChoices() {
			List<Sort> orders = new ArrayList<Sort>();
			orders.add(Sort.TITLE);
			orders.add(Sort.DATE_ASC);
			orders.add(Sort.DATE_DESC);
			return orders;
		}
		
		protected void setBreadcrumb() {
			MenuBreadCrumbPanel<?> bc = new MenuBreadCrumbPanel<>("breadcrumb") {
				public boolean isVisible() {
					return layout==Layout.TREE;
				}
			};
			bc.addElement(new AjaxBCElement<ResourceNode>(new Model<String>(getField().getLabel()), null) {
				public void onClick(AjaxRequestTarget target) {
					setFolder(null);
					EResourceSystemPanelV3.this.fireScanAll(new TreeNodeSelection<ResourceTreeNode>(target, null));
				}
			});
			if (foldermodel!=null)
			for (IModel<ResourceNode> nodemodel : getPathNodes(foldermodel)) {
				bc.addElement(new AjaxBCElement<ResourceNode>(nodemodel, new Model<String>(nodemodel.getObject().getDisplayName())) {
					public void onClick(AjaxRequestTarget target) {
						setFolder(getModel());
						ResourceTreeNode node = getTreeNode(getModel());
						EResourceSystemPanelV3.this.fireScanAll(new TreeNodeSelection<ResourceTreeNode>(target, new Model<ResourceTreeNode>(node)));
					}
				});
			}
			addOrReplace(bc);
		}
	
		/**
		 * VIEW
		 * ----
		 * Icon
		 * Thumbnail
		 * Jumbo
		 * ---------------------------
		 * SORT
		 * ---- 
		 * Title
		 * Date
		 */
		protected Panel getMenu() {
			try {
				ContextMenuPanel<KbeeEResourceSystemV3> menu = new ContextMenuPanel<KbeeEResourceSystemV3>( getFieldModel() );
				menu.setOutputMarkupId(true);
				
				menu.addItem(id ->
					new AjaxCheckMenuItemPanelV5<KbeeEResourceSystemV3>(id) {
						@Override
						public void onCheckClick(AjaxRequestTarget target) {
							try {
								setViewMode(ViewMode.ICON);
								setPreference("viewmode", String.valueOf(getViewMode().getId()));
								refresh(target);
							} 
							catch (Exception e) {
								setResponsePage(new ApplicationErrorPage<>(e));
								logger.error(e);	
							}
						}
						@Override
						public String getLabel() {
							return getLabelString("icon");
						}
						@Override
						public boolean isEnabled() {
							return getViewMode()!=ViewMode.ICON;
						}
						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							attributes.setEventPropagation(EventPropagation.STOP); 
						}
						@Override
						public boolean isIconVisible() {
							return getViewMode()==ViewMode.ICON;
						}
						@Override 
						public String getWorkingLabel() {
							return getLabelString("working");
						}
					}
				);

				menu.addItem(id ->
					new AjaxCheckMenuItemPanelV5<KbeeEResourceSystemV3>(id) {
						@Override
						public void onCheckClick(AjaxRequestTarget target) {
							try {
								setViewMode(ViewMode.THUMBNAIL);
								setPreference("viewmode", String.valueOf(getViewMode().getId()));
								refresh(target);
							} 
							catch (Exception e) {
								setResponsePage(new ApplicationErrorPage<>(e));
								logger.error(e);	
							}
						}
						@Override
						public String getLabel() {
							return getLabelString("thumbnail");
						}
						@Override
						public boolean isEnabled() {
							return getViewMode()!=ViewMode.THUMBNAIL;
						}
						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							attributes.setEventPropagation(EventPropagation.STOP); 
						}
						@Override
						public boolean isIconVisible() {
							return getViewMode()==ViewMode.THUMBNAIL;
						}
						@Override 
						public String getWorkingLabel() {
							return getLabelString("working");
						}
					}
				);

				menu.addItem(id ->
					new AjaxCheckMenuItemPanelV5<KbeeEResourceSystemV3>(id) {
						@Override
						public void onCheckClick(AjaxRequestTarget target) {
							try {
								setViewMode(ViewMode.THUMBNAIL_JUMBO);
								setPreference("viewmode", String.valueOf(getViewMode().getId()));
								refresh(target);
							} 
							catch (Exception e) {
								setResponsePage(new ApplicationErrorPage<>(e));
								logger.error(e);	
							}
						}
						@Override
						public String getLabel() {
							return getLabelString("jumbo");
						}
						@Override
						public boolean isEnabled() {
							return getViewMode()!=ViewMode.THUMBNAIL_JUMBO;
						}
						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							attributes.setEventPropagation(EventPropagation.STOP); 
						}
						@Override
						public boolean isIconVisible() {
							return getViewMode()==ViewMode.THUMBNAIL_JUMBO;
						}
						@Override 
						public String getWorkingLabel() {
							return getLabelString("working");
						}
					}
				);
				
				menu.addItem(id ->
					new SeparatorMenuItemPanelV5<KbeeEResourceSystemV3>(id) {
						@Override
						public String getCssClass() {
							return "divider";
						}
					}
				);
				
				menu.addItem(id ->
					new AjaxCheckMenuItemPanelV5<KbeeEResourceSystemV3>(id) {
						@Override
						public void onCheckClick(AjaxRequestTarget target) {
							try {
								// DashboardContentWidgetPanel.this.onViewMode(target, "compact");
								// FeedbackHelper.showInfoToast(getLabel());
							} 
							catch (Exception e) {
								setResponsePage(new ApplicationErrorPage<>(e));
								logger.error(e);	
							}
						}
						@Override
						public String getLabel() {
							return getLabelString("sort-title");
						}
						@Override
						public boolean isEnabled() {
 							return getSort()!=Sort.TITLE;
						}
						@Override
						protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
							attributes.setEventPropagation(EventPropagation.STOP); 
						}
						@Override
						public boolean isIconVisible() {
							return getSort()==Sort.TITLE;
						}
						@Override 
						public String getWorkingLabel() {
							return getLabelString("working");
						}
					}
				);
				
				menu.addItem(new MenuItemFactory<KbeeEResourceSystemV3>() {
					@Override
					public AbstractMenuItemPanelV5<KbeeEResourceSystemV3> getItem(String id) {
						return new com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5<KbeeEResourceSystemV3>(id) {
							@Override
							public void onCheckClick(AjaxRequestTarget target) {
								try {
									// DashboardContentWidgetPanel.this.onViewMode(target, "compact");
									// FeedbackHelper.showInfoToast(getLabel());
								} 
								catch (Exception e) {
									setResponsePage(new ApplicationErrorPage<>(e));
									logger.error(e);	
								}
							}
							@Override
							public String getLabel() {
								return getLabelString("sort-date");
							}
							@Override
							public boolean isEnabled() {
								return getSort()!=Sort.DATE_ASC;
							}
							@Override
							protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
								attributes.setEventPropagation(EventPropagation.STOP); 
							}
							@Override
							public boolean isIconVisible() {
								return getSort()==Sort.DATE_ASC;
							}
							@Override 
							public String getWorkingLabel() {
								return getLabelString("working");
							}
						};
					}
				});
				
				return menu;
				
			} 
			catch (Exception e) {
				logger.error(e, getSessionUser().getUserName());
				return new InvisiblePanel("menu");
			}
		}
	}
	
	
	public class ResourcesProvider extends ListDataProvider<IModel<ResourceNode>> {
		public ResourcesProvider() {
			super();
		}
		public List<IModel<ResourceNode>> getData() {
			return layout==Layout.TREE 
				? getFolderResources()
				: getFlatListResources();		
		}
	}
	
	private class ListResourceModel implements IModel<Resource> {
		private IModel<ResourceNode> model;
		public ListResourceModel(IModel<ResourceNode> model) {
			this.model = model;
		}
		public Resource getObject() {
			return model.getObject();
		}
		public void detach() {
			model.detach();
		}
	}
	
	public class ListFragment extends Fragment {
		
		public ListFragment(String id) {
			super(id, "list-fragment", EResourceSystemPanelV3.this);
			
			setOutputMarkupId(true);
			
			add(new AttributeModifier("style", new Model<String>() {
				public String getObject() {
					return getCssStyle();
				}
			}));
			
			WebMarkupContainer upitem = new WebMarkupContainer("up-item") {
				public boolean isVisible() {
					return layout==Layout.TREE && getFolder()!=null;
				}
			};
			
			upitem.add(new AttributeModifier("style", new Model<String>() {
				public String getObject() {
					return isSelectionEnabled() 
						? "padding: 15px 15px 0px 120px;" 
						: "padding: 15px 15px 0px 70px;";		
				}
			}));
			
			upitem.add(new AjaxLink<>("up-link") {
				public void onClick(AjaxRequestTarget target) {
					IModel<ResourceNode> parentNode = ListFragment.this.getParent(getFolder());
					setFolder(parentNode);
					ResourceTreeNode treeNode = parentNode!=null ? getTreeNode(parentNode) : null;
					fireScanAll(new TreeNodeSelection<ResourceTreeNode>(target, new Model<ResourceTreeNode>(treeNode)));
					target.add(ListFragment.this);
				}
			});
			
			WebMarkupContainer view = new WebMarkupContainer("resources-view");
			view.addOrReplace(upitem);
			view.add(new DataView<IModel<ResourceNode>>("resources-list", new ResourcesProvider(), ITEMS_PER_PAGE) {
				protected void populateItem(Item<IModel<ResourceNode>> item) {
					item.add(new ResourceView(new ListResourceModel(item.getModelObject())));
				}
			});
			view.add(new AttributeModifier("class", new Model<String>() {
				public String getObject() {
					return getViewMode().getListCss();
				}
			})); 
			addOrReplace(view);
		}
		
		private String getCssStyle() {
			return layout==Layout.TREE 
				? "float:left; border-left:1px solid #dededf; margin-left: -1px;"	: "float:left;width:100%; border:none;";		
		}
		
		private IModel<ResourceNode> getParent(ResourceNode node) {
			for (IModel<ResourceNode> model : getResources()) {
				if (model.getObject().getResource().equals(node.getFolder())) {
					return model;
				}
			}
			return null;
		}
	}
	
	public class SelectionResourcesProvider extends ListDataProvider<IModel<ResourceNode>> {
		public SelectionResourcesProvider() {
			super();
		}
		public List<IModel<ResourceNode>> getData() {
			return getSelectionFolderResources();
		}
	}
	
	public class SelectionFragment extends Fragment {
		public SelectionFragment(String id) {
			super(id, "selection-fragment", EResourceSystemPanelV3.this);
			setOutputMarkupId(true);
		}
		public void onBeforeRender() {
			super.onBeforeRender();
			WebMarkupContainer upitem = new WebMarkupContainer("up-item") {
				public boolean isVisible() {
					return getSelectionFolder()!=null;
				}
			};
			upitem.add(new AjaxLink<>("up-link") {
				public void onClick(AjaxRequestTarget target) {
					IModel<ResourceNode> parentNode = SelectionFragment.this.getParent(getSelectionFolder());
					setSelectionFolder(parentNode);
					target.add(SelectionFragment.this);
				}
			});
			addOrReplace(upitem);
			addOrReplace(new DataView<IModel<ResourceNode>>("resource", new SelectionResourcesProvider(), ITEMS_PER_PAGE) {
				protected void populateItem(Item<IModel<ResourceNode>> item) {
					item.add(new ResourceView(new ListResourceModel(item.getModelObject())) {
						protected void onSelect(AjaxRequestTarget target, IModel<ResourceNode> nodemodel) {
							setSelectionFolder(nodemodel);
							target.add(SelectionFragment.this);
						}
					});
				}
			});
			addOrReplace(getActionPanel());
		}
		public Component focusComponent() {
			return null;
		}
		private IModel<ResourceNode> getParent(ResourceNode node) {
			for (IModel<ResourceNode> model : getResources()) {
				if (model.getObject().getResource().equals(node.getFolder())) {
					return model;
				}
			}
			return null;
		}
	}
	
	
	public class ActionFragment extends Fragment {
		public ActionFragment(String id) {
			super(id, "action-fragment", EResourceSystemPanelV3.this);
			add(new AjaxLink<Void>("ok-button") {
				public void onClick(AjaxRequestTarget target) {
					refresh(target); 
				}
			});
			add(new AjaxLink<Void>("cancel-button") {
				public void onClick(AjaxRequestTarget target) {
					refresh(target); 
				}
			});
		}	
	}	
	
	/**
	 *
	 */
	public class ControlFragment extends Fragment {
		public ControlFragment(String id) {
			super(id, "control-fragment", EResourceSystemPanelV3.this);
			
			setOutputMarkupId(true);
			
			Label s = new Label("subtitle", new Model<String>(getField().getSublabel()));
			s.setEscapeModelStrings(false);
			s.setVisible(getField().getSublabel()!=null);
			add(s);
			
			add(new ToolbarFragment("toolbar"));
			
			WebMarkupContainer browser = new WebMarkupContainer("browser") {
				public boolean isVisible() {
					return !isSelectionVisible();
				}
			};
			
			browser.setOutputMarkupId(true);
			
			
			browser.add(new TreeFragment("tree-view") {
				public boolean isVisible() {
					return layout==Layout.TREE;
				}
			});
			
			browser.add(new ListFragment("list-view"));
			
			add(browser);
			
			WebMarkupContainer input = new WebMarkupContainer("input");
			input.add(new AttributeModifier("data-destination-id", getContentModel().getObject().getId()));
			//add(input);
	 		
			WebMarkupContainer pickfiles = new WebMarkupContainer("pickfiles") {
				public boolean isVisible() {
					return isEditionEnabled() && !isReadOnly() && EResourceSystemPanelV3.this.isEnabled() && !isQuotaLimit() && !isSelectionVisible();
				}
			};
			pickfiles.add(input);
			add(pickfiles);
//				
//			add(new WebMarkupContainer("quotalimit") {
//				@Override
//				public boolean isVisible() {
//					return isQuotaLimit();
//				}
//			});
			
			add(new WicketEventListener<TreeNodeSelection<ResourceTreeNode>>() {
				@Override
				public void onEvent(TreeNodeSelection<ResourceTreeNode> event) {
					event.getRequestTarget().add(ControlFragment.this.get("toolbar"));
					event.getRequestTarget().add(ControlFragment.this.get("browser:list-view"));
				}
			});
		}
		
		public void refresh(AjaxRequestTarget target) {
			target.add(get("toolbar"));
			target.add(get("browser"));
		}
		
		private boolean isQuotaLimit() {
			// We use local Hard Disk storage for Quota. (External storage does not count)
			long used  = getDomain()!=null?getDomainMetricsServices().getHardDisk(getDomain()):0;
			double dused  = Double.valueOf((double) used / (double) GB).doubleValue();
			if (QUOTA > 0 &&  (DQUOTA < dused)) 
				return true;
			return false;
		}
	}
	
	
	public class ResourceView extends Fragment {
		IModel<Resource> model;
		public ResourceView(IModel<Resource> model) {
			super("resource-view", "resource-view-fragment", EResourceSystemPanelV3.this);
			setModel(model);
			setOutputMarkupId(true);
			add(getSelector());
			add(getView(model));
			add(getMenu(model));
			
			add(new ResourceEditor("editor", model, getContentModel()) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					getData().setData(getField(), getResources());
					updated = true;
					setUpdatedField(new ResourceUpdated(getData().getForm(), EResourceSystemPanelV3.this.getLabel(), getModelObject()));
					target.add(ResourceView.this);
					EResourceSystemPanelV3.this.onUpdate(target);
				}
				@Override
				public void onClose(AjaxRequestTarget target) {
					target.add(ResourceView.this);
				}
			});
			add(new EResourceVersionsPanel("versions", model) {
				protected void onClose(AjaxRequestTarget target) {
					super.onClose(target);
					target.add(ResourceView.this);
				}	
			});
		}
		
		public IModel<Resource> getModel() {
			return model;
		}
		public void setModel(IModel<Resource> model) {
			this.model = model;
		}
		public void setSelected(Boolean value) {
			if (value)
				EResourceSystemPanelV3.this.select(getModel());
			else
				EResourceSystemPanelV3.this.unselect(getModel());
		}
		public Boolean isSelected() {
			return EResourceSystemPanelV3.this.isSelected(getModel());
		}
		protected void edit(AjaxRequestTarget target) {
			((ResourceEditor)get("editor")).edit(target);
			target.add(ResourceView.this);
		}
		protected void showVersions(AjaxRequestTarget target) {
			((EResourceVersionsPanel)get("versions")).open(target);
			target.add(ResourceView.this);
		}
		protected WebMarkupContainer getSelector() {
			WebMarkupContainer selector = new WebMarkupContainer("selector") {
				public boolean isVisible() {
					return isSelectionEnabled();
				}
			};
			selector.add(new CheckField("check", new PropertyModel<Boolean>(this, "selected")) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					setSelected(getValue());
//					if (!getValue()) {
//						target.add(getControl());
//						refresh(target);
//					}
		            fireScanAll(new SelectionEvent(target));
				}
				public boolean isEnabled() {
					if (((ResourceNode)model.getObject()).getResource() instanceof ResourceFolder) {
						ResourceNode node = (ResourceNode)model.getObject();
						for (IModel<ResourceNode> model : selection) {
							if (model.getObject().getResource() instanceof ResourceFolder &&
								isdescendant(node, ((ResourceFolder)model.getObject().getResource())) && 
								!model.getObject().equals(ResourceView.this.model.getObject()))
								return false;
						}
						return true;
					}	
					for (IModel<ResourceNode> selectedmodel : selection) {
						if (selectedmodel.getObject().getResource() instanceof ResourceFolder) {
							ResourceFolder folder = (ResourceFolder)selectedmodel.getObject().getResource();
							ResourceNode node = (ResourceNode)model.getObject();
							if (isdescendant(node, folder)) { 
								return false;
							}
						}	
					}
					return true;
				}
			});
			return selector;
		}
		
		protected void onSelect(AjaxRequestTarget target, IModel<ResourceNode> nodemodel) {
			setFolder(nodemodel);
			ResourceTreeNode node = getTreeNode(nodemodel);
            fireScanAll(new TreeNodeSelection<ResourceTreeNode>(target, new Model<ResourceTreeNode>(node)));
		}
		
		protected Panel getView(IModel<Resource> model) {
			if (((ResourceNode)model.getObject()).getResource() instanceof ResourceFolder) {
				return new FolderViewPanel<Content>("resource-view", model) {
					public ViewMode getViewMode() {
						return EResourceSystemPanelV3.this.getViewMode();
					}
					public void onClick(AjaxRequestTarget target) {
						onSelect(target, getNode(getResource()));
					}
				};
			}
			else {
				Panel view = new ResourceViewPanel<Content>("resource-view", model, getContentModel()) {
					@Override
					public ViewMode getViewMode() {
						return EResourceSystemPanelV3.this.getViewMode();
					}
					@Override
					public boolean showFolders() {
						return layout==Layout.FLAT;
					}
					@Override
					public boolean isShared() {
						return EResourceSystemPanelV3.this.isShared();
					}
				};
				view.add(new DraggableBehavior() {
					protected Component getContainment() {
						return EResourceSystemPanelV3.this.get("container:control:browser");
					}
				});
				view.add(new AttributeModifier("data-id", String.valueOf(((ResourceNode)model.getObject()).getResource().getId())));
				return view;
			}
		}
		
		protected WebMarkupContainer getMenu(IModel<Resource> model) {
			
			WebMarkupContainer container = new WebMarkupContainer("menu-container") {
				public boolean isVisible() {
					return isFile(model) || !isReadOnly();
				}
			};
			
			container.setOutputMarkupId(true);
			
			ContextMenuPanel<Resource> menu = new ContextMenuPanel<Resource>(model);
			
			container.add(menu);
			
			menu.addItem(id ->
				new DonwloadMenuItemPanelV5<Resource>(id) {
					@Override 
					public String getLabel() {
						return getLabelString("menu.download");
					}
					@Override
					protected File getFile() throws IOException {
						ResourceNode node = (ResourceNode)getModel().getObject();
						if (node.getResource() instanceof KBFile) {
							return ((KBFile)node.getResource()).getFile();
						}
						return null;
					}
					@Override
					public boolean isVisible() {
						return isFile(getModel());
					}
				}
			);
			
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<Resource>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						showVersions(target);
					}	
					@Override
					public boolean isVisible() {
						return getModelObject().getVersion()>1 && isFile(getModel());
					}
					@Override
					public String getLabel() {
						return getLabelString("menu.showversions");
					}
			});
			
			menu.addItem(id ->
				new ScriptMenuItemPanel<Resource>(id) {
					@Override
					public String getLabel() {	
						return getLabelString("menu.upload-version");
					}
					@Override
					public String onClickScript() {
						String contentid = String.valueOf(
							getContentModel().getObject().getId());
						String resourceid = String.valueOf(
							getModelObject().getId());
						String destination = contentid + "/" + resourceid;
						String script =
							    "if(window.kbeeUppy){" +
							    "   window.kbeeUppy.setMeta({destinationId:'"+destination+"'});" +
							    "}" +
							    "var input=document.querySelector('#uppy-dashboard input[type=file]:not([webkitdirectory])');" +
							    "if(input){input.click();}" +
							    "return false;";
						return script;	    
					}
					@Override
					public boolean isVisible() {
						if (isReadOnly() || !isEditionEnabled() || !isFile(getModel()))
							return false;
						return true;
					}
			});
			
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<Resource>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						edit(target);
					}	
					@Override
					public String getLabel() {
						return getLabelString("menu.edit");
					}
					@Override
					public boolean isVisible() {
						if (isReadOnly() || !isEditionEnabled())
							return false;
						return true;
					}
			});
			
			menu.addItem(id ->
				new SeparatorMenuItemPanelV5<Resource>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
					@Override
					public boolean isVisible() {
						if (isReadOnly() || !isEditionEnabled() || !isFile(getModel()) || getFolders().isEmpty())
							return false;
						return true;
					}
				}
			);
			
			menu.addItem(id ->
				new AjaxCheckMenuItemPanelV5<Resource>(id) {
					@Override
					public void onCheckClick(AjaxRequestTarget target) {
						try {
							((KbeeResourceNode)getModelObject()).setIndex(!((KbeeResourceNode)getModelObject()).isIndex());
							getData().setData(getField(), getResources());
							updated = true;
							setUpdatedField(new ResourceUpdated(getData().getForm(), EResourceSystemPanelV3.this.getLabel(), getModelObject()));
							refresh(target);
							EResourceSystemPanelV3.this.onUpdate(target);
						} 
						catch (Exception e) {
							setResponsePage(new ApplicationErrorPage<>(e));
							logger.error(e);	
						}
					}
					@Override
					public String getLabel() {
						return getLabelString("menu.setasindex");
					}
					@Override
					public boolean isEnabled() {
						if (isReadOnly() || !isEditionEnabled() || !isFile(getModel()))
							return false;
						return true;
					}
					@Override
					public boolean isVisible() {
						if (isReadOnly() || !isEditionEnabled() || !isFile(getModel()))
							return false;
						return true;
					}
					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						attributes.setEventPropagation(EventPropagation.STOP); 
					}
					@Override
					public boolean isIconVisible() {
						return ((ResourceNode)getModelObject()).isIndex();
					}
					@Override 
					public String getWorkingLabel() {
						return getLabelString("working");
					}
				}
			);
			
			menu.addItem(id ->
				new PrintMenuItemPanel<Resource>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						super.onClick(target);
					}	
					@Override
					public String getLabel() {
						return getLabelString("menu.print");
					}
					@Override
					public boolean isVisible() {
						return super.isVisible();
					}
			});
			
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<Resource>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						IModel<String> ms = EResourceSystemPanelV3.this.getLabel("dialog.delete.message", getModelObject().getTitle());
						getConfirmationDialog().open(target, ms, Dialog.Delete, new Dialog.Handler() {
							@Override
							public void onClick(AjaxRequestTarget target, Button button) {
								if (button.key().equals(Dialog.Delete.key())) {
									try { 
										delete(getModelObject());
										refresh(target);
									} 
									catch (Exception e) {
										logger.error(e);
									}
								}
							}
						});
					}	
					@Override
					public String getLabel() {
						return getLabelString("menu.delete");
					}
					@Override
					public boolean isVisible() {
						if (isReadOnly() || !isEditionEnabled() || !isEmpty(getModel()))
							return false;
						return true;
					}
			});
			
			return container;
		}

	}	

	/*************************************************************************************************************************
	 * 
	 * 
	 * @param id
	 * @param field
	 * @param data
	 * 
	 */
	public EResourceSystemPanelV3(String id, KbeeEResourceSystemV3 field, IModel<EFormData> data) {
		super(id, field, data);
		setOutputMarkupId(true);
		try {
			String viewmode =  getPreference("viewmode", String.valueOf(ViewMode.ICON.getId()));
			setViewMode(ViewMode.of(Integer.valueOf(viewmode).intValue()));
		} 
		catch (Exception e) {
			logger.error(e);
			setViewMode(ViewMode.ICON);
			setPreference("viewmode", String.valueOf(ViewMode.ICON.getId()));
		}
	}
	
	public boolean isMenu() {
		return true;
	}

	public ViewMode getViewMode() {
		return this.viewmode;
	}

	public void setViewMode(ViewMode mode) {
		this.viewmode = mode;
	}
	
	public Disposition getDisposition() {
		return Disposition.VERTICAL;
	}
	
	public void setContent(IModel<Content> model) {
		this.contentmodel = model;
	}
	
	public void setContent(Content content) {
		this.contentmodel = new ObjectModel<Content>(content);
	}
	
	public IModel<Content> getContentModel() {
		return contentmodel;
	}
	
	public List<IModel<ResourceNode>> getResources() {
		return resources;
	}
	
	public List<IModel<ResourceNode>> getSelection() {
		return selection;
	}
	
	public List<IModel<ResourceNode>> getFlatListResources() {
		List<IModel<ResourceNode>> resources = new ArrayList<IModel<ResourceNode>>();

		for (IModel<ResourceNode> model : getResources()) {
			if (model.getObject().getResource() instanceof KBFile) {
				resources.add(model);
			}
		}
		
		Collections.sort(resources, new ResourceComparator());
		
		return resources;
	}
	
	public List<IModel<ResourceNode>> getChilds(ResourceFolder folder) {
		List<IModel<ResourceNode>> resources = new ArrayList<>();
		for (IModel<ResourceNode> model : getResources()) {
			if ((folder==null && model.getObject().getFolder()==null) || (folder!=null && folder.equals(model.getObject().getFolder()))) {
				resources.add(model);
			}
		}
		Collections.sort(resources, new ResourceComparator());
		return resources;
	}
	
	public List<IModel<ResourceNode>> getFolderResources() {
	
		List<IModel<ResourceNode>> folders = new ArrayList<>();
		List<IModel<ResourceNode>> files = new ArrayList<>();
		List<IModel<ResourceNode>> resources = new ArrayList<>();
		
		for (IModel<ResourceNode> model : getResources()) {
			ResourceFolder folder = model.getObject().getFolder();
			if ((getFolder()==null && folder==null) || (folder!=null && getFolder()!=null && folder.equals(getFolder().getResource()))) {
				if (model.getObject().getResource() instanceof KBFile) {
					files.add(model);
				}
				else {
					folders.add(model);
				}
			}
		}
		
		Collections.sort(folders, new ResourceComparator());
		Collections.sort(files, new ResourceComparator());
		
		for (IModel<ResourceNode> model : folders) {
			resources.add(model);
		}
		
		for (IModel<ResourceNode> model : files) {
			resources.add(model);
		}

		return resources;
	}
	
	
	public List<IModel<ResourceNode>> getSelectionFolderResources() {
		
		List<IModel<ResourceNode>> folders = new ArrayList<>();
		List<IModel<ResourceNode>> files = new ArrayList<>();
		List<IModel<ResourceNode>> resources = new ArrayList<>();
		
		boolean folderselected = false;
		for (IModel<ResourceNode> model : getSelection()) {
			if (model.getObject().getResource() instanceof ResourceFolder) {
				folderselected = true;
				break;
			}
		}
		
		for (IModel<ResourceNode> model : getSelection()) {
			ResourceFolder folder = model.getObject().getFolder();
			if ((getSelectionFolder()==null && folder==null) || 
				(folder!=null && getSelectionFolder()!=null && folder.equals(getSelectionFolder().getResource())) ||
				!folderselected) {
				if (model.getObject().getResource() instanceof KBFile) {
					files.add(model);
				}
				else {
					folders.add(model);
				}
			}
		}
		
		Collections.sort(folders, new ResourceComparator());
		Collections.sort(files, new ResourceComparator());
		
		for (IModel<ResourceNode> model : folders) {
			resources.add(model);
		}
		
		for (IModel<ResourceNode> model : files) {
			resources.add(model);
		}

		return resources;
	}
	
	public List<IModel<ResourceNode>> getFolders() {
		List<IModel<ResourceNode>> resources = new ArrayList<IModel<ResourceNode>>();
		for (IModel<ResourceNode> model : getResources()) {
			if (model.getObject().getResource() instanceof ResourceFolder) {
				resources.add(model);
			}
		}
		Collections.sort(resources, new ResourceComparator());
		return resources;
	}
	
	public void setFolder(IModel<ResourceNode> model) {
		this.foldermodel = model;
	}
	
	public void setSelectionFolder(IModel<ResourceNode> model) {
		this.selectionfoldermodel = model;
	}
	
	public ResourceNode getFolder() {
		return this.foldermodel !=null ? this.foldermodel.getObject() : null;
	}
	
	public ResourceNode getSelectionFolder() {
		return this.selectionfoldermodel !=null ? this.selectionfoldermodel.getObject() : null;
	}
	
	public void addFolder() {
		String name = getUniqueName(getLabelString("folder.newname"));
		ResourceFolder folder = ServiceLocator.getService(ContentFactoryService.class).createFolder(name);
		ResourceFolder parent = getFolder()!=null ? (ResourceFolder)getFolder().getResource() : null;
 		this.resources.add(new ResourceNodeModel(new KbeeResourceNode(folder, parent))); 
		fireScanAll(new EAjaxFormEvent(null, getField()));
		setUpdatedField(new ResourceAdded(getData().getForm(), getLabel(), folder));
		getData().setData(getField(), getResources());
		updated = true;
	}
	
	public void deleteAll() {
 		this.resources.clear(); 
		fireScanAll(new EAjaxFormEvent(null, getField()));
		setUpdatedField(new ResourcesRemoved(getData().getForm(), getLabel()));
		getData().setData(getField(), getResources());
		updated = true;
	}
	
	@Override
	public synchronized void add(Resource resource) {
		ResourceFolder folder = getFolder()!=null ? (ResourceFolder)getFolder().getResource() : null;
		if (resource instanceof KBFile && ((KBFile)resource).getLocalPath()!=null && !"".equals(((KBFile)resource).getLocalPath())) {
			ResourceFolder context = folder;
			String path[] = ((KBFile)resource).getLocalPath().toLowerCase().split("/");
			for (int p=0; p<path.length; p++) {
				String pathnode = path[p];
				boolean found = false;
				for (IModel<ResourceNode> model : getChilds(context)) {
					ResourceNode node = model.getObject();
					if (node.getResource() instanceof ResourceFolder &&
							node.getResource().getName().toLowerCase().equals(pathnode)) {
						found = true;
						folder = (ResourceFolder)node.getResource();
						break;
					}
				}
				if (!found) {
					folder = ServiceLocator.getService(ContentFactoryService.class).createFolder(pathnode);
					this.resources.add(new ResourceNodeModel(new KbeeResourceNode(folder, context))); 
					setUpdatedField(new ResourceAdded(getData().getForm(), getLabel(), folder));
					layout=Layout.TREE;
				}
				context = folder;
			}
		}
		this.resources.add(new ResourceNodeModel(new KbeeResourceNode(resource, folder)));
		fireScanAll(new EAjaxFormEvent(null, getField()));
		setUpdatedField(new ResourceAdded(getData().getForm(), getLabel(), resource));
		getData().setData(getField(), getResources());
		updated = true;
	}
	
	@Override
	public void addVersion(Resource resource, Resource version) {
		
		IModel<ResourceNode> node = getNode(resource);
		
		if (node == null) {
			return;
		}
		
		KBFile file = (KBFile)node.getObject().getResource();
		KBFile fileversion = (KBFile)version;
				
		file.getService(FileService.class).setVersion(fileversion);
				
		this.resources.remove(node);
				
		ResourceFolder folder = getFolder()!=null ? (ResourceFolder)getFolder().getResource() : null;
		this.resources.add(new ResourceNodeModel(new KbeeResourceNode(version, folder)));
		fireScanAll(new EAjaxFormEvent(null, getField()));
		getData().setData(getField(), getResources());
		
		updated = true;
	}
	
	public void delete(Resource resource) {
		for (IModel<ResourceNode> model : this.resources) {
			if (model.getObject().equals(resource)) {
				this.resources.remove(model);
				break;
			}
		}
		fireScanAll(new EAjaxFormEvent(null, getField()));
		setUpdatedField(new ResourceRemoved(getData().getForm(), getLabel(), resource));
		getData().setData(getField(), getResources());
		updated = true;
	}
	
	public void move(ResourceNode resource, ResourceFolder folder) {
		((KbeeResourceNode)resource).setFolder(folder);
		fireScanAll(new EAjaxFormEvent(null, getField()));
		setUpdatedField(new ResourceUpdated(getData().getForm(), getLabel(), resource));
		getData().setData(getField(), getResources());
		updated = true;
	}

	
	public String getLabel() {
		return getField().getLabel()!=null ?
			getField().getLabel() :
			"";	
	}
	
	public void updateModel() {
		if (updated) {
			getResources().forEach(model -> model.getObject());
			getData().setData(getField(), getResources());
			updated = false;
		}
	}
	
	public boolean isSelectionVisible() {
		return selectionVisible;
	}

	public void setSelectionVisible(boolean selectionVisible) {
		this.selectionVisible = selectionVisible;
	}
	
	@Override
	public boolean listen(Event event) {
		if (event instanceof FileUploadedEvent &&
			getPage()!=null) {
			String destination = ((FileUploadedEvent)event).getDestination();
			String contentId = String.valueOf(getContentModel().getObject().getId());
			if (destination.equals(contentId)) {
				return true;
			}
			else {
				if (destination.startsWith(contentId+"/")) {
					return true;
				}
				else {
					return false;
				}
			}
		}
		else {
			return false;
		}
	}
	
	@Override
	public void onEvent(Event event) {
		String destination = ((FileUploadedEvent)event).getDestination();
		if (!destination.contains("/")) {
			KBFile file = ((FileUploadedEvent)event).getFile();
			file = (KBFile)getContentDao().reload(file);
			add(file);
		}
		else {
			Resource resource = null;
			String resourceId = destination.split("/")[1];
			for (IModel<ResourceNode> model : this.resources) {
				if (resourceId.equals(String.valueOf(model.getObject().getId()))) {
					resource = model.getObject().getResource();
				}
			}	
			addVersion(resource, ((FileUploadedEvent)event).getFile());
		}
	}

	/***
	 * 
	 * 
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		ServiceLocator.getService(EventService.class).addListener(this);
		
		setResources();
		
		this.layout = getFolders().isEmpty() ? Layout.FLAT : Layout.TREE;
		
		WebMarkupContainer layout = new WebMarkupContainer("horizontal-layout");
		
		WebMarkupContainer menu = new WebMarkupContainer("menu-container");
		menu.setOutputMarkupId(true);
		menu.add(getMenu());
		getContainer().add(menu);

		getContainer().add(new ControlFragment("control"));
		getContainer().add(layout);
		
		getContainer().add(new Label("label", new Model<String>() {
			public String getObject() {
				return getField().getLabel()!=null ?
					getField().getLabel() :
					"";	
			}
		}));
		
		getContainer().get("label").add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				return getDisposition()==null||getDisposition()==Disposition.HORIZONTAL ? "col-lg-1" : "";
			}
		}));
		
		if (getDisposition()==null || getDisposition()==Disposition.HORIZONTAL) {
			layout.add(new AttributeModifier("class", Width.W10.getCss()));
			getContainer().get("control").setVisible(false);
		}
		else {
			layout.setVisible(false);
		}
		add(new UploadTusBehavior() {
			@Override
			public boolean isEnabled() {
				return EResourceSystemPanelV3.this.isEnabled();
			}
			public void bind(Component component) {
				Component editor = getEditor(component);
				if (editor!=null) {
					boolean found = false;
					for (Behavior behavior : editor.getBehaviors()) {
						if (behavior instanceof RefreshBehavior) {
							found = true;
							break;
						}
					}
					if (!found) {
						editor.add(new RefreshBehavior(editor.getMarkupId()));
					}
					else {
						setBehaviorId(editor.getMarkupId());
					}
				}
			}
			@Override
			protected void onUpload(AjaxRequestTarget target, String component) {
				fireScanAll(new EAjaxRefreshEvent(target, component));
			}
		});
		
		add(new WicketEventListener<EAjaxRefreshEvent>() {
			@Override
			public void onEvent(EAjaxRefreshEvent event) {
				if (handle(event)) {
					refresh(event.getRequestTarget());
				}
			}
			public boolean handle(EAjaxRefreshEvent event) {
				return EResourceSystemPanelV3.this.getMarkupId().equals(event.getComponentId());
			}
		});
		
		add(new WicketEventListener<EAjaxFormResourceEvent>() {
			@Override
			public void onEvent(EAjaxFormResourceEvent event) {
				if (event.getTag()!=null && event.getTag().equals(getFieldTag())) {
					addIfNotExist(event.getResource());
				}
				refresh(event.getRequestTarget());
			}
		});
		
		add(new WicketEventListener<EAjaxFormReloadEvent>() {
			@Override
			public void onEvent(EAjaxFormReloadEvent event) {
				if (event.getTag()!=null && event.getTag().equals(getFieldTag())) {
					List<ResourceNode> values = getField().getModel().getValues(getContentModel().getObject());
					List<IModel<ResourceNode>> models = new ArrayList<>();
					for (ResourceNode node : values) {
						models.add(new ResourceNodeModel(node));
					}
					getData().setData(getField(), models);
					setResources();
					refresh(event.getRequestTarget());
				}
			}
		});

		getContainer().add(new ConfirmationDialog("confirmation-dialog"));
	}
	
	public boolean isSelectAll() {
		return selectAll;
	}
	
	public void setSelectAll(boolean selectAll) {
		this.selectAll = selectAll;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		for (IModel<ResourceNode> model : getResources()) {
			model.detach();
		}
		if (contentmodel!=null)
			contentmodel.detach();
	}
	
	protected void refresh(AjaxRequestTarget target) {
		if (isSelectionVisible()) {
			hideSelection(target);
		}

		target.add(get("container:menu-container"));
		getControl().refresh(target);
	}
	
	protected void showSelection(AjaxRequestTarget target) {
		setSelectionVisible(true);
		setSelectionFolder(null);
		target.add(get("container:menu-container"));
		target.add(getControl());
		Component component =  ((SelectionFragment)getSelectionPanel()).focusComponent();
		if (component!=null)
		target.focusComponent(component);
		//getControl().refresh(target);
	}
	
	protected void hideSelection(AjaxRequestTarget target) {
		setSelectionVisible(false);
		target.add(get("container:menu-container"));
		target.add(getControl());
		UploadBehavior behavior = getBehaviors(UploadBehavior.class).get(0);
		target.appendJavaScript(behavior.getScript());
	}
	
	protected boolean isShared() {
		return false;
	}
	
	protected boolean isSelectionEnabled() {
		return false;
	}

	protected Panel getActionsMenu() {
		return new InvisiblePanel("menu");
	}
	
	protected WebMarkupContainer getSelectionPanel() {
		return new SelectionFragment("selection-panel");
	}
	
	protected WebMarkupContainer getActionPanel() {
		return new ActionFragment("action-panel");
	}
	
	protected void setUpdated(boolean value) {
		updated = value;
	}
	
	private boolean isFile(IModel<Resource> model) {
		return ((ResourceNode)model.getObject()).getResource() instanceof KBFile; 
	}
	
	private boolean isSelected(IModel<Resource> model) {
		for (IModel<ResourceNode> selected : selection) {
			if (model.getObject().equals(selected.getObject())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isSelected(ResourceNode node) {
		for (IModel<ResourceNode> selected : selection) {
			if (node.equals(selected.getObject())) {
				return true;
			}
		}
		return false;
	}
	
	private void select(IModel<Resource> model) {
		ResourceFolder folder = null;
		Resource resource = model.getObject();
		if (resource instanceof ResourceNode) 
			resource = ((ResourceNode)resource).getResource();
		if (resource instanceof ResourceFolder)
			folder = (ResourceFolder)resource;
		for (IModel<ResourceNode> m : getResources()) {
			if (m.getObject().getResource().equals(resource)) {
				if (!isSelected(m.getObject()))
				selection.add(m);
			}
			if (folder!=null && isdescendant(m.getObject(), folder)) {
				if (!isSelected(m.getObject()))
				selection.add(m);
			}
		}
	}
	
	private void unselect(IModel<Resource> model) {
		ResourceFolder folder = null;
		Resource resource = model.getObject();
		if (resource instanceof ResourceNode) 
			resource = ((ResourceNode)resource).getResource();
		if (resource instanceof ResourceFolder)
			folder = (ResourceFolder)resource;
		for (IModel<ResourceNode> m : getResources()) {
			if (m.getObject().getResource().equals(resource)) {
				selection.remove(m);
			}
			if (folder!=null && isdescendant(m.getObject(), folder)) {
				selection.remove(m);
			}
		}
		selectAll = false;
	}
	
	private void selectAll() {
		selection.clear();
		for (IModel<ResourceNode> m : getResources()) {
			selection.add(m);
		}
		selectAll = true;
	}
	
	private void unselectAll() {
		selection.clear();
		selectAll = false;
	}
	
	private boolean isdescendant(ResourceNode node, ResourceFolder folder) {
		if (node.getFolder()!=null && node.getFolder().equals(folder)) {
			return true;
		}
		else {
			if (node.getFolder()!=null) {
				for (IModel<ResourceNode> m : getResources()) {
					if (m.getObject().getResource().equals(node.getFolder())) {
						return isdescendant(m.getObject(), folder);
					}
				}
			}
		}
		return false;
	}
	

	
	private void addIfNotExist(Resource resource) {
		boolean found = contains(resource);
		if (!found && resource instanceof Resource && !(resource instanceof ResourceFolder)) {
			updated = true;
			findPage();
			getResources().add(new ResourceNodeModel(new KbeeResourceNode(resource, null)));
			getData().setData(getField(), getResources());
			fireScanAll(new EAjaxFormEvent(null, getField()));
		}
	}
	
	private boolean isEmpty(IModel<Resource> model) {
		Resource resource = ((ResourceNode)model.getObject()).getResource();
		if (resource instanceof KBFile) return true; 
		if (resource instanceof ResourceFolder) {
			for (IModel<ResourceNode> node : getResources()) {
				ResourceFolder folder = node.getObject().getFolder();
				if (folder!=null && folder.equals(resource)) {
					return false;
				}
			}
		}
		return true; 
	}
	
	private boolean contains(Resource resource) {
		boolean found = false;
		for (IModel<ResourceNode> model : getResources()) {
			if (model.getObject().getResource().equals(resource)) {
				found = true;
				break;
			}
		}
		return found;
	}

	
	protected void setResources() {
		List<?> resources = (List<?>)getData().getData(getField());
		setContent(((EFormContentData)getData()).getContent());
		this.resources.clear();
		if (resources!=null) {
			for (Object resource : resources) {
				if (resource instanceof ResourceNode) {
					this.resources.add(new ResourceNodeModel((ResourceNode)resource));
				}
			}
		}
	}
	
	private String getUniqueName(String name) {
		int names = 0;
		String unique = name;
		boolean found = true;
		while (found) {
			found = false;
			for (IModel<ResourceNode> model : getFolderResources()) {
				String nodename = model.getObject().getTitle();
				nodename = nodename.toLowerCase();
				if (nodename.equals(unique.toLowerCase())) {
					names++;
					unique = name + " ("+String.valueOf(names)+")";
					found = true;
					break;
				}
			}
		}	
		return unique;
	}
	
	private IModel<ResourceNode> getResource(String id) {
		for (IModel<ResourceNode> model : getResources()) {
			if (id.equals(String.valueOf(model.getObject().getResource().getId()))) {
				return model;
			}
		}
		return null;
	}
	
	private boolean isFileNode(IModel<ResourceNode> model) {
		return ((ResourceNode)model.getObject()).getResource() instanceof KBFile; 
	}
	
	private ResourceTreeNode getTreeNode(IModel<ResourceNode> model) {
		//String path = getPath(model);
//		ResourceTreeNode node = nodes.get(path);
		ResourceTreeNode node = null;
		if (node == null) {
			node = new ResourceTreeNode(model);
			//nodes.put(path, node);
		}
		return node;
	}
	
//	private String getPath(IModel<ResourceNode> nodemodel) {
//
//		ResourceNode node = nodemodel.getObject();
//		String path = node.getDisplayName();
//		if (node.getFolder()!=null) {
//			path = getPath(getNode(node.getFolder())) + "/"+ path;
//		}
//		return path;
//	}
	
	private IModel<ResourceNode> getNode(Resource resource) {
		for (IModel<ResourceNode> model : getResources()) {
			if (model.getObject().getResource().getId().equals(resource.getId())) {
				return model;
			}
		}
		return null;
	}
	 
	private List<IModel<ResourceNode>> getPathNodes(IModel<ResourceNode> nodemodel) {
		List<IModel<ResourceNode>> path;
		if (nodemodel.getObject().getFolder()!=null) {
			path = getPathNodes(getNode(nodemodel.getObject().getFolder()));
		}
		else {
			path = new ArrayList<>();
		}
		path.add(nodemodel);
		return path;
	}
	
	private Panel getMenu() {
		
		ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					setViewMode(ViewMode.ICON);
					setPreference("viewmode", String.valueOf(ViewMode.ICON.getId()));
					refresh(target);
				}	
				@Override
				public boolean isEnabled() {
					return getViewMode()!= ViewMode.ICON;
				}
				@Override
				public String getLabel() {	
					return getLabelString("icon");
				}
		});
	
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					setViewMode(ViewMode.THUMBNAIL);
					setPreference("viewmode", String.valueOf(ViewMode.THUMBNAIL.getId()));
					refresh(target);
				}	
				@Override
				public boolean isEnabled() {
					return getViewMode()!= ViewMode.THUMBNAIL;
				}
				@Override
				public String getLabel() {	
					return getLabelString("thumbnail");
				}
		});
		
		menu.addItem(id ->
			new SeparatorMenuItemPanelV5<Void>(id) {
				@Override
				public String getCssClass() {
					return "divider";
				}
				@Override
				public boolean isVisible() {
					return true;
				}
			}
		);
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					layout=Layout.TREE;
					refresh(target);
				}	
				@Override
				public boolean isEnabled() {
					return layout!=Layout.TREE;
				}
				@Override
				public String getLabel() {	
					return getLabelString("tree-view");
				}
		});
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					setFolder(null);
					layout=Layout.FLAT;
					refresh(target);
				}	
				@Override
				public boolean isEnabled() {
					return layout!=Layout.FLAT;
				}
				@Override
				public String getLabel() {	
					return getLabelString("flat-view");
				}
		});

		menu.addItem(id ->
			new SeparatorMenuItemPanelV5<Void>(id) {
				@Override
				public String getCssClass() {
					return "divider";
				}
				@Override
				public boolean isVisible() {
					return !isReadOnly();
				}
			}
		);
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					addFolder();
					refresh(target);
				}	
				@Override
				public boolean isEnabled() {
					return layout==Layout.TREE;
				}
				@Override
				public boolean isVisible() {
					return !isReadOnly();
				}
				@Override
				public String getLabel() {	
					return getLabelString("menu.newfolder");
				}
		});
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
				IModel<String> ms = EResourceSystemPanelV3.this.getLabel("dialog.deleteall.message");
				getConfirmationDialog().open(target, ms, Dialog.Delete, new Dialog.Handler() {
					@Override
					public void onClick(AjaxRequestTarget target, Button button) {
						if (button.key().equals(Dialog.Delete.key())) {
							try { 
								deleteAll();
								refresh(target);
							} 
							catch (Exception e) {
								logger.error(e);
							}
						}
					}
				});
				}	
				@Override
				public boolean isEnabled() {
					return !getResources().isEmpty();
				}
				@Override
				public String getLabel() {
					return getLabelString("menu.deleteall");
				}
				@Override
				public boolean isVisible() {
					return !isReadOnly();
				}
		});
		
		return menu;
	}
	
	private EFormEditor getEditor(Component component) {
		Component editor = component.getParent();
		while (editor!=null && !(editor instanceof EFormEditor)) {
			editor = editor.getParent();
		}
		return (EFormEditor)editor;
	}
	
	private DomainMetricsService getDomainMetricsServices() {
		return  ServiceLocator.getService(DomainMetricsService.class);
	}
	
	private ResourceTag getFieldTag() {
		return ((EResourceModel<?>)getField().getModel()).getTag();
	}
	
	private ConfirmationDialog getConfirmationDialog() {
		return (ConfirmationDialog) get("container:confirmation-dialog");
	}
	
	private ControlFragment getControl() {
		return (ControlFragment)this.get("container:control");
	}
	
	private DataView<?> getDataView() {
		 return (DataView<?>)get("container:control:browser:list-view:resources-view:resources-list");
	}
}