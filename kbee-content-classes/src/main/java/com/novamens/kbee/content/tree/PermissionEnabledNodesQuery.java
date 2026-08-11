package com.novamens.kbee.content.tree;


import org.springframework.util.Assert;

import com.novamens.content.model.DataSet;
import com.novamens.indexer.service.Index;
import com.novamens.security.acl.Permission;
import com.novamens.solr.indexer.query.SolrQuery;

import kbee.query.QueryHelpher;

public class PermissionEnabledNodesQuery extends SolrQuery {
	private static final long serialVersionUID = 1L;
	
	private Permission permission;
	private String dataSetId;

	public PermissionEnabledNodesQuery(Index index, DataSet dataSet, Permission permission) {
		super(index);
		Assert.isTrue(dataSet.isHierachical(), "no hierachical");
		dataSetId = String.valueOf(dataSet.getId());
		this.permission = permission;
		setParameter("sort","title_sort");
		setParameter("ascending","true");
	}
	
	public String getStatement() {
		StringBuilder statement = new StringBuilder();
		statement.append("type:datasetmember AND ");
		statement.append("dataset:"+dataSetId);
			String writersStatement = getWritersStatement();
			if (!"".equals(writersStatement)) {
				statement.append(" AND ("+writersStatement+")");
			}
		return statement.toString();
	}
	
	@Override
	public String getSolrStatement() {
		return getStatement();
	}
	
	protected String getWritersStatement() {
		
		String statement = QueryHelpher.buildSecurityTerm(permission);
		
		return statement;
	}
	
	protected String getField(Permission permission) {
	    switch (permission.toString()) {
        case "write":
            return "writer";
        case "childs":
            return "childwriter";
        default:
            return "unknown";
	    }    
    }
}