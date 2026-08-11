package kbee.web.security.role;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.model.Classifier;
import com.novamens.content.security.Role;
import com.novamens.content.web.security.markup.ConditionWizardPanel;
import com.novamens.kbee.content.security.KbeeAbstractRole;
import com.novamens.kbee.content.security.KbeeEntityRole;

public class RoleConditionWizardPanel<T extends Role> extends ConditionWizardPanel<T> {
	
	private static final long serialVersionUID = 1L;
	
	private List<Classifier> classifiers;
	@Override
	public void updateModel() {
		
		String condition = getCondition();
		
		if ((condition==null && getObjectCondition()!=null) || !condition.equals(getObjectCondition())) {
			((KbeeAbstractRole)getModelObject()).setCondition(condition.toString());
			((KbeeAbstractRole)getModelObject()).setDisplayCondition(getDescription());
			setUpdatedPart("condition");
		}
	}
	
	
	@Override
	public String getObjectCondition() {
		return ((KbeeAbstractRole)getEditor().getModelObject()).getCondition();
	}
	
	
	public void onDetach() {
		super.onDetach();
		this.classifiers=null;
	}
	
	@Override
	protected List<Classifier> getClassifiers() {
		
		if (classifiers!=null)
			return classifiers;
				
		classifiers = new ArrayList<Classifier>();
		Role role = getModelObject();
		Classifier scope = role instanceof KbeeEntityRole ? ((KbeeEntityRole)role).getClassifier() : null;
		for (Classifier classifier : super.getClassifiers()) {
			if ((scope!=null && !classifier.equals(scope)) || scope==null) {
				classifiers.add(classifier);
			}
		}
		return classifiers;
	}
}
