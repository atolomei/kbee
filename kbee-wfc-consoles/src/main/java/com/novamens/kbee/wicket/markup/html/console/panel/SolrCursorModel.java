package com.novamens.kbee.wicket.markup.html.console.panel;

import java.io.Serializable;

import org.apache.wicket.model.IModel;

import com.novamens.indexer.query.Cursor;
import com.novamens.solr.indexer.query.SolrCursor;


/**
 * SolR cursor is Serializable
 * 
 */
public class SolrCursorModel implements IModel<Cursor>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	SolrCursor cursor;

	
	public SolrCursorModel( SolrCursor solrCursor) {
		this.cursor=solrCursor;
	}
	
 

	@Override
	public Cursor getObject() {

		return cursor;
	}
	
	

}
