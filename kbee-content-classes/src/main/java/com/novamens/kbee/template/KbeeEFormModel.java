package com.novamens.kbee.template;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.util.Map;


import com.novamens.content.base.Content;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EIdentifiableForm;
import com.novamens.content.service.TokenService;
import com.novamens.content.service.UrlService;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class KbeeEFormModel implements TemplateDirectiveModel  {
	
	@SuppressWarnings("rawtypes")
	public void execute(Environment env, Map parameters, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
		Writer out = env.getOut();
		
		KbeeClassificableTemplateModel contentmodel = (KbeeClassificableTemplateModel)parameters.get("content");
		Content  content = (Content)contentmodel.getObject();
		
		TemplateModel namemodel = (TemplateModel)parameters.get("name");
		String eformname = namemodel.toString();
		EForm eform = getForm(content, eformname);
		
		String text = read(getUrl(content, eform));
		
		out.write(text);
	}
	
	private String getUrl(Content content, EForm form) {
		KbeeJson data = new KbeeJson();
		
		data.put("content", String.valueOf(content.getId()));
		data.put("form", String.valueOf(((EIdentifiableForm)form).getId()));
		data.put("user", getSessionUser().getName());
		
		String server = content.getService(UrlService.class).getServerUrl();
		
		return server + "/sharedform/" + ServiceLocator.getService(TokenService.class).getToken(data);
	}
	
	private EForm getForm(Content content, String name) {
		for (EForm eform : content.getContentTemplate().getForms()) {
			if (name.equals(eform.getName())) {
				return eform;
			}
		}
		return null;
	}
	
	private String read(String url) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader((new URL(url)).openStream()));
		String inputLine, text="";
		while ((inputLine = in.readLine()) != null) {
			text += inputLine + "\n";
		}
		in.close();
		return text;
	}
	
	private User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
