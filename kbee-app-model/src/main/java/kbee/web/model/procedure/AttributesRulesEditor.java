package kbee.web.model.procedure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.model.Attribute;
import com.novamens.content.workflow.AttributeRule;
import com.novamens.kbee.content.workflow.KbeeAttributeRule;
import com.novamens.kbee.template.KbeeTemplateModelInfo;
import com.novamens.text.TemplateModelInfo;

import kbee.web.form.RelationEditor;
import kbee.web.template.ModelHelpModal;

@SuppressWarnings("serial")
public class AttributesRulesEditor<T> extends RelationEditor<T, AttributeRule> {
	private static final long serialVersionUID = 1L;
	
	public AttributesRulesEditor(String id) {
		super(id);
		setPropertyModel(new PropertyModel<Collection<AttributeRule>>(this, "rules"));
		add(new ModelHelpModal("help-modal"));
	}
	
	public AttributesRulesEditor() {
		this("attributesrules");
	}
	
	public IModel<String> getHelp() {
		return new StringResourceModel("attributesrules.help", this, null);
	}
	
	@Override
	protected String getTitle(AttributeRule value) {
		String title = value.getAttribute().getDisplayName();
		if (value.getValue()!=null)  
			title += " <span class=\"highlight\">(" + value.getValue()+ ")</span>";
		return title;
	}
	
	@Override
	protected List<Property<?>> getProperties() {
		List<Property<?>> properties = new ArrayList<Property<?>>();
		
		properties.add(new Property<String>() {
			@Override
			public String getName() {
				return "value";
			}
			@Override
			public boolean isHelpInfo() {
				return true;
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, getHelpModel());
			}
		});
		
		return properties;
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
	protected AttributeRule getNewValue() {
	
		KbeeAttributeRule rule=new KbeeAttributeRule();
		
		// ${.now?string('yyyy-MM-dd')}
		
		return rule;
	}

	public List<Attribute> getAttributes() {
		List<Attribute> attributes =  new ArrayList<Attribute>();
		List<Attribute> domainattributes =  getContentDao().getAttributes(getDomain());
		for (Attribute attribute : domainattributes) {
			boolean found = false;
			for (IModel<AttributeRule> model : getValues()) {
				if (model.getObject().getAttribute().equals(attribute)) {
					found = true;
					break;
				}
			}
			if (!found && getEditor()!=null && 
				attributes.add(attribute));
		}
		Collections.sort(attributes, new Comparator<Attribute>() {
			@Override
			public int compare(Attribute a, Attribute b) {
				try{
					return a.getName()!=null ? a.getName().compareToIgnoreCase(b.getName()) : 1;
				} 
				catch (RuntimeException e) {
					return 0;
				}
			}
		}); 
		return attributes;
	}
	
	public List<AttributeRule> getRules() {
		return new ArrayList<AttributeRule>();
	}
	
	public void setRules(List<AttributeRule> rules) {
		
	}
	
	protected TemplateModelInfo getHelpModel() {
		KbeeTemplateModelInfo model = new KbeeTemplateModelInfo();
		
		List<TemplateModelInfo> elements = new ArrayList<TemplateModelInfo>();
		KbeeTemplateModelInfo e;
		e = new KbeeTemplateModelInfo();
		e.setName("content");
		e.setType(KbeeTemplateModelInfo.ModelType.CONTENT);
		elements.add(e);
		
		model.setElements(elements);
		return model;
	}
	
	private ModelHelpModal getHelpModal() {
		return (ModelHelpModal) get("help-modal");
	}
	
//	private ContentDao getContentDao() {
//		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
	
//	private Domain getDomain() {
//		return ServiceLocator.getService(UserService.class).getDomain();
//	}
}
