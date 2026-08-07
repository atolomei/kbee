package com.novamens.content.web.treefile.markup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.document.TreeFile;
import com.novamens.content.document.TreeFileKBFile;
import com.novamens.content.user.UserService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.kbee.wicket.markup.html.behaviour.AjustableHeightBehavior;
import com.novamens.kbee.wicket.markup.html.console.grid.GlyphiconColumnPanel;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.repeater.util.NavigationToolbar2;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.FSUtils;
import kbee.web.resource.WebResourceReference;

@SuppressWarnings("serial")
public class TreeFileExplorer extends ModelPanel<TreeFile>  {
	private static final long serialVersionUID = 1L;
	
	private IModel<TreeFile> rootmodel;
	private IModel<TreeFile> treemodel = null;
	private List<TreeFile> nodes;
	
	public class TreeProvider extends SortableDataProvider<TreeFile, String> {
		public Iterator<TreeFile> iterator(long first, long count) {
			ArrayList<TreeFile> iteration = new ArrayList<TreeFile>();
			Iterator<TreeFile> iterator = getNodes().listIterator((int)first);
			int i = 0;
			while (i++<count) {
				iteration.add(iterator.next());
			}
			return iteration.iterator();
		}	
		public IModel<TreeFile> model(TreeFile object) {
			return new ObjectModel<TreeFile>(object);
		}
		public long size() {
			return getNodes().size();
		}
	}
	
