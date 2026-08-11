package com.novamens.kbee.content.orgchart;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;

import com.novamens.content.orgchart.Node;
import com.novamens.content.orgchart.OrgChartElement;

@Deprecated
public class OrgChartImporter {

	private org.apache.logging.log4j.Logger logger = LogManager.getLogger(this.getClass().getName());			

	static private final int PARENT 	 = 1;
	static private final int DESCRIPTION = 2;
	
	Node<OrgChartElement> root = null;
	
/**
 * 
 * @param map
 * @return
 */
	public Node<OrgChartElement> getRoot(Map<String, String> map) {
		
		List<NodeAux> list = new ArrayList<NodeAux>();
		
		Iterator<Entry<String, String>> it = map.entrySet().iterator();
		
		while (it.hasNext()) {
			Map.Entry<String, String> pairs = (Map.Entry<String, String>)it.next();
			
			if (pairs.getKey().equals("node")) {
				String value = (String) pairs.getValue();
				addNode(value);
			}
		}
		
		if (list.size()==0)
			return null;
				
		Node<OrgChartElement> root = list.get(0).node;
		
		for (int n=1; n<list.size(); n++) {
			
			String arr []= list.get(n).level.split("-");
			
			String level = arr[0];
			String sublevel = arr[1];
			
			Node<OrgChartElement> node = list.get(n).node;
			
			if (level.equals("L1")) {
				root.addChildren(node);
				
			} else if (level.equals("L2")) {

				Integer sub = Integer.valueOf(sublevel);
				Node<OrgChartElement>  nd = root.getChildren().get(sub);
				nd.addChildren(node);
				
			} else if (level.equals("L3")) {
				
				Integer sub = Integer.valueOf(sublevel);
				Node<OrgChartElement>  nd = root.getChildren().get(sub);
				nd.addChildren(node);
				
				
			} else if (level.equals("L4")) {
				root.addChildren(node);
				
			} else if (level.equals("L5")) {
				root.addChildren(node);
			}
		}
		
		return null;
	}
	
	/**
	 * @param str
	 * @param list
	 */
	private void addNode(String str) {
		
		OrgChartElement element = new KbeeOrgChartElement();
		String[] arr  = str.split(";");

		String parent = arr[PARENT].toLowerCase().trim();

		Node<OrgChartElement> node = new Node<OrgChartElement>(element);
		
		if (parent==null || parent.equals("null") || parent.equals(arr[DESCRIPTION].toLowerCase().trim())) {
			
			if (root!=null)
				logger.error("Root node already exists");
			
			root = node;
		}
		else {
			assign(node, parent);
		}
	}

@SuppressWarnings("unused")
private void assign(Node<OrgChartElement> node, String parent) {
	Node<OrgChartElement> no = root;
	
	
	
	
}

}



