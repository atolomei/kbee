package kbee.web.multidimensional;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.novamens.indexer.query.Facet;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.web.nav.FacetsBC;
import kbee.web.nav.SettingsDropDownBC;
import kbee.web.object.TitleHeaderPanel;

public class FacetHeaderPanel extends TitleHeaderPanel<Facet> {
	
	private static final long serialVersionUID = 1L;
	
	IModel<String> icon = new Model<String>("fal fa-ballot-check");

	public FacetHeaderPanel(IModel<Facet> model) {
		super("facet-panel", model);
		MenuBreadCrumbPanel<?>  bc = new MenuBreadCrumbPanel<>();
		bc.addElement(new SettingsDropDownBC());
		bc.addElement(new FacetsBC());
		bc.addElement(new BCElement(new Model<String>(model.getObject().getDisplayName())));
		setBreadCrumbPanel(bc);

		
	}
	
	protected IModel<String> getGlyphicon() {
		return icon; 
	}
	
	protected IModel<String> getTitle() {
		return new PropertyModel<String>(getModel(), "displayName");	
	}
} 
