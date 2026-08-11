package kbee.web.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetElementTemplate;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.model.KbeeDataSetElementTemplate;
import com.novamens.service.ServiceLocator;

import kbee.web.form.RelationGridEditor;

@SuppressWarnings("serial")

@Deprecated
public class StructureEditor2<T extends DataSet> extends RelationGridEditor<T, DataSetElementTemplate> {
	private static final long serialVersionUID = 1L;

	public StructureEditor2() {
		super("structure");
	}

	public List<ModelElement> getElements() {
		List<ModelElement> elements =  new ArrayList<ModelElement>();
		
		// Atributos del dominio:
		List<Attribute> domainattributes =  getContentDao().getAttributes(getDomain());
		for (Attribute attribute : domainattributes) {
			if (!contains(attribute) && getEditor()!=null)
				elements.add(attribute);
		}
		
		// Clasificadores del dominio:
		List<Classifier>  domainclassifiers =  getContentDao().getClassifiers(getDomain());
		for (Classifier classifier : domainclassifiers) {
			if (!contains(classifier) && getEditor()!=null && classifier.getDataSet()!=null) {
				if (!classifier.getDataSet().isAggregation()) {
					elements.add(classifier);
				}
			}	
		}
		
		Collections.sort(elements, new Comparator<ModelElement>() {
			@Override
			public int compare(ModelElement a, ModelElement b) {
				String aname = a.getName();
				String bname = b.getName();
				if(aname == null) aname="";
				if(bname == null) bname="";
				return aname.compareToIgnoreCase(bname);
			}
		}); 
		return elements;
	}
	
	public List<Multiplicity> getMultiplicities() {
		List<Multiplicity> multiplicities = new ArrayList<Multiplicity>();
		multiplicities.add(Multiplicity.M01);
		multiplicities.add(Multiplicity.M11);
		multiplicities.add(Multiplicity.M0N);
		multiplicities.add(Multiplicity.M1N);
		return multiplicities;
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
		
		return properties;
	}

	// Para que pueda ser agregación del dataset en edición, el dataset relacionado tiene que ser agregacion
	// y tiene que tener un único  clasificador reverso que referencie al dataset en edición
	protected boolean isAggregation(DataSetElementTemplate template) {
		DataSet dataset = ((Classifier)template.getElement()).getDataSet();
		if (!dataset.isAggregation()) return false;
		boolean found = false;
		for (ModelElementTemplate t :  dataset.getStructure()) {
			if (t.getElement() instanceof Classifier) {
				DataSet d = ((Classifier)t.getElement()).getDataSet();
				if (d.equals(getModelObject())) {
					if (found) return false;
					found = true;
				}
			}
		}
		return found;
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
	
	protected boolean deleteEnabled(DataSetElementTemplate value) {
		return !value.isCanonical();
	}
	
	protected String getStringValue(Object value) {
		if (value instanceof Classifier) {
			return ((Classifier)value).getDisplayName() + "(" + new StringResourceModel("classifier", this, null).getObject() +")" ;
		}
		if (value instanceof Attribute) {
			return ((Attribute)value).getDisplayName() +  "(" + new StringResourceModel("attribute", this, null).getObject() +")" ;
		}
		return super.getStringValue(value);
	}

	@Override
	protected DataSetElementTemplate getNewValue() {
		KbeeDataSetElementTemplate template = new KbeeDataSetElementTemplate();
		template.setMultiplicity(Multiplicity.M01);
		return template;
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