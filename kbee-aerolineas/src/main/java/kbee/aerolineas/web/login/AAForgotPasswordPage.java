package kbee.aerolineas.web.login;

import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.service.BrandingService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Form;

import io.odilon.log.Logger;
import kbee.util.PropertiesFactory;
import kbee.web.page.ApplicationMenuSection;

public class AAForgotPasswordPage extends AbstractKbeeWebPage {

	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(AAForgotPasswordPage.class.getName());



	private static final ResourceReference CSS_AA_LOGIN = new CssResourceReference( AAForgotPasswordPage.class, "aa.css");

	
	
	private static final ResourceReference CSS_BOOTSTRAP = new CssResourceReference(Form.class, com.novamens.wicket.markup.html.form.Form.BOOTSTRAP);

	private static final ResourceReference BOOTSTRAP_JS = new JavaScriptResourceReference(Form.class, Form.BOOTSTRAP_JS);
	private static final ResourceReference BOOTSTRAP_CSS = new CssResourceReference(Form.class, Form.BOOTSTRAP);

	static private final String XUA_Compatible = PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.portal.xuacompatible", "IE=Edge");

	private static final long serialVersionUID = 1L;

	private PageParameters parameters;

	public AAForgotPasswordPage() {
		this(null);
	}

	private Locale locale;

	public AAForgotPasswordPage(PageParameters _parameters) {
		super();
		
		locale = ((WebRequest)RequestCycle.get().getRequest()).getLocale();
		getSession().setLocale(locale);
		
 	
		
		setFonts("https://fonts.googleapis.com/css2?family=Montserrat:ital,wght@0,100..900;1,100..900&family=Noto+Sans:ital,wght@0,100..900;1,100..900&display=swap");
		
	 
		this.parameters = _parameters;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		setPageFonts(getFonts());
		setPageXUACompatible(XUA_Compatible);
		setPageTitle(new Model<String>(ServiceLocator.getService(BrandingService.class).getProductTabTitle()));
		WebMarkupContainer area = new WebMarkupContainer("main-area-container");
		
		//area.add(new AttributeModifier("class", "all " + ServiceLocator.getService(BrandingService.class).getLoginCss()));
		add(area);
		area.add(new AAForgotPasswordPanel("mainPanel", this.parameters));

	}

	@Override
	public String getPageHelpKey() {
		return getApplicationMenuSection().getKey() + "-" + this.getClass().getSimpleName().toLowerCase();
	}

	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.GENERAL;
	}

	/**
	 * Esto es para que no se mande kbee2.css
	 * que es obsoleto pero no puede sacarse
	 * por ahora.
	 */
	@Override
	protected ResourceReference getCssResource() {
		return CSS_AA_LOGIN;
	}

	private static final ResourceReference AW = new CssResourceReference(Form.class, Form.FONTAWESOME);

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(CSS_BOOTSTRAP));
	 	response.render(CssHeaderItem.forReference(BOOTSTRAP_CSS));
		response.render(JavaScriptHeaderItem.forReference(BOOTSTRAP_JS));
		response.render(CssHeaderItem.forReference(AW));
	}
}
