package kbee.web.console;

import org.apache.wicket.model.IDetachable;

import com.novamens.indexer.query.Cursor;
import com.novamens.wicket.markup.html.repeater.util.Searcher;

import kbee.web.nav.Navigator;

public class SolrSearcherNavigator<T> implements Navigator<T>, IDetachable {

	private static final long serialVersionUID = 1L;
	private Cursor cursor;
	private Searcher searcher;
	
	public SolrSearcherNavigator(Searcher searcher, long index) {
		this.searcher=searcher;
		this.cursor = searcher.getResultSet().getCursor();
		this.cursor.setIndex(index);
	}
	
	
	public Searcher getSearcher() {
		return this.searcher;
	}
	
	public Cursor getCursor() {
		return cursor;
	}
	
	public long getIndex() {
		return getCursor().getIndex();
	}
	
	@Override
	public void detach() {
		if (cursor instanceof IDetachable)
			((IDetachable) cursor).detach();
		if (searcher!=null)
			searcher.detach();
	}
	


}
