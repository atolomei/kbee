package kbee.web.security.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.PersonMember;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
// import com.novamens.content.web.dataset.markup.MemberClassificationEditor;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.util.logging.Logger;
import kbee.web.editor.ClassificationEditor;
import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class UserClassificationEditor extends DomainObjectEditor<PersonMember> {
	private static final long serialVersionUID = 1L;
			
	static private Logger logger = new Logger(LogManager.getLogger(UserClassificationEditor.class.getName()));
	
	final boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot();
	final boolean role_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_security	= role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SECURITY.getId());

	public UserClassificationEditor(String id, IModel<PersonMember> model) {
		this(id, model, false);
	}
	
	public UserClassificationEditor(String id, IModel<PersonMember> model, boolean isMyAccount) {
		super(id, model);
		
		setOutputMarkupId(true);
		setEditionEnabled(false);
		
		setReadOnly(isMyAccount);
		
		add( new WebMarkupContainer("readonly") {
			public boolean isVisible() {
				return isReadOnly();
			}
		});

		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		if (UserClassificationEditor.this.getModelObject()==null) {
			form.add(new InvisiblePanel("classification"));
		}
		else {
		form.add(new ClassificationEditor<PersonMember>() {
			@Override
			public List<AttributeTemplate> getAttributes() {
				if ((UserClassificationEditor.this.getModelObject()).getDataSet()==null)
					return new ArrayList<AttributeTemplate>();
				return ((UserClassificationEditor.this.getModelObject()).getDataSet()).getAttributes();
			}
			@Override
			public List<Classifier> getClassifiers() {
				if ((UserClassificationEditor.this.getModelObject()).getDataSet()==null)
					return new ArrayList<Classifier>();
				return ((UserClassificationEditor.this.getModelObject()).getDataSet()).getClassifiers();
			}
			@Override
			public List<ModelElementTemplate> getStructure() {
				if ((UserClassificationEditor.this.getModelObject()).getDataSet()==null)
						return new ArrayList<ModelElementTemplate>();
				return ((UserClassificationEditor.this.getModelObject()).getDataSet()).getStructure();
			}

		});
		}
		
		add(form);
		
		add(new EditButtonsV5<PersonMember>(this) {
			@Override
			public boolean isVisible()  {
				
				if (getModel()==null || getModel().getObject()==null)
					return false;
				
				if (getModel().getObject().getState()==ObjectState.DELETED)
					return false;
				if (isReadOnly())
					return false;
				if  (UserClassificationEditor.this.getModelObject().getProfile(UserProfile.class).getUser().getUserName().startsWith("root@"))
					return is_root;
				if (!role_security && isAdmin(UserClassificationEditor.this.getModelObject())) 
					return true;
				return role_security;
			}
		});
	}
	
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				ServiceLocator.getService(SecurityContentMgmtService.class).update(getModelObject(), getUpdatedParts());
				reset();
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
	
	protected boolean isAdmin(PersonMember person) {
		return ServiceLocator.getService(UserService.class).isUserAdmin(person);
	}
}
