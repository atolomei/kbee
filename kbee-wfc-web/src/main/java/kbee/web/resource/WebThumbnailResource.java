package kbee.web.resource;

public class WebThumbnailResource extends WebResource {
	private static final long serialVersionUID = 1L;

	public WebThumbnailResource(URI uri) {
		super(uri);
	}

	@Override
	protected AbstractResourceResponse newResourceResponse()	{
		return new WebThumbnailResourceResponse(getUri());
	}
}