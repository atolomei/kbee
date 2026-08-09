package com.novamens.wicket.util;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.Model;

@SuppressWarnings("serial")
public class FloatingBehavior7 extends Behavior {
	private static final long serialVersionUID = 1L;
	private Component trigger;
	private String os;
	
	public FloatingBehavior7(Component trigger) {
		trigger.setOutputMarkupId(true);
		setTrigger(trigger);
	}

	public void bind(final Component component) {

		
		Model<String> onTriggerClickScript = new Model<String>() {
			public String getObject() {
				String script  = "";
				script += "if (event.stopPropagation) event.stopPropagation(); if (event.preventDefault) event.preventDefault(); closepanel(top.panel5, top.trigger5);";
				script += "top.panel5='"+component.getMarkupId()+"';";
				script += "top.trigger5='"+trigger.getMarkupId()+"';";
				script += "top.term5 = showpanel5(top.panel5,'"+trigger.getMarkupId()+"');";
				return script;
			}
		};	
		
		os = onTriggerClickScript.getObject();
		
		Model<String> onTriggerOutScript = new Model<String>() {
			public String getObject() {
				String script = "";
				script += " clearTimeout(top.term5); top.term5=setTimeout(\"closepanel('"+component.getMarkupId()+"', '"+trigger.getMarkupId()+"')\",1000);";
				return script;
			}
		};
		
		Model<String> onPanelOverScript = new Model<String>() {
			public String getObject() {
				String script = "";
				script += "clearTimeout(top.term5);";
				return script;
			}
		};
		
		Model<String> onPanelOutScript = new Model<String>() {
			public String getObject() {
				String script = "";
				script += "top.term5=setTimeout(\"closepanel('"+component.getMarkupId()+"', '"+trigger.getMarkupId()+"')\",1000);";
				return script;
			}
		};
		
		trigger.add(new AttributeModifier("onClick", onTriggerClickScript));
		trigger.add(new AttributeModifier("onMouseOut", onTriggerOutScript));
		
		component.add(new AttributeModifier("onMouseOver", onPanelOverScript));
		component.add(new AttributeModifier("onMouseOut", onPanelOutScript));
	}
	
	public void setTrigger(Component trigger) {
		this.trigger = trigger;
	}
	
	public String getOnTriggerOverScript() {
		return os;
	}
	
	@Override
	public void renderHead(Component c, IHeaderResponse response) {
		super.renderHead(c, response);
		
		StringBuilder script = new StringBuilder();
		
//		script.append("getViewportHeight = function getViewportHeight() {");
//		script.append("	if (window.innerHeight)");
//		script.append("		viewPortHeight = window.innerHeight;");
//		script.append("	else if (document.documentElement && document.documentElement.clientHeight)");
//		script.append("		viewPortHeight = document.documentElement.height;");
//		script.append("	else if (document.body)");
//		script.append("		viewPortHeight = document.body.clientHeight;");
//		script.append("	return viewPortHeight;");
//		script.append("}\n");
		
//		script.append("function setpos5(panel, trigger) {");
//		script.append("	var container = $('#scrolllist');");
//		script.append("	var containerposition = trigger.position();");
//		script.append("	var containerheight = 900;");
//		script.append("	if (document.getElementById('scrolllist')!=null) { ");
//		script.append("		containerposition = container.position(); ");
//		script.append("		containerheight = container.height();");
//		script.append("	};");
//		script.append("	var panelposition = panel.position();");
//		script.append("	var triggerposition = trigger.position();");
//		script.append("	var panelheight = (panel.children('div').eq(0)).height();");
//		script.append("	var toppos = triggerposition.top;");
//		script.append("	if (triggerposition.top+panelheight>containerposition.top+containerheight) {");
//		script.append("		toppos = containerposition.top+containerheight-panelheight;};");
//		script.append(" panel.css({left:triggerposition.left+30,top:toppos});");
//		script.append("}\n");
		
		script.append("function closepanel(panelid, triggerid) {");
		script.append("	if (panelid) {");
		script.append("		if (document.getElementById(panelid)!==null) {");
		script.append("			var styleObj = document.getElementById(panelid).style;");
		script.append("			var p = $('#'+panelid); p.show();");
		script.append("			styleObj.display = 'none';");
		script.append("		}");
		script.append("	}");
		script.append("	if (triggerid && document.getElementById(triggerid)!==null) {");
//		script.append("		document.getElementById(triggerid).className=\"menutrigger\";");
//		script.append("		document.getElementById(triggerid).innerHTML=\" >\";");
		script.append("	}");
		script.append("}\n");
		
		script.append("function showpanel5(panelid, triggerid) {");
		script.append("if (panelid) { ");
		script.append("		if (document.getElementById(panelid)) { ");
		script.append("			var styleObj = document.getElementById(panelid).style;");
		script.append("			var styleObj2 = document.getElementById(triggerid).style;");
//		script.append("			document.getElementById(triggerid).className=\"smenu\";");
//		script.append("			document.getElementById(triggerid).innerHTML=\"V\";");
		script.append("			styleObj.display = 'block';");
		script.append("			styleObj.position = 'absolute';");
		script.append("			styleObj.zIndex = 100000;");
//		script.append("			setpos5($('#'+panelid), $('#'+triggerid));");
		script.append("		}");
		script.append("	}");
		script.append("}\n");

		response.render(JavaScriptHeaderItem.forScript(script.toString(), "floating5"));
	}
}