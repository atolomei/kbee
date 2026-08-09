package kbee.web.eform;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IFormModelUpdateListener;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

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
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.dom.Proxy;
import com.novamens.kbee.content.form.KbeeEResourceSystem;
import com.novamens.kbee.content.resource.KbeeResourceNode;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.metrics.domain.DomainMetricsService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.UploadMenuItemPanelV5;
import com.novamens.wicket.markup.html.form.DraggableBehavior;
import com.novamens.wicket.markup.html.form.DroppableBehavior;
import com.novamens.wicket.markup.html.form.Field.Width;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.markup.html.repeater.util.DataViewNavigationToolbar;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.error.ApplicationErrorPage;
import kbee.web.resource.FolderViewPanel;
import kbee.web.resource.ResourceModel;
import kbee.web.resource.ResourceNodeModel;
import kbee.web.resource.ResourceViewPanel;
import kbee.web.resource.ResourcesPanel;
import kbee.web.uploader.UploadBehavior;

@SuppressWarnings("serial")
public class EResourceSystemPanel extends EFieldPanel<KbeeEResourceSystem> implements ResourcesPanel, IFormModelUpdateListener {
			
	private static final long serialVersionUID = 1L;
	
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EResourceSystemPanel.class.getName());

	
	private List<IModel<ResourceNode>> resources = new ArrayList<IModel<ResourceNode>>();
	private ViewMode viewmode = ViewMode.ICON;
	private IModel<Content> contentModel;
	private IModel<ResourceFolder> foldermodel;
	private boolean updated = false;
	
	
	final long QUOTA = getDomain()!=null?getDomain().getQuota():0;
	final double DQUOTA = Double.valueOf(QUOTA).doubleValue();
	private static final double GB = 1000000000.0;
	private Sort sort = Sort.TITLE;
	
	static final int ITEMS_PER_PAGE = 30;
	
	private enum Layout {
		TREE,
		FLAT
	} ;
	
	private Layout layout = Layout.FLAT;
	
	private enum Sort {
		TITLE,
		DATE_ASC,
		DATE_DESC
		
	} ;
	

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
	
	public class TreeFragment extends Fragment {
		
		public TreeFragment(String id) {
			super(id, "tree-fragment", EResourceSystemPanel.this);
			
			setOutputMarkupId(true);
			
			add(new ListView<IModel<Resource>>("folders-list", new PropertyModel<List<IModel<Resource>>>(EResourceSystemPanel.this, "folders")) {
				
				protected void populateItem(ListItem<IModel<Resource>> item) {
					
					ResourceNode node = (ResourceNode)item.getModelObject().getObject();
					
					WebMarkupContainer droppable = new WebMarkupContainer("droppable");
					
					droppable.add(new DroppableBehavior() {
						protected void onDrop(AjaxRequestTarget target, String id) {
							IModel<ResourceNode> droppedmodel = getResource(id);
							ResourceNode node = (ResourceNode)item.getModelObject().getObject();
							if (droppedmodel!=null && isFileNode(droppedmodel)) {
								move(droppedmodel.getObject(), (ResourceFolder)node.getResource());
							}
							refresh(target);
						}
					});
					
					AjaxLink<?> titleLink = new AjaxLink<Void>("title-link") {
						@Override
						public void onClick(AjaxRequestTarget target) {
							ResourceNode node = (ResourceNode)item.getModelObject().getObject();
							setFolder((ResourceFolder)node.getResource());
							refresh(target);
						}
					};
					
					if (node.getResource().equals(getFolder())) {	
						titleLink.add(new AttributeModifier("class", "folder btn-link selected"));
					}
					else {
						titleLink.add( new AttributeModifier("class","folder  btn-link"));
					}
					
					droppable.add(titleLink);
					titleLink.add(new Label("resource-title", node.getTitle()));
					titleLink.add(new Label("resource-childs", new Model<String>() {
						public String getObject() {
							ResourceNode node = (ResourceNode)item.getModelObject().getObject();
							return "("+getChilds((ResourceFolder)node.getResource()).size()+")";
						}
					}));
					
					item.add(droppable);
				}
			});
			
		}	
	}
	
	
	/**---------------------------------------------------------------
	 * 
	 * 
	 *
	 */
	public class ToolbarFragment extends Fragment {
		
		public ToolbarFragment(String id) {
			super(id, "toolbar-fragment", EResourceSystemPanel.this);
			
			setOutputMarkupId(true);
			
			
			WebMarkupContainer menuCon = new WebMarkupContainer("menu-container");
			add(menuCon);
			menuCon.setVisible(isMenu());
			menuCon.add(getMenu());

			
			AjaxLink<Void> rootlink = new AjaxLink<Void>("root-link") {
				public void onClick(AjaxRequestTarget target) {
					setFolder(null);
					refresh(target);
				}
			};
			 
			rootlink.add(new Label("label", getField().getLabel()));
			
			add(rootlink);
			
			add(new WebMarkupContainer("sep") {
				public boolean isVisible() {
					return getFolder()!=null;
				}
			});
			
			IModel<String> foldermodel = new Model<String>() {
				public String getObject() {
					return getFolder()!=null ? getFolder().getTitle() : "";
				}
			};
			
			add(new Label("folder", foldermodel) {
				public boolean isVisible() {
					return getFolder()!=null;
				}
			});
			
			DropDownChoice<Sort> sortchoices = new DropDownChoice<Sort>("sort", new PropertyModel<Sort>(this, "sort"), getSortChoices());
			sortchoices.setChoiceRenderer(new ChoiceRenderer<Sort>() {
				public String getIdValue(Sort value, int index) {
					return value.toString(); 
				};
				public String getDisplayValue(Sort value) {
					return EResourceSystemPanel.this.getLabelString(value.toString().toLowerCase());
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

			//AjaxLink<Void> b_view_icon;
			/**
			 * AjaxLink<Void> b_view_condensed;
			b_view_condensed = new AjaxLink<Void>("condensed-button") {
				public void onClick(AjaxRequestTarget target) {
								
					refresh(target);
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
			add(b_view_condensed);
			**/
			
			
			
			b_new_folder  = new AjaxLink<Void>("new-folder-button") {
				public void onClick(AjaxRequestTarget target) {
					addFolder();			
					refresh(target);
				}
				
				@Override
				public boolean isEnabled() {

					if (isReadOnly())
						return false;
					
					return  (getFolder()==null && layout==Layout.TREE);
				}
				@Override
				public boolean isVisible() {
					return !isReadOnly();
				}

			};

			
			b_new_folder.add( new AttributeModifier("title", EResourceSystemPanel.this.getLabel("menu.newfolder")));
			add(b_new_folder);
			
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

			/**
			b_view_icon  = new AjaxLink<Void>("icon-view-button") {
				public void onClick(AjaxRequestTarget target) {
					if (viewmode == ViewMode.ICON)
						viewmode = ViewMode.THUMBNAIL;
					else	
						viewmode = ViewMode.ICON;
					setPreference("viewmode", String.valueOf(viewmode.getId()));
					refresh(target);
				}
			};
			
			b_view_icon.add( new AttributeModifier("title", "Condensed / Icons / Thumbnails"));
			
			b_view_icon.add(new AttributeModifier("class",  new Model<String>() {
				public String getObject() {
					return viewmode == ViewMode.THUMBNAIL ? "selected" : "";
				}
			}));
			add(b_view_icon);
			**/
			
		}	
		
		public void setSort(Sort sort) {
			EResourceSystemPanel.this.sort = sort;
		}
		
		public Sort getSort() {
			return EResourceSystemPanel.this.sort;
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
	
	
	
	
		/**
		 * 
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
		 * 
		 * 
		 * 
		 * 
		 */
		protected Panel getMenu() {

			try {
				ContextMenuPanel<KbeeEResourceSystem> menu = new ContextMenuPanel<KbeeEResourceSystem>( getFieldModel() );
				menu.setOutputMarkupId(true);
				
				
				
				menu.addItem(new MenuItemFactory<KbeeEResourceSystem>() {
					private static final long serialVersionUID = 1L;
					@Override
					public AbstractMenuItemPanelV5<KbeeEResourceSystem> getItem(String id) {
						return new com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5<KbeeEResourceSystem>(id) {
							private static final long serialVersionUID = 1L;
							@Override
							public void onCheckClick(AjaxRequestTarget target) {
								try {
									setViewMode(ViewMode.ICON);
									setPreference("viewmode", String.valueOf(getViewMode().getId()));
									refresh(target);
									// FeedbackHelper.showInfoToast(getLabel());
								} 
								catch (Exception e) {
									setResponsePage(new ApplicationErrorPage<>(e));
									logger.error(e);	
								}
								
							}
							@Override
							public String getLabel() {
								return new StringResourceModel("icon", this, null).getObject();
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
							public boolean isVisible() {
									return true;
							}
							
							@Override
							public boolean isIconVisible() {
								return getViewMode()==ViewMode.ICON;
							}
							@Override 
							public String getWorkingLabel() {
								return new StringResourceModel("working", EResourceSystemPanel.this, null).getString();
							}
						};
					}
				});


				menu.addItem(new MenuItemFactory<KbeeEResourceSystem>() {
					private static final long serialVersionUID = 1L;
					@Override
					public AbstractMenuItemPanelV5<KbeeEResourceSystem> getItem(String id) {
						return new com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5<KbeeEResourceSystem>(id) {
							private static final long serialVersionUID = 1L;
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
								return new StringResourceModel("thumbnail", this, null).getObject();
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
							public boolean isVisible() {
									return true;
							}
							
							@Override
							public boolean isIconVisible() {
								return getViewMode()==ViewMode.THUMBNAIL;
							}
							@Override 
							public String getWorkingLabel() {
								return new StringResourceModel("working", EResourceSystemPanel.this, null).getString();
							}
						};
					}
				});


				menu.addItem(new MenuItemFactory<KbeeEResourceSystem>() {
					private static final long serialVersionUID = 1L;
					@Override
					public AbstractMenuItemPanelV5<KbeeEResourceSystem> getItem(String id) {
						return new com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5<KbeeEResourceSystem>(id) {
							private static final long serialVersionUID = 1L;
							@Override
							public void onCheckClick(AjaxRequestTarget target) {
								try {
									setViewMode(ViewMode.THUMBNAIL_JUMBO);
									setPreference("viewmode", String.valueOf(getViewMode().getId()));
									refresh(target);
									// FeedbackHelper.showInfoToast(getLabel());
								} 
								catch (Exception e) {
									setResponsePage(new ApplicationErrorPage<>(e));
									logger.error(e);	
								}
								
							}
							@Override
							public String getLabel() {
								return new StringResourceModel("jumbo", this, null).getObject();
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
							public boolean isVisible() {
									return true;
							}
							
							@Override
							public boolean isIconVisible() {
								return getViewMode()==ViewMode.THUMBNAIL_JUMBO;
							}
							@Override 
							public String getWorkingLabel() {
								return new StringResourceModel("working", EResourceSystemPanel.this, null).getString();
							}
						};
					}
				});

				
				
				
				menu.addItem(new MenuItemFactory<KbeeEResourceSystem>() {
					private static final long serialVersionUID = 1L;
					@Override
					public AbstractMenuItemPanelV5<KbeeEResourceSystem> getItem(String id) {
						return new SeparatorMenuItemPanelV5<KbeeEResourceSystem>(id) {
							private static final long serialVersionUID = 1L;
							@Override
							public String getCssClass() {
								return "divider";
							}
							@Override
							public boolean isVisible() {
								return  true;
							}
						};
					}
				});
				
				
				menu.addItem(new MenuItemFactory<KbeeEResourceSystem>() {
					private static final long serialVersionUID = 1L;
					@Override
					public AbstractMenuItemPanelV5<KbeeEResourceSystem> getItem(String id) {
						return new com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5<KbeeEResourceSystem>(id) {
							private static final long serialVersionUID = 1L;
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
								return new StringResourceModel("sort-title", this, null).getObject();
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
							public boolean isVisible() {
									return true;
							}
							
							@Override
							public boolean isIconVisible() {
								return getSort()==Sort.TITLE;
							}
							@Override 
							public String getWorkingLabel() {
								return new StringResourceModel("working", EResourceSystemPanel.this, null).getString();
							}
						};
					}
				});
				
				
				
				menu.addItem(new MenuItemFactory<KbeeEResourceSystem>() {
					private static final long serialVersionUID = 1L;
					@Override
					public AbstractMenuItemPanelV5<KbeeEResourceSystem> getItem(String id) {
						return new com.novamens.wicket.markup.html.actions.AjaxCheckMenuItemPanelV5<KbeeEResourceSystem>(id) {
							private static final long serialVersionUID = 1L;
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
								return new StringResourceModel("sort-date", this, null).getObject();
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
							public boolean isVisible() {
									return true;
							}
							
							@Override
							public boolean isIconVisible() {
								return getSort()==Sort.DATE_ASC;
							}
							@Override 
							public String getWorkingLabel() {
								return new StringResourceModel("working", EResourceSystemPanel.this, null).getString();
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
	
	public class ListFragment extends Fragment {
		
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
		
		public ListFragment(String id) {
			super(id, "list-fragment", EResourceSystemPanel.this);
			
			setOutputMarkupId(true);
			
			add(new AttributeModifier("style", new Model<String>() {
				public String getObject() {
					return getCssStyle();
				}
			}));
			
			WebMarkupContainer view = new WebMarkupContainer("resources-view");
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
				? "float:left; border-left:1px solid #dededf; margin-left: -1px;"
				: "float:left;width:100%; border:none;";		
		}
	}	
	
	/**
	 *
	 */
	public class ControlFragment extends Fragment {
		public ControlFragment(String id) {
			super(id, "control-fragment", EResourceSystemPanel.this);
			
			setOutputMarkupId(true);
			
			Label s=new Label("subtitle", new Model<String>(getField().getSublabel()));
			s.setEscapeModelStrings(false);
			s.setVisible(getField().getSublabel()!=null);
			add(s);
			
			add(new ToolbarFragment("toolbar"));
			
			WebMarkupContainer browser = new WebMarkupContainer("browser");
			
			browser.setOutputMarkupId(true);
			
			browser.add(new TreeFragment("tree-view") {
				public boolean isVisible() {
					return layout==Layout.TREE;
				}
			});
			
			browser.add(new ListFragment("list-view"));
			
			add(browser);
	 		
			WebMarkupContainer pickfiles = new WebMarkupContainer("pickfiles") {
				public boolean isVisible() {
					return isEditionEnabled() && !isReadOnly() && EResourceSystemPanel.this.isEnabled() && !isQuotaLimit();
				}
			};
			add(pickfiles);
			
//			WebMarkupContainer menu = new WebMarkupContainer("menu-container");
//			menu.setOutputMarkupId(true);
//			menu.add(getMenu());
//			add(menu);
				
			add(new WebMarkupContainer("quotalimit") {
				@Override
				public boolean isVisible() {
					return isQuotaLimit();
				}
			});
		}
		
		public void refresh(AjaxRequestTarget target) {
			target.add(get("toolbar"));
			target.add(get("browser"));
			//target.add(get("menu-container"));
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
		public ResourceView(IModel<Resource> model) {
			super("resource-view", "resource-view-fragment", EResourceSystemPanel.this);
			setOutputMarkupId(true);
			add(getView(model));
			add(getMenu(model));
			add(new ResourceEditor("editor", model, getContentModel()) {
				@Override
				public void onUpdate(AjaxRequestTarget target) {
					getData().setData(getField(), getResources());
					updated = true;
					EResourceSystemPanel.this.setUpdatedField(new ResourceUpdated(getData().getForm(), EResourceSystemPanel.this.getLabel(), getModelObject()));
					target.add(ResourceView.this);
				}
				@Override
				public void onClose(AjaxRequestTarget target) {
					target.add(ResourceView.this);
				}
			});
			add(new EResourceVersionsPanel("versions", model));
		}
		@Override
		public void onBeforeRender() {
			super.onBeforeRender();
		}
		protected void edit(AjaxRequestTarget target) {
			((ResourceEditor)get("editor")).edit(target);
			target.add(ResourceView.this);
		}
		protected void showVersions(AjaxRequestTarget target) {
			((EResourceVersionsPanel)get("versions")).open(target);
			target.add(ResourceView.this);
		}
		protected Panel getView(IModel<Resource> model) {
			if (((ResourceNode)model.getObject()).getResource() instanceof ResourceFolder) {
				return new FolderViewPanel<Content>("resource-view", model) {
					public ViewMode getViewMode() {
						return EResourceSystemPanel.this.getViewMode();
					}
					public void onClick(AjaxRequestTarget target) {
						ResourceNode node = (ResourceNode)getResource();
						setFolder((ResourceFolder)node.getResource());
						refresh(target);
					}
				};
			}
			else {
				Panel view = new ResourceViewPanel<Content>("resource-view", model, getContentModel()) {
					@Override
					public ViewMode getViewMode() {
						return EResourceSystemPanel.this.getViewMode();
					}
					@Override
					public boolean showFolders() {
						return layout==Layout.FLAT;
					}
					@Override
					public boolean isShared() {
						return EResourceSystemPanel.this.isShared();
					}
				};
				view.add(new DraggableBehavior() {
					protected Component getContainment() {
						return EResourceSystemPanel.this.get("container:control:browser");
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
				new UploadMenuItemPanelV5<Resource>(id) {
					@Override
					public String getLabel() {	
						return getLabelString("menu.upload-version");
					}
					@Override
					protected String getUploadUrl() {
						Content content = getContentModel().getObject();
						String resourceid = String.valueOf(getModelObject().getId());
						String classname = Proxy.getClassName(content).toLowerCase();
						return "/versionupload?id="+ content.getId() +
							"&class="+classname+
							"&resource="+resourceid+
							"&path="+EResourceSystemPanel.this.getPath();
					}	
					@Override
					protected String getRefreshFunction() {
						return "refreshfiles('"+EResourceSystemPanel.this.getMarkupId()+"');";
					}
					@Override
					protected Component getResourcesView() {
						return EResourceSystemPanel.this.get("container:control:browser:list-view:resources-view");
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

			
			if (isFile(model) && !isReadOnly()) {
				menu.addItem(id ->
					new AjaxMenuItemPanelV5<Resource>(id) {
						@Override
						public void onClick(AjaxRequestTarget target) {
							move((ResourceNode)getModelObject(), null);
							refresh(target);
						}	
						@Override
						public String getLabel() {
							return getLabelString("menu.move", getField().getLabel());
						}
						@Override
						public boolean isVisible() {
							return true;
						}
					});
				for (IModel<ResourceNode> foldermodel : getFolders()) {
					menu.addItem(id ->
						new AjaxMenuItemPanelV5<Resource>(id) {
							@Override
							public void onClick(AjaxRequestTarget target) {
								move((ResourceNode)getModelObject(), (ResourceFolder)foldermodel.getObject().getResource());
								refresh(target);
							}	
							@Override
							public String getLabel() {
								return getLabelString("menu.move", foldermodel.getObject().getTitle());
							}
							@Override
							public boolean isVisible() {
								return true;
							}
						});
				}
			}
			
			menu.addItem(id ->
				new SeparatorMenuItemPanelV5<Resource>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
					@Override
					public boolean isVisible() {
						if (isReadOnly() || !isEditionEnabled())
							return false;
						return true;
					}
				}
			);
			
			menu.addItem(id ->
				new AjaxMenuItemPanelV5<Resource>(id) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						IModel<String> ms = EResourceSystemPanel.this.getLabel("dialog.delete.message", getModelObject().getTitle());
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
	public EResourceSystemPanel(String id, KbeeEResourceSystem field, IModel<EFormData> data) {
		super(id, field, data);
		setOutputMarkupId(true);
		try {
			String viewmode =  getPreference("viewmode", String.valueOf(ViewMode.ICON.getId()));
			setViewMode(ViewMode.of(Integer.valueOf(viewmode).intValue()));
		} catch (Exception e) {
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
		this.contentModel = model;
	}
	
	public void setContent(Content content) {
		this.contentModel = new ObjectModel<Content>(content);
	}
	
	public IModel<Content> getContentModel() {
		return contentModel;
	}
	
	public List<IModel<ResourceNode>> getResources() {
		return resources;
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
		List<IModel<ResourceNode>> resources = new ArrayList<IModel<ResourceNode>>();

		for (IModel<ResourceNode> model : getResources()) {
			if (folder.equals(model.getObject().getFolder())) {
				resources.add(model);
			}
		}
		
		Collections.sort(resources, new ResourceComparator());
		
		return resources;
	}
	
	public List<IModel<ResourceNode>> getFolderResources() {
		List<IModel<ResourceNode>> folders = new ArrayList<IModel<ResourceNode>>();
		List<IModel<ResourceNode>> files = new ArrayList<IModel<ResourceNode>>();
		List<IModel<ResourceNode>> resources = new ArrayList<IModel<ResourceNode>>();
		
		for (IModel<ResourceNode> model : getResources()) {
			ResourceFolder folder = model.getObject().getFolder();
			if ((getFolder()==null && folder==null) || (folder!=null && folder.equals(getFolder()))) {
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
	
	public void setFolder(ResourceFolder folder) {
		this.foldermodel = folder!=null ? new ObjectModel<ResourceFolder>(folder) : null;
	}
	
	public ResourceFolder getFolder() {
		return this.foldermodel !=null ? this.foldermodel.getObject() : null;
	}
	
	public void addFolder() {
		String name = getUniqueName(getLabel("folder.newname").getObject());
		ResourceFolder folder = ServiceLocator.getService(ContentFactoryService.class).createFolder(name);
		this.resources.add(new ResourceNodeModel(new KbeeResourceNode(folder, null))); 
		fireScanAll(new EAjaxFormEvent(null, getField()));
		setUpdatedField(new ResourceAdded(getData().getForm(), getLabel(), folder));
		getData().setData(getField(), getResources());
		updated = true;
	}
	
	@Override
	public void add(Resource resource) {
		ResourceFolder folder = getFolder();
		if (folder==null && resource instanceof KBFile && ((KBFile)resource).getLocalPath()!=null && !"".equals(((KBFile)resource).getLocalPath())) {
			boolean found = false;
			String path = ((KBFile)resource).getLocalPath().toLowerCase();
			for (IModel<ResourceNode> model : getFolders()) {
				Resource node = model.getObject().getResource();
				if (node instanceof ResourceFolder && path.equals(node.getTitle().toLowerCase()) ) {
					found = true;
					folder = (ResourceFolder)node;
					break;
				}
			}
			if (!found) {
				folder = ServiceLocator.getService(ContentFactoryService.class).createFolder(((KBFile)resource).getLocalPath());
				this.resources.add(new ResourceNodeModel(new KbeeResourceNode(folder, null))); 
				setUpdatedField(new ResourceAdded(getData().getForm(), getLabel(), folder));
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
		for (IModel<ResourceNode> model : this.resources) {
			if (model.getObject().equals(resource)) {
				IModel<Resource> versionmodel = new ResourceModel(version);
				versionmodel.detach();
				model.detach();
				break;
			}
		}
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

	
	/***
	 * 
	 * 
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setResources();
		
		WebMarkupContainer layout = new WebMarkupContainer("horizontal-layout");
		
		
		WebMarkupContainer menu = new WebMarkupContainer("menu-container");
		menu.setOutputMarkupId(true);
		menu.add(getMenu());
		getContainer().add(menu);
		
		layout.add(new ControlFragment("control"));
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
		add(new UploadBehavior() {
			@Override
			public boolean isEnabled() {
				return EResourceSystemPanel.this.isEnabled();
			}
			@Override
			protected String getUrl() {
				return "/formupload?path="+EResourceSystemPanel.this.getPath();
			}
			@Override
			protected String getDropElement() {
				return getContainer().getMarkupId();
			}
			@Override
			public Component getResourcesPanel() {
				return EResourceSystemPanel.this.get("container:control:browser:list-view:resources-view");
			}
			@Override
			public void bind(Component component) {
				Component parent = component.getParent();
				while (parent!=null && !(parent instanceof EFormEditor)) {
					parent = parent.getParent();
				}
				if (parent!=null) {
					boolean found = false;
					for (Behavior behavior : parent.getBehaviors()) {
						if (behavior instanceof RefreshBehavior) {
							found = true;
							break;
						}
					}
					if (!found) {
						parent.add(new RefreshBehavior(parent.getMarkupId()));
					}
					else {
						setBehaviorId(parent.getMarkupId());
					}
				}
			}
			@Override
			protected void onUpload(AjaxRequestTarget target, String component) {
				fireScanAll(new EAjaxRefreshEvent(target, component));
			}
			@Override
			protected String getBrowseButton() {
				return EResourceSystemPanel.this.get("container:control:pickfiles").getMarkupId();
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
				return EResourceSystemPanel.this.getMarkupId().equals(event.getComponentId());
			}
		});
		
		add(new WicketEventListener<EAjaxFormResourceAddedEvent>() {
			@Override
			public void onEvent(EAjaxFormResourceAddedEvent event) {
				if (event.getTag().equals(getFieldTag())) {
					if (!contains(event.getResource())) {
						((EAjaxFormResourceAddedEvent)event).getEditor().setUpdatedField(new ResourceAdded(getData().getForm(), getLabel(), event.getResource()));
					}
					addIfNotExist(event.getResource());
				}
				refresh(event.getRequestTarget());
			}
		});
		
		getContainer().add(new ConfirmationDialog("confirmation-dialog"));
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		for (IModel<ResourceNode> model : getResources()) {
			model.detach();
		}
		if (contentModel!=null)
			contentModel.detach();
	}
	
	protected void refresh(AjaxRequestTarget target) {
		target.add(get("container:menu-container"));
		getControl().refresh(target);
	}
	
	protected ConfirmationDialog getConfirmationDialog() {
		return (ConfirmationDialog) get("container:confirmation-dialog");
	}
	
	protected boolean isShared() {
		return false;
	}
	
	private IModel<ResourceNode> getResource(String id) {
		for (IModel<ResourceNode> model : getResources()) {
			if (id.equals(String.valueOf(model.getObject().getResource().getId()))) {
				return model;
			}
		}
		return null;
	}
	
	private boolean isFile(IModel<Resource> model) {
		return ((ResourceNode)model.getObject()).getResource() instanceof KBFile; 
	}
	
	private boolean isFileNode(IModel<ResourceNode> model) {
		return ((ResourceNode)model.getObject()).getResource() instanceof KBFile; 
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
	
	private ControlFragment getControl() {
		return (ControlFragment)this.get("container:control");
	}
	
	private void setResources() {
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
					return EResourceSystemPanel.this.getLabel("icon").getObject();
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
					return EResourceSystemPanel.this.getLabel("thumbnail").getObject();
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
					return EResourceSystemPanel.this.getLabel("tree-view").getObject();
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
					return EResourceSystemPanel.this.getLabel("flat-view").getObject();
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
					return getFolder()==null && layout==Layout.TREE;
				}
				@Override
				public boolean isVisible() {
					return !isReadOnly();
				}
				@Override
				public String getLabel() {	
					return EResourceSystemPanel.this.getLabel("menu.newfolder").getObject();
				}
		});
		
		return menu;
	}
	
	private DomainMetricsService getDomainMetricsServices() {
		return  ServiceLocator.getService(DomainMetricsService.class);
	}
	
	private ResourceTag getFieldTag() {
		ResourceTag fieldtag = ((EResourceModel<?>)getField().getModel()).getTag();
		return fieldtag;
	}
	
	
	private DataView<?> getDataView() {
		 DataView<?> view = (DataView<?>)get("container:control:browser:list-view:resources-view:resources-list");
		 return view;
		 
	}

	

	
	

	
	
	
	

	
}