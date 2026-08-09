package kbee.web.searcher.page;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.portal.service.PortalUrlService;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KeyValue;

public class LegalPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private IModel<Site> model;
	
	public LegalPanel(String id, IModel<Site> model) {
		super(id);
	
		this.model=model;
		
		ListView<KeyValue<String>> menu = new ListView<KeyValue<String>>("menu", getMenuOptions()) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<KeyValue<String>> item) {
				Link<Void> link = new Link<Void>("link") {
					private static final long serialVersionUID = 1L;
					@Override
					public void onClick() {
						setResponsePage(new RedirectPage(item.getModelObject().getValue()));
					}
				};

				item.add(link);
				Label label = new Label("label", item.getModelObject().getDisplayName());
				
				WebMarkupContainer sepa = new WebMarkupContainer("separator");
				sepa.setVisible(item.getIndex()>0);
				link.add(label);
				item.add(sepa);
			}
		};
		
		add(menu);
	}
	
	public void onDetach() {
		super.onDetach();
		
		if (this.model!=null)
			this.model.detach();
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	
	private String getServerUrl() {
		String protocol =((WebRequest)RequestCycle.get().getRequest()).getUrl().getProtocol();
		String host =((WebRequest)RequestCycle.get().getRequest()).getUrl().getHost();
		Integer iport =((WebRequest)RequestCycle.get().getRequest()).getUrl().getPort(); 
		String port = (iport.equals(80) || iport.equals(443) ? "":  ( ":" + iport.toString()) );
		return protocol +"://" + host + port;
	}

	
	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	List<KeyValue<String>> list = null;
	
	private List<KeyValue<String>> getMenuOptions() {
		
		if (list!=null)
			return list;
		
		if (this.model==null || this.model.getObject()==null) {
			list= new ArrayList<KeyValue<String>>();
			return list;
		}

		PortalUrlService service = ServiceLocator.getService(PortalUrlService.class);
		
		String context = getServerUrl();
		
		list= new ArrayList<KeyValue<String>>();
		
		
		if (this.model!=null)
			list.add(new KeyValue<String>("about", context + "/" +  service.getRelativeSiteUrl(this.model.getObject())+"/about"));
		
		if (this.model!=null)
			list.add(new KeyValue<String>("contact", context + "/" +  service.getRelativeSiteUrl(this.model.getObject())+"/contact"));
		
		//list = getDomain().getService(DomainSearcherPortalService.class).getFooterMenuOptions();
		//if (list==null)
		//	list= new ArrayList<Pair>();
		
		return list;
	}

	
	

	

}
