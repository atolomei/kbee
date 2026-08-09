package kbee.web.console;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.model.ModelPanel;

@SuppressWarnings("serial")
public class NameColumnPanel<T> extends ModelPanel<T> {
	private static final long serialVersionUID = 1L;

	public NameColumnPanel(String id, IModel<T> model) {
		super(id, model);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		Label name = new Label("name", getStringResourceModelName());

		if (getCss()!=null) 
			name.add(new AttributeModifier("class", getCss()));

		name.add(new AjaxEventBehavior("click") {
			@Override
			protected void onEvent(AjaxRequestTarget target) {
				fireScanAll(new ClickEvent<T>(target, NameColumnPanel.this.getModel(), 0));
			}
		});
		
		add(name);
	}
	
	protected IModel<String> getStringResourceModelName() {
		 return new PropertyModel<String>(getModel(), getDisplayProperty());
	}

	protected String getDisplayProperty() {
		return "displayName";
	}
	
	protected String getCss() {
		return null;
	}
}
