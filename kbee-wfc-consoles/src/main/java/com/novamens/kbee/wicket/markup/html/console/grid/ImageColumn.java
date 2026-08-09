package com.novamens.kbee.wicket.markup.html.console.grid;


import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import com.novamens.indexer.query.SearchResult;


public abstract class ImageColumn<T extends com.novamens.dom.Object> extends GridColumn<SearchResult, String> {
	
 	private static final long serialVersionUID = 1L;
 	public static final int THUMBNAIL_WIDTH = 96;
	
	
	public ImageColumn(String id, IModel<String> displayModel) {
		super(id, displayModel);
 	}

	public void populateItem(Item<ICellPopulator<SearchResult>> cellItem, String componentId, IModel<SearchResult> resultmodel) {
 	}
	
 
	protected void onClick(AjaxRequestTarget target) {
	}

	protected abstract Image getImage(String id, T object);
	
	
	protected String  getCss() {
		return "thumbnailcolumn";
	}

	@Override
	public int getWidth() {
		return THUMBNAIL_WIDTH;
	}
	
	@Override
	public boolean isResizable() {
		return false;
	}
	

	/**
	 * do not export to xls, csv
	 */
	@Override
	public boolean isExportable() {
		return false;
	}
	

	 


}
