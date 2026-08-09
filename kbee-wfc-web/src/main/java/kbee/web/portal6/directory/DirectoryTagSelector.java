package kbee.web.portal6.directory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.wicket.markup.html.actions.AbstractLinkMenuItemPanelV5;

import kbee.web.portal6.panel.PortalPanel;

public class DirectoryTagSelector extends PortalPanel {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	static final private org.apache.logging.log4j.Logger logger = LogManager.getLogger(DirectoryTagSelector.class.getName());

	List<String> list;
	Map<String, Boolean> selected = new HashMap<String, Boolean>();

	public DirectoryTagSelector(String id) {
		super(id);
		list = new ArrayList<String>();
		list.add("sites");
		list.add("utilities");
		list.add("favorites");
		list.add("all");
		
		setOutputMarkupId(true);
	}

	 
	public void onInitialize() {
		super.onInitialize();

		String se=getUserPreference("site-favorites", "favorites");
		for (String s : list)
			selected.put(s, s.contentEquals(se) ? Boolean.valueOf(true) : Boolean.valueOf(false));
		
		com.novamens.wicket.model.ListModel<String> lm = new com.novamens.wicket.model.ListModel<String>(new Model<Panel>(this), "sites");

		ListView<String> tags = new ListView<String>("tags", lm) {
			@Override
			protected void populateItem(ListItem<String> item) {

				AjaxLink<String> link = new AjaxLink<String>("tag", item.getModel()) {
					@Override
					public void onClick(AjaxRequestTarget target) {
						String tag = (String) getModel().getObject();
						for (String s : list)
							selected.put(s, tag.equals(s) ? true : false);
						setUserPreference("site-favorites", tag);
						fire(new TagSelectionEvent(target, tag));
						target.add(DirectoryTagSelector.this);
					}
					
					@Override
					protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
						super.updateAjaxAttributes(attributes);
						attributes.setEventPropagation(EventPropagation.STOP); 
					}
				};

				final String s = item.getModel().getObject();

				link.add(new AttributeModifier("class", new Model<String>(s) {
					@Override
					public String getObject() {
						return isSelected(s) ? "tag tagselected " : "tag tagnot-selected";
					}
				}));

				WebMarkupContainer icon = new WebMarkupContainer("icon") {
					public boolean isVisible() {
						return isSelected(s);
					}
				};

				icon.add(new AttributeModifier("class", new Model<String>() {
					@Override
					public String getObject() {
						return isSelected(s) ?  AbstractLinkMenuItemPanelV5.CHECK : "";
					}
				}));
				link.add(icon);

				Label lab = new Label("label",
						new StringResourceModel(item.getModel().getObject(), DirectoryTagSelector.this, null));
				link.add(lab);
				item.add(link);
			}
		};
		add(tags);
	}

	private boolean isSelected(String object) {
		return selected.get(object).booleanValue();
	}

	public List<String> getSites() {
		return list;
	}

	

}
