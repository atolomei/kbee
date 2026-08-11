package com.novamens.kbee.content.multidimensional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.novamens.content.model.Attribute;
import com.novamens.indexer.query.Member;
import com.novamens.indexer.query.ResultSet;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.multidimensional.SolrFacet;
import com.novamens.solr.indexer.multidimensional.SolrMember;
import com.novamens.solr.indexer.query.SolrQuery;
import com.novamens.solr.indexer.query.SolrResultSet;

@Deprecated
public class DateFacet extends SolrFacet implements Serializable {
	private static final long serialVersionUID = 1L;
				
	static private Logger logger = LogManager.getLogger(DateFacet.class.getName());
	
	private Attribute attribute;
	
	private String months[] = {"", "january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december" };
	
	public class RangeMember extends SolrMember {
		private static final long serialVersionUID = 1L;
		private DateRange range;
		public RangeMember(DateRange range) {
			this.range = range;
		}
		public DateRange getRange() {
			return range;
		}
	};
	
	public enum DateRange {
		TODATE	("todate", 86400000L),
		WEEK ("week", 604800000L),
		MONTH ("month", 2592000000L);
		
		private long duration;
		private String value;
		private DateRange(String value, long duration) {
			this.duration = duration;
			this.value = value;
		}
		public String value() {
			return this.value;
		}
		public String toString() {
			return this.value;
		}
		public boolean contains(Date date) {
			return ((new Date()).getTime() - date.getTime())<duration;
		}
	}

	public DateFacet() {
		super.setDisplayName("Date");
	}

	public List<Member> getMembers(ResultSet resultSet, int maxmembers) {
		List<Member> members = new ArrayList<Member>();
		Map<Integer, Map<String, Integer>> counts = new HashMap<Integer, Map<String, Integer>>();
		Map<Integer, String> lastids = new HashMap<Integer, String>();
		Assert.isInstanceOf(SolrResultSet.class, resultSet);
		FacetField facetField = ((SolrResultSet)resultSet).getQueryResponse().getFacetField(getName());
		if (facetField!=null) {
			int maxlevel = 2;
			for (Count count : facetField.getValues()) {
				String memberid = count.getName();
				
				DateRange range = getRange(memberid);
				if (range!=null) { 
					if (!yearInFilter(resultSet)) {
						Map<String, Integer> levelcounts = counts.get(1);
						if (levelcounts==null) {
							levelcounts = new HashMap<String, Integer>();
							counts.put(1,  levelcounts);
						}
						levelcounts.put(memberid, (int)count.getCount());
						maxlevel = 1;
					}
				}
				else {
					String levels[] = memberid.contains("-")? memberid.split("-") : memberid.split("/");
					int memberlevel = levels.length;
				
					int fromlevel = memberlevel<=maxlevel ? memberlevel : maxlevel;
					for (int level = fromlevel; level>=1; level--) {
						String memberlevelid = ""; 
						if (level==memberlevel)
							memberlevelid = memberid;
						else
							for(int l=0; l<level; l++) {
								memberlevelid+=levels[l];
								if (l+1<level) memberlevelid+="/";
							}	
						Map<String, Integer> levelcounts = counts.get(level);
						if (levelcounts==null) {
							levelcounts = new HashMap<String, Integer>();
							counts.put(level,  levelcounts);
						}
						Integer memberscount = levelcounts.get(memberlevelid);
						if (memberscount==null)
							memberscount =  (int)count.getCount();
						else
							memberscount =  memberscount + (int)count.getCount();
						levelcounts.put(memberlevelid, memberscount);
						if (level<maxlevel) {
							String lastmemberid = lastids.get(level);
							if (lastmemberid!=null && !lastmemberid.equals(memberlevelid)) {
								maxlevel--;
							}
							else
								lastids.put(level, memberlevelid);
						}	
					}
				}
			}
			Map<String, Integer> memberscounts = counts.get(maxlevel);
			if (memberscounts!=null) {
				Member parent = null; 
				for (String memberid : memberscounts.keySet()) {
					SolrMember member = null;
					DateRange range = getRange(memberid);
					if (range!=null) {
						member = new RangeMember(range);
						member.setDisplayName(getLabel(range));
						member.setPath(getName()+"/"+memberid);
						member.setFacet(super.getName());
						member.setFacetDisplayName(super.getDisplayName());
						member.setCount(memberscounts.get(memberid));
					}
					else {
						member = new SolrMember();
						String levels[] = memberid.split("/");
						//member.setDisplayName(levels[maxlevel-1]);
						member.setDisplayName(getLabel(levels));
						if (levels.length>2)
							member.setPath(getName()+"/"+memberid);
						else
							member.setPath(getName()+"/"+memberid+"*");
						member.setFacet(super.getName());
						member.setFacetDisplayName(super.getDisplayName());
						member.setCount(memberscounts.get(memberid));
					}
					if (maxlevel>1) {
						if (parent==null) parent = getParent(member);
						member.setParent(parent);
					}
					if (ordered()) {
						int i =0;
						for (Member m : members) {
							if (compare(m, member) > 0) {
								break;
							}
							else 
								i++;
						}
						members.add(i, member);
					}
					else {
						members.add(member);
						if (members.size()==maxmembers)
							break;
					}
				}
			}	
			if (members.size()>maxmembers) {
				List<Member> ordered = new ArrayList<Member>();
				for (Member member : members) {
					ordered.add(member);
					if (ordered.size()==maxmembers)
						break;
				}
				members = ordered;
			}
		}
		return members;
	}
	
