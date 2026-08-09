package kbee.web.cursor;

import org.apache.wicket.model.IModel;

import com.novamens.indexer.query.Cursor;

public class ModelListCursor<T> implements IModel<Cursor> {

	private static final long serialVersionUID = 1L;
	private CursorListModel<T> object;
	
	public ModelListCursor(CursorListModel<T> c) {
		this.object=c;
	}
	
	
	@Override
	public void detach() {
		if (object!=null)
			object.detach();
	}
	
	@Override
	public CursorListModel<T> getObject() {
		return object;
	}

}
