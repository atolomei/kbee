package kbee.web.searcher.panel;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.model.Classification;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.portal6.model.Site;

/**
 * @param <T>
 */
@SuppressWarnings("serial")
public class SearcherDetailMetadataPanel<T extends Content> extends SearcherDetailPanel<T> {
	private static final long serialVersionUID = 1L;

	private String subtitle = null;
	
	public SearcherDetailMetadataPanel(String id, IModel<T> model, IModel<Site> site_model) {
		super(id, model, site_model);
	}
	 
	public String getSubtitle() {
		
		if (subtitle!=null)
			return this.subtitle;
		
		String rule=getModel().getObject().getContentTemplate().getPortalsSubtitleRule();
		
		
		if (rule!=null && !"".equals(rule)) {
			try {
				KbeeTextTemplate template = new KbeeTextTemplate(rule);
				subtitle = template.process(getModel().getObject());
				
			}
			catch (Exception e) {
				subtitle=  "Error. "+ e.getClass().getName();
				logger.error(e);
			}
		}
		else {
			subtitle = getContentType();
		}
		return subtitle;
	}


	@Override
	public void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer metadata_container = new WebMarkupContainer("container") {
			@Override
			public boolean isVisible() {
				return true;
			}
		};

		add(metadata_container);
	
		Label subtitle = new Label("metadata", getSubtitle());
		subtitle.setEscapeModelStrings(false);
		metadata_container.add(subtitle);
		add(metadata_container);
	}


	private String getContentType() {
		StringBuilder str = new StringBuilder();
		for (Classification clasi : getModel().getObject().getClassification()) {
			if (clasi.getClassifier().isContentType()) {
				if (str.length() > 0)
					str.append(", ");
				str.append(clasi.getStrValue());
			}
		}
		return str.toString();
}

}
