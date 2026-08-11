package kbee.web.model;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import com.novamens.content.model.DataSet;
import com.novamens.content.service.DOMObjectService;

import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.editor.DomainObjectEditor;
import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class DataSetStructureEditor<T extends DataSet> extends DomainObjectEditor<T> {
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DataSetStructureEditor.class.getName());
	
	public DataSetStructureEditor(String id, IModel<T> model) {
		super(id, model);
		
		final boolean role_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
		
		setOutputMarkupId(true);
		setEditionEnabled(false);
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);

		WebMarkupContainer cont=new WebMarkupContainer("alert-info");
		cont.setVisible(getModel().getObject().isOnlyRootEdit());
		form.add(cont);
				
		form.add(new StructureEditor<DataSet>());	
		
		add(form);
		
		add(new EditButtonsV5<T>(this) {
			@Override
			public boolean isEnabled() {
				
				if (isExpressVersion() && !isRoot())
					return false;
				
				if (DataSetStructureEditor.this.getModel().getObject().isOnlyRootEdit())
					return isRoot();
				
				return role_admin;
			}
		});
	}

	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				getModelObject().getService(DOMObjectService.class).update();
				super.reset();
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));
		}
	}
}