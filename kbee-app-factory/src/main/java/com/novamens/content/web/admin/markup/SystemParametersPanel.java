
package com.novamens.content.web.admin.markup;

import org.apache.wicket.model.Model;
import com.novamens.wicket.util.BCElement;

public class SystemParametersPanel extends AbstractSystemInfoPanel {
			
	private static final long serialVersionUID = 1L;

	public SystemParametersPanel(String id) {
		super(id);
		setOutputMarkupId(true);
	}
		
	@Override
	public void onInitialize() {
		super.onInitialize();
		 
		add (new SystemPropertiesPanel("panel"));
		
	}
	
	protected BCElement getPageBCElement() {
		return new BCElement(new Model<String>("Parameters"));
	}
	
}
