package com.novamens.content.orgchart;

import java.util.List;
import com.novamens.content.base.Content;


public interface OrgChart extends Content {
	
	public static final String CLASS_CODE = "oc";
	
	public String getName();
	public void setName(String name);
	public void setOrgChart(List<OrgChartElement> chart);
	public void setOrgCharfromXML(String xmlstr);
	public String getChartAsXMLString();
	public List<OrgChartElement> getChart();
	
	public String getMision();
	public String getDescription();	
	public void setMision(String s);
	public void setDescription(String s);
		
}
