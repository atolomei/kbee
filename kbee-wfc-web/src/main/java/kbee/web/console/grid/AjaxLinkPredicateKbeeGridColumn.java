package kbee.web.console.grid;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.danekja.java.util.function.serializable.SerializableFunction;


import com.novamens.indexer.query.SearchResult;

public class AjaxLinkPredicateKbeeGridColumn<T> extends LinkPredicateKbeeGridColumn<T> {

	private static final long serialVersionUID = 1L;

	public AjaxLinkPredicateKbeeGridColumn(String id, IModel<String> displayModel, SerializableFunction<T, String> valueResolver, SerializableFunction<T, IModel<T>> modelCreator) {
		super(id, displayModel, valueResolver, modelCreator);
	}

	public AjaxLinkPredicateKbeeGridColumn(String id, 
			IModel<String> displayModel, 
			String sortProperty, 
			SerializableFunction<T, String> valueResolver, 
			SerializableFunction<T, IModel<T>> modelCreator) {
		super(id, displayModel, sortProperty, valueResolver, modelCreator);
	}
	
	

	@SuppressWarnings("unchecked")
	@Override
	protected Panel getTitlePanel(String componentId, IModel<SearchResult> resultmodel) {
		Object object = resultmodel.getObject().getObject();
		IModel<T> objectmodel = createModel((T) object);
		String title = super.getLabelModel( resultmodel.getObject()).getObject();
		KbeeTitleAjaxColumnPanel<T> titlePanel = new KbeeTitleAjaxColumnPanel<>(componentId, title, objectmodel);
		titlePanel.setCss("cell-label btn-link");
		return titlePanel;
	}
}
