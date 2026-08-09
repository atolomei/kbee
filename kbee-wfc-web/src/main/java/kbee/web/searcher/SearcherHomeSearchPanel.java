package kbee.web.searcher;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.novamens.kbee.wicket.services.BrandingWebService;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.KBPanel;


public class SearcherHomeSearchPanel extends KBPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private WebMarkupContainer main_panel;
	
	
	public SearcherHomeSearchPanel(String id, IModel<Site> model) {
		super(id);
		setModel(model);
		setOutputMarkupId(true);

	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		main_panel = new WebMarkupContainer("searcher-container");
		add(main_panel);
		main_panel.setOutputMarkupId(true);
		
		PackageResourceReference res = null;
		res=ServiceLocator.getService(BrandingWebService.class).getSearchLibraryBckImage();
		String imagehref = RequestCycle.get().urlFor(res, null).toString();
		main_panel.add(new AttributeModifier("style", "background: url(" + imagehref + ") no-repeat 0 0 scroll; background-size: cover;"));
		//kbee.web.searcher.searchform.SearcherFormPanel<Site> panel = (SearcherFormPanel<Site>) 	new SearcherForm("main-searcher", getModel(), getModel().getObject().getTitle() );
		
	}
	
	
	
	
	IModel<Site> model;
	
	public IModel<Site> getModel() {
		return model;
	}
	
	public void setModel(IModel<Site> siteModel) {
		this.model = siteModel;
	}
	

	
	public void onDetach() {
		super.onDetach();
		this.model.detach();
	}
		
}


/**
 * 
 * try {
			java.util.Map<String, kbee.web.searcher.searchform.SearcherFormFactory> beans = ServiceLocator.getService(BeansService.class).getBeansOfType(kbee.web.searcher.searchform.SearcherFormFactory.class);
			SearcherFormFactory fac = beans.get(key_a);
			kbee.web.searcher.searchform.SearcherFormPanel<Site> panel = (fac!=null) ? fac.create() : new SearcherForm("main-searcher", getSiteModel(), getSiteModel().getObject().getTitle());
			((Panel) panel).setVisible(!this.isAdvancedSearcherVisible());	
			panel.setModel(getSiteModel());
			panel.setAdvancedSearchLinkVisible(key_b!=null && !(key_a.equals(key_b)));
			panel.setAdvancedSearchLinkLabel(new StringResourceModel("advanced",SearcherHomePage.this, null));
			main_panel.add((Panel) panel);
		} 
		catch (Exception e) {
			logger.error(e);
			main_panel.addOrReplace(new SearcherSimpleErrorPanel("main-searcher", e.getClass().getSimpleName(), e.getMessage()));
		}

 */














