package com.novamens.content.web.content.markup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.Relation;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.service.ContentService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.Proxy;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.content.model.KbeeRelation;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.WebSuggestion;
import com.novamens.wicket.markup.html.panel.SelectorPanel;
import com.novamens.wicket.markup.html.repeater.util.OnClickListener;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.form.RelationEditor;
import kbee.web.search.service.ParametricSearchSuggestionService;

@SuppressWarnings("serial")
public class ContentRelationEditor extends RelationEditor<Content, Relation> {
	private static final long serialVersionUID = 1L;

	private IModel<RelationTemplate> templatemodel;
	private boolean reverse;

	private class RelationModel implements IModel<Relation> {
		IModel<Content> contentmodel;
		private IModel<RelationTemplate> templatemodel;
		public RelationModel(IModel<Content> contentmodel, IModel<RelationTemplate> templatemodel) {
			this.contentmodel = contentmodel;
			this.templatemodel = templatemodel;
		}
		public void setObject(Relation relation) {
		}
		public Relation getObject() {
			KbeeRelation relation = new KbeeRelation();
			// si es reverso es source sino es target-
			relation.setTarget(contentmodel.getObject());
			relation.setTemplate(templatemodel.getObject());
			return relation;
		}
		public void detach() {
			templatemodel.detach();
			contentmodel.detach();
		}
	}

	public ContentRelationEditor(IModel<RelationTemplate> model, boolean reverse) {
		super("relations");
		setTemplateModel(model);
		this.reverse = reverse;
	}

	@Override
	public String getProperty() {
		return isReverseRelation() ? "reverseRelations" : "relations";
	}

