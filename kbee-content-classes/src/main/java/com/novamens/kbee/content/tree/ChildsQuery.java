package com.novamens.kbee.content.tree;

import org.springframework.util.Assert;

import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.security.acl.KbeePermission;
import com.novamens.solr.indexer.query.SolrQuery;

import kbee.query.QueryHelpher;

public class ChildsQuery extends SolrQuery {
	private static final long serialVersionUID = 1L;
	
	private String memberId;
	private boolean securedSet;

	public ChildsQuery(Index index, DataSetMember member) {
		super(index);
		Assert.isTrue(member.getDataSet().isHierachical(), "no hierachical");
		memberId = String.valueOf(member.getId());
		setParameter("sort","title_sort");
		setParameter("ascending","true");
		securedSet = DataSetType.SECURED.equals(member.getDataSet().getDataSetType());
	}
	
	public String getStatement() {
		StringBuilder statement = new StringBuilder();
		statement.append("type:datasetmember AND ");
		statement.append("parent:"+memberId);
		if (securedSet) {
			String readersStatement = getReadersStatement();
			if (!"".equals(readersStatement)) {
				statement.append(" AND ("+readersStatement+")");
			}
		}
		return statement.toString();
	}
	
	@Override
	public String getSolrStatement() {
		return getStatement();
	}
	
	protected String getReadersStatement() {
		
		String statement = QueryHelpher.buildSecurityTerm(KbeePermission.READ);

		
//		SecurityService service = ServiceLocator.getService(SecurityService.class);
//		
//		User user = service.getSessionUser();
//		
//		boolean admin = service.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()); 
//		boolean support = service.isMember(KbeeGlobalRole.SUPPORT.getId());
//		
//		if (!service.isRoot() && !admin && !support) {
//			List<String> readers = new ArrayList<>();
//			readers.add(String.valueOf(user.getId()));
//			for (Group group : user.getGroups()) {
//				readers = getReaders(group, readers);
//			}
//			for (String principal : readers) {
//				if ("".equals(statement))
//					statement += "reader:(";
//				else
//					statement += " OR ";
//				statement += principal;
//			}
//			statement +=")";
//		}	
		
		return statement;
	}

//	protected List<String> getReaders(Group group, List<String> readers) {
//	
//		String id = ((KbeeGroup)group).getId().toString();
//	
//		if (readers.contains(id)) 
//			return readers;
//		
//		readers.add(id);
//	
//		for (Group parent : ((KbeeGroup)group).getGroups()) {
//			readers = getReaders(parent, readers);
//		}
//		
//		return readers;
//	}
}