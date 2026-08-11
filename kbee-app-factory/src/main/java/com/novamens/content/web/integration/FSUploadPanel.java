package com.novamens.content.web.integration;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.form.upload.UploadProgressBar;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.file.Folder;
import org.apache.wicket.util.lang.Bytes;

import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Form;

import kbee.util.FSUtils;

public class FSUploadPanel extends ModelPanel<Domain> {
					
	private static final long serialVersionUID = 1L;
																					
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(FSUploadPanel.class.getName());
	
	private String folder;
	private String bpath;

	Query query;
	
	List<File> uploaded = new ArrayList<File>();

	private 	List<File> getUploadedFiles() {
		return uploaded;
	}
	
	
	/**
	 * @param bc
	 * @param path
	 */
	public FSUploadPanel(String id, Query query, String folder_to_upload_path, String bpath) {
		super(id);
					
		this.folder=folder_to_upload_path;
		this.bpath=bpath;
		this.query=query;
		
        add(new FeedbackPanel("uploadFeedback"));
        add(new FileUploadForm("form"));
	}
	
	public void setUploadFolder(String path)  {
		this.folder=path;
	}
	
	public Folder getUploadFolder()  {
 		Folder uploadFolder = new Folder(folder);
        return uploadFolder;
	}



	private class FileUploadForm extends Form<Void> {

		private static final long serialVersionUID = 1L;
		
		FileUploadField fileUploadField;

		public FileUploadForm(String name)  {
			super(name);
			setMultiPart(true);
			
										
			add(new Label("folder", bpath));
			
			add(fileUploadField = new FileUploadField("fileInput"));
			setMaxSize(Bytes.megabytes(20000));
			setFileMaxSize(Bytes.megabytes(20000));
			
			add(new UploadProgressBar("progress", this));
			
	        add( new Link<Void>("back") {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick() {
					setResponsePage(new FileSystemIntegrationPage(FSUploadPanel.this.query));
				}
	        });
	        
			
	       add(new ListView<File>("already-uploaded", getUploadedFiles()) {
	    	   private static final long serialVersionUID = 1L;
				public void populateItem(final ListItem<File> item) {
					WebMarkupContainer icon = new 	WebMarkupContainer("icon");
					icon.add(new AttributeModifier("class",  FSUtils.getGlyphIcon( item.getModelObject() )));
					item.add(icon);
					Label name = new Label("name", item.getModelObject().getName()); 
					item.add(name);
					Label si  = new Label("size", " (" + ServiceLocator.getService(DateTimeService.class).formatFileSize(item.getModelObject().length())+")" ); 
					si.setEscapeModelStrings(false);
					item.add(si);
				}
			});
		}
		
        @Override
        protected void onSubmit() {
            final List<FileUpload> uploads = fileUploadField.getFileUploads();
            if (uploads != null) {
            	for (FileUpload upload : uploads) {
            		File newFile = new File(getUploadFolder(), upload.getClientFileName());
                    boolean inew = !newFile.exists();
                    if (!inew) {
            			if (!Files.remove(newFile)) {
            				throw new IllegalStateException("Unable to overwrite " + newFile.getAbsolutePath());
            			}
            		}
                    try  {
                        newFile.createNewFile();
                        upload.writeTo(newFile);
                        
                        
                		try {
                			if (isExecutable(newFile)) {
	                			logger.debug("chmod +x "+ newFile.getAbsolutePath());
	                			Process proc = Runtime.getRuntime().exec("chmod +x "+newFile.getAbsolutePath());
	                			proc.waitFor();
	                			logger.debug("After chmod");
                			}
                			
                		} catch (Exception e) {
                			logger.error(e.getClass().getName() + " | " + e.getMessage());
                			throw(new IOException(e));
                		}
                        
                        
                        uploaded.add(newFile);
                        
                        
                        
                    }
                    catch (Exception e) {
                    	logger.error(e);
                        throw new IllegalStateException("Unable to write file", e);
                    }
                    
                }
            }
        }
    }



	public boolean isExecutable(File f) {
		
		if (!f.exists())
			return false;
		if (f.isDirectory())
			return false;
		String name = f.getName();
		return name.toLowerCase().matches("^.*\\.(cmd|bat|sh|deb|exe)$");
	}

	
}

