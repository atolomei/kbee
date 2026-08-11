package com.novamens.content.web.integration;


import java.io.File;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.content.command.ListQuery;
import com.novamens.kbee.content.command.ListResultSet;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.ServiceLocator;
import com.novamens.system.parameters.SystemParameterService;

import kbee.util.PropertiesFactory;

public class LocalFSQuery extends ListQuery<File>  {
										
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(LocalFSQuery.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	
	private boolean is_root = true;
	
	private File root_dir;
	private File dir;
	private List<File> list;
	
	//private Map<String, Object> parameters;
	
	private Boolean is_kbee = null;
	
	
	public LocalFSQuery() {
		super();
		this.root_dir = new File(getDriveDir());
		this.dir= new File(getDriveDir());
		
	}
	
	public LocalFSQuery(File dir, boolean isRoot) {
		super();
		this.dir=dir;
		is_root =isRoot;
		if(is_root)
			this.root_dir = dir;
	}
	

	public LocalFSQuery(File dir) {
		super();
		this.dir=dir;
		this.root_dir = dir;
	}
	
	
	//public LocalFSQuery(List<File> list) {
	//	super(list);
	//	is_root =true;
	//}
	
	
	public File getRootDir() {
		return this.root_dir;
	}
	
	public boolean isRoot() {
		return this.is_root;
	}

	
	public File getDirectory() {
		return this.dir;
	}
	
	
	public void setDirectory(File dir) {
		this.dir=dir;
		list = null;
		this.is_root=(dir.getAbsolutePath().equals(this.root_dir.getAbsolutePath()));
	}

	
	@Override
	public ResultSet execute() {
		

		//if (getParameters().containsKey("domain"))
			//doimailist=((CommandService) ServiceLocator.getService(CommandService.class)).getCommandsAsList((Serializable) getParameters().get("domain"));
		//else
			//list=((CommandService) ServiceLocator.getService(CommandService.class)).getCommandsAsList();
		

		this.list=null;
		
		String asc = (String) getParameters().get("ascending");
		
		if (asc==null)
			asc="true";
		
		if (getParameters().containsKey("sort")) {
			
				String sort = (String) getParameters().get("sort");
				
				if (sort.equals("name") || sort.equals("title_sort") || sort.equals("icon"))
					sortName(getList(), asc);
				
				else if (sort.equals("size")) 
					sortSize(getList(), asc);
				
				else if (sort.equals("modified")) 
					sortModified(getList(), asc);
				
				else if (sort.equals("type")) 
					sortType(getList(), asc);

				else  
					sortName(getList(), asc);
		}
		else
			sortName(getList(), asc);
		
		ListResultSet<File> resultset=new ListResultSet<File>(getList());
		return resultset;
		
  }
	
	
	protected Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}
	

	/**
	 * KBEE Domain ca walk through the whole FS
	 */
	protected List<File> getList() {
		if (list==null) {
			list = new ArrayList<File>();
			if (this.dir.getParent()!=null) {
				if (isDomainKbee() || !this.isRoot()) 
					list.add(new ParentLinkFile(this.dir.getParent()));
				}
			for (File fi: this.dir.listFiles())
					list.add(fi);
		}
		return list;
	}
	
	
	
	
	private boolean isDomainKbee() {
		if (is_kbee == null)
			is_kbee = Boolean.valueOf(getPerson()!=null && getPerson().getDomain().getName().equals("kbee"));
		return is_kbee.booleanValue();
	}

	
	public String getDriveDir() {
		 return ServiceLocator.getService(ApplicationServerService.class).getDriveDir();
	}
	
	
					
	private void sortType(List<File> list, final String asc_order) {
		Collections.sort(list, new	 Comparator<File>() {
			@Override
			public int compare(File a, File b) {
				try {

					// Dir
					if (a.isDirectory() && b.isDirectory()) {
						if (asc_order.equals("true"))
							return a.getName().compareToIgnoreCase(b.getName());
						else
							return b.getName().compareToIgnoreCase(a.getName());
					}
					else if (a.isDirectory() && !b.isDirectory()) 
						return (asc_order.equals("true"))?-1:1;
					
					else if (!a.isDirectory() && b.isDirectory()) 
						return (asc_order.equals("true"))?1:-1;
					
					// --
					
					String a_ext=FilenameUtils.getExtension(a.getName()); 
					String b_ext=FilenameUtils.getExtension(b.getName());
					
					if (asc_order.equals("true"))
							return a_ext.compareToIgnoreCase(b_ext);
					else
						return b_ext.compareToIgnoreCase(a_ext);
						
				} catch (RuntimeException e) {
					logger.error(e);
					return 0;
				}
			}
		});
	}
	
	
								
