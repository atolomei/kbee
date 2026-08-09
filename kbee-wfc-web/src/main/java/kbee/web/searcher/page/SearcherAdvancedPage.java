package kbee.web.searcher.page;

import com.novamens.content.base.Content;


/**
 * 
 * 
 * 
 * SearcheDetailVideo 
 * SearcheDetailAudio 
 * SearcherDetailPhotGallery
 * 
 * SearcheDetailDocument
 *    SearcheDetailManual
 * 
 * Documentos
 * Revisiones
 * Update
 * 
 *
 */
public class SearcherAdvancedPage extends AbstractSearcherPage<Content> {

	private static final long serialVersionUID = 1L;

	@Override
	protected boolean isEditableOn() {
		return false;
	}
	
	
	
	@Override
	protected boolean isExplorerOn() {
		return false;
	}

	
}
