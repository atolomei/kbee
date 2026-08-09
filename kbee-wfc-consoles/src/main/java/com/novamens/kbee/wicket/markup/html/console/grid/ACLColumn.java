package com.novamens.kbee.wicket.markup.html.console.grid;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

public class ACLColumn<T> extends GridColumn<T, String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public ACLColumn(String id, IModel<String> displayModel) {
		super(id, displayModel);
		// TODO Auto-generated constructor stub
		
		/**
		 * 
		 * Cualquier cosa que se le aplique ACL
		 * 
		 * Object
		 * 
		 */
	}

	
	@Override
	public void populateItem(Item<ICellPopulator<T>> cellItem, String componentId, IModel<T> model) {
		super.populateItem(cellItem, componentId, model);
	}
	
		
	
	
}
