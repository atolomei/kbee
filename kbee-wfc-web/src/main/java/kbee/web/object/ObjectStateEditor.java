package kbee.web.object;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.enoti.ENotiRule;
import com.novamens.content.enoti.ENotiRuleService;
import com.novamens.content.model.ModelObject;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.user.UserService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.wicket.markup.html.console.event.EditEvent;
import com.novamens.security.User;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.util.logging.Logger;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;
 
public class ObjectStateEditor<T> extends ObjectEditor<T> {

	private static final long serialVersionUID = 1L;

	static Logger logger = new Logger(LogManager.getLogger(ObjectStateEditor.class.getName()));
	List<ObjectState> states = null;
	
	public ObjectStateEditor(IModel<T> model) {
		this("editor", model);
	}
	
	public ObjectStateEditor(String id, IModel<T> model) {
		this(id, model, false);
	}
	
	public ObjectStateEditor(String id, IModel<T> model, final boolean read_only) {
		super(id, model);
		
		final boolean role_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		final boolean role_info = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());

		setOutputMarkupId(true);
		setEditionEnabled(false);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new ChoiceField<ObjectState>("state", new PropertyModel<List<ObjectState>>(this, "states")) {
			@Override
			public String getDisplayValue(ObjectState value) {
				return ObjectStateEditor.this.getLabel(value);
			}
		});
				
		add(form);
		
		add(new EditButtonsV5<T>(this)  {

			@Override
			public boolean isVisible() {
				return !read_only && (role_admin || role_info);
			}
			
			@Override
			public boolean isEnabled() {
				return (role_admin || role_info) && !read_only;
			}
		}); 
	}

	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				if (getModelObject() instanceof ModelObject) {
					if ( ((ModelObject)getModelObject()).getState()!=ObjectState.DELETED)
						((ModelObject)getModelObject()).setName( ((ModelObject)getModelObject()).getName().replace( DOMObjectService._DELETED_, ""));
				}
				if (getModelObject() instanceof AbstractObject) {
					((AbstractObject)getModelObject()).getService(DOMObjectService.class).update(getUpdatedParts());
				}	
				else if (getModelObject() instanceof ENotiRule) {
					ServiceLocator.getService(ENotiRuleService.class).update((ENotiRule)getModelObject(), getUpdatedParts());
				}
				else if (getModelObject() instanceof User) {
					ServiceLocator.getService(SecurityContentMgmtService.class).update((User) getModelObject(), getUpdatedParts());
				}
				else {
					logger.error(" not supported -> " + getModelObject().getClass().getName());
				}
				fire(new EditEvent<T>(target, getModel()));
				reset();
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
	
	public List<ObjectState> getStates() {
		if (states!=null)
			return states;
		states = new ArrayList<ObjectState>();
		states.add(ObjectState.ENABLED);
		states.add(ObjectState.ARCHIVED);
		return states;
	}
	
	protected String getLabel(ObjectState value) {
		return value.getLabel(getSessionUser().getLocale());
	}
 
	private KbeeUser getSessionUser() {
		return (KbeeUser) ServiceLocator.getService(UserService.class).getSessionUserProfile().getUser();
	}
}