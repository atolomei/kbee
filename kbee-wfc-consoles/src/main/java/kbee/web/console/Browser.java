package kbee.web.console;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import com.novamens.indexer.query.Query;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;

public interface Browser<T> {

	public Query getQuery();
	
	public List<IModel<T>> getSelection();
	public void refresh(AjaxRequestTarget target);
	public String getConsoleKey();
	public List<ToolbarItem> getSelectionToolbarItems();
	public <P extends WebMarkupContainer> P getPanel(Class<P> panelclass);
}
