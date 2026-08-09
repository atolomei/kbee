package kbee.web.portal6.panel;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.portal6.model.PortalObject;

public class PortalDummyBlockPanel<T extends PortalObject> extends PortalPanel<T> {

	
	private static final long serialVersionUID = 1L;

	private String css;
	private IModel<String> title;
	private IModel<String> text;
	

	public PortalDummyBlockPanel(String id, IModel<T> model) {
		super(id, model);
		title=getDefaultTitle();
	}
	
	public PortalDummyBlockPanel(String id, IModel<T> model, String css) {
		super(id, model);
		this.title=getDefaultTitle();
		this.css=css;
	}
	
	public PortalDummyBlockPanel(String id, IModel<T> model, String css, IModel<String> title) {
		super(id, model);
		this.title=title;
		this.css=css;
	}

	public String getCss() {
		 return (css!=null?css:"") + getDefaultCss();
	
	}

	protected String getDefaultCss() {
		if (getModel()==null)
			return " portal-info";
		return " " + getModel().getObject().getClassKey()+"-info";
	}

	
	
	private IModel<String> getDefaultTitle() {
		if (getModel()!=null) {
			return new Model<String>(getModel().getObject().getDisplayName());
		}
		return null;
	}
	public void onInitialize() {
		super.onInitialize();
		setOutputMarkupId(true);
	
		WebMarkupContainer r=new WebMarkupContainer("container");
		
		
		r.add(new AttributeModifier("class", getCss()));
		
		Label la=new Label("title", (title!=null?title.getObject():""));
		la.setEscapeModelStrings(false);
		la.setVisible(title!=null);
		r.add(la);
		
		Label lar=new Label("text", (text!=null?text.getObject():""));
		lar.setVisible(text!=null);
		lar.setEscapeModelStrings(false);
		r.add(lar);
		
		add (r);
	}
	

}
