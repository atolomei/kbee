package com.novamens.kbee.content.text.template;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.model.ContentId;
import com.novamens.content.text.template.Include;
import com.novamens.content.text.template.IncludeResolver;
import com.novamens.content.text.template.ContentTextTemplate;
import com.novamens.content.text.template.Variable;
import com.novamens.content.text.template.VariableResolver;

// *
// Variable: $type:name:formato$
// Type = date | user | field | classifier | attribute
//		date: Es la fecha del dia
//		field: se usa para definir un input field en el template
//		user: es el usuario de la session
//		classifier: es un clasificador del contenido donde se evalua el template
//
// Name = nombre del elemento clasificador o atributo
// Formato: opcional. Opciones 'capital', 'MMMMM dd, YYYY'
//
//
// Para type field la estructura es $field:field name:field type:formato$  
//		field type: 'Date', 'Text', 'String' o 'Template
//

public class KbeeContentTextTemplate implements ContentTextTemplate, Serializable {
															
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeContentTextTemplate.class.getName());

	
	private static final long serialVersionUID = 1L;
	private String title;
	private String id;
	private String text;
	
	public KbeeContentTextTemplate(OrganizationalText text) {
		
		if (text.getText()!=null)
			setText(text.getText().asString());
		
		if (text.getTitle()!=null)
			setTitle(text.getTitle());
		
		setId((new ContentId(text)).toString());
	}
	
	public KbeeContentTextTemplate(String text) {
		setText(text);
	}

	public String getText(VariableResolver resolver) {
		return getText(resolver, null);
	}
	
	public String getContentId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getText(VariableResolver variableResolver, IncludeResolver includeResolver) {
	
		StringBuilder text = new StringBuilder();
		
		String templatetext = getText();

		if (templatetext==null)
			return "";
					
		text = new StringBuilder();
		
		int i = 0;
		for (Variable variable : getVariables(templatetext)) {

			text.append(templatetext.substring(i, variable.getOffset()));

			String res = null;

			try {
			
				res = variableResolver.getValue(variable);
				
			} catch (Exception e) {
				res = null;
				logger.error(e);
			}
			
			if (res!=null)
				text.append(res);
			
			i = variable.getOffset() + variable.getLength();
		}
		text.append(templatetext.substring(i));
		
		templatetext = text.toString();
		text = new StringBuilder();
		
		i = 0;
		for (Include include : getIncludes(templatetext)) {
			String res = templatetext.substring(i, include.getOffset());
			
			if (res!=null)
				text.append(res);
			
			text.append(includeResolver.getInclude(include));
			i = include.getOffset() + include.getLength();
		}
		
		text.append(templatetext.substring(i));
		
		String string = text.toString();
		
		return string;
	}
	
	public List<Variable> getVariables() {
		return getVariables(getText()); 
	}
	
	private List<Variable> getVariables(String templatetext) {
		List<Variable> variables = new ArrayList<Variable>();
		int i = 0, f=0;
		while (i<templatetext.length() && f>=0) { 
			f = templatetext.indexOf("$", i);
			if (f>=0) {
				i = f+1;
				String text = templatetext.substring(f+1);
				Variable variable = parseVariable(text);
				if (variable!=null) {
					variable.setOffset(f);
					variables.add(variable);
					i = f+variable.getLength()+1;
				}
				else {
					Include include = parseInclude(text);
					if (include!=null) {
						i = f+include.getLength();
					}
				}
			}
		}
		
		Collections.sort(variables, new Comparator<Variable>() {
			@Override
			public int compare(Variable a, Variable b) {
				try {
					return a.getOffset()-b.getOffset();
				} catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}
		}); 
		
		return variables;
	}
	
	private List<Include> getIncludes(String templatetext) {
		List<Include> includes = new ArrayList<Include>();
		int i = 0, f=0;
		while (i<templatetext.length() && f>=0) {
			f = templatetext.indexOf("$include:", i);
			if (f>=0) {
				
				i = f+1;
//				int dp = templatetext.indexOf("$", f+1);
//				if (dp>f && dp-f<30) {
//					Include include = new Include();
//					String includename = templatetext.substring(f+1, dp);
//					include.setName(includename);
//					include.setOffset(f);
//					include.setLength(dp-f);
//					includes.add(include);
//					i = dp+1;
//				}
				
				
				Include include = parseInclude(templatetext.substring(f+1));
				if (include!=null) {
					include.setOffset(f);
					includes.add(include);
					i = f+include.getLength();
				}
				
			}
		}
		return includes;
	}
	
	private Variable parseVariable(String text) {
		
		String type = parseType(text);
		
		if ("attribute".equals(type)) {
			return parseAttribute(text);
		}
		
		if ("classifier".equals(type)) {
			return parseClassifier(text);
		}
		
		if ("field".equals(type)) {
			return parseField(text);
		}
		
		if ("date".equals(type)) {
			return parseDate(text);
		}
		
		if ("user".equals(type)) {
			return parseUserAttribute(text);
		}
		
		if ("file".equals(type)) {
			return parseFileAttribute(text);
		}
		
		if ("library".equals(type)) {
			return parseLibraryAttribute(text);
		}

		return null;
	}
	
	private Include parseInclude(String text) {
		Include include = new Include();
		if (!text.startsWith("include:")) return null;
		int end = text.indexOf("$");
		if (end<=0 || end>50) return null;
		String includename = text.substring(0, end);
		include.setName(includename);
		include.setLength(end+2);
		return include;
	}
	
	private Variable parseAttribute(String text) {
		String type = parseType(text) ;
		if (!"attribute".equals(type)|| !":".equals(text.substring(9, 10))) {
			return null;
		}
		int i1 = text.indexOf("$", 10);
		if (i1<0 || i1>50) {
			return null;
		}
		String attributename = null, format = null;
		int i2 = text.indexOf(":",10);
		if (i2<0 || i2>i1) {
			attributename = text.substring(10, i1);
		}
		else {
			attributename = text.substring(10, i2);
			if (i2>0 && i2<i1) {
				format = text.substring(i2+1, i1);
			}
		}
		Variable variable = new Variable();
		variable.setName(attributename);
		variable.setType(type);
		variable.setFormat(format);
		variable.setLength(i1+2);
		return variable;
	}
	
	private Variable parseClassifier(String text) {
		String type = parseType(text);
		if (!"classifier".equals(type)|| !":".equals(text.substring(10, 11))) {
			return null;
		}
		int i1 = text.indexOf("$", 11);
		if (i1<0 || i1>50) {
			return null;
		}
		String classifiername = null, format = null, attributename = null;
		int i2 = text.indexOf(":",11);
		if (i2<0 || i2>i1) {
			classifiername = text.substring(11, i1);
		}
		else {
			classifiername = text.substring(11, i2);
			if (i2>0 && i2<i1) {
				format = text.substring(i2+1, i1);
			}
		}
		if (classifiername.contains(".")) {
			String path = classifiername;
			int i3 = path.indexOf(".");
			attributename = path.substring(i3+1);
			if (!"".equals(attributename))
			classifiername = path.substring(0, i3);
			else
			attributename = null;	
		}
		
		Variable variable = new Variable();
		variable.setName(classifiername);
		variable.setAttribute(attributename);
		variable.setType(type);
		variable.setFormat(format);
		variable.setLength(i1+2);
		return variable;
	}
						
	private Variable parseFileAttribute(String text) {
		String type = parseType(text);
		if (!text.startsWith("file:")) {
			return null;
		}
		if (type==null)
			return null;
		int i1 = text.indexOf("$", 5);
		if (i1<0 || i1>50) {
			return null;
		}
		String attributename = null;
		attributename = text.substring(5, i1);
		Variable variable = new Variable();
		variable.setName(attributename);
		variable.setType("file");
		variable.setLength(i1+2);
		return variable;
	}
	
