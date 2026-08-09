package kbee.web.search.service;

import org.apache.wicket.model.IModel;
import com.novamens.content.base.Content;
import com.novamens.content.model.DataSetMember;
import com.novamens.indexer.query.Suggestion;
import com.novamens.portal6.model.Site;
import com.novamens.wicket.markup.html.form.WebSuggestion;
import com.novamens.wicket.model.ObjectModel;

public class SiteSearchSuggestionService extends com.novamens.kbee.portal.service.SiteSearchSuggestionService {
	
	public SiteSearchSuggestionService() {
	}
	
	public SiteSearchSuggestionService(Site site) {
		super(site);
	}
	
	@Override
	protected Suggestion createSuggestion(Object object, String label, String facet, float score) {
		IModel<?> model = object instanceof Content  
			? new ObjectModel<Content>((Content)object)
			: ((object instanceof DataSetMember) 
				? new ObjectModel<Content>((Content)object) 
				: null);
		return new WebSuggestion(model, label, score, false);
	}
}	