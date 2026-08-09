package com.novamens.wicket.markup.html.form;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;



@SuppressWarnings("serial")
public class PrintableBehavior extends Behavior {
	private static final long serialVersionUID = 1L;
	
	private static final ResourceReference JS = new JavaScriptResourceReference(PrintableBehavior.class, "jquery.printelement.js");
	
	public class PrintButton extends WebMarkupContainer {
		private static final long serialVersionUID = 1L;
		public PrintButton(String id, final String panel) {
			super(id);
			add(new AttributeModifier("onclick", new Model<String>() {
				public String getObject() {
					return "printPanel('#"+panel+"');";
				}
			}));
		}
	}
	
	public PrintableBehavior() {
//		Request request = RequestCycle.get().getRequest();
//		String json = request.getRequestParameters().getParameterValue("json").toString("");

	}
	
//	public void onSort(AjaxRequestTarget target, List<String> ordered) {
//	}
//	
//	protected void respond(AjaxRequestTarget target) {
//		Request request = RequestCycle.get().getRequest();
//		String json = request.getRequestParameters().getParameterValue("json").toString("");
//		
//		List<String> ids = new ArrayList<String>();
//		StringTokenizer tokens = new StringTokenizer(json, "&");
//		
//		while (tokens.hasMoreTokens()) {
//			String token = tokens.nextToken();
//			int i = token.indexOf("=");
//			if (i>0) {
//				String id = token.substring(i+1);
//				ids.add(id);
//			}
//		}
//		
//		onSort(target, ids);
//	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(JavaScriptHeaderItem.forReference(JS));

		
		String script = "function printPanel(id) { "+
			//"$('#response').printElement();"+
			"	$(id).printElement();"+
		"}\n";
		
		response.render(JavaScriptHeaderItem.forScript(script.toString(), "printable"));
	}
}
