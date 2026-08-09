package com.novamens.kbee.wicket.markup.html.console.grid;

import java.time.OffsetDateTime;

import org.apache.wicket.model.IModel;


public class LastModifiedColumn<T extends com.novamens.security.Auditable> extends DateColumn<T> {
	
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(LastModifiedColumn.class.getName());
												
	
	public LastModifiedColumn(String id, IModel<String> displayModel) {
		super(id, displayModel, null);
	}


	public LastModifiedColumn(String id, IModel<String> displayModel, String sortProperty) {
		super(id, displayModel, sortProperty);
	}

	@Override
	protected OffsetDateTime getOffsetDateTime(T object) {
		try {
			return object.getLastModifiedOffsetDateTime();
		} 
		catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

}
