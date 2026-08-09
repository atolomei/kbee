package kbee.web.resource;

public class WebPdfFileResource extends WebFileResource {
	private static final long serialVersionUID = -1L;

	public WebPdfFileResource(URI uri) {
		super(uri);
	}

	public static WebPdfFileResource get(URI uri) {
		return new WebPdfFileResource(uri);
	}
	
	protected ResourceResponse getResponse() {
		return new WebPdfFileResourceResponse(getUri());
	}
}