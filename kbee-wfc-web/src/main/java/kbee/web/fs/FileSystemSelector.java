package kbee.web.fs;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.user.UserService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.kbee.wicket.markup.html.behaviour.AjustableHeightBehavior;
import com.novamens.kbee.wicket.markup.html.console.grid.GlyphiconColumnPanel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.repeater.util.NavigationToolbar2;

import kbee.util.FSUtils;
import kbee.web.model.util.FileModel;

@SuppressWarnings("serial")
public class FileSystemSelector extends Panel  {
	private static final long serialVersionUID = 1L;
	
	private List<File> files = null;
	private IModel<File> rootmodel;
	private IModel<File> foldermodel = null;
	private List<IModel<File>> selection = new ArrayList<IModel<File>>();
	
	public class FilesProvider extends SortableDataProvider<File, String> {
		public Iterator<File> iterator(long first, long count) {
			ArrayList<File> iteration = new ArrayList<File>();
			Iterator<File> iterator = getFiles().listIterator((int)first);
			int i = 0;
			while (i++<count) {
				iteration.add(iterator.next());
			}
			return iteration.iterator();
		}	
		public IModel<File> model(File object) {
			return new FileModel(object);
		}
		public long size() {
			return getFiles().size();
		}
	}
	
	public class SelectorFragment extends Fragment {
		private IModel<File> model;
		private Boolean selected;
		public SelectorFragment(String id, File file) {
			super(id, "selector-fragment", FileSystemSelector.this);
			this.model = new FileModel(file);
			setSelected(FileSystemSelector.this.isSelected(file));
			CheckBox selector = new AjaxCheckBox("selector", new PropertyModel<Boolean>(this, "selected")) {
				protected void onUpdate(AjaxRequestTarget target) {
					onSelect(target, model, isSelected());
				}
			};
			add(selector);
		}
		public Boolean isSelected() {
			return selected;
		}
		public void setSelected(Boolean value) {
			this.selected = value;
		}
		public void onDetach() {
			super.onDetach();
			this.model.detach();
		}
	};
	
