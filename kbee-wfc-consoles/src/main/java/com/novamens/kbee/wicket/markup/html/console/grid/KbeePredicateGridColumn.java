package com.novamens.kbee.wicket.markup.html.console.grid;

import org.apache.wicket.model.IModel;
import org.danekja.java.util.function.serializable.SerializableFunction;


public class KbeePredicateGridColumn<T> extends KbeeGridColumn<T> {

	private static final long serialVersionUID = 1L;
	
	private SerializableFunction<T, String> cssValueResolver;
	private SerializableFunction<T, String> textValueResolver;
	private SerializableFunction<T, String> expandedValueResolver;
	
	private SerializableFunction<T, String> htmlValueResolver = null;

	public KbeePredicateGridColumn(String id, IModel<String> displayModel) {
		super(id, displayModel);
	}
	
	public KbeePredicateGridColumn(String id, IModel<String> displayModel, SerializableFunction<T, String> textValueResolver) {
		super(id, displayModel);
		this.textValueResolver = textValueResolver;
	}

	public KbeePredicateGridColumn(String id, IModel<String> displayModel, String sortProperty, SerializableFunction<T, String> textValueResolver) {
		super(id, displayModel, sortProperty);
		this.textValueResolver = textValueResolver;
	}

	public void setCssValueResolver(SerializableFunction<T, String> cssValueResolver) {
		this.cssValueResolver = cssValueResolver;
	}

	public SerializableFunction<T, String> getCssValueResolver() {
		return cssValueResolver;
	}

	public SerializableFunction<T, String> getTextValueResolver() {
		return textValueResolver;
	}

	public SerializableFunction<T, String> getExpandedValueResolver() {
		return expandedValueResolver;
	}
	
	public void setTextValueResolver(SerializableFunction<T, String> textValueResolver) {
		this.textValueResolver = textValueResolver;
	}

	public void setExpandedValueResolver(SerializableFunction<T, String> expandedValueResolver) {
		this.expandedValueResolver = expandedValueResolver;
	}
	
	public SerializableFunction<T, String> getHtmlValueResolver() {
		return htmlValueResolver;
	}

	public void setHtmlValueResolver(SerializableFunction<T, String> htmlValueResolver) {
		this.htmlValueResolver = htmlValueResolver;
	}
	
	@Override
	protected String getCellCssClass(T object) {
		if (getCssValueResolver()!=null) 
			return getCssValueResolver().apply(object);
		else							 
			return super.getCssClass();
	}

/**
 *  for Expanded Panel.
 *  @see {@link ExpandedPanel}
 *  
 *  Sometimes we need to trunc the grid cell (HTMLValueResolver) but the Expanded panel should display the info completely.
 *  
 * 
 */
	@Override
	protected String getExpandedValueAsString(T object) {
		if (expandedValueResolver!=null)
			return expandedValueResolver.apply(object);
		return getValueAsHTML(object);
	}

	
	@Override
	protected String getValueAsString(T object) {
		return textValueResolver.apply(object);
	}

	@Override
	protected String getValueAsHTML(T object) {
		if(this.htmlValueResolver != null)
			return htmlValueResolver.apply(object);
		return super.getValueAsHTML(object);
	}

}