	public class FileNameFragment extends Fragment {
		private IModel<TreeFile> model;
		public FileNameFragment(String id, TreeFile file) {
			super(id, "filename-fragment", TreeFileExplorer.this);
			this.model = new ObjectModel<TreeFile>(file);
			AjaxLink<Void> filenamelink = new AjaxLink<Void>("file-link") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					if (model.getObject().isDirectory()) {
						nodes = null;
						if (model.getObject().getId().equals(rootmodel.getObject().getId())) {
							treemodel = null;
						}
						else {
							treemodel = model;
						}
						target.add(TreeFileExplorer.this);
					}
				}
			};
			if (!file.isDirectory()) {
				ResourceReference resourceReference = new WebResourceReference(model.getObject());
				String resourcehref = RequestCycle.get().urlFor(resourceReference, null).toString();
				filenamelink.add(new AttributeModifier("href", resourcehref));
				filenamelink.add(new AttributeModifier("target", "_blank"));
			}
			String name = file.getName();
			if (treemodel!=null && file.getId().equals(treemodel.getObject().getParent().getId())) {
				if (name==null) name = "Tree File";
				name = "..["+name+"]";
			}
			filenamelink.add(new Label("file-name", name));
			add(filenamelink);
		};
		public void onDetach() {
			super.onDetach();
			this.model.detach();
		}
	};
	
	public class BreadCrumbFragment extends Fragment {
		public BreadCrumbFragment(String id) {
			super(id, "breadcrumb-fragment", TreeFileExplorer.this);
			add(new ListView<TreeFile>("file", new PropertyModel<List<TreeFile>>(TreeFileExplorer.this, "breadCrumb")) {
				public void populateItem(ListItem<TreeFile> item) {
					String filename = item.getModelObject().getName() == null ? "Tree File" : item.getModelObject().getName(); 
					item.add(new Label("file-name", filename));
				}
			});
		}
	};	
	
	public class MenuFragment extends Fragment {
		private IModel<TreeFile> model;
		public MenuFragment(String id, TreeFile file) {
			super(id, "menu-fragment", TreeFileExplorer.this);
			this.model = new ObjectModel<TreeFile>(file);
			Panel menuPanel = getMenu();
			WebMarkupContainer menulink = new WebMarkupContainer("menulink") {
				public boolean isVisible() {
					return true;
				}
			};
			add(menulink);
			add(menuPanel);
		}
		@Override
		public boolean isVisible() {
			try {
				return getFile().isBinaryFile();
			}
			catch (IOException e) {
				return false;
			}
		}
		public IModel<TreeFile> getModel() {
			return model;
		}
		public TreeFile getFile() {
			return getModel().getObject();
		}
		@Override
		public void onDetach() {
			super.onDetach();
			getModel().detach();
		}
		protected Panel getMenu() {
			
			ContextMenuPanel<TreeFile> menu = new ContextMenuPanel<TreeFile>(getModel());
			
			menu.addItem(new MenuItemFactory<TreeFile>() {
				@Override
				public AbstractMenuItemPanelV5<TreeFile> getItem(String id) {
					return new AjaxMenuItemPanelV5<TreeFile>(id) {
						@Override
						public void onClick(AjaxRequestTarget target) {
							
						}
						@Override
						public String getLabel() {	
							return "Set as Index";
						}
						@Override
						public boolean isVisible() {
							return !getModelObject().isAccessPoint();
						}
						@Override
						public boolean isEnabled() {
							return false;
						}
					};
				}
			});
			
			menu.addItem(new MenuItemFactory<TreeFile>() {
				@Override
				public AbstractMenuItemPanelV5<TreeFile> getItem(String id) {
					return new AjaxMenuItemPanelV5<TreeFile>(id) {
						@Override
						public void onClick(AjaxRequestTarget target) {
							
						}
						@Override
						public String getLabel() {	
							return "Remove as Index";
						}
						@Override
						public boolean isVisible() {
							return getModelObject().isAccessPoint();
						}
						@Override
						public boolean isEnabled() {
							return false;
						}
					};
				}
			});
			
			menu.addItem(new MenuItemFactory<TreeFile>() {
				@Override
				public AbstractMenuItemPanelV5<TreeFile> getItem(String id) {
					return new AjaxMenuItemPanelV5<TreeFile>(id) {
						@Override
						public void onClick(AjaxRequestTarget target) {
							
						}
						@Override
						public String getLabel() {	
							return "Delete";
						}
						@Override
						public boolean isVisible() {
							return !getModelObject().isAccessPoint();
						}
						@Override
						public boolean isEnabled() {
							return false;
						}
					};
				}
			});

			return menu;
		}
	};	

	public TreeFileExplorer(String id, IModel<TreeFile> model) {
		super(id);
		setOutputMarkupId(true);
		rootmodel = model;
	}
	
	public List<TreeFile> getNodes() {
		if (this.nodes!=null)
			return nodes;
		
		nodes = new ArrayList<TreeFile>();
		
		TreeFile root = treemodel!=null ? treemodel.getObject() : rootmodel.getObject();
		
		if (treemodel!=null) {
			nodes.add(treemodel.getObject().getParent());
		}
		
		for (TreeFile child : root.getChildren()) {
			nodes.add(child);
		}
		
		Collections.sort(nodes, new Comparator<TreeFile>() {
			@Override
			public int compare(TreeFile a, TreeFile b) {
				try {
					String na = a.getName().toLowerCase();
					String nb = b.getName().toLowerCase();
					return na.compareTo(nb);
				} 
				catch (Exception e) {
					return 0;
				}
			}
		}); 
		
		return nodes;
	}
	
	public List<TreeFile> getBreadCrumb() {
		List<TreeFile> breadcrumb = new ArrayList<TreeFile>();
		if (treemodel!=null) {
			TreeFile parent = treemodel.getObject();
			while (parent!=null) {
				breadcrumb.add(0, parent);
				parent = parent.getParent();
				if (parent!=null && parent.equals(rootmodel.getObject())) {
					breadcrumb.add(0, rootmodel.getObject());
					break;
				}
			}
		}
		else {
			breadcrumb.add(rootmodel.getObject());
		}
		return breadcrumb;
	}
	
	public void onDetach() {
		super.onDetach();
		rootmodel.detach();
		if (treemodel!=null) treemodel.detach();
		nodes = null;
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		addTable();
	}
	
	protected List<IColumn<TreeFile, String>> getColumns() {
		
		List<IColumn<TreeFile, String>> columns = new ArrayList<IColumn<TreeFile, String>>();
		
		columns.add(new AbstractColumn<TreeFile, String>(new StringResourceModel("file.icon", this, null)) {
			@Override
			public String getCssClass() {
				return "col-xs-1";
			}
			@Override
			public void detach() {
			}
			@Override
			public String getSortProperty() {
				return null;
			}
			@Override
			public boolean isSortable() {
				return false;
			}
			@Override
			public void populateItem(Item<ICellPopulator<TreeFile>> cellItem, String componentId, IModel<TreeFile> rowModel) {
				try {
					cellItem.add(new GlyphiconColumnPanel<TreeFile>(componentId, rowModel) {
						@Override
						protected String getGlyphiconClass() {
							try {
								if (getModel().getObject().isDirectory()) {
									return FSUtils.getResourceGlyphIconByKey("directory");
								}
								else {
									return FSUtils.getGlyphIcon(getModel().getObject().getName());
								}
							} 
							catch (Exception e) {
								return "";
							}
						}
						@Override
						protected String getCss() {
							return "iconcolumn";
						}
					});
				} 
				catch (Exception e) {
					cellItem.add(new Label(componentId, e.getClass().getName())); 
				}
			}	
		});
		
		columns.add(new AbstractColumn<TreeFile, String>(new StringResourceModel("file.menu", this, null)) {
			@Override
			public String getCssClass() {
				return "col-xs-1";
			}
			@Override
			public void detach() {
			}
			@Override
			public String getSortProperty() {
				return null;
			}
			@Override
			public boolean isSortable() {
				return false;
			}
			@Override
			public void populateItem(Item<ICellPopulator<TreeFile>> cellItem, String componentId, IModel<TreeFile> rowModel) {
				cellItem.add(new MenuFragment(componentId, rowModel.getObject()));
			}	
		});
		
		columns.add(new AbstractColumn<TreeFile, String>(new StringResourceModel("file.index", this, null)) {
			@Override
			public String getCssClass() {
				return "col-xs-1";
			}
			@Override
			public void detach() {
			}
			@Override
			public String getSortProperty() {
				return null;
			}
			@Override
			public boolean isSortable() {
				return false;
			}
			@Override
			public void populateItem(Item<ICellPopulator<TreeFile>> cellItem, String componentId, IModel<TreeFile> rowModel) {
				cellItem.add(new Label(componentId, rowModel.getObject().isAccessPoint()?"X":"")); 
			}	
		});
		
		columns.add(new AbstractColumn<TreeFile, String>(new StringResourceModel("file.name", this, null)) {
			@Override
			public String getCssClass() {
				return "col-xs-6";
			}
			@Override
			public void detach() {
			}
			@Override
			public String getSortProperty() {
				return null;
			}
			@Override
			public boolean isSortable() {
				return false;
			}
			@Override
			public void populateItem(Item<ICellPopulator<TreeFile>> cellItem, String componentId, IModel<TreeFile> rowModel) {
				cellItem.add(new FileNameFragment(componentId, rowModel.getObject()));
			}
		});
		
		columns.add(new AbstractColumn<TreeFile, String>(new StringResourceModel("file.size", this, null)) {
			@Override
			public String getCssClass() {
				return "col-xs-2";
			}
			@Override
			public void detach() {
			}
			@Override
			public String getSortProperty() {
				return null;
			}
			@Override
			public boolean isSortable() {
				return false;
			}
			@Override
			public void populateItem(Item<ICellPopulator<TreeFile>> cellItem, String componentId, IModel<TreeFile> rowModel) {
				TreeFile file = rowModel.getObject();
				String size = "";
				if (file instanceof TreeFileKBFile) {
					size = ServiceLocator.getService(DateTimeService.class).formatFileSize (((TreeFileKBFile)file).getSize(), getSessionUser().getLocale());
				}	
				cellItem.add(new Label(componentId, size)); 
			}
		});
		
		columns.add(new AbstractColumn<TreeFile, String>(new StringResourceModel("file.modified", this, null)) {
			@Override
			public String getCssClass() {
				return "col-xs-2";
			}
			@Override
			public void detach() {
			}
			@Override
			public String getSortProperty() {
				return null;
			}
			@Override
			public boolean isSortable() {
				return false;
			}
			@Override
			public void populateItem(Item<ICellPopulator<TreeFile>> cellItem, String componentId, IModel<TreeFile> rowModel) {
				cellItem.add(new Label(componentId, ServiceLocator.getService(DateTimeService.class).getDateDisplayString(rowModel.getObject().getLastModifiedOffsetDateTime())));
			}
		});

		return columns;
	}
	
	protected void addTable() {
		
		DataTable<TreeFile, String> table = new DataTable<TreeFile, String>("files", getColumns(), new TreeProvider(), 10);
		
		table.addTopToolbar(new AjaxFallbackHeadersToolbar<String>(table, (TreeProvider)table.getDataProvider()));
		WebMarkupContainer container = new WebMarkupContainer("files-container");
		
		WebMarkupContainer tablecontainer = new WebMarkupContainer("table-container");
		container.add(new AjustableHeightBehavior(221));
		tablecontainer.add(new AjustableHeightBehavior(287));
		tablecontainer.add(table);
		container.add(tablecontainer);
		//container.add(new NavigationToolbar("navigation", table));
		container.add(new NavigationToolbar2("navigation", table) {
			@Override
			protected Component getLeftPanel(String id) {
				return new BreadCrumbFragment(id);
			}
		});
		add(container);
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}
