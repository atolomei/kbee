package kbee.web.object;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;
import com.novamens.wicket.model.ObjectModel;

public class ObjectStatusColumn<T extends com.novamens.dom.Object> extends GridColumn<SearchResult, String> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ObjectStatusColumn.class.getName());
	
	public static final int STATUS_WIDTH = 48;
	
	private String consoleName;

	public ObjectStatusColumn(String id, String consoleName, IModel<String> displayModel) {
		super(id, displayModel);
		this.consoleName=consoleName;
		super.setPreferred(true);
	}
	
	@Override
	protected String getContextKey() {
		return consoleName + super.getContextKey();
	}
	
	@Override
	public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
		try {
			T object = getObject(resultmodel);
			cellItem.add( new ObjectStatusPanel<T>(componentId, new  ObjectModel<T>(object)));
		} 
		catch (Exception e) {
			logger.error(e);
			cellItem.add(new Label(componentId, e.getClass().getSimpleName()));
		}
 	}
	
	@Override
	public void populateItemExpanded(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> model) {
		populateItem(cellItem, componentId, model);
	}
	
	@SuppressWarnings("unchecked")
	protected T getObject(IModel<SearchResult> resultmodel) {
		return (T) resultmodel.getObject().getObject();
	}
	
	@Override
	public int getWidth() {
		return STATUS_WIDTH;
	}
	
	@Override
	public boolean isResizable() {
		return false;
	}
	
	@Override
	public String getCssClass()	{
		return "icon-column cell-container";
	}

	@Override
	public boolean isExportable() {
		return false;
	}
}