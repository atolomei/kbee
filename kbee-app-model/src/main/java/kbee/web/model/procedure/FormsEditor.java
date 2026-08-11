package kbee.web.model.procedure;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.model.IModel;

import com.novamens.content.form.EForm;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.workflow.ContentProcedure;
import com.novamens.kbee.content.workflow.KbeeTask;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.wicket.markup.html.form.FormLayout;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.Task;

import kbee.web.error.ApplicationErrorPage;
import kbee.web.form.RelationEditor;

@SuppressWarnings("serial")
public class FormsEditor extends RelationEditor<WebTask, EForm> {	
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(FormsEditor.class.getName());

	private static final long serialVersionUID = 1L;
	
	private IModel<ContentTemplate> templatemodel;
	private IModel<Task> taskmodel;

	public FormsEditor(String id, IModel<Task> taskmodel) {
		super(id);
		this.taskmodel = taskmodel;
		KbeeTask task = (KbeeTask)taskmodel.getObject();
		ContentProcedure procedure = (ContentProcedure)task.getProcedure();
		this.templatemodel = new ObjectModel<ContentTemplate>(procedure.getContentTemplate());
	}
	
	public ContentTemplate getTemplate() {
		return templatemodel.getObject();
	}
	
	@Override
	public String getTarget() {
		return null;
	}
	
	@Override
	public String getProperty() {
		return "forms"; 
	}
	
	public Task getTask() {
		return taskmodel.getObject();
	}
	
	protected void onValueClick(IModel<EForm> model) {
		
		try {
			if (model.getObject() instanceof KbeeTaskForm) {
				setResponsePage(new RedirectPage( getServerUrl()+"/eform/"+getTemplate().getId().toString()+"/"+ ((KbeeTaskForm)model.getObject()).getId()));				
			}
		} 
		catch (Exception e) {
			logger.error(e);
			setResponsePage(new ApplicationErrorPage<>(e));
		}
	}


	@Override
	protected List<Property<?>> getProperties() {
		List<Property<?>> properties = new ArrayList<Property<?>>();
		
		properties.add(new Property<Boolean>() {
			public String getName() {
				return "readOnly";
			}
			public boolean getTitle() {
				return true;
			}
			public boolean isBoolean() {
				return true;
			}
		});
		
		properties.add(new Property<Boolean>() {
			public String getName() {
				return "signatureRequired";
			}
			public boolean getTitle() {
				return true;
			}
			public boolean isBoolean() {
				return true;
			}
		});
		
		
		properties.add(new Property<FormLayout>() {
			public String getName() {
				return "FormLayout";
			}
			public boolean getTitle() {
				return true;
			}
			public boolean isSelectable() {
				return true;
			}
			public Multiplicity getMultiplicity() {
				return Multiplicity.M01;
			}
			public List<FormLayout> getChoices() {
				return getFormLayouts();
			}
			
		});
		
		return properties;
	}
	
	
	public List<FormLayout> getFormLayouts() {
		List<FormLayout> li = new ArrayList<FormLayout>();
		
		li.addAll(FormLayout.getFormLayouts());
		return li;
	}
	
	
	protected Property<?> getKey() {
		return new Property<EForm>() {
			public String getName() {
				return "form";
			}
			public List<EForm> getChoices() {
				return getForms();
			}
		};	
	}
	
	protected String getTitle(EForm value) {
		return value.getDisplayName()+ " ( "+value.getName()+" )";
	}
	
	protected List<EForm> getForms() {
		List<EForm> forms =  new ArrayList<EForm>();
		forms.addAll(getTemplate().getForms());
		for (EForm form : forms) {
			form.getFields();
		}
		return forms;
	}
	
	@Override
	protected EForm getNewValue() {
		EForm form = new KbeeTaskForm();
		return form;
	}
}