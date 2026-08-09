package com.novamens.kbee.wicket.markup.html;

import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IStyledColumn;
import org.apache.wicket.model.IModel;

public  abstract  class AbstractLinkColumn<T, S> extends AbstractColumn<T, S> implements IStyledColumn<T, S> {
	
	public AbstractLinkColumn(IModel<String> displayModel) {
		super(displayModel);
	}

	private static final long serialVersionUID = 1L;

	public abstract void onClick();
}
