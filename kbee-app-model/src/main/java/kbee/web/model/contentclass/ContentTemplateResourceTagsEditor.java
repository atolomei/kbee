package kbee.web.model.contentclass;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.content.model.ContentTemplate;
import com.novamens.content.service.DOMObjectService;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class ContentTemplateResourceTagsEditor extends ObjectEditor<ContentTemplate> {
	private static final long serialVersionUID = 1L;

	static Logger logger = LogManager.getLogger(ContentTemplateResourceTagsEditor.class.getName());
	
	final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	
	public ContentTemplateResourceTagsEditor(String id, IModel<ContentTemplate> model) {
		super(id, model);
		setEditionEnabled(false);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		form.add(new ResourceTagsEditor("resourceTags"));
		
		add(form);
		
		add(new EditButtonsV5<ContentTemplate>(this) {
			@Override
			public boolean isEnabled() {
				return role_admin || role_model;
			}
		});
	}
	

	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				getModelObject().getService(DOMObjectService.class).update(getUpdatedParts());
				super.reset();
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<Void>(target, e));
		}	
	}
}