	public class FileNameFragment extends Fragment {
		private IModel<File> model;
		public FileNameFragment(String id, File file) {
			super(id, "filename-fragment", FileSystemSelector.this);
			this.model = new FileModel(file);
			AjaxLink<Void> filenamelink = new AjaxLink<Void>("file-link") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					if (model.getObject().isDirectory()) {
						files = null;
						if (model.getObject().equals(rootmodel.getObject())) {
							foldermodel = null;
						}
						else {
							foldermodel = model;
						}
						target.add(FileSystemSelector.this);
					}
				}
			};
			String name = file.getName();
			if (foldermodel!=null && file.equals(foldermodel.getObject().getParentFile())) {
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
			super(id, "breadcrumb-fragment", FileSystemSelector.this);
			add(new ListView<File>("file", new PropertyModel<List<File>>(FileSystemSelector.this, "breadCrumb")) {
				public void populateItem(ListItem<File> item) {
					item.add(new Label("file-name", item.getModelObject().getName()));
				}
			});
		}
	};	

	public FileSystemSelector(String id, File root, boolean multiple, boolean onlyfolders, boolean onlyfiles) {
		super(id);
		setOutputMarkupId(true);
		rootmodel = new FileModel(root);
	}
	
	public List<File> getFiles() {
		if (this.files!=null)
			return files;
		
		files = new ArrayList<File>();
		
		File root = foldermodel!=null ? foldermodel.getObject() : rootmodel.getObject();
		
		if (!root.exists()) {
			return files;
		}
		
		File childs[] = root.listFiles();
		
		if (foldermodel!=null && root.equals(foldermodel.getObject())) {
			files.add(foldermodel.getObject().getParentFile());
		}
		
		for (int f=0; f<childs.length; f++) {
			File child = childs[f];
			//if (child.isDirectory()) {
				files.add(child);
			//}
		}
		
		return files;
	}
	
	public List<File> getBreadCrumb() {
		List<File> breadcrumb = new ArrayList<File>();
		if (foldermodel!=null) {
			File parent = foldermodel.getObject();
			while (parent!=null) {
				breadcrumb.add(0, parent);
				parent = parent.getParentFile();
				if (parent.equals(rootmodel.getObject())) {
					breadcrumb.add(0, rootmodel.getObject());
					break;
				}
			}
		}
		else {
			if (rootmodel.getObject()!=null)
			breadcrumb.add(rootmodel.getObject());
		}
		return breadcrumb;
	}
	
	public boolean isSelected(File file) {
		for (IModel<File> model : selection) {
			if (model.getObject().getAbsolutePath().equals(file.getAbsolutePath())) {
				return true;
			}
		}
		return false;
	}
	
	public	List<IModel<File>> getSelection() {
		return selection;
	}
	
	public void onDetach() {
		super.onDetach();
		rootmodel.detach();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		addTable();
	}
	
	
	protected void onSelect(AjaxRequestTarget target, IModel<File> model, Boolean value) {
		if (value) {
			selection.add(model);
		}
		else {
			selection.removeIf((selectedmodel) -> selectedmodel.getObject().getAbsolutePath().equals(model.getObject().getAbsolutePath()));
		}
		onSelection(target, selection);	
	}
	
	protected void onSelection(AjaxRequestTarget target, List<IModel<File>> selection) {
	}
	
	protected List<IColumn<File, String>> getColumns() {
		
		List<IColumn<File, String>> columns = new ArrayList<IColumn<File, String>>();

		columns.add(new AbstractColumn<File, String>(new StringResourceModel("file.selector", this, null)) {
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
			public void populateItem(Item<ICellPopulator<File>> cellItem, String componentId, IModel<File> rowModel) {
				cellItem.add(new SelectorFragment(componentId, rowModel.getObject()));
			}
		});
		
		columns.add(new AbstractColumn<File, String>(new StringResourceModel("file.icon", this, null)) {
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
			public void populateItem(Item<ICellPopulator<File>> cellItem, String componentId, IModel<File> rowModel) {
				try {
					cellItem.add(new GlyphiconColumnPanel<File>(componentId, rowModel) {
						@Override
						protected String getGlyphiconClass() {
							try {
								return FSUtils.getGlyphIcon(getModel().getObject()); 
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
		
		columns.add(new AbstractColumn<File, String>(new StringResourceModel("file.name", this, null)) {
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
			public void populateItem(Item<ICellPopulator<File>> cellItem, String componentId, IModel<File> rowModel) {
				cellItem.add(new FileNameFragment(componentId, rowModel.getObject()));
			}
		});
		
		columns.add(new AbstractColumn<File, String>(new StringResourceModel("file.modified", this, null)) {
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
			public void populateItem(Item<ICellPopulator<File>> cellItem, String componentId, IModel<File> rowModel) {
				cellItem.add(new Label(componentId, ServiceLocator.getService(DateTimeService.class).getDateDisplayString(new Date(rowModel.getObject().lastModified()))));
			}
		});
		


		return columns;
	}
	
	protected void addTable() {
		
		DataTable<File, String> table = new DataTable<File, String>("files", getColumns(), new FilesProvider(), 10);
		
		table.addTopToolbar(new AjaxFallbackHeadersToolbar<String>(table, (FilesProvider)table.getDataProvider()));
		WebMarkupContainer container = new WebMarkupContainer("files-container");
		
		WebMarkupContainer tablecontainer = new WebMarkupContainer("table-container");
		container.add(new AjustableHeightBehavior(221));
		tablecontainer.add(new AjustableHeightBehavior(287));
		tablecontainer.add(table);
		container.add(tablecontainer);
		container.add(new NavigationToolbar2("navigation", table) {
			@Override
			protected Component getLeftPanel(String id) {
				return new BreadCrumbFragment(id);
			}
		});
		add(container);
	}
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}
