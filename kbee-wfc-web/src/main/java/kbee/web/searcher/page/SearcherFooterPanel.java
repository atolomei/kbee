package kbee.web.searcher.page;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;

public class SearcherFooterPanel extends Panel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private IModel<Site> model;

	public SearcherFooterPanel(String id, IModel<Site> model) {
		super(id);
		this.model=model;
	}
	
	public void onDetach() {
		super.onDetach();
		
		if (this.model!=null)
			this.model.detach();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		add (new InvisiblePanel("company"));
		add (new InvisiblePanel("social"));
			
		//add(new CopyrightPanel("copyright"));
		//add(new LegalPanel("legal", this.model));
	}
}