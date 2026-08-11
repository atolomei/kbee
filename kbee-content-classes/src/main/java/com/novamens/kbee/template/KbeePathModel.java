package com.novamens.kbee.template;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.novamens.content.model.DataSetMember;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;

public class KbeePathModel implements TemplateDirectiveModel  {
	
	private static final int MAX_NAME_LENGTH = 50;

	@SuppressWarnings("rawtypes")
	public void execute(Environment env, Map parameters, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
 			Writer out = env.getOut();
 			
 			
 			TemplateHashModel template = env.getDataModel();
 			
 			
 			TemplateModel model = template.get(".");
 			
 			
 			DataSetMember member = model instanceof KbeeValueTemplateModel ? ((KbeeValueTemplateModel)model).getValue() : null;
 			
			
 			List<String> paths = new ArrayList<>();
 			
 			for (DataSetMember parent : member.getParents()) {
 				paths.addAll(getPaths(parent));
 			}
 			
 			String value = "";
 			
 			paths.sort(new Comparator<String>() {
 				@Override
 				public int compare(String n1, String n2) {
 					return n1.compareToIgnoreCase(n2);
 				}	
 			});
 			
 			for (String path : paths) {
 				//if (path.contains(" -> ")) {
 					
 				//if (!"".equals(value)) {
 				//		value += "</br>";
 				//}
 				
 				value += "<div class=\"segment\">"+path+"</div>";
 				
 				//}
 			}
 			
 			if (!"".equals(value)) {
 				String text = "<div class=\"segment\">"+value+"</div>";
 				out.write(text);
 			}
	}
	
	

	private List<String> getPaths(DataSetMember member) {
	    if (member == null) {
	        return Collections.emptyList();
	    }

	    String name = truncate(member.getDisplayName());
	    List<DataSetMember> parents = member.getParents();

	    // Leaf node (no parents)
	    if (parents == null || parents.isEmpty()) {
	        return Collections.singletonList(name);
	    }

	    List<String> paths = new ArrayList<>();

	    for (DataSetMember parent : parents) {
	        List<String> parentPaths = getPaths(parent);

	        for (String parentPath : parentPaths) {
	            paths.add(parentPath + " -> " + name);
	        }
	    }

	    return paths;
	}

	private String truncate(String value) {
	    if (value == null) return "";
	    return value.length() > MAX_NAME_LENGTH
	            ? value.substring(0, MAX_NAME_LENGTH) + "..."
	            : value;
	}
}
