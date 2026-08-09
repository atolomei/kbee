package kbee.web.dashboard;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.novamens.wicket.markup.html.panel.KBPanel;

public class DashboardSimpleInfoPanel extends KBPanel {


	private static final long serialVersionUID = 1L;

	
	IModel<String> label;
	String icon;

	public DashboardSimpleInfoPanel(String id, IModel<String> label, String css) {
		super(id);
		
		this.label=label;
		this.icon=css;
	}
	
	
	public void onInitialize() {
		super.onInitialize();
		
		Label la=new Label("label", label);
		la.setEscapeModelStrings(false);
		add(la);
		
		WebMarkupContainer c=new WebMarkupContainer("icon");
		add(c);
		c.setVisible((icon!=null && icon.length()>0));
		
		c.add( new AttributeModifier("class", icon));
		
		
		
		
	}

}
