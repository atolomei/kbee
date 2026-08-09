package kbee.web.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.resource.KBFile;
import com.novamens.file.PdfService;
import com.novamens.kbee.content.multidimensional.ClassificationDisplayNameExtractor;
import com.novamens.service.ServiceLocator;

public class KbeePdfViewer extends Panel {
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePdfViewer.class.getName());

	
	private static final long serialVersionUID = 1L;
	
	private IModel<KBFile> filemodel;
	private String text;
	
	public KbeePdfViewer(String id, IModel<KBFile> model) {
		super(id);
		setFile(model);
	}
	
	public KBFile getFile() {
		return filemodel.getObject();		
	}

	public void setFile(IModel<KBFile> model) {
		this.filemodel = model;
	}
	
	public String getText() {
		return text;		
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		try {
			File file = ServiceLocator.getService(PdfService.class).getHtml(String.valueOf(getFile().getId()), getFile().getInputStream());
		    String text = FileUtils.readFileToString(file, "UTF-8");
		    setText(text);
		    Label textlabel = new Label("text", ()->getText());
		    textlabel.setEscapeModelStrings(false);
		    add(textlabel);
		}
		catch (IOException e) {
			logger.error(e);
		}
	}
}
