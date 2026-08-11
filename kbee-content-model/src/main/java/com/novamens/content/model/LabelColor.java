package com.novamens.content.model;

 
import java.util.Locale;
import java.util.ResourceBundle;

import com.novamens.security.PersistentEnum;

public enum LabelColor implements PersistentEnum {
	
	PURPLE(1, "purple"),
	BLUE(2, "blue"),
	GREEN(3, "green"),
	YELLOW(4, "yellow"),
	ORANGE(5, "orange"),
	RED(6, "red"),
	PINK(7, "pink"),
	BROWN(8, "brown"),
	GRAY(9, "gray"),
	LIGHTBLUE(10, "lightblue"),
	CHOCOLATE(11, "chocolate"),
	CADETBLUE(12, "cadetblue"),
	CRIMSON(13, "crimson"),
	CORNFLOWERBLUE(14, "cornflowerblue"),
	DARK_BLUE(15, "darkblue"),
	DARK_CYAN(16, "darkcyan"),
	DARK_GRAY(17, "darkgray"),
	DARK_GREEN(18,"darkgreen"),
	DARK_MAGENTA(19,"darkmagenta"),
	DARK_ORANGE(20, "darkorange"),
	DARK_SEA_GREEN(21, "darkseagreen"),
	FORESTGREEN(22, "forestgreen"),
	BLACK(23, "black");
	
	
	private String label;
	private int id;
			
	private LabelColor(int code, String label) {
		this.label = label;
		this.id = code;
	}
	
	public String toString() {
		return ("id: " + getId() + "  label: "+ getLabel());
	}
	
	
	public String getLabel(Locale locale) {
		ResourceBundle res = ResourceBundle.getBundle(LabelColor.class.getName(), locale);
		return res.getString(this.label);
	}
	
	public String getLabel() {
		ResourceBundle res = ResourceBundle.getBundle(LabelColor.class.getName(), Locale.getDefault());
		return res.getString(this.label);
	}

	public String getKey() {
		return this.label;
	}
	public int getId() {
		return id;
	}
	
	static private final LabelColor labels[] = {	
			PURPLE,
			BLUE,
			GREEN,
			YELLOW,
			ORANGE,
			RED,
			PINK,
			BROWN,
			GRAY,
			LIGHTBLUE,
			CHOCOLATE,
			CADETBLUE,
			CRIMSON,
			CORNFLOWERBLUE,
			DARK_BLUE,
			DARK_CYAN,
			DARK_GRAY,
			DARK_GREEN,
			DARK_MAGENTA,
			DARK_ORANGE,
			DARK_SEA_GREEN,
			FORESTGREEN,
			BLACK
	};
	 
	static public LabelColor get(int n) {
		return labels[n%labels.length]; 
	}

	 
			
	
	public static final LabelColor[] getAll() {
		return labels;
	}
}
