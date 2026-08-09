package kbee.web.content.template;

import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.base.Content;

import kbee.web.page.KbeeWebPage;
import kbee.web.searcher.page.AbstractSearcherPage;

public class BrowserPage extends KbeeWebPage<Void>  {
	private static final long serialVersionUID = 1L;

	
	public BrowserPage(PageParameters parameters) {
		
			add(new BrowserPanel<Content>("browser"));
	}
	
	@Override
	protected void setHeaders(WebResponse response) {
		super.setHeaders(response);
		response.setHeader("X-Frame-Options", "SAMEORIGIN");
	}
	
//	@Override
//	protected void addListeners() {
//		super.addListeners();
//	}
	
}