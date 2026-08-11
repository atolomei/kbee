package com.novamens.kbee.content.orgchart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.novamens.content.orgchart.OrgChartElement;
import com.novamens.content.orgchart.OrgChartMember;

/**
 * 
 */
@Deprecated
public class KbeeOrgChartElement implements OrgChartElement, Serializable {

	private static final long serialVersionUID = -2882530902143362025L;
	public String name = null;
	public String mision = null;
	public Integer level = null;
	
	static private final String TAB = "    ";
	
	private List<OrgChartMember> member = null;
	
	public KbeeOrgChartElement() {
		this.member = new ArrayList<>();
	}
	
	public KbeeOrgChartElement(String name) {
		this.name = name;
		this.member = new ArrayList<>();
	}
	
	public KbeeOrgChartElement(String name, String mision) {
		this.name = name;
		this.mision  = mision;
		this.member = new ArrayList<>();
 	}
	

	public String  toString() {
		StringBuilder str = new StringBuilder();
		xappend(str, name);
		if(level!=null)
			xappend(str, level.toString());
		xappend(str, mision);
		return str.toString();
	}
	
	public String  toXMLString(String tab) {
		StringBuilder str = new StringBuilder();
		xappend(str, tab, name, "name");
 		xappend(str, tab, level.toString(), "level");
		xappend(str, tab, mision, "mision");
		if(member!=null&&!member.isEmpty()){
			str.append("\n"+tab+"<members>\n");
			for(OrgChartMember mem : member){
				str.append(tab+TAB+"<member>\n");
				str.append(((KbeeOrgChartMember)mem).toXMLString(tab+TAB)+"\n");
				str.append(tab+TAB+"</member>"+"\n");
			}
			str.append(tab+"</members>");
		}
		return str.toString();
	}

	private void xappend(StringBuilder str, String value) {
		if(value!=null)
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
	
	public String  toXMLString() {
		return toXMLString(null); 
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name=name;
	}

	public String getMision() {
		return mision;
	}
	
	public void setMision(String s) {
		mision=s;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}
	
	@Override
	public List<OrgChartMember> getMembers() {
		return member;
	}

	@Override
	public void setMembers(List<OrgChartMember> members) {
		this.member = members;
	}

	@Override
	public OrgChartMember getResponsible() {
		for(OrgChartMember memb : member){
			if(memb.getResponsible()!=null&&memb.getResponsible())
				return memb;
		}
		return null;
	}

	@Override
	public List<OrgChartMember> getMembersWithoutResponsible() {
		List<OrgChartMember> lista = new ArrayList<OrgChartMember>();
		for(OrgChartMember memb : member){
			if(memb.getResponsible()==null||!memb.getResponsible())
				lista.add(memb);	
		}
		return lista;
	}
	
}

