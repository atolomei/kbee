package com.novamens.kbee.template;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.text.TextTemplate;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class KbeeSignatureModel implements TemplateDirectiveModel  {
	@SuppressWarnings("rawtypes")
	public void execute(Environment env, Map parameters, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
 			Writer out = env.getOut();
 			
 			String template =
 					
  			"<div>" +
 			"<#if signeddata.signature.image??>" +
			"	<img src=\"${signeddata.signature.image.url}\" style=\"max-height: 200px; max-width:200px; display:block; margin:auto;\"/>" +
 			"<#else>" +
 			"	<span wicket:id=\"icon\" style=\"width: 100%; display: inline-block;  text-align: center;  padding: 20px; font-size: 40px;\">"+
 			"		<i class=\"fa-solid fa-file-signature\"></i>"+
 			"	</span>"+
 			"</#if>"+
 			"<div style=\"text-align:center\">" +
 			"	<p>${signeddata.signature.user.lastfirstname}</p>"+
 			"	<p>${signeddata.date}</p>"+
  			"</div>" +
  			"</div>";
			
			KbeeObjectTemplateModel signedmodel = (KbeeObjectTemplateModel)parameters.get("data");
			
			KbeeEMailTemplateModel model = new KbeeEMailTemplateModel();
			model.setModel("signeddata", signedmodel);
			TextTemplate texttemplate = new KbeeTextTemplate(template);
			String text = texttemplate.process(model);
			
			out.write(text);
	  }
}
