package com.novamens.content.web.admin.markup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.wicket.model.Model;

import com.novamens.kbee.wicket.markup.html.areainfo.AreaInfoPanel;
import com.novamens.kbee.wicket.markup.html.areainfo.GridInfoPanel;
import com.novamens.wicket.util.BCElement;

import kbee.util.PropertiesFactory;
import kbee.util.Tuple;

public class SystemInfoPropertiesPanel extends AbstractSystemInfoPanel {
	
	private static final long serialVersionUID = 1L;

	public SystemInfoPropertiesPanel() {
		this("info-panel");
	}

		
	public SystemInfoPropertiesPanel(String id) {
		super(id);
	}
	
	
	/**
	 * 
	 */
	public void onInitialize() {
		super.onInitialize();
		
		
		
		AreaInfoPanel area = new AreaInfoPanel("info");
		add(area);
		area.setSections(AreaInfoPanel.ONE_SECTION);
		area.setCss("col-lg-12");
		area.addPanel(new GridInfoPanel("element", propertiesKbeeInfo(), new Model<String>("kbee"), true));
		area.addPanel(new GridInfoPanel("element", propertiesJavaInfo(), new Model<String>("Java")));

	}
	

	protected BCElement getPageBCElement() {
		return new BCElement(new Model<String>("Properties"));
	}

	
private List<Tuple> propertiesKbeeInfo() {
		
		Properties properties = PropertiesFactory.getInstance("kbee").getProperties();
		List<Tuple> data = new ArrayList<Tuple>();
		Set<Entry<Object, Object>> entries = properties.entrySet();
		for (Entry<Object, Object> entry: entries) {
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			data.add(new Tuple(key, value));
		}
		
		Collections.sort(data, new Comparator<Tuple>() {
			@Override
			public int compare(Tuple o1,	Tuple o2) {
				return o1.label.toLowerCase().compareTo(o2.label.toLowerCase());
				}
			}); 
		return data;
	}


private List<Tuple> propertiesJavaInfo() {
	List<Tuple> data = systemEnv();
	Collections.sort(data, new Comparator<Tuple>() {
		@Override
		public int compare(Tuple o1,	Tuple o2) {
			try {
				return o1.label.toLowerCase().compareTo(o2.label.toLowerCase());
			} catch (Exception e) {
				return 0;
			}
			}
		}); 
	return data;
}

private List<Tuple> systemEnv() {
	return dumpVars(System.getenv());
}


/***
 * 
 * 
 */


private List<Tuple> dumpVars(Map<String, ?> m) {
	List<Tuple> list = new ArrayList<Tuple>(m.size());
	List<String> keys = new ArrayList<String>(m.keySet());
	  for (String k : keys) {
		  list.add(new Tuple(k,m.get(k).toString()));
	  }
	return list;
}



}
