package com.novamens.portal.model.diagrammablesite;

import com.novamens.security.PersistentEnum;

public enum PopulationMode implements PersistentEnum {

		HQL 			(3, "Hibernate Query Language"), 
		CLASSIFICATION  (2, "Classification"),
		MANUAL	 		(1, "Manual");

		private String label;
		private int id;
															
		private PopulationMode(int code, String label) 		{this.label = label;this.id = code;}
		public String toString() 							{return ("id: " + getId() + "  label: "+ getLabel());} 
		public String getLabel() 							{return label;}
		public int getId() 									{return id;}
}
