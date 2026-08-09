package kbee.web.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.novamens.content.base.Resource;
import com.novamens.content.service.ResourceService;
import com.novamens.thumbnail.ThumbnailSize;

public class WebThumbnailResourceResponse extends AbstractResourceResponse {

	public WebThumbnailResourceResponse(URI uri) {
		super(uri);
	}

	public long getContentLength() {
		try {
			if (super.getContentLength()>0) {
				return super.getContentLength();
			}	
			File thumbnailfile = getResource().getService(ResourceService.class).getThumbnailFile(getSize());
			long length = thumbnailfile!=null ? thumbnailfile.length() : 0;
			setContentLength(length);
			return length;
		}
		catch (IOException e) {
			logger.debug(e);
			if (logger.isDebugEnabled()) {
				if (getURI()!=null) {
					logger.debug(" URI -> " + getURI());
				}
			}
					
			throw new ResourceException(e);
		}
	}

	public ThumbnailSize getSize() {
		String name = getURI().getName();
		String segments[] = name.split("/");
		String sizesegments[] = segments[0].split("-");
		ThumbnailSize size = ThumbnailSize.valueOf(sizesegments[1].toUpperCase());
		return size;
	}
	
	public String getResourceId() {
		String name = getURI().getName();
		String segments[] = name.split("/");
		String id = null;
		if (segments.length>0 && segments[0]!=null && segments[0].toLowerCase().contains("thumbnail")) {
			id = segments[1];
		}
		return id;
	}
	
	@Override
	public InputStream openStream() throws IOException {
		return getResource().getService(ResourceService.class).getThumbnail(getSize());
	}
	
	public String getContentType() {
		return "image/jpg";
	}

	@Override
	protected Resource loadResource() throws IOException {
		return getResource(getResourceId());
	}
}