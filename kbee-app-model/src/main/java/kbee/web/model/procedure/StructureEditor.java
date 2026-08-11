package kbee.web.model.procedure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.model.IModel;

import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.Multiplicity;
import com.novamens.kbee.content.model.KbeeModelElementTemplate;
import com.novamens.kbee.content.workflow.WebTask;

import kbee.web.form.RelationEditor;

@Deprecated
@SuppressWarnings("serial")
public class StructureEditor extends RelationEditor<WebTask, ModelElementTemplate> {
	private static final long serialVersionUID = 1L;

	public StructureEditor() {
		super("structure");
	}
	
//	@Override
//	public void onInitialize() {
//		super.onInitialize();
//	}
	
	@Override
	protected List<Property<?>> getProperties() {
		List<Property<?>> properties = new ArrayList<Property<?>>();
		
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
			public boolean getTitle() {
				return true;
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
	
	public List<ModelElement> getElements() {
		List<ModelElement> elements =  new ArrayList<ModelElement>();
		List<Attribute> domainattributes =  getContentDao().getAttributes(getDomain());
		for (Attribute attribute : domainattributes) {
			boolean found = false;
			for (IModel<ModelElementTemplate> model : getValues()) {
				ModelElementTemplate template = model.getObject();
				if (template instanceof AttributeTemplate &&
					((AttributeTemplate)template).getAttribute()!=null && 
					((AttributeTemplate)template).getAttribute().equals(attribute)) {
					found = true;
					break;
				}
			}
			if (!found && getEditor()!=null)		
				elements.add(attribute);
		}
		List<Classifier> domainclassifiers =  getContentDao().getClassifiers(getDomain());
		for (Classifier classifier : domainclassifiers) {
			boolean found = false;
			for (IModel<ModelElementTemplate> model : getValues()) {
				ModelElementTemplate template = model.getObject();
				if (template instanceof ClassifierTemplate &&
					((ClassifierTemplate)template).getClassifier()!=null && 
					((ClassifierTemplate)template).getClassifier().equals(classifier)) {
					found = true;
					break;
				}
			}
			if (!found && getEditor()!=null &&classifier.getDataSet()!=null) 
				elements.add(classifier);
		}
		Collections.sort(elements, new Comparator<ModelElement>() {
			@Override
			public int compare(ModelElement a, ModelElement b) {
				return a.getName().compareToIgnoreCase(b.getName());
			}
		}); 
		return elements;
	}
	
	protected Property<?> getKey() {
		return new Property<ModelElement>() {
			public String getName() {
				return "element";
			}
			public List<ModelElement> getChoices() {
				return getElements();
			}
		};
	}
	
	@Override
	protected ModelElementTemplate getNewValue() {
		KbeeModelElementTemplate template = new KbeeModelElementTemplate();
		return template;
	}
	
//	private ContentDao getContentDao() {
//		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
//	
//	private Domain getDomain() {
//		return ServiceLocator.getService(UserService.class).getDomain();
//	}
}
