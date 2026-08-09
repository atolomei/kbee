package kbee.web.searcher.panel;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.library.Library;
import com.novamens.content.relationshipsbycriteria.RelationshipByCriteriaTemplate;
import com.novamens.content.relationshipsbycriteria.RelationshipsByCriteriaService;
import com.novamens.content.service.ContentService;
import com.novamens.dom.DomainType;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.console.grid.LabelSetPanel;
import kbee.web.error.ErrorPanel;
import kbee.web.nav.ArchiveBC;
import kbee.web.nav.ContentBaseBC;
import kbee.web.nav.DropDownMenuBC;
import kbee.web.nav.HomeBC;
import kbee.web.nav.LibraryBC;
import kbee.web.nav.RecycleBinBC;
import kbee.web.nav.SeparatorBC;
import kbee.web.workflow.task.PageTaskToolbar;

public class SearcherDetailHeaderPanel<T extends Content> extends SearcherDetailPanel<T> {

	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger =  kbee.util.logging.Logger.getLogger(SearcherDetailHeaderPanel.class.getName());

	final boolean is_root = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());

	
	private WebMarkupContainer main_area;
	private WebMarkupContainer main_area_container;
	
	private Panel navigator;
	//private Panel tools;

	private PageTaskToolbar<T> toolbar;
	
	private boolean isConsole;
	private boolean isStandAlonePage;	

	/**
	 * 
	 * 
	 * @param id
	 * @param model
	 * @param site_model
	 */
	public SearcherDetailHeaderPanel(String id, IModel<T> model, IModel<Site> site_model) {
				this(id, model, site_model, null, false);
	}
	
	
	public SearcherDetailHeaderPanel(String id, IModel<T> model, IModel<Site> site_model, Panel navigator, boolean  isConsole) {
		super(id, model, site_model);
		this.navigator=navigator;
		this.isConsole= isConsole;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addComponents();
	}
	
	public boolean isConsole() {
		return this.isConsole;
	}
	
	
	public void setToolsPanel(Panel panel) {
		try {																								
			if (!panel.getId().equals("tools"))
					throw new IllegalArgumentException("id must be = 'tools'");
			//this.tools=panel;
			getMainAreaContainer().addOrReplace(panel);
			
		} catch (Exception e) {		
			logger.error(e);
			getMainAreaContainer().addOrReplace(new ErrorPanel("tools",e ));
		}
	}
	
	/**
	public void setNavigationPanel(Panel panel) {
		try {																								
			if (!panel.getId().equals("navigator"))
					throw new IllegalArgumentException("id must be navigation");
			
			this.navigator=panel;
			getMainArea().addOrReplace(panel);
			
		} catch (Exception e) {		
			logger.error(e);
			getMainArea().addOrReplace(new ErrorPanel("navigator",e ));
		}
		
	}
	**/


	
	public void addToolbarPanel() {
		
		try {																								
			if (getSessionUser()==null) { 
				getMainArea().addOrReplace(new InvisiblePanel("toolbar"));
				return;
			}
			
			if (isStandAlonePage()) {
				getMainArea().addOrReplace(new InvisiblePanel("toolbar"));
				return;
			}
			
			if (!hasToolbar()) {
				getMainArea().addOrReplace(new InvisiblePanel("toolbar"));
				return;
			}
			
			List<WebMarkupContainer> l_list = new ArrayList<WebMarkupContainer>();
			List<WebMarkupContainer> r_list = new ArrayList<WebMarkupContainer>();
			
			r_list.add(new SearcherDetailToolsPanel<T>("panel", getModel(), getSiteModel()));
			
			if (this.navigator!=null)
				r_list.add(this.navigator);

			
			toolbar = new PageTaskToolbar<T>("toolbar", getModel(), l_list, r_list);
			getMainArea().addOrReplace(toolbar);
					
			
		} catch (Exception e) {		
			logger.error(e);
			getMainArea().addOrReplace(new ErrorPanel("toolbar",e ));
		}
	}
	

	public void addToolsPanel() {
		try {																								
			if (getSessionUser()==null) { 
				getMainArea().addOrReplace(new InvisiblePanel("tools"));
				return;
			}
			
			if (isStandAlonePage()) {
				getMainArea().addOrReplace(new InvisiblePanel("tools"));
				return;
			}
	
			//getMainArea().addOrReplace(new DummyBlockPanel("tools"));
			getMainArea().addOrReplace(new InvisiblePanel("tools"));

			// getMainArea().addOrReplace(new Searche rDetailToolsPanel<T>("tools", getModel(), getSiteModel()));
			
			
		} catch (Exception e) {		
			logger.error(e);
			getMainArea().addOrReplace(new ErrorPanel("tools",e ));
		}
	}
	
						
	public void addTagsPanel() {
		try {																								
			if (getSessionUser()==null) { 
				getMainArea().addOrReplace(new InvisiblePanel("tags"));
				return;
			}
			
			if (isStandAlonePage()) {
				getMainArea().addOrReplace(new InvisiblePanel("tags"));
				return;
			}
	
			
			LabelSetPanel<T> labelset = new LabelSetPanel<T>("tags", getModel(), 
					false,  // remove not enabled 
					true,   // label list 
					false); // dropdown

			
			getMainArea().addOrReplace(labelset);

			// getMainArea().addOrReplace(new Searche rDetailToolsPanel<T>("tools", getModel(), getSiteModel()));
			
			
		} catch (Exception e) {		
			logger.error(e);
			getMainArea().addOrReplace(new ErrorPanel("tags",e ));
		}
	}
	
	
	/**
	 * 
	 */
	public void addCriteriaRelationshipPanel() {

		if (isConsole()) {
			getMainArea().add(new InvisiblePanel("criteria-relationship"));
			return;
		}
		
		
		try {
			
			Map<RelationshipByCriteriaTemplate, List<Content>> related = getModel().getObject().getService(RelationshipsByCriteriaService.class).getRelatedTemplates();
			if (related!=null && !related.isEmpty()) {
				SearcherDetailCriteriaRelationshipPanel<T> panel = new SearcherDetailCriteriaRelationshipPanel<T>("criteria-relationship", 
						getModel(), 
						related,
						getSiteModel());
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
			
			getMainArea().add(new InvisiblePanel("metadata"));

			/**
			if (this.isStandAlonePage()) {
				getMainArea().add(new InvisiblePanel("metadata"));
				return;
			}
			SearcherDetailMetadataPanel<T> panel = new SearcherDetailMetadataPanel<T>("metadata", getModel(), getSiteModel());
			panel.setVisible(panel.getSubtitle()!=null);
			getMainArea().add(panel);
			**/
		} 
		catch (Exception e) {		
			logger.error(e);
			getMainArea().addOrReplace(new InvisiblePanel("metadata"));
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
	 
		protected List<Library> getLibraries() {
			List<Library> cabinets = new ArrayList<Library>();
			try {
				for (Library cabinet : getRepository(Library.class).findAll()) {
					if (cabinet.isReadable()) 
						cabinets.add(cabinet);
				};
			} catch (Exception e) {
				logger.error(e);
			}
			return cabinets;
		}
		
		
	 public void addBreadcrumbPanelPanel() {
		 
			try {
				
				if (getSessionUser()==null) {
					getMainArea().add( new InvisiblePanel("breadcrumb"));
					return;
				}

				if (isStandAlonePage()) {
					getMainArea().add( new InvisiblePanel("breadcrumb"));
					return;
				}
				
				if (!hasBreadcrumb()) {
					getMainArea().addOrReplace(new InvisiblePanel("breadcrumb"));
					return;
				}
		
				MenuBreadCrumbPanel<?>  bc =new MenuBreadCrumbPanel<>();
				bc.addElement( new HomeBC());
		 		
				
				
				if (getModel().getObject().getState()==ObjectState.DELETED) {
					bc.addElement( new RecycleBinBC());
				}
				else {
					DropDownMenuBC<?> dd = new DropDownMenuBC<Void>();
					dd.addElement(new ContentBaseBC(), true);
				
					for (Library library : getLibraries()) {
						if (is_root || (!isExpressVersion()) || (library.isReadOnly())) 
							dd.addElement(new LibraryBC( new ObjectModel<Library>(library)));
					}
					if (!isExpressVersion()) { 
						dd.addElement(new SeparatorBC());
						dd.addElement(new ArchiveBC());
					}
					bc.addElement(dd);
				}
				
				
				getMainArea().add(bc);

			} catch (Exception e) {
				logger.error(e);
				getMainArea().addOrReplace(new InvisiblePanel("breadcrumb"));
			}
	 }
	 
	 
	 
	 public boolean isStandAlonePage() {
		return isStandAlonePage;
	}


	public void addTitlePanel() {
			try {
			
				if (!isTitle()) {
					getMainArea().addOrReplace(new InvisiblePanel("title"));
					return;
				}
					
				SearcherDetailTitlePanel<T> panel = new SearcherDetailTitlePanel<T>("title", getModel(), getSiteModel(), isConsole());
				panel.setShowAbstract(true);
				getMainArea().add(panel);

			} catch (Exception e) {
				logger.error(e);
				getMainArea().addOrReplace(new InvisiblePanel("title"));
			}
		}

	

	public boolean isTitle() {
		return true;
	}


	public void addPreviousVersionPanel() {
			try {
										
				WebMarkupContainer previous_version = new WebMarkupContainer("version") {
					private static final long serialVersionUID = 1L;
					@Override
					public boolean isVisible() {
						
						if (getContent().getWorkspace()!=null)
							return false;

						if (getContent().getState()==ObjectState.DELETED)
							return false;
						
						return !getContent().isHeadVersion() && 
							   !getContent().getService(ContentService.class).isValidVersion();
					}
				};
				
				
				Label versionNumber = new Label("version-number", new StringResourceModel("version").setParameters( new Object[]  { String.valueOf(getContent().getVersion())}));
				previous_version.add(versionNumber);
				
				getMainArea().addOrReplace(previous_version);

			} catch (Exception e) {
				logger.error(e);
				getMainArea().addOrReplace(new InvisiblePanel("version"));
			}
		}
	
	
	
	
	
	
	
	/**
	 * 		WebMarkupContainer previous_version = new WebMarkupContainer("previous-version") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				
				if (getContent().getWorkspace()!=null)
					return false;

				if (getContent().getState()==ObjectState.DELETED)
					return false;
				
				return !getContent().isHeadVersion() && 
					   !getContent().getService(ContentService.class).isValidVersion();
			}
		};

	 * @return
	 */
	
	
	
	
	
	 
	private WebMarkupContainer getMainArea() {
		return main_area;
	}

	private WebMarkupContainer getMainAreaContainer() {
		return main_area_container;
	}
	


	private void addComponents() {
		
		this.main_area_container = new WebMarkupContainer("main-area-container");
		this.main_area_container.setOutputMarkupId(true);
		add(this.main_area_container);
		
		this.main_area = new WebMarkupContainer("main-area");
		this.main_area.setOutputMarkupId(true);
		this.main_area_container.add(this.main_area);

		//if (this.navigator==null)
		//	this.main_area.add(new InvisiblePanel("navigator"));
		//else
		//	this.main_area.add(this.navigator);
			

		addCriteriaRelationshipPanel();
		addBreadcrumbPanelPanel();
		addTitlePanel();
		addMetadataPanel();
		
		addTagsPanel();
		addToolsPanel();
		addToolbarPanel();
		addPreviousVersionPanel();
		
		
		
	}

	
	boolean has_toolbar = true;
	
	public void setHasToolbar( boolean b) {
		this.has_toolbar=b;
	}
	public boolean hasToolbar() {
		return has_toolbar;
	}


	boolean has_breadcrumb = true;
				
	public void setHasBreadcrumb( boolean b) {
		this.has_breadcrumb=b;
	}
	public boolean hasBreadcrumb() {
		return has_breadcrumb;
	}

	
	protected boolean isExpressVersion() {
		try {
			return getDomain().getDomainType()==DomainType.EXPRESS;
		}
		 catch (Exception e) {
			 logger.error(e);
			 return false;
		 }
	}


	public void setStandAlonePage(boolean tre) {
		this.isStandAlonePage=tre;
	}

}


//addBreadcrumbToolsPanel();
