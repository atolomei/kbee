package kbee.web.searcher.panel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormAccessLevel;
import com.novamens.content.relationshipsbycriteria.RelationshipByCriteriaTemplate;
import com.novamens.content.relationshipsbycriteria.RelationshipsByCriteriaService;
import com.novamens.kbee.content.workflow.KbeeTaskForm;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.content.eform.ContentFormViewer;

@SuppressWarnings("serial")
public class SearcherDetailCriteriaRelationshipPanel<T extends Content> extends SearcherDetailPanel<T> {
	private static final long serialVersionUID = 1L;
	
	private Map<RelationshipByCriteriaTemplate, List<Content>> related;
	
	public SearcherDetailCriteriaRelationshipPanel(String id, IModel<T> model, IModel<Site> site_model) {
		super(id, model, site_model);
	}
	
	public SearcherDetailCriteriaRelationshipPanel(String id, IModel<T> model, Map<RelationshipByCriteriaTemplate, List<Content>> related, IModel<Site> site_model) {
		super(id, model, site_model);
		this.related = related;
	}
	
	public T getModelObject() {
		return getModel().getObject();
	}
	
	public List<RelationshipByCriteriaTemplate> getRelations() {
		List<RelationshipByCriteriaTemplate> relations = new ArrayList<RelationshipByCriteriaTemplate>();
		relations.addAll(getRelated().keySet());
		return relations;
	}
	
	public Map<RelationshipByCriteriaTemplate, List<Content>> getRelated() {
		if (this.related==null) {
			related = getModelObject().getService(RelationshipsByCriteriaService.class).getRelatedTemplates();
		}
		return related;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer container = new WebMarkupContainer("container") {
			@Override
			public boolean isVisible() {
				return true;
			}
		};
		
		container.add(new ListView<RelationshipByCriteriaTemplate>("relationship-list", () -> getRelations()) {
			@Override
			protected void populateItem(ListItem<RelationshipByCriteriaTemplate> item) {
				item.add(new RelationshipItem(new ObjectModel<RelationshipByCriteriaTemplate>(item.getModelObject())));
			}
		});
		
		add(container);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		related = null;
	}
	
	public class RelationshipItem extends Fragment {
		
		private IModel<RelationshipByCriteriaTemplate> templatemodel;

		public RelationshipItem(IModel<RelationshipByCriteriaTemplate> model) {
			super("relationship-item", "relationship-table-fragment", SearcherDetailCriteriaRelationshipPanel.this);
			setOutputMarkupId(true);
			setTemplateModel(model);
		}
		
		@Override
		public void onInitialize() {
			super.onInitialize();
			add(new ListView<Content>("related", () -> getRelated()) {
				@Override
				protected void populateItem(ListItem<Content> item) {
					Component view = getView(item.getModelObject());
					if (view!=null)
						item.add(view);
					else
						item.add(new InvisiblePanel("related-view"));
				}
			});
		}
		
		public IModel<String> getLabel() {
			return new Model<String>(getTemplate().getReverseLabel());
		}
		
		public RelationshipByCriteriaTemplate getTemplate() {
			return getTemplateModel().getObject();
		}
		
		@Override
		public boolean isVisible() {
			return !getRelated().isEmpty();
		}
		
		public List<Content> getRelated() {
			List<Content> related = new ArrayList<Content>();
			related.addAll(SearcherDetailCriteriaRelationshipPanel.this.getRelated().get(getTemplate()));
			return related;
		}
		
		@Override
		public void onDetach() {
			super.onDetach();
			getTemplateModel().detach();
		}
		
		private Fragment getView(Content content) {
			content = (Content)getContentDao().reload(content);
			return new RelatedEmbeddedView(new ObjectModel<Content>(content));
		}
		
		private IModel<RelationshipByCriteriaTemplate> getTemplateModel() {
			return templatemodel;
		}
		
		private void setTemplateModel(IModel<RelationshipByCriteriaTemplate> model) {
			this.templatemodel = model;
		}
	}
	
	public class RelatedEmbeddedView extends Fragment {
		
		private IModel<Content> model;
	
		public RelatedEmbeddedView(IModel<Content> model) {
			super("related-view", "related-embedded-view-fragment", SearcherDetailCriteriaRelationshipPanel.this);
			setOutputMarkupId(true);
			
			setModel(model);
			
			Panel viewer = new ContentFormViewer<Content>("viewer", model, getForm());
			
			add(viewer);
		}
		public void setModel(IModel<Content> model) {
			this.model = model;
		}
		public IModel<Content> getModel() {
			return model;
		}
		public EForm getForm() {
			for (EForm form : getModel().getObject().getContentTemplate().getForms()) {
				if (form.getFormAccessLevel().equals(EFormAccessLevel.GENERAL)) {
					return new KbeeTaskForm(form);
				}
			}
			return null;
		}
		@Override
		public void onDetach() {
			super.onDetach();
			getModel().detach();
		}
	}
}
