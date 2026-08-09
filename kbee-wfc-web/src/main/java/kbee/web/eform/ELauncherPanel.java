package kbee.web.eform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.ResourceNode;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.entity.Person;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormAccessLevel;
import com.novamens.content.form.EFormData;
import com.novamens.content.form.EFormField;
import com.novamens.content.form.EValidatable;
import com.novamens.content.form.UpdatedField;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.service.ContentService;
import com.novamens.content.user.UserProfile;
import com.novamens.content.workflow.ProcessLauncher;
import com.novamens.kbee.content.document.KbeeIDoc;
import com.novamens.kbee.content.form.KbeeEMemContentData;
import com.novamens.kbee.content.script.KbeeClassificableScriptWrapper;
import com.novamens.kbee.content.workflow.ManualEndCondition;
import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Process;
import com.novamens.workflow.Task;

import kbee.web.workflow.PersonCollaboratorSelector;

@SuppressWarnings("serial")
public class ELauncherPanel extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	private String note;
	private IModel<ProcessLauncher> launchermodel;
	private IModel<Content> contentmodel;
	private IModel<Person> collaboratormodel;
	private IModel<ResourceTag> donetagmodel;
	private IModel<ResourceTag> targettagmodel;
	private List<IModel<ResourceNode>> resources;
	
	EFormDataModel model;
	
	public class EContentEditor extends Fragment implements Editor<Content> {
		EFormEditor eform;
		
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
				return model.getObject();
			}
			public void error(String key) {
				error(key, getField().getLabel());
			}
			public void error(String key, String... parameter) {
				String message;
				try {
					message = getLabelString(key, parameter);
				}
				catch (Exception e) {
					message = key;
				}
				eform.setError(getField(), message);
			}
		}
		
		public EContentEditor(EFormEditor eform) {
			super("eform", "eform-fragment", ELauncherPanel.this);
			this.eform = eform;
			if (eform!=null)
				add(eform);
			else
				add(new InvisiblePanel("eform"));
		}
		
		public void update(AjaxRequestTarget target) {
		}
		
		public void edit(AjaxRequestTarget target) {
		}
		
		public Form<?> getForm() {
			return null;
		}
		
		public IModel<Content> getModel() {
			return null;
		}
		
		public Content getModelObject() {
			return null;
		}
		
		public void update(Content object) {
		}
		
		public boolean isEditionEnabled() {
			return true;
		}
		
		public boolean isReadOnly() {
			return true;
		}
		
		public void setIsNew(boolean isnew) {
		}
		
		public boolean isNew() {
			return true;
		}
		
		public boolean isFullWidth() {
			return true;
		}
		
		public List<UpdatedField> getUpdatedFields() {
			return null;
		}
		
		public void setUpdatedField(UpdatedField updatedField) {
			
		}
		
		public List<String> getUpdatedParts() {
			return null;
		}
		
		public void setUpdatedPart(String updatedPart) {
		}
		
		public boolean isVisible() {
			return eform!=null;
		}
		
		
		public void validate() {
			eform.clearMessages();
			for (EFormField<?> field : getEForm().getFields()) {
				field.validate(new KbeeEValidatable(getEForm(), field)); 
			}
		}
		
		public EForm getEForm() {
			return model.getObject().getForm();
		}

	}	
	
	public ELauncherPanel(IModel<Content> contentmodel, 
			List<IModel<ResourceNode>> resources, 
			IModel<ProcessLauncher> launchermodel, 
			IModel<ResourceTag> sourcetagmodel, 
			IModel<ResourceTag> targettagmodel) {
		
		super("action-panel");
		setOutputMarkupId(true);
		
		setLauncher(launchermodel);
		setContent(contentmodel);
		setDoneTag(sourcetagmodel);
		setTargetTag(targettagmodel);
		setResources(resources);
		
		add ( (new Label("confirmation-message", getMessageModel())).setEscapeModelStrings(false));
		
		add(new EContentEditor(getFormEditor(getLauncher())));
		
		add(new PersonCollaboratorSelector(getConditionModel(), 
			new PropertyModel<Person>(this, "collaborator"),
			getUserSetModel()));
		
		add(new TextAreaField<String>("note", new PropertyModel<String>(this, "note")) {
			public void onUpdate(AjaxRequestTarget target) {
				setNote(getValue());
			}
		});
		
		add(new AjaxLink<Void>("submit-button") {
			public void onClick(AjaxRequestTarget target) {
				validateForm();
				boolean needcollaboration = getConditionModel().getObject().getCollaboration();
				if ((!needcollaboration || getCollaborator()!=null) && !hasErrorMessage()) {
					onBeforeLaunch(target);
					Process process = getContent().getService(ContentService.class).startProcess(
						getLauncher(), 
						getInitialProcessData(), 
						getResources(), 
						getCollaborator()!=null ? getUser(getCollaborator()) : null,
						getNote(),
						getDoneTag(), 
						getTargetTag());
					onAfterLaunch(target, process);
				}
				target.add(ELauncherPanel.this);
			}
		});
		add(new AjaxLink<Void>("cancel-link") {
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
		});
		add(new FeedbackPanel("feedback") {
			@Override
			public boolean isVisible() {
				return true;
			}
		});
	}

	public void setLauncher(IModel<ProcessLauncher> model) {
		this.launchermodel = model;
	}
	
	public ProcessLauncher getLauncher() {
		return launchermodel!=null ? launchermodel.getObject() : null;
	}
	
	public void setContent(IModel<Content> model) {
		this.contentmodel = model;
	}
	
	public Content getContent() {
		return contentmodel!=null ? contentmodel.getObject() : null;
	}
	
	public void setResources(List<IModel<ResourceNode>> resources) {
		this.resources = resources;
	}
	
	public List<ResourceNode> getResources() {
		List<ResourceNode> resources = new ArrayList<>();
		for (IModel<ResourceNode> model : this.resources) {
			resources.add(model.getObject());
		}
		return resources;
	}
	
	public void setCollaborator(Person person) {
		collaboratormodel = person!=null ? new ObjectModel<Person>(person) : null;
	}
	
	public Person getCollaborator() {
		return collaboratormodel!=null ? collaboratormodel.getObject() : null;
	}
	
	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public void setDoneTag(IModel<ResourceTag> model) {
		this.donetagmodel = model;
	}
	
	public ResourceTag getDoneTag() {
		return this.donetagmodel!=null ? this.donetagmodel.getObject() : null;
	}
	
	public void setTargetTag(IModel<ResourceTag> model) {
		this.targettagmodel = model;
	}
	
	public ResourceTag getTargetTag() {
		return this.targettagmodel!=null ? this.targettagmodel.getObject() : null;
	}
	
	public IModel<String> getMessageModel() {
		Model<String> messagemodel = new Model<String> () {
			public String getObject() {
				return getLauncher()!=null ? getLabelString("confirmation-message", getLauncher().getDisplayName()) : "";
			}
		};
		return messagemodel;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		for (IModel<ResourceNode> model : resources) {
			model.detach();
		}
	}
	
	protected void onBeforeLaunch(AjaxRequestTarget target) {
	}
	
	protected void onAfterLaunch(AjaxRequestTarget target, Process process) {
	}
	
	protected void onCancel(AjaxRequestTarget target) {
	}
	
	private void validateForm() {
		if (get("eform")!=null) {
			((EContentEditor)get("eform")).validate();
		}
	}
	
	private EFormEditor getFormEditor(ProcessLauncher launcher) {
		EFormDataModel datamodel =	getFormDataModel(launcher.getContentTemplate());
		if (datamodel==null) return null;
		EFormEditor editor = new EFormEditor("eform", datamodel);
		return editor;
	}
	
	private HashMap<?,?> getInitialProcessData() {
		HashMap<String, Object> data = new HashMap<>();
		if (model!=null) {
			EFormData formData = model.getObject();
			for (EFormField<?> field : formData.getForm().getFields()) {
				Object fieldData = formData.getData(field);
				data.put(field.getName(), fieldData);
			}
		}
		data.put("sourcecontent", new KbeeClassificableScriptWrapper(getContent()));
		return data;
	}
	
	private EFormDataModel getFormDataModel(ContentTemplate template) {
		EForm form = getForm(template);
		if (form==null) return null;
		KbeeIDoc idoc = new KbeeIDoc();
		idoc.setContentTemplate(template);
		EFormData data = new KbeeEMemContentData(form, idoc);
		model = new EFormDataModel(data);
		return model;
	}
	
	private EForm getForm(ContentTemplate template) {
		for (EForm form : template.getForms()) {
			if (EFormAccessLevel.PROCESS_LAUNCHER.equals(form.getFormAccessLevel())) {
				return form;
			}
		}
		return null;
	}
	
	private User getUser(Person person) {
		UserProfile profile = person.getProfile(UserProfile.class);
		User user = profile.getUser();
		return user;
	}

	private IModel<ManualEndCondition> getConditionModel() {
		boolean value = false;
		for (Task task : getLauncher().getProcedure().getTasks()) {
			if (task.isInitial()) {
				if (com.novamens.workflow.TriggerType.COLLABORATOR.equals(task.getTriggerType())) {
					value = true;
					break;
				}
			}
		}
		final boolean collaboration = value;
		return new Model<ManualEndCondition>() {
			public ManualEndCondition getObject() {
				ManualEndCondition condition = new ManualEndCondition("", "");
				condition.setCollaboration(collaboration);
				return condition;
			}
		};
	}
	
	private IModel<DataSet> getUserSetModel() {
		DataSet collaborationset = getContentDao().getUserSet();
		return new ObjectModel<DataSet>(collaborationset);
	}
}