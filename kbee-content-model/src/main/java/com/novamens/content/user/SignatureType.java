package com.novamens.content.user;

import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;

public enum SignatureType implements PersistentEnum {
 
		PHONE_APP			(1, "phone"), 	// Firma desde android con certificados y claves kbee
		SMS					(2, "sms");       // Firma mediante un SMS al telefono de la persona
		
		private int id;
		private String label;
		
		private  SignatureType(int code, String label) {
			this.label = label;
			this.id = code; 
		}
		
		public String toString() {
			return ("id: " + getId() + "  label: "+ getLabel());
		}
		
		public String getLabel() {
			return getLabel(Locale.getDefault());
		}
		
		public String getLabel(Locale locale) {			
			ResourceBundle res = ResourceBundle.getBundle(SignatureType.this.getClass().getName(), locale);
			return res.getString(this.label);
		}
		
		public int getId() {
			return id;
		}
}