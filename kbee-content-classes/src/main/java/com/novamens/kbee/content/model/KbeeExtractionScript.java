package com.novamens.kbee.content.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.base.Content;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.ExtractionRule;

import kbee.util.logging.Logger;

@JsonTypeName("script")
public class KbeeExtractionScript implements ExtractionRule {
	
	private static Logger logger = Logger.getLogger(KbeeExtractionScript.class.getName());
	
	private String script;
	
	public String getScript() {
		return this.script;
	}
	
	public void setScript(String macro) {
		this.script = macro;
	}
	
	@JsonIgnore
	public Serializable extract(Classificable content) {
		try {
			KbeeCodeExecutor executor = new KbeeCodeExecutor();
			Object evaluation = executor.execute(getScript(), (Content)content);
			return (String)evaluation;
		}
		catch (Exception e) {
			logger.error(e);
		}
		return null;
	}
}