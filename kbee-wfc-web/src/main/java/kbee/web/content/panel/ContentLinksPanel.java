package kbee.web.content.panel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentLink;
import com.novamens.content.service.ContentService;
import com.novamens.content.text.TextPart;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.content.text.KbeeText;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

				
@SuppressWarnings("serial")
public class ContentLinksPanel<T extends Content> extends ModelPanel<T> {
	private static final long serialVersionUID = 1L;
	
	public class LinksProvider extends SortableDataProvider<ContentLink, String> {
		public Iterator<ContentLink> iterator(long first, long count) {
			ArrayList<ContentLink> iteration = new ArrayList<>();
			Iterator<ContentLink> iterator = getLinks().listIterator((int)first);
			int i = 0;
			while (i++<count) {
				iteration.add(iterator.next());
			}
			return iteration.iterator();
		}	
		public IModel<ContentLink> model(ContentLink object) {
			return new ObjectModel<ContentLink>(object);
		}
		public long size() {
			return getLinks().size();
		}
		public List<ContentLink> getLinks() {
			return getModelObject().getReverseLinks();
		}
	}
	
	public class InboundsProvider extends LinksProvider {
		public List<ContentLink> getLinks() {
			List<ContentLink> links = new ArrayList<>();
			for (ContentLink link : getModelObject().getReverseLinks()) {
				if (link.getSource().getWorkspace()!=null) {
					links.add(link);
				}
			}
			if (links.isEmpty())
			for (ContentLink link : getModelObject().getReverseLinks()) {
				if (link.getSource().isHeadVersion() || link.getSource().getWorkspace()!=null) {
					links.add(link);
				}
			}
			return links;
		}
	}
	
	public class OutboundsProvider extends LinksProvider {
		public List<ContentLink> getLinks() {
			List<ContentLink> links =new ArrayList<>();
			for (ContentLink link : getModelObject().getLinks()) {
				if (link.getTarget().isHeadVersion()) {
					links.add(link);
				}
			};
			return links;
		}
	}
	
	public abstract class LinkFragment extends Fragment {
		private IModel<ContentLink> model;
		public LinkFragment(String id, IModel<ContentLink> model) {
			super(id, "link-fragment", ContentLinksPanel.this);
			this.model = model;
			WebMarkupContainer link = new WebMarkupContainer("link");
			link.add(new AttributeModifier("href", () -> getUrl()));
			add(link);
			link.add(new Label("title", () -> getTitle()));
		};
		public abstract String getUrl();
		public abstract String getTitle();
		public ContentLink getLink() {
			return model.getObject();
		}
	};
	
	public class SourceLinkFragment extends LinkFragment {
		public SourceLinkFragment(String id, IModel<ContentLink> model) {
			super(id, model);
		}
		public String getUrl() {
			Content content = getLink().getSource();
			return "/id/"+content.getOId();
		}
		public String getTitle() {
			Content content = getLink().getSource();
			String title = content.getTitle();
			return title;
		}
	}
	
	public class TargetLinkFragment extends LinkFragment {
		public TargetLinkFragment(String id, IModel<ContentLink> model) {
			super(id, model);
		}
		public String getUrl() {
			Content content = getLink().getTarget();
			return "/id/"+content.getOId();
		}
		public String getTitle() {
			Content content = getLink().getTarget();
			return content.getTitle();
		}
	}
	
	public class TargetPartLinkFragment extends LinkFragment {
		public TargetPartLinkFragment(String id, IModel<ContentLink> model) {
			super(id, model);
		}
		public String getUrl() {
			Content content = getLink().getTarget();
			return "/id/"+content.getOId() + "#" +  getLink().getAnchor();
		}
		public String getTitle() {
			Content content = getLink().getTarget();
			KbeeText text = (KbeeText)content.getService(ContentService.class).getText();
			TextPart part = text.getPart(getLink().getAnchor());
			String label = part!=null ? part.getTitle() : "";
			return label;
		}
	}
	

