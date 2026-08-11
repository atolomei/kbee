package kbee.web.rule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.rule.ActionRule;
import com.novamens.content.rule.ClassificationAction;
import com.novamens.content.rule.RemoveClassificationAction;
import com.novamens.kbee.content.rule.KbeeClassificationAction;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;
import com.novamens.wicket.markup.html.form.ChoiceField;
import com.novamens.wicket.markup.html.form.StaticField;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;

@SuppressWarnings("serial")
public class ClassificationActionEditor extends ObjectEditorPanel<ActionRule> {
	private static final long serialVersionUID = 1L;
																										
	private static Logger logger = Logger.getLogger(ClassificationActionEditor.class.getName());
	
	private ClassificationAction action;
	private IModel<Classifier> classifiermodel =  null;
	private List<IModel<DataSetMember>> valuemodels =  null;
	
	public ClassificationActionEditor(ClassificationAction action) {
		super("editor");
		
		this.action = action;
		
		setOutputMarkupId(true);
		
		if (action instanceof RemoveClassificationAction)
		add(new StaticField<String>("type", new StringResourceModel("remove-type", this)));
		else
		add(new StaticField<String>("type", new StringResourceModel("type", this)));
		
		setClassifier(((KbeeClassificationAction)action).getClassifier());
		add(new ChoiceField<Classifier>("classifier", new PropertyModel<Classifier>(this, "classifier"), new PropertyModel<List<Classifier>>(this, "classifiers")) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				setClassifier(getValue());
				setValues(new ArrayList<DataSetMember>());
				ClassificationActionEditor.this.addValuesEditor();
				target.add(ClassificationActionEditor.this);
			}
		});
		
		setValues(((KbeeClassificationAction)action).getValues());
		addValuesEditor();
	}
		
	public Classifier getClassifier() {
		return classifiermodel!=null ? classifiermodel.getObject() : null;
	}
	
	public void setClassifier(Classifier classifier) {
		classifiermodel = classifier!=null ? new ObjectModel<Classifier>(classifier) : null;
	}
	
	public Collection<DataSetMember> getValues() {
		Collection<DataSetMember> values = new ArrayList<DataSetMember>();
		if (valuemodels==null)
			return values;
		for (IModel<DataSetMember> model : valuemodels) {
			values.add(model.getObject());
		}
		return values;
	}
	
	public void setValues(Collection<DataSetMember> values) {
		valuemodels = new ArrayList<IModel<DataSetMember>>();
		for (DataSetMember value : values) {
			valuemodels.add(new ObjectModel<DataSetMember>(value)); 
		}
	}
		
	
	
	public List<Classifier> getClassifiers() {
		
		List<Classifier> classifiers = new ArrayList<Classifier>();
		for (Classifier classifier : getContentDao().getClassifiers(getModelObject().getDomain())) {
			classifiers.add(classifier);
		}
		
		
		Collections.sort(classifiers , new Comparator<Classifier>() {

			@Override
			public int compare(Classifier o1, Classifier o2) {
				if (o1.getDisplayName()==null)
					return 1;
				if (o2.getDisplayName()==null)
					return -1;
				return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
			}
			
		});
		
		
		return classifiers;
	}
	
	public void updateModel() {
		try {
			KbeeClassificationAction kbeeaction = (KbeeClassificationAction)action;
			if (getClassifier()!=null)
				kbeeaction.setClassifier(getClassifier());
			if (getValues()!=null) {
				kbeeaction.setValues(getValues());
			}			
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (classifiermodel!=null)
			classifiermodel.detach();

		if (valuemodels!=null) {
			valuemodels.forEach( model -> model.detach());
			
		}
	}
	
	private void addValuesEditor() {
		addOrReplace(new ValuesEditor(new PropertyModel<Collection<DataSetMember>>(this, "values")) {
			@Override
			protected Classifier getClassifier() {
				return ClassificationActionEditor.this.getClassifier();
			}
		});
	}
	
//	private ContentDao getContentDao() {
//		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
}
