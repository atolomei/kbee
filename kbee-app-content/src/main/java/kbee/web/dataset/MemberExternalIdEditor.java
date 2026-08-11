package kbee.web.dataset;

import org.apache.logging.log4j.LogManager;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.content.model.DataSetMember;
import com.novamens.content.service.DOMObjectService;
import com.novamens.dom.ObjectState;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.form.EditButtonsV5;
import kbee.web.page.ErrorPageEvent;

public class MemberExternalIdEditor extends DomainObjectEditor<DataSetMember> {

	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = new kbee.util.logging.Logger(LogManager.getLogger(MemberExternalIdEditor.class.getName()));
	
	final boolean role_admin 				= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model 				= role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	final boolean role_dataset_members 		= role_model || role_admin || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());
	final boolean role_dataset_members_read = role_dataset_members || ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DATASET_VALUES_READ.getId());
	
	public MemberExternalIdEditor(String id, IModel<DataSetMember> model) {
		super(id, model);
		setEditionEnabled(false);

	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);

		form.add(new TextField<String>("externalId", false));
	
		
		
		add(form);
		
		add(new EditButtonsV5<DataSetMember>(this) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {

				if (getModel().getObject().getDataSet().isReadonly())
					return isRoot();
				
				if (getModel().getObject().getState()==ObjectState.DELETED)
					return false;
				
				if (isReadOnly())
					return false;
				
				if (isSupportSessionUser() && !isRoot())
					return false;
				
				
				if (!role_dataset_members)
					return false;
				
				return true;
			}
		});

		
	}
	
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				
					getModelObject().getService(DOMObjectService.class).update(getUpdatedParts());
				reset();
				target.add(this);
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire (new ErrorPageEvent(target, e));
		}
	}

	
}
