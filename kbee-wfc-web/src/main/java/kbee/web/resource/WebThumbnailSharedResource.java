package kbee.web.resource;

public class WebThumbnailSharedResource extends WebResource {
	private static final long serialVersionUID = 1L;

	public WebThumbnailSharedResource(URI uri) {
		super(uri);
	}

	@Override
	protected AbstractResourceResponse newResourceResponse()	{
		return new WebThumbnailSharedResourceResponse(getUri());
	}
}