	public ContentLinksPanel(String id, IModel<T> model) {
		super(id, model);
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		addInboundsTable();
		addOutboundsTable();
	}
	
	public void addInboundsTable() {
		DataTable<ContentLink, String> table = new DataTable<ContentLink, String>("inbounds", getInboundsColumns(), new InboundsProvider(), 40);
		table.addTopToolbar(new AjaxFallbackHeadersToolbar<String>(table, (InboundsProvider)table.getDataProvider()));
		WebMarkupContainer tablecontainer = new WebMarkupContainer("inbounds-container");
		tablecontainer.add(table);
		add(tablecontainer);
	}
	
	public void addOutboundsTable() {
		DataTable<ContentLink, String> table = new DataTable<ContentLink, String>("outbounds", getOutboundsColumns(), new OutboundsProvider(), 40);
		table.addTopToolbar(new AjaxFallbackHeadersToolbar<String>(table, (OutboundsProvider)table.getDataProvider()));
		WebMarkupContainer tablecontainer = new WebMarkupContainer("outbounds-container");
		tablecontainer.add(table);
		add(tablecontainer);
	}
	
	public List<IColumn<ContentLink, String>> getInboundsColumns() {
		List<IColumn<ContentLink, String>> columns = new ArrayList<>();
				
		columns.add(new PropertyColumn<ContentLink, String>(getLabel("column.source"), "source.title") {
			@Override
			public void populateItem(Item<ICellPopulator<ContentLink>> item, String componentId, IModel<ContentLink> rowModel) {
				item.add(new SourceLinkFragment(componentId, rowModel));
			}
			@Override
			public String getCssClass()	{
				return "col-lg-7 col-md-7 col-xs-7";
			}
		});
		
		columns.add(new PropertyColumn<ContentLink, String>(getLabel("column.part"), "part") { 
			@Override
			public void populateItem(Item<ICellPopulator<ContentLink>> item, String componentId, IModel<ContentLink> rowModel) {
				item.add(new TargetPartLinkFragment(componentId, rowModel));
			}
			@Override
			public String getCssClass()	{
				return "col-lg-4 col-md-4 col-xs-4 text-center";
			}
		});

		
		return columns;
	}
	
	public List<IColumn<ContentLink, String>> getOutboundsColumns() {
		List<IColumn<ContentLink, String>> columns = new ArrayList<>();
				
		columns.add(new PropertyColumn<ContentLink, String>(getLabel("column.target"), "target.title") {
			public void populateItem(Item<ICellPopulator<ContentLink>> item, String componentId, IModel<ContentLink> rowModel) {
				item.add(new TargetLinkFragment(componentId, rowModel));
			}
			@Override
			public String getCssClass()	{
				return "col-lg-6 col-md-6 col-xs-6";
			}
		});
		
		columns.add(new PropertyColumn<ContentLink, String>(getLabel("column.part"), "part") { 
			@Override
			public void populateItem(Item<ICellPopulator<ContentLink>> item, String componentId, IModel<ContentLink> rowModel) {
				item.add(new TargetPartLinkFragment(componentId, rowModel));
			}
			@Override
			public String getCssClass()	{
				return "col-lg-4 col-md-4 col-xs-4 text-center";
			}
		});
		
		columns.add(new PropertyColumn<ContentLink, String>(getLabel("column.updated"), "updated") { 
			@Override
			public void populateItem(Item<ICellPopulator<ContentLink>> item, String componentId, IModel<ContentLink> rowModel) {
				String label="";
				if (rowModel.getObject().isTargetUpdated()) {
					Content target = rowModel.getObject().getTarget();
					String date = ServiceLocator.getService(DateTimeService.class).format(target.getLastModifiedOffsetDateTime());
					label = "Parte modificada el " + date + " por ";
					label += target.getLastModifiedUser().getDisplayName(); 
				}
				item.add(new Label(componentId, label));
			}
			@Override 
			public String getCssClass()	{
				return "col-lg-2 col-md-2 col-xs-2 text-center";
			}
		});
		
		return columns;
	}	
}
 