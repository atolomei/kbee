package kbee.web.dataset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.Method;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptContentHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.IFormSubmitter;
import org.apache.wicket.markup.html.form.IFormSubmittingComponent;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.ConstraintException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EValidatable;
import com.novamens.content.model.Classification;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.service.DOMObjectService;
import com.novamens.content.service.DataSetService;
import com.novamens.content.user.UserService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.form.KbeeEMemMemberData;
import com.novamens.kbee.wicket.editor.ClassificableEditor;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.ServiceNotFoundException;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;
import kbee.web.editor.DomainObjectEditor;
import kbee.web.eform.EAjaxFormEvent;
import kbee.web.eform.EFieldPanel;
import kbee.web.eform.EFormDataModel;
import kbee.web.eform.EFormMessage;
import kbee.web.eform.EFormEditor;
import kbee.web.eform.FieldMessage;
import kbee.web.form.EditButtonsV5;
import kbee.web.model.DataSetPage;
import kbee.web.page.ErrorPageEvent;
import kbee.web.panel.AlertPanel;

@SuppressWarnings("serial")
public class MemberFormEditor extends DomainObjectEditor<DataSetMember> implements ClassificableEditor<DataSetMember> {
	private static final long serialVersionUID = 1L;

	static private Logger logger = new Logger(LogManager.getLogger(MemberFormEditor.class.getName()));
	
