package kbee.web.searcher;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.services.BrandingWebService;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.model.PortalViewRender;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.portal6.factory.PanelPortalModel;
import kbee.web.portal6.panel.PortalPanel;
import kbee.web.searcher.panel.SearcherSimpleErrorPanel;
import kbee.web.searcher.searchform.AdvancedSearchClickEvent;

import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.user.PreferencesService;


/**
 * @param <T>
 */
public class PortalSearchForm<T extends PortalObject> extends PortalPanel<T> implements PanelPortalModel<T>, PortalViewRender  {
				
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalSearchForm.class.getName());

	private boolean is_advanced_search = false;
	private boolean is_advanced_search_visible = false;
	
	
	private WebMarkupContainer main_panel;
	private WebMarkupContainer searcher_panel;
	private SearcherForm sf;
	private Panel advanved_sf;
	
	public PortalSearchForm(String id) {
		super(id);
		is_advanced_search=true;
	}
	
	/**
	 */
	@Override
	public void onInitialize() {
		super.onInitialize();
	
		this.main_panel = new WebMarkupContainer("main-container");
		this.main_panel.setOutputMarkupId(true);
		add(this.main_panel);
		
		this.searcher_panel = new WebMarkupContainer("searcher-container");
		this.searcher_panel.setOutputMarkupId(true);
		this.main_panel.add(this.searcher_panel);
		
		String is_a=((com.novamens.kbee.security.KbeeUser) getSessionUser()).getService(PreferencesService.class).getValue("portal-"+getModel().getObject().getSite().getKey(), "advanced-search", "no");
		setAdvancedSearcherVisible(is_a.equals("yes"));
		
		
		
 /**
		PackageResourceReference res = null;
		res=ServiceLocator.getService(BrandingWebService.class).getSearchLibraryBckImage( getModel().getObject().getSite().getKey());
		String imagehref = RequestCycle.get().urlFor(res, null).toString();
		main_panel.add(new AttributeModifier("style", "background: url(" + imagehref + ") no-repeat 0 0 scroll; background-size: cover;"));
		
	**/
		
		main_panel.add(new AttributeModifier("style", "background: #0d5072;"));
		
		
		
		// Search  ---------------
		
		try {
			
			IModel<Site> m=new ObjectModel<Site>(getModel().getObject().getSite());
			this.sf=new SearcherForm("main-searcher", m, m.getObject().getTitle());
			this.sf.setAdvancedSearchLinkVisible(true);
			this.searcher_panel.add(this.sf);
			this.sf.setVisible(!isAdvancedSearchVisible());
		}
		catch (Exception e) {
			logger.error(e);
			this.searcher_panel.addOrReplace(new SearcherSimpleErrorPanel("main-searcher", e.getClass().getSimpleName(), e.getMessage()));
		}
		
		// Advanced search  ---------------
 
		try {
			if (!isAdvancedSearchVisible()) {
				advanved_sf = new InvisiblePanel("advanced-searcher");
			} else {
				 advanved_sf = getAdvancedSearchPanel();
			}
			this.searcher_panel.add(advanved_sf);
			
		} catch (Exception e) {
			logger.error(e);
			this.searcher_panel.addOrReplace(new SearcherSimpleErrorPanel("advanced-searcher", e.getClass().getSimpleName(), e.getMessage()));
		}
	}

	@Override
	public void addListeners() {
		super.addListeners();
		
	add(new WicketEventListener<AdvancedSearchClickEvent>() {
		private static final long serialVersionUID = 1L;
			@Override
			public
			void onEvent(AdvancedSearchClickEvent event) {
				
				try {

					setAdvancedSearcherVisible(!isAdvancedSearchVisible());
					
					if (!isAdvancedSearchVisible()) {
						 advanved_sf.setVisible(false);
						 sf.setVisible(true);
						 event.getRequestTarget().add(searcher_panel);
					}
					else {
					
						if (advanved_sf instanceof InvisiblePanel) {
							advanved_sf =getAdvancedSearchPanel();
							searcher_panel.addOrReplace(advanved_sf);
						}
						advanved_sf.setVisible(true);
						sf.setVisible(false);
						 event.getRequestTarget().add(searcher_panel);
					}
					
					((com.novamens.kbee.security.KbeeUser) getSessionUser()).getService(PreferencesService.class).setValue("portal-"+getModel().getObject().getSite().getKey(), "advanced-search",	isAdvancedSearchVisible() ? "yes" : "no");
				} catch (Exception e) {
					logger.error(e);
				}
			}
		});
	}
	
	protected Panel getAdvancedSearchPanel() {
		return new SearcherAdvancedSearchForm("advanced-searcher", new ObjectModel<Site>(getModel().getObject().getSite()));
	}
	
	public boolean isAdvancedSearch() {
		return is_advanced_search;
	}

	public boolean isAdvancedSearchVisible() {
		return is_advanced_search_visible;
	}

	protected void setAdvancedSearcherVisible(boolean b) {
		is_advanced_search_visible = b;
	}
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	@Override
	public void setPortalModel(IModel<T> model) {
		setModel(model);
	}

	@Override
	public IModel<T> getPortalModel() {
		return getModel();
	}	

}
