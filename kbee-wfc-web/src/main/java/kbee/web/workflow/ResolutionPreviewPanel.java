package kbee.web.workflow;


import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

// import com.novamens.content.web.workflow.markup.ResolutionPage;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.wicket.markup.html.form.PrintableBehavior;



@SuppressWarnings("serial")
public class ResolutionPreviewPanel extends ModelPanel<String> {
	private static final long serialVersionUID = 1L;

	public boolean print_button = true;
	
	public ResolutionPreviewPanel(String id) {
		this(id, (IModel<String>)null, true);
	}
	
	public ResolutionPreviewPanel(String id, String text) {
		this(id, new Model<String>(text), true);
	}
	
	public ResolutionPreviewPanel(String id, IModel<String> model, boolean ispb) {
		super(id, model);
		
		setPrintButton(ispb);
		
		Label textlabel = new Label("text", new Model<String>() {
			public String getObject() {
				return ResolutionPreviewPanel.this.getModelObject();
			}
		});
		
		textlabel.setEscapeModelStrings(false);
		
		add(textlabel);

		Link<Void> ln = new Link<Void>("print-page") {
			@Override
			public void onClick() {
				setResponsePage(new ResolutionPage(ResolutionPreviewPanel.this.getModelObject()));
				
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

	
	public void setPrintButton(boolean b) {
		this.print_button=b;
	}
	
	public boolean isPrintButton() {
		return print_button;
	}
	
	
	
	
	
}
