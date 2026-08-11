package com.novamens.content.orgchart;

public interface OrgChartMember  {
	
	public String getJobdescription();
	public Boolean getResponsible();
	public String getMemberid();

	public void setJobdescription(String s);
	public void setResponsible(Boolean responsible);
	public void setMemberid(String memberid);
}
