package com.novamens.text;

import java.io.Serializable;
import java.util.List;

import com.novamens.content.model.Multiplicity;

public interface TemplateModelInfo extends Serializable {

	public enum ModelType {
		
		ACTIVITY ("Activity"), 
		COMPOUND ("Compound"),
		CONTENT ("Content"),
		DATE ("Date", "https://freemarker.apache.org/docs/ref_builtins_date.html"),
		DEVICE ("Device"),
		DOMAIN ("Domain"),
		NUMBER ("Number", "https://freemarker.apache.org/docs/ref_builtins_number.html"),
		PERSON ("Person"),
		PROCEDURE ("Procedure"),
		RELATED ("Related"),
		RELATION ("Relation"),
		RESOURCE ("Resource"),
		SIGNATURE ("Signature"),
		SIGNED ("Signed"),
		STRING ("String", "https://freemarker.apache.org/docs/ref_builtins_string.html"),
		TASK  ("Task"), 
		TEMPLATE ("Template"), 
		USER ("User"), 
		VALUE ("Value");
		
		private String label;
		private String modifiers;
		private boolean canonical = false;
		
		public String getLabel() {
			return label;
		}
		public String getModifiers() {
			return modifiers;
		}
		public boolean isCanonical() {
			return canonical;
		}
		private ModelType(String label) {
			this.label = label;
		}
		private ModelType(String label, String modifiers) {
			this.label = label;
			this.modifiers = modifiers;
			this.canonical = true;
		}
	}	

	public String getName();
	public ModelType getType();
	public String getDescription();
	public String getDataSet();
	public String getTemplate();
	public Multiplicity getMultiplicity();
	public List<TemplateModelInfo> getElements();
}
