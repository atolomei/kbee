package kbee.web.searcher.panel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.portal6.model.Site;

public class SearcherDetailFutureVigencyPanel<T extends Content> extends SearcherDetailPanel<T> {
	
	private static final long serialVersionUID = 1L;
	
	boolean isConsole;
		
	public SearcherDetailFutureVigencyPanel(String id, IModel<T> model, IModel<Site> site_model, boolean isConsole) {
		super(id, model, site_model);
		this.isConsole= isConsole;
	}
	

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		Label future = new Label("future-vigency", new StringResourceModel("future-vigency", this, null));
		add(future);
		
	}
	
	

}
