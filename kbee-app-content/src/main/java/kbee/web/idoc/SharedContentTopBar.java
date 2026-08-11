package kbee.web.idoc;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.web.security.login.LoginSimplePage;
import com.novamens.kbee.wicket.services.BrandingWebService;
import com.novamens.service.BrandingService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.panel.KBPanel;

import kbee.web.application.PageBeanResolver;
import kbee.web.nav.NavBarUserMenu;

@SuppressWarnings("serial")
public class SharedContentTopBar extends KBPanel {
	private static final long serialVersionUID = 1L;
	
	WebMarkupContainer navbar = new WebMarkupContainer("top-navbar");
	

	IModel<Content> model;
	
	
	protected WebPage getLoginPage() {
		return (new PageBeanResolver(
			"login-page", 
			LoginSimplePage.class)).getPage();
	}
	
	public class BrandFragment extends Fragment {
		
		private static final long serialVersionUID = 1L;

		public BrandFragment(String id) {
			super(id, "brand-fragment", SharedContentTopBar.this);
			
			this.setOutputMarkupId(true);
			
			Image img=null;
			img = new Image("brand",  ServiceLocator.getService(BrandingWebService.class).getSearchLibraryApplicationLogo()) {
					protected boolean shouldAddAntiCacheParameter()	{
						return false;
					}
			};

			Link<Void> rplink = new Link<Void>("icon-link") {
				@Override
					public void onClick() {
					 setResponsePage( new RedirectPage(   ServiceLocator.getService(BrandingService.class).getApplicationURL() ));
				}
			};
			
			add(rplink);
			rplink.add(new AttributeModifier("title", ServiceLocator.getService(BrandingService.class).getProductKey()));
			rplink.add(img);
			WebMarkupContainer dlink = new WebMarkupContainer("domain-link");
			dlink.add( new AttributeModifier("href", "/myhome"));
			add(dlink);
			
			
			Label product_name = new Label("product", ServiceLocator.getService(BrandingService.class).getProductKey());
			dlink.add(product_name);
			
			dlink.add(new AttributeModifier("title", ServiceLocator.getService(BrandingService.class).getProductKey()));
			Label domain_name = new Label("domain-name", SharedContentTopBar.this.getModel().getObject().getDomain().getOrganization());
			dlink.add(domain_name);
		}
	}


	
	
	public class LoginFragment extends Fragment {
		
		private static final long serialVersionUID = 1L;

		public LoginFragment(String id) {
			super(id, "login-fragment", SharedContentTopBar.this);
			
			this.setOutputMarkupId(true);
			
			Link<Void> link = new Link<Void>("login-link") {
				private static final long serialVersionUID = 1L;
					@Override
					public void onClick() {
						getLoginPage();
					}
			};
			add(link);
		}
	}
	
	

	
	/**
	 * 
	 * 
	 * 
	 * @param id
	 */
	public SharedContentTopBar(String id, IModel<Content> model) {
		super(id, model);
		this.model= model;

	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(navbar);
		navbar.add(new BrandFragment("brand"));
		
		navbar.add( new NavBarUserMenu("user"));
		//navbar.add(new LoginFragment("login"));
		
		
		
	}
	
	public IModel<Content> getModel() {
		return model;
	}

	
	
	public void onDetach() {
		super.onDetach();
		model.detach();
	}

}
