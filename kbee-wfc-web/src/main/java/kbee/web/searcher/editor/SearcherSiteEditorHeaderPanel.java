package kbee.web.searcher.editor;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.portal6.model.Site;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.nav.SitesBC;
import kbee.web.object.TitleHeaderPanel;

public class SearcherSiteEditorHeaderPanel extends TitleHeaderPanel<Site> {
				
	private static final long serialVersionUID = 1L;
	
	IModel<String> icon = new Model<String>("fal fa-sitemap");

	public SearcherSiteEditorHeaderPanel(IModel<Site> model) {
		super("searcher-title-panel", model);
		setOutputMarkupId(true);
		MenuBreadCrumbPanel  bc = new MenuBreadCrumbPanel();
		bc.addElement(new SitesBC());
		bc.addElement(new BCElement(new Model<String>(model.getObject().getDisplayName())));
		setBreadCrumbPanel(bc);
	} 

	@Override
	protected IModel<String> getGlyphicon() {
		return icon; 
	}
}
