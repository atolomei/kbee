package com.novamens.kbee.content.multidimensional;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.lucene.search.BooleanQuery;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.IntervalFacet;
import org.springframework.util.Assert;

import com.novamens.indexer.query.Member;
import com.novamens.indexer.query.ResultSet;

import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.multidimensional.SolrFacet;
import com.novamens.solr.indexer.multidimensional.SolrMember;
import com.novamens.solr.indexer.query.SolrResultSet;

@SuppressWarnings("serial")
public class SolrDateFacet extends SolrFacet implements Serializable {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SolrDateFacet.class.getName());
	
	private String months[] = {"", "january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december" };
	
	public class RangeMember extends SolrMember implements com.novamens.indexer.query.RangeMember {
		private DateRange range;
		public RangeMember(String facet, Date from, Date to) {
			DateFormat format = new SimpleDateFormat("yyyy-MM-ddThh:mm:ssZ");
			String criteria = "[" + format.format(from) + " TO " + format.format(to) + "]"; 
			setDisplayName("Date Range");
			setPath(facet+"/"+criteria);
		}
		public RangeMember(String facet, DateRange range) {
			setFacet(facet);
			String criteria = range.criteria().replace("(", "[");
			criteria = criteria.replace(")", "]");
			criteria = criteria.replace(",", " TO ");
			setPath(facet+"/"+criteria);
			setDisplayName(getLabel(range));
			this.range = range;
		}
		public DateRange getRange() {
			return range;
		}
	};
	
	public class MonthMember extends SolrMember {
		private int month;
		public MonthMember(String facet, int year, int month) {
			setFacet(facet);
			String criteria = "["+String.valueOf(year)+"-"+String.valueOf(month)+"-01T00:00:00.000Z";
			criteria += " TO " + String.valueOf(year) +"-"+String.valueOf(month)+"-"+String.valueOf(lastDay(year, month)) + "T23:59:59.999Z]";
			setPath(facet+"/"+criteria);
			setDisplayName(getLabel(months[month]));
			setParent(new YearMember(facet, year));
			this.month = month;
		}
		private int lastDay(int year, int month) {  
	        Calendar calendar = Calendar.getInstance();  
	        calendar.set(Calendar.YEAR, year);
	        calendar.set(Calendar.MONTH, month-1);
	        calendar.set(Calendar.DAY_OF_MONTH, 1);
	        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	    }  
		public int getMonth() {
			return month;
		}
	};
	
	public class YearMember extends SolrMember {
		private int year;
		public YearMember(String facet, int year) {
			setFacet(facet);
			String criteria = "["+String.valueOf(year)+"-01-01T00:00:00.000Z";
			criteria += " TO " + String.valueOf(year) +"-12-31T23:59:59.999Z]";
			setPath(facet+"/"+criteria);
			setDisplayName(String.valueOf(year));
			this.year = year;
		}
		public int getYear() {
			return year;
		}
	}	
	
	public enum DateRange {
		MINUTES ("minutes", "(NOW/MINUTE-5MINUTE, NOW/MINUTE+1MINUTE)", 0),
		HOUR ("hour", "(NOW/HOUR-1HOUR, NOW/HOUR+1HOUR)", 1),
		TODATE ("todate", "(NOW/DAY-1DAY, NOW/DAY+1DAY)",2),
		WEEK ("week", "(NOW/DAY-7DAY, NOW/DAY+1DAY)", 3),
		MONTH ("month", "(NOW/DAY-30DAY, NOW/DAY+1DAY)", 4),
		ALL ("all", "(NOW/MONTH-240MONTH, NOW/MONTH)", 5);
		
		private long duration;
		private String id;
		private String criteria;
		private DateRange(String id, String criteria, long duration) {
			this.id = id;
			this.criteria = criteria;
		}
		public String id() {
			return this.id;
		}
		public String toString() {
			return this.id;
		}
		public String criteria() {
			return this.criteria;
		}
		public long duration() {
			return this.duration;
		}
	}

	public SolrDateFacet() {
		super.setDisplayName("Date");
	}

	public List<Member> getMembers(ResultSet resultSet, int maxmembers) {
		List<Member> members = new ArrayList<Member>();
		
		Assert.isInstanceOf(SolrResultSet.class, resultSet);
		
		//QueryResponse response = ((SolrResultSet)resultSet).getQueryResponse();
		QueryResponse response = getMembersResponse(resultSet);
		
		if (response==null) return members;
		
		List<IntervalFacet> intervals = response.getIntervalFacets();
		for (IntervalFacet interval : intervals) {
			if (interval.getField().equals(getName()))
			for (org.apache.solr.client.solrj.response.IntervalFacet.Count count : interval.getIntervals()) {
				if 		(count.getKey().equals(DateRange.MINUTES.criteria()) && count.getCount()>0) {members.add(getMember(count, DateRange.MINUTES));}
				else if (count.getKey().equals(DateRange.HOUR.criteria()) && count.getCount()>0) 	{members.add(getMember(count, DateRange.HOUR));}
				else if (count.getKey().equals(DateRange.TODATE.criteria()) && count.getCount()>0)  {members.add(getMember(count, DateRange.TODATE));}
				else if (count.getKey().equals(DateRange.WEEK.criteria()) && count.getCount()>0)    {members.add(getMember(count, DateRange.WEEK));}
				else if (count.getKey().equals(DateRange.MONTH.criteria()) && count.getCount()>0)   {members.add(getMember(count, DateRange.MONTH));}
			}
		}
		
		@SuppressWarnings("rawtypes")
		List<RangeFacet> ranges = response.getFacetRanges();
		Map<Integer, Integer> yearsmap = new HashMap<Integer, Integer>();
		if (!ranges.isEmpty()) {
			for (RangeFacet<?,?> range : ranges) {
				if (range.getName().equals(getName())) {
					for (org.apache.solr.client.solrj.response.RangeFacet.Count count : range.getCounts()) {
						String month = count.getValue().substring(0, 7);
						try {
							Integer year = Integer.valueOf(month.substring(0, 4));
							Integer yearcount = yearsmap.get(year);
							if (yearcount==null) {
								yearcount = count.getCount(); 
							}
							else {
								yearcount = count.getCount() + yearcount; 
							}
							yearsmap.put(year, yearcount);
						}
						catch (Exception e) {
							
						}
					}
					if (yearsmap.keySet().size()>1) {
						for (Integer year : yearsmap.keySet()) {
							if (members.size()<maxmembers)
							members.add(getMember(year, yearsmap.get(year)));
						}
					}
					else {
						for (org.apache.solr.client.solrj.response.RangeFacet.Count count : range.getCounts()) {
							try {
								Integer month = Integer.valueOf(count.getValue().substring(5, 7));
								Integer year = Integer.valueOf(count.getValue().substring(0, 4));
								if (members.size()<maxmembers)
								members.add(getMember(year, month, count.getCount()));
							}
							catch (Exception e) {
							}
						}
					}
				}
			}
		}
		
		Collections.sort(members, new Comparator<Member>() {
			@Override
			public int compare(Member m1, Member m2) {
				try {
				return SolrDateFacet.this.compare(m1, m2);
				} catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}
		});
		
		
		return members;
	}
	
	
	
	public boolean ordered() {
		return true;
	}
	
	@Override
	public void setParameters(org.apache.solr.client.solrj.SolrQuery query) {
//		super.setParameters(query);
//		query.add("facet.range", getName());
//		query.add("f."+getName()+".facet.range.start","NOW/MONTH-240MONTH");
//		query.add("f."+getName()+".facet.range.end","NOW/MONTH");
//		query.add("f."+getName()+".facet.range.gap","+1MONTH");
//		String intervals[] = { DateRange.MINUTES.criteria(), 
//				DateRange.HOUR.criteria(), 
//				DateRange.TODATE.criteria(), 
//				DateRange.WEEK.criteria(), 
//				DateRange.MONTH.criteria() };
//		query.addIntervalFacets(getName(), intervals);	
	}
	
	public int compare(Member m1, Member m2) {
				
		DateRange r1 = m1 instanceof RangeMember ? ((RangeMember)m1).getRange() : null;
		DateRange r2 = m2 instanceof RangeMember ? ((RangeMember)m2).getRange() : null;
		
		if (r1!=null || r2!=null) {
			if (r1!=null && r2==null)
				return -1;
			else
				if (r1==null && r2!=null)
					return 1;
				else
					return r1.duration()<r2.duration() ? 1 : -1;
		}
		else {
			if (m1 instanceof YearMember && m2 instanceof YearMember) {
				return ((YearMember)m1).getYear()<((YearMember)m2).getYear() ? 1 : -1;
			}
			else {
				if (m1 instanceof MonthMember && m2 instanceof MonthMember) {
					return ((MonthMember)m1).getMonth()>((MonthMember)m2).getMonth() ? 1 : -1;
				}
			}
		}
		
		return 0;
	}
	
	public Member getMember(Count count) {
		SolrMember member = new SolrMember();
		return member;
	}
	
	public Member getParent(Member member) {
		SolrMember parent = new SolrMember();
		String levels[] = member.getPath().split("/");
		parent.setDisplayName(levels[levels.length-2]);
		parent.setPath(getName()+"/"+levels[levels.length-2]+"*");
		parent.setFacet(member.getFacet());
		parent.setFacetDisplayName(super.getDisplayName());
		return parent;
	}
	
	@Override
	public boolean isVisible(ResultSet resultSet) {
		return true;
	}
	
	@Override
	public boolean isRangeEnabled() {
		return true;
	}
	
	private String getLabel(DateRange range) {
		return getLabel(range.id());
	}
	
	private QueryResponse getMembersResponse(ResultSet resultSet) {
		try {
			Assert.isInstanceOf(SolrResultSet.class, resultSet);
			SolrResultSet solrResultSet = (SolrResultSet)resultSet;
			SolrQuery query = new SolrQuery();
			
			String stm = ((com.novamens.solr.indexer.query.SolrQuery)solrResultSet.getQuery()).getSolrStatement();
			String fq = ((com.novamens.solr.indexer.query.SolrQuery)solrResultSet.getQuery()).getSolrFilterStatement();
			
			if ((stm==null || "".equals(stm)) && fq!=null && !"".equals(fq)) {
				query.setQuery(fq);
			}
			else {
				query.setQuery(stm);
				if (fq!=null)
				query.setFilterQueries(fq);
			}
			query.setStart(0);
			query.setRows(1);
	
			query.setHighlight(false);
			query.set("df", "text");
			
			query.setFields(((com.novamens.solr.indexer.query.SolrQuery)solrResultSet.getQuery()).fields());
			
			query.setIncludeScore(false);
			
			query.setFacet(true);
			query.setFacetMinCount(1);
			query.setFacetLimit(-1);
			
			query.add("facet.range", getName());
			query.add("f."+getName()+".facet.range.start","NOW/MONTH-240MONTH");
			query.add("f."+getName()+".facet.range.end","NOW/MONTH");
			query.add("f."+getName()+".facet.range.gap","+1MONTH");
			String intervals[] = { DateRange.MINUTES.criteria(), 
					DateRange.HOUR.criteria(), 
					DateRange.TODATE.criteria(), 
					DateRange.WEEK.criteria(), 
					DateRange.MONTH.criteria(), 
					DateRange.ALL.criteria() 
			};
			query.addIntervalFacets(getName(), intervals);	
	
			BooleanQuery.setMaxClauseCount(2048);
			
			long t1=0, t2;
			
			if (logger.isDebugEnabled())
				t1 = System.currentTimeMillis();
	
			QueryResponse response = solrResultSet.getIndex().getServer().query(query);
			
			if (logger.isDebugEnabled()) {
				t2 = System.currentTimeMillis();
				logger.debug("Solr Range Query: "+(t2-t1));
			}
			
			return response;
		}
		catch (SolrServerException | IOException e) {
			logger.error(e);
			return null;
		}
	}
	
	private Member getMember(org.apache.solr.client.solrj.response.IntervalFacet.Count count, DateRange range) {
		SolrMember member = new RangeMember(super.getName(), range);
		member.setFacetDisplayName(super.getDisplayName());
		member.setCount(count.getCount());
		return member;
	}
	
	private Member getMember(int year, int month, int count) {
		SolrMember member = new MonthMember(super.getName(), year, month);
		member.setFacetDisplayName(super.getDisplayName());
		member.setCount(count);
		return member;
	}
	
	private Member getMember(int year, int count) {
		SolrMember member = new YearMember(super.getName(), year);
		member.setFacetDisplayName(super.getDisplayName());
		member.setCount(count);
		return member;
	}
	
	private String getLabel(String id) {
		Locale locale = SolrDateFacet.this.getUser()!=null? getUser().getLocale(): Locale.getDefault(); 
		ResourceBundle rb = ResourceBundle.getBundle(SolrDateFacet.this.getClass().getName(), locale);
		String label = rb.getString(id);
		return label;
	}
	
	private User getUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}