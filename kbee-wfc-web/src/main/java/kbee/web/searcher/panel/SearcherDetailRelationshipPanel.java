package kbee.web.searcher.panel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.base.Relation;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.document.IDoc;
import com.novamens.content.model.RelationTemplate;
import com.novamens.content.resource.ExternalResource;
import com.novamens.content.resource.KBFile;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.ContentService;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.resource.ResourceGlyphIcon;
import kbee.web.resource.ResourceIcon;
import kbee.web.resource.ResourceLink;
import kbee.web.searcher.page.SearcherDetailDocumentPage;

@SuppressWarnings("serial")
public class SearcherDetailRelationshipPanel<T extends Content> extends SearcherDetailPanel<T> {
	private static final long serialVersionUID = 1L;
	
	List<IModel<RelationTemplate>> relations = null;
	
	private boolean has_items = false;
	

	public SearcherDetailRelationshipPanel(String id, IModel<T> model,  IModel<Site> site_model) {
		super(id, model, site_model);
		
		has_items=true;
		
	}
	
	public T getModelObject() {
		return getModel().getObject();
	}


	@Override
	public boolean isVisible() {
			return has_items;
	}
	
	
	
	public List<IModel<RelationTemplate>> getRelations() {
		
		if (relations!=null)
			return relations;
		
		relations = new ArrayList<IModel<RelationTemplate>>();
		
		
		for (RelationTemplate rt:getModelObject().getContentTemplate().getRelations()) {
			relations.add(new ObjectModel<RelationTemplate>(rt));	
		}
		
		for (RelationTemplate rt:getModelObject().getContentTemplate().getReverseRelations()) {
			relations.add(new ObjectModel<RelationTemplate>(rt));	
		}
		
		Collections.sort(relations, new Comparator<IModel<RelationTemplate>>() {
			@Override
			public int compare(IModel<RelationTemplate> a, IModel<RelationTemplate> b) {
				
				try {
				int aorder = 0, border = 0;
				aorder = a.getObject().getSourceTemplate().equals(getModelObject().getContentTemplate()) ?
					a.getObject().getTargetOrder() :
					a.getObject().getReverseOrder();
				border = b.getObject().getSourceTemplate().equals(getModelObject().getContentTemplate()) ?
					b.getObject().getTargetOrder() :
					b.getObject().getReverseOrder();
				return aorder<border ? -1 : 1;
				} catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}
		}); 
		return relations;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new ListView<IModel<RelationTemplate>>("relationship-list", () -> getRelations()) {
			@Override
			protected void populateItem(ListItem<IModel<RelationTemplate>> item) {
				try {
					//item.add(new RelationshipItem(new ObjectModel<RelationTemplate>(item.getModelObject())));
					//item.add(new RelationshipItem(item.getModel()));
					item.add(new RelationshipItem(item.getModelObject()));
					
				} catch (Exception e) {
					logger.error(e);
				}
			}
		});
	}
	
	
	
	
	public class RelationshipItem extends Fragment {
		
		private IModel<RelationTemplate> templatemodel;

		public RelationshipItem(IModel<RelationTemplate> model) {
			super("relationship-item", "relationship-table-fragment", SearcherDetailRelationshipPanel.this);
			setOutputMarkupId(true);
			setTemplateModel(model);
		}
		
		@Override
		public void onInitialize() {
			super.onInitialize();
			
			add(new Label("name", getLabel()));
			
			add(new ListView<IModel<Content>>("related", () -> getRelated()) {
				@Override
				protected void populateItem(ListItem<IModel<Content>> item) {
					item.add(getView(item.getModelObject()));
				}
			});
		}
		
		public IModel<String> getLabel() {
			return isReverseRelation() ? new Model<String>(getTemplate().getReverseLabel()) : new Model<String>(getTemplate().getTargetLabel());
		}
		
		public RelationTemplate getTemplate() {
			return getTemplateModel().getObject();
		}
		
		@Override
		public boolean isVisible() {
			// return true;
			return !getRelated().isEmpty();
		}
		
		
		private List<IModel<Content>> related = null;
		
		public List<IModel<Content>> getRelated() {
			
			if (related!=null)
				return related;
			
			related = new ArrayList<IModel<Content>>();
			List<Relation> relations = isReverseRelation() ? getModelObject().getReverseRelations() : getModelObject().getRelations();
			for (Relation relation : relations) {
				if (relation.getTemplate().equals(getTemplate())) {
					Content content = isReverseRelation() ? relation.getSource() : relation.getTarget();
					if (getSite()!=null && getSite().isDisplayValidVersion()) {
						content = content.getService(ContentService.class).getValidVersion();
					}
					if (content!=null && isReadable(content)) {
						related.add(new ObjectModel<Content>(content));
						has_items=true;
					}
				}
			}
			return related;
		}
		
		@Override
		public void onDetach() {
			super.onDetach();
			try {
				
				if (related!=null) {
					for (IModel<Content> m: related)
						m.detach();
				}
				
				getTemplateModel().detach();
				
			}  catch (Exception e) {
				logger.error(e);
			}
		}
		
		
		
		private Fragment getView(IModel<Content> mcontent) {
			Fragment view = null;
			Content content = (Content)getContentDao().reload(mcontent.getObject());
			if (content instanceof IDoc) {
				if (getDisplayMode()==RelationTemplate.ResourceDispalyMode) {
					view = new RelatedResourceView<IDoc>(new ObjectModel<IDoc>((IDoc)content));
				}
				else {
					view = new RelatedLinkView<IDoc>(new ObjectModel<IDoc>((IDoc)content));
				}
			}
			else {
				Assert.isTrue(false, "no view");
			}
			return view;
		}
		
		private boolean isReverseRelation() {
			return !getTemplate().getTargetTemplates().isEmpty() && getTemplate().getTargetTemplates().contains(getModelObject().getContentTemplate());
		}
		
		private int getDisplayMode() {
			return isReverseRelation() ? getTemplate().getReverseDisplayMode() : getTemplate().getTargetDisplayMode();
		}
		
		private IModel<RelationTemplate> getTemplateModel() {
			return templatemodel;
		}
		
		private void setTemplateModel(IModel<RelationTemplate> model) {
			this.templatemodel = model;
		}
	}
	
	public class RelatedLinkView<C extends Content> extends Fragment {
		
		private IModel<C> model;
	
		public RelatedLinkView(IModel<C> model) {
			super("related-view", "related-link-view-fragment", SearcherDetailRelationshipPanel.this);
			setOutputMarkupId(true);
			
			this.model = model;
			
			Link<?> link = new Link<Void>("link") {
				public void onClick() {
					setResponsePage(new SearcherDetailDocumentPage<C>(RelatedLinkView.this.getModel(), SearcherDetailRelationshipPanel.this.getSiteModel()));
				}
			};
			
			link.add(new Label("title", getModel().getObject().getTitle()));
			
			add(new Label("summary", model.getObject().getService(ContentService.class).getPortalSubtitle()));
			
			add(link);
		}
		
		public void setModel(IModel<C> model) {
			this.model = model;
		}
		
		public IModel<C> getModel() {
			return model;
		}
		
		@Override
		public void onDetach() {
			super.onDetach();
			getModel().detach();
		}
	}
	
	public class RelatedResourceView<C extends Content> extends Fragment {
		
		private IModel<C> model;
		private IModel<Resource> resourcemodel = null;
	
		public RelatedResourceView(IModel<C> model) {
			super("related-view", "related-resource-view-fragment", SearcherDetailRelationshipPanel.this);
			setOutputMarkupId(true);
			setModel(model);
			
			WebMarkupContainer imageContainer = new WebMarkupContainer("image-container") {
				@Override
				public boolean isVisible() {
					return true;
				}
			};
			add(imageContainer);
			
			if (getResourceModel()!=null) {
				ResourceLink<C> imageLink = new ResourceLink<C>("image-link", getResourceModel(), getModel());
				imageContainer.add(imageLink);
				imageLink.add(getImage(getResourceModel()));
				imageLink.add(getGlyphIcon(getResourceModel()));
			}
			else {
				imageContainer.add(new InvisiblePanel("image-link"));
			}
		
			WebMarkupContainer body = new WebMarkupContainer("body");
			body.setOutputMarkupId(true);
			
			Link<?> titleLink = new Link<Void>("title-link") {
				public void onClick() {
					setResponsePage(new SearcherDetailDocumentPage<C>(RelatedResourceView.this.getModel(), SearcherDetailRelationshipPanel.this.getSiteModel()));
				}
			};
			
			body.add(titleLink);
			
			titleLink.add(new Label("resource-title", () -> getModel().getObject().getTitle()));
			
			Label description = new Label("resource-description", () -> model.getObject().getService(ContentService.class).getPortalSubtitle());
			description.setEscapeModelStrings(false);
			body.add(description);
			
			add(body);
		}
		
		@Override
		public boolean isVisible() {
			return true;
		}
		
		public void setModel(IModel<C> model) {
			this.model = model;
			C content = getModel().getObject();
			if (content instanceof ResourceContainer) {
				if (!((ResourceContainer)content).getResources().isEmpty()) {
					Resource resource = ((ResourceContainer)content).getResources().get(0);
					this.resourcemodel = new ObjectModel<Resource>(resource);
				}
			}
		}
		
		public IModel<C> getModel() {
			return model;
		}
		
		public IModel<Resource> getResourceModel() {
			return resourcemodel;
		}
		
		public Component getImage(IModel<Resource> model) {
			try {
				return (new ResourceIcon("image", model.getObject()).setVisible(false));
			} 
			catch (Exception e) {
				logger.error(e);
				WebMarkupContainer c=new WebMarkupContainer("image");
				c.setVisible(false);
				return c;
			}
			
			
		}
		
		public Component getGlyphIcon(IModel<Resource> model) {
			String gi;

			if (model==null || model.getObject()==null)
				return new ResourceGlyphIcon("glyphicon");
			
			else if(model.getObject() instanceof KBFile)
				gi = ((KBFile) model.getObject()).getGlyphIcon();

			else if(model.getObject() instanceof ExternalResource)
				gi = ((ExternalResource) model.getObject()).getGlyphIcon();
			
			else
				return new ResourceGlyphIcon("glyphicon");
			
			return new ResourceGlyphIcon("glyphicon", gi);
		}
		
		public String getSummary() {
			return getModel().getObject().getService(ContentService.class).getSummary();
		}
		
		@Override
		public void onDetach() {
			super.onDetach();

			if (relations!=null)
				relations.forEach(item->item.detach());
			
			if (getModel()!=null)
				getModel().detach();
			
			if (getResourceModel()!=null)
				getResourceModel().detach();
		}
	}	
	
	private boolean isReadable(Content content) {
		return ServiceLocator.getService(ContentSystemSecurityService.class).isReadable(content);
	}	
//
//	private ContentDao getContentDao() {
//		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
//	}
}
