package kbee.web.model.contentclass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.Multiplicity;
import com.novamens.kbee.content.model.KbeeClassifierTemplate;

import kbee.web.form.RelationEditor;

@Deprecated
@SuppressWarnings("serial")
public class ClassifiersEditor extends RelationEditor<ContentTemplate, ClassifierTemplate> {
	private static final long serialVersionUID = 1L;

	public ClassifiersEditor() {
		super("classifiers");
	}
	
	@Override
	protected String getText(ClassifierTemplate value) {
		String type = "<span class=\"label\"> " + new StringResourceModel("type", ClassifiersEditor.this, null).getObject() + ":</span> <span class=\"highlight\"> " + value.getClassifier().getDataSetType().getLabel() + "</span>" ;
		return type + ". " + super.getText(value);
	}
	
	@Override
	protected List<Property<?>> getProperties() {
		
		List<Property<?>> properties = new ArrayList<Property<?>>();
		
		properties.add(new Property<Boolean>() {
			public String getName() {
				return "visible";
			}
			public boolean isBoolean() {
				return true;
			}
		});
		
		properties.add(new Property<Boolean>() {
			public String getName() {
				return "MetadataSubtitle";
			}
			public boolean isBoolean() {
				return true;
			}
		});
		
		properties.add(new Property<Boolean>() {
			public String getName() {
				return "inherited";
			}
			public boolean isBoolean() {
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
	
	public List<Multiplicity> getMultiplicities() {
		List<Multiplicity> multiplicities = new ArrayList<Multiplicity>();
		multiplicities.add(Multiplicity.M01);
		multiplicities.add(Multiplicity.M11);
		multiplicities.add(Multiplicity.M0N);
		multiplicities.add(Multiplicity.M1N);
		return multiplicities;
	}
	
	@Override
	protected ClassifierTemplate getNewValue() {
		KbeeClassifierTemplate template = new KbeeClassifierTemplate();
		template.setVisible(true);
		return template;
	}
	
	public List<Classifier> getClassifiers() {
		List<Classifier> classifiers =  new ArrayList<Classifier>();
		List<Classifier> domainclassifiers =  getContentDao().getClassifiers(getDomain());
		for (Classifier classifier : domainclassifiers) {
			boolean found = false;
			for (IModel<ClassifierTemplate> model : getValues()) {
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
				} catch (RuntimeException e) {
					return 0;
				}
			}
		}); 
		return classifiers;
	}
	
//	private ContentDao getContentDao() {
//		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
//	
//	private Domain getDomain() {
//		return ServiceLocator.getService(UserService.class).getDomain();
//	}
}
