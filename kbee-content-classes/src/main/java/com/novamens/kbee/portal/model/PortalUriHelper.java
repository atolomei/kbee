package com.novamens.kbee.portal.model;


import com.novamens.content.base.Content;
import com.novamens.content.base.ResourceContainer;
import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.Site;

import kbee.util.PropertiesFactory;

/**
 * 
 */

@Deprecated
public class PortalUriHelper {

	private static PortalUriHelper Instance;

	private String xserver = PropertiesFactory.getInstance("kbee").getProperties().getProperty("server",
			"http://localhost");
	private String xport = PropertiesFactory.getInstance("kbee").getProperties().getProperty("port", "8089");

	private String vanity_server = PropertiesFactory.getInstance("kbee").getProperties().getProperty("server", xserver);
	private String vanity_port = PropertiesFactory.getInstance("kbee").getProperties().getProperty("port", xport);

	private String baseurl = PropertiesFactory.getInstance("kbee").getProperties()
			.getProperty("com.novamens.kbee.portal.baseurl", "portal");

	public static PortalUriHelper getInstance() {
		if (Instance == null)
			Instance = new PortalUriHelper();
		return Instance;
	}

	/**
	 * URL relativa del nodo raiz del portal
	 */
	public String getBaseURL() {
		return baseurl;

	}

	/**
	 * URL absoluta del nodo raiz del portal
	 * 
	 * @return
	 */
	public String getPortalURL(String domain_name) {
		return vanity_server.replace("${domain}", domain_name)
				+ (vanity_port.length() == 0 || vanity_port.equals("80") ? "" : (":" + vanity_port)) + "/"
				+ getBaseURL() + "/";
		// return vanity_server + ((vanity_port!=null &&
		// !vanity_port.equals("80"))?":"+vanity_port:"")+"/"+getBaseURL()+"/";
	}

	/**
	 * @param name
	 * @return
	 */
	public String getTitleName(String name) {
		return get_title(name);
	}

	public String getTitle(ResourceContainer container) {
		String title = container.getTitle();
		return get_title(title);
	}

	public String getTitle(String title) {
		return get_title(title);
	}

	public String getName(Content content) {
		StringBuilder str = new StringBuilder();
		str.append((content.getOId() != null ? content.getOId() : content.getId()) + "-" + content.getId());
		if (content.getTitle() != null)
			str.append("-" + getTitle(content));
		return str.toString();
	}

	public String getName(ResourceContainer container) {
		StringBuilder str = new StringBuilder();
		str.append((container.getId() != null ? container.getId() : container.getId()) + "-" + container.getId());
		if (container.getTitle() != null)
			str.append("-" + getTitle(container));
		return str.toString();
	}

	/**
	 * @param name
	 * @return
	 */
	public String getId(String name) {
		String segments[] = name.split("-");
		if (segments.length == 2 || segments.length == 3) {
			String id = segments[1];
			if (isNumeric(id)) {
				return id;
			}
		}
		return null;
	}

	public String getTitle(Site site) {
		String title = site.getName();
		return get_title(title);
	}

	public String getTitle(Page page) {
		String title = page.getTitle();
		return get_title(title);
	}

	public String getTitle(Content content) {
		String title = content.getTitle();
		return get_title(title);
	}

	private String get_title(String title) {
		StringBuilder text = new StringBuilder();
		title = title.toLowerCase();
		title = title.replace("á", "a");
		title = title.replace("é", "e");
		title = title.replace("í", "i");
		title = title.replace("ó", "o");
		title = title.replace("ú", "u");
		title = title.replace("ñ", "n");
		title = title.replace("ü", "u");
		title = title.replaceAll("[°,¡!?¿:\\/\"-().]|<br>", "");
		title = title.replace("<br>", "");

		// Se pasa dos veces para los casos donde estan consecutivos dos términos a
		// eliminar: ej. " el que "
		//
		//
		title = title.replaceAll(
				" el | que | para | se | de | en | una | sin | las | del | por | con | la | lo | los | si ", " ");
		title = title.replaceAll(
				" el | que | para | se | de | en | una | sin | las | del | por | con | la | lo | los | si ", " ");

		String words[] = title.split("\\s+");

		boolean end = false;
		for (int w = 0; w < words.length && !end; w++) {
			if (text.length() > 0)
				text.append("_");
			String word = words[w];
			text.append(word);
			if (w >= 4)
				end = true;
		}
		return text.toString();
	}

	public static boolean isNumeric(String str) {
		try {
			Long.parseLong(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
}
