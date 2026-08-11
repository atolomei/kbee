package com.novamens.kbee.content.multidimensional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.springframework.util.Assert;

import com.novamens.indexer.query.Member;
import com.novamens.indexer.query.ResultSet;
import com.novamens.solr.indexer.multidimensional.SolrFacet;
import com.novamens.solr.indexer.multidimensional.SolrMember;
import com.novamens.solr.indexer.query.SolrResultSet;

public abstract class HierarchicalFacet extends SolrFacet implements Serializable {
	private static final long serialVersionUID = 1L;

	public HierarchicalFacet() {
	}
	
	public List<Member> getMembers2(ResultSet resultSet, int maxmembers) {
		return getMembers(resultSet, (Member)null, maxmembers);
	}

	public List<Member> getMembers(ResultSet resultSet, Member rootMember, int maxmembers) {
		List<Member> members = new ArrayList<Member>();
		Map<Integer, Map<String, Integer>> counts = new HashMap<Integer, Map<String, Integer>>();
		Map<Integer, String> lastids = new HashMap<Integer, String>();
		Set<String> navigables = new HashSet<String>();
		Assert.isInstanceOf(SolrResultSet.class, resultSet);
		FacetField facetField = ((SolrResultSet)resultSet).getQueryResponse().getFacetField(getName());
		if (facetField==null) return members;
		int maxlevel = 2;
//		for (Count count : facetField.getValues()) {
		for (Count count : getValues(facetField, rootMember)) {
			String memberid = count.getName();
			
			String levels[] = memberid.split("/");

			int memberlevel = levels.length;
			int fromlevel = memberlevel<=maxlevel ? memberlevel : maxlevel;
			for (int level = fromlevel; level>=1; level--) {
				String memberlevelid = ""; 
				if (level==memberlevel)
					memberlevelid = memberid;
				else
					for(int l=0; l<level; l++) {
						memberlevelid+=levels[0];
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
				if (level==1 && levels.length>1) 
					navigables.add(memberlevelid);
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
		Map<String, Integer> memberscounts = counts.get(maxlevel);
		if (memberscounts!=null) {
			Member parent = null; 
			for (String memberid : memberscounts.keySet()) {
				SolrMember member = null;
				member = new SolrMember();
				String levels[] = memberid.split("/");
				member.setDisplayName(getDisplayName(levels[levels.length-1]));
				member.setPath(getName()+"/"+memberid+"*");
				member.setFacet(super.getName());
				member.setFacetDisplayName(super.getDisplayName());
				member.setCount(memberscounts.get(memberid));
				member.setNavigable(navigables.contains(memberid));
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
		return members;
	}
	
	public List<Member> getMembers(ResultSet resultSet, int maxmembers) {
		List<Member> members = new ArrayList<Member>();
		
		Map<Integer, Map<String, Integer>> counts = new HashMap<Integer, Map<String, Integer>>();
		
		//Map<Integer, String> lastids = new HashMap<Integer, String>();
		Set<String> navigables = new HashSet<String>();
		Assert.isInstanceOf(SolrResultSet.class, resultSet);
		FacetField facetField = ((SolrResultSet)resultSet).getQueryResponse().getFacetField(getName());
		
		if (facetField==null) {
			return members;
		}
		
		int maxlevel = 2;
		for (Count count : facetField.getValues()) {
			String memberid = count.getName();
			String levels[] = memberid.split("/");

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
					counts.put(level, levelcounts);
				}
				
				Integer memberscount = levelcounts.get(memberlevelid);
				
				if (memberscount==null)
					memberscount =  (int)count.getCount();
				else
					memberscount =  memberscount + (int)count.getCount();
				
				levelcounts.put(memberlevelid, memberscount);
			}
		}
		
		Integer resultlevel = 1;
		for (int level = 1; level<=maxlevel; level++) {
			Map<String, Integer> levelcounts = counts.get(level);
			if (levelcounts!=null && levelcounts.keySet().size()>1) {
				resultlevel = level;
				break;
			}	
		}
			
		Map<String, Integer> memberscounts = counts.get(resultlevel);
		if (memberscounts==null) return members;
		
		Member parent = null; 
		for (String memberid : memberscounts.keySet()) {
			SolrMember member = null;
			member = new SolrMember();
			String levels[] = memberid.split("/");
			
			member = new SolrMember();

			member.setDisplayName(getDisplayName(levels[levels.length-1]));
			member.setPath(getName()+"/"+memberid+"*");
			member.setFacet(super.getName());
			member.setFacetDisplayName(super.getDisplayName());
			member.setCount(memberscounts.get(memberid));
			member.setNavigable(navigables.contains(memberid));
			
			if (resultlevel>1) {
				parent = getParent(member);
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
			if (members.size()>maxmembers) {
				List<Member> ordered = new ArrayList<Member>();
				for (Member m : members) {
					ordered.add(m);
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
	
	public int compare(Member m1, Member m2) {
		return m1.getDisplayName().toLowerCase().compareTo(m2.getDisplayName().toLowerCase());
	}
	
	public Member getMember(Count count) {
		SolrMember member = new SolrMember();
		String levels[] = count.getName().split("/");
		member.setDisplayName(getDisplayName(levels[levels.length-1].replace("*", "")));
		member.setPath(getName()+"/"+count.getName());
		member.setFacet(super.getName());
		member.setFacetDisplayName(super.getDisplayName());
		member.setCount((int)count.getCount());
		return member;
	}
	
	public Member getParent(Member member) {
		SolrMember parent = new SolrMember();
		String levels[] = member.getPath().split("/");
		parent.setDisplayName(getDisplayName(levels[levels.length-1].replace("*", "")));
		parent.setPath(getName()+"/"+levels[levels.length-2]+"*");
		parent.setFacet(member.getFacet());
		parent.setFacetDisplayName(super.getDisplayName());
		return parent;
	}
	
	protected abstract String getDisplayName(String id);
	
	List<Count> getValues(FacetField facetField, Member rootMember) {
		if (rootMember == null) return facetField.getValues();
		String levels[] = rootMember.getPath().split("/");
		String rootId = (levels[levels.length-1].replace("*", ""));
		List<Count> values = new ArrayList<Count>();
		for (Count count : facetField.getValues()) {
			if (count.getName().startsWith(rootId))
				values.add(count);
		}
		return values;
	}
}
