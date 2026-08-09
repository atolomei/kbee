package kbee.web.searcher.panel;

import org.apache.wicket.model.IModel;

import com.novamens.content.searcher.SearcherHomeBlock;

import kbee.web.editor.DomainObjectEditor;

public class SearchHomeBlockEditor extends DomainObjectEditor<SearcherHomeBlock> {

	private static final long serialVersionUID = 1L;

	public SearchHomeBlockEditor(String id, IModel<SearcherHomeBlock> model) {
		super(id, model);
	}
	
	public void onInitialize() {
		super.onInitialize();
		
	}
	
}
