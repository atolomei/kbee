package kbee.web.resource;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.thumbnail.ThumbnailSize;

public class SharedResourceThumbnailImage<T extends Content> extends ResourceThumbnailImage<T> {
	private static final long serialVersionUID = 1L;
	
	public SharedResourceThumbnailImage(String id, IModel<Resource> model, ThumbnailSize size) {
		super (id, null, model, size);
	}
	
	protected ResourceReference getResourceReference() {
		return new WebThumbnailSharedReference(getResource(), getSize());
	}
}