package kbee.web.searcher.panel;

import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.portal6.model.Site;

public class SearcherDetailPanel<T extends Content> extends SearcherPanel {
	private static final long serialVersionUID = 1L;
	
	static kbee.util.logging.Logger logger =  kbee.util.logging.Logger.getLogger(SearcherDetailPanel.class.getName());
	
	private IModel<T> model;
	private IModel<Site> site_model;

	public SearcherDetailPanel(String id, IModel<T> model, IModel<Site> site_model) {
		super(id, "detail");
		setModel(model);
		setSiteModel(site_model);
	}

	public IModel<T> getModel() {
		return model;
	}

	public void setModel(IModel<T> model) {
		this.model = model;
	}
	
	public IModel<Site> getSiteModel() {
		return site_model;
	}
	
	public Site getSite() {
		return getSiteModel()!=null ? getSiteModel().getObject() : null;
	}

	public void setSiteModel(IModel<Site> model) {
		this.site_model = model;
	}
	
	public T getModelObject() {
		return getModel().getObject();
	}
	
	public Content getContent() {
		return getModelObject();
	}
	
	@Override
	public void onDetach() {
		if (model != null)
			model.detach();
		
		if (site_model!=null)
			site_model.detach();
		
		super.onDetach();
	}
}
