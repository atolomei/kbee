package com.novamens.thumbnail;

import java.io.Serializable;

import com.novamens.security.PersistentEnum;

public enum ThumbnailSize implements PersistentEnum, Serializable {
	
    SMALL 		(1, "small", "width", 320, false),  // Fija width en 236 y el alto libre 
	MEDIUM 		(2, "medium", 280), 				// fija alto en 280 y el ancho en alto x 4/3
	LARGE 		(3, "large", 480), 					// fija alto en 480 y el ancho en alto x 4/3 
	W980		(4, "W980", "width", 980),			// fija ancho en 980 y el ancho en alto x 4/3
	MINI		(5, "mini", 82, 82),				// fija alto y ancho en 80, el thumbnail hace el resizing óptimo para que ocupe la mayor superficie posible del 80x80
															
					
	AVATAR_STATUS (6, "avatar_status", 20, 20);				// fija alto y ancho en 80, el thumbnail hace el resizing óptimo para que ocupe la mayor superficie posible del 240x24

	
	private String label;
	private int id;
	public int height=0;
	public int width=0;
	
	public int getHeight() {return height;}
	public int getWidth() {return width;}
																		
	private  ThumbnailSize(int code, String label, String dim, int value, boolean frame) {
		this.label = label;
		this.id = code;
		if (dim.toLowerCase().trim().equals("width")) {
			this.width=value; 
			if (frame)
				this.height=(int)(value*3/4);
			else
				this.height=value;
		}
		else { 
			this.height=value; 
			if (frame)
				this.width=(int)(height*4/3);
			else
				this.width=value;
		}
	}

	private  ThumbnailSize(int code, String label, String dim, int value) {
		this.label = label;
		this.id = code;
		if (dim.toLowerCase().trim().equals("width")) {
			this.width=value; 
			this.height=(int)(value*3/4);
		}
		else { 
			this.height=value; 
			this.width=(int)(height*4/3);
		}
	}
	
	private  ThumbnailSize(int code, String label, int width, int height) {
		this.label = label;
		this.id = code;
		this.height=height; 
		this.width=width;
	}
	
	private  ThumbnailSize(int code, String label, int height) 
	{
		this.label = label;
		this.id = code;
		this.height=height; 
		this.width=(int)(height*4/3);
	}
	public String toString() {return ("id: " + getId() + "  label: "+ getLabel());} 
	public String getLabel() {return label;}
	public int getId() {return id;}
}
