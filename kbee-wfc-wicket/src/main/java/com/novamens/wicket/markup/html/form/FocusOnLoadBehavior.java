package com.novamens.wicket.markup.html.form;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;

public class FocusOnLoadBehavior extends Behavior
{
    private Component component;
 
    public void bind( Component component )
    {
        this.component = component;
        component.setOutputMarkupId(true);
    }
 
	public void renderHead(Component component, IHeaderResponse response)   {
        super.renderHead(component, response);
		response.render(OnLoadHeaderItem.forScript("document.getElementById('" + component.getMarkupId() + "').focus()"));
    }

    public boolean isTemporary(Component component)
    {
        // remove the behavior after component has been rendered      
        return true;
    }
}  