	private void sortName(List<File> list, final String asc_order) {
		Collections.sort(list, new	 Comparator<File>() {
			@Override
			public int compare(File a, File b) {
				try {

					// Dir
					if (a.isDirectory() && b.isDirectory()) {
						if (asc_order.equals("true"))
							return a.getName().compareToIgnoreCase(b.getName());
						else
							return b.getName().compareToIgnoreCase(a.getName());
					}
					else if (a.isDirectory() && !b.isDirectory()) 
						return (asc_order.equals("true"))?-1:1;
					
					else if (!a.isDirectory() && b.isDirectory()) 
						return (asc_order.equals("true"))?1:-1;
					
					// --

				
					else if (a.getName()==null && b.getName()!=null)
							return (asc_order.equals("true"))?-1:1;
						
					else if (a.getName()!=null && b.getName()==null)
							return (asc_order.equals("true"))?1:-1;
						
					if (asc_order.equals("true"))
						return a.getName().compareToIgnoreCase(b.getName());
					else
						return b.getName().compareToIgnoreCase(a.getName());
						
				} catch (RuntimeException e) {
					logger.error(e);
					return 0;
				}
			}
		});
	}
		
						
	private void sortSize(List<File> list, final String asc_order) {
			Collections.sort(list, new	 Comparator<File>() {
				@Override
				public int compare(File a, File b) {
					try {
						// Dir
						if (a.isDirectory() && b.isDirectory()) {
							if (asc_order.equals("true"))
								return a.getName().compareToIgnoreCase(b.getName());
							else
								return b.getName().compareToIgnoreCase(a.getName());
						}
						else if (a.isDirectory() && !b.isDirectory()) 
							return (asc_order.equals("true"))?-1:1;
						
						else if (!a.isDirectory() && b.isDirectory()) 
							return (asc_order.equals("true"))?1:-1;
						
						// --

						
							
							else if (a.length()==b.length()) {
								if (asc_order.equals("true"))
									return a.getName().compareToIgnoreCase(b.getName());
								else
									return b.getName().compareToIgnoreCase(a.getName());
							}
							else if (asc_order.equals("true"))
								return a.length()<b.length()?1:-1;
							else
								return a.length()>b.length()?1:-1;
								
								
								
								
								
								
								
								
					} catch (RuntimeException e) {
						logger.error(e);
						return 0;
					}
				}
			});
			
	}
							
	
	private void sortModified(List<File> list, final String asc_order) {
				Collections.sort(list, new	 Comparator<File>() {
					@Override
					public int compare(File a, File b) {
						try {
							// Dir
							if (a.isDirectory() && b.isDirectory()) {
								if (asc_order.equals("true"))
									return a.getName().compareToIgnoreCase(b.getName());
								else
									return b.getName().compareToIgnoreCase(a.getName());
							}
							else if (a.isDirectory() && !b.isDirectory()) 
								return (asc_order.equals("true"))?-1:1;
							
							else if (!a.isDirectory() && b.isDirectory()) 
								return (asc_order.equals("true"))?1:-1;
							
							// --
								
								
								else if (a.lastModified()==b.lastModified()) {
									if (asc_order.equals("true"))
										return a.getName().compareToIgnoreCase(b.getName());
									else
										return b.getName().compareToIgnoreCase(a.getName());
								}

								else if (asc_order.equals("true"))
									return a.lastModified()<b.lastModified()?1:-1;
								else
									return a.lastModified()>b.lastModified()?1:-1;
									
						} catch (RuntimeException e) {
							logger.error(e);
							return 0;
						}
					}
				});
		
	}

	
	public void reload() {
			this.list=null;
	}
	
}




