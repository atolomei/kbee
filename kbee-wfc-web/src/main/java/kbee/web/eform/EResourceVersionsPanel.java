package kbee.web.eform;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.wicket.model.ProxyModel;

import kbee.web.resource.ResourceLink;

@SuppressWarnings("serial")
public class EResourceVersionsPanel extends ModelPanel<Resource> {
	private static final long serialVersionUID = 1L;
	
	private boolean opened = false;

	public EResourceVersionsPanel(String id, IModel<Resource> model) {
		super(id, model);
	}
	
	public EResourceVersionsPanel(String id, IModel<Resource> model, boolean opened) {
		super(id, model);
		this.opened = opened;
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		add(new AjaxLink<Void>("close") {
			public void onClick(AjaxRequestTarget target) {
				EResourceVersionsPanel.this.onClose(target);
			}
		});

		add(new ListView<IModel<Resource>>("r-version", getVersions()) {
			protected void populateItem(ListItem<IModel<Resource>> item) {
				IModel<Resource> model = item.getModelObject();
				Resource resource = model.getObject();
				WebMarkupContainer link = new ResourceLink<Content>("link", model);
				link.add(new Label("name", resource.getDisplayName()));
				item.add(link);
				item.add(new Label("version", resource.getVersion()));
				item.add((new Label("date", resource.getLastModifiedOffsetDateTimeColloquial())).setEscapeModelStrings(false));
				item.add(new Label("user", resource.getLastModifiedUser().getDisplayName()));
				model.detach();
			}
		});
	}
	
	public Resource getResource() {
		return getModelObject();
	}
	
	public List<IModel<Resource>> getVersions() {
		List<IModel<Resource>> resources = new ArrayList<IModel<Resource>>();
		Resource version =  getResource();
		while (version.getPreviousVersion()!=null && resources.size()<10) {
			resources.add(new ProxyModel<Resource>(version.getPreviousVersion()));
			version = version.getPreviousVersion();
		}
		return resources;
	}
	
	
	public void open(AjaxRequestTarget target) {
		opened = true;
	}
	
	@Override
	public boolean isVisible() {
		return opened;
	}
	
	protected void onClose(AjaxRequestTarget target) {
		opened = false;
	}
}
