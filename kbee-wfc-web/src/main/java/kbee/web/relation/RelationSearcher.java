package kbee.web.relation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.base.Content;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.Suggestion;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.SelectorPanel;
import com.novamens.wicket.markup.html.repeater.util.OnClickListener;

import kbee.web.form.AdvancedSearchField;
import kbee.web.search.service.ParametricSearchSuggestionService;

@SuppressWarnings("serial")
public class RelationSearcher extends Panel implements SelectorPanel<Content>  {
	private static final long serialVersionUID = 1L;
	
	private IModel<RelationTemplate> model;
	private List<OnClickListener<Content>> listeners = new ArrayList<>();
	
	public RelationSearcher(String id, IModel<RelationTemplate> model) {
		super(id);
		setTemplateModel(model);
		setOutputMarkupId(true);
	}
	
	public void onSelect(AjaxRequestTarget target, Content content) {
		for (OnClickListener<Content> listener : listeners) {
			listener.onClick(target, content);
		}
	}
	
	public void setTemplateModel(IModel<RelationTemplate> model) {
		this.model = model;
	}
	
	public IModel<RelationTemplate>  getTemplateModel() {
		return model;
	}
	
	public RelationTemplate  getRelation() {
		return getTemplateModel().getObject();
	}
	
	public void setContent(Content content) {
	}
	
	public Content getContent() {
		return null;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		Panel searcher = new AdvancedSearchPanel("searcher") {
			@Override
			public void onSelect(AjaxRequestTarget target, Content content) {
				RelationSearcher.this.onSelect(target, content);
			}
			@Override
			@SuppressWarnings("unchecked")
			protected void onClose(AjaxRequestTarget target) {
				setVisible(false);
				((AdvancedSearchField<Content>) RelationSearcher.this.get("content")).setOpen(false);
				target.add(RelationSearcher.this);
			}
//			@Override
//			protected List<Classifier> getClassifiers() {
//				if (getRelation().getTargetTemplates().isEmpty()) {
//					return getContentDao().getClassifiers(getDomain());
//				}
//				else {
//					List<Classifier> classifiers = new ArrayList<Classifier>();
//					for (ContentTemplate template : getRelation().getTargetTemplates()) {
//						for (ClassifierTemplate classifertemplate : template.getClassifiers()) {
//							Classifier classifier = classifertemplate.getClassifier();
//							if (!classifiers.contains(classifier)) {
//								classifiers.add(classifier);
//							}
//						}
//					}
//					return classifiers;
//				}
//			}
//			@Override
//			protected List<Attribute> getAttributes() {
//				if (getRelation().getTargetTemplates().isEmpty()) {
//					return getContentDao().getAttributes(getDomain());
//				}
//				else {
//					List<Attribute> attributes = new ArrayList<Attribute>();
//					for (ContentTemplate template : getRelation().getTargetTemplates()) {
//						for (AttributeTemplate attributetemplate : template.getAttributes()) {
//							Attribute attribute = attributetemplate.getAttribute();
//							if (!attributes.contains(attribute)) {
//								attributes.add(attribute);
//							}
//						}
//					}
//					return attributes;
//				}
//			}
			@Override
			protected Map<String, Object> getBaseFilters() {
				Map<String, Object> filters = super.getBaseFilters();
				if (!getRelation().getTargetTemplates().isEmpty()) {
					String templates = "";
					for (ContentTemplate template : getRelation().getTargetTemplates()) {
						if (!"".equals(templates)) templates += ", ";
						templates += String.valueOf(template.getId());
					}
					filters.put("template", templates);
				}
				return filters;
			}	
		};
		
		add(searcher);
		
		add(new AdvancedSearchField<Content>("content",  new PropertyModel<Content>(this, "content")) {
			@Override
			public void onOpenAdvancedSearch(AjaxRequestTarget target) {
				searcher.setVisible(!searcher.isVisible());
				target.add(RelationSearcher.this);
			}
			public void onUpdate(AjaxRequestTarget target) {
				setStringValue(null);
				RelationSearcher.this.onSelect(target, getValue());
			}
			@Override
			public IModel<String> getLabel() {
				return new Model<String>(getTemplateModel().getObject().getTargetLabel());
			}
			public boolean isHelpVisible() {
				return false;
			}
			@Override
			public List<Suggestion> getSuggestions(String pattern) {
				Map<String, Object> parameters = new HashMap<String, Object>();
				if (!getTemplateModel().getObject().getTargetTemplates().isEmpty()) {
					String templates = "";
					for (ContentTemplate template : getTemplateModel().getObject().getTargetTemplates()) {
						if (!templates.equals("")) templates +=", ";
						templates += template.getId();
					}
					templates = "[" + templates + "]";
					parameters.put("template", templates);
				}
				parameters.put("type", "[idoc, text]");
				List<Suggestion> suggestions = getDomain().getService(ParametricSearchSuggestionService.class)
						.getSuggestions(pattern, parameters);
				return suggestions;
			}
		}); 
	}	
	
	public void addListener(OnClickListener<Content> listener) {
		this.listeners.add(listener);
	}

	protected void onClose(AjaxRequestTarget target) {
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}