	public boolean ordered() {
		return true;
	}
	
	// static int nnnn = 0;
	// logger.info(nnnn++);
	
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
					return r1.duration<r2.duration ? -1 : 1;
		}
		else {
			int s = StringUtils.countOccurrencesOf(m1.getPath(), "/");
			
			if (s==1) { 
				try {
					return Integer.valueOf(m2.getDisplayName()).compareTo(Integer.valueOf(m1.getDisplayName()));
					
				} catch (java.lang.NumberFormatException e) {
					logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + "  " + e.getMessage()  + " | reindex required");
					return m2.getDisplayName().compareTo(m1.getDisplayName());
				}
			}
			else {
				String m1value = m1.getPath().split("/")[2];
				String m2value = m2.getPath().split("/")[2];
				m1value = m1value.replace("*", "");
				m2value = m2value.replace("*", "");
				try {
					return Integer.valueOf(m1value).compareTo(Integer.valueOf(m2value));
					
				} catch (java.lang.NumberFormatException e) {
					logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + "  " + e.getMessage() + " | reindex required");
					return m2.getDisplayName().compareTo(m1.getDisplayName());
				}
			}	
		}
	}
	
	
	
	public Member getMember(Count count) {
		SolrMember member = new SolrMember();
		DateRange range = getRange(count.getName());
		String rangelabel = range!=null ? getLabel(range) : null;
		member.setFacet(super.getName());
		member.setFacetDisplayName(super.getDisplayName());
		member.setPath(super.getName() + "/" + count.getName());
		member.setDisplayName(range!=null ? rangelabel : count.getName().replace("*", ""));
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
	
	public DateRange getRange(String memberid) {
		if (Character.isDigit(memberid.charAt(0)))
			return null;
		if (DateRange.MONTH.value.equals(memberid))
			return DateRange.MONTH;
		else
		if (DateRange.WEEK.value.equals(memberid))
			return DateRange.WEEK;
		else
		if (DateRange.TODATE.value.equals(memberid))
			return DateRange.TODATE;
		return null;
	}
	
	public String getLabel(DateRange range) {
		return getLabel(range.value());
	}
	
	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}
	
	public Attribute getAttribute() {
		return this.attribute;
	}
	
	protected String getLabel(String levels[]) {
		if (levels.length==2) {
			try {
				int month = Integer.valueOf(levels[1]);
				if (month>=1 && month<=12) {
					return getLabel(months[month]);
				}
				else {
					return levels[1];
				}
			}
			catch (NumberFormatException e) {
				return levels[1];
			}
		}
		else {
			return levels[0];
		}
	}
	
	protected String getLabel(String id) {
		Locale locale = DateFacet.this.getUser()!=null? getUser().getLocale(): Locale.getDefault(); 
		ResourceBundle rb = ResourceBundle.getBundle(DateFacet.this.getClass().getName(), locale);
		String label = rb.getString(id);
		return label;
	}
	
	@SuppressWarnings("unchecked")
	protected boolean yearInFilter(ResultSet resultSet) {
		Assert.isInstanceOf(SolrResultSet.class, resultSet);
		SolrResultSet solrResultSet = (SolrResultSet)resultSet;
		SolrQuery query = (SolrQuery)solrResultSet.getQuery();
		List<String> members = (List<String>)query.getParameters().get("members");
		if (members!=null) {
			for (String member : members) {
				if (member.startsWith(getName())) {
					int s = member.indexOf("/");
					if (s>0) {
						String value = member.substring(s+1);
						value = value.replace("*", "");
						if (org.apache.commons.lang3.StringUtils.isNumeric(value)) {
							return true;
						}
					}		
				}
			}
		}
		return false;
	}
	
	protected User getUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
