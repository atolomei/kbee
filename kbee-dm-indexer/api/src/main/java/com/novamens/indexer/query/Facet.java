package com.novamens.indexer.query;

import java.util.List;
import java.util.Locale;

import com.novamens.security.Identifiable;

public interface Facet extends Identifiable {
	public String getName();
	public String getDisplayName();
	
	public String getDisplayName(Locale locale);
	
	public List<Member> getMembers(ResultSet resultSet, int maxmembers);
	public List<Member> getMembers(ResultSet resultSet, Member rootMember, int maxmembers);
	public List<Member> getMembers(ResultSet resultSet, String filter, int maxmembers);
	public boolean isVisible(ResultSet resultSet);
	public boolean isNavigable();
	public boolean isRangeEnabled();
	public boolean isFilterable();
	public boolean isHierachical();
	public boolean isSuggester();
	public int getOrder();
}