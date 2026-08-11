package kbee.web.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.model.IModel;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetType;

import kbee.web.form.RelationEditor;

@SuppressWarnings("serial")
public class DataSetRelationsEditor extends RelationEditor<DataSet, Classifier> {
	private static final long serialVersionUID = 1L;

	
	private List<Classifier> listc;
	
	
	public DataSetRelationsEditor() {
		super("classifiers");
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
	public void onDetach() {
		super.onDetach();
		listc=null;
	}
	
	public List<Classifier> getClassifiers() {
	
		if (listc!=null)
			return listc;
		
		List<Classifier> classifiers =  new ArrayList<Classifier>();
		List<Classifier> domainclassifiers =  getContentDao().getClassifiers(getDomain());
		for (Classifier classifier : domainclassifiers) {
			boolean found = false;
			for (IModel<Classifier> model : getValues()) {
				if (model!=null && model.getObject()!=null  && model.getObject().equals(classifier)) {
					found = true;
					break;
				}
			}
			if (!found && getEditor()!=null && 
				classifier.getDataSet()!=null &&		
				!classifier.getDataSet().getDataSetType().equals(DataSetType.DATE) &&	
				!classifier.getDataSet().equals(getEditor().getModelObject()) &&
				!getEditor().getModelObject().isAFunctionOf(classifier.getDataSet())) 
				classifiers.add(classifier);
		}
		Collections.sort(classifiers, new Comparator<Classifier>() {
			@Override
			public int compare(Classifier a, Classifier b) {
				try {
					return a.getName().compareToIgnoreCase(b.getName());
				}catch (Exception e) {
					return 0;
				}
			}
		});
		
		listc = classifiers;
		return listc;
	}
	
	
//	private ContentDao getContentDao() {
//		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
//	
//	private Domain getDomain() {
//		return ServiceLocator.getService(UserService.class).getDomain();
//	}
}
