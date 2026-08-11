package com.novamens.content.text;

public interface Text {
	public String asString();
	public String getUri(String attach);
	public String getText(AncordResolver ancordResolver);
	public String getText(AncordResolver ancordResolver, ImageResolver imageResolver);
}