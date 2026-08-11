package kbee.web.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.model.IModel;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetElementTemplate;
import com.novamens.content.model.KbeeModelElement;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Multiplicity;
import com.novamens.kbee.content.model.KbeeDataSetElementTemplate;

import kbee.util.logging.Logger;
import kbee.web.form.RelationGridEditor;

@Deprecated
@SuppressWarnings("serial")
public class StructureEditor<T extends DataSet> extends RelationGridEditor<T, DataSetElementTemplate> {
	private static final long serialVersionUID = 1L;
	
	static private Logger logger = Logger.getLogger(StructureEditor.class.getName());
	
	IModel<ContentTemplate> templateModel;
	
	public StructureEditor() {
		super("structure");
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
		
		for (Attribute attribute : getContentDao().getAttributes(getDomain())) {
			if (!contains(attribute) && getEditor()!=null)		
				elements.add(attribute);
		}
		
		for (Classifier classifier : getContentDao().getClassifiers(getDomain())) {
			if (!contains(classifier) && getEditor()!=null && 
					classifier.getDataSet()!=null && 
					!classifier.getDataSet().isAggregation()) 
				elements.add(classifier);
		}
		
		for (IModel<DataSetElementTemplate> model : getValues()) {
			elements.addAll(getDerived(model.getObject()));
		}
		
		Collections.sort(elements, new Comparator<ModelElement>() {
			@Override
			public int compare(ModelElement a, ModelElement b) {
				try {
					String aname = a.getName();
					String bname = b.getName();
					if(aname == null) aname="";
					if(bname == null) bname="";
					return aname.compareToIgnoreCase(bname);
				} 
				catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}
		});
		
		return elements;
	}

	@Override
	protected List<Property<?>> getProperties() {
		
		List<Property<?>> properties = new ArrayList<Property<?>>();
		
		properties.add(new Property<String>() {
			@Override
			public String getName() {
				return "name";
			}
			public String getDisplayValue(DataSetElementTemplate template) {
				return template.getDisplayName();
			}
			@Override
			public boolean isEditable(IModel<DataSetElementTemplate> model) {
				return false;
			}
			@Override
			public String getStringWidth() {
				return "50%";
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
			@Override
			public boolean isEditable(IModel<DataSetElementTemplate> model) {
				return !model.getObject().isCanonical();
			}
			@Override
			public String getStringWidth() {
				return "20%";
			}
		});
		
		properties.add(new BooleanProperty() {
			public String getName() {
				return "readOnly";
			}
			@Override
			public String getStringWidth() {
				return "10%";
			}
			@Override
			public boolean isEditable(IModel<DataSetElementTemplate> model) {
				return !model.getObject().isCanonical();
			}
		});
		
		properties.add(new Property<ModelElement>() {
			@Override
			public String getName() {
				return "parent";
			}
			@Override
			public boolean isVisible() {
				return false;
			}
		});
		
		return properties;
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
	protected DataSetElementTemplate getNewValue() {
		KbeeDataSetElementTemplate template = new KbeeDataSetElementTemplate();
		template.setMultiplicity(Multiplicity.M01);
		return template;
	}
	
	protected boolean contains(Attribute attribute) {
		boolean found = false;
		for (IModel<DataSetElementTemplate> model : getValues()) {
			DataSetElementTemplate template = model.getObject();
			if (template.getElement() instanceof Attribute &&
				((Attribute)template.getElement()).equals(attribute)) {
				found = true;
				break;
			}
		}
		return found;
	}
	
	protected boolean contains(Classifier classifier) {
		boolean found = false;
		for (IModel<DataSetElementTemplate> model : getValues()) {
			DataSetElementTemplate template = model.getObject();
			if (template.getElement() instanceof Classifier &&
				((Classifier)template.getElement()).equals(classifier)) {
				found = true;
				break;
			}
		}
		return found;
	}
	
	protected List<ModelElement> getDerived(DataSetElementTemplate template) {
		List<ModelElement> derived = new ArrayList<ModelElement>();
		if (template.getElement() instanceof Classifier) {
			DataSet templatedataset = ((Classifier)template.getElement()).getDataSet();
			for (Classifier datasetclassifier : templatedataset.getClassifiers()) {
				if (!contains(datasetclassifier) && 
						getEditor()!=null && 
						datasetclassifier.getDataSet()!=null) {
					derived.add(new KbeeModelElement(template.getElement(), datasetclassifier));
				}
			}
			for (AttributeTemplate datasetattribute : templatedataset.getAttributes()) {
				if (!contains(datasetattribute.getAttribute()) && getEditor()!=null) {
					derived.add(new KbeeModelElement(template.getElement(), datasetattribute.getAttribute()));
				}
			}
		}
		return derived;
	}
	
	protected List<Property<?>> getPropertiesCache() {
		return getProperties();
	}
//	
//	private ContentDao getContentDao() {
//		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
//
//	private Domain getDomain() {
//		return ServiceLocator.getService(UserService.class).getDomain();
//	}
}