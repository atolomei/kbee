package com.novamens.content.web.admin.markup;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public class ActionsPanel extends Panel {
	private static final long serialVersionUID = 1L;
	
	IModel<String> title;
	List<XLink> list = new ArrayList<XLink>();
	

	public ActionsPanel(String id) {
		super(id);
	}

	public ActionsPanel(String id, IModel<String> title) {
		super(id);
		setTitle(title);
	}

	public void add(XLink link) {
			list.add(link);
	}
	
	public List<XLink> getList() {
		return list;
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();

		if (get("title-container")==null) {
			WebMarkupContainer title_container = new WebMarkupContainer("title-container") {
							public boolean isVisible() {
								return getTitle()!=null;
							}
			};
			
			add(title_container);
			
			if (getTitle()!=null) {
				title_container.add(new Label("title", getTitle()));
			}
			else {
				title_container.add(new Label("title", ""));
			}
		}
		
		if (get("action")==null) {
			ListView<XLink> actions = new ListView<XLink>("action", getList()) {
				public void populateItem(ListItem<XLink> item) {
					XLink element = item.getModelObject();
					AbstractLink link = element.getLink("link");
					link.addOrReplace(new Label("label", element.getLabel()));
					if (element.isNewTab())
						link.add(new AttributeModifier("target", "_blank"));
					item.addOrReplace(link);
				}
			};
			add(actions);
		}

	}


	public IModel<String> getTitle() {
		return title;
	}
	

	public void setTitle(IModel<String> title) {
		this.title=title;
	}
}
