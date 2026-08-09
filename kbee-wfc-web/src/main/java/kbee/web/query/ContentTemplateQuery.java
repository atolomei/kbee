package kbee.web.query;


import com.novamens.dom.ObjectState;
import com.novamens.indexer.service.Index;

public class ContentTemplateQuery extends DomainSolrQuery {
	private static final long serialVersionUID = 4838162792638978932L;
	
	
	public ContentTemplateQuery(Index index) {
		super(index);
		
		getParameters().put("type", "[idoc, text]");
		getParameters().put("sort", "modified");
		getParameters().put("head", "true");
		getParameters().put("istemplate", "true");
		getParameters().put("state", String.valueOf(ObjectState.ENABLED.getId()));
		getParameters().put("domain", String.valueOf(getDomain().getId()));
		getParameters().put("ascending", "false");

		
	}


}
