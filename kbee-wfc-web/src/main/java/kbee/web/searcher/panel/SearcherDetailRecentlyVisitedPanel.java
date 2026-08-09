package kbee.web.searcher.panel;

import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.portal6.model.Site;

public class SearcherDetailRecentlyVisitedPanel<T extends Content> extends SearcherDetailPanel<T> {
		
	private static final long serialVersionUID = 1L;
	
	static kbee.util.logging.Logger logger =  kbee.util.logging.Logger.getLogger( SearcherDetailRecentlyVisitedPanel.class.getName());

	
	public SearcherDetailRecentlyVisitedPanel(String id, IModel<Site> site_model) {
		super(id, null, site_model);
	}
	
	
	
	public void onIntialize() {
		super.onInitialize();
	}

	
}
