package com.novamens.kbee.template;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.novamens.content.resource.KBFile;
import com.novamens.file.PdfService;
import com.novamens.service.ServiceLocator;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class KbeePdfModel implements TemplateDirectiveModel  {
	
	@SuppressWarnings("rawtypes")
	public void execute(Environment env, Map parameters, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
		InputStream inputStream = null;
		try {
			Writer out = env.getOut();
			KbeeObjectWrapperTemplateModel filemodel = (KbeeObjectWrapperTemplateModel)parameters.get("file");
			KBFile kbfile = (KBFile)filemodel.getObject();
			inputStream = kbfile.getInputStream();
			File file = ServiceLocator.getService(PdfService.class).getHtml(String.valueOf(kbfile.getId()), inputStream);
			String text = FileUtils.readFileToString(file, "UTF-8");
			
			out.write(text);
		}
		finally {
			if (inputStream!=null) {
				inputStream.close();
			}
		}
	}	
}
