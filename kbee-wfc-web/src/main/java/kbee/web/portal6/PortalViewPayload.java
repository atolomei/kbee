package kbee.web.portal6;

import java.util.Locale;
import java.util.ResourceBundle;

public enum PortalViewPayload {

	
	RENDER_PAYLOAD 		(1, "render", 				"render", 					"fa-check-circle"), 
	NOT_PAYLOAD 		(2, "donotrender", 				"donotrender", 			"fal fa-archive");
	
	
		
	private String label;
	private int id;
	private String css;
	private String icon;
	
	/**
	 * 
	 * Site 			*****
	 * Page 			****
	 * PageSection 		***
	 * Area 			**
	 * Block 			*
	 * 
	 * 
	 * @param code
	 * @param label
	 * @param css
	 * @param icon
	 */
	private PortalViewPayload(int code, String label, String css, String icon) {
		this.label = label;
		this.id = code; 
		
		this.css=css;
		this.icon=icon;
	}
	
	public String toString() {
		return ("id: " + getId() + ". label: "+ getLabel()) + ". css: "+getCss();
	}
	
	public String getCode() {
		return label;
	}

	
	public String getIcon() {
		return this.icon;
	}
	
	public String getDisplayName() {
		return getLabel();
	}
	
	public String getLabel() {
		return getLabel(Locale.getDefault());
	}
	
	public String getCss()	{
		return css;
	}
	
	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(PortalViewPayload.this.getClass().getName(), locale);
		return res.getString(this.label);
	}
	
	public int getId() {
		return id;
	}

	public String getHTMLLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(PortalViewPayload.this.getClass().getName(), locale);
		return  "<span class=\"" + getCss() + "\">" + res.getString(this.label) + "</span>";
	}
}
