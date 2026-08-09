package kbee.web.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class HTMLUtils {
	
						
	static public String cleanUpHTMLText(String text) {
	
		if (text==null)
			return null;
		
		// Replace empty Paragraphs
		String str = text.replaceAll("(\\r\\n<p>)(\\W)*(</p>)", "" );
		str = str.replaceAll("(<p\\s*(class=\".+\")*\\s*>(&nbsp;\\s*)*</p>)","");
		str = str.replaceAll("\\r\\n", "" );
		str = str.replaceAll("<p class=\"last\">", "<p>");
		
		if (str.matches(".+([^\\w]</p>)$")) {
				return replaceLast(str, "<p>", "<p class= \"last\">");
			}
		
		return str;
	}


	static private String replaceLast(String string, String toReplace, String replacement) {
	
	if (string==null)
		return null;

	Pattern p = Pattern.compile(replacement);
	Matcher m = p.matcher(string);
	if (m.find()) 
		return string;
	int pos = string.lastIndexOf(toReplace);
    if (pos > -1) {
        return string.substring(0, pos)
             + replacement
             + string.substring(pos + toReplace.length(), string.length());
    } else {
        return string;
    }
}










}
