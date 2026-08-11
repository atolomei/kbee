package kbee.web.model.procedure;

import java.util.ArrayList;
import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.workflow.TaskAttributeTemplate;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.service.ServiceLocator;

import kbee.web.form.RelationEditor;

@Deprecated
@SuppressWarnings("serial")
public class TaskAttributesEditor extends RelationEditor<WebTask, AttributeTemplate> {
	private static final long serialVersionUID = 1L;

	public TaskAttributesEditor() {
		super("attributeTemplates");
		setOutputMarkupId(true);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	}
	
	
	@Override
	protected List<Property<?>> getProperties() {
		List<Property<?>> properties = new ArrayList<Property<?>>();
		
		properties.add(new Property<Attribute>() {
			public String getName() {
				return "attribute";
			}
			public List<Attribute> getChoices() {
				return getAttributes();
			}
			public boolean getTitle() {
				return true;
			}
			public boolean isSelectable() {
				return true;
			}
			public boolean getKey() {
				return true;
			}
		});
		
		properties.add(new Property<Boolean>() {
			public String getName() {
				return "readOnly";
			}
			public boolean isBoolean() {
				return true;
			}
		});
		
		return properties;
	}
	
	public List<Attribute> getAttributes() {
		List<Attribute> attributes =  new ArrayList<Attribute>();
		attributes =  getContentDao().getAttributes(getDomain());
		return attributes;
	}
	
	protected Property<?> getKey() {
		return new Property<Attribute>() {
			public String getName() {
				return "attribute";
			}
			public List<Attribute> getChoices() {
				return getAttributes();
			}
		};
	}
	
	@Override
	protected AttributeTemplate getNewValue() {
		return new TaskAttributeTemplate();
	}
	
//	private ContentDao getContentDao() {
//		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
//	
//	private Domain getDomain() {
//		return ServiceLocator.getService(UserService.class).getDomain();
//	}
}
