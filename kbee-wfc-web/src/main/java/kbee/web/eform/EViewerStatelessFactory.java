package kbee.web.eform;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormData;
import com.novamens.kbee.content.form.KbeeEResource;
import com.novamens.kbee.content.form.KbeeEResources;

public class EViewerStatelessFactory extends EViewerFactory {
	private static final long serialVersionUID = 1L;
	
	public EViewerStatelessFactory(IModel<EFormData> model) {
		super(model);
	}
	
	public Panel getPanel(String id, EFormComponent component) {
		
		Panel panel = getMap().get(component.getName());
		
		if (panel!=null) {
			return panel;
		}
		else
		if (component instanceof KbeeEResources) {
			panel =  new EResourcesStatelessViewer(id, (KbeeEResources)component, getDataModel());
			if (panel != null) {
				getMap().put(component.getName(), panel);
			}
		}
		if (component instanceof KbeeEResource) {
			panel =  new EResourceStatelessViewer(id, (KbeeEResource)component, getDataModel());
			if (panel != null) {
				getMap().put(component.getName(), panel);
			}
		}
		else {
			panel = super.getPanel(id, component);
		}
	
		return panel;
	}
}