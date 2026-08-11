package com.novamens.content.web.admin.files;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.AttributeModifier;
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
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.web.admin.markup.XAjaxLink;
import com.novamens.content.web.admin.markup.XLink;
import com.novamens.content.web.admin.markup.datamanagement.AbstractDataManagementPanel;
import com.novamens.content.web.admin.markup.datamanagement.SystemDataManagementGeneralPage;
import com.novamens.content.web.deployManagement.DeployManagementFormPanel;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.wicket.markup.html.console.browser.BrowserNavigationToolbar;
import com.novamens.kbee.wicket.markup.html.console.browser.AjaxToolbarButton;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.ContentExportService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.actions.MenuItemFactory;
import com.novamens.wicket.markup.html.actions.AbstractMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.MenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.SeparatorMenuItemPanelV5;
import com.novamens.wicket.model.ListModel;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.DummyBlockPanel;

import kbee.util.FSUtils;
import kbee.util.NumberFormatter;
import kbee.web.console.grid.DatePropertyColumn;
import kbee.web.resource.WebFileReference;
import kbee.web.service.ApplicationSiteMapService;

/**
 * ONLY ROOT CAN EDIT, DELETE, UPLOAD FILES
 * 
 * File (Directory)
 * 
 * label read
 * edit
 * save, cancel
 *
 */
