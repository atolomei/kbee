package kbee.web.console;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;

import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.kbee.wicket.util.InvisiblePanel;

import kbee.web.console.grid.LabelSetPanel;


public class TargetBlankTitleColumnPanel<T extends Content> extends ModelPanel<T> {
	
	private static final long serialVersionUID = 1L;
	static final String PROPERTY_UNREAD = "unread";

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TargetBlankTitleColumnPanel.class.getName());

	@SuppressWarnings("serial")
	public TargetBlankTitleColumnPanel(String id, IModel<T> model) {
		super(id, model);
				Link<?> link = new Link<Void>("title-link") {
					public void onClick() {
						fireScanAll(new ClickEvent<T>(null, TargetBlankTitleColumnPanel.this.getModel(), 0));
					}
				};
			
				link.add(new Label("title", new Model<String>() { 
					public String getObject() { 
						return getTitle(); 
					};
				}));
				
				link.setPopupSettings(new PopupSettings(PopupSettings.LOCATION_BAR | PopupSettings.MENU_BAR | 
					PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS | 
					PopupSettings.STATUS_BAR | PopupSettings.TOOL_BAR));
				
				if (getCss()!=null) {
					((Label) link.get("title")).add(new AttributeModifier("class", getCss()));
				}
			
				link.add(new WebMarkupContainer("lock-icon") { 
					public boolean isVisible() {
						return false;
					};
				});
				
				WebMarkupContainer newi = new WebMarkupContainer("new-icon") { 
					public boolean isVisible() {
						return false;
					};
				};
				link.add(newi);
				add(link);
				
			try {
				add(new LabelSetPanel<T>("labels", getModel(), false, true, false));
				
			} catch (Exception e) {
				logger.error(e);
				addOrReplace(new InvisiblePanel("labels"));
			}
	}
	
	public String getTitle() {
		try {
			return getModelObject().getTitle();
		} catch (Exception e) {
			logger.error(e);
			return "err";
		}
	}
	
	protected String getCss() {
		return null;
	}
	
	
}
