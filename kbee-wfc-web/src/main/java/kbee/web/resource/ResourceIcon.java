package kbee.web.resource;

import org.apache.wicket.markup.html.image.Image;

import com.novamens.content.base.Resource;
import com.novamens.content.resource.ExternalResource;
import com.novamens.wicket.util.FileUtil;

public class ResourceIcon extends Image {
	
	private static final long serialVersionUID = 1L;
	
	public ResourceIcon(String id, Resource resource) {
		super(id);

		if(resource instanceof ExternalResource) {
			if (resource !=null && resource.getUrl()!=null && (resource.getUrl().startsWith("https://www.youtube.com") || resource.getUrl().startsWith("https://youtu.be")))
				setImageResourceReference(FileUtil.getResourceIconByKey("video"));
			else
				setImageResourceReference(FileUtil.getResourceIconByKey("link"));
		}
		else
			setImageResourceReference(FileUtil.getResourceIcon(resource.getName()));
	}

	@Override
	protected boolean shouldAddAntiCacheParameter()	{
		return false;
	}
}
