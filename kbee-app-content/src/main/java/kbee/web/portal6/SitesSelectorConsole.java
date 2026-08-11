package kbee.web.portal6;

import org.apache.wicket.Page;

import com.novamens.indexer.query.Query;

public class SitesSelectorConsole extends SitesConsole {

	private static final long serialVersionUID = 1L;

	public SitesSelectorConsole(Query query) {
		super(query);
	}

	@Override
	public Page getConsolePage(Query query, long index) {
		return null;
	}

}
