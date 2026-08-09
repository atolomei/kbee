package kbee.web.console.grid;


import com.novamens.event.Event;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.model.ModelPanel;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class KbeeTitleColumnPanel<T> extends ModelPanel<T> {

	private static final long serialVersionUID = 1L;
	private String title;
	String css;
	
	private String target  ="_blank";
	

	public KbeeTitleColumnPanel(String id, String title, IModel<T> model) {
		super(id, model);
		this.title = title;
	}

	
	public String getTarget() {
		return this.target;
	}
	
	public void setTarget(String t) {
		this.target=t;
	}
	
	
	protected Event getEvent(IModel<T> model) {
		return new ClickEvent<>(null, model, 0);
	}
	
	protected AbstractLink getLink() {
		Link<?> link = new Link<Void>("title-link") {
			private static final long serialVersionUID = 1L;
			public void onClick() {
				fireScanAll(getEvent(KbeeTitleColumnPanel.this.getModel()));
			}
		};
		return link;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();

		AbstractLink link = getLink();

		link.add(new Label("title", new Model<String>() {
			private static final long serialVersionUID = 1L;
			public String getObject() {
				return KbeeTitleColumnPanel.this.getTitle();
			};
		}));

		if (getCss()!=null) {
			((Label) link.get("title")).add(new AttributeModifier("class", getCss()));
		}
		
		if (getTarget()!=null)
			link.add(new AttributeModifier("target", getTarget()));
		
		add(link);

	}


	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}
}
