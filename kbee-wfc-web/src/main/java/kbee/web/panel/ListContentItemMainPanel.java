package kbee.web.panel;

import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.service.ContentService;
import com.novamens.portal6.model.Site;
import com.novamens.wicket.model.ObjectModel;

public class ListContentItemMainPanel extends ListSimpleItemMainPanel<Content> {
	private static final long serialVersionUID = 1L;
	
	public ListContentItemMainPanel(String id, 
			IModel<Content> model, 
			IModel<Site> siteModel, 
			int index, 
			boolean is_expanded) {
		super(id, model, index, is_expanded);
		
		if (siteModel.getObject().isDisplayValidVersion()) {
			Content content = model.getObject();
			Content version = content.getService(ContentService.class).getValidVersion();
			if (version!=null && !content.equals(version)) {
				setModel(new ObjectModel<Content>(version));
			}
		}
	}
}
