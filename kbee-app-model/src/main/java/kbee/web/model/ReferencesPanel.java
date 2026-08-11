package kbee.web.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;

import com.novamens.content.model.ModelElement;
import com.novamens.content.model.ModelReference;
import com.novamens.content.model.ModelService;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.wicket.markup.html.repeater.util.DataViewNavigationToolbar;

@SuppressWarnings("serial")
public class ReferencesPanel<T extends ModelElement> extends ModelPanel<T> {
				
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReferencesPanel.class.getName());
	
	private transient List<ModelReference> references;
	
	public ReferencesPanel(String id, IModel<T> model) {
		super(id, model);
		add(new ListView<String>("group", () -> getGroups()) {
			public void populateItem(ListItem<String> item) {
				
				WebMarkupContainer container = new WebMarkupContainer("container");
				container.setOutputMarkupId(true);
				
				container.add(new Label("name", item.getModelObject()));
				
				ListDataProvider<ModelReference> references = new ListDataProvider<ModelReference>() {
					public List<ModelReference> getData() {
						return getReferences(item.getModelObject());
					}
				};
				
				container.add(new DataView<ModelReference>("reference", references, 25) {
					public void populateItem(Item<ModelReference> item) {
						Link<?> link = new Link<Void>("link") {
							public void onClick() {
								setResponsePage(new RedirectPage(item.getModelObject().getUrl()));
							}
						};
						link.add(new Label("description", item.getModelObject().getObject()));
						item.add(link);
					}
				});
				
				((DataView<?>)container.get("reference")).setOutputMarkupId(true);
				container.add(new DataViewNavigationToolbar("pager", (DataView<?>)container.get("reference")) {
					public void onUpdate(AjaxRequestTarget target) {
						target.add(container);
					}
					@Override
					public boolean isVisible() {
						return getPageCount()>0;
					}
				});
				
				item.add(container);
			}
		});
	}
	
	private List<String> getGroups() {
		List<String> groups = new ArrayList<String>();

		for (ModelReference reference : getReferences()) {
			if (reference.getGroup()!=null && !groups.contains(reference.getGroup())) {
				groups.add(reference.getGroup());
			}
		}
		
		groups.sort( new Comparator<String> () {
			@Override
			public int compare(String a, String b) {
				try {
					return a.compareToIgnoreCase(b);
				} catch (Exception e) {
					logger.error(e);
				}
				return 0;
			}
			
		});
		
		return groups;
	}
	
	private List<ModelReference> getReferences(String group) {
		List<ModelReference> references = new ArrayList<ModelReference>();
		for (ModelReference reference : getReferences()) {
			if (reference.getGroup().equals(group)) {
				references.add(reference);
			}
		}
		
	references.sort( new Comparator<ModelReference> () {
		@Override
		public int compare(ModelReference a, ModelReference b) {
			try {
				return a.getObject().compareToIgnoreCase(b.getObject());
				
			} catch (Exception e) {
				logger.error(e);
			}
			return 0;
		}
		
	});
	
	
		return references;
	}
	
	private List<ModelReference> getReferences() {
		if (references == null) {
			references = getModelObject().getDomain().getService(ModelService.class).getReferences(getModelObject());
		}
		return references;
	}
}