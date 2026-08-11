package kbee.web.model.contentclass;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.base.ResourceTag;
import com.novamens.content.model.ContentTemplate;
import com.novamens.repository.DomRepositoryService;
import com.novamens.service.ServiceLocator;

import kbee.web.form.RelationEditor;

@SuppressWarnings("serial")
public class ResourceTagsEditor extends RelationEditor<ContentTemplate, ResourceTag> {	
	private static final long serialVersionUID = 1L;
	
	public ResourceTagsEditor(String id) {
		super(id);
	}
	
	@Override
	public String getProperty() {
		return "resourceTags"; 
	}
	
	@Override
	protected List<Property<?>> getProperties() {
		List<Property<?>> properties = new ArrayList<Property<?>>();
		return properties;
	}
	
	protected Property<?> getKey() {
		return new Property<ResourceTag>() {
			public String getName() {
				return "tag";
			}
			public List<ResourceTag> getChoices() {
				return getTags();
			}
		};	
	}
	
	protected List<ResourceTag> getTags() {
		List<ResourceTag> tags =  new ArrayList<ResourceTag>();
		tags.addAll(ServiceLocator.getService(DomRepositoryService.class).getRepository(ResourceTag.class).findAll());
		return tags;
	}
	
	@Override
	protected String getPart() {
		return "resource tags";
	}
	
	@Override
	protected ResourceTag getNewValue() {
		return null;
	}
}