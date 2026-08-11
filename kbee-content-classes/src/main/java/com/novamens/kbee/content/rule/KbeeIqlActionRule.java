package com.novamens.kbee.content.rule;

import java.util.HashMap;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.base.Content;
import com.novamens.content.base.RuleCondition;
import com.novamens.content.model.Classificable;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.rule.Action;
import com.novamens.content.rule.ActionRule;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.SearchResult;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.library.IqlCriteria;
import com.novamens.solr.indexer.query.SolrParametersQuery;

import kbee.util.logging.Logger;

@Entity
@DiscriminatorValue("iql")
public class KbeeIqlActionRule extends KbeeActionRule {
	
	public static String Type = "iql";
	
	private static Logger actionrulesLogger 	= Logger.getLogger("actionrules");
	
	private static String Rule_Property = "action_rule";

	
	@Column(name = "condition")
	private String iqlstatement;
	
	
	@Override
	public void evaluate() {
		evaluate(ActionRule.TEST_CONDITION);
	}
	
	@Override
	public void evaluate(String mode) {
		
		if (getCondition()==null) {
			logger.debug("condition is null or empty.");
			return;
		}
		
		long start = System.currentTimeMillis();
		ResultSet resultSet = null;
		
		try {
			logger.debug(this.getDisplayName());
			logger.debug(this.getDomain().getName());
			logger.debug("Mode -> " + mode);
			logger.debug("Condition -> " +getDisplayCondition());
			
			resultSet = getQuery().execute();
			logger.debug("Starting evaluate " + String.valueOf(resultSet.size()) +" items");
			
			while (resultSet.hasNext()) {
				SearchResult result = resultSet.next();
				Content content = (Content)result.getObject();
				if (content!=null) {
					if (mode.contentEquals(ActionRule.TEST_CONDITION)) 
						logger.debug(content.getDisplayName() + " " + content.getId().toString());
					else
						evaluate(content);
				}
			}
		}
		catch (Exception e) {
			logger.error(e);
			actionrulesLogger.error(e);
			throw(e);
		}
		finally {
			logger.debug("Duration : " + String.valueOf(System.currentTimeMillis()-start) + " ms");
			logger.debug("done");
			if (resultSet!=null)
				resultSet.close();
		}
	}
	
	public boolean evaluate(Content content) {
		if ( (getAction()!=null) && (!getAction().justOneTime() || !isApplied(content)) ) {
			Action action = getAction();
			logger.debug(action.toString());
			//action.setActionRuleId(this.getId());
			//action.setActionRuleName(this.getName());
			action.execute(content);
			if (getAction().justOneTime()) 
				setApplied(content);
		}
		return true;
	}
	
	public boolean evaluate(Classificable classificable) {
		return false;
	}
	
	public void setCondition(String condition) {
		this.iqlstatement = condition;
	}
	
	public String getCondition() {
		String statement = iqlstatement;
		//if (isContentRule()) {
		//	statement = "contentOId("+String.valueOf(getContentOId()) + ") AND ("+ statement + ")"; 
		//}
		return statement;
	}
	
	public String getIqlStatement() {
		return iqlstatement;
	}
	
	public RuleCondition  getRuleCondition() {
		return new IqlRuleCondition(getIqlStatement());
	}
	
	public String getType() {
		return Type;
	}
	
	private Query getQuery() {
		SolrParametersQuery query = new SolrParametersQuery(getIndex());
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("type", "[idoc, text]");
 		parameters.put("state", String.valueOf(ObjectState.ENABLED.getId()));
		parameters.put("domain", String.valueOf(getDomain().getId()));
		IqlCriteria iqlcriteria = new IqlCriteria(getDomain(), getIqlStatement());
		if (iqlcriteria!=null) {
			for (String parametername : iqlcriteria.getParameters().keySet()) {
				parameters.put(parametername, iqlcriteria.getParameters().get(parametername));
			}
		}
		parameters.remove("head");
		query.setParameters(parameters);
		logger.debug(query.getStatement());
		return query;
	}
	
	private void setApplied(Content content) {
		String propertyvalue = (String)content.getService(PropertyService.class).getProperty(Rule_Property);
		if (propertyvalue==null) propertyvalue = "";
		StringBuilder value = new StringBuilder(propertyvalue);
		String ruleid = String.valueOf(getId()) + " ";
		if (!value.toString().contains(ruleid)) 
			value.append(ruleid);
		content.getService(PropertyService.class).setProperty(Rule_Property, value.toString());
	}
	
	private boolean isApplied(Content content) {
		String value = (String)content.getService(PropertyService.class).getProperty(Rule_Property);
		if (value==null) 
			value="";
		String ruleid = String.valueOf(getId()) + " ";
		return value.contains(ruleid);
	}
	
	private Index getIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
}
