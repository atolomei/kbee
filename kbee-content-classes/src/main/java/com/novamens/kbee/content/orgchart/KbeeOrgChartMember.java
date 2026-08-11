package com.novamens.kbee.content.orgchart;

import java.io.Serializable;

import com.novamens.content.orgchart.OrgChartMember;

@Deprecated
public class KbeeOrgChartMember implements OrgChartMember, Serializable {

	private static final long serialVersionUID = 1L;
	public String jobdescription = null;
	public Boolean responsible;
	public String memberid;
	
	static private final String TAB = "    ";
	
	public KbeeOrgChartMember() {
		this.responsible=false;
	}
	
 	public KbeeOrgChartMember(String jobdescription) {
		this.jobdescription = jobdescription;
		this.responsible=false;
	}
	
	public String  toString() {
		StringBuilder str = new StringBuilder();
		xappend(str, memberid);
		xappend(str, jobdescription);
		if(responsible!=null&&responsible)
			xappend(str, responsible.toString());
		return str.toString();
	}
	
	public String  toXMLString(String tab) {
		StringBuilder str = new StringBuilder();
		xappend(str, tab+TAB,memberid, "memberid");
		xappend(str, tab+TAB,jobdescription, "jobdescription");
		if(responsible!=null&&responsible)
			xappend(str, tab+TAB,responsible.toString(), "responsible");
		return str.toString();
	}
	
	private void xappend(StringBuilder str, String value) {
		xappend(str, value, null, "\n", null);
	}

  	private void xappend(StringBuilder str, String tab, String value, String tag) {
		xappend(str, tab, value, "<"+tag+">", "</"+tag+">");
	 }
  	
	private void xappend(StringBuilder str, String tab, String value, String prefix, String sufix) {		
		if (value!=null) {
			if (str!=null)	
 				if (str.length()>0)
					str.append("\n");
 				str.append((tab!=null?tab:"")+prefix+value+sufix);
		}		
	}
	
	public String getJobdescription()		{return jobdescription;}
	public Boolean getResponsible() 		{return responsible;}
	public String getMemberid() 			{return memberid;}

	public void setMemberid(String memberid) 				{this.memberid = memberid;}
	public void setJobdescription(String jobdescription)	{this.jobdescription=jobdescription;}
	public void setResponsible(Boolean responsible) 		{this.responsible = responsible;}

}
