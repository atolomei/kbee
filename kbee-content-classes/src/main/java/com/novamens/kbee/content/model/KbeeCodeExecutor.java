package com.novamens.kbee.content.model;

import java.util.ArrayList;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.novamens.content.base.Content;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.ModelSection;
import com.novamens.content.text.template.ContentTextTemplate;
import com.novamens.kbee.content.text.template.ContentVariableResolver;
import com.novamens.kbee.content.text.template.KbeeContentTextTemplate;
import com.novamens.util.KbeeRuntimeException;

import kbee.util.logging.Logger;

public class KbeeCodeExecutor {

	private ScriptEngine engine = null;
	
	private static Logger logger = Logger.getLogger(ContentVariableResolver.class.getName());
	
	public class MacroEvaluator {
		private Content content;
		public MacroEvaluator(Content content) {
			this.content = content;
		}
		public String evaluate(String macro) {
			try {
				ContentTextTemplate template = new KbeeContentTextTemplate(macro);
				String str=template.getText(new ContentVariableResolver(content));
				return (str!=null? str.replace("- _", ""):"");
				
			}
			catch (Exception e) {
				logger.error(e);
			}
			return null;
		}
	}
	
	public Object execute(String code, Content content) {
		try {
			setBindings(content);
			Object evaluation = getEngine().eval(code);
			return evaluation;
		}
		catch (ScriptException e) {
			throw new KbeeRuntimeException(e);
		}
	}
	
	public String validate(String code, ContentTemplate template) {
		try {
			setBindings(template);
			getEngine().eval(code);
			return null;
		}
		catch (ScriptException e) {
			return e.getMessage();
		}	
	}
	
	public static String GetHelpText(ContentTemplate template) {
		StringBuilder text = new StringBuilder();
		text.append("<div class=\"panel col-lg-12\">");
		text.append("<p class=\"text col-lg-12\">A script must write using JavaScript with these context variables:</p>");
		text.append("<ul class=\"col-lg-12 panel\" style=\"margin-top: 10px;\">");
		
		KbeeCodeExecutor executor = new KbeeCodeExecutor();
		
		String variableexample = null;
				
//		for (ModelSection section : template.getSections()) {
//			for (ModelElementTemplate elementtemplate : section.getStructure()) {
//				if (elementtemplate instanceof ClassifierTemplate) {
//					boolean multiple = ((ClassifierTemplate)elementtemplate).getMultiplicity().isMultiple();
//					text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
//					String variablename = executor.getName(elementtemplate);
//					text.append(variablename);
//					if (multiple) {
//						text.append("<span class=\"ago\"> ( String[] ) </span>");
//					}
//					else {
//						if (variableexample==null) variableexample = variablename;
//						text.append("<span class=\"ago\"> ( String ) </span>");
//					}	
//					text.append("</li>");
//				}
//				if (elementtemplate instanceof AttributeTemplate) {
//					boolean multiple = ((AttributeTemplate)elementtemplate).getMultiplicity()!=null ? 
//						((AttributeTemplate)elementtemplate).getMultiplicity().isMultiple() :
//						false;
//					AttributeType type =  ((AttributeTemplate)elementtemplate).getAttribute().getType();	
//					text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
//					text.append(executor.getName(elementtemplate));
//					if (multiple) {
//						text.append("<span class=\"ago\"> ( "+type.getLabel()+"[] ) </span>");
//					}
//					else {
//						text.append("<span class=\"ago\"> ( "+type.getLabel()+" ) </span>");
//					}	
//					text.append("</li>");
//				}			
//			}
//		}
		
		text.append("<li class=\"col-lg-12\" style=\"margin-top:20px;\"><span class=\"predicate\">");
		text.append("macro <span class=\"ago\"> (Macro Evaluator) </span>");
		text.append("</li>");
		
		text.append("<li class=\"col-lg-12\"><span class=\"predicate\">");
		text.append("content <span class=\"ago\"> (Content) </span>");
		text.append("</li>");
		
		text.append("</ul>");
		text.append("<p class=\"text col-lg-12\">Examples:</p>");
		text.append("<ul class=\"col-lg-12 panel\" style=\"margin-top: 10px;\">");
		text.append("<li class=\"col-lg-12\">");
		text.append("if ("+variableexample+"=='value') <br/>"+
				"&nbsp;&nbsp;'result1'; // 'result1' is the return value of the script <br/>"	+ 
				"else <br/>"	+ 
				"&nbsp;&nbsp;'result2'; // 'result2' is the return value of the script");
		text.append("</li>");
		text.append("<li class=\"col-lg-12\" style=\"margin-top: 10px;\">");
		text.append("if ("+variableexample+"=='value') { <br/>"+ 
				"&nbsp;&nbsp;macro.evaluate('$classifier:classifer1$ - $attribute:date:MM/dd/yy$')<br/>" + 
				"	} <br/>" + 
				"	else { <br/>" + 
				"&nbsp;&nbsp;macro.evaluate('$classifier:classifer2$ - $attribute:date:MM/dd/yy$')<br/>" + 
				"	} <br/>");
		text.append("</li>");
		text.append("</ul>");
		text.append("</div>");
		return text.toString();
	}

