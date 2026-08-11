package kbee.web.security.user;

import com.novamens.content.command.Command;
import com.novamens.content.resource.KBFile;
import com.novamens.kbee.bulkImport.BulkImportCommand;
import com.novamens.kbee.bulkImport.ExcelBulkImporter;
import com.novamens.kbee.bulkImport.RowEntityLoader;
import com.novamens.kbee.command.CommandService;
import com.novamens.kbee.wicket.markup.html.console.panel.AJAXDownload;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeFileUtils;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.panel.KBPanel;

import kbee.web.command.CommandStatusPanelV5;
import kbee.web.command.panel.CommandModel;
import kbee.web.form.FileUploadField;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import java.io.*;

@SuppressWarnings("serial")
public abstract class BulkCreationPanel extends KBPanel {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(BulkCreationPanel.class.getName());
	
	String working_dir = ServiceLocator.getService(ApplicationServerService.class).getDataExportDir() + File.separator + "upload";
    
    AJAXDownload download;
//    private Serializable command_id;
    
    IModel<KBFile> filemodel;

    public BulkCreationPanel(String id) {
        super(id);
    }

	@Override
    protected void onInitialize() {
        super.onInitialize();

        download = new AJAXDownload();
        this.add(download);
        AjaxLink<Void> ajaxLink = new AjaxLink<Void>("downloadTemplateBtn") {
			@Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                File dir = new File(working_dir);
                try {

                	//String filename = UUID.randomUUID().toString() + ".xlsx";
                	
                    if (!dir.exists() || !dir.isDirectory()) {
                        KbeeFileUtils.forceMkdir(new File(working_dir));
                    }
                    File file = File.createTempFile("template_", ".xlsx");
                    file.deleteOnExit();
                    ExcelBulkImporter excelBulkImport = new ExcelBulkImporter(getRowLoader());

                    try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                        excelBulkImport.downloadTemplate(out);
                    }

                    download.setFile(file);
                    download.initiate(ajaxRequestTarget);
                } 
                catch (Throwable e) {
                	e.printStackTrace();
                	logger.error(e);
                }
            }
        };

        this.add(ajaxLink);

        Form<?> form = new Form<Void>("form", Form.Disposition.VERTICAL);
        
        FileUploadField importFileField = new FileUploadField("importFile") {
 			@Override
            public void onUpdate(AjaxRequestTarget target) {
                this.setModel(this.getModel(this.getValue()));
            }
        };
        
        form.add(importFileField);
        form.add(new AjaxSubmitLink("submit") {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            protected void onSubmit(AjaxRequestTarget target) {
				try {
	                KBFile file = importFileField.getValue();
	                ExcelBulkImporter excelBulkImport = new ExcelBulkImporter(getRowLoader());
	
	                BulkImportCommand bulkImportCommand = new BulkImportCommand(excelBulkImport, file);
	                getCommandService().add(bulkImportCommand);
	
	                final Panel runningCommand = getRunningCommand(bulkImportCommand);
	                BulkCreationPanel.this.replace(runningCommand);
	                target.add(runningCommand);
				} catch (Exception e) {
					logger.error(e);
				}
                
            }

            @Override
            public boolean isEnabled() {
                return !ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
            }
        });
        this.add(form);

        MarkupContainer commandPanelContainer = null;

        if(commandPanelContainer == null) {
            commandPanelContainer= new WebMarkupContainer("commandPanel");
            commandPanelContainer.setOutputMarkupId(true);
            commandPanelContainer.setOutputMarkupPlaceholderTag(true);
            commandPanelContainer.setVisible(false);
        }
        this.add(commandPanelContainer);
    }

    public abstract RowEntityLoader getRowLoader();
    

    private Panel getRunningCommand(Command cmd){
       // this.command_id = cmd.getId();

        CommandStatusPanelV5 panel = new CommandStatusPanelV5("commandPanel", new CommandModel(ServiceLocator.getService(CommandService.class).getCommand(cmd.getId()))) {
            private static final long serialVersionUID = 1L;
            @Override
            public void onAfterExecution(AjaxRequestTarget target) {
                target.add(BulkCreationPanel.this);
                //command_id = null;
            }
        };
        panel.setOutputMarkupId(true);
        return panel;

    }

//    protected Domain getDomain() {
//        return ServiceLocator.getService(UserService.class).getDomain();
//    }

    private CommandService getCommandService() {
        try {
            return (CommandService) ServiceLocator.getService(CommandService.class);
        } 
        catch (Exception e) {
            throw new KbeeRuntimeException(e);
        }

    }

//    protected User getSessionUser() {
//        return ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
//    }

}
