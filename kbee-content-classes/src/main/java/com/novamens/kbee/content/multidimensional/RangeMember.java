package com.novamens.kbee.content.multidimensional;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.multidimensional.SolrMember;

public class RangeMember extends SolrMember {
	private static final long serialVersionUID = 1L;

	public RangeMember(String facet, Date from, Date to) {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat displayformat = new SimpleDateFormat(getDefaultDatePattern());
		String criteria = "[" + format.format(from) + "T00:00:00.000Z TO " + format.format(to) + "T00:00:00.000Z]"; 
		setFacet(facet);
		String displayname = displayformat.format(from) + " - " + displayformat.format(to);
		setDisplayName(displayname);
		setPath(facet+"/"+criteria);
	}
	
	protected String getDefaultDatePattern() {
		// String pattern = "es".equals(WebSession.get().getLocale().getLanguage()) ? "dd/MM/yyyy" : "MM/dd/yyyy";
		try {
			String pattern = "es".equals(ServiceLocator.getService(SecurityService.class).getSessionUser().getLocale().getLanguage()) ? "dd/MM/yyyy" : "MM/dd/yyyy";
		return pattern;
		} catch (Exception e) {
			return "MM/dd/yyyy";
		}
	}
};