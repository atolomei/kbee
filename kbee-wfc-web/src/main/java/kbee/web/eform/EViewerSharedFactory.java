package kbee.web.eform;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormData;
import com.novamens.kbee.content.form.KbeeERelation;
import com.novamens.kbee.content.form.KbeeEResource;
import com.novamens.kbee.content.form.KbeeEResources;
import com.novamens.portal6.model.SiteService;

@SuppressWarnings("serial")
public class EViewerSharedFactory extends EViewerFactory {
	private static final long serialVersionUID = 1L;
	
	public EViewerSharedFactory(IModel<EFormData> model) {
		super(model);
	}
	
	public Panel getPanel(String id, EFormComponent component) {
		
		Panel panel = getMap().get(component.getName());
		
		if (panel!=null) {
			return panel;
		}
		else
		if (component instanceof KbeeEResources) {
			panel =  new EResourcesViewer(id, (KbeeEResources)component, getDataModel()) {
				protected boolean isShared() {
					return true;
				}
			};
			if (panel != null) {
				getMap().put(component.getName(), panel);
			}
		}
		else
		if (component instanceof KbeeEResource) {
			panel =  new EResourceStatelessViewer(id, (KbeeEResource)component, getDataModel()) {
				protected boolean isShared() {
					return true;
				}
			};
			if (panel != null) {
				getMap().put(component.getName(), panel);
			}
		}
		else
		if (component instanceof KbeeERelation) {
			panel =  new ERelationPanel(id, (KbeeERelation)component, getDataModel()) {
				@Override
				public boolean isViewer() {
					return true;
				}
				@Override
				protected boolean isShared() {
					return true;
				}
				@Override
				protected String getUrl(Content content) {
					return getSite()!=null ?
						getSite().getService(SiteService.class).getUrl(content) :
						super.getUrl(content);	
				}
			};
		}
		else {
			panel = super.getPanel(id, component);
		}

	
		return panel;
	}
}