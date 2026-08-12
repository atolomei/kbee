package com.novamens.solr.indexer.query;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novamens.dom.ObjectState;
import com.novamens.indexer.iql.IqlQuery;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.query.Filter;
import com.novamens.indexer.query.TextFilter;
import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.IndexerException;

import com.novamens.solr.indexer.iql.SolrIqlQuery;
import com.novamens.solr.indexer.multidimensional.SolrFacet;

/***
 * 
 * <p><b>IMPORTANT</b>. This Query is the only one that can combine SolR parameters with IQL Sentences</p>
 *  
 */

public class SolrParametersQuery extends SolrQuery  {
				
	private static final long serialVersionUID = 1L;
	
	static final public String ENABLED=String.valueOf(ObjectState.ENABLED.getId());
	static final public String ARCHIVED=String.valueOf(ObjectState.ARCHIVED.getId());
	
	
	static final public String STATE_ENABLED_ARCHIVED="["+  ENABLED +", "+ ARCHIVED + "]";


	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SolrParametersQuery.class.getName());
	
	
	public SolrParametersQuery(Index index) {
		super(index);
	}
	
	@Override
	public String getStatement() {
		Map<String, Object> parameters;
		if (getParameters().get("text")!=null) {
			parameters = new HashMap<String, Object>();
			for (String parameter : getParameters().keySet()) {
				if (parameter.equals("text") || parameter.equals("sort") || parameter.equals("ascending")) {
					parameters.put(parameter, getParameters().get(parameter));
				}
			}
			setTextQuery(true);
		}
		else {
			parameters = getParameters();
			setTextQuery(false);
		}
		return getStatement(parameters);
	}

	@Override
	public String getSolrStatement() {
		return getStatement();
	}
	
	@Override
	public String getSolrFilterStatement() {
		Map<String, Object> parameters;
		if (getParameters().get("text")!=null) {
			parameters = getFilterParameters();
			if (parameters==null) parameters = new HashMap<String, Object>();
			for (String parameter : getParameters().keySet()) {
				if (!parameter.equals("text") && !parameter.equals("sort") && !parameter.equals("ascending")) {
					parameters.put(parameter, getParameters().get(parameter));
				}
			}
		}
		else {
			parameters = getFilterParameters();
		}
		return parameters!=null && !parameters.isEmpty() ? getStatement(parameters) : null;
	}
	
	public IqlService getIqlService() {
		return null;
	}
	
	@SuppressWarnings("unchecked")
	protected String getStatement(Map<String, Object> parameters) {
		StringBuilder statement = new StringBuilder();
		try {
			for (String parameter : parameters.keySet()) {
				if (parameter.equals("members")) {
					if (!((List<String>)parameters.get(parameter)).isEmpty()) {
						String clause = getMembersClause((List<String>)parameters.get(parameter));
						if (statement.length()>0) statement.append(" AND ");
						statement.append(clause);
					}
				}
				else {
					if (parameter.equals("iql") && parameters.get(parameter)!=null) {
						String clause = getIqlClause((String)parameters.get(parameter));
						if (statement.length()>0) statement.append(" AND ");
						statement.append(clause);
					}
					else
					if (parameter.equals("domain")) {
						if (statement.length()>0) 
							statement.append(" AND ");
						Object value = parameters.get(parameter);
						if (value instanceof Filter) {
							statement.append(((Filter)value).getClause());
						}
						else {
							statement.append("domain:"+(String)parameters.get(parameter));
						}
					}
					else
					if (parameter.equals("solrclause")) {
						String clause = (String)parameters.get(parameter);
						if (statement.length()>0) statement.append(" AND ");
						statement.append("(");
						statement.append(clause);
						statement.append(")");
					}
					else
					if (!parameter.equals("sort") && !parameter.equals("ascending")) {
						if (statement.length()>0 && parameters.get(parameter)!=null) 
							statement.append(" AND ");
						Object value = parameters.get(parameter);
						if (value instanceof Filter) {
							statement.append(((Filter)value).getClause());
							if (value instanceof TextFilter) {
								setTextQuery(true);
							}
						}
						else
						if (value instanceof String && !"".equals(value.toString())) {
							String strvalue = (String)value;
							if (strvalue.startsWith("[")) {
								strvalue = strvalue.substring(1, strvalue.length()-1);
								String values[] =strvalue.split(",");
								//statement.append("(");
								statement.append(parameter+":(");
								int i = 0;
								for (String option : values) {
									if (i>0) {
										statement.append(" ");
									}	
									//statement.append(" OR ");
									//statement.append(parameter+":"+option.trim());
									statement.append(option.trim());
									i++;
								}
								statement.append(")");
							}
							else {
								if ("text".equals(parameter)) {
									if(((String)value).trim().contains(" "))
										statement.append("("+(String)value+")");
									else
										statement.append((String)value);
								}
								else {
									if(((String)value).trim().contains(" "))
										statement.append(parameter+":("+(String)value+")");
									else
										statement.append(parameter+":"+(String)value);
								}
							}	
						}
						else
						if (value instanceof SolrDateRangeFilter) {
							String strvalue = (String)((SolrDateRangeFilter)value).getValue();
							statement.append(parameter+":"+strvalue);
						}
					}
				}	
			}
		} 
		catch (Exception e) {
			logger.error(e);
			throw e;
		}
	
		logger.debug(statement.toString());
		
		return statement.toString();
	}
	
	
	static final String SEPARATOR = "|";
	static final String SEPARATOR_ESCAPED = "\\|";
	
	protected String getMembersClause(List<String> members) {
		StringBuilder statement = new StringBuilder();
		
		for (String member : members) {
			if (member.contains(SEPARATOR)) {
				String ormembers[] = member.split(SEPARATOR_ESCAPED);
				if (statement.length()>0) 
						statement.append(" AND ");
				statement.append("(");
				for (int m=0; m<ormembers.length; m++) {
					String ormember = ormembers[m];
					if (ormember.length()>0) {
						int i = ormember.indexOf("/");
						String facetname = ormember.substring(0,i);
						String memberid = ormember.substring(i+1);
						if (memberid.contains(" ")) 
							memberid="\""+memberid+"\"";
						SolrFacet facet = (SolrFacet)getIndex().getCube().getFacet(facetname);
						if (m>0) 
							statement.append(" OR ");
						statement.append(facet.getName() +":" + memberid);
					}
				}
				statement.append(")");
			}
			else {
				if (member.length()>0) {
					int i = member.indexOf("/");
					String facetname = member.substring(0,i);
					String memberid = member.substring(i+1);
					SolrFacet facet = (SolrFacet)getIndex().getCube().getFacet(facetname);
					if (facet!=null) {
						if (memberid.contains(" ") && !memberid.startsWith("[")) 
							memberid="\""+memberid+"\"";
						if (statement.length()>0) 
							statement.append(" AND ");
						statement.append(facet.getName() +":" + memberid);
					}
				}
			}
		}
		return statement.toString();
	}
	
	protected String getIqlClause(String iql) {
		
		StringBuilder statement = new StringBuilder();
		try {
			iql = iql.replace("&", "");
			iql = iql.replace("'", "");
			
			IqlService se=getIqlService();
			
			if (se==null) 
				throw new IndexerException("IQL term must provide a IqlService");
			
			IqlQuery query = getIqlService().getNewQuery(iql);
			String solrstatement = ((SolrIqlQuery)query).getSolrStatement();
			if (solrstatement.startsWith("NOT ")) { 
				statement.append(iql); // con los parentesis no anda!! es un bug del solr
			}
			else {
				statement.append(" (");
				statement.append(solrstatement);
				statement.append(")");
			}
			
			logger.debug("IQL -> " + iql + "  |  Clause -> " + statement.toString());
			
			return statement.toString();
		} 
		catch (Exception e) {
			logger.error(e, "IQL -> " + iql + "  |  Clause -> " + statement.toString());
			throw(e);
		}
	}
}
