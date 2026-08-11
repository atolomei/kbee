package com.novamens.kbee.content.model;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novamens.content.model.ExtractionRule;
import com.novamens.kbee.content.form.KbeeEFormParser;

import kbee.util.logging.Logger;

public  class KbeeExtractionRuleParser extends ExtractionRuleParser  {
	
	private static Logger logger = Logger.getLogger(KbeeEFormParser.class.getName());

	private static ObjectMapper mapper = new ObjectMapper();

	static  {
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.registerSubtypes(	KbeeExtractionMacro.class,
									KbeeExtractionScript.class);
		
	}
	
	public  String getJson(ExtractionRule rule) {
		try {
			String json = getMapper().writeValueAsString(rule);
			return json;
		}
		catch (JsonProcessingException e) {
			logger.error(e);
			return null;
		}
	}
	
	public ExtractionRule getRule(String json) {
		
		if (json==null || "".equals(json)) {
			return new KbeeDefaultTitleRule();
		}
		
		if (!json.startsWith("{")) {
			return new KbeeExtractionMacro(json);
		}
		
		try {
			ExtractionRule rule = getMapper().readValue(json, ExtractionRule.class);
			return rule;
		}	
		catch (IOException e) {
			logger.error(e);
			return null;
		}
	}
	
	private ObjectMapper getMapper() {
		return mapper;
	}
}