	private void setBindings(ContentTemplate contenttemplate) {
		Bindings bindings = getEngine().getBindings(ScriptContext.ENGINE_SCOPE);

		for (ModelSection section : contenttemplate.getSections()) {
			for (ModelElementTemplate template : section.getStructure()) {
				if (template instanceof ClassifierTemplate) {
					List<String> values = new ArrayList<String>();
					boolean multiple = ((ClassifierTemplate)template).getMultiplicity().isMultiple();
					values.add("value");
					if (multiple) {
						bindings.put(getName(template), values);
					}
					else {
						bindings.put(getName(template), values.get(0));
					}	
				}
				else
				if (template instanceof AttributeTemplate) {
					List<String> values = new ArrayList<String>();
					boolean multiple = ((AttributeTemplate)template).getMultiplicity()!=null ? 
							((AttributeTemplate)template).getMultiplicity().isMultiple() :
							false;
					values.add("value");
					if (multiple) {
						bindings.put(getName(template), values);
					}
					else {
						bindings.put(getName(template), values.get(0));
					}	
				}	
			}
		}
	}
	
	protected void setBindings(Content content) {
		Bindings bindings = getEngine().getBindings(ScriptContext.ENGINE_SCOPE);

//		for (ModelSection section : content.getContentTemplate().getSections()) {
//			for (ModelElementTemplate template : section.getStructure()) {
//				if (template instanceof ClassifierTemplate) {
//					List<String> values = new ArrayList<String>();
//					boolean multiple = ((ClassifierTemplate)template).getMultiplicity().isMultiple();
//					for (Classification classification :content.getClassification(((ClassifierTemplate)template).getClassifier())) {
//						DataSetMember value = classification.getDataSetMember();
//						if (value!=null) {
//							values.add(value.getStrValue());
//						}	
//					}	
//					if (!values.isEmpty()) {
//						if (multiple) {
//							bindings.put(getName(template), values);
//						}
//						else {
//							bindings.put(getName(template), values.get(0));
//						}	
//					}
//				}
//				else
//				if (template instanceof AttributeTemplate) {
//					List<String> values = new ArrayList<String>();
//					boolean multiple = ((AttributeTemplate)template).getMultiplicity()!=null ? 
//							((AttributeTemplate)template).getMultiplicity().isMultiple() :
//							false;
//					values = content.getAttributeValues(((AttributeTemplate)template).getAttribute());		
//					if (!values.isEmpty()) {
//						if (multiple) {
//							bindings.put(getName(template), values);
//						}
//						else {
//							bindings.put(getName(template), values.get(0));
//						}	
//					}
//				}	
//			}
//		}
		
		bindings.put("macro", new MacroEvaluator(content));
		bindings.put("content", content);

		//bindings.put("today", getToday());
	}	

	private String getName(ModelElementTemplate template) {
		String name = template.getElement().getAlias();
		
		if (name == null) {
			name = template.getElement().getName();
			name = name.toLowerCase().replaceAll("[°,¡!?¿:\\/\"-().\\s]", "")
				.replace("á", "a")
				.replace("é", "e")
				.replace("í", "i")
				.replace("ó", "o")
				.replace("ú", "o")
				.replace("ñ", "n")
				.trim();
		}
		
		name = name.toLowerCase();
		
		return name;
	}	
	
	protected ScriptEngine getEngine() {
		if (engine==null) {
			engine = new ScriptEngineManager().getEngineByName("JavaScript");
		}
		return engine;
	}
}
