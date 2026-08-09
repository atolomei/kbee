package kbee.web.page;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.wicket.markup.html.form.DateField;
import com.novamens.wicket.markup.html.form.Form;

import kbee.web.page.AbstractApplicationPage;

public class BootstrapApplicationPage<T> extends AbstractApplicationPage<T> {
	
	private static final long serialVersionUID = 1L;
	
	protected static final ResourceReference CSS = new CssResourceReference(Form.class, Form.BOOTSTRAP);
	protected static final ResourceReference AW = new CssResourceReference(Form.class, "font-awesome.css");
	protected static final ResourceReference BL = new CssResourceReference(Form.class, "build.css");
	protected static final ResourceReference JS = new JavaScriptResourceReference(DateField.class, Form.BOOTSTRAP_JS);
	protected static final ResourceReference BS = new CssResourceReference(Form.class, "bootstrap-select.css");
	protected static final ResourceReference BSJS = new JavaScriptResourceReference(Form.class, "bootstrap-select.js");
	
	public BootstrapApplicationPage(IModel<T> model, Panel navigation) {
		super(model, navigation);
	}
	
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(CssHeaderItem.forReference(CSS));
		response.render(CssHeaderItem.forReference(AW));
		response.render(CssHeaderItem.forReference(BL));
		response.render(JavaScriptHeaderItem.forReference(JS));
		response.render(CssHeaderItem.forReference(BS));
		response.render(JavaScriptHeaderItem.forReference(BSJS));
	}
}