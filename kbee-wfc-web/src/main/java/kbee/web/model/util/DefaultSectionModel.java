package kbee.web.model.util;

import org.apache.wicket.model.IModel;
import org.springframework.util.Assert;

import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.ModelSection;
import com.novamens.kbee.content.model.KbeeModelSection;
import com.novamens.wicket.model.ObjectModel;

@Deprecated
public class DefaultSectionModel implements IModel<ModelSection> {
	private static final long serialVersionUID = 1L;
	
	private IModel<ContentTemplate> templatemodel;
	private ModelSection section;
	
	public DefaultSectionModel(ModelSection section) {
		setObject(section);
	}
	
	@Override
	public ModelSection getObject() {
		if (section==null) {
			section = new KbeeModelSection(templatemodel.getObject());
			((KbeeModelSection)section).setDefault(true);
		}
		return section;
	}
	
	public void setObject(ModelSection section) {
		Assert.isInstanceOf(KbeeModelSection.class, section, "invalid section");
		templatemodel = new ObjectModel<ContentTemplate>(((KbeeModelSection)section).getContentTemplate());
		this.section = section;
	}
	
	@Override
	public void detach() {
		templatemodel.detach();
		section = null;
	}
}
