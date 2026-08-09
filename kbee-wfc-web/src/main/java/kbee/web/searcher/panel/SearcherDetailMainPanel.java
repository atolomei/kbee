package kbee.web.searcher.panel;

import java.util.List;


import org.apache.wicket.markup.html.WebMarkupContainer;

import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.communication.OrganizationalText;
import com.novamens.content.relationshipsbycriteria.RelationshipsByCriteriaService;
import com.novamens.content.service.ContentService;
import com.novamens.kbee.wicket.markup.html.event.GeneralWicketAjaxEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;

import kbee.web.content.panel.ContentLibraryPanel;

public class SearcherDetailMainPanel<T extends Content> extends SearcherDetailPanel<T> {
	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger =  kbee.util.logging.Logger.getLogger(SearcherDetailMainPanel.class.getName());
	
	private WebMarkupContainer main_area;
	private WebMarkupContainer main_area_container;
	
	private boolean isConsoles = false;
	
	public SearcherDetailMainPanel(String id, IModel<T> model, IModel<Site> site_model) {
		this(id, model, site_model, false);
	}
	
	public SearcherDetailMainPanel(String id, IModel<T> model, IModel<Site> site_model, boolean isConsole) {
		super(id, model, site_model);
		this.isConsoles=isConsole;
	}
	
	public boolean isConsole() {
		return this.isConsoles;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addComponents();
	}

