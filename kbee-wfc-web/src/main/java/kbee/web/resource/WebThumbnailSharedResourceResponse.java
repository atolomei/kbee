package kbee.web.resource;

import com.novamens.content.service.TokenService;
import com.novamens.dom.Json;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailSize;

public class WebThumbnailSharedResourceResponse extends WebThumbnailResourceResponse {

	public WebThumbnailSharedResourceResponse(URI uri) {
		super(uri);
	}

	public ThumbnailSize getSize() {
		Json token = getToken();
		if (token==null) return null;
		ThumbnailSize size = ThumbnailSize.valueOf((String)token.get("size"));
		return size;
	}
	
	public String getResourceId() {
		Json token = getToken();
		if (token==null) return null;
		return (String)token.get("resource");
	}
	
	public Json getToken() {
		String name = getURI().getName();
		String segments[] = name.split("/");
		if (segments.length<2) return null;
		String tokenstring = segments[1];
		Json token = ServiceLocator.getService(TokenService.class).decode(tokenstring);
		return token;
	}
}