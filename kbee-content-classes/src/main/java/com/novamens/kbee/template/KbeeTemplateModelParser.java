package com.novamens.kbee.template;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.novamens.text.TemplateModelInfo;
import com.novamens.text.TemplateModelParser;

import kbee.util.logging.Logger;

public class KbeeTemplateModelParser extends TemplateModelParser {
	
	static private Logger logger = Logger.getLogger(KbeeTemplateModelParser.class.getName());
	
	static private ObjectMapper mapper = new ObjectMapper();
	static  {
		mapper.registerSubtypes(KbeeTemplateModelInfo.class);
	}
	
	//private ObjectMapper mapper;
	
	public String getJson(TemplateModelInfo model) {
		try {
			return getMapper().writeValueAsString(model);
		}
		catch (JsonProcessingException e) {
			logger.error(e);
			return null;
		}
		
	}
	
	public TemplateModelInfo getModel(String jsonvalue) {
		try {
			JsonNode json = getMapper().readTree(jsonvalue);
			KbeeTemplateModelInfo model = new KbeeTemplateModelInfo();
			model.setName(json.get("name").asText()); 
			model.setType(TemplateModelInfo.ModelType.valueOf(json.get("type").asText())); 
			model.setDescription(json.get("description").asText()); 
			ArrayNode elements = (ArrayNode)json.get("elements");
			for (int i=0; i<elements.size(); i++) {
				JsonNode jsonelement = elements.get(i);
				KbeeTemplateModelInfo element = new KbeeTemplateModelInfo();
				element.setName(jsonelement.get("name").asText()); 
				element.setType(TemplateModelInfo.ModelType.valueOf(jsonelement.get("type").asText())); 
				element.setDescription(jsonelement.get("description").isEmpty() ? null : jsonelement.get("description").asText());
				model.add(element);
			}
			return model;
		}	
		catch (IOException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}
	
	private ObjectMapper getMapper() {
		//if (mapper==null) {
		//	mapper = new ObjectMapper();
			// mapper.registerSubtypes(KbeeTemplateModelInfo.class);

		//}
		return mapper;
	}
}
