package kbee.web.nav;

import org.apache.wicket.model.IDetachable;

import com.novamens.indexer.query.Cursor;

public interface Navigator<T> extends IDetachable  {
	//public void onNavigate(T object);
	public Cursor getCursor();
	public long getIndex();
	
}
