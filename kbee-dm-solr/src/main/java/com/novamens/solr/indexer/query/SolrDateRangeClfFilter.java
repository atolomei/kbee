package com.novamens.solr.indexer.query;

import com.novamens.indexer.query.Filter;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 
 * [] means:
 *  >= lower bound
 *  <= upper bound
 *
 */
public class SolrDateRangeClfFilter implements Filter {
	private static final long serialVersionUID = 1L;
	//private long amount;
	private String name, display;
	//private ChronoUnit unit;
	private String value;


	public SolrDateRangeClfFilter(String name, OffsetDateTime from, OffsetDateTime to,boolean ignoreHour) {
		this(name,null,from,to,ignoreHour);
	}
	public SolrDateRangeClfFilter(String name, String display, OffsetDateTime from, OffsetDateTime to,boolean ignoreHour) {
		this.name = name;
		String format="YYYY/MM/dd'T'HH:mm:ss'Z'";
		if(ignoreHour){
			format="YYYY/MM/dd";
		}

		if (display == null) {
			String fromlabel = from!=null ? DateTimeFormatter.ofPattern(format).format(from) : null;
			String tolabel = to!=null ? DateTimeFormatter.ofPattern(format).format(to) : null;
			if (fromlabel==null)
				display ="To " + tolabel;
			else
			if (tolabel==null)
				display ="From " + fromlabel;
			else
				display = fromlabel + " To " + tolabel;
		}
		this.display  = display;
		String fromstr = from!=null ? DateTimeFormatter.ofPattern(format).format(from) : " * ";
		String tostr = to!=null ? DateTimeFormatter.ofPattern(format).format(to) : " * ";
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
