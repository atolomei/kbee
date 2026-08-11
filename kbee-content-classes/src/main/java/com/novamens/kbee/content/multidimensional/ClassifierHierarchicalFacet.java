package com.novamens.kbee.content.multidimensional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Member;
import com.novamens.indexer.query.ResultSet;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.multidimensional.SolrFacet;
import com.novamens.solr.indexer.multidimensional.SolrMember;
import com.novamens.solr.indexer.query.SolrResultSet;

public class ClassifierHierarchicalFacet extends SolrFacet implements Serializable {
	private static final long serialVersionUID = 1L;
	private Classifier classifier;

	public ClassifierHierarchicalFacet() {
	}
	
	@Override
	public boolean isHierachical() {
		return classifier.isHierarchical();
	}	
	
	public List<Member> getMembers2(ResultSet resultSet, int maxmembers) {
		List<Member> members = new ArrayList<Member>();
		
		Map<Integer, Map<String, Integer>> counts = new HashMap<Integer, Map<String, Integer>>();
		
		Set<String> navigables = new HashSet<String>();
		Assert.isInstanceOf(SolrResultSet.class, resultSet);
		FacetField facetField = ((SolrResultSet)resultSet).getQueryResponse().getFacetField(getName());
		
		List<String> membersparameters = (List<String>)((SolrResultSet)resultSet).getQuery().getParameters().get("members");
		
		
		if (facetField==null) {
			return members;
		}
		
		String memberparameter = null;
		if (membersparameters!=null)
		for (String parameter : membersparameters) {
			if (parameter.startsWith(classifier.getUniqueName())) {
				String value = parameter.replace(classifier.getUniqueName()+"member/", "");
				value = value.replace("*", "");
				if (memberparameter==null || value.length()>memberparameter.length()) {
					memberparameter = value;
				}
			}
		}
		
		int maxlevel = -1;
		for (Count count : facetField.getValues()) {
			
			String memberid = count.getName();
			if (memberparameter==null || memberid.startsWith(memberparameter)) { 
				String levels[] = memberid.split("/");
	
				int memberlevel = levels.length;
				if (memberlevel>maxlevel) maxlevel = memberlevel;
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
					if (level==1 && levels.length>1) 
					navigables.add(memberlevelid);
				}
			}
		}
		
		Integer resultlevel = 1;
		for (int level = 1; level<=maxlevel; level++) {
			Map<String, Integer> levelcounts = counts.get(level);
			int totallevel = sum(levelcounts);
			if ((levelcounts!=null && levelcounts.keySet().size()>1)||level==maxlevel||counts.keySet().size()==1||totallevel<resultSet.size()) {
				resultlevel = level;
				break;
			}	
		}
			
		Map<String, Integer> memberscounts = counts.get(resultlevel);
		if (memberscounts==null ) return members;
		
