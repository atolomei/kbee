package com.novamens.content.web.admin.files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.form.upload.UploadProgressBar;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.file.Folder;
import org.apache.wicket.util.lang.Bytes;

import com.novamens.content.web.admin.markup.XAjaxLink;
import com.novamens.content.web.admin.markup.XLink;
import com.novamens.content.web.admin.markup.datamanagement.AbstractDataManagementPanel;
import com.novamens.content.web.admin.markup.datamanagement.SystemDataManagementGeneralPage;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.model.ListModel;
import com.novamens.wicket.util.BCElement;

@SuppressWarnings("serial")
public class DMUploadPanel extends AbstractDataManagementPanel {
			
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DMUploadPanel.class.getName());
	
	private String folder;
	
	private List<XAjaxLink> bc = new ArrayList<XAjaxLink>();

	private class FileUploadForm extends Form<Void> {

		FileUploadField fileUploadField;

		public FileUploadForm(String id)  {
			super(id);
			setMultiPart(true);
			add(fileUploadField = new FileUploadField("fileInput"));
			setMaxSize(Bytes.megabytes(20000));
			setFileMaxSize(Bytes.megabytes(20000));
			add(new UploadProgressBar("progress", this));
		}
		
        @Override
        protected void onSubmit() {
        	
            final List<FileUpload> uploads = fileUploadField.getFileUploads();
            
            if (uploads != null) {
                for (FileUpload upload : uploads) {
                    File newFile = new File(getUploadFolder(), upload.getClientFileName());
                   
                    boolean newfile = !newFile.exists();
            		if (!newfile) {
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
                        
                        DMUploadPanel.this.info((newfile?"uploaded":"replaced")+ " file: " + upload.getClientFileName());
                    }
                    catch (Exception e) {
                    	logger.error(e);
                        throw new IllegalStateException("Unable to write file", e);
                    }
                }
            }
        }
    }
	
	
	/**
	 * 
	 */
	public DMUploadPanel(String id, PageParameters parameters) {
		super(id);
		
		String folder_to_upload_path = parameters.get("directory").toOptionalString();
		String bc_path = parameters.get("bc-path").toOptionalString();
		
		//List<XAjaxLink> bc = new ArrayList<XAjaxLink>();
		//for (String str:bc_path.split("/")) {
		//	bc.add(new XAjaxLink());
		//}
		
		
		/**
		 * 
		 */
		setBreadCrumb(bc);
		this.folder =  folder_to_upload_path;
		
		add(new ListView<XAjaxLink>("directory", new ListModel<XAjaxLink>(new Model<Panel>(this), "breadcrumb")) {
			@Override
			protected void populateItem(ListItem<XAjaxLink> item) {
				XLink element = item.getModelObject();
				AbstractLink link = element.getLink("link");
				link.add(new Label("label", element.getLabel()));
				item.add(link);
				if (item.getIndex()==getBreadcrumb().size()-1)
					item.add(new AttributeModifier("class", "active"));
			}			
		});
		
        add(new FeedbackPanel("uploadFeedback"));
        add(new FileUploadForm("form"));
		
		
	}
	/**
	 * 
	 * 
	 * @param bc
	 * @param path

	public DMUploadPanel(List<XAjaxLink> bc, String folder_to_upload_path) {
		super("info-panel");
		
		setBreadCrumb(bc);
		this.folder =  folder_to_upload_path;
		
		add(new ListView<XAjaxLink>("directory", new ListModel<XAjaxLink>(new Model<Panel>(this), "breadcrumb")) {
			@Override
			protected void populateItem(ListItem<XAjaxLink> item) {
				XLink element = item.getModelObject();
				AbstractLink link = element.getLink("link");
				link.add(new Label("label", element.getLabel()));
				item.add(link);
				if (item.getIndex()==getBreadcrumb().size()-1)
					item.add(new AttributeModifier("class", "active"));
			}			
		});
		
        add(new FeedbackPanel("uploadFeedback"));
        add(new FileUploadForm("form"));
	}

	 */	
	
	public List<XAjaxLink> getBreadcrumb() {
		return bc;
	}
	
	
	public void setBreadCrumb(List<XAjaxLink> breadcrumb) {
		bc = new ArrayList<XAjaxLink>();
		for (XAjaxLink link : breadcrumb) {
			bc.add(new XAjaxLink(link.getLabel(), link.getLocalPath()) {
				public void onClick(AjaxRequestTarget target) {
					DMFilesPanel explorer = new DMFilesPanel(); 
					explorer.setDirectory(getLocalPath());
					SystemDataManagementGeneralPage page=new SystemDataManagementGeneralPage("file-explorer");
					page.setDMFilesPanel(explorer);
					setResponsePage(page);
				}
			});
		}
	}
	
	@Override
	protected BCElement getPageBCElement() {
		return new BCElement(new Model<String>("Upload"));
	}
	
	private Folder getUploadFolder()  {
 		Folder uploadFolder = new Folder(folder);
        return uploadFolder;
	}
	
	public boolean isExecutable(File f) {
		if (!f.exists())
			return false;
		if (f.isDirectory())
			return false;
		String name = f.getName();
		return name.toLowerCase().matches("^.*\\.(cmd|bat|sh|deb|exe)$");
	}
	
	protected void setPath(String path) {
		
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
					//addTable();
					//target.add(DMFilesPanel.this);
				}
			});

		}
	}

}