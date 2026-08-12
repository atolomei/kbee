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
import com.novamens.wicket.util.DummyBlockPanel;

import io.odilon.log.Logger;
import kbee.util.PropertiesFactory;
import kbee.web.page.ApplicationMenuSection;

public class AAPasswordResetPage extends AbstractKbeeWebPage {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(AAPasswordResetPage.class.getName());

	private static final ResourceReference CSS_KBEE_BOOTSTRAP = new CssResourceReference(AbstractKbeeWebPage.class, "kbeebootstrap.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_0_360 = new CssResourceReference(AbstractKbeeWebPage.class, "kb-0-360.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_361_800 = new CssResourceReference(AbstractKbeeWebPage.class, "kb-361-800.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_801_1200 = new CssResourceReference(AbstractKbeeWebPage.class, "kb-801-1200.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1201_1600 = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1201-1600.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1601 = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1601.css");
	
	private static final ResourceReference CSS_AA_LOGIN = new CssResourceReference( AAPasswordResetPage.class, "aa.css");

	private static final ResourceReference CSS_BOOTSTRAP = new CssResourceReference(Form.class, com.novamens.wicket.markup.html.form.Form.BOOTSTRAP);

	private static final ResourceReference BOOTSTRAP_JS = new JavaScriptResourceReference(Form.class, Form.BOOTSTRAP_JS);
	private static final ResourceReference BOOTSTRAP_CSS = new CssResourceReference(Form.class, Form.BOOTSTRAP);

	static private final String XUA_Compatible = PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.portal.xuacompatible", "IE=Edge");

	private static final long serialVersionUID = 1L;
	private static final ResourceReference AW = new CssResourceReference(Form.class, Form.FONTAWESOME);
	
	private PageParameters parameters;

	public AAPasswordResetPage() {
		this(null);
	}

	public AAPasswordResetPage(PageParameters _parameters) {
		super();
		
		setFonts("https://fonts.googleapis.com/css2?family=Montserrat:ital,wght@0,100..900;1,100..900&family=Noto+Sans:ital,wght@0,100..900;1,100..900&display=swap");
		
		Locale locale = ((WebRequest) RequestCycle.get().getRequest()).getLocale();
		getSession().setLocale(locale);
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
		//area.add(new AAForgotPasswordPanel("mainPanel", this.parameters));
		area.add(new DummyBlockPanel("mainPanel"));
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
		return CSS_KBEE_BOOTSTRAP;
	}

	

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(CSS_BOOTSTRAP));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_0_360));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_361_800));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_801_1200));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1201_1600));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1601));
		response.render(CssHeaderItem.forReference(CSS_AA_LOGIN));
		response.render(CssHeaderItem.forReference(BOOTSTRAP_CSS));
		response.render(JavaScriptHeaderItem.forReference(BOOTSTRAP_JS));
		response.render(CssHeaderItem.forReference(AW));
	}
}