	@SuppressWarnings("unchecked")
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
		List<Suggestion> contentsuggestions = getDomain().getService(ParametricSearchSuggestionService.class)
				.getSuggestions(pattern, parameters);
		List<Suggestion> suggestions = new ArrayList<Suggestion>();
		for (Suggestion suggestion : contentsuggestions)
			suggestions.add(new WebSuggestion(new RelationModel((IModel<Content>) suggestion.getObject(), getTemplateModel()),suggestion.getText(), suggestion.getScore(), suggestion.isOutstanding()));
		return suggestions;
	}

	@Override
	public boolean ordered() {
		return true;
	}
	
	public String getTargetLabel() {
		return	getTemplateModel().getObject().getTargetLabel();	
	}

	public void setTemplateModel(IModel<RelationTemplate> model) {
		this.templatemodel = model;
	}

	public IModel<RelationTemplate> getTemplateModel() {
		return this.templatemodel;
	}
	
	public RelationTemplate getTemplate() {
		return getTemplateModel().getObject();
	}

	public boolean isReverseRelation() {
		if (getEditor()==null)
			return false;
		return reverse;
	}
	
	public boolean isAggregation() {
		if (getTemplateModel().getObject().isAggregation())
			return true;
		return false;
	}
	
	@Override
	public void updateModel() {
		
		if (!isUpdated()) 
			return;
		
		// si es reverso y agregado habria que asentar en la auditoria del agregado
		List<Relation> values = new ArrayList<Relation>();
		values.addAll(getPropertyModel().getObject());
		values.removeIf(relation -> relation.getTemplate().equals(getTemplate()));
		for (IModel<Relation> model : getValues()) {
			values.add(model.getObject());
		}
		getPropertyModel().setObject(values);
		setUpdatedPart(getTemplate().getTargetLabel());
		//setUpdatedPart(((Label)get("label")).getDefaultModelObjectAsString().toLowerCase());
		setUpdated(false);
	}
	
	/**
	 * Reverse -> mensaje Read only
	 * 
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
		if (isReverseRelation() && !isAggregation())
			setReadOnly(true);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected WebMarkupContainer getCreationPanel() {
		
		Object selector =  ServiceLocator.getService(BeansService.class).getBean("relation-selector-panel", "creation-panel", getTemplateModel());
		 
		if (selector==null || !(selector instanceof SelectorPanel<?>)) {
			selector = super.getCreationPanel();
		}
		else {
			((SelectorPanel<Content>)selector).addListener(new OnClickListener<Content>() {
				public void onClick(AjaxRequestTarget target, Content content) {
					onUpdate(getKeyCache(), (new RelationModel(new ObjectModel<Content>(content), getTemplateModel())).getObject(), target);
				}
				public void onDblClick(AjaxRequestTarget target, Content content) {
				}
			});
			((Panel)selector).setVisible(getEditor()!=null && getEditor().isEditionEnabled() && creationEnabled() && !isReadOnly() && !isReverseRelation());
		}
					
		return (WebMarkupContainer)selector;
	}
	
	@Override
	protected boolean isValid(Relation value) {
		if (value.getTemplate().equals(getTemplate()))
			return true;
		return false;
	}

	@Override
	protected void onUpdate(Property<?> property, Object propertyvalue, AjaxRequestTarget target) {
		boolean duplicate = false;
		Relation value = (Relation) propertyvalue;
		for (IModel<Relation> model : getValues()) {
			if (model.getObject().getTarget().getId().equals(((Content) value.getTarget()).getId())) {
				duplicate = true;
				break;
			}
		}
		if (!duplicate) {
			super.onUpdate(property, propertyvalue, target);
		} 
		else {
			target.add(ContentRelationEditor.this);
		}
	}

	protected void onValueClick(IModel<Relation> model) {
		Page page;
		if (isReverseRelation())
			page = getPage(new ObjectModel<Content>((Content) getContentDao().reload(model.getObject().getSource())));
		else
			page = getPage(new ObjectModel<Content>((Content) getContentDao().reload(model.getObject().getTarget())));
		setResponsePage(page);
	}

	protected IModel<Relation> getModel(Relation value) {
		if (((KbeeRelation) value).getId() == null) {
			return new RelationModel(new ObjectModel<Content>(value.getTarget()), getTemplateModel());
		} else {
			return new ObjectModel<Relation>(value);
		}
	}

	@Override
	protected Property<?> getKey() {
		return new Property<Relation>() {
			@Override
			public String getName() {
				return isReverseRelation() ? "source" : "target";
			}
			@Override
			public boolean isAutocomplete() {
				return true;
			}
			@Override
			public String getLabel() {
				return getTargetLabel();
			}
			@Override
			public List<Suggestion> getSuggestions(String pattern) {
				return ContentRelationEditor.this.getSuggestions(pattern);
			}
			@Override
			public IModel<Relation> getModel(Relation value) {
				return ContentRelationEditor.this.getModel(value);
			}
			@Override
			public String serialize(IModel<Relation> model) {
				return ContentRelationEditor.this.serialize(model);
			}
			@Override
			public IModel<Relation> deserialize(String value) {
				return ContentRelationEditor.this.deserialize(value);
			}
		};
	}

	protected Page getPage(IModel<Content> model) {
		
		String beanname;
		
		beanname = getContentClass(model.getObject()) + "-page";
		
		Page page = (Page) ServiceLocator.getService(BeansService.class).getBean(beanname, model);
		return page;
	}

	@Override
	protected String getTitle(Relation value) {
		return isReverseRelation() ? value.getSource().getDisplayName() : value.getTarget().getDisplayName();
	}

	protected String getText(Relation value) {
		return isReverseRelation() ? getText(value.getSource()) : getText(value.getTarget());
	}

	protected String getText(Content value) {
		String text = value.getService(ContentService.class).getSummary();
		if (text==null || "".equals(text)) {
			text =value.getService(ContentService.class).getPortalSubtitle();
		}
		return text;
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	protected String serialize(IModel<Relation> model) {
		Content target = model.getObject().getTarget();
		return Proxy.getClassName(target) + "-" + target.getId();
	}

	protected IModel<Relation> deserialize(String token) {
		int i = token.indexOf("-");
		if (i <= 0)
			return null;
		String classname = token.substring(0, i);
		String id = token.substring(i + 1);
		IModel<Content> contentmodel = getModel(classname, id);
		if (contentmodel == null)
			return null;
		IModel<Relation> model = new RelationModel(contentmodel, getTemplateModel());
		return model;
	}

	protected IModel<Content> getModel(String classname, String id) {
		ObjectModel<Content> model = null;
		try {
			Class<?> clazz = Class.forName(classname);
			SessionFactory sf = (SessionFactory) ServiceLocator.getService(BeansService.class)
					.getBean("sessionFactory");
			Object object = (Content) sf.getCurrentSession().get(clazz, Long.valueOf(id));
			if (object != null) {
				model = new ObjectModel<Content>(clazz, Long.valueOf(id));
				model.getObject();
			}
		} catch (Exception e) {
			model = null;
		}
		return model;
	}

	protected String getContentClass(Content content) {
		String javaclass = content.getContentTemplate().getContentClass().getJavaClass();
		String classname = javaclass.substring(javaclass.lastIndexOf(".") + 1);
		classname = classname.toLowerCase();
		return classname;
	}
}
