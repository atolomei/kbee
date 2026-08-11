package com.novamens.portal.model.diagrammablesite;

import com.novamens.security.PersistentEnum;

public enum SitePermission implements PersistentEnum {

	// los permisos son acumulativos, por ejemplo Write tiene
	// tambien Read, Diagram tiene tambien R y W, y Admin tiene
	// todos. Al asignar se pone un entero qe es la suma de todos los permisos que tiene
	// luego para chequearlo se debe... 
		
	READ 		(1, "Read"), 
	WRITE 		(3, "Write"),
	DIAGRAM 	(7, "Diagram"),
	ADMIN		(15, "Admin");

	private String label;
	private int id;
														
	private  SitePermission(int code, String label) 	{this.label = label;this.id = code;}
	public String toString() 							{return ("id: " + getId() + "  label: "+ getLabel());} 
	public String getLabel() 							{return label;}
	public int getId() 									{return id;}
}


