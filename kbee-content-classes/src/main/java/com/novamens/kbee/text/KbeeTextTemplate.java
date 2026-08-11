package com.novamens.kbee.text;


import java.io.IOException;
import java.io.StringWriter;

import com.novamens.content.base.Content;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.PersonMember;
import com.novamens.content.text.template.ContentTextTemplate;
import com.novamens.kbee.content.text.template.ContentVariableResolver;
import com.novamens.kbee.content.text.template.KbeeContentTextTemplate;
import com.novamens.kbee.template.KbeeContentTemplateModel;
import com.novamens.kbee.template.KbeeEMailTemplateModel;
import com.novamens.kbee.template.KbeeObjectTemplateModel;
import com.novamens.kbee.template.KbeeUserTemplateModel;
import com.novamens.kbee.template.KbeeValueTemplateModel;
import com.novamens.text.TemplateException;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

public class KbeeTextTemplate implements com.novamens.text.TextTemplate  {
	

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeTextTemplate.class.getName());
	private static Configuration cfg;
	
	
	private String template;
	
	
	class MyTemplateExceptionHandler implements TemplateExceptionHandler {
		public void handleTemplateException(freemarker.template.TemplateException te, Environment env, java.io.Writer out)
			throws freemarker.template.TemplateException {
				try {
					out.write("");
				} 
				catch (IOException e) {
					logger.error(e);
					throw new freemarker.template.TemplateException("Failed to print error message. Cause: " + e, env);
				}
		}
	}
	
	public KbeeTextTemplate(String text) {
		setTemplate(text);
	}
	
	public String process(Object model) throws TemplateException {
		try {
			
			Object modelwrapper = getModelWrapper(model);
			escape(modelwrapper);
			String templatetext = getTemplate();
			Template template = new Template("template", templatetext, getConfiguration());
			template.setTemplateExceptionHandler(getExceptionHandler());
			StringWriter out = new StringWriter();
			template.process(modelwrapper, out);
			String text = out.toString();
			
			if (model instanceof Content) {
				ContentTextTemplate texttemplate = new KbeeContentTextTemplate(text);
				text = texttemplate.getText(new ContentVariableResolver((Content)model));
			}
			
			return text;
		}
		catch (freemarker.template.TemplateException | IOException e) {
			logger.error(e);
			throw new TemplateException(e);
		}
	}
	
	public void  setTemplate(String template) {
		this.template = template;
	}
	
	public String getTemplate() {
		return template;
	}
	
	public Configuration getConfiguration() {
		if (cfg==null) {
			cfg = new Configuration(Configuration.VERSION_2_3_29);
			cfg.setDefaultEncoding("UTF-8");
			cfg.setLogTemplateExceptions(false);
			cfg.setWrapUncheckedExceptions(true);
			cfg.setFallbackOnNullLoopVariable(false);
			cfg.setTemplateExceptionHandler(new MyTemplateExceptionHandler());
			cfg.setNumberFormat("computer");
		}
		return cfg;
	}
	
	protected void escape(Object model) {
		if (model instanceof KbeeObjectTemplateModel) {
			String template = ((KbeeObjectTemplateModel)model).escape(getTemplate());
			setTemplate(template);
		}
//		else {
//			logger.error("model is " + model.getClass().getName() +" | expected -> " + KbeeEMailTemplateModel.class.getName());
//		}
	}
	
	protected Object getModelWrapper() {
		return null;
	}
	
	protected TemplateExceptionHandler getExceptionHandler() {
		return new MyTemplateExceptionHandler();
	}
	
	protected Object getModelWrapper(Object model) {
		Object wrapper = model;
		
		if (model instanceof PersonMember) {
			wrapper = new KbeeUserTemplateModel((DataSetMember)model);
		}
		else if (model instanceof DataSetMember) {
			wrapper = new KbeeValueTemplateModel((DataSetMember)model);
		}
		else if (model instanceof Content) {
			wrapper = new KbeeContentTemplateModel((Content)model);
		}
		else if (model instanceof KbeeEMailTemplateModel) {
			wrapper = model;
		}
		else if (model instanceof KbeeContentTemplateModel) {
			wrapper = model;
		}
		else {
			logger.error("model is " + model.getClass().getName() +" not supported ");
		}
		
		return wrapper;
	}
}