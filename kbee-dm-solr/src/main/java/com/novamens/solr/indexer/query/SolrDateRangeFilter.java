package com.novamens.solr.indexer.query;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.novamens.content.model.Attribute;
import com.novamens.indexer.query.Filter;

/**
 * 
 * [] means:
 *  >= lower bound
 *  <= upper bound
 *
 */
public class SolrDateRangeFilter implements Filter {
	private static final long serialVersionUID = 1L;
	
	private String name, display;
	private String value;
	
	public SolrDateRangeFilter(String name, String display, long amount, ChronoUnit unit) {
		this.name = name;
		this.display = display;
		OffsetDateTime now = OffsetDateTime.now();
		OffsetDateTime from = now.minus(amount, unit);
		String fromstr = DateTimeFormatter.ofPattern("YYYY-MM-dd'T'hh:mm:ss'Z'").format(from);
		String value = "["+fromstr+" TO NOW]";
		this.value = value;
	}
	
	public SolrDateRangeFilter(String name, OffsetDateTime from, OffsetDateTime to) {
		this(name,null,from,to);
	}
	
	public SolrDateRangeFilter(Attribute attribute, OffsetDateTime from, OffsetDateTime to) {
		this(attribute.getUniqueName()+"name", null, from, to);
	}
	
	public SolrDateRangeFilter(String name, String display, OffsetDateTime from, OffsetDateTime to) {
		this.name = name;
		if (display == null) {
			String fromlabel = from!=null ? DateTimeFormatter.ofPattern("MM-dd-YYYY").format(from) : null;
			String tolabel = to!=null ? DateTimeFormatter.ofPattern("MM-dd-YYYY").format(to) : null;
			if (fromlabel==null)
				display ="To " + tolabel;
			else
				if (tolabel==null)
					display ="From " + fromlabel;
				else
					display = fromlabel + " To " + tolabel;
		}
		this.display  = display;
		String fromstr = from!=null ? DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm:ss'Z'").format(from) : " * ";
		String tostr = to!=null ? DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm:ss'Z'").format(to) : " * ";
		String value = "["+fromstr+" TO "+tostr+"]";
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDisplayName() {
		return display;
	}
	
	public Serializable getValue() {
		return value;
	}
	
	public String getDisplayValue() {
		return display;
	}
	
	public String getClause() {
		return name + ":" + getValue();
	}
}
