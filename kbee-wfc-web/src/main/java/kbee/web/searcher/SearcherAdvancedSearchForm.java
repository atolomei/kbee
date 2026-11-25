package kbee.web.searcher;

import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.portal6.model.Site;
import com.novamens.wicket.markup.html.panel.KBPanel;

import kbee.web.search.AdvancedSearchForm;
import kbee.web.searcher.searchform.AdvancedSearchClickEvent;
import kbee.web.searcher.searchform.AdvancedSearchEvent;
import kbee.web.searcher.searchform.SearcherOnChangeEvent;
import kbee.web.util.PanelBeanResolver;

public class SearcherAdvancedSearchForm extends KBPanel {
	private static final long serialVersionUID = 1L;

	private String key;
	private String title;
	
	private Map<String, Object> filters = null;;

	public SearcherAdvancedSearchForm(String id, IModel<Site> sitemodel) {
		super(id);
		setOutputMarkupId(true);
		this.key = sitemodel.getObject().getName();
	}

	public String getKey() {
		return this.key;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		Panel customPanel =  (new PanelBeanResolver(
				"portal-advanced-search-form", 
				"form")).getPanel();
		
		if (customPanel!=null) {
			add(customPanel);
			add(new WicketEventListener<AdvancedSearchEvent>() {
				@Override
				public void onEvent(AdvancedSearchEvent event) {
					filters = event.getFilters();
					update(event.getRequestTarget());
				}
			});
		}
		else {
			add(new AdvancedSearchForm("form") {
				public void onSubmit(AjaxRequestTarget target) {
					update(target);
				}
				public void onClose(AjaxRequestTarget target) {
					fire(new AdvancedSearchClickEvent(target));
				}
			});
		}
	}
	
	public  void update(AjaxRequestTarget target) {
		fire(new SearcherOnChangeEvent(target, getFilters()));
		target.add(this);
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	private Map<String, Object> getFilters() {
		return filters!=null
			? filters	
			: ((AdvancedSearchForm)get("form")).getFilters();
	} 
}