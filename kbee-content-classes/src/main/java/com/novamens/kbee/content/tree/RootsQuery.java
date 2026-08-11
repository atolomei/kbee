package com.novamens.kbee.content.tree;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import com.novamens.content.model.DataSet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrQuery;

public class RootsQuery extends SolrQuery {
	private static final long serialVersionUID = 1L;
	
	private String dataSetId;

	public RootsQuery(Index index, DataSet dataSet) {
		super(index);
		Assert.isTrue(dataSet.isHierachical(), "no hierachical");
		dataSetId = String.valueOf(dataSet.getId());
		setParameter("sort","title_sort");
		setParameter("ascending","true");
	}
	
	public String getStatement() {
		StringBuilder statement = new StringBuilder();
		statement.append("type:datasetmember AND ");
		statement.append("dataset:"+dataSetId+ " AND ");
		statement.append("level:0");
		String securityStatement = getReadersStatement();
		if (!"".equals(securityStatement)) {
			statement.append(" AND "+securityStatement);
		}	
		return statement.toString();
	}
	
	@Override
	public String getSolrStatement() {
		return getStatement();
	}
	
	protected String getReadersStatement() {
		
		String statement = "";
		
		SecurityService service = ServiceLocator.getService(SecurityService.class);
		
		User user = service.getSessionUser();
		
		boolean admin = service.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()); 
		boolean support = service.isMember(KbeeGlobalRole.SUPPORT.getId());
		
		if (!service.isRoot() && !admin && !support) {
			List<String> readers = new ArrayList<>();
			readers.add(String.valueOf(user.getId()));
			for (Group group : user.getGroups()) {
				readers = getReaders(group, readers);
			}
			for (String principal : readers) {
				if ("".equals(statement))
					statement += "reader:(";
				else
					statement += " OR ";
				statement += principal;
			}
			statement +=")";
		}	
		
		return statement;
	}
	
	protected List<String> getReaders(Group group, List<String> readers) {
		
		String id = ((KbeeGroup)group).getId().toString();
	
		if (readers.contains(id)) 
			return readers;
		
		readers.add(id);
	
		for (Group parent : ((KbeeGroup)group).getGroups()) {
			readers = getReaders(parent, readers);
		}
		
		return readers;
	}
}

