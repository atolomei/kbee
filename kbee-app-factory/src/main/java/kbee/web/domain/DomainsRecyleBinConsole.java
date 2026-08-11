package kbee.web.domain;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import com.novamens.dom.Domain;
import com.novamens.indexer.query.Query;
import com.novamens.kbee.wicket.markup.html.console.browser.ToolbarItem;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.console.BaseBrowser;

public class DomainsRecyleBinConsole extends DomainsConsole {
		
	private static final long serialVersionUID = 1L;

	
	public DomainsRecyleBinConsole(Query query) {
		super("domains-recycle-bin", query);
	}

	
	
	protected  IModel<Domain> getModel(Domain object) {
		return new ObjectModel<Domain>(object, true);
	}
	
	@Override
	public Page getConsolePage(Query query, long index) {
		return null;
	}
	

	@Override
	public Query newQuery() {
		return setUserPreference(new DomainsRecycleBinQuery(getQueryIndex()));
	}

	@Override
	protected List<ToolbarItem> getToolbarItems(BaseBrowser<Domain> browser) {
		List<ToolbarItem> items = new ArrayList<ToolbarItem>();
		return items;
	}

	@Override
	protected Component newIcon() {
		WebMarkupContainer icon = new WebMarkupContainer("icon");
		icon.add(new AttributeModifier("class", "far fa-recycle"));
		return icon;
	}
	
}
