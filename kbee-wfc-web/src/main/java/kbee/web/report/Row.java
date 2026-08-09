package kbee.web.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;


public class Row implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	static private kbee.util.logging.Logger logger = new kbee.util.logging.Logger(LogManager.getLogger(Row.class.getName()));
	
	public class Pair implements Serializable {
		private static final long serialVersionUID = 1L;
		String key;
		Serializable value;
		public Pair(String key, String value) {
			this.key=key;
			this.value=value;
		}
		public Pair(String key, Serializable value) {
			this.key=key;
			this.value=value;
		}
		
		public String getKey() {
			return key;
		}
		
		
	};
	
	private List<Pair> values = new ArrayList<Pair>();
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (Pair p:values) {
			if (str.length()>0)
				str.append(" | ");
			str.append((p.key!=null? p.key:"null") +" -> " + (p.value!=null?p.value:"null"));
		}
		return str.toString();
	}
	public void put(String key, String value) {
		values.add(new Pair(key, value));
	}
	
	
	public List<Pair> getValues() {
		return values;
	}
	
	public void putValue(String key, Serializable value) {
		values.add(new Pair(key, value));
	}
	
	public String get(String key) {
		for (Pair pair : values) {
			if (pair.key.equals(key))
				return pair.value != null ? String.valueOf(pair.value) : "";
		}
		logger.debug("key not found " + key);
		return null;
	};
	
	public Serializable getValue(String key) {
		for (Pair pair : values) {
			if (pair.key.equals(key))
				return pair.value;
		}
		logger.debug("key not found " + key);
		return null;
	};

}