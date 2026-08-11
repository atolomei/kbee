package com.novamens.content.web.deployManagement;


import com.novamens.content.entity.Person;
import com.novamens.content.web.integration.LocalFSQuery;
import com.novamens.indexer.query.ResultSet;
import com.novamens.util.KbeeFileUtils;
import com.novamens.wicket.markup.html.actions.*;
import com.novamens.wicket.markup.html.editor.ObjectEditor;

import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.model.ListModel;

import kbee.web.model.util.FileModel;
import kbee.web.resource.WebFileReference;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DeployManagementFormPanel extends ObjectEditor<Person> {

	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DeployManagementFormPanel.class.getName());
    private static final String kbeeHome = System.getProperty("user.dir");
    static private final SimpleDateFormat dateformat = new SimpleDateFormat("dd MMM HH:mm:ss");

    private String filePrefix;
    WebMarkupContainer backupsContainer = null;


    public DeployManagementFormPanel(String id) {
        super(id);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        
        setOutputMarkupId(true);

        backupsContainer = new WebMarkupContainer("backupsContainer");
        backupsContainer.setOutputMarkupId(true);

        try {
        	createBackupDirectoryIfNotExists();
        } catch (Exception e) {
        	logger.error(e);
        }
        
        //-----------------------------------------------------
        

        ListView<IModel<File>> logsview = new ListView<IModel<File>>("backup", new ListModel<IModel<File>>(new org.apache.wicket.model.Model<Panel>(this), "backups")) {
            private static final long serialVersionUID = 1L;

            public void populateItem(final ListItem<IModel<File>> item) {

                WebMarkupContainer loglink = new WebMarkupContainer("fileLink");

                WebFileReference fileReference = new WebFileReference(item.getModelObject().getObject());
                String fileUrl = RequestCycle.get().urlFor(fileReference, null).toString();
                loglink.add(new AttributeModifier("href", fileUrl));
                item.add(new MenuFragment("action", item.getModelObject()));
                loglink.add(new Label("name", item.getModelObject().getObject().getName()));

                String sizelabel;
                long size = item.getModelObject().getObject().length();
                if (size < 1024) {
                    sizelabel = size + " bytes";
                } else {
                    sizelabel = size / 1024 + " KB";
                }

                item.add(new Label("size", sizelabel));
                item.add(new Label("date", dateformat.format(item.getModelObject().getObject().lastModified())));

                item.add(loglink);
            }
        };

        backupsContainer.setOutputMarkupId(true);
        backupsContainer.add(logsview);
        add(backupsContainer);
        
        //-----------------------------------------------------

        com.novamens.wicket.markup.html.form.Form<?> form = new com.novamens.wicket.markup.html.form.Form<Void>("backupForm", Form.Disposition.VERTICAL);

        TextField<String> lclass = new TextField<String>("filePrefix", new PropertyModel<String>(this, "filePrefix"));

        form.add(lclass);

        Model<String> createBackupfeedbackModel = Model.of("");
        final Label createBackupfeedback = new Label("createBackupfeedback", createBackupfeedbackModel);
        createBackupfeedback.setOutputMarkupPlaceholderTag(true);
        createBackupfeedback.setOutputMarkupId(true);
        form.add(createBackupfeedback);


        String createBackupLabel = new StringResourceModel("createBackup", this).getString();
        form.add(new AjaxSubmitLink("createBackup", form) {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            protected void onSubmit(AjaxRequestTarget target) {
                super.onSubmit(target);
                try {
                    String backupType = "app";

                    String backupFile = kbeeHome + "/bin/backup.sh";

                    ProcessBuilder builder = new ProcessBuilder(backupFile, "--bkPrefix", filePrefix, backupType);
                    final String logFileName = String.format("logs/backup_%s.log", OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
                    File outputFile = new File(logFileName);
                    builder.redirectOutput(outputFile);
                    builder.redirectError(outputFile);
                    Process p = builder.start();
                    final int i = p.waitFor();
                    if (i != 0) throw new RuntimeException("Process exited with error");

                    createBackupfeedbackModel.setObject("Backup log creation at " + outputFile.getAbsolutePath());

                } catch (Exception e) {
                    logger.error(e, "Error executing backup script");
                    createBackupfeedbackModel.setObject("Error creating backup.");
                }
                target.add(createBackupfeedback);

                target.add(backupsContainer);
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                IAjaxCallListener listener = new IAjaxCallListener() {
                    @Override
                    public CharSequence getSuccessHandler(Component component) {
                        return null;
                    }

                    @Override
                    public CharSequence getPrecondition(Component component) {
                        return null;
                    }

                    @Override
                    public CharSequence getFailureHandler(Component component) {
                        return null;
                    }

                    @Override
                    public CharSequence getCompleteHandler(Component component) {
                        String s = "document.getElementById('" + component.getMarkupId() + "').innerHTML = '" + createBackupLabel + "'";
                        return s;
                    }

                    @Override
                    public CharSequence getBeforeSendHandler(Component component) {
                        return null;
                    }

                    @Override
                    public CharSequence getBeforeHandler(Component component) {
                        String s = "document.getElementById('" + component.getMarkupId() + "').innerHTML = '<span class=\"far fa-sync fa-spin fa-fw spinning\"></span> " + createBackupLabel + "'";
                        return s;
                    }

                    @Override
                    public CharSequence getAfterHandler(Component component) {
                        return null;
                    }

                    @Override
                    public CharSequence getDoneHandler(Component component) {
                        return null;
                    }

                    @Override
                    public CharSequence getInitHandler(Component component) {
                        return null;
                    }
                };
                attributes.getAjaxCallListeners().add(listener);
            }
        });
        add(form);
        //-----------------------------------------------------

        final RestartApplicationPanel restartApplicationPanel = new RestartApplicationPanel("restartApplicationPanel");
        add(restartApplicationPanel);

        add(new DeployDownloadPanel("downloadPanel", getBackupDirectory() + File.separator + "download"){
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public void onFileDownloaded(String filePath, AjaxRequestTarget target) {
                super.onFileDownloaded(filePath, target);
                restartApplicationPanel.update(target);
            }
        });

    }

    
    private void createBackupDirectoryIfNotExists() throws  IOException {
    	File directory = new File(getBackupDirectory());
        if (!directory.exists() || !directory.isDirectory()) {
	        try {
	        	logger.debug("create backup directory -> " + directory);
				KbeeFileUtils.forceMkdir(directory);
			}
			catch (java.io.IOException e) {
				logger.error(e);
				throw e;
			}
        }
    }


    private String getBackupDirectory() {
    	logger.debug("backup directory -> " + kbeeHome + File.separator + ".." + File.separator + "kbee_backups");
        return kbeeHome + File.separator + ".." + File.separator + "kbee_backups";
    }

    public List<IModel<File>> getBackups() {
        final LocalFSQuery localFSQuery = new LocalFSQuery();

        localFSQuery.setDirectory(new File(getBackupDirectory()));
        List<IModel<File>> backups = new ArrayList<>();
        final ResultSet rs = localFSQuery.execute();
        while (rs.hasNext()) {
            final File file = (File) rs.next().getObject();
            if (file.isFile())
                backups.add(new FileModel(file));
        }
        return backups;
    }

    public String getFilePrefix() {
        return filePrefix;
    }

    public void setFilePrefix(String filePrefix) {
        this.filePrefix = filePrefix;
    }

    private class MenuFragment extends Fragment {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private IModel<File> model;

        public MenuFragment(String id, IModel<File> model) {
            super(id, "menu-fragment", DeployManagementFormPanel.this);
            this.model = model;
            Serializable objid = String.valueOf(model.getObject().hashCode());
            WebMarkupContainer menulink = new WebMarkupContainer("menulink");
            menulink.add(new AttributeModifier("id", String.valueOf(objid)));
            add(menulink);
            Panel menupanel = getMenu(getFileModel());
            if (menupanel != null) {
                menupanel.add(new AttributeModifier("aria-labelledby", String.valueOf(objid)));
                add(menupanel);
            } else {
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

    protected Panel getMenu(IModel<File> model) {
        ContextMenuPanel<File> menu = new ContextMenuPanel<File>(model);
        menu.setOutputMarkupId(true);

        menu.addItem(new MenuItemFactory<File>() {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public AbstractMenuItemPanelV5<File> getItem(String id) {
                return new AjaxMenuItemPanelV5<File>(id) {

                    /**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
                    public String getTarget() {
                        return "_blank";
                    }

                    @Override
                    public void onClick(AjaxRequestTarget target) throws Exception {
                        final File file = this.getModelObject();
                        try {
	                        // file.delete();
	                        KbeeFileUtils.forceDelete(file);
	                        target.add(backupsContainer);
                        } catch (Exception e) {
                        	logger.error(e);
                        }
                    }

                    @Override
                    public String getLabel() {
                        return "Delete";
                    }
                };
            }
        });

        return menu;
    }


}
