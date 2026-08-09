package kbee.web.help;

import java.util.Map;

public class InlineHelpContent {

	public String key;
	public Map<String, String> context;
	
	 public InlineHelpContent(String key, Map<String, String> context) {
	 }
	
	 public String getHTMLContent() {
		return "<p>este es el texto</p>";
	 }
	 
}
