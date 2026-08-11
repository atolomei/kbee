package kbee.web.model.contentclass;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.model.ContentTemplate;
import com.novamens.content.service.DOMObjectService;
import com.novamens.dom.ObjectState;

import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

public class ContentClassStateEditor extends DomainObjectEditor<ContentTemplate> {
	private static final long serialVersionUID = 1L;

	static Logger logger = LogManager.getLogger(ContentClassStateEditor.class.getName());
	
	public ContentClassStateEditor(IModel<ContentTemplate> model) {
		this("editor", model);
	}
	
	public ContentClassStateEditor(String id, IModel<ContentTemplate> model) {
		super(id, model);
		
		final boolean role_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		final boolean infomodel	 = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
		
		setOutputMarkupId(true);
		setEditionEnabled(false);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new ChoiceField<ObjectState>("state", new PropertyModel<List<ObjectState>>(this, "states")) {
			private static final long serialVersionUID = 1L;
			@Override
			protected String getDisplayValue(ObjectState value) {
				return value.getLabel(getSessionUser().getLocale()); 
			}
			
		});
				
		add(form);
		
		add(new EditButtonsV5<ContentTemplate>(this)  {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isEnabled() {
				
				if (isExpressVersion() && !isRoot())
					return false;
				
				if (getModel().getObject().isOnlyRootEdit())
					return false;

				
				return role_admin || infomodel;
			}
		});
	}

	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				getModelObject().getService(DOMObjectService.class).update(getUpdatedParts());
				reset();
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<Void>(target, e));
		}
	}
	
	public List<ObjectState> getStates() {
		List<ObjectState> states = new ArrayList<ObjectState>();
		states.add(ObjectState.ENABLED);
		states.add(ObjectState.ARCHIVED);
		return states;
	}
	

}