package com.novamens.wicket.markup.html.form;

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

public class DroppableBehavior extends AbstractDefaultAjaxBehavior {
	private static final long serialVersionUID = 1L;
	
	public DroppableBehavior() {
	}
	
	protected void respond(AjaxRequestTarget target) {
		Request request = RequestCycle.get().getRequest();
		String dropped = request.getRequestParameters().getParameterValue("dropped").toString("");
		onDrop(target, dropped);
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(JavaScriptReferenceHeaderItem.forReference(JQueryUIResourceReference.get()));

		String script = "$('#"+component.getMarkupId()+"' ).droppable({"+
"hoverClass: 'bggray',"+		
 "drop: function( event, ui ) {  top.dropped=ui.draggable.attr(\"data-id\"); "+getCallbackScript()+"\n  },"+
 "over: function( event, ui ) {  }"+
		"	});";

		
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
	protected void onDrop(AjaxRequestTarget target, String id) {
		
	}
	
	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		attributes.getDynamicExtraParameters().add("return {dropped: top.dropped};");
	}
}
