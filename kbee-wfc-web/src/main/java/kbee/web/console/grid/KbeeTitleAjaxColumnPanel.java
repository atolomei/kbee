package kbee.web.console.grid;


import com.novamens.kbee.wicket.model.ModelPanel;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import org.apache.wicket.model.Model;

public class KbeeTitleAjaxColumnPanel<T>  extends ModelPanel<T> {
				
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String title;
	String css;

	public KbeeTitleAjaxColumnPanel(String id, String title, IModel<T> model) {
		super(id, model);
		this.title = title;
	}
	
	protected AbstractLink getLink() {
		
		AjaxLink<Void> link = new AjaxLink<Void>("title-link") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				fireScanAll(new ClickEvent<>(target, KbeeTitleAjaxColumnPanel.this.getModel(), 0));
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
				return KbeeTitleAjaxColumnPanel.this.getTitle();
			};
		}));

		if (getCss()!=null) {
			((Label) link.get("title")).add(new AttributeModifier("class", getCss()));
		}
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
