package kbee.web.model.procedure;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.Multiplicity;
import com.novamens.kbee.content.workflow.TaskClassifierTemplate;
import com.novamens.kbee.content.workflow.WebTask;

import kbee.web.form.RelationEditor;

@Deprecated
@SuppressWarnings("serial")
public class TaskClassifiersEditor extends RelationEditor<WebTask, ClassifierTemplate> {
	private static final long serialVersionUID = 1L;

	public TaskClassifiersEditor() {
		super("classifierTemplates");
		setOutputMarkupId(true);
	}
	
	@Override
	protected List<Property<?>> getProperties() {
		List<Property<?>> properties = new ArrayList<Property<?>>();
		
		properties.add(new Property<Classifier>() {
			public String getName() {
				return "classifier";
			}
			public List<Classifier> getChoices() {
				return getClassifiers();
			}
			public boolean getTitle() {
				return true;
			}
			public boolean isSelectable() {
				return true;
			}
			public boolean getKey() {
				return true;
			}
		});
		
		properties.add(new Property<Multiplicity>() {
			public String getName() {
				return "multiplicity";
			}
			public List<Multiplicity> getChoices() {
				return getMultiplicities();
			}
			public boolean getTitle() {
				return true;
			}
			public boolean isSelectable() {
				return true;
			}
			public boolean getKey() {
				return false;
			}
			public Multiplicity getMultiplicity() {
				return Multiplicity.M01;
			}
		});
		
		properties.add(new Property<Boolean>() {
			public String getName() {
				return "readOnly";
			}
			public boolean isBoolean() {
				return true;
			}
		});
		
		return properties;
	}
	
	public List<Multiplicity> getMultiplicities() {
		List<Multiplicity> multiplicities = new ArrayList<Multiplicity>();
		multiplicities.add(Multiplicity.M01);
		multiplicities.add(Multiplicity.M11);
		multiplicities.add(Multiplicity.M0N);
		multiplicities.add(Multiplicity.M1N);
		return multiplicities;
	}
	
	public List<Classifier> getClassifiers() {
		List<Classifier> classifiers =  new ArrayList<Classifier>();
		classifiers =  getContentDao().getClassifiers(getDomain());
		return classifiers;
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
	protected ClassifierTemplate getNewValue() {
		return new TaskClassifierTemplate();
	}
}
