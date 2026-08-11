package kbee.web.model.contentclass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.AccessStrategy;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeSource;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.KbeeModelElement;
import com.novamens.content.model.ModelElement;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.ModelSection;
import com.novamens.content.model.Multiplicity;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.service.ServiceLocator;
import com.novamens.system.parameters.SystemParameterService;

import kbee.web.form.RelationGridEditor;

public class AbstractStructureEditor<T> extends RelationGridEditor<T, ModelElementTemplate> { 
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AbstractStructureEditor.class.getName());
	
	private Boolean parentsEnabled = null;

	public AbstractStructureEditor(IModel<ModelSection> model) {
		super("structure");
		setPropertyModel(new PropertyModel<Collection<ModelElementTemplate>>(model, "structure"));
	}
	
	public AbstractStructureEditor(String id, IModel<ModelSection> model) {
		super(id);
		setPropertyModel(new PropertyModel<Collection<ModelElementTemplate>>(model, "structure"));
	}
	
	public List<ModelElement> getElements() {
	
		List<ModelElement> elements =  new ArrayList<ModelElement>();
		
		for (Attribute attribute : getContentDao().getAttributes(getDomain())) {
			if (!contains(attribute) && getEditor()!=null && (attribute.getState()==ObjectState.ENABLED || attribute.getState()==ObjectState.ARCHIVED))		
				elements.add(attribute);
		}
		
		List<Classifier> domainclassifiers =  getContentDao().getClassifiers(getDomain());
		
		for (Classifier classifier : domainclassifiers) {
			if (!contains(classifier) && getEditor()!=null && 
					classifier.getDataSet()!=null && 
					!classifier.getDataSet().isAggregation() &&
					(classifier.getState()==ObjectState.ENABLED || classifier.getState()==ObjectState.ARCHIVED)) 
				elements.add(classifier);
		}
		
		if (parentsEnabled()) {
			// Clasificadores de los datasets de los clasificadores en la estructura:
			for (IModel<ModelElementTemplate> model : getValues()) {
				elements.addAll(getDerived(model.getObject()));
				elements.addAll(getReverse(model.getObject()));				
			}
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
	
	public List<Multiplicity> getMultiplicities() {
		List<Multiplicity> multiplicities = new ArrayList<Multiplicity>();
		multiplicities.add(Multiplicity.M01);
		multiplicities.add(Multiplicity.M11);
		multiplicities.add(Multiplicity.M0N);
		multiplicities.add(Multiplicity.M1N);
		return multiplicities;
	}
	
	public List<AccessStrategy> getAccessibilities() {
		List<AccessStrategy> accessibilities = new ArrayList<AccessStrategy>();
		accessibilities.add(AccessStrategy.All);
		accessibilities.add(AccessStrategy.Roles);
		accessibilities.add(AccessStrategy.Iql);
		accessibilities.add(AccessStrategy.Script);
		return accessibilities;
	}
	
	public List<AttributeSource> getSources() {
		List<AttributeSource> sources = new ArrayList<AttributeSource>();
		sources.add(AttributeSource.UserInput);
		sources.add(AttributeSource.Script);
		return sources;
	}

	protected List<ModelElement> getDerived(ModelElementTemplate template) {
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
	
	protected List<ModelElement> getReverse(ModelElementTemplate template) {
		List<ModelElement> reverse = new ArrayList<ModelElement>();
		if (!(template.getElement() instanceof Classifier)) {
			return reverse;
		}
		DataSet templatedataset = ((Classifier)template.getElement()).getDataSet();
		for (DataSet dataset : getContentDao().getDataSets(getDomain())) {
			if (!dataset.equals(templatedataset)) {
				for (ModelElementTemplate datasettemplate : dataset.getStructure()) {
					if (datasettemplate instanceof ClassifierTemplate &&
						((ClassifierTemplate)datasettemplate).getClassifier()!=null && 
						// !((ClassifierTemplate)datasettemplate).getMultiplicity().isMultiple() && 
						templatedataset.equals(((ClassifierTemplate)datasettemplate).getClassifier().getDataSet())) {
						for (Classifier classifier : getContentDao().getClassifiers(getDomain())) {
							if (classifier.getDataSet()!=null && classifier.getDataSet().equals(dataset)) {
								if (!contains(classifier) && getEditor()!=null && classifier.getDataSet()!=null) {
									reverse.add(new KbeeModelElement(((ClassifierTemplate)datasettemplate).getClassifier(), classifier, true));
								}
							}
						}
					}
				}
			}
		}
		return reverse;
	}
	
	protected boolean contains(Attribute attribute) {
		boolean found = false;
		for (ModelElementTemplate template : getTemplateStructure()) {
			if (template.getElement()!=null &&	template.getElement().equals(attribute)) {
				found = true;
				break;
			}
		}
		return found;
	}
	
	protected boolean contains(Classifier classifier) {
		boolean found = false;
		for (ModelElementTemplate template : getTemplateStructure()) {
			if (template.getElement()!=null &&	template.getElement().equals(classifier)) {
				found = true;
				break;
			}
		}
		return found;
	}
	
	protected List<ModelElementTemplate> getTemplateStructure() {
		return new ArrayList<ModelElementTemplate>();
	}
	
	protected boolean linkView() {
		return false;
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
//	private ContentDao getContentDao() {
//		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
	
	private boolean parentsEnabled() {
		if (parentsEnabled == null) {
			String value = ServiceLocator.getService(SystemParameterService.class).getParameter("com.novamens.content.contentclass.parentsenabled", "true");
			parentsEnabled = "true".equals(value);
		}
		return parentsEnabled.booleanValue();
	}
}
