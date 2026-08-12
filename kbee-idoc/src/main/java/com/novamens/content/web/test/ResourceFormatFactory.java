package com.novamens.content.web.test;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.model.ContentId;
import com.novamens.content.service.UrlService;
import com.novamens.dom.Domain;
import com.novamens.kbee.template.KbeeObjectTemplateModel;
import com.novamens.service.ApplicationServerService;
import com.novamens.service.ServiceLocator;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

import kbee.util.PropertiesFactory;
	
public class ResourceFormatFactory implements TemplateDirectiveModel  {
	

	@SuppressWarnings("rawtypes")
	public void execute(Environment env, Map parameters, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {

		Writer out = env.getOut();
		
		TemplateModel resourcemodel = (TemplateModel)parameters.get("resource");
		
		if (resourcemodel == null) {
			throw new TemplateException("resource not found!", env);
		}
		
		TemplateModel contentmodel = (TemplateModel)parameters.get("content");
		
		if (contentmodel == null) {
			throw new TemplateException("content not found!", env);
		}
		
		if (!(resourcemodel instanceof KbeeObjectTemplateModel) || !(((KbeeObjectTemplateModel)resourcemodel).getObject() instanceof Resource)) {
			throw new TemplateException("invalid resource type", env);
		}
		
		if (!(contentmodel instanceof KbeeObjectTemplateModel) || !(((KbeeObjectTemplateModel)contentmodel).getObject() instanceof Content)) {
			throw new TemplateException("invalid content type", env);
		}
		
		Resource resource = (Resource)((KbeeObjectTemplateModel)resourcemodel).getObject();
		Content content = (Content)((KbeeObjectTemplateModel)contentmodel).getObject();
		
		String uri = (new ContentId(content)).toString() +"/" + resource.getPath();
		
		String url = getServerUrl(content.getDomain()) + "/" + uri;
		
		out.write("<a href=\""+url+"\">"+resource.getTitle()+"</a>");
	}	
	
	protected String getServerUrl(Domain domain) {
		return domain.getService(UrlService.class).getServerUrl();
	}
}