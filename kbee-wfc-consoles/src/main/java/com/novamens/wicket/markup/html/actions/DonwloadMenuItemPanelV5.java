package com.novamens.wicket.markup.html.actions;

import java.io.File;
import java.io.IOException;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import kbee.util.logging.Logger;

@SuppressWarnings("serial")
public abstract class DonwloadMenuItemPanelV5<T> extends  LinkMenuItemPanel<T> {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(DonwloadMenuItemPanelV5.class.getName());
	//static private final File null_file = new File("nullfile");
	
	public DonwloadMenuItemPanelV5(String id, String icon) {
		super(id, icon);
	}
	
	public DonwloadMenuItemPanelV5(String id) {
		super(id);
	}

	protected abstract File getFile() throws IOException; 
	
	@Override
	protected AbstractLink getNewLink(String id) {
		
		IModel<String> filenamemodel = new Model<String>() {
			public String getObject() {
				return getFileName();
			}
		};
		
		IModel<File> filemodel = new Model<File>() {
			public File getObject() {
				try {
					File file = getFile();
					return file;
				}
				catch (IOException e) {
					logger.error(e);
					return null;
				}		
			}
		};
		
		Link<?> link = new DownloadLink(id,  filemodel, filenamemodel) {
			@Override
			public boolean isEnabled() {
				return DonwloadMenuItemPanelV5.this.isEnabled();
			}
//			@Override
//			public void onClick() {
//				try {
//					File file = getFile();
//					if (file!=null) {
//						super.setModelObject(file);
//						super.onClick();
//					}
//					else {
//						logger.error("file is null ");
//					}	
//
//				}
//				catch (IOException e) {
//					logger.error(e);
//				}
//			}
		};
		
		if (isDeleteFileAfterDownload()) 
			 ((DownloadLink) link).setDeleteAfterDownload(true);
		
		if (getTarget()!=null)
			link.add(new AttributeModifier("target", getTarget()));
		
		return link;
	}

	@Override
	public String getLabel() {
		return new StringResourceModel("download", this, null).getObject();
	}
	
	public boolean isDeleteFileAfterDownload()  {
		return false;
	}
	
	public String getFileName() {
		return null;
	}
	
	@Override
	public void onClick() throws Exception {
		 DownloadLink link = (DownloadLink) get("item-link");
		 link.onClick();
	}
}
