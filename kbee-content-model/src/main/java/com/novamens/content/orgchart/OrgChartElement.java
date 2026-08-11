package com.novamens.content.orgchart;

import java.util.List;

public interface OrgChartElement {
	
	public String getName();
	public void setName(String name);
	public Integer getLevel();
	public void setLevel(Integer level);
	public String getMision();
	public void setMision(String s);
	
	public String  toString();
	public String  toXMLString();
	public String  toXMLString(String tab);

	public List<OrgChartMember> getMembers();
	public void setMembers(List<OrgChartMember> members);
	
	public List<OrgChartMember> getMembersWithoutResponsible();
	
	public OrgChartMember getResponsible();
}
