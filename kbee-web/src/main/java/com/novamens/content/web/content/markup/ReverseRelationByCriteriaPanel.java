package com.novamens.content.web.content.markup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.relationshipsbycriteria.RelationshipByCriteriaTemplate;
import com.novamens.content.relationshipsbycriteria.RelationshipsByCriteriaService;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

@SuppressWarnings("serial")
public class ReverseRelationByCriteriaPanel<T extends Content> extends ModelPanel<T> {
	private static final long serialVersionUID = 1L;
	
	private IModel<RelationshipByCriteriaTemplate> templatemodel;
	List<IModel<Content>> related = null;

	public ReverseRelationByCriteriaPanel(String id) {
		super(id);
		setOutputMarkupId(true);
	}
	
	public void setTemplateModel(IModel<RelationshipByCriteriaTemplate> model) {
		this.templatemodel = model;
	}
	
	public IModel<RelationshipByCriteriaTemplate> getTemplateModel() {
		return this.templatemodel;
	}
	
	public RelationshipByCriteriaTemplate getTemplate() {
		return getTemplateModel().getObject();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new ListView<IModel<Content>>("value", new PropertyModel<List<IModel<Content>>>(this, "related")) {
			protected void populateItem(final ListItem<IModel<Content>> item){
				Link<Void> link = new Link<Void>("value-link") {
					@Override
					public void onClick() {
						Page page = ReverseRelationByCriteriaPanel.this.getPage(new ObjectModel<Content>((Content) getContentDao().reload(item.getModelObject().getObject())));
						setResponsePage(page);
					}
				};
				link.add(new AttributeModifier("target", "_blank"));
				
				item.add(link);
				
				link.add(new Label("title", new Model<String>() {
					public String  getObject() {
						return item.getModelObject().getObject().getTitle();
					};
				}));
			}	
		});
	}
	
	public List<IModel<Content>> getRelated() {
		if (related!=null)
			return related;
		related = new ArrayList<IModel<Content>>();
		Map<RelationshipByCriteriaTemplate,List<Content>> relatedtemplates = getModel().getObject().getService(RelationshipsByCriteriaService.class).getRelatedTemplates();
		for (Content content : relatedtemplates.get(getTemplateModel().getObject())) {
			related.add(new ObjectModel<Content>(content, true));
		}
		return related;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		related = null;
		if (getTemplateModel()!=null)
			getTemplateModel().detach();
	}
	
	protected Page getPage(IModel<Content> model) {
		String beanname = getContentClass(model.getObject()) + "-page";
		Page page = (Page) ServiceLocator.getService(BeansService.class).getBean(beanname, model);
		return page;
	}
	
	protected String getContentClass(Content content) {
		String javaclass = content.getContentTemplate().getContentClass().getJavaClass();
		String classname = javaclass.substring(javaclass.lastIndexOf(".") + 1);
		classname = classname.toLowerCase();
		return classname;
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}