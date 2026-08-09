package kbee.web.console.grid;


import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.security.Identifiable;

public class TargetBlankObjectTitleColumnPanel<T extends Identifiable> extends ModelPanel<T> {
	private static final long serialVersionUID = 1L;

	public TargetBlankObjectTitleColumnPanel(String id, IModel<T> model) {
		super(id, model);
		
			Link<?> link = new Link<Void>("title-link") {
				private static final long serialVersionUID = 1L;
				public void onClick() {
					TargetBlankObjectTitleColumnPanel.this.onClick();
				}
			};
		
			Label la=new Label("title", new Model<String>() { 
				private static final long serialVersionUID = 1L;
				public String getObject() { 
					return getTitle(); 
				};
			});
			
			la.setEscapeModelStrings(false);
			link.add(la);
			
			if (getCss()!=null) {
				((Label) link.get("title")).add(new AttributeModifier("class", getCss()));
			}
			add(link);
	}
	
	public String getTitle() {
		return getModelObject().getDisplayName();
	}
	
	protected void onClick() {
		fireScanAll(new ClickEvent<T>(null, getModel(), 0));
	}
	
	protected String getCss() {
		return null;
	}
}
