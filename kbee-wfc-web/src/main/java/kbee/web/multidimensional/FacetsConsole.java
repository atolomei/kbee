package kbee.web.multidimensional;


import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import java.util.List;
import java.util.Locale;

import com.novamens.content.entity.Person;
import com.novamens.content.multidimensional.FacetWrapper;
import com.novamens.kbee.wicket.markup.html.console.grid.*;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.datetime.DateTimeService;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.content.multidimensional.AttributeFacet;
import com.novamens.kbee.content.multidimensional.ClassifierHierarchicalFacet;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.security.Identifiable;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.actions.ContextMenuPanel;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.BreadCrumb;

import kbee.web.console.AbstractSimpleConsole;
import kbee.web.console.ExpandedPanel;
import kbee.web.console.grid.LinkPredicateKbeeGridColumn;
import kbee.web.dashboard.LabelPanel;
import kbee.web.model.object.ObjectAuditModal;
import kbee.web.model.object.WrapperModel;
import kbee.web.nav.SecurityBC;
import kbee.web.object.ObjectStatusColumn;
import kbee.web.query.FacetsQuery;

@SuppressWarnings({ "serial", "deprecation" })
public abstract class FacetsConsole extends AbstractSimpleConsole<Facet> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(FacetsConsole.class.getName());
							
	final boolean is_support = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());
	final boolean is_root	 = ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 

	private Locale user_locale;
	private ZoneId user_zoneid;

	private List<GridColumn<SearchResult,String>> columns;
	
	public FacetsConsole(Query query) {
		super("facets", query);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		user_zoneid = ZoneId.of(getSessionUser().getTimeZone());
		if (user_zoneid==null) 
			user_zoneid=ZoneId.systemDefault();
		user_locale = getSessionUser().getLocale();
	}

	@Override
	public boolean isSelectionEnabled() {
		return false;
	}
	
	@Override
	protected Panel getItemListPanel(IModel<Facet> model, int index) {
		return new LabelPanel("item", new Model<String>(model.getObject().toString()));
	}
	
	
	@Override
	public void onDetach() {
		super.onDetach();
		this.columns=null;
	}
	
	@Override
	public Query newQuery() {
		return setUserPreference(new FacetsQuery());
	}
	
	public Page getConsolePage(Query query) {
		return getConsolePage(query, -1);
	}

	@Override
	protected boolean isFiltersEnabled() {
		return false;
	}
	

	protected BreadCrumb getBreadCrumb() {
		return new BreadCrumb(new SecurityBC());
	};

	
	@Override
	protected Panel getMenu(IModel<Facet> model) {
		
		ContextMenuPanel<Facet> menu = new ContextMenuPanel<Facet>(model);
		
		menu.setOutputMarkupId(true);
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Facet>(id) {
				public void onClick(AjaxRequestTarget target) {
					setResponsePage(FacetsConsole.this.getPage(getModel(), FacetsConsole.this.getIndex(getModel().getObject()), false));
				}
				@Override 
				public String getLabel() {
					return FacetsConsole.this.getLabel("facetsconsole.contextmenu.open").getObject();
				}
			}
		);
		
		menu.addItem(id ->
			new AjaxMenuItemPanelV5<Facet>(id) {
				@SuppressWarnings("unchecked")
				public void onClick(AjaxRequestTarget target) {
					Modal modal = FacetsConsole.this.getAuditTrailModal();
					((ObjectAuditModal<Facet>)modal).open(target, getModel(), true);
				}
				@Override 
				public String getLabel() {
					return getConsoleLabel("facetsconsole.contextmenu.audittrail").getObject();
				}
			}
		);
		
		return menu;
	}

	/**
	 * Note that the Query is Hibernate Query
	 */
	@Override
	public List<GridColumn<SearchResult, String>> getColumns() {
		
		if (this.columns!=null)
			return this.columns;
		
		this.columns = new ArrayList<GridColumn<SearchResult,String>>();

		this.columns.add(new ObjectStatusColumn<Person>("icon_status", getName(), new Model<String>("St")));

		
		//final Locale locale = getSessionUser().getLocale();
		
		LinkPredicateKbeeGridColumn<Facet> titleColumn =
			new LinkPredicateKbeeGridColumn<Facet>("title", getLabel("facetsconsole.column.name"), "title_sort", facet->facet.getDisplayName(), facet->getModel(facet));
		titleColumn.setContextKey(this.getName() + titleColumn.getContextKey());
		
		columns.add(titleColumn);
		
		KbeePredicateGridColumn<Facet> typeColumn = new KbeePredicateGridColumn<Facet>("type", getLabel("facetsconsole.column.type"), null ) {
			protected String getValueAsString(Facet object) {
				String value ="";
				if (object == null) return null;
				if (object instanceof FacetWrapper) {
					object = ((FacetWrapper)object).getFacet();
				}
				if (object instanceof ClassifierHierarchicalFacet) {
					String cn = ((ClassifierHierarchicalFacet) object).getClassifier()!=null?(
							"( " + ((ClassifierHierarchicalFacet) object).getClassifier().getName() + " )") 
							: "";
					value = getLabel("facetsconsole.facetype.classifier").getObject() +  cn ;
				}
				else {
					if (object instanceof AttributeFacet || object instanceof com.novamens.kbee.content.multidimensional.DateFacet) {
						String cn;
						if (object instanceof AttributeFacet) {					
							cn = ((AttributeFacet) object).getAttribute()!=null? ( "( " + ((AttributeFacet) object).getAttribute().getName() +" )") : "";
						}
						else {
							if (((com.novamens.kbee.content.multidimensional.DateFacet) object).getAttribute()!=null) 
								cn = " ( " + ((com.novamens.kbee.content.multidimensional.DateFacet) object).getAttribute().getName() +" )";
							else {
								cn = "  ( " + ((com.novamens.kbee.content.multidimensional.DateFacet) object).getDisplayName() +" )";
							}
										
									  
						}
						value = getLabel("facetsconsole.facetype.attribute").getObject() + cn;
					}
					else {
						value = getLabel("facetsconsole.facetype.canonical").getObject();
					}
				}
				return value;
			}

			
			protected String getValueAsHTML(Facet object) {
				String value ="";
				if (object == null) return null;
				if (object instanceof FacetWrapper) {
					object = ((FacetWrapper)object).getFacet();
				}
				if (object instanceof ClassifierHierarchicalFacet) {
					String cn = ((ClassifierHierarchicalFacet) object).getClassifier()!=null?(
							"<span class=\"ago\"> ( " + ((ClassifierHierarchicalFacet) object).getClassifier().getName() + " )</span>") 
							: "";
					value = getLabel("facetsconsole.facetype.classifier").getObject() +  cn ;
				}
				else {
					if (object instanceof AttributeFacet || object instanceof com.novamens.kbee.content.multidimensional.DateFacet) {
						String cn;
						if (object instanceof AttributeFacet) {					
							cn = ((AttributeFacet) object).getAttribute()!=null? ( "<span class=\"ago\"> ( " + ((AttributeFacet) object).getAttribute().getName() +" )</span>") : "";
						}
						else {
							if (((com.novamens.kbee.content.multidimensional.DateFacet) object).getAttribute()!=null) 
								cn = " <span class=\"ago\">( " + ((com.novamens.kbee.content.multidimensional.DateFacet) object).getAttribute().getName() +" )</span>";
							else {
								cn = " <span class=\"ago\"> ( " + ((com.novamens.kbee.content.multidimensional.DateFacet) object).getDisplayName() +" )</span>";
							}
										
									  
						}
						value = getLabel("facetsconsole.facetype.attribute").getObject() + cn;
					}
					else {
						value = getLabel("facetsconsole.facetype.canonical").getObject();
					}
				}
				return value;
			}
		};
		typeColumn.setContextKey(this.getName() + typeColumn.getContextKey());
		columns.add(typeColumn);
		
		
		this.columns.add(new LastModifiedColumn<AbstractObject>("modified", getLabel("facetsconsole.column.modified"), "modified") {
			protected OffsetDateTime getOffsetDateTime(AbstractObject object) {
				try {
					if (object.getLastModifiedOffsetDateTime()==null) {
						
						
						
						return object.getCreationOffsetDateTime();
						
					}
					return object.getLastModifiedOffsetDateTime();
				} 
				catch (Exception e) {
					logger.error(e);
					return null;
				}
			}
			
			@Override
			protected String getContextKey() {
				return FacetsConsole.this.getName() + super.getContextKey();
			}
		});
	
		{
		KbeePredicateGridColumn<Facet> idColumn = new KbeePredicateGridColumn<>("id", getLabel("facetsconsole.column.id"),  (facet) -> facet.getName());
		idColumn.setContextKey(this.getName() + idColumn.getContextKey());
		columns.add(idColumn);
		}
		
		/*
		{
			
			
			KbeePredicateGridColumn<Facet> idColumn = new KbeePredicateGridColumn<>("published", getLabel("status"),  (facet) -> getFacetPusblifacet.getName());
			idColumn.setContextKey(this.getName() + idColumn.getContextKey());
			columns.add(idColumn);
			
			
		}*/
		
		return this.columns;
	}
	
	@Override
	protected void addModals () {
		super.addModals();
		replace(new ObjectAuditModal<Facet>("audit-trail-modal"));
	}
	
	protected Page getPage(IModel<Facet> model, long index, boolean isnew) {
		return new FacetPage(model);
	}
	
	@Override
	protected void addListeners() {
		super.addListeners();
		add(new WicketEventListener<ClickEvent<Facet>>() {
			@Override
			public void onEvent(ClickEvent<Facet> event) {
				setResponsePage(FacetsConsole.this.getPage(event.getModel(), getIndex(event.getModel().getObject()), false));
			}
		});
	}
	

	/**
	protected Panel getNavigationPanel(long index) {						

		GlobalNavigationBar<Facet> navigationbar = new GlobalNavigationBar<Facet>("navigation",  getDisplayName().getObject()) {
		//GlobalNavigationBar<Facet> navigationbar = new GlobalNavigationBar<Facet>("navigation", getSearcher(), index, getDisplayName().getObject()) {
			@Override
			public void onNavigate(Facet dataset) {
				IModel<Facet> model = new ObjectModel<Facet>(dataset);
				model.detach();
				setResponsePage(new FacetPage(model));
			}
			@Override
			public void onDetach() {
				super.onDetach();
				FacetsConsole.this.onDetach();
			}
			@Override
			public void onReturn() {
				setResponsePage(getConsolePage(getQuery(), -1));
			}
			@Override
			protected void onSearch(AjaxRequestTarget target, String text) {
				getQuery().getParameters().put("text", text);
				getQuery().getParameters().put("sort", "relevance");
				setResponsePage(getConsolePage(getQuery(), -1));
			}
		};
		navigationbar.setSearchPlaceHolder(new StringResourceModel("searchplaceholder", FacetsConsole.this, null).getString());
		return navigationbar;									
	}
	**/

	protected IModel<Facet> getModel(Facet facet) {
		if (facet instanceof Identifiable && ((Identifiable)facet).getId()!=null)  {
			return new ObjectModel<Facet>(facet);
		}
		else {
			return new WrapperModel(facet);
		}
	}
		
	protected IModel<String> getStringDateModel(OffsetDateTime dt) {
		if (dt==null)
			return new Model<String>("err");
		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		ZonedDateTime zd = ZonedDateTime.ofInstant(dt.toInstant(), user_zoneid);
		return new Model<String>(service.timeElapsed(zd, user_zoneid, user_locale, DateTimeService.DATE_COLlOQUIAL_AGO, "ago"));
	}
	
	@Override
	protected Panel getPanel(IModel<Facet> model) {
		return new ExpandedPanel<Facet>("editor", this, model);
	}
	
	@Override
	protected Panel getPanel(IModel<Facet> model, List<String> snippets) {
		return new ExpandedPanel<Facet>("editor", this, model, snippets);
	}
	
	@Override
	protected boolean hasExpander() {
		return false;
	} 
}
