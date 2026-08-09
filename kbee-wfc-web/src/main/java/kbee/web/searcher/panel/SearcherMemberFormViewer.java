package kbee.web.searcher.panel;

import org.apache.wicket.model.IModel;

import com.novamens.content.form.EFormData;
import com.novamens.wicket.markup.html.panel.KBPanel;

import kbee.web.eform.EFormViewer;

public class SearcherMemberFormViewer extends KBPanel {
	private static final long serialVersionUID = 1L;

	public SearcherMemberFormViewer(String id, IModel<EFormData> model) {
		super(id);
		add(new EFormViewer("eform", model));
	}
}
