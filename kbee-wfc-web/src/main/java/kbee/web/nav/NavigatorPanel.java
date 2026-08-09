package kbee.web.nav;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.value.IValueMap;

import com.novamens.indexer.query.Cursor;
import com.novamens.indexer.query.SearchResult;
import com.novamens.kbee.wicket.markup.html.ajax.WorkingAjaxLink;
import com.novamens.kbee.wicket.util.DisplayNameExtractor;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.markup.html.repeater.util.Searcher;



@Deprecated
@SuppressWarnings("serial")
public class NavigatorPanel<T> extends Panel {

	private static final long serialVersionUID = 1L;

	static private final int MAX_LENGTH = 40;
	
	private Cursor cursor;
	private Navigator<T> navigator;

	public NavigatorPanel(String id, Searcher searcher, long index) {
		super(id);
		if (searcher!=null) {
			setCursor(searcher.getResultSet().getCursor());
			if (getCursor()!=null)
			getCursor().setIndex(index);
		}
		add(newPreviousLink());
		add(newNextLink());
	}
	
	public NavigatorPanel(String id, Navigator<T> navigator) {
		super(id);
		if (navigator!=null) {
			setNavigator(navigator);
			setCursor(navigator.getCursor());
			if (getCursor()!=null)
			getCursor().setIndex(navigator.getIndex());
		}
		add(newPreviousLink());
		add(newNextLink());
	}
	
	public Cursor getCursor() {
		return cursor;
	}
	
	public void setCursor(Cursor cursor) {
		this.cursor = cursor; 
	}
	
	public Navigator<T> getNavigator() {
		return navigator;
	}
	
	public void setNavigator(Navigator<T> navigator) {
		this.navigator = navigator; 
	}
	
	public long getIndex() {
		return getCursor().getIndex();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (navigator!=null) {
			navigator.detach();
		}	
	}
	
	@SuppressWarnings("unchecked")
	protected AjaxLink<?> newNextLink() {
		return new WorkingAjaxLink<T>("next-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				SearchResult result = getCursor().next();
				if (result!=null) {
					if (getNavigator()!=null) {
						// getNavigator().onNavigate((T)result.getObject());
						throw new KbeeRuntimeException("getNavigator().onNavigate((T)result.getObject());");
					}
					else {
						onNavigate((T)result.getObject());
					}
				}	
			}
			@Override
			public boolean isEnabled() {
				return getCursor()!=null && getCursor().hasMoreElements();
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
					String label = getLabel(getCursor().get(getCursor().getIndex()+1));
					attributes.put("title", "Next: " + label);
				}
				else {
					attributes.remove("title");
				}
			}
		};
	}
	
	
	@SuppressWarnings("unchecked")
	protected AjaxLink<?> newPreviousLink() {
		return new WorkingAjaxLink<T>("previous-link") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				SearchResult result = getCursor().previous();
				if (result!=null) {
					if (getNavigator()!=null) {
						//getNavigator().onNavigate((T)result.getObject());
						throw new KbeeRuntimeException("getNavigator().onNavigate((T)result.getObject());");
					}
					else {
						onNavigate((T)result.getObject());
					}
				}
			}
			@Override
			public boolean isEnabled() {
				return getCursor()!=null && getCursor().getIndex() > 0;
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
				if (isVisible() && isEnabled()) {
					String label = getLabel(getCursor().get(getCursor().getIndex()-1));
					attributes.put("title", "Prev: " + label);
				}
				else {
					attributes.remove("title");
				}
			}
		};
	}
	
	protected void onNavigate(T object) {
		throw new KbeeRuntimeException("onNavigate(T);");
	}
	
	protected String getLabel(SearchResult result) {
		if (result==null) return "-";
		String label = DisplayNameExtractor.get(result.getObject());
		if (label!=null && label.length()>MAX_LENGTH) 
			label = label.substring(0, MAX_LENGTH-3)+"...";
		return label;
	}
}
