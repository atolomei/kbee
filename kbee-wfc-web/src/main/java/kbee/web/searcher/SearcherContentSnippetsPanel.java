package kbee.web.searcher;

import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.service.FileSnippet;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.resource.ResourceGlyphIcon;
import kbee.web.resource.ResourceLink;
				
@SuppressWarnings("serial")
public class SearcherContentSnippetsPanel<T extends Content> extends ModelPanel<T> {
	private static final long serialVersionUID = 1L;
	
	private List<FileSnippet> filessnippets;
	
	public SearcherContentSnippetsPanel(String id, IModel<T> model, String query, List<FileSnippet> snippets) {
		super(id, model);
		setSnippets(snippets);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	public void setSnippets(List<FileSnippet> snippets) {
		this.filessnippets=snippets;
	}
	
	public List<FileSnippet> getFilesSnippets() {
		return this.filessnippets;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		WebMarkupContainer wlist = new WebMarkupContainer("snippets-list") {
			@Override
			public boolean isVisible() {
				return true;
			}
		};
			
		add(wlist);
								
		wlist.add(new ListView<FileSnippet>("snippet",  getFilesSnippets()) {
			@Override 
			protected void populateItem(ListItem<FileSnippet> item) {
				IModel<Resource> filemodel = SearcherContentSnippetsPanel.this.getModel(item.getModelObject().file);
				if (filemodel != null) {
					Resource resource = filemodel.getObject();
					ResourceLink<T> resourceLink = new ResourceLink<T>("resource-link", filemodel, SearcherContentSnippetsPanel.this.getModel());
					resourceLink.add(new ResourceGlyphIcon("glyphicon", resource.getGlyphIcon()));
					item.add(resourceLink);
					ResourceLink<T> titleLink = new ResourceLink<T>("title-link", filemodel, SearcherContentSnippetsPanel.this.getModel());
					item.add(titleLink);
					titleLink.add(new Label("resource-title", new Model<String>() {
						public String getObject() {
							String title = filemodel.getObject().getTitle();
							if (title==null) 
								title = filemodel.getObject().getName();
							return title;
						}
					}));
					item.add((new Label("snippet", item.getModelObject().snippet)).setEscapeModelStrings(false));
					filemodel.detach();
				}
			}
		});
	}
	
	private IModel<Resource> getModel(String fileId) {
		for (Resource resource : ((ResourceContainer)getModel().getObject()).getResources()) {
			if (("kbfileimpl#" + String.valueOf(resource.getId())).equals(fileId) ||
				("kbeefileproxy#" + String.valueOf(resource.getId())).equals(fileId)) {
				return new ObjectModel<Resource>(resource);
			}
		}		
		return null;
	}	
}
 