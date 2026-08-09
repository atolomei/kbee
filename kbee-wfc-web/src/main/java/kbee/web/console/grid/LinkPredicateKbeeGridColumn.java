package kbee.web.console.grid;



import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.grid.KbeePredicateGridColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.danekja.java.util.function.serializable.SerializableFunction;


/**
 * 
 * @param <T>
 */
public class LinkPredicateKbeeGridColumn<T> extends KbeePredicateGridColumn<T> {

	private static final long serialVersionUID = 1L;
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(LinkPredicateKbeeGridColumn.class.getName());
	private SerializableFunction<T, IModel<T>> modelCreator;

	
	public LinkPredicateKbeeGridColumn(String id, IModel<String> displayModel, SerializableFunction<T, String> valueResolver, SerializableFunction<T, IModel<T>> modelCreator) {
		super(id, displayModel, valueResolver);
		this.modelCreator = modelCreator;
		this.setCssClass("col title col-xs-1 col-md-1 col-lg-1");
	}
 
	
	public LinkPredicateKbeeGridColumn(String id, IModel<String> displayModel, String sortProperty, SerializableFunction<T, String> valueResolver, SerializableFunction<T, IModel<T>> modelCreator) {
		super(id, displayModel, sortProperty, valueResolver);
		this.modelCreator = modelCreator;
		this.setCssClass("col title col-xs-1 col-md-1 col-lg-1");
	}
	
	@SuppressWarnings("unchecked")
	protected Panel getTitlePanel(String componentId, IModel<SearchResult> resultmodel) {
		Object object = resultmodel.getObject().getObject();
		IModel<T> objectmodel = createModel((T) object);
		String title = super.getLabelModel( resultmodel.getObject()).getObject();
		KbeeTitleColumnPanel<T> titlePanel = new KbeeTitleColumnPanel<>(componentId, title, objectmodel);
		titlePanel.setCss("cell-label btn-link");
		titlePanel.setTarget(getTarget());
		return titlePanel;
	}
	
	
	private String target;
	
	public void setTarget(String t) {
		this.target=t;
	}
	
	public String getTarget() {
		return target;
	}


	@Override
	public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
		try {
			Panel titlePanel = getTitlePanel(componentId, resultmodel);
			cellItem.add(titlePanel);
		} catch (Exception e) {
			logger.error(e);
			cellItem.add(new Label(componentId, e.getClass().getName()));
		}
	}
				
	
	protected IModel<T> createModel(T object) {
		return modelCreator.apply(object);
	}

	public void setModelCreator(SerializableFunction<T, IModel<T>> modelCreator) {
		this.modelCreator = modelCreator;
	}

}