//	private Variable parseOid(String text) {
//		String type = parseType(text);
//		if (!"file-oid".equals(type)) {
//			return null;
//		}
//		Variable variable = new Variable();
//		variable.setName("file-oid");
//		variable.setType("file-oid");
//		variable.setLength(10);
//		return variable;
//	}
	
	private Variable parseLibraryAttribute(String text) {
		String type = parseType(text);
		if (!text.startsWith("library:")) {
			return null;
		}
		if (type==null)
			return null;
		int i1 = text.indexOf("$", 8);
		if (i1<0 || i1>50) {
			return null;
		}
		String attributename = null;
		attributename = text.substring(8, i1);
		Variable variable = new Variable();
		variable.setName(attributename);
		variable.setType("library");
		variable.setLength(i1+2);
		return variable;
	}
	
	
	private Variable parseField(String text) {
		String type = parseType(text);
		if (!"field".equals(type) || !":".equals(text.substring(5, 6))) {
			return null;
		}
		int i1 = text.indexOf("$", 6);
		if (i1<0 || i1>50) {
			return null;
		}
		String fieldname = null;
		int i2 = text.indexOf(":", 6);
		if (i2<0 || i2>i1) {
			return null; // no type
		}
		else {
			fieldname = text.substring(6, i2);
		}
		String fieldtype = null;
		int i3 = text.indexOf(":", i2+1);
		if (i3<0 || i3>i1) {
			fieldtype = text.substring(i2+1, i1);
		}
		else {
			fieldtype = text.substring(i2+1, i3);
		}
		
		String fieldformat = null;
		if (i3<i1) {
			fieldformat = text.substring(i3+1, i1);
		}
		
		Variable variable = null;
		
		if ("template".equals(fieldtype)) {
			int i5 = text.indexOf(":start", i3);
			if (i5<0 || i5>i1) {
				return null;
			}
			String starttag = "$field:"+fieldname+":template:start$";
			String endtag = "$field:"+fieldname+":template:end$";
			int i6 = text.indexOf(endtag, i3+1);
			if (i6<0) {
				return null;
			}
			variable = new Variable();
			variable.setType(type);
			variable.setName(fieldname);
			variable.setLength(i6+endtag.length()+1);
			String defaultText = text.substring(starttag.length()-1, i6);
			List<String> options = new ArrayList<String>();
			defaultText = parseOptions(defaultText, options);
			variable.setDefaultValue(defaultText);
			if (!options.isEmpty()) {
				variable.setOptions(options);
			}
			variable.setValueType(fieldtype);
		}
		
		if ("date".equals(fieldtype)) {
			variable = new Variable();
			variable.setType(type);
			variable.setName(fieldname);
			variable.setValueType(fieldtype);
			variable.setFormat(fieldformat);
			variable.setValueType(fieldtype);
			variable.setLength(i1+2);
		}
		
		if ("string".equals(fieldtype)) {
			variable = new Variable();
			variable.setType(type);
			variable.setName(fieldname);
			variable.setValueType(fieldtype);
			variable.setLength(fieldname.length()+15);
		}

		return variable;
	}
	
	
	private String parseOptions(String text, List<String> options) {
	
		
		String startlabel = "$options:start$";
		
		int i0 = text.indexOf(startlabel);
		if (i0<0) return text;
		
		String endlabel = "$options:end$";
		
		int i1 = text.indexOf(endlabel);
		if (i1<0) return text;
		String templatetext = text.substring(0, i0) + text.substring(i1+endlabel.length()); 
		String optionstext = text.substring(i0+startlabel.length(), i1); 
		
		i0 = optionstext.indexOf("<li>");
		while (i0>0 && i0<optionstext.length()) {
			i1 = optionstext.indexOf("</li>", i0);
			if (i1>=0) {
				String option = optionstext.substring(i0+4, i1);
				options.add(option);
				i0 = optionstext.indexOf("<li>", i0+5);
			}
		}
		
		return templatetext;
	}
	
	private Variable parseDate(String text) {
		String type = parseType(text);
		if (!"date".equals(type) && !":".equals(text.substring(4, 5)) && !"$".equals(text.substring(4, 5))) {
			return null;
		}
		
		int lenght = 6;
		String format = null;
		
		if (":".equals(text.substring(4, 5))) {
			int i1 = text.indexOf("$", 5);
			if (i1<0 || i1>50) {
				return null;
			}
			format = text.substring(5, i1);
			lenght = i1+2;
		}
		
		Variable variable = new Variable();
		variable.setName("date");
		variable.setType(type);
		variable.setLength(lenght);
		variable.setFormat(format);
		return variable;
	}
	
	private Variable parseUserAttribute(String text) {
		
		if (text==null)
			return null;
				
		String type = parseType(text) ;
		
		
		if (!"user".equals(type)|| !":".equals(text.substring(4, 5))) {
			return null;
		}
		
		int i1 = text.indexOf("$", 5);
		
		if (i1<0 || i1>50) {
			return null;
		}
		
		String attributename = null, format = null;
		
		int i2 = text.indexOf(":",5);
		
		if (i2<0 || i2>i1) {
			attributename = text.substring(5, i1);
		}
		else {
			attributename = text.substring(5, i2);
			if (i2>0 && i2<i1) {
				format = text.substring(i2+1, i1);
			}
		}
		Variable variable = new Variable();
		variable.setName(attributename);
		variable.setType(type);
		variable.setFormat(format);
		variable.setLength(i1+2);
		return variable;
	}
	
	
	
	private String parseType(String text) {
		
		if (text==null)
			return null;
		
		int i0 = text.indexOf(":");
		if (i0<0) i0 = text.length();
		int i1 = text.indexOf("$");
		if (i1<0) i1 = text.length();
		String type = i0<i1 ? text.substring(0, i0) : text.substring(0, i1) ;
		return type;
	}
}
