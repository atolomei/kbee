package com.novamens.solr.indexer.multidimensional;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.springframework.util.Assert;

import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Member;
import com.novamens.indexer.query.ResultSet;
import com.novamens.service.LanguageService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrResultSet;

public abstract class SolrFacet implements Facet {
	private String name;
	private String displayName;
	private boolean navigable;
	private boolean filterable = false;
	private int order;

	public String getName()  {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getOrder()  {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String name) {
		this.displayName = name;
	}

	
	@Override
	public Serializable getId() {
		return name;
	}

	
	
	@Override
	public String getDisplayName(Locale locale) {
		return ServiceLocator.getService( LanguageService.class ).getString(name, locale, displayName);
	}

	
	public List<Member> getMembers(ResultSet resultSet, int maxmembers) {
		List<Member> members= new ArrayList<Member>();
		Assert.isInstanceOf(SolrResultSet.class, resultSet);
		FacetField facetField = ((SolrResultSet)resultSet).getQueryResponse().getFacetField(getName());
		if (facetField!=null) {
			for (Count count : facetField.getValues()) {
				Member member = getMember(count);
				if (member!=null) members.add(member);
				if (members.size()==maxmembers) break;
			}
		}
		Collections.sort(members, new Comparator<Member>() {
			public int compare(Member m1, Member m2) {
				try {
					return m1.getDisplayName().compareTo(m2.getDisplayName());
				} catch (Exception e) {
					return 0;
				}
			}
		});
		return members;
	}
	
	public List<Member> getMembers(ResultSet resultSet, Member rootMember, int maxmembers) {
		return getMembers(resultSet, maxmembers);
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

	public Member getMember(Count count) {
		return null;
	}

	public boolean isVisible(ResultSet resultSet) {
		FacetField facetField = ((SolrResultSet)resultSet).getQueryResponse().getFacetField(getName());
		return facetField!=null && facetField.getValueCount()>=1;
	}

	public boolean isNavigable() {
		return navigable;
	}

	public void setNavigable(boolean value) {
		this.navigable = value;
	}
	
	public void setFilterable(boolean value) {
		this.filterable = value;
	}
	
	public boolean isFilterable() {
		return filterable;
	}
	
	public boolean isRangeEnabled() {
		return false;
	}
	
	public boolean isSuggester() {
		return true;
	}	
	
	public boolean isHierachical() {
		return false;
	}	
	
	public void setParameters(SolrQuery query) {
		query.addFacetField(getName());
	}
}
