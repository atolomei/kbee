package kbee.web.error;



import java.io.File;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.cycle.RequestCycle;

import com.novamens.content.entity.Person;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ListModel;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
import kbee.web.resource.WebFileReference;


public class LogsInfoPanel extends  ObjectEditor<Person> {
			
 	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(LogsInfoPanel.class.getName());
 	
	static private final SimpleDateFormat dateformat = new SimpleDateFormat("dd MMM HH:mm:ss");
	
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
	

	private Level rootlevel;
	private Level classlevel;
	private String logclass;

	
	
	public LogsInfoPanel(String id) {
		super(id);

		setOutputMarkupId(true);
		 
		ListView<IModel<File>> logsview = new ListView<IModel<File>>("log", new ListModel<IModel<File>>(new org.apache.wicket.model.Model<Panel>(this), "fileModel")) {

			private static final long serialVersionUID = 1L;

			public void populateItem(final ListItem<IModel<File>> item) {
				WebMarkupContainer loglink = new WebMarkupContainer("loglink");
				WebFileReference fileReference = new WebFileReference(item.getModelObject().getObject());
				String fileUrl = RequestCycle.get().urlFor(fileReference, null).toString();
				loglink.add(new AttributeModifier("href", fileUrl));
				loglink.add(new Label("name", item.getModelObject().getObject().getName()));
				
				String sizelabel;
				long size = item.getModelObject().getObject().length();
				if (size<1024) {
					sizelabel = String.valueOf(size) + " bytes";
				}
				else {
					sizelabel = String.valueOf(size/1024) + " KB";
				}
				item.add(new Label("size", sizelabel));
				item.add(new Label("date", dateformat.format(item.getModelObject().getObject().lastModified())));
				item.add(loglink);
			}
		};
		
		add(logsview);
	}

	
	
	public Level getRootlevel() {
		return rootlevel;
	}

	public void setRootlevel(Level rootlevel) {
		this.rootlevel = rootlevel;
	}

	public Level getClasslevel() {
		return classlevel;
	}

	public void setClasslevel(Level classlevel) {
		this.classlevel = classlevel;
	}

	public String getLogclass() {
		return logclass;
	}


	public void setLogclass(String logclass) {
		this.logclass = logclass;
	}


	public List<Level> getLevels() {
		 List<Level> list =new ArrayList<Level>();
		 list.add(Level.ALL);
		 list.add(Level.DEBUG);
		 list.add(Level.ERROR);
		 list.add(Level.FATAL);
		 list.add(Level.INFO);
		 list.add(Level.OFF);
		 list.add(Level.OFF);
		 list.add(Level.TRACE);
		 list.add(Level.WARN);
		 return list;
	}
	
	
	
	@Override
	public Form<?> getForm() {
		return form;
	}
	
	
	com.novamens.wicket.markup.html.form.Form<?> form;
	
	/**
	 * 
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		 form = new com.novamens.wicket.markup.html.form.Form<Void>("form", Disposition.VERTICAL);

		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration config = ctx.getConfiguration();
		LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME); 
		setRootlevel(loggerConfig.getLevel());
		
		form.add(new ChoiceField<Level>	("rootlevel",  new PropertyModel<Level>(this, "rootlevel"), new PropertyModel<List<Level>>(this, "levels"), false)
		{
			private static final long serialVersionUID = 1L;
			protected String getDisplayValue(Level value) {
				return value.name();
			}
		});
		
		WebMarkupContainer wm = new WebMarkupContainer("log-level-container");
		wm.setVisible(getDomain().getName().equals("kbee"));
		add(wm);
		wm.add(form);
		
		TextField<String> lclass = new TextField<String>("logclass", new PropertyModel<String>(this, "logclass"));
		
		form.add(lclass);
		form.add(new ChoiceField<Level>	("classlevel",  new PropertyModel<Level>(this, "classlevel"), new PropertyModel<List<Level>>(this, "levels"), false)
		{
			private static final long serialVersionUID = 1L;
			protected String getDisplayValue(Level value) {
				return value.name();
			}
		});

		
		
		form.setOutputMarkupId(true);
		lclass.setOutputMarkupId(true);
		
		
		wm.add(new EditButtonsV5<Person>(this, false) {
			private static final long serialVersionUID = 1L;
			@Override
			protected String getEditClass() {
				return "btn btn-default btn-sm";
			}

			@Override
			protected String getSubmitClass() {
				return "btn btn-primary btn-sm";
			}
			@Override
			protected String getCancelClass() {
				return "btn btn-default btn-sm";
			}
		});
	
	}
	

	
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				
						LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
						Configuration config = ctx.getConfiguration();
						LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME); 
						loggerConfig.setLevel(getRootlevel());
						
						if (getLogclass()!=null && getClasslevel()!=null) {
							Configurator.setLevel(getLogclass(), getClasslevel());
						}
						ctx.updateLoggers();  // This causes all Loggers to refetch information from their LoggerConfig.
						
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent(target, e));

			//LogManager.getLogger(UserEditor.class.getName()).error(e);
			//throw new RuntimeException(e);
		}
	}


	/** 
	 * 
	 */
	public List<IModel<File>> getFileModel() {
		List<IModel<File>> model = new ArrayList<IModel<File>>();
		for (File log : getLogFiles()) {
			model.add(new FileModel(log));
		}
		return model;
	}

	/**
	 * 
	 */

	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	
	/** ---------------------------------------------------------------------------------------
	 */

	private List<File> getLogFiles() {
		List<File> logs = new ArrayList<File>();
		File root = new File(".");
		String rootpath = root.getAbsolutePath();
		String rootsegments[] = rootpath.split("\\"+File.separator);
		for (int s=rootsegments.length-1; s>rootsegments.length-5 && s>=0; s--) {
			String path = "";
			for (int p=0; p<=s-1; p++) {
				path += rootsegments[p];
				if (p<s-1) path+= File.separator;
			}
			File file = new File(path);
			if (file.isDirectory()) {
				File child[] = file.listFiles();
				for (int i = 0; i<child.length; i++) {
					String childname = child[i].getName();
					if (child[i].isDirectory() && childname.equals("logs")) {
						File log[] = child[i].listFiles();
						for (int l = 0; l<log.length; l++) {
							if (log[l].getName().endsWith(".log")) {
								logs.add(log[l]);
							}
						}
					}
				}
			}
		}
		
		/**
		 * Sort Alfabetico Descendente 
		 */
	    Collections.sort(logs, new Comparator<File>() {
	    	@Override
			public int compare(File c1, File c2) { 
	    		try {
	    			if (c1.lastModified()>c2.lastModified())
	    				return -1;
	    			else
	    				return 1;
	    			// return (c1.getName().compareToIgnoreCase(c2.getName()));
	    		} catch (Exception e) {
	    			return 0;
	    		}
			}
	    });
	    
		return logs;
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

}
