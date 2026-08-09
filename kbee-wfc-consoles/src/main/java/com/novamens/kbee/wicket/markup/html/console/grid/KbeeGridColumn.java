package com.novamens.kbee.wicket.markup.html.console.grid;

import com.novamens.indexer.query.SearchResult;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


public abstract class KbeeGridColumn<T> extends GridColumn<SearchResult, String> {

	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeGridColumn.class.getName());

	private String cssClass = null;
	private String contextKey = null;
	private String headerCssClass = null;
	private String rowCssClass = null;
	
	public KbeeGridColumn(String id, IModel<String> displayModel) {
		super(id, displayModel);
	}

	public KbeeGridColumn(String id, IModel<String> displayModel, String sortProperty) {
		super(id, displayModel, sortProperty);
	}

	public KbeeGridColumn(String id, IModel<String> displayModel, String sortProperty, String gridContextKey) {
		super(id, displayModel, sortProperty, gridContextKey);
	}

	@Override
	public IModel<String> getCellAsString(SearchResult object) {
		if (object.getObject() == null)
			return new Model<String>("null");
		try {
			@SuppressWarnings("unchecked")
			String value = getValueAsString((T) object.getObject());
			return new Model<>(value);
		} 
		catch (Exception e) {
			logger.error(e);
			return new Model<String>(e.getClass().getSimpleName()+" | " + e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getCssClass(SearchResult object) {
		return this.getCellCssClass( (T) object.getObject());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected IModel<String> getLabelModel(SearchResult object) {
		if (object.getObject() == null)
			return new Model<String>("err");
		try {
			String value = getValueAsHTML((T) object.getObject());
			return new Model<>(value);
		} 
		catch (Exception e) {
			logger.error(e);
			return new Model<String>(e.getClass().getName());
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	protected IModel<String> getExpandedLabelModel(SearchResult object) {
		if (object.getObject() == null)
			return new Model<String>("err");
		try {
			String value = getExpandedValueAsString((T) object.getObject());
			return new Model<>(value);
		} 
		catch (Exception e) {
			logger.error(e);
			return new Model<String>(e.getClass().getName());
		}
	}

	
	@Override
	public String getCssClass() {
		if (cssClass != null)
			return cssClass;
		return super.getCssClass();
	}

	@Override
	public String getContextKey() {
		if (this.contextKey != null)
			return this.contextKey;
		return super.getContextKey();
	}

	@Override
	public String getHeaderCssClass() {
		if (this.headerCssClass != null)
			return this.headerCssClass;
		return headerCssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public void setContextKey(String contextKey) {
		this.contextKey = contextKey;
	}

	public void setHeaderCssClass(String headerCssClass) {
		this.headerCssClass = headerCssClass;
	}

	@Override
	public String getRowCssClass() {
		return rowCssClass;
	}

	public void setRowCssClass(String rowCssClass) {
		this.rowCssClass = rowCssClass;
	}
	
	protected String getCellCssClass(T object) {
		return super.getCssClass();
	}
	
	protected abstract String getValueAsString(T object);
	protected abstract String getExpandedValueAsString(T object);

	
	protected String getValueAsHTML(T object) {
		return getValueAsString(object);
	}

	


}
