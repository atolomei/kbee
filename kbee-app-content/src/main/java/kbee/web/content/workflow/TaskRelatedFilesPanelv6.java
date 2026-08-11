package kbee.web.content.workflow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.document.IDoc;
import com.novamens.content.document.TreeIDoc;
import com.novamens.content.library.Library;
import com.novamens.content.library.LibraryService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Filter;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.content.workflow.KbeeContext;
import com.novamens.kbee.content.workflow.WebTask;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.ProxyUtil;
import com.novamens.wicket.markup.html.repeater.util.Searcher;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.workflow.WorkflowContext;

import kbee.web.content.nav.ContentNavigationBar;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.page.AbstractApplicationPage;
import kbee.web.query.ContextCriteriaQuery;
import kbee.web.service.ApplicationSiteMapService;
			
public class TaskRelatedFilesPanelv6<T extends Content> extends ModelPanel<WorkflowContext> {
			
	private static final long serialVersionUID = 1L;

	private List<IModel<Content>> contentlist;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TaskRelatedFilesPanelv6.class.getName());
	
	private Map<Integer, Boolean> item_expanded = new HashMap<Integer, Boolean>();
	private ContextCriteriaQuery query;

	public TaskRelatedFilesPanelv6(String id, IModel<WorkflowContext> model) {
		super(id, model);
		setOutputMarkupId(true); 
	}
	
	
	/**
	 * 
	 */
	public void onClose(AjaxRequestTarget target) {
		
	}
	
	/**
	 * 
	 */
	public List<IModel<Content>> getContent() {
	
		if (contentlist!=null)
			return contentlist;
		
		contentlist = new ArrayList<IModel<Content>>();
	
		int MAX_VALUES = 0;
		try {
			String mv = getContentDao().findSystemParameterValueByKey("related.task.suggestions", "10");
			MAX_VALUES = Integer.valueOf(mv).intValue();
			
		} catch (Exception e) {
			logger.error(e);
			MAX_VALUES = 10;
		}
		
		try {
			
			IModel<Content> contentmodel = new ObjectModel<Content>(((KbeeContext)getModelObject()).getContent());
			
			if (query==null)
				 query = new ContextCriteriaQuery(contentmodel, ((WebTask)getModelObject().getTask()).getRelatedCriteria());
			
			logger.debug(query); 
			
			
			ResultSet resultset = query.execute();
			int i = 0;
			
			while (resultset.hasNext() && i<MAX_VALUES) {
				SearchResult result = resultset.next();
				if (((Content) result.getObject()).getState()!=ObjectState.ENABLED) {
						logger.debug( "Incorrect Query: " + ((Content) result.getObject()).getTitle() + "   " + ((Content) result.getObject()).getState().getLabel());
				}		

				if (result.getObject() instanceof Content && 
						((Content) result.getObject()).isHeadVersion() && 
						(((Content) result.getObject()).getState()==ObjectState.ENABLED)) { 
					i++;
					contentlist.add(new ObjectModel<Content>((Content)result.getObject()));
				}
			}
			
			
			contentlist.sort(new Comparator<IModel<Content>>() {

				@Override
				public int compare(IModel<Content> o1, IModel<Content> o2) {
					
					if (o1.getObject()==null)
						return (o2.getObject()!=null) ? 1 : -1;
								
					if (o2.getObject()==null)
						return (o1.getObject()!=null) ? -1 : 1;
					
					return o1.getObject().getTitle().compareToIgnoreCase(o2.getObject().getTitle());
				}
				
			});

			
		}
		catch (Exception e) {
			logger.error(e);
		}
		
		return contentlist;
	}
	
	@SuppressWarnings("serial")
	public void onInitialize() {
		super.onInitialize();

		if (get("content")==null) {
			addListView();
			Link<Void> kbase = new Link<Void>("kbase") {
			
				@Override
				public void onClick() {
					boolean done = false;
					for (Library library : getDomain().getService(LibraryService.class).getLibraries()) {
						if (library.getKey().equals(Library.ALL)) {
							done=true;
							setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage(ApplicationSiteMapService.ContentBasePage, new ObjectModel<Library>(library)));
							break;
						}
					};
					
					if (!done) {
						setResponsePage(ServiceLocator.getService(ApplicationSiteMapService.class).getPage(ApplicationSiteMapService.ContentBasePage));
					}	
				}
			};
			add(kbase);
			
			
			
			/*
			AjaxLink<Void> vql = new AjaxLink<Void>("view-iql-query") {

				@Override
				public void onClick(AjaxRequestTarget target) {
					TaskRelatedFilesPanelv6.this.get("iql-query").setVisible(
							! TaskRelatedFilesPanelv6.this.get("iql-query").isVisible());


					TaskRelatedFilesPanelv6.this.get("instanced-iql-query").setVisible(
							! TaskRelatedFilesPanelv6.this.get("instanced-iql-query").isVisible());
					target.add(TaskRelatedFilesPanelv6.this);
					
				}
			};
			
								
			final boolean root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot();

			vql.setVisible(logger.isDebugEnabled() || root );

			
			Label vq = new Label("iql-query", new Model<String>() { 
				public String getObject() {
					return "<b>iql</b> ->" + ((WebTask)getModelObject().getTask()).getRelatedCriteria();
				}
			});
			
			vq.setEscapeModelStrings(false);
			
			vq.setVisible(false);
			add(vq);
			
			
			
			
			

			
			Label vqi = new Label("instanced-iql-query", new Model<String>() { 
				public String getObject() {
				   if (TaskRelatedFilesPanelv6.this.query!=null) {
					   return "<b>query parameters</b> -> " + TaskRelatedFilesPanelv6.this.query.getQuery().getParameters().toString();
				   }
					return "null";	
				}
			});
			
			vqi.setEscapeModelStrings(false);
			vqi.setVisible(false);
			add(vqi);

			
			add(vql);
			*/

			
			
			
			
			
			
			
			
		}
	}
	
	/**
	 * 
	 */
	@Override
	public void onDetach() {
		super.onDetach();
		for (IModel<Content> model : contentlist) 
			model.detach();
		if (this.query!=null)
			this.query.detach();
	}
	
	
	/**
	 * 
	 * @param content
	 * @return
	 */
	protected Panel getPanel(Content content) {
		
//		if (content instanceof IDoc) {
//			IModel<IDoc> model = new ObjectModel<IDoc>((IDoc)content);
//			model.detach();
//			return (new IDocHitExpandedPanel(model));
//		}
//		else if (content instanceof OrganizationalText)
//			return  (new TextHitExpandedPanel(new ObjectModel<OrganizationalText>((OrganizationalText)content)));
//		else if (content instanceof TreeIDoc) {
//			logger.error("TreeIDoc does not have a HitExpandedPanel : " + content.getTitle());
//		}
//		else {
//			logger.error("No valid HitExpandedPanel : " + content.getTitle());
//		}
		return new InvisiblePanel("editor");
	}

	/**
	 * 
	 */
	protected void addListView() {
								
		add(new ListView<IModel<Content>> ("content", getContent()) {
			
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("serial")
			public void populateItem(ListItem<IModel<Content>> item) {
				
				Content content = item.getModelObject().getObject();
				final int index = item.getIndex();
				
				IModel<Content> md = new ObjectModel<Content>(content);
				
				AjaxLink<Content> link=new AjaxLink<Content>("expand", md) {
						private static final long serialVersionUID = 1L;
						@Override
						public void onClick(AjaxRequestTarget target) {
							Integer in=Integer.valueOf(index);
							toggle(in);
							target.add(TaskRelatedFilesPanelv6.this);
						}					
				};
						
				WebMarkupContainer icon = new WebMarkupContainer("icon");
						icon.add(new AttributeModifier("class", new Model<String>() {
							@Override
							public String getObject() {
								return isExpanded(item.getIndex())?"far fa-angle-down fw":"far fa-angle-up fw";
							}
				}));
				link.add(icon);
				item.add(link);

				
				Link<Content> ln = new Link<Content>("title-link", item.getModel().getObject()) {
					@SuppressWarnings("unchecked")
					@Override
					public void onClick() {
						
						Page page = TaskRelatedFilesPanelv6.this.getPage(getModel());
						
						if (page!=null) {
							((AbstractApplicationPage<T>)page).setTopNavigation(getNavigationPanel(getModel(), index));
							setResponsePage(page);
						}
						else {
							logger.error("TaskKnowledgeBasePanel.this.getPage(getModel()) is null");
						}
					}
				};
				
				ln.add(new AttributeModifier("target", "_blank"));
				ln.add(new Label("content-title", content.getTitle()));
				item.add(ln);
				
				Label meta = new Label("metadata", content.getContentTypeClassificationAsString()) {
					/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					@Override
					public boolean isVisible() {
						return !isExpanded(item.getIndex());
					}
				};
				item.add(meta);
				
				Integer in=Integer.valueOf(index);
//				if (isExpanded(in)) {
//					Panel panel;
//					if (content instanceof IDoc)
//						panel = new IDocHitExpandedPanel(new ObjectModel<IDoc>((IDoc) content));
//					else if (content instanceof OrganizationalText)
//						panel = new TextHitExpandedPanel(new ObjectModel<OrganizationalText>( (OrganizationalText) content));
//					else
//						panel = new InvisiblePanel("editor");
//					item.addOrReplace(panel);
//				}
//				else
					item.addOrReplace(new InvisiblePanel("editor"));

			}
		});
	}
	
	
	
	protected boolean isExpanded(Integer index) {
		if (this.item_expanded.containsKey(index))
			return this.item_expanded.get(index).booleanValue();
		this.item_expanded.put(index, Boolean.valueOf(false));
		return this.item_expanded.get(index).booleanValue();
	}
	
				
	private void toggle(Integer index) {
		if (this.item_expanded.containsKey(index)) {
			Boolean b =this.item_expanded.get(index);
			this.item_expanded.put(index, !b);
		}
		else
			this.item_expanded.put(index, Boolean.valueOf(true));
		
	}
	
	
	protected IModel<T> getModel(T object) {
		return new ObjectModel<T>(object, true);
	}
	

	
	protected Query getQuery() {
		return this.query;
	}
	

	protected Panel getNavigationPanel(IModel<Content> model, long index) {
		Panel panel = new ContentNavigationBar<Content>("navigation", model,  new Searcher(getQuery()), index) {
			private static final long serialVersionUID = 1L;
			@Override
			@SuppressWarnings("unchecked")
			public void onNavigate(Content content) {
				IModel<Content> model = new ObjectModel<Content>(content, true); 
				setModel(model);
				Page page = TaskRelatedFilesPanelv6.this.getPage(model);
				((AbstractApplicationPage<Content>)page).setTopNavigation(this);
				setResponsePage(page);
			} 
		};
		return panel;
	}
	
		
	protected Page getPage(IModel<Content> model) {
		
		try {
			Page page = (Page)ServiceLocator.getService(BeansService.class).getBean(ProxyUtil.getClassName(model.getObject()).toLowerCase() + "-page", model);
		return page;
		} catch (Exception e) {
			logger.error(e);
			return new ApplicationErrorPage<Void>(e);
		}
	}

	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
								
	protected Panel getPanel(IModel<IDoc> model, List<String> snippets) {
		
		String bean = ProxyUtil.getClassName(model.getObject()).toLowerCase() +"-panel";
		 
		ViewMode view_mode = ViewMode.ICON; 
		
		try {
			Object textparameter = getQuery().getParameters().get("text");
			String query = textparameter!=null && textparameter instanceof Filter ? (String)((Filter)textparameter).getValue() : (String)textparameter;
			Panel panel = (Panel)ServiceLocator.getService(BeansService.class).getBean(bean, model, view_mode, false, query, snippets);
			return panel;
		} 
		catch (Exception e) {
			logger.error("Can not resolve Spring bean " + bean);
			logger.error(e);
			return new InvisiblePanel("editor");
		}
	}

}
