package com.novamens.content.web.admin.markup;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ListModel;

public class FileSystemXplorerPanel extends Panel {

	static private final SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


	@SuppressWarnings("unused")
	private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(FileSystemXplorerPanel.class.getName());

	private List<File> logs;
	
	private Map<String, String> dirs = new HashMap<String, String>();
	private Map<String, String> extensions = new HashMap<String, String>();
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public class FileModel implements IModel<File> {

		private static final long serialVersionUID = 1L;
		private String path;
		private File file;
		
		public FileModel(File file) {
			path = file.getAbsolutePath();
		}
		public File getObject() {
			if (file==null)
				file = new File(path);
			return file;
		}
		public void setObject(File file) {
			
		}
		public void detach() {
			file = null;
		}
	}
	
	
	
	/** ---------------------------------------------------------------------------------------
	 */

	public FileSystemXplorerPanel(String id) {
		super(id);
		
		this.setOutputMarkupId(true);
		
	}
	

	@Override
	public void onDetach() {
		super.onDetach();
		 if (model!=null)
			 for(IModel<File> m: model)
				 m.detach();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	

		ListView<IModel<File>> logsview = new ListView<IModel<File>>("log", new ListModel<IModel<File>>(new org.apache.wicket.model.Model<Panel>(this), "model")) {

			private static final long serialVersionUID = 1L;

			public void populateItem(final ListItem<IModel<File>> item) {

				Link<?> loglink = new DownloadLink("loglink", item.getModelObject().getObject());
				loglink.add(new Label("name", item.getModelObject().getObject().getName()));
				
				String sizelabel;
				long size = item.getModelObject().getObject().length();
				if (size<1024) {
					sizelabel = String.valueOf(size) + " bytes";
				}
				else {
					sizelabel = String.valueOf(size/1024) + " KB";
				}
				
				
				item.add(new Label("dir", FilenameUtils.getPath(item.getModelObject().getObject().getAbsolutePath())));
				item.add(new Label("size", sizelabel));
				item.add(new Label("date", dateformat.format(item.getModelObject().getObject().lastModified())));
				item.add(loglink);
			}
		};
		
		add(logsview);
		
	}

	
	 
	public void setExtensions(List<String> ext) {
		
		for (String st: ext) {
			extensions.put(st, st);
		}
	}

	

	public void setDirs(List<String> subdirs) {
			
		dirs.clear();
		
		for (String st: subdirs) {
			dirs.put(st, st);
		}
	}

	

	List<IModel<File>> model;
	
	public List<IModel<File>> getModel() {
		
		if (model!=null)
			return model;
		
		 model = new ArrayList<IModel<File>>();
		for (File log : getFiles()) {
			model.add(new FileModel(log));
			//logger.info(log.getAbsolutePath());
		}
		return model;
	}


	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	
	
	private List<File> getFiles() {

		logs = new ArrayList<File>();
		
		
		
		
		// File root = new File(".");

		File root = new File(ServiceLocator.getService(ApplicationServerService.class).getHomeDirAbsolutePath());
		
		File child[] = root.listFiles();
		
		for (int i = 0; i<child.length; i++) {
			
			if (!child[i].isDirectory()) {
					String ext = FilenameUtils.getExtension(child[i].getName());
				if (extensions.containsKey("*")  || extensions.containsKey(ext)) {
					logs.add(child[i]);
				}
			}
			else {
				 if (dirs.containsKey(FilenameUtils.getBaseName(child[i].getName()))) {
					 	addFiles(child[i]);
				 }
			}
		}
		
		
		Collections.sort(logs, new Comparator<File>() {
	    	@Override
			public int compare(File c1, File c2) { 
	    		try {
	    			int c = FilenameUtils.getFullPath(c1.getAbsolutePath()).compareTo(FilenameUtils.getFullPath(c2.getAbsolutePath()));
	    			if (c==0) {
	    				return (c1.getName()).compareTo(c2.getName());	
	    			}
	    			else
	    				return c;
	    			
	    		} catch (Exception e) {
	    			return 0;
	    		}
			}
	    });
	    
	    return logs;

	}
	
	

	private void addFiles(File file) {
	
		if (file.isDirectory()) {
				
				File child[] = file.listFiles();
				
				for (int i = 0; i<child.length; i++) {
				
					if (!child[i].isDirectory()) {
							String ext = FilenameUtils.getExtension(child[i].getName());
						if (extensions.containsKey("*")  || extensions.containsKey(ext)) {
							logs.add(child[i]);
						}
					}
					else {
						 	addFiles(child[i]);
					}
				}
			}
		}
		
	

	
	
	
	
	
	
	
	
	
}
