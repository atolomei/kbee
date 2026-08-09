package kbee.web.workflow.util;

import org.apache.wicket.model.IModel;

import com.novamens.content.model.ModelSection;

@Deprecated
@SuppressWarnings("serial")
public class TaskSectionModel implements IModel<ModelSection> {
	private ModelSection section;
	//@SuppressWarnings("rawtypes")
	//private Map json;
	public TaskSectionModel(ModelSection section) {
		this.section = section;
	}
	public ModelSection getObject() {
//		if (section==null) {
//			section = ((WebTaskParser)TaskParser.Get()).getSection(json);
//		}
		return section;
	}
	public void setObject(ModelSection section) {
		this.section = section;
	}
	public void detach() {
//		if (section!=null)
//			json = ((WebTaskParser)TaskParser.Get()).getMap(section);
		section = null;
	}
}
