package kbee.web.searcher.panel;


import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;

import com.novamens.content.searcher.SearcherHomeBlock;
import com.novamens.dom.Domain;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.editor.DomainObjectMainPanel;
import kbee.web.portal6.DomainSearcherPortalService;

/**
 * 
 * 
 *  SearcherSettingsMainPnel
 *  SearcherSettingsHomeMainPanel
 *   SettingsHomeBlockEditor
 *  
 * 
 *  
 *  
 *
 */
public class SearcherSettingsHomeMainPanel extends DomainObjectMainPanel<Domain> {
	
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SearcherSettingsHomeMainPanel.class.getName());
	
	public SearcherSettingsHomeMainPanel(String id, IModel<Domain> model) {
		super(id, model);
	}
	
	List<IModel<SearcherHomeBlock>> list = null;
	
	public List<IModel<SearcherHomeBlock>> getBlocks() {

		if (list!=null)
			return list;
		list = new ArrayList<IModel<SearcherHomeBlock>>();
		for (SearcherHomeBlock b: getDomain().getService(DomainSearcherPortalService.class).getSearcherHomeBlock()) 
			list.add(new ObjectModel<SearcherHomeBlock>(b));
		return list;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
								
		ListView<IModel<SearcherHomeBlock>> panels = new ListView<IModel<SearcherHomeBlock>>("section", getBlocks()) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(ListItem<IModel<SearcherHomeBlock>> item) {
				SearchHomeBlockEditor editor = new SearchHomeBlockEditor("block-editor", item.getModelObject());
				item.add(editor);
			}
		};
		add(panels);
	}
}
