package com.novamens.wicket.markup.html.form;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;

import com.googlecode.wicket.jquery.ui.resource.JQueryUIResourceReference;

//@SuppressWarnings("serial")
public abstract class SortableBehavior extends AbstractDefaultAjaxBehavior {
	private static final long serialVersionUID = 1L;
	
	//protected static final ResourceReference JS = new CssResourceReference(SortableBehavior.class, "jquery-sortable.js");
	
	public SortableBehavior() {
	}
	
	public void onSort(AjaxRequestTarget target, String id, List<String> ordered) {
	}
	
	public void onSort(AjaxRequestTarget target, List<String> ordered) {
	}
	
	protected void respond(AjaxRequestTarget target) {
		Request request = RequestCycle.get().getRequest();
		String json = request.getRequestParameters().getParameterValue("json").toString("");
		String sorted = request.getRequestParameters().getParameterValue("sorted").toString("");
		
		List<String> ids = new ArrayList<String>();
		StringTokenizer tokens = new StringTokenizer(json, "&");
		
		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken();
			int i = token.indexOf("=");
			if (i>0) {
				String id = token.substring(i+1);
				ids.add(id);
			}
		}
		
		onSort(target, sorted, ids);
		onSort(target, ids);
	}

	/**
	 * 
	 * It seems that there is a bug 
	 * and Columns with a "_" in the id cause this function to fail.
	 * 
	 */
	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(JavaScriptReferenceHeaderItem.forReference(JQueryUIResourceReference.get()));

		//response.render(JavaScriptHeaderItem.forReference(JS));
		
		String script = "var adjustment; $(function () {$('#"+component.getMarkupId()+"').sortable("+
		"{ "+
		"	group: '"+component.getMarkupId()+"', "+
		"	cursor: \"move\", "+
		"	pullPlaceholder: false, " +
		"	helper:\"clone\", " +
		"	nested:false, "+
//		"	itemSelector:\"li.media\","+
		"	itemSelector:'"+getItemSelector()+"',"+
		"	delay:500,"+
		"	stop: function( event, ui ) { "+
		"		var i = ui.item; top.sorted=i.attr('data-id'); var data = $('#"+component.getMarkupId()+"').sortable(\"serialize\", { key:\"resource\", attribute: \"data-id\" }); top.json=data; sort(); " +
		"	},"+ 
		"	onDrop: function  ($item, container, _super, event) {"+
		"		alert($item); alert($item.id); var $clonedItem = $('<li/>').css({height: 0});"+
		"		$item.before($clonedItem);"+
		"		var data = $('#"+component.getMarkupId()+"').sortable(\"serialize\");"+
		"		top.json = JSON.stringify(data, null, ' '); console.log(top.json);"+
		"		$clonedItem.animate({'height': $item.height()});"+
		"		$item.animate($clonedItem.position(), function  () {"+
		"			$clonedItem.detach();"+
		"			_super($item, container);"+
		"		});"+
		"		sort();"+
		"	},"+
		"	onDragStart: function ($item, container, _super) {"+
		"		var offset = $item.offset(),"+
		"		pointer = container.rootGroup.pointer;"+
		"		adjustment = {"+
		"			left: pointer.left - offset.left,"+
		"			top: pointer.top - offset.top"+
		"		};"+
		"		_super($item, container);"+
		"	},"+
		"	onDrag: function ($item, position) {"+
		"		$item.css({"+
		"			left: position.left - adjustment.left,"+
		"			top: position.top - adjustment.top"+
		"		});"+
		"	}"+
		"}"+				
		");});\n";
		
		script += "function sort() {\n " +
		getCallbackScript() + "\n" +
		"}\n";
		
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
	protected abstract String getItemSelector();
	
	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		attributes.getDynamicExtraParameters().add("return {json: top.json, sorted: top.sorted};");
	}
}
