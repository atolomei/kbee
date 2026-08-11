package com.novamens.kbee.template;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import com.novamens.content.model.DataSetMember;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;

public class KbeeShortPathModel implements TemplateDirectiveModel  {
	@SuppressWarnings("rawtypes")
	public void execute(Environment env, Map parameters, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
 			Writer out = env.getOut();
 			
 			
 			TemplateHashModel template = env.getDataModel();
 			
 			
 			TemplateModel model = template.get(".");
 			
 			String path = null;
 			
 			DataSetMember member = model instanceof KbeeValueTemplateModel ? ((KbeeValueTemplateModel)model).getValue() : null;
 			
// 			while (member!=null && member.getParents()!=null && !member.getParents().isEmpty()) {
// 				DataSetMember parent = member.getParents().get(0);
// 				String name = parent.getDisplayName();
// 				if (name.length()>50) name = name.substring(0,50)+"...";
// 				path = path==null ? name : name + ; 
// 	 			member = parent;
// 			}
 			
 			path = getPath(member);
 			
 			if (path!=null && path.contains(" -> ")) {
 				String text = "<div style=\"color:#666666;\">"+path+"</div>";
 				out.write(text);
 			}
	}
	
	
	private String getPath(DataSetMember member) {
		String path = null;
		
		if (member==null)
			return null;
		
		String name = member.getDisplayName();
		if (name.length()>50) name = name.substring(0,50)+"...";
		path = name;
		
		if (member.getParents()!=null) {
			String p = null;
			for (DataSetMember parent : member.getParents()) {
				String pp = getPath(parent);
				if (pp!=null && (p==null || pp.length()<p.length())) {
					p = pp;
				}
			}
			if (p!=null) {
				path = p + " -> " + path;
			}
		}
		
		return path;
		
	}
	
	
	
}
