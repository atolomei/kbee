package com.novamens.content.web.content.markup;

import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.RelationshipByCriteria;
import com.novamens.content.relationshipsbycriteria.RelationshipByCriteriaTemplate;
import com.novamens.content.web.editor.markup.ObjectEditorPanel;
import com.novamens.kbee.content.model.KbeeRelationshipByCriteria;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;

@SuppressWarnings("serial")
public class RelationByCriteriaPanel<T extends Content> extends ObjectEditorPanel<T> {
	private static final long serialVersionUID = 1L;
	
	private IModel<RelationshipByCriteriaTemplate> templatemodel;
	
	private class RelationModel implements IModel<RelationshipByCriteria> {
		private IModel<RelationshipByCriteriaTemplate> templatemodel;
		private KbeeRelationshipByCriteria relation;
		private String condition;

		public RelationModel(IModel<RelationshipByCriteriaTemplate> templatemodel) {
			this.templatemodel = templatemodel;
		}
		public void setObject(RelationshipByCriteria relation) {
		}
		public RelationshipByCriteria getObject() {
			for (RelationshipByCriteria relation : RelationByCriteriaPanel.this.getModelObject2().getRelationshipsByCriteria()) {
				if (relation.getTemplate().equals(getTemplate())) {
					return relation;
				}
			}
			relation = new KbeeRelationshipByCriteria();
			relation.setTemplate(getTemplate());
			if (condition!=null) relation.setCondition(condition);
			return relation;
		}
		public void detach() {
			if (relation!=null) {
				condition = relation.getCriteria();
				relation=null;
			}
			templatemodel.detach();
		}
	}

	public RelationByCriteriaPanel(String id) {
		super(id);
		setOutputMarkupId(true);
	}
	
	public void setTemplateModel(IModel<RelationshipByCriteriaTemplate> model) {
		this.templatemodel = model;
	}
	
	public IModel<RelationshipByCriteriaTemplate> getTemplateModel() {
		return this.templatemodel;
	}
	
	public RelationshipByCriteriaTemplate getTemplate() {
		return getTemplateModel().getObject();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addEditor();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (getTemplateModel()!=null)
			getTemplateModel().detach();
	}
	
	protected void addEditor() {
		
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new RelationConditionWizardPanel<T>(new RelationModel(getTemplateModel())));
		
		add(form);
	}
}