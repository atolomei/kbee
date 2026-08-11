package com.novamens.content.orgchart;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @author atolomei
 *
 * @param <T>
 * 
 */
public class Node<T> {

	static private final String TAB = "    ";
 	
	private T data;
    private Node<T> parent;
    private List<Node<T>> children;
    
    public Node(T da) {
    	this(da, null);
    }

    public Node(T da, Node<T> parent) {
    	this.data=da;
    	this.parent=parent;
    	children = new ArrayList<Node<T>>();
    }
    
    public void setParent(Node<T> parent) {
    	this.parent = parent;
    }
    
    public void addChildren(Node<T> node) {
    	node.setParent(this);
    	children.add(node);
    }
    
    public void addChildren(T da) {
    	children.add(new Node<T>(da, this));
    }
    					
    public void addRemoveChildren(T da) {
    	children.remove(new Node<T>(da, this));
    }

    public Node<T> getNodeChildren(T chdata) {
    	for (Node<T> node: children) {
    	 if (node.data.equals(chdata))
    			 return node;
    	}
    	return null;
    }

    public T getData() {
    	return data;
    }
    
    public Node<T> getParent() {
    	return parent;
    }
    
    
    public List<Node<T>> getChildren(){
    	return children;
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
 	
	public String toXMLString() {
		return toXMLString(0);
	}
 	
	/**
	 * 
	 * @param level
	 * @return
	 */
    private String toXMLString(int level) {
    	
    	StringBuilder str = new StringBuilder();
     	StringBuilder tab = new StringBuilder();
    	
    	for (int n=0; n<level;n++)
    		tab.append(TAB);
    	
    	String tabstr = (tab.length()==0?"":tab.toString());
    	
    	str.append(tabstr);
     	str.append("<node>\n");

    	if (data instanceof OrgChartElement) {
     		str.append(((OrgChartElement) data).toXMLString(tabstr+TAB)+"\n");
    	}
    	
    	for (Node<T> node: children) {
    		xappend(str, node.toXMLString(level+1)+"\n");
    	}
    	
       	str.append(tabstr);
     	str.append("</node>");
    	
    	return str.toString();
    }

    /*
    @SuppressWarnings("rawtypes")
    static Node<OrgChartElement> processNode(Element node) {

    	OrgChartElement orgchart = new OrgChartElement();
    	Node<OrgChartElement> subtreeroot = new Node<OrgChartElement>(orgchart);
    	
        for (Iterator i = node.elementIterator(); i.hasNext(); ) {
        	Element element = (Element) i.next();
        	if (element.getName().equals("node")) {
        		Node<OrgChartElement> child = processNode(element);
        		child.setParent(subtreeroot);
        		subtreeroot.addChildren(child);
        	} else {
        		if (element.getName().equals("jobdescription"))
        			orgchart.jobdescription=element.getText();

        		else if (element.getName().equals("jobcomments"))
        			orgchart.jobcomments=element.getText();

        		else if (element.getName().equals("phone"))
        			orgchart.phone=element.getText();
        		
        		else if (element.getName().equals("firstname"))
        			orgchart.firstname=element.getText();
        		
        		else if (element.getName().equals("lastname"))
        			orgchart.lastname=element.getText();
        			
        		else if (element.getName().equals("email"))
        			orgchart.email=element.getText();
        	
        		}
	        }
    		return subtreeroot;
    }
    static public Node<OrgChartElement> orgCharfromXMLString(String xmlstr) {
    	try {
			Document document = DocumentHelper.parseText(xmlstr);
			Element root = document.getRootElement();
			Node<OrgChartElement> tree = processNode(root);
 			return tree;
 			
	     } catch (DocumentException e) {
			e.pvv  rintStackTrace();
			return null;
		}
    }
     */

}