@SuppressWarnings("serial")
public class DMFilesPanel extends AbstractDataManagementPanel {
				
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DMFilesPanel.class.getName());
	
	private String path = System.getProperty("user.dir");
	
	private List<XAjaxLink> bc = new ArrayList<XAjaxLink>();
	
	private class MenuFragment extends Fragment {
		private IModel<File> model;
		public MenuFragment(String id, IModel<File> model) {
			super(id, "menu-fragment", DMFilesPanel.this);
			this.model = model;
			Serializable objid = String.valueOf(model.getObject().hashCode());
			WebMarkupContainer menulink = new WebMarkupContainer("menulink");
			menulink.add(new AttributeModifier("id", String.valueOf(objid)));
			add(menulink);
			Panel menupanel = getMenu(getFileModel());
			if (menupanel!=null) {
				menupanel.add(new AttributeModifier("aria-labelledby", String.valueOf(objid))); 
				add(menupanel);
			}
			else {
				menulink.setVisible(false);
				add((new Label("menu")).setVisible(false));
			}
		}
		
		public IModel<File> getFileModel() {
			return model;
		}
		@Override
		public void onDetach() {
			this.model.detach();
			super.onDetach();
		}
	}
	
	private class FileNameFragment extends Fragment {
		IModel<File> model;
		public FileNameFragment(String id, File file) {
			super(id, "name-fragment", DMFilesPanel.this);
			this.model = new Model<File>(file);

			if (file.isDirectory()) {
				AjaxLink<File> link=new AjaxLink<File>("name-link") {
					@Override
					public void onClick(AjaxRequestTarget target) {
						
						logger.debug(getFileModel().getObject().getAbsolutePath());
						
						DMFilesPanel.this.setPath(getFileModel().getObject().getAbsolutePath());
						DMFilesPanel.this.addTable();
						target.add(DMFilesPanel.this);
					}
				};
				add(link);
				link.add(new AttributeModifier("class", " btn-link  "+ (file.isDirectory()?" f_directory":" f_file")));
				link.add((new Label("name", file.getName())).setEscapeModelStrings(false));
			}
			else {
				WebMarkupContainer c = new WebMarkupContainer("name-link");
				add(c);
				c.add(new AttributeModifier("class","f_file"));
				c.add((new Label("name", file.getName())).setEscapeModelStrings(false));
				ResourceReference resourceReference = new WebFileReference(file);
				String resourcehref = RequestCycle.get().urlFor(resourceReference, null).toString();
				c.add(new AttributeModifier("href", resourcehref));
				c.add(new AttributeModifier("target", "_blank"));
			}
		};
		
		public IModel<File> getFileModel() {
			return this.model;
		}
		
		public void onDetach() {
			super.onDetach();
			this.model.detach();
		}
	};	

	private class FIt implements Iterator<File> {
		private List<File> list;
		int index = 0;		
		public FIt(List<File> list) {
			this.list = list;
		}		
		@Override
		public boolean hasNext() {
			return (index<list.size());
		}
		@Override
		public File next() {
			return list.get(index++);
		}
	}

	private class FilesProvider extends SortableDataProvider<File, String> {
		List<File> list = new ArrayList<File>();
		String dirpath;
		
		public FilesProvider(String dirpath) {
			this.dirpath=dirpath;
			 File dir = new File(this.dirpath);
				if (dir.exists() && dir.isDirectory()) {
					
					File [] files = dir.listFiles();
					
					for (File file: files) 
							list.add(file);
				
					Collections.sort(list, new Comparator<File>() {
						@Override
						public int compare(File o1, File o2) {
							try {
							if (o1.isDirectory() && !o2.isDirectory())
								return -1;
							
							if (o2.isDirectory() && !o1.isDirectory())
								return 1;
							return o1.getName().compareToIgnoreCase(o2.getName());
							} catch (Exception e) {
								logger.error(e);
								return 0;
							}
						}					
					}); 
				}
		}
		
		public Iterator<File> iterator(long first, long count) {
			List<File> xl = new ArrayList<File>();	
			int index = 0;
			for (File file: list) {
					if ((index>=first) && index<(first+count))
						xl.add(file);
					index++;
			}
			return new FIt(xl);
		}
		
		public long size() {
			return list.size();
		}
		
		@Override
		public IModel<File> model(File object) {
			return new Model<File>(object);
		}
	}
	
	/**
	 * 
	 * 
	 * 
	 */
	public DMFilesPanel() {
		super("info-panel");
		setOutputMarkupId(true);
	}
	
	public DMFilesPanel(String id) {
		super(id);
		setOutputMarkupId(true);
	}

	public List<XAjaxLink> getDirectoryXLinks() {
		return bc;
	}
	
	public void setDirectory(String d) {
		this.path=d;
	}
	
	public String getDirectory()  {
		return path;
	}
	
	@Override
	protected BCElement getPageBCElement() {
		return new BCElement(new Model<String>("File Explorer"));
	}
	
	
	 
	final boolean root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();
	protected boolean hasPermissions() {
			return root;
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		if (hasPermissions()) {
		setDirectory(ServiceLocator.getService(ApplicationServerService.class).getHomeDirAbsolutePath());
		setPath(getDirectory());
		addTable();
		}
		else {
			add( new DummyBlockPanel("directory"));
			add( new DummyBlockPanel("files-container"));
		}
	}
	
	/**
	 * menu
	 * name
	 * size
	 * 
	 * @return
	 */
	protected List<IColumn<File, String>> getColumns() {
		
		List<IColumn<File, String>> columns = new ArrayList<IColumn<File, String>>();
			
		columns.add(new AbstractColumn<File, String>(new Model<String>("Actions"), "actions") {
			private static final long serialVersionUID = 1L;
			@Override
			public String getCssClass() {
				return "col-xs-1";
			}
			@Override
			public void populateItem(Item<ICellPopulator<File>> cellItem, String componentId, IModel<File> rowModel) {
				cellItem.add(new MenuFragment(componentId,rowModel));
			}
			@Override
			public boolean isSortable() {
				return false;
			}
		});
			
		columns.add(new AbstractColumn<File, String>(new Model<String>("Name"), "name") {
			@Override
			public String getCssClass() {
				return "col-xs-3";
			}
			@Override
			public void populateItem(Item<ICellPopulator<File>> cellItem, String componentId, IModel<File> rowModel) {
				FileNameFragment panel = new FileNameFragment(componentId, rowModel.getObject());
				cellItem.add(panel);
			}
		});
			
		columns.add(new AbstractColumn<File, String>(new Model<String>("Size"), "size") {
			@Override
			public String getCssClass() {
				return "col-xs-1";
			}
			@Override
			public void populateItem(Item<ICellPopulator<File>> cellItem, String componentId, IModel<File> rowModel) {
				try {
				if (!rowModel.getObject().isDirectory()) {
					String stsize=NumberFormatter.formatFileSize(FileUtils.sizeOf(rowModel.getObject()), getSessionUser().getLocale(), "ago");
					cellItem.add((new Label(componentId, stsize)).setEscapeModelStrings(false));
				}
				else
					cellItem.add((new Label(componentId, "")).setEscapeModelStrings(false));
				} catch (Exception e) {
					cellItem.add((new Label(componentId, e.getClass().getSimpleName() + " " + e.getLocalizedMessage())).setEscapeModelStrings(false));
					logger.error(e);
				}
			}
			@Override
			public boolean isSortable() {
				return false;
			}
		});
		
		String zid = ServiceLocator.getService(DateTimeService.class).getMapZoneIds().get(getSessionUser().getTimeZone());
		if (zid==null)
			zid=ZoneId.systemDefault().getId();
			
		columns.add(new DatePropertyColumn<File, String>(new Model<String>("Modified"), "lastmodified", ZoneId.of(zid), getSessionUser().getLocale(), false) {
			@Override
			public String getCssClass() {
				return "col-xs-2";
			}
			public void populateItem(final Item<ICellPopulator<File>> item, final String componentId, final IModel<File> rowModel) {
				try {
					long lastm=rowModel.getObject().lastModified();
					OffsetDateTime o=OffsetDateTime.ofInstant(Instant.ofEpochMilli(lastm),  getZoneId());
					DateTimeService service = ServiceLocator.getService(DateTimeService.class);
					String tst = service.timeElapsed(o, getZoneId(),  getLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
					item.add((new Label(componentId, new Model<String>(tst))).setEscapeModelStrings(false));
				} 
				catch (Exception e) {
					item.add((new Label(componentId, new Model<String>(e.getClass().getName()))).setEscapeModelStrings(false));
				}
			}
			@Override
			public boolean isSortable() {
				return false;
			}
		});
		
		
		columns.add(new AbstractColumn<File, String>(new Model<String>("User Rights")) {
			@Override
			public String getCssClass() {
				return "col-xs-1";
			}
			@Override
			public void populateItem(Item<ICellPopulator<File>> cellItem, String componentId, IModel<File> rowModel) {
				if (!rowModel.getObject().isDirectory()) {
					File file = rowModel.getObject();
					String per= (file.canRead() ? "r":"_ ") + (file.canWrite()  ? "w":"_ ") + (file.canExecute() ? "x":"_ ");
					cellItem.add((new Label(componentId, per)).setEscapeModelStrings(false));
				}
				else
					cellItem.add((new Label(componentId, "")).setEscapeModelStrings(false));
			}
			@Override
			public boolean isSortable() {
				return false;
			}
		});
		
		return columns;
		
	}
		
	/**
	 * 
	 * Edit
	 * Delete
	 * Archive
	 * Upload Replacement 
	 * 
	 * @param model
	 * @return
	 */
	protected Panel getMenu(IModel<File> model) {
		ContextMenuPanel<File> menu = new ContextMenuPanel<File>(model);
		menu.setOutputMarkupId(true);
				
		menu.addItem(new MenuItemFactory<File>() {
			@Override
			public AbstractMenuItemPanelV5<File> getItem(String id) {
				return new MenuItemPanelV5<File>(id) {
					@Override
					public void onClick() {
						try {
							
							PageParameters pa = new PageParameters();
							pa.add("file", getModel().getObject().getAbsolutePath().toString());
							pa.add("id", "dm-text-file-editor-panel");
							
							//add(ServiceLocator.getService(ApplicationSiteMapService.class).getFactoryPanel("dm-text-file-editor-panel", pa));
							
							setResponsePage(new SystemDataManagementGeneralPage(pa));
							
							// TODO V6
							// setResponsePage( new SystemDataManagementGeneralPage(new DMTextFileEditorPanel(getModel()), "textfile-editor"));
						} 
						catch (Exception e) {
							logger.error(e);
						}
					}
					@Override 
					public String getTarget() {
						return "_blank";
					}
					@Override 
					public String getLabel() {
						return "Open";
					}
					@Override
					public boolean isVisible() {
						return isDomainKbee() && isRoot() && isTextEditable(getModel());
					}
					@Override
					public boolean isEnabled() {
						return isDomainKbee() && isRoot() && isTextEditable(getModel());
					}
				};
			}
		});

		menu.addItem(new MenuItemFactory<File>() {
			@Override
			public AbstractMenuItemPanelV5<File> getItem(String id) {
				return new com.novamens.wicket.markup.html.actions.DonwloadMenuItemPanelV5<File>(id) {
					@Override 
					public String getLabel() {
						if (getModel().getObject().isDirectory())
							return "Download Dir (zip)";
						else
							return "Download";
						
					}
							
					@Override
					public boolean isDeleteFileAfterDownload()  {
						return false;
					}
					@Override
					protected File getFile() {
						
						if (getModel().getObject().isDirectory()) {
							com.novamens.kbee.content.service.datamanagement.DirectoryZipper zipper = 
									new com.novamens.kbee.content.service.datamanagement.DirectoryZipper( 
											getModel().getObject(), 
											new File(ServiceLocator.getService(ApplicationServerService .class).getWorkDirAbsolutePath()),
											 getModel().getObject().getName() +".zip");
							
							try {
								zipper.execute();
								return new File(ServiceLocator.getService(ApplicationServerService .class).getWorkDirAbsolutePath() + File.separator + getModel().getObject().getName() +".zip");
							} catch (IOException e) {
								logger.error(e);
								return null;
							}
						}
						else {
							return getModel().getObject();
						}
					}
							
					@Override 
					public boolean isEnabled() {
						return isDomainKbee() && getModel().getObject().exists();
					}							
					@Override
					public boolean isVisible()  {
						return isDomainKbee() && getModel().getObject().exists();
					}
				};
			}
		});
				
		menu.addItem(new MenuItemFactory<File>() {
			@Override
			public AbstractMenuItemPanelV5<File> getItem(String id) {
				return new SeparatorMenuItemPanelV5<File>(id) {
					@Override
					public String getCssClass() {
						return "divider";
					}
					@Override
					public boolean isVisible() {
						return true;
					}
				};
			}
		});
				
		menu.addItem(new MenuItemFactory<File>() {
			@Override
			public AbstractMenuItemPanelV5<File> getItem(String id) {
				return new AjaxMenuItemPanelV5<File>(id) {
					public void onClick(AjaxRequestTarget target) {
						
						if (getModel().getObject().exists()) {
							if (getModel().getObject().isDirectory()) {
								logger.debug("Delete directory " + getModel().getObject().getName());
								deleteDirectory(getModel());
								
							}
							else {
								logger.debug("Delete file " + getModel().getObject().getName());
								deleteFile(getModel());
							}
							DMFilesPanel.this.addTable();
							target.add(DMFilesPanel.this);
						}
					
					}
					@Override 
					public String getLabel() {
						if (getModel().getObject().isDirectory())
							return "Delete (including all contents)";
						else
						return "Delete";
					}
					@Override
					public boolean isVisible() {
						return isDomainKbee() && isRoot();
					}
					@Override
					public boolean isEnabled() {
						return isDomainKbee() && isRoot();
					}
				};
			}
		});
			
		return menu;
	}

	
	protected void setPath(String path) {
		
		this.path = path;
		
		List<String> paths = new ArrayList<String>();
		path = path.replace("\\", "/");
		paths.addAll(Arrays.asList(path.split("/")));
		
		bc = new ArrayList<XAjaxLink>();
		path = "";
		int n =0;
		
		for (String node : paths) {
			if( node.equals("") && !"".equals(path)) //Last emtpy separator
				continue;

            if ("".equals(path) && node.equals("")) {
				path = File.separator;
				node = "root";
            }else
                path = path + node + File.separator;


			bc.add(new XAjaxLink(new Model<String>(node), path) {
				public void onClick(AjaxRequestTarget target) {
				
					logger.debug( "getLocalPath() -> " + getLocalPath());
					
					setPath(getLocalPath());
					addTable();
					target.add(DMFilesPanel.this);
				}
			});

		}
	}
	
	private void addTable() {

		// path
		ListView<XAjaxLink> dp = new ListView<XAjaxLink>("directory", new ListModel<XAjaxLink>(new Model<Panel>(this), "DirectoryXLinks")) {
			@Override
			protected void populateItem(ListItem<XAjaxLink> item) {
				XLink element = item.getModelObject();
				AbstractLink link = element.getLink("link");
				link.add(new Label("label", element.getLabel()));
		 		if (element.isNewTab())
					link.add(new AttributeModifier("target", "_blank"));
				item.add(link);
				if (item.getIndex()==getDirectoryXLinks().size()-1)
					item.add(new AttributeModifier("class", "active"));
			}			
		};
		addOrReplace(dp);

		
		// grid
		
		DataTable<File, String> table = new DataTable<File, String>("gridfiles", getColumns(), new FilesProvider(this.getDirectory()), 200);
		table.addTopToolbar(new AjaxFallbackHeadersToolbar<String>(table, (FilesProvider)table.getDataProvider()));
		
		
		WebMarkupContainer container = new WebMarkupContainer("files-container");
		WebMarkupContainer tablecontainer = new WebMarkupContainer("table-container");
		tablecontainer.add(table);
		container.add(tablecontainer);
		
		container.addOrReplace(new BrowserNavigationToolbar("navigation", table, String.valueOf(table.getItemCount())) {
			
			public List<ToolbarItem> getToolbarItems() {
				
				List<ToolbarItem> items = new ArrayList<ToolbarItem>();
				
				items.add(new AjaxToolbarButton( null, ToolbarItem.Align.TOP_NONE, true) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						
						PageParameters pageParameters = new PageParameters();
						pageParameters.set("directory", getDirectory());
						pageParameters.set("id", "dm-upload-panel");

						getPage().setResponsePage(new SystemDataManagementGeneralPage(pageParameters));
		
						// new SystemDataManagementGeneralPage(new DMUploadPanel(getDirectoryXLinks(), getDirectory())
						// setResponsePage("file-info");
						//
					}
					@Override
					public boolean isVisible() {
						return isDomainKbee();
					}
					@Override
					public boolean isEnabled() {
						return true;
					}
					@Override
					protected String getAnchorTitle() {
						return "Upload";
					}
					
					 protected String getLabelStr() {
						 return "Upload";
					 }
					 
					 
					@Override
					protected String getIcon() {
						return ""; 
					}
				});
				
				
				items.add(new AjaxToolbarButton( null, ToolbarItem.Align.TOP_NONE, false) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						
						PageParameters pageParameters = new PageParameters();
						pageParameters.set("directory", getDirectory());
						pageParameters.set("id", "directory-creation" );

						getPage().setResponsePage(new SystemDataManagementGeneralPage(pageParameters));
						
						// setResponsePage(new SystemDataManagementGeneralPage(new  DMDirectoryCreationPanel("info-panel", getDirectory()), "directory-creation" ));
						
						
					}
					@Override
					public boolean isVisible() {
						return isDomainKbee();
					}
					@Override
					public boolean isEnabled() {
						return true;
					}
					@Override
					protected String getAnchorTitle() {
						return "Create Directory";
					}
					
					 protected String getLabelStr() {
						 return "Create Directory";
					 }
					 
					@Override
					protected String getIcon() {
						return ""; 
					}
				});

				
				
				
				
				
				
				return items;
			}
		});
		
		addOrReplace(container);

		
	}
		
	
	/**
	 * 
	 * 
	 * @param model
	 */
	private void deleteDirectory(IModel<File> model) {
		try {
			DateTimeFormatter workdf = DateTimeFormatter.ofPattern("YYYYMMdd");
			File destfile = new File(getDMFilesWorkDir() + File.separator + workdf.format(LocalDateTime.now()) + File.separator + model.getObject().getName());
			FileUtils.moveDirectory(model.getObject(), destfile);
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	
	private void deleteFile(IModel<File> model) {
		try {
			DateTimeFormatter workdf = DateTimeFormatter.ofPattern("YYYYMMdd");
			File candidate = new File(getDMFilesWorkDir()+ File.separator + workdf.format(LocalDateTime.now()) + File.separator + model.getObject().getName());
			int n=0;
			while (candidate.exists())
				candidate = new File(getDMFilesWorkDir() + File.separator + workdf.format(LocalDateTime.now()) + File.separator + model.getObject().getName()+String.valueOf(++n));
			File destfile = candidate;
			
			FileUtils.moveFile(model.getObject(), destfile);
			
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	private boolean isTextEditable(IModel<File> model) {
		if (FSUtils.isText(model.getObject()))
			return true;
		return false;
 	}
	
	private boolean isRoot() {
		return ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(getSessionUser());
	}
		
	
	
	
	
 	private String getDMFilesWorkDir() {
 		return ServiceLocator.getService(ApplicationServerService.class).getWorkDirAbsolutePath() +  File.separator + "dmfiles";
 	}
	
	

	
	private boolean isDomainKbee() {
		try {
			return getPerson().getDomain().getName().toLowerCase().trim().equals("kbee");
		} 
		catch (Exception e) {
			logger.error(e);
			logger.error(" isDomainKbee ");
			return false;
		}
	}

}
