package kbee.web.console;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.browser.HitExpandedPanel;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;

@SuppressWarnings("serial")
@Deprecated
public class ExpandedPanel<T> extends Panel implements HitExpandedPanel {
	private static final long serialVersionUID = 1L;
																										
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ExpandedPanel.class.getName());
	
	private AbstractConsole<?> console;
	
	private class ColumnsProvider implements IDataProvider<ICellPopulator<SearchResult>> {
		@Override
		public Iterator<ICellPopulator<SearchResult>> iterator(long first, long count) {
			List<ICellPopulator<SearchResult>> populators = new ArrayList<ICellPopulator<SearchResult>>((int)count);
			for (GridColumn<SearchResult, String> column : getColumns()) {
				if (column.isExpanded())
					populators.add(column);
			}
			return populators.iterator(); 
		}
		@Override
		public long size() {
			return getColumns().size();
		}
		public IModel<ICellPopulator<SearchResult>> model(ICellPopulator<SearchResult> object) {
			return new Model<ICellPopulator<SearchResult>>(object);
		}
		@Override
		public void detach() {
			for (GridColumn<?,?> column : getColumns()) {
				column.detach();
			};
		}
	};

	/**
	 * @param id
	 * @param console
	 * @param model
	 */
	
	public ExpandedPanel(String id, AbstractConsole<?> console, IModel<T> model) {
		this(id, console, model, null);
	}
	
	public ExpandedPanel(String id, AbstractConsole<?> console, IModel<T> model, List<String> snippets) {
		super(id);
		
		this.console = console;
		
		add(new DataView<ICellPopulator<SearchResult>>("td", new ColumnsProvider(), getColumns().size()) {
			
			@SuppressWarnings("unchecked")
			public void populateItem(Item<ICellPopulator<SearchResult>> item) {
				SearchResult result = new SearchResult() {
					@Override
					public String getText() {
						return null;
					}
					@Override
					public List<String> getSnippets() {
						return null;
					}
					@Override
					public float getScore() {
						return 0;
					}
					@Override
					public Map<String, Object> getParameters() {
						return null;
					}
					@Override
					public Object getObject() {
						return model.getObject();
					}
					@Override
					public void detach() {
						model.detach();
					}
				};
				try {
					GridColumn<?,?> column = (GridColumn<?, ?>) item.getModelObject();
					
					Label label;
					
					try {
						logger.debug(column.getDisplayModel().getObject());
						label = new Label("td-label", column.getDisplayModel());
					} 
					catch (Exception e) {
						logger.error(e);
						label = new Label("td-label", e.getClass().getSimpleName() + " " + e.getMessage());
					}
					label.setEscapeModelStrings(false);
					item.add(label);
					
					IModel<SearchResult> ms=new Model<SearchResult>(result);
					ICellPopulator<SearchResult> cell_o=item.getModelObject();
					if (cell_o instanceof GridColumn) {
						((GridColumn<SearchResult, String>) item.getModelObject()).populateItemExpanded(item, "td-container", ms);
					}
					else
						cell_o.populateItem(item, "td-container", ms);
					column.detach();
					ms.detach();
					result.detach();
				} 
				catch (Exception e) {
					
					item.detach();
					
					item.setVisible(false);
					logger.error(e);
				}
			}
		});
	}
	
	public AbstractConsole<?> getConsole() {
		return console;
	}
	
	public List<GridColumn<SearchResult, String>> getColumns() {
		return getConsole().getColumns();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
}