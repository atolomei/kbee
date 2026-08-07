package com.novamens.content.web.security.markup;

import com.novamens.content.base.Rule;

public class RuleConditionWizardPanel<T extends Rule> extends ConditionWizardPanel<T> {
	private static final long serialVersionUID = 1L;

	@Override
	public void updateModel() {
		String condition = getCondition();
		if ((condition==null && getModelObject().getCondition()!=null) || !condition.equals(getModelObject().getCondition())) {
			getModelObject().setCondition(condition.toString());
			getModelObject().setDisplayCondition(getDescription());
			setUpdatedPart("condition");
		}
	}
	
	@Override
	public String getObjectCondition() {
		return getEditor().getModelObject().getCondition();
	}
	
	@Override
	protected boolean includeMonitorCondition() {
		return false;
	}
}
