package kbee.web.branding;

import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

public class LoginImageWrapper {

	public String button_style;
	public String text_style;
	
	public String title;
	public String text;
	public String url;
	
	public ResourceReference resource;
	
	public LoginImageWrapper(ResourceReference resource) {
		this(resource, null, null, null);
	}
	
	public LoginImageWrapper(ResourceReference resource, String text, String text_style) {
			this(resource, text, text_style, null);
		
	}
	public LoginImageWrapper(ResourceReference resource, String text, String text_style, String button_style) {

		this.button_style=button_style;
		this.text_style=text_style;
		this.resource= resource;
	}
	
	public ResourceReference getResource() {
		return resource;
	}
	
	public String getStyle() {
		return this.text_style;
	}

	public String getButtonStyle() {
		return button_style;
	}
}
