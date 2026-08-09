package kbee.web.util;

import com.novamens.content.base.Content;
import com.novamens.content.base.ResourceContainer;

public class UriHelper {
	
	private static UriHelper Instance;
	
	
	public static UriHelper getInstance() {
		if (Instance==null) 
			Instance = new UriHelper();
		return Instance;
	}
	
	public String getUri(Object object) {
		return null;
	}
	
	public String getPrintUri(Object object) {
		return null;
	}
	
	public String getName(Content content) {
		StringBuilder str = new StringBuilder();
		str.append((content.getOId()!=null ? content.getOId() : content.getId()) + "-" + content.getId());
		if (content.getTitle()!=null) 
			str.append("-" + getTitle(content));
		return str.toString();
	}
	
	public String getName(ResourceContainer container) {
		StringBuilder str = new StringBuilder();
		str.append((container.getId()!=null ? container.getId() : container.getId()) + "-" + container.getId());
		if (container.getTitle()!=null) 
			str.append("-" + getTitle(container));
		return str.toString();
	}

	public String getId(String name) {
		String segments[] = name.split("-");
		if (segments.length==2 || segments.length==3) {
			String id = segments[1];
			if (isNumeric(id)) {
				return id;
			}
		}
		return null;
	}
	
	public String getTitle(ResourceContainer container) {
		String title = container.getTitle();
		return getTitle(title);
	}
	
	public String getTitle(Content content) {
		String title = content.getTitle();
		return getTitle(title);
	}
	
	public String getTitle(String title) {
		
		if (title==null)
			return "err";
		
		StringBuilder text = new StringBuilder();
		title = title.toLowerCase();
		
		title = title.replace("á", "a");
		title = title.replace("é", "e");
		title = title.replace("í", "i");
		title = title.replace("ó", "o");
		title = title.replace("ú", "u");
		title = title.replace("ñ", "n");
		title = title.replace("<br/>", "");
		title = title.replace("<br>", "");
		
		// Se pasa dos veces para los casos donde estan consecutivos dos términos a eliminar: ej. " el que "
		title = title.replaceAll(" el | que | para | se | de | en | una | sin | las | del | por | con | la | lo | los | si ", " ");
		title = title.replaceAll(" el | que | para | se | de | en | una | sin | las | del | por | con | la | lo | los | si ", " ");
		
		String words[] = title.split("\\s+");
		
		boolean end = false;
		for (int w=0; w<words.length && !end; w++) {
		
			if (text.length()>0) 
				text.append("_");
			
			String word = words[w];
			text.append(word);
		
			if (w>=4)
				end = true;
			
		}
		return text.toString();
	}
	
	public static boolean isNumeric(String str) {  
		try	{
			Long.parseLong(str);  
		}
		catch(NumberFormatException nfe) {  
			return false;  
		} 
		return true;  
	}
}
