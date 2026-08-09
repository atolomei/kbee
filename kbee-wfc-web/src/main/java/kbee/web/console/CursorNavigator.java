package kbee.web.console;


import org.apache.wicket.model.IDetachable;

import com.novamens.indexer.query.Cursor;

import kbee.web.nav.Navigator;

public class CursorNavigator<T> implements Navigator<T>, IDetachable {
	private static final long serialVersionUID = 1L;
	
	private Cursor cursor;
	
	public CursorNavigator(Cursor cursor) {
		this.cursor = cursor;
	}
	
	public CursorNavigator(Cursor cursor, long index) {
		this.cursor = cursor; 
		this.cursor.setIndex(index);
	}
	
	public Cursor getCursor() {
		return cursor;
	}
	
	public long getIndex() {
		return getCursor().getIndex();
	}
	
	public void detach() {
		if (cursor instanceof IDetachable)
			((IDetachable) cursor).detach();
	}
}
