package com.novamens.content.base;

import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;

public enum DisplayMode implements PersistentEnum {
 
		IMAGE				(1, "thumbnail-large", "thumbnail-large", 260), 	
		THUMBNAIL			(2, "thumbnail-medium", "thumbnail-medium", 120),
		ICON 				(3, "icon", "icon", 25),
		THUMBNAIL_CONSOLE	(4, "thumbnail-small", "thumbnail-medium", 65),
		ICON_METADATA	    (5, "icon-metadata", "icon-metadata", 25),;
		
		private String label;
		private String css;
		private int id;
		public String height;
		public String width;
		
		private  DisplayMode(int code, String label, String css, int height) {
			this.label = label;this.id = code; this.css=css;
			this.height = String.valueOf(height);
			Integer w =  (Integer) (Integer.valueOf(height)*4/3);
			this.width= w.toString();
		}
		
		public String getHeight() {
			return height;
		}
		
		public String getWidth() {
			return width;
		}
		
		public String toString() {
			return ("id: " + getId() + "  label: "+ getLabel() + "  css: "+ getCss());
		}
		
		public String getLabel() {
			return getLabel(Locale.getDefault());
		}
		
		
		public String getLabel(Locale locale) {			
			ResourceBundle res = ResourceBundle.getBundle(DisplayMode.this.getClass().getName(), locale);
			return res.getString(this.label);
		}
		
		public String getCss() {
			return css;
		}
		
		public int getId() {
			return id;
		}
}
