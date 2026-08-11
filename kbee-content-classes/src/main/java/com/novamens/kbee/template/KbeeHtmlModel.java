package com.novamens.kbee.template;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Element;

import com.novamens.content.base.Content;
import com.novamens.content.model.ContentId;
import com.novamens.content.resource.KBFile;
import com.novamens.content.text.AncordResolver;
import com.novamens.content.text.ImageResolver;
import com.novamens.file.PdfService;
import com.novamens.kbee.content.text.KbeeText;
import com.novamens.service.ServiceLocator;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class KbeeHtmlModel implements TemplateDirectiveModel  {
	
	@SuppressWarnings("rawtypes")
	public void execute(Environment env, Map parameters, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
		InputStream inputStream = null;
		try {
			Writer out = env.getOut();
			KbeeAttributeTemplateModel textmodel = (KbeeAttributeTemplateModel)parameters.get("html");
			String text = textmodel.getValue();
			KbeeText ktext = new KbeeText(text);
			
			KbeeContentTemplateModel contentmodel = (KbeeContentTemplateModel)textmodel.getParentNode();
			
			String strvalue = ktext.getText(new AncordResolver() {
				@Override
				public Element resolve(Element ancord) {
					return ancord;
				}
			}, new ImageResolver() {
				@Override
				public Element resolve(Element image) {
					String src = image.getAttribute("src");
					ContentId contentId = new ContentId(contentmodel.getContent());
					if (!src.contains("resource"))
						src = "/resource/content/"+contentId.toString() +"/" + src;
					image.setAttribute("src", src);
					return image;
				}
			});
			
			
			out.write(strvalue);
		}
		finally {
			if (inputStream!=null) {
				inputStream.close();
			}
		}
	}	
	
}
