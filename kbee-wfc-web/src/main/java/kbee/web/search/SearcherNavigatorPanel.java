package kbee.web.search;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.value.IValueMap;

import com.novamens.indexer.query.Cursor;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingAjaxLink;
import com.novamens.kbee.wicket.markup.html.console.panel.SolrCursorModel;
import com.novamens.kbee.wicket.util.DisplayNameExtractor;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.panel.KBPanel;
import com.novamens.wicket.markup.html.repeater.util.Searcher;

import kbee.web.cursor.CursorListModel;
import kbee.web.cursor.ModelListCursor;

public class SearcherNavigatorPanel<T> extends KBPanel {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SearcherNavigatorPanel.class.getName());

	static private final int MAX_LENGTH = 40;

	private IModel<Cursor> cursor_model;
	
	
	public SearcherNavigatorPanel(String id, Searcher searcher, long index) {
		super(id);
		
		if (searcher!=null) {
			if ( searcher.getResultSet().getCursor() instanceof com.novamens.solr.indexer.query.SolrCursor) {
				setCursor(new SolrCursorModel( (com.novamens.solr.indexer.query.SolrCursor) searcher.getResultSet().getCursor() ) );
				if (getCursor()!=null)
					getCursor().getObject().setIndex(index);	
			}
			else if ( searcher.getResultSet().getCursor() instanceof CursorListModel) {
					setCursor(new ModelListCursor<T>( (CursorListModel<T>) searcher.getResultSet().getCursor() ) );
					if (getCursor()!=null)
						getCursor().getObject().setIndex(index);
			}
			else 
				throw new KbeeRuntimeException ("SolrCursor and CursorListModel -> " + searcher.getResultSet().getCursor() !=null ? searcher.getResultSet().getCursor().getClass().getName() : "");
		}
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		add(newPreviousLink());
		add(newNextLink());
	}
	
	
	public SearcherNavigatorPanel(String id, IModel<Cursor> cursor) {
		super(id);
		setCursor(cursor);
	}
	
	public IModel<Cursor> getCursor() {
		return cursor_model;
	}
	
	public void setCursor(IModel<Cursor> cursor) {
		this.cursor_model = cursor; 
	}
	
	public long getIndex() {
		return getCursor().getObject().getIndex();
	}
	
	
	public void onDetach() {
		super.onDetach();
		
		if (cursor_model!=null)
			cursor_model.detach();
	}
	@SuppressWarnings({ "unchecked", "serial" })
	protected AjaxLink<?> newNextLink() {
		return new WorkingAjaxLink<T>("next-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					SearchResult result = getCursor().getObject().next();
					if (result!=null)
						onNavigate((T)result.getObject());
				} catch (Exception e) {
					logger.error(e);
				}
			}
			@Override
			public boolean isEnabled() {
				return getCursor()!=null && getCursor().getObject().hasMoreElements();
			}

			@Override
			public boolean isVisible() {
				return getCursor()!=null;
			}
			@Override
			public String getBeforeClick() {
				return "if (typeof submit === \"function\") { submit(); }";
			}
			@Override
			protected void onComponentTag(final ComponentTag tag) {
				super.onComponentTag(tag);
				IValueMap attributes = tag.getAttributes();
				if (isEnabled()) {
					String label = getLabel(getCursor().getObject().get(getCursor().getObject().getIndex()+1));
					attributes.put("title", " - " + label);
				}
				else {
					attributes.put("title", "[no more items]");
					// attributes.remove("title");
				}
			}
		};
	}
	
	@SuppressWarnings({ "unchecked", "serial" })
	protected AjaxLink<?> newPreviousLink() {
		return new WorkingAjaxLink<T>("previous-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				SearchResult result = getCursor().getObject().previous();
				if (result!=null)
					onNavigate((T)result.getObject());
			}
			@Override
			public boolean isEnabled() {
				return getCursor()!=null && getCursor().getObject().getIndex() > 0;
			}
			
			@Override
			public boolean isVisible() {
				return getCursor().getObject()!=null;
			}
			
			@Override
			public String getBeforeClick() {
				return "if (typeof submit === \"function\") { submit(); }";
			}
			@Override
			protected void onComponentTag(final ComponentTag tag) {
				super.onComponentTag(tag);
				IValueMap attributes = tag.getAttributes();
				if (isVisible() && isEnabled()) {
					String label = getLabel(getCursor().getObject().get(getCursor().getObject().getIndex()-1));
					attributes.put("title", " - " + label );
				}
				else {
					// attributes.remove("title");
					attributes.put("title", "[no more items]");
				}
			}
		};
	}
	
	
	
	protected void onNavigate(T object) {
	}
	
	protected String getLabel(SearchResult result) {
		if (result==null) return "-";
		String label = DisplayNameExtractor.get(result.getObject());
		if (label!=null && label.length()>MAX_LENGTH) 
			label = label.substring(0, MAX_LENGTH-3)+"...";
		return label;
	}
}
