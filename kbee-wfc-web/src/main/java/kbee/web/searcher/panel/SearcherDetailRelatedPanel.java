package kbee.web.searcher.panel;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.portal6.model.Site;

public class SearcherDetailRelatedPanel<T extends Content> extends SearcherDetailPanel<T> {

	private static final long serialVersionUID = 1L;

	public SearcherDetailRelatedPanel(String id, IModel<T> model, IModel<Site> site_model) {
		super(id, model, site_model);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer container = new WebMarkupContainer("container") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return true;
			}
		};
		add(container);
		
	}
}
