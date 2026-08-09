package com.novamens.wicket.markup.html.form;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

import com.googlecode.wicket.jquery.ui.resource.JQueryUIResourceReference;

public class DraggableBehavior extends AbstractDefaultAjaxBehavior {
	private static final long serialVersionUID = 1L;
	
	public DraggableBehavior() {
	}
	
	protected void respond(AjaxRequestTarget target) {
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(JavaScriptReferenceHeaderItem.forReference(JQueryUIResourceReference.get()));
		
		String script = "$('#"+component.getMarkupId()+"' ).draggable({";
		script += "revert: '"+getRevert()+"', revertDuration:0,"; 
		if (getContainment()!=null) {
			script += "containment: \"#"+getContainment().getMarkupId()+"\","; 
		}
		script += "start: function() { $(this).css('width', '200px'); $(this).css('height', '50px'); $(this).css('overflow', 'hidden'); }";
		script += ", stop: function() { $(this).css('width', '100%'); $(this).css('height', 'unset'); $(this).css('overflow', 'unset'); }";
		script += " });";

		
		response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
	protected String getRevert() {
		return "invalid";
	}
	
	protected Component getContainment() {
		return null;
	};
	
	@Override 
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		//attributes.getDynamicExtraParameters().add("return {json: top.json, sorted: top.sorted};");
	}
}
