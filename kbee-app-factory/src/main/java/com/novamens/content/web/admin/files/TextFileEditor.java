package com.novamens.content.web.admin.files;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.security.User;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeFileUtils;

import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

import com.novamens.wicket.markup.html.form.TextAreaField;

public class TextFileEditor extends ObjectEditor<File> {
			
	private static final long serialVersionUID = 1L;

	Boolean  is_domain_kbee = null;

	int BUFFER_SIZE = 8192;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TextFileEditor.class.getName());

	
	
	
	public String text;
	
	public TextFileEditor(String id, IModel<File> model) {
		super(id, model);
	}
	
	
	

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
		setEditionEnabled(false);

		getFileText();
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new Label("filename",  getModel().getObject().getName()));

		form.add(new TextAreaField<String>("text", new PropertyModel<String>(this, "text"), 30, 40));

		form.add(new EditButtonsV5<File>(this) {
			
			private static final long serialVersionUID = 1L;
			@Override
			public boolean getDisableAfterSubmit() {
				return true;
			}
			@Override
			protected IModel<String> getSubmitLabel() {
				return new Model<String>("save");
			}
			
			@Override
			protected String getSubmitClass() {
				return "btn btn-primary btn-sm";
			}
		});
		
		add(form);
	}

	/**
	 * 
	 * 
	 */
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			saveFile(); 
			target.add(TextFileEditor.this);
		} 
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent(target, e));
		}

	}
	
	/**
	 * 
	 * 
	 * 
	 */
	protected void saveFile() {
		
 		BufferedWriter out = null;
		String nf = getTempDir() + File.separator + getModel().getObject().getName();
		try {
			out = new BufferedWriter(new FileWriter(new File(nf)));
			
			String tx=getText();
			if (tx!=null) {
				if (isLinux())
					tx=tx.replace("\r\n", "\n");
				out.write(tx);
			}
			 
		} catch (Exception e) {
			logger.error(e);
			return;
			
		} finally {
			if (out!=null)
				try {
					out.close();
				} catch (IOException e) {
					logger.error(e);
				}	
		}
		
		
		try {
			
			String path = getModel().getObject().getAbsolutePath();
			
			boolean isx, isr;
			
			isx = getModel().getObject().canExecute();
			isr = getModel().getObject().canRead();
			//isw = getModel().getObject().canWrite();
			
			deleteFile(getModel());
			
			FileUtils.moveFile(new File(nf), new File(path));
			
			// permissions
			File nfile = new File(path);
			
			
			try {
				nfile.setExecutable(isx, true);
				nfile.setReadable(isr, true);
				nfile.setWritable(true, true);
			} catch (Exception e) {
				logger.error(e);
			}
			
			getModel().setObject(new File(nf));
			 
		}
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	/**
	 *
	 * 
	 * 
	 * 
	 */
	protected void getFileText() {
		
		StringBuilder str = new StringBuilder();
		
		BufferedReader stream = null;
		
		char[] buf = new char[BUFFER_SIZE];
		int bytesRead;

		 try {
				stream = new BufferedReader(new FileReader(getModel().getObject()), BUFFER_SIZE);
				while ((bytesRead = stream.read(buf, 0, buf.length)) >= 0) {
					for (int n=0; n<bytesRead;  n++) {
						str.append(buf[n]);
					}
				}
				setText(str.toString());
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
					if (stream!=null)
						try {
							stream.close();
						} catch (IOException e) {
							logger.error(e); 
						}
			}
	}
	

	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}


	protected boolean isDomainKbee() {
		if (this.is_domain_kbee == null) {
			try {
				this.is_domain_kbee = Boolean.valueOf(
						getPerson().getDomain().getName().toLowerCase().trim().equals("kbee"));
			} catch (Exception e) {
				this.is_domain_kbee = Boolean.valueOf(false);
			}
		}
		return this.is_domain_kbee.booleanValue();
	}

	protected Person getPerson() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getPerson();
	}

	
	protected User getSessionUser() {
		try {
			return ServiceLocator.getService(SecurityService.class).getSessionUser();
		} catch (Exception e) {
			return null;
		}
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	
 	private String getTexFileEditorWorkDir() {
 		return ServiceLocator.getService(ApplicationServerService.class).getWorkDirAbsolutePath() +  File.separator + "texteditor";
 	}
 	

	public void setText(String text) {
		this.text=text;
	}
	
	public String getText() {
		return this.text;
	}

 	/**
 	 * @return
 	 */
 	private String getTempDir() {
 		
 		DateTimeFormatter workdf = DateTimeFormatter.ofPattern("YYYYMMdd");
 		
 		String dir = getTexFileEditorWorkDir()  + File.separator + workdf.format(LocalDateTime.now());

 		File base = new File(dir);
 		
 		if (!base.exists()) {
 			synchronized (this) {
	 			try {
					KbeeFileUtils.forceMkdir(base);
				} catch (IOException e) {
					logger.error(e);
				}
 			}
 		}
 		else if (!base.isDirectory()) {
 			synchronized (this) {
	 			KbeeFileUtils.deleteQuietly(base);
	 			try {
					KbeeFileUtils.forceMkdir(base);
				} catch (IOException e) {
					logger.error(e);
				}
 			}
 		}

 		return dir;
 	}

 	
 	/**
 	 * @param model
 	 */
	private void deleteFile(IModel<File> model) {
		try {
			DateTimeFormatter workdf = DateTimeFormatter.ofPattern("YYYYMMdd");
			File candidate = new File(getTexFileEditorWorkDir()  + File.separator + workdf.format(LocalDateTime.now()) + File.separator + model.getObject().getName());
			int n=0;
			while (candidate.exists())
				candidate = new File(getTexFileEditorWorkDir()  + File.separator + workdf.format(LocalDateTime.now()) + File.separator + model.getObject().getName()+String.valueOf(++n));
			File destfile = candidate;
			FileUtils.moveFile(model.getObject(), destfile);
			
		} catch (Exception e) {
			logger.error(e);
		}
		
	}

	private boolean isLinux() {
		if  (System.getenv("OS")!=null && System.getenv("OS").toLowerCase().contains("windows")) 
			return false;
		return true;
	}	

}
