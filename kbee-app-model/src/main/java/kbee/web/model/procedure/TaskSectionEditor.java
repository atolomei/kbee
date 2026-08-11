package kbee.web.model.procedure;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.ModelSection;
import com.novamens.kbee.content.model.KbeeCodeExecutor;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.workflow.Task;

@Deprecated
@SuppressWarnings("serial")
public class TaskSectionEditor extends ObjectEditorPanel<Task> {
	private static final long serialVersionUID = 1L;

	static Logger logger = LogManager.getLogger(TaskSectionEditor.class.getName());
	
	IModel<ModelSection> model;
	IModel<ContentTemplate> templateModel;
	
	public TaskSectionEditor(String id, IModel<ModelSection> model, IModel<ContentTemplate> templateModel) {
		super(id);
		
		this.model = model;
		this.templateModel = templateModel;
		
		add(new TextField<String>("section", new PropertyModel<String>(model, "name")));
		
		add(new SectionStructureEditor<Task>(model, templateModel) {
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				TaskSectionEditor.this.onUpdate(target);
			}
			@Override
			protected List<ModelElementTemplate> getTemplateStructure() {
				return TaskSectionEditor.this.getTaskStructure();
			}
			@Override
			protected void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, () -> { return "How to write a Script"; }, 
					getScriptHelp());
			}
		});
		
		add(new InfoDialog("help-modal"));
	}
	
	public IModel<ModelSection> getSectionModel() {
		return model;
	}
	
	public IModel<ContentTemplate> getTemplateModel() {
		return templateModel;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		templateModel.detach();
	}
	
	protected void onUpdate(AjaxRequestTarget target) {
		
	}
	
	@SuppressWarnings("unchecked")
	protected List<ModelElementTemplate> getStructure() {
		List<ModelElementTemplate> structure = new ArrayList<ModelElementTemplate>();
		for (IModel<ModelElementTemplate> model : ((SectionStructureEditor<ContentTemplate>)get("structure")).getValues()) {
			structure.add(model.getObject());
		}
		return structure;
	}
	
	
	protected List<ModelElementTemplate> getTaskStructure() {
		return getStructure();
	}
	
	protected InfoDialog getHelpModal() {
		return (InfoDialog) get("help-modal");
	}
	
	protected IModel<String> getScriptHelp() {
		return new Model<String>(KbeeCodeExecutor.GetHelpText(getTemplateModel().getObject()));
	}
}
