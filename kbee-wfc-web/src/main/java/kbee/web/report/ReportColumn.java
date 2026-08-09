package kbee.web.report;

import org.apache.wicket.model.IModel;

import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.console.grid.GridColumn;

public class ReportColumn extends GridColumn<SearchResult, String> {
	private static final long serialVersionUID = 1L;

	public ReportColumn(String id, IModel<String> displayModel, String sortProperty) {
		super(id, displayModel, sortProperty);
	}
	
	public IModel<String> getValueModel(SearchResult result) {
		return getLabelModel(result);
	}	
}
