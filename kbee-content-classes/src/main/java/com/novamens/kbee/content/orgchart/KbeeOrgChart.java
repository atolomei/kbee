package com.novamens.kbee.content.orgchart;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
**/

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classification;
import com.novamens.content.orgchart.OrgChart;
import com.novamens.content.orgchart.OrgChartElement;
import com.novamens.kbee.content.base.KbeeResourceContainer;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;

/**
 * Organizational Chart
 */
@Entity

// @Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="content")

@Deprecated
@PrimaryKeyJoinColumn(name="content_id")
@Table(name = "Orgchart")
public class KbeeOrgChart  extends KbeeResourceContainer implements OrgChart {

	public static final String CLASS_CODE = "oc";
	static private final String TAB = "    ";

	@Column(name = "name")
	private String name;

	@Column(name = "xmlchart")
	private String xmlchart;

	@Column(name = "mision")
	private String mision;
	
	@Column(name = "description")
	private String description;
	
	
	@Transient
	private List<OrgChartElement> chart = null;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name=name;
	}

	public String getMision() {
		return mision;
	}
	
	public String getDescription() {
		return description;
	}


	public void setMision(String s) {
		mision=s;
	}
	
	public void setDescription(String s) {
		description=s;
	}
	
	public String getXmlchart() {
		return xmlchart;
	}

	public void setXmlchart(String xmlchart) {
		this.xmlchart = xmlchart;
	}

	public void setOrgChart(List<OrgChartElement> chart) {
		this.chart=chart;
		this.xmlchart= toXMLString();
	}
	
	private String toXMLString() {
 		StringBuilder str = new StringBuilder();
		str.append("<org>\n");
		xappend(str, toXMLString(1, chart));
		str.append("</org>"+"\n");
		return str.toString();
	}
	
	/**
	 * @param level
	 * @param subChart 
	 */
	private String toXMLString(Integer level, List<OrgChartElement> subChart) {
 		StringBuilder str = new StringBuilder();
		StringBuilder tab = new StringBuilder();
	    	
		for (int n=0; n<level;n++)
			tab.append(TAB);
		String tabstr = (tab.length()==0?"":tab.toString());
		
		int indice = 0;
		while(indice<subChart.size()){
			OrgChartElement data = subChart.get(indice);
			if(data.getLevel().equals(level)){
				str.append(tabstr);
				str.append("<node>\n");
				str.append(data.toXMLString(tabstr+TAB)+"\n");
				List<OrgChartElement> subList = getSubList(level, subChart, indice+1);
				if(subList!=null&&!subList.isEmpty())
					xappend(str, toXMLString(level+1, subList));
				str.append(tabstr);
				str.append("</node>"+"\n");
				indice = indice+subList.size()+1;
			}else{
				level++;
			}
		}
		return str.toString();
	}
	
	private void xappend(StringBuilder str, String value) {
		xappend(str, value, null, null);
	}
	 
	private void xappend(StringBuilder str, String value, String prefix, String sufix) {		
		if (value!=null) {
			if (str!=null)	
				str.append((prefix!=null?prefix:"")+value+(sufix!=null?sufix:""));
		}		
	}	 	

	private List<OrgChartElement> getSubList(Integer level,	List<OrgChartElement> subChart, int indice) {
		List<OrgChartElement> subList = new ArrayList<>();
		for(int i=indice;i<subChart.size();i++){
			OrgChartElement data = subChart.get(i);
			if(data.getLevel()>level){
				subList.add(data);
			}else
				break;
		}
		return subList;
	}

	public void setOrgCharfromXML(String xmlstr) {
		this.xmlchart=xmlstr;
		this.chart=orgCharfromXMLString(xmlstr); 
	}
	
	public String getChartAsXMLString() {
		if(xmlchart!=null)
			return xmlchart;
		if(chart!=null)
			this.xmlchart= toXMLString(1, chart);
		return xmlchart;
	}
	
	public List<OrgChartElement> getChart() {
		if(chart==null){
			chart = new ArrayList<OrgChartElement>();
			if(xmlchart!=null)
				setOrgCharfromXML(xmlchart);
		}
		return chart; 
	}
	
    private List<OrgChartElement> orgCharfromXMLString(String xmlstr) {

    	/**
    	try {
			Document document = DocumentHelper.parseText(xmlstr);
			Element root = document.getRootElement();
			List<OrgChartElement> tree = processNode(root);
 			return tree;
 			
	     } catch (DocumentException e) {
			e.pr  intStackTrace();
			return null;
		}
		**/
    	
    	return null;
    }
    
	/**
    private List<OrgChartElement> processNode(Element node) {
    	
    
    	OrgChartElement orgchart = new KbeeOrgChartElement();
    	List<OrgChartElement> subtreeroot = new ArrayList<OrgChartElement>();
    	if(!node.getName().equals("org"))
    	   	subtreeroot.add(orgchart);
        for (Iterator i = node.elementIterator(); i.hasNext(); ) {
        	Element element = (Element) i.next();
        	if (element.getName().equals("node")) {
        		List<OrgChartElement> child = processNode(element);
        		subtreeroot.addAll(child);
        	} else {
        		
        		if (element.getName().equals("name"))
        			orgchart.setName(element.getText());
        		if (element.getName().equals("level"))
        			orgchart.setLevel(Integer.valueOf(element.getText()));
        		if (element.getName().equals("mision"))
        			orgchart.setMision(element.getText());
        		
        		if (element.getName().equals("members"))
        			orgchart.setMembers(processMembers(element));
        	}
        }
        return subtreeroot;
       
    	
    }
    */

    /**
	private List<OrgChartMember> processMembers(Element element) {
		List<OrgChartMember> members = new ArrayList<OrgChartMember>();
		for (Iterator it = element.elementIterator(); it.hasNext(); ) {
			Element subElement = (Element) it.next();
			if (subElement.getName().equals("member")){
				OrgChartMember member = new KbeeOrgChartMember();
				for (Iterator ite = subElement.elementIterator(); ite.hasNext(); ) {
					Element fieldsElement = (Element) ite.next();
					if (fieldsElement.getName().equals("memberid"))
	        			member.setMemberid(fieldsElement.getText());
					if (fieldsElement.getName().equals("jobdescription"))
	        			member.setJobdescription(fieldsElement.getText());
	        		else if (fieldsElement.getName().equals("responsible"))
	        			member.setResponsible(true);
				}
				members.add(member);
			}
		}
		return members;
	}
	*/

	/**
     * 
     * @param map
     * @return
     */
    public static OrgChart createFromMap(Map<String, String> map) {
		 
		KbeeOrgChart orgchart =  null;
		
		BeansService beans = ServiceLocator.getService(BeansService.class);
		ContentDao dao = (ContentDao) beans.getBean("contentDao");
		
		if (map.get("domain_id")==null) 
			throw new KbeeRuntimeException("domain is null");
		
		if (map.get("name")==null)
			throw new KbeeRuntimeException("name is null");
		else if (dao.findContentByName(OrgChart.class, map.get("name"), map.get("domain_id"))!=null)
			throw new KbeeRuntimeException("OrgChart already exists.");
		
		orgchart =  new KbeeOrgChart();
		
		orgchart.setName(map.get("name"));
		orgchart.setDescription(map.get("description"));
		orgchart.setMision(map.get("mission"));

		return orgchart;
	}

    @Override
	public String getClassCode() {
		return OrgChart.CLASS_CODE;
	}
    
    @Override
	public Content clone() {

		KbeeOrgChart clone = new KbeeOrgChart();
		clone.setOId(getOId());
		clone.setDomain(getDomain());
		clone.setState(getState());
		clone.setName(getName());
		clone.setTitle(getTitle());
		clone.setContentTemplate(getContentTemplate());
		clone.setAbstract(getAbstract());
		clone.setXmlchart(getXmlchart());
		clone.setUserDefinedAttributes(this.getUserDefinedAttributes());

		List<Classification> clonedclassification = new ArrayList<Classification>();
		for (Classification classification : getClassification()) {
			Classification cc = classification.clone();
			clonedclassification.add(cc);
		}	 
		
		clone.setClassification(clonedclassification);
		
		for (Resource resource: getResources()) 
			clone.addResource(resource);  
		
		return clone;
	}
}
