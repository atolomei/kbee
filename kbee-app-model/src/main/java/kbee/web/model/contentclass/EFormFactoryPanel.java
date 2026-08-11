package kbee.web.model.contentclass;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.form.EForm;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.service.ObjectFactoryService;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;

@SuppressWarnings("serial")
public class EFormFactoryPanel extends ModelPanel<ContentTemplate> {
	private static final long serialVersionUID = 1L;

	public EFormFactoryPanel(String id, IModel<ContentTemplate> model) {
		super(id, model);
		
		WebMarkupContainer button = new WebMarkupContainer ("new-multiple-button");
		add(button);
		
		ContextMenuPanel<Void> menu = new ContextMenuPanel<Void>(null);
		
		menu.addItem(item -> 
			new AjaxMenuItemPanelV5<Void>(item) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					onCreate(target, createEForm());
				}
				@Override
				public String getLabel() {	
					return new StringResourceModel("empty", EFormFactoryPanel.this, null).getObject();
				}
		});
		
		menu.addItem(item -> 
			new AjaxMenuItemPanelV5<Void>(item) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					onCreate(target, createDefaultEForm());
				}
				@Override
				public String getLabel() {	
					return new StringResourceModel("default", EFormFactoryPanel.this, null).getObject();
				}
		});
		
		add(menu);
	}
	
	public void onCreate(AjaxRequestTarget target, EForm newform) {
		
	}
	
	private EForm createEForm() {
		return ServiceLocator.getService(ObjectFactoryService.class).createEForm(getModel().getObject());
	}
	
	private EForm createDefaultEForm() {
		return ServiceLocator.getService(ObjectFactoryService.class).createDefaultEForm(getModel().getObject());
	}
}