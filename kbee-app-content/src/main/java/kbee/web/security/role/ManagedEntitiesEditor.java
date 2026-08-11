package kbee.web.security.role;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;

import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.EntitySet;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.content.model.SecuredSet;
import com.novamens.content.model.UserSet;
import com.novamens.content.security.Role;
import com.novamens.kbee.content.security.KbeeEntityRole;

import kbee.web.form.RelationEditor;

@SuppressWarnings("serial")
public class ManagedEntitiesEditor extends RelationEditor<Role, DataSet> {
	private static final long serialVersionUID = 1L;
	
	public ManagedEntitiesEditor() {
		super("managedEntities");
	}
		
	public ManagedEntitiesEditor(String id) {
		super(id, "managedEntities");
	}
	
	public List<DataSet> getDataSets() {
		
		List<DataSet> datasets = new ArrayList<>(); 
				
		for (DataSet dataset : getContentDao().getDataSets(getDomain())) {
			if ((dataset instanceof UserSet || dataset instanceof EntitySet || dataset instanceof SecuredSet ) 
					&& !contains(dataset) 
					&& manageable(dataset)) {
				datasets.add(dataset);
			}
		}
		
		return datasets;
	}
	
	protected boolean contains(DataSet dataset) {
		for (IModel<DataSet> model : getValues()) {
			if (model.getObject().equals(dataset)) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean manageable(DataSet dataset) {
		Role role = getModelObject();
		if (dataset instanceof SecuredSet) {
			return true;
		}
		if (dataset.equals(((KbeeEntityRole)role).getClassifier().getDataSet())) {
			return true;
		}
		for (ModelElementTemplate template : dataset.getStructure()) {
			if (template instanceof ClassifierTemplate &&
				((KbeeEntityRole)role).getClassifier().equals(((ClassifierTemplate)template).getClassifier())) {
				return true;
			}
		}
		return false;
	}

	 
	@Override
	protected Property<?> getKey() {
		return new Property<DataSet>() {
			public String getName() {
 				return "dataSet";
			}
			public List<DataSet> getChoices() {
				return getDataSets();
			}
		};
	}
	
	@Override	
	protected String getStringValue(Object value) {
		return ((DataSet)value).getDisplayName();
	}

}
