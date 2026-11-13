package com.novamens.kbee.content.command;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.SecuredMember;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainObject;
import com.novamens.hibernate.session.Session;
import com.novamens.indexer.java.KbeeJavaIndex;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.IndexProxy;
import com.novamens.kbee.content.tree.ChildsQuery;
import com.novamens.security.acl.Acl;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class ReindexHierarchyCommand extends AsyncCommand {
											
	private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ReindexByCriteriaCommand.class.getName());
	
	private Index index;
	private Domain domain;
	private long totalitems = 0, totalindexed = 0;

	
	public ReindexHierarchyCommand(Index index) {
		setIndex(index);
	}
	
	public Index getIndex() {
		return ((IndexProxy)index).getIndex();
	}
	
	public void setIndex(Index index) {
		this.index = index;
	}
	
	@Override
	public void executeAsync() {
		setDateStarted(OffsetDateTime.now());
		ResultSet resultSet = null;
		try {
			Session.open();
			DataSetMember member = (DataSetMember)getParameter("member");
			su(((DomainObject)member).getDomain());
			index(member, new HashSet<>());
			this.getIndex().commit();
			end();
		}
		catch (Exception e) {
			logger.error(e);
			stop();
			throw new RuntimeException(e);
		}
		finally {
 			if (resultSet!=null)
				resultSet.close();
			Session.close();
		}
	}
	
	
	@Override
	public long getTotalItems() {
		return totalitems;
	}
	
	@Override
	public double getProgress() {
		return totalitems>0 ? (double)totalindexed/(double)totalitems*100 : 0;
	}
	
	protected void index(DataSetMember member, Set<String> indexed) {
		((KbeeJavaIndex)this.getIndex()).index(member);
		indexed.add(String.valueOf(member.getId()));
		totalindexed++;
		Query query = new ChildsQuery(getIndex(), member);
		ResultSet resulSet = query.execute();
		while (resulSet.hasNext()) {
			DataSetMember child = (DataSetMember)resulSet.next().getObject();
			if (child instanceof SecuredMember) {
				Acl acl = ((SecuredMember)child).getSecurityRule().getAcl();
				if (acl==null || acl.getEntries().isEmpty()) {
					if (!indexed.contains(String.valueOf(child.getId()))) {
						index(child, indexed);
					}
				}
 			}
		}
	}
	
	protected void su(Domain domain) {
		if (this.domain==null || !domain.equals(this.domain)) {
			ServiceLocator.getService(SecurityService.class).authenticate("root@" + domain.getName());
			this.domain = domain;
		}
	}
}
