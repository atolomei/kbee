package kbee.web.security;

import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.service.BrandingService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Form;
import kbee.util.PropertiesFactory;
import kbee.web.branding.LoginImageService;
import kbee.web.branding.LoginImageWrapper;
import kbee.web.page.ApplicationMenuSection;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

public class LoginDomainSelectionPage extends AbstractKbeeWebPage {

	private static final ResourceReference CSS_KBEE_BOOTSTRAP			 = new CssResourceReference(AbstractKbeeWebPage.class, "kbeebootstrap.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_0_360      = new CssResourceReference(AbstractKbeeWebPage.class, "kb-0-360.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_361_800    = new CssResourceReference(AbstractKbeeWebPage.class, "kb-361-800.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_801_1200   = new CssResourceReference(AbstractKbeeWebPage.class, "kb-801-1200.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1201_1600  = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1201-1600.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1601  	 = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1601.css");

	private static final ResourceReference CSS_BOOTSTRAP = new CssResourceReference(Form.class, Form.BOOTSTRAP);

	static private final String XUA_Compatible = PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.portal.xuacompatible", "IE=Edge");

	private static final long serialVersionUID = 1L;


	public LoginDomainSelectionPage() {
		 this(null);
	}


	public LoginDomainSelectionPage(PageParameters parameters) {

			setPageFonts(getFonts());
		 	setPageXUACompatible(XUA_Compatible);
			setPageTitle(new Model<String>(ServiceLocator.getService(BrandingService.class).getProductTabTitle()));
			WebMarkupContainer area = new WebMarkupContainer("main-area-container");

			area.add(new AttributeModifier("class", "all " + ServiceLocator.getService(BrandingService.class).getLoginCss()));
			add(area);

			area.add(new LoginDomainSelectionPanelV6("login", parameters));

			// ------------------
			// Background
			//
			WebMarkupContainer bck = new WebMarkupContainer("background");
			LoginImageService service = ServiceLocator.getService(LoginImageService.class);
			LoginImageWrapper iw = service.getTodayLoginImageWrapper();
			String imagehref = RequestCycle.get().urlFor(iw.getResource(), null).toString();
			bck.add(new AttributeModifier("style", "background: url(" + imagehref + ") no-repeat 0 0 scroll; background-size: cover;"));
			area.add(bck);
    }
	
	@Override
	public String getPageHelpKey() {
		return  getApplicationMenuSection().getKey() + "-" + this.getClass().getSimpleName().toLowerCase();
	}

	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.GENERAL;
	}

	
	/**
	 * Esto es para que no se mande kbee2.css que es obsoleto pero no puede sacarse por ahora.
	 */
	@Override
	protected ResourceReference getCssResource()			{
		return CSS_KBEE_BOOTSTRAP;
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(CSS_BOOTSTRAP));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_0_360));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_361_800 ));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_801_1200));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1201_1600));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1601));
	}
}
