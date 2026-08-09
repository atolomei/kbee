package kbee.web.searcher.searchform;

import com.novamens.portal6.model.Site;
import com.novamens.service.FactoryService;

public interface SearcherFormFactory extends FactoryService {
	
	public SearcherFormPanel<Site> create();
	public SearcherFormPanel<Site> create(String id);
	
	public String getDomainName();
	

}
