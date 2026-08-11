package com.novamens.kbee.content.iql;


import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.novamens.content.base.Content;
import com.novamens.content.iql.CaseExpression;
import com.novamens.dom.Domain;
import com.novamens.indexer.iql.Expression;
import com.novamens.indexer.iql.IqlService;
import com.novamens.kbee.content.security.JavaIqlEvaluator;

/**
// CASE IQL THEN VALUE;
// ...;
// DEFAULTCASE VALUE;
 * 
// KEYWORDS IN UPPERCASE 
*/
public class KbeeCaseExpression implements CaseExpression {
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeCaseExpression.class.getName());
	
	private class CaseCondition {
		private String iql;
		private String value;
		public CaseCondition(String iql, String value) {
			this.iql = iql;
			this.value = value;
		}
		public String getIql() {
			return iql;
		}
		public String getValue() {
			return value;
		}
		public boolean evaluate(Content content) {
			IqlService iqlservice = content.getDomain().getService(IqlService.class);
			try {
				Expression iqlexpression = iqlservice.getExpression(getIql());
				JavaIqlEvaluator evaluator = new JavaIqlEvaluator(iqlexpression);
				return evaluator.evaluate(content);
			} 
			catch (RuntimeException e) {
				logger.error(e);
				return false;
			}	
		}
	}
	

	private List<CaseCondition> conditions;
	
	private String defaultValue;
	private String expression;
	private boolean isvalid = false;

	
	public KbeeCaseExpression(Domain domain, String expression) {
		this.expression = expression; 
		isvalid = parse(expression, domain);
	}

	public String evaluate(Content content) {
		String result = null;
		for (CaseCondition condition : conditions) {
			if (condition.evaluate(content)) {
				result = condition.getValue();
				break;
			}	
		}
		if (result==null) {
			result = defaultValue;
		}
		return result;
	}
	
	public boolean isValid() {
		return isvalid;
	}
	
	public String getExpression() {
		return expression;
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		if (expression!=null)
			str.append("Expr: "+ expression);
		
		if (defaultValue!=null) {
			if (str.length()>0)
				str.append(" | Default: ");
			str.append(defaultValue);
		}
		
		return str.toString();
	}
	

	
	private boolean parse(String expression, Domain domain) {
		if (expression == null) return false;
		conditions = new ArrayList<CaseCondition>();
		defaultValue = null;
		StringTokenizer lines = new StringTokenizer(expression, ";");
		while (lines.hasMoreElements()) {
			String line = lines.nextToken().trim();
			if (line!=null) {
				line=line.toUpperCase();
				if (line.startsWith("CASE ")) {
					int t = line.indexOf("THEN ");
					if (t<0) return false;
					String iql = line.substring(5, t-1);
					if (!isValid(iql, domain)) {
						logger.error("invalid iql in case expression "+iql);
						return false;
					}
					String value = line.substring(t+5);
					conditions.add(new CaseCondition(iql, value));
				}
				else {
					if (line.startsWith("DEFAULTCASE ")) {
						if (defaultValue!=null) 
							return false;
						defaultValue = line.substring(12).trim();
					}
					else {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private boolean isValid(String iqlExpression, Domain domain) {
		IqlService iqlservice = domain.getService(IqlService.class);
		try {
			iqlservice.getExpression(iqlExpression);
		} 
		catch (RuntimeException e) {
			logger.error(e);
			return false;
		}
		return true;
	}
}
