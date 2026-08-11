package kbee.translation;

import java.util.Locale;

public interface Dictionary {
	public Locale getLocale();
	public String get(String key);
}
