package com.novamens.content.web.deployManagement;


import com.novamens.content.web.integration.LocalFSQuery;
import com.novamens.indexer.query.ResultSet;
import com.novamens.logging.ApplicationRestartEvent;
import com.novamens.wicket.markup.html.form.BooleanField;

import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;

import kbee.web.model.util.FileModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RestartApplicationPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(RestartApplicationPanel.class.getName());
	
    private static final String kbeeHome = System.getProperty("user.dir");
    static private Logger txlogger = LogManager.getLogger("TxLogger");

    ChoiceField<File> fallbackBackupSelector;
    ChoiceField<File> deploySelector;

    public RestartApplicationPanel(String id) {
        super(id);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        //-----------------------------------------------------
        com.novamens.wicket.markup.html.form.Form<?> formDeploy = new com.novamens.wicket.markup.html.form.Form<Void>("restartForm", Form.Disposition.VERTICAL);

        final Model<Boolean> doDeploy = new Model<>(false);

        IModel<File> deployFile = new FileModel(null);
        final List<File> deploys = getDeploys().stream().map(b -> b.getObject()).collect(Collectors.toList());
        deploySelector = new ChoiceField<>("deploySelector", deployFile, () -> deploys);
        deploySelector.setEnabled(false);
        formDeploy.add(deploySelector);

        formDeploy.add(new BooleanField("deployCheck", doDeploy){
			private static final long serialVersionUID = 1L;
			@Override
            protected String getFalseStr() {
                return "No";
            }

            @Override
            protected String getTrueStr() {
                return "Yes";
            }

            @Override
            public void onUpdate(AjaxRequestTarget target) {
                super.onUpdate(target);
                deploySelector.setEnabled(getValue());
                target.add(deploySelector);
            }
        });


        final Model<Boolean> doFallback = new Model<>(false);

        IModel<File> backupFile = new FileModel(null);
        final List<File> backups = getBackups().stream().map(b -> b.getObject()).collect(Collectors.toList());

        fallbackBackupSelector = new ChoiceField<>("fallbackBackupSelector", backupFile, () -> backups);
        fallbackBackupSelector.setEnabled(false);
        formDeploy.add(fallbackBackupSelector);

        formDeploy.add(new BooleanField("fallbackCheck", doFallback){
			private static final long serialVersionUID = 1L;
			@Override
            protected String getFalseStr() {
                return "No";
            }

            @Override
            protected String getTrueStr() {
                return "Yes";
            }

            @Override
            public void onUpdate(AjaxRequestTarget target) {
                super.onUpdate(target);
                fallbackBackupSelector.setEnabled(getValue());
                target.add(fallbackBackupSelector);
            }
        });


        Model<String> restartfeedbackModel = Model.of("");
        final Label restartBackupfeedback = new Label("restartfeedback", restartfeedbackModel);
        restartBackupfeedback.setOutputMarkupPlaceholderTag(true);
        restartBackupfeedback.setOutputMarkupId(true);
        formDeploy.add(restartBackupfeedback);

        String restartLabel = new StringResourceModel("restart", this).getString();
        
        formDeploy.add(new AjaxSubmitLink("restart", formDeploy) {
			private static final long serialVersionUID = 1L;

			@Override
            protected void onSubmit(AjaxRequestTarget target) {
                try {
                    String restartScriptFile = kbeeHome + "/bin/restart.sh";
                    List<String> cmd = new ArrayList<>();
                    cmd.add(restartScriptFile);
                    if(doDeploy.getObject()){
                        cmd.add("--deployFile");
                        cmd.add(deployFile.getObject().getAbsolutePath());
                    }
                    if(doFallback.getObject()){
                        cmd.add("--fallbackFile");
                        cmd.add(backupFile.getObject().getAbsolutePath());
                    }

                    ProcessBuilder builder = new ProcessBuilder(cmd);
                    final String logFileName = String.format("logs/restart_%s.log", OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
                    File outputFile = new File(logFileName);
                    builder.redirectOutput(outputFile);
                    builder.redirectError(outputFile);
                    txlogger.info(new ApplicationRestartEvent("Deploy Manager restart request"));

                    Process p = builder.start();
                    final int i = p.waitFor();
                    if (i != 0) throw new RuntimeException("Process exited with error");

                    restartfeedbackModel.setObject("Backup log creation at " + outputFile.getAbsolutePath());

                } catch (Exception e) {
                    logger.error(e, "Error executing backup script");
                    restartfeedbackModel.setObject("Error creating backup.");
                }
            }
        });
        add(formDeploy);

        WebMarkupContainer deployMsgContainer= new WebMarkupContainer("deployMsgContainer");
        deployMsgContainer.setOutputMarkupId(true);
        fillDeployMsgValidator(deployMsgContainer);
        formDeploy.add(deployMsgContainer);

    }

    private void fillDeployMsgValidator(WebMarkupContainer deployMsgContainer){
        File deployControlFile = new File(getDeployControlFile());
        
        
		@SuppressWarnings("serial")
		AjaxLink<String> markDeployOK = new AjaxLink<String>("markDeployOK") {
			
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                deployControlFile.delete();
                fillDeployMsgValidator(deployMsgContainer);
                ajaxRequestTarget.add(deployMsgContainer);
            }
        };
        deployMsgContainer.addOrReplace(markDeployOK);

        String validationText;
        if(deployControlFile.exists()){
            validationText = "<span class=\"alert alert-info\">There is a deploy waiting to be marked as valid.</span>";
        }else{
            validationText = "<span class=\"alert alert-info\">No deploy is waiting to be marked as valid.</span>";
            markDeployOK.setVisible(false);
        }
        Label deployValidationMessage = new Label("deployValidationMessage", validationText);
        deployValidationMessage.setEscapeModelStrings(false);
        deployMsgContainer.addOrReplace(deployValidationMessage);

    }


    private String getDeployControlFile() {
        return kbeeHome + File.separator + "deployControl";
    }
    private String getBackupDirectory() {
        return kbeeHome + File.separator + ".." + File.separator + "kbee_backups";
    }
    private String getDeploysDirectory() {
        return kbeeHome + File.separator + ".." + File.separator + "kbee_backups" + File.separator + "download";
    }

    public void update(AjaxRequestTarget target){
        target.add(this.deploySelector);
        target.add(this.fallbackBackupSelector);
    }

    public List<IModel<File>> getBackups() {
        final LocalFSQuery localFSQuery = new LocalFSQuery();
        final File dir = new File(getBackupDirectory());
        if(!dir.exists())
            return new ArrayList<>();
        localFSQuery.setDirectory(dir);
        List<IModel<File>> backups = new ArrayList<>();
        final ResultSet rs = localFSQuery.execute();
        while (rs.hasNext()) {
            final File file = (File) rs.next().getObject();
            if (file.isFile())
                backups.add(new FileModel(file));
        }
        return backups;
    }

    public List<IModel<File>> getDeploys() {
        final LocalFSQuery localFSQuery = new LocalFSQuery();
        final File dir = new File(getDeploysDirectory());
        if(!dir.exists())
            return new ArrayList<>();
        localFSQuery.setDirectory(dir);
        List<IModel<File>> deploys = new ArrayList<>();
        final ResultSet rs = localFSQuery.execute();
        while (rs.hasNext()) {
            final File file = (File) rs.next().getObject();
            if (file.isFile())
                deploys.add(new FileModel(file));
        }
        return deploys;
    }
}