	private void addComponents() {
		
		this.main_area_container = new WebMarkupContainer("main-area-container");
		this.main_area_container.setOutputMarkupId(true);
		add(this.main_area_container);
		this.main_area = new WebMarkupContainer("main-area");
		this.main_area.setOutputMarkupId(true);
		this.main_area_container.add(this.main_area);
		try {
			addAbstractPanel();
			addVigencyPanel(); // for Console. Head version that is not valid, portals should not show invalid versions
			addVersionPanel();
			addTextPanel();
			addResourcesPanel();
			addRelationshipPanel();
			addAttributesPanel();
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}
	
	public void addVersionPanel() {
		try {																								
			getMainArea().add(new ContentLibraryPanel<T>("version", getModel(), getSiteModel(), isConsole(), false));
		} 
		catch (Exception e) {		
			logger.error(e);
			getMainArea().addOrReplace(new InvisiblePanel("version"));
		}
	}
	
	public void addVigencyPanel() {
		try {																								
			//logger.debug("addVigencyPanel()");
			if (getModel().getObject().isHeadVersion()) {
				if (!getModel().getObject().getService(ContentService.class).isValid())
					getMainArea().addOrReplace(new SearcherDetailFutureVigencyPanel<T>("vigency", getModel(), getSiteModel(), isConsole()));
				else
					getMainArea().addOrReplace(new InvisiblePanel("vigency"));
				
			} else {
				getMainArea().addOrReplace(new InvisiblePanel("vigency"));
			}
			
		} catch (Exception e) {		
			logger.error(e);
			getMainArea().addOrReplace(new InvisiblePanel("vigency"));
		}

	}
	
	
	/**
	public void addBreadcrumbToolsPanel() {
		try {																								
			SearcherDetail ToolsPanel<T> panel = new SearcherDe tailToolsPanel<T>("detail-tools", getModel(), getSiteModel());
			getMainAreaContainer().add(panel);
			
		} catch (Exception e) {		
			logger.error(e);
			getMainAreaContainer().addOrReplace(new InvisiblePanel("detail-tools"));
		}

	}**/
	
	
	public void addRelatedPanel() {
		try {		
			SearcherDetailRelatedPanel<T> panel = new SearcherDetailRelatedPanel<T>("related", getModel(), getSiteModel());
			getMainArea().add(panel);
		} catch (Exception e) {		
			logger.error(e);
			getMainArea().addOrReplace(new InvisiblePanel("related"));
		}

	}
	

	public void addRelationshipPanel() {
		try {
			if (getModel().getObject().getContentTemplate().getRelations().isEmpty() && 
				getModel().getObject().getContentTemplate().getReverseRelations().isEmpty()) {
				getMainArea().addOrReplace(new InvisiblePanel("relationships"));
				return;
			}
			SearcherDetailRelationshipPanel<T> panel = new SearcherDetailRelationshipPanel<T>("relationships", getModel(), getSiteModel());
			getMainArea().add(panel);
		} 
		catch (Exception e) {		
			logger.error(e);
			getMainArea().addOrReplace(new InvisiblePanel("relationships"));
		}
	}
					
	public void addReverseRelationshipPanel() {
		
		try {										
			//logger.debug("addReverseRelationshipPanel()");
			if (getModel().getObject().getContentTemplate().getReverseRelations().isEmpty()) {
				getMainArea().addOrReplace(new InvisiblePanel("reverse-relationships"));
				return;
			}
			
			SearcherDetailRelationshipPanel<T> panel = new SearcherDetailRelationshipPanel<T>("reverse-relationships", getModel(), getSiteModel());
			getMainArea().add(panel);
			
		} catch (Exception e) {		
			logger.error(e);
			getMainArea().addOrReplace(new InvisiblePanel("reverse-relationships"));
		}

	}
	
	
	public void addAbstractPanel() {
		
		try {
			//logger.debug("addAbstractPanel()");
			if (getModel().getObject().getContentTemplate().isAbstract() && getModel().getObject().getAbstract()!=null)
				getMainArea().add(new SearcherDetailAbstractPanel<T>("abstract", getModel(), getSiteModel()));
			else
				getMainArea().addOrReplace(new InvisiblePanel("abstract"));
		} catch (Exception e) {		
			logger.error(e);
			getMainArea().addOrReplace(new InvisiblePanel("abstract"));
		}

	}

	/**
	 * 
	 */
	public void addCriteriaRelationshipPanel() {
		try {
			List<Content> related = getModel().getObject().getService(RelationshipsByCriteriaService.class).getRelated();
			if (related!=null && !related.isEmpty()) {
				SearcherDetailCriteriaRelationshipPanel<T> panel = new SearcherDetailCriteriaRelationshipPanel<T>("criteria-relationship", getModel(), getSiteModel());
				getMainArea().add(panel);
			}
			else {
				getMainArea().add(new InvisiblePanel("criteria-relationship"));
			}
		} 
		catch (Exception e) {		
			logger.error(e);
			getMainArea().addOrReplace(new InvisiblePanel("criteria-relationship"));
		}
	}
	
	/**
	 * 
	 */
	public void addMetadataPanel() {
		try {
			SearcherDetailMetadataPanel<T> panel = new SearcherDetailMetadataPanel<T>("metadata", getModel(), getSiteModel());
			panel.setVisible(panel.getSubtitle()!=null);
			getMainArea().add(panel);
		} 
		catch (Exception e) {		
			logger.error(e);
			getMainArea().addOrReplace(new InvisiblePanel("metadata"));
		}
	}
	
	/**
	 * 
	 */
	public void addAttributesPanel() {
		try {

			getMainArea().add(new SearcherDetailAttributesPanel<T>("attributes", getModel(), getSiteModel(), isConsole()));
		} 
		catch (Exception e) {		
			logger.error(e);
			getMainArea().addOrReplace(new InvisiblePanel("attributes"));
		}
	}
	
	@Override
	public void onDetach() {
		 super.onDetach();
		 if (getMainArea()!=null)
			 getMainArea().detach();
		 
		 if (getMainAreaContainer()!=null)
			 getMainAreaContainer().detach();
	}
	
	@Override
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<GeneralWicketAjaxEvent>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void onEvent( GeneralWicketAjaxEvent event) {
			}
		});
	}

	private void addResourcesPanel() {
		try {

			if (getModel().getObject().getContentTemplate().isResources()) {
				SearcherDetailResourcesPanel<T> panel = new SearcherDetailResourcesPanel<T>("resources", getModel(), getSiteModel(), !this.isConsoles, false);
				getMainArea().add(panel);
			}
			else  {
				getMainArea().addOrReplace(new InvisiblePanel("resources"));
			}

		} catch (Exception e) {		
			logger.error(e);
			getMainArea().addOrReplace(new InvisiblePanel("resources"));
		}
	}
	
	private void addTextPanel() {
		try {
			if (getModel().getObject() instanceof OrganizationalText) {
				getMainArea().addOrReplace(new SearcherDetailTextPanel<T>("text", getModel(), getSiteModel(), this.isConsoles));
			}
			else  {
				getMainArea().addOrReplace(new InvisiblePanel("text"));
			}

		} catch (Exception e) {		
			logger.error(e);
			getMainArea().addOrReplace(new InvisiblePanel("text"));
		}
	}

	private WebMarkupContainer getMainArea() {
		return main_area;
	}

	private WebMarkupContainer getMainAreaContainer() {
		return main_area_container;
	}
}
