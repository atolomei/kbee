package kbee.web.workflow;


import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.form.EFormData;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.wicket.markup.html.form.PrintableBehavior;

import kbee.web.eform.EFormViewer;
import kbee.web.error.ErrorPanel;

/**
 * 
 * 
 * Audit
 * 
 *
 */
@SuppressWarnings("serial")
public class EFormViewerPanel extends ModelPanel<EFormData>  {
	private static final long serialVersionUID = 1L;

	private  boolean print_button = true;
	private  WebMarkupContainer e_container;
	
	public EFormViewerPanel(String id) {
		this(id, null);
	}
	
	public EFormViewerPanel(String id, IModel<EFormData> model) {
		super(id, model);

		setPrintButton(false);

		Link<Void> ln = new Link<Void>("print-page") {
			@Override
			public void onClick() {
			}
			@Override
			public boolean isVisible() {
				return isPrintButton();
			}
		};
		add(ln);
		
		add((new PrintableBehavior()).new PrintButton("print-button", "response") {
			@Override
			public boolean isVisible() {
				return isPrintButton();
			}
		});
		
		add(new PrintableBehavior());
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		e_container = new WebMarkupContainer("eform-container");  
		e_container.add(new AttributeModifier("class", "eform-audit"));
		add(e_container);
		
		if (getModel()!=null) {
			e_container.add(new EFormViewer("eform", getModel()));
		}
		else 
			e_container.addOrReplace(new ErrorPanel("eform", new Model<String>("model is null")));
	}
	
	public void setPrintButton(boolean b) {
		this.print_button=b;
	}
	
	public boolean isPrintButton() {
		return print_button;
	}
}