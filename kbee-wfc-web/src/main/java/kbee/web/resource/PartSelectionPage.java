package kbee.web.resource;

import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;

import kbee.web.page.KbeeWebPage;

public class PartSelectionPage extends  KbeeWebPage<Void>  {
	private static final long serialVersionUID = 1L;

	public PartSelectionPage(IModel<Content> model) {
		add(new PartSelectionPanel("selector", model));
	}
	
}
