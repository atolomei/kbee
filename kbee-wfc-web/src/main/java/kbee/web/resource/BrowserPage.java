package kbee.web.resource;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.MetaDataHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.PriorityHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.StringValue;

import com.novamens.content.base.Content;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.content.document.KbeeIDoc;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.query.LibraryQuery;
import kbee.web.searcher.page.AbstractSearcherPage;
import kbee.web.searcher.page.SearcherMinimalTopToolbar;
import kbee.web.searcher.panel.SearcherResultsBrowser;

 
public class BrowserPage extends AbstractSearcherPage<Void>  {
	private static final long serialVersionUID = 1L;

	private boolean image;
	
	@Override
	protected boolean isExplorerOn() {
		return false;
	}

	@Override
	protected boolean isEditableOn() {
		return false;
	}
	
	
	public BrowserPage(PageParameters parameters) {
		
		StringValue type = parameters.get("type");
		image = type!=null && "image".equals(type.toString());
		
		if (image) {
			Content text = getContent(parameters); 
			add(new BrowserPanel<Content>("browser", type!=null ? type.toString() : null, new ObjectModel<Content>(text)));
		}
		else {
			Site site = getPortalDao().findSiteByURI("all", getDomain());
			IModel<Site> model = new ObjectModel<Site>(site);
			setSiteModel(model);
			add(new SearcherResultsBrowser("browser", new LibraryQuery(getIndex()), model) {
				@Override
				public String getConsoleKey()	 		{
					return "linksconsole";
				}
			});
		}
	}
	
	public BrowserPage(IModel<Site> site_model, Query query) {
		//this.query=query;
		setOutputMarkupId(true);
		setSiteModel(site_model);
		getPageParameters().set("siteurl", getSiteModel().getObject().getUrl().toString());
		add(new SearcherResultsBrowser("browser", query, site_model) {
			@Override
			public String getConsoleKey()	 		{
				return "linksconsole";
			}
		});
	}
	
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
			MetaDataHeaderItem headerItem = new MetaDataHeaderItem("meta");
			headerItem.addTagAttribute("http-equiv", "upgrade-insecure-requests");
			response.render(new PriorityHeaderItem(headerItem));
	
	}
	
	@Override
	protected void setHeaders(WebResponse response) {
		super.setHeaders(response);
		response.setHeader("X-Frame-Options", "SAMEORIGIN");
		response.setHeader("Content-Type", "text/html");

	}
	
	@Override
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<ClickEvent<?>>() {
			@Override
			public void onEvent(ClickEvent<?> event) {
				Object object = event.getModel().getObject();
				if (object instanceof Content) {
					IModel<Content> model = new ObjectModel<Content>((Content)object);
					setResponsePage(new PartSelectionPage(model));
				}
			}
		});
	}
	
	@Override
	protected Panel getToolbar() {
		Panel toolbar = null;
		try {
			if (image) {
				toolbar = new InvisiblePanel("navigation");
			}
			else {
			SearcherMinimalTopToolbar<?> s=new SearcherMinimalTopToolbar<Void>( "navigation", getSiteModel());
			s.setName(getName());
			//s.setSearchForm(isSearchForm());
			//s.setHome(isHome());
			//s.setInstitutional(isInstitutional());
			toolbar = s;
			}
		} 
		catch (Exception e) {
			toolbar = new InvisiblePanel("navigation");
 		}
		return toolbar;
	}
	
	private Content getContent(PageParameters parameters) {
		try {
			StringValue id = parameters.get("content");
			Content text = (Content)getContentDao().findContentById(KbeeIDoc.class, id.toLong());
			return text;
		}
		catch (Exception e) {
			return null;
		}
	}
}