	final boolean role_admin =
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model = role_admin || 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	final boolean role_dataset_members	= role_model || role_admin || 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.DATASET_VALUES_WRITE.getId());
	final boolean role_dataset_members_read = role_dataset_members || 
		ServiceLocator
		.getService(SecurityService.class)
		.isMember(KbeeGlobalRole.DATASET_VALUES_READ.getId());
	
	private IModel<EFormData> datamodel;
	private Form<?> form;
	private List<IModel<DataSet>> aggregations = null;
	
	
	public class Submitener extends AbstractDefaultAjaxBehavior implements IFormSubmitter {
		@Override
		protected void respond(AjaxRequestTarget target) {
			if (getForm()==null)
				return;
			getForm().process(this);
			MemberFormEditor.this.update(target);
		}
		public void renderHead(final Component component, final IHeaderResponse response) {
			super.renderHead(component, response);
			StringBuilder script = new StringBuilder();
			
			script.append("function submit() { \n ");
			script.append(getCallbackScript());
			script.append("}\n");
			response.render(new JavaScriptContentHeaderItem(script.toString(), "submit"));
		}	
		public Form<?> getForm() {
			return MemberFormEditor.this.getForm();
		};
		public boolean getDefaultFormProcessing() {
			return true;
		}
		public void onSubmit() {
		}
		public void onAfterSubmit()  {
		}
		public void onError() {
		}
		@Override
		protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
			super.updateAjaxAttributes(attributes);

			Form<?> form = getForm();
			
			if (form==null) return;
			
			attributes.setFormId(form.getMarkupId());

			String formMethod = form.getMarkupAttributes().getString("method");
			if (formMethod == null || "POST".equalsIgnoreCase(formMethod)) {
				attributes.setMethod(Method.POST);
			}

			if (getComponent() instanceof IFormSubmittingComponent)	{
				String submittingComponentName = ((IFormSubmittingComponent)getComponent()).getInputName();
				attributes.setSubmittingComponentName(submittingComponentName);
			}
			
			AjaxCallListener myAjaxCallListener = new AjaxCallListener() {
				@Override 
				public CharSequence getBeforeHandler(Component component) { 
					return "if (typeof(tinyMCE) != \"undefined\") tinyMCE.triggerSave(true,true);";
				}
			};
			attributes.getAjaxCallListeners().add(myAjaxCallListener);
			
			attributes.getDynamicExtraParameters().add("return {save: top.issave};");
			
			//updateSubmitenerAttributes(attributes);
		}
	}
	
	/**
	 * 
	 *
	 */
	public class FeedbackPanel extends Fragment {
		public FeedbackPanel() {
			super("feedback", "feedback-fragment", MemberFormEditor.this);
			add(new ListView<String>("message", ()->getMessages()) {
				public void populateItem(ListItem<String> item) {
					item.add(new Label("text", item.getModelObject()));	
				}
			});
		}
		public List<String> getMessages() {
			List<String> messages = new ArrayList<String>();
			for (FeedbackMessage message : MemberFormEditor.this.getFeedbackMessages()) {
				if (message.getMessage() instanceof EFormMessage) {
					EFormMessage eformmessage = (EFormMessage)message.getMessage(); 
					messages.add((String)eformmessage.getMessage());
				}
			}
			return messages;
		}
		
	}	

	
	/**
	 * 
	 *
	 */
	public class KbeeEValidatable implements EValidatable {
		EForm form;
		EFormField<?> field;
		public KbeeEValidatable(EForm form, EFormField<?> field) {
			this.form = form;
			this.field = field;
		}
		public Object getValue() {
			return getData().getData(getField());
		}
		public EFormField<?> getField() {
			return field;
		}
		public EFormData getData() {
			return datamodel.getObject();
		}
		public void error(String key, String... parameter) {
			String message = getLabelString(key, parameter);
			setError(getField(), message);
			MemberFormEditor.this.error(new FieldMessage(MemberFormEditor.this, getEForm(), getField(), message, FeedbackMessage.ERROR));
		}
		public void error(String key) {
			error(key, getField().getLabel());
		}
	}
	
	
	public MemberFormEditor(IModel<DataSetMember> model) {
		this("editor", model, false);
	}
	
	public MemberFormEditor(String id, IModel<DataSetMember> model, boolean isNew) {
		this(id, model, isNew, false);
	}

	public MemberFormEditor(String id, IModel<DataSetMember> model, boolean isNew, boolean isReadOnly) {
		super(id, model);
		setIsNew(isNew);
		setReadOnly(isReadOnly);
		setEditionEnabled(isNew);
		add(new Submitener());
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if(aggregations!=null)
			aggregations.forEach( x -> detach());
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);

		form = new Form<Void>("form", Disposition.VERTICAL);
		
		
		if (!getDataSet().isDisplayNameEditable()) {
			final String url = getServerUrl() + "/model/datasets/"+getModel().getObject().getDataSet().getId().toString()+"?tab=display";
			final String ds = getModel().getObject().getDataSet().getDisplayName();
			form.add( new AlertPanel<DataSetMember>("display-name-generated", AlertPanel.INFO, getModel(), null, getLabel("display-name-generated", url, ds)));
		}
		else {
			form.add( new InvisiblePanel("display-name-generated"));
		}
		
		form.add(new MemberDisplayNamePanel<DataSetMember>("displayname") {
			public boolean isVisible() {
				return !getDataSet().isDisplayNameEditable();
			}
		});
		
		datamodel = getFormData(getModel().getObject());
		form.add(new EFormEditor("eform", datamodel));
		add(form);
		
		Link<DataSetMember> li = new Link<DataSetMember>( "dataset", getModel()) {
			@Override
			public void onClick() {
				setResponsePage( new DataSetPage<DataSet>( new ObjectModel<DataSet>( getModel().getObject().getDataSet())));
			}
			@Override
			public boolean isVisible() {
				return role_admin || role_model;
			}
		};
		
		form.add(li);
		
		add(new FeedbackPanel() {
		});
		
		add(new EditButtonsV5<DataSetMember>(this) {
			public void onSubmitClick(AjaxRequestTarget target) {
				validate();
				if (!MemberFormEditor.this.getFeedbackMessages().hasMessage(FeedbackMessage.ERROR)) {
					super.onSubmitClick(target);
				}
				else {
					target.add(MemberFormEditor.this);
				}
			}
			@Override
			public boolean isVisible() {
				if (getModelObject().getDataSet().isReadonly())
					return isRoot();
				if (getModelObject().getState()==ObjectState.DELETED)
					return false;
				if (isReadOnly())
					return false;
				if (isSupportSessionUser() && !isRoot())
					return false;
				if (role_dataset_members)
					return true;
				if (!isWriteable(getModelObject()))
					return false;
				return true;
			}
		});
		
		add(new AttributeModifier("class", "eform"));

		
		
		AjaxLink<DataSetMember> ar = new AjaxLink<DataSetMember>("apply-rule", getModel()) {
			@Override
			public void onClick(AjaxRequestTarget target) {
				fireScanAll(new EAjaxFormEvent(target));
				target.add(form);
			}
			
			public boolean isVisible() {
				return !getDataSet().isDisplayNameEditable();
			}
		};
		
		form.add(ar);
	}
	

	/**
	 * 
	 */
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedFields().isEmpty()) {
				for (EFormField<?> field : datamodel.getObject().getForm().getFields()) {
					field.set(getModelObject(), datamodel.getObject());
				}
				getModelObject().getService(DOMObjectService.class).update(getUpdatedParts());
				reset();
				//target.add(MemberFormEditor.this.getPage());
				//target.add(MemberFormEditor.this);
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
		
		if (getModelObject().getDataSet().isUniqueValues() && 
			!getModelObject().getDataSet().isHierachical() && 
			!validateUniqueness()) {
			this.error(new EFormMessage(MemberFormEditor.this, getEForm(), "el valor ingresado no es unico", FeedbackMessage.ERROR));
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
	
	public Form<?> getForm() {
		return (Form<?>)get("form");
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
	
	public void setError(EFormField<?> field, Serializable message) {
		onInitialize();
		getFormPanel().setError(field, message);
	}
	
	public List<Classification> getClassification() {
		return getModelObject().getClassification();
	}
	
	protected boolean isWriteable(DataSetMember member) {
		return ServiceLocator
			.getService(UserService.class)
			.isWriteable(member);
	}

	protected List<IModel<DataSet>> getAggregations() {
		if(aggregations!=null)
			return aggregations;
		
		aggregations = new ArrayList<IModel<DataSet>>();
		List<DataSet> list = getModelObject().getDataSet().getService(DataSetService.class).getAggregations();
		
		if (list==null)
			return new ArrayList<IModel<DataSet>>();

		for (DataSet d:list) 
			aggregations.add(new ObjectModel<DataSet>(d));

		return aggregations;
	}
	
	private EFormEditor getFormPanel() {
		return (EFormEditor) (form.get("eform"));
	}
	
	private EForm getEForm() {
		return getFormPanel().getForm();
	}
	
	private List<EFieldPanel<?>> getFieldPanels() {
		List<EFieldPanel<?>> panels = new ArrayList<EFieldPanel<?>>();
		for (Panel panel: getFormPanel().getPanelFactory().getPanels()) {
			if (panel instanceof EFieldPanel) {
				panels.add((EFieldPanel<?>)panel);
			}
		}
		
		return panels;
	}
	
	private IModel<EFormData> getFormData(DataSetMember member) {
		EForm form = getForm(member);
		EFormData data = new KbeeEMemMemberData(form, member);
		for (EFormField<?> field : form.getFields()) {
			field.get(member, data);
		}	
		IModel<EFormData> model = new EFormDataModel(data);
		return model;
	}
	
	private EForm getForm(DataSetMember member) {
		return new com.novamens.kbee.content.form.KbeeMemberForm(member);
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