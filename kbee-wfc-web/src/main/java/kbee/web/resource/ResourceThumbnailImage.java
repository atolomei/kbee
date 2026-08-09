package kbee.web.resource;

import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailService;
import com.novamens.thumbnail.ThumbnailSize;

public class ResourceThumbnailImage<T extends Content> extends Image {
	private static final long serialVersionUID = 1L;

//	private static Logger logger = Logger.getLogger(ResourceThumbnailImage.class.getName());
	
	private IModel<T> model_content;
	private IModel<Resource> model_resource;
	private ThumbnailSize size;

	public ResourceThumbnailImage(String id, IModel<T> model_content,  IModel<Resource> model_resource) {
		this(id, model_content, model_resource, ThumbnailSize.MINI);
	}
	
	public ResourceThumbnailImage(String id, IModel<Resource> model_resource, ThumbnailSize size) {
		this (id, null, model_resource, size);
	}
	
	public ResourceThumbnailImage(String id, IModel<T> model_content,  IModel<Resource> model_resource, ThumbnailSize size) {
		super(id);
		this.model_content=model_content;
		this.model_resource=model_resource;
		this.size=size;
	}
	
	public Resource getResource() {
		return model_resource.getObject();
	}
	
	public ThumbnailSize getSize() {
		return size;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		setImageResourceReference(getResourceReference());
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		if (model_content!=null)
			this.model_content.detach();
		if (model_resource!=null)
			this.model_resource.detach();
	}
	
	@Override
	protected boolean shouldAddAntiCacheParameter()	{
		return false;
	}
	
	protected ResourceReference getResourceReference() {
		ResourceReference imagereference = null;
//		if (model_resource.getObject() instanceof ExternalResource) {
			if (model_content!=null)
				imagereference = new WebThumbnailReference(model_resource.getObject(), model_content.getObject(), size);
			else
				imagereference = new WebThumbnailReference(model_resource.getObject(), size);
//		}
//		else {
//			if (model_content!=null)
//				imagereference = new WebThumbnailReference(model_resource.getObject(), model_content.getObject(), size);
//			else
//				imagereference = new WebThumbnailReference(model_resource.getObject(), size);
//		}
		return imagereference;
	}
	
	protected String getThumbnail(Resource file) {
		if (file!=null && file.getUrl()!=null && (file.getUrl().startsWith("https://www.youtube.com") || file.getUrl().startsWith("https://youtu.be"))) {
			return ServiceLocator.getService(ThumbnailService.class).getDefaultThumbnail("video");
		}
		else {
			return ServiceLocator.getService(ThumbnailService.class).getDefaultThumbnail("link");
		}
	}
}