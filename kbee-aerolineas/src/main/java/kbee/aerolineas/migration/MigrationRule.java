package kbee.aerolineas.migration;

import java.util.Map;
import java.util.function.Predicate;

public class MigrationRule {
	
	public enum RuleType {
		
		TEMPLATE ("Template"), 
		CLASSIFICATION ("Classification"), 
		CLASSIFICATION_REPLACE ("Classification_Replace"), 
		CLASSIFICATION_UNIQUE ("Classification_Unique"); 
		
		private String label;
		
		private  RuleType(String label) {
			this.label = label;
		}
		
		public String getLabel() {
			return label;
		}
	}
	
	private RuleType type;
	private Predicate<Map<String,String>> predicate;
	private Map<String, String> result;
	
	public MigrationRule(Predicate<Map<String,String>> predicate, RuleType type, Map<String, String> result) {
		this.predicate = predicate;
		this.type = type;
		this.result = result;
	}
	
	public RuleType getType() {
		return type;
	}
	
	public Predicate<Map<String,String>> getPredicate() {
		return predicate;
	}
	
	public Map<String, String> getResult() {
		return result;
	}
	
	public Map<String, String> evaluate(Map<String, String> data) {
		if (predicate.test(data)) {
			return result;
		}
		else {
			return null;
		}
	}

}