		if (resultlevel>1) {
			int resultlevelcount = sum(counts.get(resultlevel));
			int upperlevelcount = sum(counts.get(resultlevel-1));
			if (upperlevelcount>resultlevelcount && upperlevelcount<resultSet.size()) {
				Map<String, Integer> uppercounts = counts.get(resultlevel-1);
				for (String memberid : uppercounts.keySet()) {
					SolrMember member = new SolrMember();
					String levels[] = memberid.split("/");
					DataSetMember datamember = (DataSetMember)getContentDao().findModelObjectById(DataSetMember.class, levels[levels.length-1]);
					member.setDisplayName(datamember!=null && datamember.getStrValue()!=null ? datamember.getStrValue() : "-");
					member.setPath(getName()+"/"+memberid+"*");
					member.setFacet(super.getName());
					member.setFacetDisplayName(super.getDisplayName());
					//member.setCount(upperlevelcount-resultlevelcount);
					member.setCount(upperlevelcount);
					member.setNavigable(navigables.contains(memberid));
					members.add(member);
				}
			}
		}
		
		
		Member parent = null; 
		for (String memberid : memberscounts.keySet()) {
//			String levels[] = memberid.split("/");
//			SolrMember member = new SolrMember();
//			DataSetMember datamember = (DataSetMember)getContentDao().findModelObjectById(DataSetMember.class, levels[levels.length-1]);
//			member.setDisplayName(datamember!=null && datamember.getStrValue()!=null ? datamember.getStrValue() : "-");
//			member.setPath(getName()+"/"+memberid+"*");
//			member.setFacet(super.getName());
//			member.setFacetDisplayName(super.getDisplayName());
//			member.setCount(memberscounts.get(memberid));
//			member.setNavigable(resultlevel<counts.keySet().size());
			
			SolrMember member = getMember(memberid, memberscounts.get(memberid), resultlevel<counts.keySet().size());
			
			if (member!=null) {
				if (resultlevel>1) {
					parent = getParent(member);
					member.setParent(parent);
				}
				if (membersparameters!=null && membersparameters.contains(member.getPath()) && resultlevel<maxlevel) {
					String levels[] = memberid.split("/");
					String datasetmemberid = levels[levels.length-1];
					Map<String, Integer> childscounts = counts.get(resultlevel+1);
					boolean childs = false;
					for (String childid : childscounts.keySet()) {
						if (childid.contains(datasetmemberid)) {
							SolrMember childmember = getMember(childid, childscounts.get(childid), resultlevel+1<counts.keySet().size());
							if (childmember!=null) {
								members = addMember(childmember, members, maxmembers);
								childs = true;
							}
						}
					}
					if (!childs) {
						members = addMember(member, members, maxmembers);
					}
				}
				else {
					members = addMember(member, members, maxmembers);
//					if (ordered()) {
//						int i =0;
//						for (Member m : members) {
//							if (compare(m, member) > 0) {
//								break;
//							}
//							else 
//								i++;
//						}
//						members.add(i, member);
//					}
//					else {
//						members.add(member);
//						if (members.size()==maxmembers)
//							break;
//					}
//					if (members.size()>maxmembers) {
//						List<Member> ordered = new ArrayList<Member>();
//						for (Member m : members) {
//							ordered.add(m);
//							if (ordered.size()==maxmembers)
//								break;
//						}
//						members = ordered;
//					}
				}
			}
		}
		return members;
	}
	
	private SolrMember getMember(String memberid, int count, boolean navigable) {
		
		String levels[] = memberid.split("/");
		
		DataSetMember datamember = (DataSetMember)getContentDao().findModelObjectById(DataSetMember.class, levels[levels.length-1]);
		if (datamember==null) return null;
		SolrMember member = new SolrMember();
		member.setDisplayName(datamember!=null && datamember.getStrValue()!=null ? datamember.getStrValue() : "-");
		member.setPath(getName()+"/"+memberid+"*");
		member.setFacet(super.getName());
		member.setFacetDisplayName(super.getDisplayName());
		member.setCount(count);
		member.setNavigable(navigable);
		
		return member;
	}
	
	private List<Member>  addMember(Member member, List<Member> members, int maxmembers) {
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
		return members;
	}
	
	public List<Member> getMembers(ResultSet resultSet, int maxmembers) {
		return getMembers2(resultSet, maxmembers);
	}
	
	public List<Member> getMembers(ResultSet resultSet, String filter, int maxmembers) {
		List<Member> members= new ArrayList<Member>();
		Assert.isInstanceOf(SolrResultSet.class, resultSet);
		FacetField facetField = ((SolrResultSet)resultSet).getQueryResponse().getFacetField(getName());
		if (facetField!=null) {
			for (Count count : facetField.getValues()) {
				Member member = getMember(count);
				if (member!=null) {
					if (filter==null || member.getDisplayName().toLowerCase().contains(filter.toLowerCase()))
						members.add(member);
				}
				if (members.size()==maxmembers) break;
				
			}
		}
//		for (Member member : getMembers(resultSet, maxmembers) ) {
//			if (filter==null || member.getDisplayName().toLowerCase().contains(filter.toLowerCase()))
//			members.add(member);
//		}
		Collections.sort(members, new Comparator<Member>() {
			public int compare(Member m1, Member m2) {
				try {
					return m1.getDisplayName().toLowerCase().compareTo(m2.getDisplayName().toLowerCase());
				} catch (Exception e) {
					return 0;
				}
			}
		});
		return members;
	}


	
	public List<Member> getMembers3(ResultSet resultSet, int maxmembers) {
		List<Member> members = new ArrayList<Member>();
		
		Map<Integer, Map<String, Integer>> counts = new HashMap<Integer, Map<String, Integer>>();
		
		Set<String> navigables = new HashSet<String>();
		Assert.isInstanceOf(SolrResultSet.class, resultSet);
		FacetField facetField = ((SolrResultSet)resultSet).getQueryResponse().getFacetField(getName());
		
		if (facetField==null) {
			return members;
		}
		
		@SuppressWarnings("unchecked")
		ArrayList<String> membersparameter = (ArrayList<String>)((SolrResultSet)resultSet).getQuery().getParameters().get("members");
		
		String filter = null;
		
		if (membersparameter!=null)
			for (String member : membersparameter) {
				if (member.startsWith(facetField.getName())) {
					filter = member.replace(facetField.getName(), "");
					filter = filter.substring(1, filter.length()-1);
				}
			}
	
		
		int maxlevel = -1;
		for (Count count : facetField.getValues()) {
			String memberid = count.getName();
			
			
			if (filter==null || memberid.startsWith(filter)) {
			
				String levels[] = memberid.split("/");
	
				int memberlevel = levels.length;
				if (memberlevel>maxlevel) maxlevel = memberlevel;
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
					if (level==1 && levels.length>1) 
					navigables.add(memberlevelid);
				}
			}
		}
		
		Integer resultlevel = 1;
		for (int level = 1; level<=maxlevel; level++) {
			Map<String, Integer> levelcounts = counts.get(level);
			if ((levelcounts!=null && levelcounts.keySet().size()>1)||level==maxlevel||counts.keySet().size()==1) {
				resultlevel = level;
				break;
			}	
		}
			
		Map<String, Integer> memberscounts = counts.get(resultlevel);
		if (memberscounts==null) return members;
		
		if (resultlevel>1) {
			int resultlevelcount = sum(counts.get(resultlevel));
			int upperlevelcount = sum(counts.get(resultlevel-1));
			if (upperlevelcount>resultlevelcount) {
				Map<String, Integer> uppercounts = counts.get(resultlevel-1);
				for (String memberid : uppercounts.keySet()) {
					SolrMember member = new SolrMember();
					String levels[] = memberid.split("/");
					DataSetMember datamember = (DataSetMember)getContentDao().findModelObjectById(DataSetMember.class, levels[levels.length-1]);
					member.setDisplayName(datamember!=null && datamember.getStrValue()!=null ? datamember.getStrValue() : "-");
					member.setPath(getName()+"/"+memberid);
					member.setFacet(super.getName());
					member.setFacetDisplayName(super.getDisplayName());
					member.setCount(upperlevelcount-resultlevelcount);
					member.setNavigable(navigables.contains(memberid));
					members.add(member);
				}
			}
		}
		
		Member parent = null; 
		for (String memberid : memberscounts.keySet()) {
			SolrMember member = null;
			member = new SolrMember();
			String levels[] = memberid.split("/");
			
			DataSetMember datamember = (DataSetMember)getContentDao().findModelObjectById(DataSetMember.class, levels[levels.length-1]);
			member.setDisplayName(datamember!=null && datamember.getStrValue()!=null ? datamember.getStrValue() : "-");
			member.setPath(getName()+"/"+memberid+"*");
			member.setFacet(super.getName());
			member.setFacetDisplayName(super.getDisplayName());
			member.setCount(memberscounts.get(memberid));
			member.setNavigable(navigables.contains(memberid));
			
			if (resultlevel>1) {
				parent = getParent(member);
				member.setParent(parent);
			}
			
			if (datamember!=null) {
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
		}
		return members;
	}

	
	public boolean ordered() {
		return true;
	}
	
	public int compare(Member m1, Member m2) {
		return m1.getDisplayName().toLowerCase().compareTo(m2.getDisplayName().toLowerCase());
	}
	
	public Classifier getClassifier() {
		return classifier;
	}
	
	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}
	
	public Member getMember(Count count) {
		SolrMember member = new SolrMember();
		String levels[] = count.getName().split("/");
		DataSetMember datamember = (DataSetMember)getContentDao().findModelObjectById(DataSetMember.class, levels[levels.length-1].replace("*", ""));
		if (datamember==null) return null;
		member.setDisplayName(datamember.getStrValue()!=null?datamember.getStrValue():"-");
		member.setPath(getName()+"/"+count.getName());
		member.setFacet(super.getName());
		member.setFacetDisplayName(super.getDisplayName());
		member.setCount((int)count.getCount());
		return member;
	}
	
	public Member getMember(DataSetMember datamember) {
		SolrMember member = new SolrMember();
		member.setDisplayName(datamember.getDisplayName());
		member.setPath(getName()+"/"+getPath(datamember));
		member.setFacet(super.getName());
		member.setFacetDisplayName(super.getDisplayName());
		member.setCount(0);
		return member;	
	}
	
	public Member getParent(Member member) {
		SolrMember parent = new SolrMember();
		String levels[] = member.getPath().split("/");
		DataSetMember datamember = (DataSetMember)getContentDao().findModelObjectById(DataSetMember.class, levels[levels.length-2]);
		parent.setDisplayName(datamember.getStrValue()!=null?datamember.getStrValue():"-");
		parent.setPath(getName()+"/"+levels[levels.length-2]+"*");
		parent.setFacet(member.getFacet());
		parent.setFacetDisplayName(super.getDisplayName());
		return parent;
	}
	
	public boolean isVisible(ResultSet resultSet) {
		return super.isVisible(resultSet) &&  getClassifier().getState().equals(ObjectState.ENABLED);
	}
	
	public ContentDao  getContentDao() {
		return	(ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private String getPath(DataSetMember member) {
		return member.getParents()!=null && !member.getParents().isEmpty() ? 
			getPath(member.getParents().get(0)) + "/" + member.getId() : 
			String.valueOf(member.getId());
	}
	
	private int sum(Map<String, Integer> counts) {
		int sum = 0;
		for (Integer count :  counts.values()) {
			sum += count;
		}
		return sum;
	}
}
