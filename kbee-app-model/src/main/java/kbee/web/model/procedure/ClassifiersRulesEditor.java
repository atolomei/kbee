package kbee.web.model.procedure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.service.DataAccessService;
import com.novamens.content.workflow.ClassificationRule;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.content.workflow.KbeeClassificationRule;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.form.RelationEditor;

@SuppressWarnings("serial")
public class ClassifiersRulesEditor<T> extends RelationEditor<T, ClassificationRule> {
	private static final long serialVersionUID = 1L;
	
	public class ValueProperty extends Property<DataSetMember> {
		private IModel<Classifier> model;
		@Override
		public DataSetMember getValue(ClassificationRule value) {
			model = new ObjectModel<Classifier>(value.getClassifier());
			model.detach();
			return super.getValue(value);
		}
		@Override
		public List<Suggestion> getSuggestions(String pattern) {
			Classifier classifier = model.getObject();
			model.detach();
			return classifier.getService(DataAccessService.class).getSuggestions(pattern); 
		}
		@Override
		public IModel<DataSetMember> getModel(DataSetMember value) {
			return value!=null ? new ObjectModel<DataSetMember>(value) : null;
		}
		@Override
		public String getHistoryKey() {
			return null;
		}
		@Override
		public boolean isAutocomplete() {
			return true;
		}
		@Override
		public Multiplicity getMultiplicity() {
			return Multiplicity.M01;
		}

	}
	
	public ClassifiersRulesEditor() {
		this("rules");
	}

	public ClassifiersRulesEditor(String id) {
		super(id);
		setPropertyModel(new PropertyModel<Collection<ClassificationRule>>(this, "rules"));
	}
	
	@Override
	public IModel<String> getHelp() {
		return null;
	}
	
	@Override
	public boolean isItemLink() {
		return false;
	}

	public List<Classifier> getClassifiers() {
		List<Classifier> classifiers =  new ArrayList<Classifier>();
		
		List<Classifier> domainclassifiers =  getContentDao().getClassifiers(getDomain().getId(), ObjectState.ENABLED);
		
		for (Classifier classifier : domainclassifiers) {
			
			boolean found = false;
			
			for (IModel<ClassificationRule> model : getValues()) {
				if (model.getObject().getClassifier().equals(classifier)) {
					found = true;
					break;
				}
			}
			if (!found && getEditor()!=null && 
				classifier.getDataSet()!=null &&		
				classifiers.add(classifier));
		}
		
		Collections.sort(classifiers, new Comparator<Classifier>() {
			@Override
			public int compare(Classifier a, Classifier b) {
				try{
					return a.getName().compareToIgnoreCase(b.getName());
				} catch (Exception e) {
					return 0;
				}
			}
		}); 
		return classifiers;
	}
	
	public List<ClassificationRule> getRules() {
		return new ArrayList<ClassificationRule>();
	}
	
	@Override
	public boolean ordered() {
		return true;
	}
	
	@Override
	protected String getTitle(ClassificationRule value) {
		String title = value.getClassifier().getDisplayName() +
				((value.getValue()!=null) ?	
				" <span class=\"highlight\">(" + value.getValue().getDisplayName() + ")</span>"
			    : "");
		return title;
	}
	
	public void setRules(List<ClassificationRule> rules) {
	}

	@Override
	protected List<Property<?>> getProperties() {
		List<Property<?>> properties = new ArrayList<Property<?>>();
		properties.add(new ValueProperty() {
			@Override
			public String getName() {
				return "value";
			}
		});
		return properties;
	}

	protected Property<?> getKey() {
		return new Property<Classifier>() {
			public String getName() {
				return "classifier";
			}
			public List<Classifier> getChoices() {
				return getClassifiers();
			}
		};
	}
	
	@Override
	protected ClassificationRule getNewValue() {
		KbeeClassificationRule rule = new KbeeClassificationRule();
		return rule;
	}
	
	@Override
	protected int compare(IModel<ClassificationRule> a, IModel<ClassificationRule> b) {
		if (a.getObject().getValue().getDisplayName()==null)
			return (b.getObject().getValue().getDisplayName()!=null?1:0);
		else if(b.getObject().getValue().getDisplayName()==null)
			return -1;
		return a.getObject().getValue().getDisplayName().compareToIgnoreCase(b.getObject().getValue().getDisplayName());
	}
}