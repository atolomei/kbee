package kbee.web.workflow;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.event.LogEvent;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.logging.TaskEndEvent;
import com.novamens.wicket.markup.html.form.Form;

import kbee.web.page.KbeeWebPage;

@SuppressWarnings("serial")
public class ResolutionPage extends KbeeWebPage<LogEvent> {
	private static final long serialVersionUID = 1L;
	

	private static final ResourceReference ICONS_CSS = new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/css/icons/icomoon/styles.css");
	
	private static final ResourceReference COMPONENTS_CSS = new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/css/components.css");
	
	private static final ResourceReference CORE_CSS = new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/css/core.css");
	private static final ResourceReference APP_JS = new JavaScriptResourceReference(AbstractKbeeWebPage.class, "assets/js/core/app.js");
	
	private static final ResourceReference BOOTSTRAP_JS = new JavaScriptResourceReference(Form.class, Form.BOOTSTRAP_JS);
	private static final ResourceReference BOOTSTRAP_CSS = new CssResourceReference(Form.class, Form.BOOTSTRAP);
	
	private static final ResourceReference KBEE_BOOTSTRAP_CSS = new CssResourceReference(AbstractKbeeWebPage.class, "kbeebootstrap.css");
	
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_0_360      = new CssResourceReference(AbstractKbeeWebPage.class, "kb-0-360.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_361_800    = new CssResourceReference(AbstractKbeeWebPage.class, "kb-361-800.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_801_1200   = new CssResourceReference(AbstractKbeeWebPage.class, "kb-801-1200.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1201_1600  = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1201-1600.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1601  	 = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1601.css");

	private static final ResourceReference AW = new CssResourceReference(Form.class, Form.FONTAWESOME);
	private static final ResourceReference CSS_KBEE_LIMITLESS = new CssResourceReference(AbstractKbeeWebPage.class, "kbee-limitless.css");

	
	public ResolutionPage(IModel<LogEvent> model) {
		super(model);
		
		add(new ResolutionPreviewPanel("resolution", new Model<String>(getResolution()), false) {
			@Override
			public boolean isVisible() {
				return getResolution()!=null;
			}
		});
	}

	
	
	public ResolutionPage(final String text) {
		super(null);
		
		add(new ResolutionPreviewPanel("resolution", new Model<String>(text), false) {
			@Override
			public boolean isVisible() {
				return text!=null;
			}
		});
	}

	
	 
	
	
	public String getResolution() {
		return getEvent()!=null ? getEvent().getResolution() : null; 
	}
	
	
	public TaskEndEvent getEvent() {
		return getModelObject() instanceof TaskEndEvent ? (TaskEndEvent)getModelObject() : null;
	}
	
	
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings()
				.getJQueryReference()));  

		
		response.render(CssHeaderItem.forReference(ICONS_CSS));
		
		response.render(CssHeaderItem.forReference(COMPONENTS_CSS));
		response.render(JavaScriptHeaderItem.forReference(APP_JS));
		response.render(CssHeaderItem.forReference(BOOTSTRAP_CSS));
		response.render(JavaScriptHeaderItem.forReference(BOOTSTRAP_JS));
		response.render(CssHeaderItem.forReference(CORE_CSS));
		
		

		
		response.render(CssHeaderItem.forReference(AW));
		
		response.render(CssHeaderItem.forReference(KBEE_BOOTSTRAP_CSS));
		response.render(CssHeaderItem.forReference(CSS_KBEE_LIMITLESS));
		
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_0_360));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_361_800));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_801_1200));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1201_1600));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1601));
	}
}

