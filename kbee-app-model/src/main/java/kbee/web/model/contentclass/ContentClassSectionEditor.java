package kbee.web.model.contentclass;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.ModelSection;
import com.novamens.kbee.content.model.KbeeCodeExecutor;
import com.novamens.kbee.content.model.KbeeModelSection;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.modal.InfoDialog;


@SuppressWarnings("serial")
public class ContentClassSectionEditor extends ObjectEditorPanel<ContentTemplate> {

	private static final long serialVersionUID = 1L;

	private IModel<ModelSection> model;
	
	public ContentClassSectionEditor(String id, IModel<ModelSection> model) {
		super(id);
		this.model = model;
	}
	
	public IModel<ModelSection> getSectionModel() {
		return model;
	}
	
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("structure")==null) {
			add(new ContentTemplateSectionStructureEditor<ContentTemplate>(model, getModel()) {
				public void updateModel() {
					if (updated())
					((KbeeModelSection)getSectionModel().getObject()).setDefault(false);
					super.updateModel();
				}
				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					ContentClassSectionEditor.this.onUpdate(target);
				}
				@Override
				protected List<ModelElementTemplate> getTemplateStructure() {
					return ContentClassSectionEditor.this.getTemplateStructure();
				}
				@Override
				protected void onHelp(AjaxRequestTarget target) {
					getHelpModal().open(target, () -> { return "How to write a Script"; }, 
						getScriptHelp());
				}
			});
			add(new InfoDialog("help-modal"));
		}
	}
	
	protected void onUpdate(AjaxRequestTarget target) {
		
	}
	
	@SuppressWarnings("unchecked")
	protected List<ModelElementTemplate> getStructure() {
		List<ModelElementTemplate> structure = new ArrayList<ModelElementTemplate>();
		
		if (get("structure")!=null) {
			for (IModel<ModelElementTemplate> model : ((ContentTemplateSectionStructureEditor<ContentTemplate>)get("structure")).getValues()) {
				structure.add(model.getObject());
			}
		}
		return structure;
	}
	
	protected IModel<String> getScriptHelp() {
		return new Model<String>(KbeeCodeExecutor.GetHelpText(getModel().getObject()));
	}
	
	protected InfoDialog getHelpModal() {
		return (InfoDialog) get("help-modal");
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected List<ModelElementTemplate> getTemplateStructure() {
		return getStructure();
	}
}
