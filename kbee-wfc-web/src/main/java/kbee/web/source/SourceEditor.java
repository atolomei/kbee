package kbee.web.source;


import com.novamens.content.base.Source;
import com.novamens.content.service.DomService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.*;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.InfoDialog;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

@SuppressWarnings("serial")
public class SourceEditor extends ObjectEditor<Source> {
	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger =  kbee.util.logging.Logger.getLogger(SourceEditor.class.getName());

	public SourceEditor(IModel<Source> model) {
		this("editor", model, false);
	}
	
	/**
	 * @param id
	 * @param model
	 * @param isnew
	 */
	public SourceEditor(String id, IModel<Source> model, boolean isnew) {
		super(id, model);
		setOutputMarkupId(true);
		setIsNew(isnew);
		setEditionEnabled(isnew);
	}

	
	public void onDetach() {
		super.onDetach();
		
	if (getModel()!=null)
		getModel().detach();
	
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new TextField<String>("displayName", true));
		form.add(new TextField<String>("name", true));
		form.add(new StaticField<String>("id", new Model<String>( String.valueOf(getModel().getObject().getId()))));

		
		
		add(form);
		
		add(new EditButtonsV5<Source>(this) {
			@Override
			protected String getCancelClass() {
				return "btn btn-default btn-sm";
			}
			@Override
			protected String getSubmitClass() {
				return "btn btn-primary btn-sm";
			}
			protected String getEditClass() {
				return "btn btn-primary btn-sm";
			}
		});	
		
		add(new InfoDialog("help-modal"));
	}

	public void onClose(AjaxRequestTarget target) {
		
	}
	
	@Override
	public void cancel(AjaxRequestTarget target) {
		
		if (isNew()) {
//			try {
//				ServiceLocator.getService(SecurityContentMgmtService.class).delete(getModel().getObject());
//			}
//			catch (DataIntegrityViolationException e) {
//				logger.error(" {} | {} | {} | {}", getSessionUser().getUserName(), e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
//			}
//			catch (Exception e) {
//				logger.error(" {} | {} | {} | {}", getSessionUser().getUserName(), e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
//			}
//			onClose(target);
		}
		
		onCancel(target);
	}


	public void edit(AjaxRequestTarget target) {
		super.edit(target);
	}
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
//				KbeeSource source = (KbeeSource)getModelObject();
//				source.getService(DOMObjectService.class).update(getUpdatedParts());
				getModelObject().getService(DomService.class).update(getUpdatedParts());
				super.reset();
				target.add(SourceEditor.this.getPage());
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire(new ErrorEvent<>(target, e));

		}
	}

	protected void onCancel(AjaxRequestTarget target) {
	}

	protected void onAfterSubmit(AjaxRequestTarget target) {
		setEditionEnabled(false);
		target.add(this);
	}
	
	protected Domain getDomain() {
		return (Domain)ServiceLocator.getService(UserService.class).getDomain();
	}

	protected void onUpdate(AjaxRequestTarget target) {
		
	}
	
	protected InfoDialog getHelpModal() {
		return (InfoDialog) get("help-modal");
	}

	public IModel<String> getText(String key) {
		return new StringResourceModel(key, this, null);
	}
}
