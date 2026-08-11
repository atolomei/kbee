package com.novamens.kbee.content.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.ExtractionRule;
import com.novamens.kbee.content.text.template.ContentVariableResolver;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.text.TextTemplate;


import kbee.util.logging.Logger;

@JsonTypeName("macro")
public class KbeeExtractionMacro implements ExtractionRule {
	
	private static Logger logger = Logger.getLogger(ContentVariableResolver.class.getName());

	private String macro;
	
	
	public KbeeExtractionMacro() {
	}
	
	public KbeeExtractionMacro(String macro) {
		this.macro = macro;
	}
	
	public String getMacro() {
		return this.macro;
	}
	
	public void setMarco(String macro) {
		this.macro = macro;
	}
	
	@JsonIgnore
	public Serializable extract(Classificable content) {
		try {
			if (getMacro()==null) return null;
			
			TextTemplate template = new KbeeTextTemplate(getMacro());

			String text = template.process(content);
			
			return text;
		}
		catch (Exception e) {
			logger.error(e);
		}
		return null;
	}
}