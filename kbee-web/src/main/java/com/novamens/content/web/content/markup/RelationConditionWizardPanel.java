package com.novamens.content.web.content.markup;

import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.RelationshipByCriteria;
import com.novamens.content.web.security.markup.ConditionWizardPanel;
import com.novamens.kbee.content.model.KbeeRelationshipByCriteria;

public class RelationConditionWizardPanel<T extends Content> extends ConditionWizardPanel<T> {
	private static final long serialVersionUID = 1L;

	IModel<RelationshipByCriteria> relationmodel;
	
	public RelationConditionWizardPanel(IModel<RelationshipByCriteria> relationmodel) {
		this.relationmodel = relationmodel;
	}
	
	@Override
	public void updateModel() {
		String condition = getCondition();
		if ((condition==null && getRelation().getCriteria()!=null) || !condition.equals(getRelation().getCriteria())) {
			((KbeeRelationshipByCriteria)getRelation()).setCondition(condition);
			getModelObject().addRelation(getRelation());
			setUpdatedPart("condition");
		}
	}
		
	public	RelationshipByCriteria getRelation() {
		return this.relationmodel.getObject();
	}
	
	@Override
	public String getObjectCondition() {
		return getRelation().getCriteria();
	}
	
	public void onDetach() {
		super.onDetach();
		relationmodel.detach();
	}
}
