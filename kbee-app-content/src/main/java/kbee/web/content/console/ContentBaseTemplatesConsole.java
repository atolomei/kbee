package kbee.web.content.console;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;

import com.novamens.indexer.query.Query;

import kbee.web.query.ContentTemplatesQuery;

public abstract class ContentBaseTemplatesConsole extends ContentBaseConsole {
	private static final long serialVersionUID = 1L;

	
	public ContentBaseTemplatesConsole(Query query) {
		super("templates", null, query);
	}
	
	
	
	
	@Override
	public Query newQuery() {
		return setUserPreference(new ContentTemplatesQuery(getQueryIndex()));
	}
	
	
	protected Component newIcon() {
		return (new WebMarkupContainer("icon")).add(new AttributeModifier("class", "glyphicon glyphicon-templates"));
	}
}
