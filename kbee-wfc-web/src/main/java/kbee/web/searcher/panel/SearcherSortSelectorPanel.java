package kbee.web.searcher.panel;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;


import com.novamens.wicket.markup.html.form.ExtendedChoiceField;
		
@SuppressWarnings("serial")
public class SearcherSortSelectorPanel extends Panel {
	private static final long serialVersionUID = 1L;

	private String source;

	private static String relevance = "relevance";
	private static String title = "title_sort";
	private static String date = "modified";
	
	
	public SearcherSortSelectorPanel(String id) {
		super(id);
		setOutputMarkupId(true);
		addChoices();
	}

	public List<String> getSources() {
		List<String> sources = new ArrayList<String>();
		sources.add(relevance);
		sources.add(title);
		sources.add(date);
		return sources;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSource() {
		return source;
	}

	protected void addChoices() {
		add(new ExtendedChoiceField<String>("source", new PropertyModel<String>(this, "source"),
				new PropertyModel<List<String>>(this, "sources")) {
			@Override
			public void onUpdate(AjaxRequestTarget target) {
				String source = getValue();
				SearcherSortSelectorPanel.this.onUpdate(source, target);
			}
			@Override
			public String getIdValue(String value) {
				return value.toLowerCase();
			}
			@Override
			public String getDisplayValue(String value) {
				return (new StringResourceModel(value, SearcherSortSelectorPanel.this, null)).getObject();
			}
		});
	}

	protected void onUpdate(String source2, AjaxRequestTarget target) {
	}
}