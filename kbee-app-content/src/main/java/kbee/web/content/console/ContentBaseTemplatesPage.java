package kbee.web.content.console;

import java.util.List;

import org.apache.wicket.Page;

import com.novamens.content.base.Content;
import com.novamens.content.web.suggestion.service.TemplatesSearchSuggestionService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.Suggestion;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;

import kbee.web.console.Console;
import kbee.web.console.ContentConsolePage;
import kbee.web.page.ApplicationMenuSection;

@SuppressWarnings("serial")
public class ContentBaseTemplatesPage extends ContentConsolePage<Content> {
	private static final long serialVersionUID = 1L;
	
	final boolean is_root					= ServiceLocator.getService(com.novamens.service.SecurityService.class).isRoot(); 
	final boolean is_domain_admin			= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	//final boolean is_cb						= ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.CABINET_TEMPLATES.getId());

	public ContentBaseTemplatesPage() {
		super(null);
		
	}
	
	public ContentBaseTemplatesPage(Query query) {
		super(query);
	}
	
	public Console<Content> newConsole(Query query) {
		return new ContentBaseTemplatesConsole(query) {
			@Override
			public Page getConsolePage(Query query, long index) {
				return new ContentBaseTemplatesPage(query);
			}
		};
	}

	@Override
	public ApplicationMenuSection getApplicationMenuSection() {
		return ApplicationMenuSection.CONTENT;
	}
	
	@Override
	public Page getConsolePage(Query query, long index) {
		return new ContentBaseTemplatesPage(query);
	}
	
	@Override
	protected List<Suggestion> getSuggestions(String pattern) {
		return getDomain().getService(TemplatesSearchSuggestionService.class).getSuggestions(pattern); 
	}
	
	@Override
	protected boolean isSuggester() {
		return true;
	}
	
	@Override
	public boolean hasPermissions(){
		return is_domain_admin || is_root; 
	}
}
