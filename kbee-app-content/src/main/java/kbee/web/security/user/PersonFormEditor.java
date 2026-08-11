package kbee.web.security.user;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.ConstraintException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.entity.Person;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.model.Classification;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.PersonMember;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.service.PersonService;
import com.novamens.content.user.UserService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.wicket.editor.ClassificableEditor;
import com.novamens.kbee.wicket.markup.html.console.event.EditEvent;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;
import kbee.web.dataset.MemberDisplayNamePanel;
import kbee.web.eform.EFieldPanel;
import kbee.web.eform.EFormMessage;
import kbee.web.eform.EFormEditor;
import kbee.web.eform.KbeeUserForm;
import kbee.web.eform.ObjectFormEditor;
import kbee.web.form.EditButtonsV5;
import kbee.web.page.ErrorPageEvent;

@SuppressWarnings("serial")
public class PersonFormEditor extends ObjectFormEditor<DataSetMember> implements ClassificableEditor<DataSetMember> {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(PersonFormEditor.class.getName());
	
	final boolean role_admin =
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_security = role_admin || 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.SECURITY.getId());

	private WebMarkupContainer container;
	private Form<?> form;
	
	public PersonFormEditor(IModel<DataSetMember> model) {
		this("editor", model, false);
	}
	
	public PersonFormEditor(String id, IModel<DataSetMember> model, boolean isNew) {
		this(id, model, isNew, false);
	}
	
	public PersonFormEditor(String id, IModel<DataSetMember> model, boolean isNew, boolean isReadOnly) {
		super(id, model, isNew, isReadOnly);
	}
	
	@Override
	public void setModel(IModel<DataSetMember> model) {
		super.setModel(model);
		
		if (model!=null) {
			
			IModel<EFormData> m=getFormData(model.getObject());
			setDataModel(m);
		}
	}
	
	
	@Override
	public Form<?> getForm() {
		return form;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		if (form!=null) 
			return;
	
		container = new WebMarkupContainer("panel-container");
		
		container.setOutputMarkupId(true);
		add(container);
		
		
		form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new MemberDisplayNamePanel<DataSetMember>("displayname") {
			public boolean isVisible() {
				return !getDataSet().isDisplayNameEditable();
			}
		});
		
		EFormEditor ed = new EFormEditor("eform", getDataModel());
		
		
		
		
		form.add(ed);
		
		container.add(form);
		
		container.add(new FeedbackPanel());
		
		container.add(new EditButtonsV5<DataSetMember>(this) {
			public void onSubmitClick(AjaxRequestTarget target) {
				if (!hasErrors()) {
					super.onSubmitClick(target);
				}
				else {
					target.add(PersonFormEditor.this);
				}
			}
			@Override
			public boolean isVisible() {
				if (getModel().getObject().getDataSet().isReadonly())
					return isRoot();
				if (getModel().getObject().getState()==ObjectState.DELETED)
					return false;
				if (isReadOnly())
					return false;
				if (role_security)
					return true;
				if (!isWriteable(getModelObject()))
					return false;
				return true;
			}
		});
	}
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedFields().isEmpty()) {
				validate();
				if (!hasErrors()) {
					for (EFormField<?> field : getDataModel().getObject().getForm().getFields()) {
						field.set(getModelObject(), getDataModel().getObject());
					}
					((ObjectEditorPanel<?>) form.get("displayname")).updateModel();
					getModelObject().getService(DOMObjectService.class).update(getUpdatedParts());
					getModelObject().getService(PersonService.class).updateUser();
					reset();
					if (target!=null && getModelObject().getService(PersonService.class).getUserMember()!=null) {
						PersonMember usermember =  getModelObject().getService(PersonService.class).getUserMember();
						if (usermember!=null) {
							fireScanAll( new EditEvent<Person>(
								target, 
								new ObjectModel<Person>(
									getModelObject().getService(PersonService.class).getUserMember().getPerson())));
						}
						target.add(PersonFormEditor.this.getPage());		
					}	
					
					
				}
			}
		}
		catch (Exception e) {
			logger.error(e);
			fire (new ErrorPageEvent(target, e));
		}
	}
	
	public void update(DataSetMember member) {
		for (EFieldPanel<?> fieldpanel : getFieldPanels()) {
			fieldpanel.update(member);
		}
	}
	
	public void validate() {
		EFormEditor formpanel = getFormPanel();
		formpanel.clearMessages();
		getFeedbackMessages().clear();
		for (EFormField<?> field : formpanel.getForm().getFields()) {
			field.validate(new KbeeEValidatable(formpanel.getForm(), field)); 
		}
		if (!validateUniqueness()) {
			this.error(new EFormMessage(PersonFormEditor.this, getEForm(), "el nombre de la persona ya esta registrado", FeedbackMessage.ERROR));
		}
	}

	@Override
	public void cancel(AjaxRequestTarget target) {
		super.cancel(target);
		if (isNew()) {
			try {
				getModelObject().getService(DOMObjectService.class).delete();
			}
			catch ( ConstraintException | ContentMgmtException | ServiceNotFoundException e) {
				logger.error(e);
				fire (new ErrorPageEvent(target, e));
			}
			onCancel(target);
		}
	}

	public void onUpdate(AjaxRequestTarget target) {
	}

	public void onCancel(AjaxRequestTarget target) {
	}

	public boolean isHierachical() {
		return getModelObject().getDataSet().isHierachical();
	}
	
	public DataSet getDataSet() {
		return getModelObject().getDataSet();
	}
	
	public List<Classification> getClassification() {
		return getModelObject().getClassification();
	}
	
	protected EForm getForm(DataSetMember member) {
		return new KbeeUserForm((PersonMember)member);
	}

	protected Form<?> getWicketForm() {
		return form;
	}
	
	protected boolean isWriteable(DataSetMember member) {
		return ServiceLocator
			.getService(UserService.class)
			.isWriteable(member);
	}

	private boolean validateUniqueness() {
		String memberValue = getModelObject().getStrValue();
		DataSetMember member =  getContentDao().findMemberByValue(getModelObject().getDataSet(), memberValue);
		if (member!=null && member.getState()!=ObjectState.DELETED && !member.equals(getModelObject())) {
			return false;
		}
		return true;
	}
}