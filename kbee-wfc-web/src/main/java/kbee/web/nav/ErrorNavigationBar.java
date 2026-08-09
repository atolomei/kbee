package kbee.web.nav;


import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.request.resource.PackageResourceReference;

import com.novamens.kbee.wicket.editor.Editor;
import com.novamens.service.BrandingService;
import com.novamens.service.ServiceLocator;

import kbee.web.service.ApplicationSiteMapService;

/**
 * Bootstrap based. Navigation Bar
 *
 * @param <T>
 */
@SuppressWarnings("serial")
public class ErrorNavigationBar<T> extends NavigationPanel<T>  {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ErrorNavigationBar.class.getName());
	
	
	private static final long serialVersionUID = 1L;
	private Boolean is_domain_kbee = null;
	
	public ErrorNavigationBar(String id) {
		super(id);
		// add(newCloseLink());
		
		add(newIDocBrandPanel());  				// KBEE
		add(newMobileIDocBrandPanel());  		// KBEE
		add(newFactoryBrandPanel()); 	// kbee/iDOC: Factory		

	}
	
	
	
	public  void navigate() {
		
	};
	
	public void onNavigate(T object) {
		
	}
	
	public void onStartWorkflow() {
		
	}
	
	public void setEditor(Editor<?> editor) {
	}
	
	public boolean isFromContentBase() {
		return false;
	}
	
	/**
	protected Component newCloseLink()  {
		Link<?> link = new Link<Void>("close-link")	{
			public void onClick() {
			}
		};
		link.add(new AttributeModifier("onclick", "if (window.opener.refresh) { window.opener.refresh(); } window.close();"));
		return link;
	}
	**/
	
	/**
	 * 
	 * 
	 */
	public class BrandFactoryFragment extends Fragment {
		public BrandFactoryFragment(String id) {
			super(id, "brand-factory-fragment", ErrorNavigationBar.this);
			add(new Label("product", ServiceLocator.getService(BrandingService.class).getApplicationShortName()));
		}
	}
	
	public class BrandIDocFragment extends Fragment {
		public BrandIDocFragment(String id) {
			super(id, "brand-idoc-fragment", ErrorNavigationBar.this);
			
			this.setOutputMarkupId(true);
			
			Image img=null;
			img = new Image("brand",  getApplicationIcon()) {
					protected boolean shouldAddAntiCacheParameter()	{
						return false;
					}
			};

			Link<Void> rplink = new Link<Void>("rp-link") {
					@Override
					public void onClick() {
						setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage(ApplicationSiteMapService.HomePage));
					}
			};
			rplink.add(new AttributeModifier("class", "brand visible-lg visible-md  visible-sm  hidden-xs  " + ServiceLocator.getService(BrandingService.class).getProductKey()));
			add(rplink);
			img.add(new AttributeModifier("class", ServiceLocator.getService(BrandingService.class).getApplicationIconCss()));
			rplink.add(img);

			
			
			Link<Void> dlink = new Link<Void>("domain-link") {
				@Override
				public void onClick() {
					setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage(ApplicationSiteMapService.HomePage));
				}
			};
				
			add(dlink);
			Label product_name = new Label("product", ServiceLocator.getService(BrandingService.class).getApplicationName());	
			dlink.add(product_name);
			Label domain_name;
			
			if (getDomain()!=null) {
					domain_name = new Label("domain-name", getDomain().getOrganization());	

			}
			else {
				domain_name = new Label("domain-name", "");	
			}
			dlink.add(domain_name);
			dlink.add( new AttributeModifier("class", "brand visible-lg visible-md  visible-sm  hidden-xs brand-" + ServiceLocator.getService(BrandingService.class).getProductKey()));
			
		}
	}

				
	
	public class MobileBrandIDocFragment extends Fragment {
		public MobileBrandIDocFragment(String id) {
			super(id, "mobile-brand-idoc-fragment", ErrorNavigationBar.this);
			
			this.setOutputMarkupId(true);
			
			Image img=null;
			img = new Image("brand",  getApplicationIcon()) {
					protected boolean shouldAddAntiCacheParameter()	{
						return false;
					}
			};

			Link<Void> rplink = new Link<Void>("rp-link") {
					@Override
					public void onClick() {
						// setResponsePage(new RedirectPage(ServiceLocator.getService(BrandingService.class).getApplicationURL()));
						setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage(ApplicationSiteMapService.HomePage));
					}
			};
			rplink.add(new AttributeModifier("class", "brand hidden-lg hidden-md  hidden-sm  visible-xs brand-" + ServiceLocator.getService(BrandingService.class).getProductKey()));
			add(rplink);
			img.add(new AttributeModifier("class", ServiceLocator.getService(BrandingService.class).getApplicationIconCss()));
			rplink.add(img);

			
			Link<Void> dlink = new Link<Void>("domain-link") {
				@Override
				public void onClick() {
					setResponsePage( ServiceLocator.getService(ApplicationSiteMapService.class).getPage(ApplicationSiteMapService.HomePage));
					//if (getDomain().getWebsite()!=null && getDomain().getWebsite().length()>1)
					//	setResponsePage(new RedirectPage(getDomain().getWebsite()));
				}
			};
					
			add(dlink);
			
			Label product_name = new Label("product", ServiceLocator.getService(BrandingService.class).getApplicationName());	
			dlink.add(product_name);
			
			Label domain_name;
			
			if (getDomain()!=null) {
				domain_name = new Label("domain-name", getDomain().getOrganization());
			}
			else
				domain_name = new Label("domain-name", "");
			
			dlink.add(domain_name);
			dlink.add( new AttributeModifier("class", "brand hidden-lg hidden-md  hidden-sm  hidden-xs brand-" + ServiceLocator.getService(BrandingService.class).getProductKey()));
		}
	}


	protected Component newIDocBrandPanel()  {
		Component co = new BrandIDocFragment("brand-idoc");
		co.setVisible(!isDomainKbee());
		return co;
	} 
	
 	

 	protected Component newMobileIDocBrandPanel()  {
		Component co = new MobileBrandIDocFragment("mobile-brand-idoc");
		co.setVisible(!isDomainKbee());
		return co;
	} 
 	

	protected Component newFactoryBrandPanel()  {
		Component co = new BrandFactoryFragment("brand-factory");
		co.setVisible(isDomainKbee());
		return co;
	}

	
	private PackageResourceReference getApplicationIcon() {
		return ServiceLocator.getService(com.novamens.kbee.wicket.services.BrandingWebService.class).getApplicationIcon();
	}
	private boolean isDomainKbee() {
		if (this.is_domain_kbee == null) {
			try {						
				this.is_domain_kbee = Boolean.valueOf(getPerson().getDomain().getName().toLowerCase().trim().equals("kbee"));
			} 
			catch (Exception e) {
				//logger.error(e);
				this.is_domain_kbee = Boolean.valueOf(false);
			}
		}
		return this.is_domain_kbee.booleanValue();
	}
	
 }

