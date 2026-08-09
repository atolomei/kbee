package kbee.web.searcher.panel;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.portal6.model.Site;

public class SearcherDetailAbstractPanel<T extends Content> extends SearcherDetailPanel<T> {

	private static final long serialVersionUID = 1L;

	public SearcherDetailAbstractPanel(String id, IModel<T> model,  IModel<Site> site_model) {
		super(id, model, site_model);
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer container = new WebMarkupContainer("container") {
		
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
				return getModel().getObject().getAbstract()!=null;
			}
		};

		add(container);

		try {
			String abs = getModel().getObject().getAbstract()!=null? getModel().getObject().getAbstract().asString():"";
			Label c  = new Label("abs", abs);
			c.setEscapeModelStrings(false);
			container.add(c);
		} catch (Exception e) {
			container.add(new Label("abs", e.getClass().getName()));
		}
		
	}

}
