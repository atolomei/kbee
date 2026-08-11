package com.novamens.content.web.deployManagement;

import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;

import kbee.util.PropertiesFactory;

import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class DeployDownloadPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	private boolean isDownloading = false;
    private double downloadProgress;
    private String downloadingFilePath = null;
    private AbstractAjaxTimerBehavior downloadProgressTimer;
    private String downloadDirectory;


    public DeployDownloadPanel(String id, String downloadDirectory) {
        super(id);

        this.downloadDirectory = downloadDirectory;
    }
    

    static private final String snapshot_jenkins_url = PropertiesFactory.getInstance("kbee").getProperties().getProperty("snapshot_jenkins_url", "https://dev.novamens.com/view/Multibranch%20(snapshot)/job/kbee-webapp/job/snapshot/lastSuccessfulBuild/artifact/target/app.zip").trim();
    static private final String release_jenkins_server = PropertiesFactory.getInstance("kbee").getProperties().getProperty("release_jenkins_server", "https://dev.novamens.com/view/multibranch/job/kbee-webapp/job").trim();
    						

    @Override
    protected void onInitialize() {
        super.onInitialize();

        com.novamens.wicket.markup.html.form.Form<?> downloadForm = new com.novamens.wicket.markup.html.form.Form<Void>("downloadForm", Form.Disposition.VERTICAL);


        Map<String, String> branches = new HashMap<>();
        
        
        branches.put("Snapshot", snapshot_jenkins_url );
                          
        // 
        
        
        branches.put("Release6.1",   "/release6.1/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release6.2", release_jenkins_server + "/release6.2/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release6.3", release_jenkins_server + "/release6.3/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release6.3.1",  release_jenkins_server + "/release6.3.1/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release6.4",  release_jenkins_server + "/release6.4/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release6.5",  release_jenkins_server + "/release6.5/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release6.6",  release_jenkins_server + "/release6.6/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release6.7",  release_jenkins_server + "/release6.7/lastSuccessfulBuild/artifact/target/app.zip");
        
        branches.put("Release7.0",  release_jenkins_server + "/release7.0/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release7.1",  release_jenkins_server + "/release7.1/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release7.2",  release_jenkins_server + "/release7.2/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release7.3",  release_jenkins_server + "/release7.3/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release7.4",  release_jenkins_server + "/release7.4/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release7.5",  release_jenkins_server + "/release7.5/lastSuccessfulBuild/artifact/target/app.zip");
        
        
      
        
        branches.put("Release8.0",  release_jenkins_server + "/release8.0/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release8.1", release_jenkins_server + "/release8.1/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release8.2", release_jenkins_server + "/release8.2/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release8.3", release_jenkins_server + "/release8.3/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release8.4", release_jenkins_server + "/release8.4/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release8.5", release_jenkins_server + "/release8.5/lastSuccessfulBuild/artifact/target/app.zip");
        
        
        branches.put("Release9.0", release_jenkins_server + "/release9.0/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release9.1", release_jenkins_server + "/release9.1/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release9.2", release_jenkins_server + "/release9.2/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release9.3", release_jenkins_server + "/release9.3/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release9.4", release_jenkins_server + "/release9.4/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release9.5", release_jenkins_server + "/release9.5/lastSuccessfulBuild/artifact/target/app.zip");


        
        branches.put("Release10.0", release_jenkins_server + "/release10.0/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release10.1", release_jenkins_server + "/release10.1/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release10.2", release_jenkins_server + "/release10.2/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release10.3", release_jenkins_server + "/release10.3/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release10.4", release_jenkins_server + "/release10.4/lastSuccessfulBuild/artifact/target/app.zip");
        branches.put("Release10.5", release_jenkins_server + "/release10.5/lastSuccessfulBuild/artifact/target/app.zip");
        

        IModel<String> branchModel = Model.of(branches.keySet().stream().findFirst().get());
        downloadForm.add(new ChoiceField<String>("branchSelector", branchModel, () -> new ArrayList(branches.keySet())));

        Model<String> downloadFeedbackModel = Model.of("");
        final Label downloadFeedback = new Label("downloadFeedback", downloadFeedbackModel);
        downloadFeedback.setOutputMarkupPlaceholderTag(true);
        downloadFeedback.setOutputMarkupId(true);
        downloadForm.add(downloadFeedback);

        String downloadLabel = new StringResourceModel("download", this).getString();

        final AjaxSubmitLink download = new AjaxSubmitLink("download", downloadForm) {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            protected void onSubmit(AjaxRequestTarget target) {
                super.onSubmit(target);
                final String url = branches.get(branchModel.getObject());
                try {
                    String fileName = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + "_app.zip";

                    File directory = new File(downloadDirectory);
                    if (! directory.exists()){
                        directory.mkdir();
                    }
                    downloadingFilePath = downloadDirectory + File.separator + fileName;
                    downloadProgress=0d;

                    Thread t = new Thread(() -> {
                        try {
                            isDownloading=true;
                            download(url, downloadingFilePath, "jenkins", "jenktexyc0w#");
                        } catch (IOException e) {
                            downloadFeedbackModel.setObject(e.toString());
                        }finally {
                            isDownloading=false;
                        }
                    });
                    downloadProgressTimer.restart(target);
                    t.start();
                    downloadFeedbackModel.setObject("Saving file \"" + downloadingFilePath +"\"");
                    target.add(downloadFeedback);
                    this.setEnabled(false);
                    target.add(this);
                } catch (Exception e) {
                    downloadFeedbackModel.setObject(e.getMessage());
                    target.add(downloadFeedback);
                }
            }
        };
        downloadProgressTimer = new AbstractAjaxTimerBehavior(Duration.ofSeconds(1))
        {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;
            DecimalFormat df = new DecimalFormat("#.#");
            @Override
            protected void onTimer(AjaxRequestTarget ajaxRequestTarget) {
                String js;
                if(isDownloading){
                    js = "document.getElementById('" + download.getMarkupId() + "').innerHTML = '<span class=\"far fa-sync fa-spin fa-fw spinning\"></span> Downloading(" + df.format(downloadProgress*100) + "%)'";
                }else{
                    js = "document.getElementById('" + download.getMarkupId() + "').innerHTML = '" + downloadLabel + "'";
                    download.setEnabled(true);
                    ajaxRequestTarget.add(download);
                    this.stop(ajaxRequestTarget);

                    if(downloadProgress == 1d){
                        downloadFeedbackModel.setObject("Download complete. File saved to \"" + downloadingFilePath +"\".");
                        onFileDownloaded(downloadingFilePath, ajaxRequestTarget);
                    }
                    ajaxRequestTarget.add(downloadFeedback);
                }
                ajaxRequestTarget.appendJavaScript(js);
            }
        };

        download.setOutputMarkupId(true);
        downloadForm.add(downloadProgressTimer);
        downloadForm.add(download);
        add(downloadForm);
    }

    public void onFileDownloaded(String filePath, AjaxRequestTarget target){

    }


    public void download(String sourceURL, String outputFile, String username, String password) throws IOException {
        URL url = new URL(sourceURL);
        final File file = new File(outputFile);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        String userpass = username + ":" + password;
        String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
        con.setRequestProperty("Authorization", basicAuth);
        final double contentLength = con.getContentLength();
        int downloaded = 0;
        try (BufferedInputStream in = new BufferedInputStream(con.getInputStream());
             FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
                downloaded += bytesRead;
                this.downloadProgress = downloaded /contentLength ;
            }
        }
    }
}
