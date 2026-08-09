package kbee.web.console;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;

import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.model.ModelPanel;

import kbee.web.console.grid.LabelSetPanel;
import kbee.web.error.ErrorPanel;

@SuppressWarnings("serial")
public class TitleColumnPanel<T extends Content> extends ModelPanel<T> {
	private static final long serialVersionUID = 1L;
	
	static final String PROPERTY_UNREAD = "unread";

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(TitleColumnPanel.class.getName());

	public TitleColumnPanel(String id, IModel<T> model) {
		super(id, model);
		Link<?> link = getNewLink("title-link");
			
		link.add(new Label("title", new Model<String>() { 
			public String getObject() { 
				return getTitle(); 
			};
		}));
				
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
			boolean isLabels = true;
			add(new LabelSetPanel<T>("labels", getModel(), false, isLabels, false));
		} 
		catch (Exception e) {
			logger.error(e);
			addOrReplace(new ErrorPanel("labels", e));
		}
	}
	
	public String getTitle() {
		try {
			return getModelObject().getTitle();
		} 
		catch (Exception e) {
			logger.error(e);
			return "err";
		}
	}
	
	protected Link<?> getNewLink(String id) {
		Link<?> link = new Link<Void>("title-link") {
			public void onClick() {
				fireScanAll(new ClickEvent<T>(null, TitleColumnPanel.this.getModel(), 0));
			}
		};
		return link;
	}
	
	protected String getCss() {
		return null;
	}
}