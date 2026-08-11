package kbee.importer;


import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.dom.Domain;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.service.SecurityContentMgmtService;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;

import kbee.api.model.ApiProxy;
import kbee.api.model.IGroup;
import kbee.api.model.IResultSet;
import kbee.api.service.ApiService;

public class GroupsImporter extends ClassificablesImporter {
	
	private int total = 0;
	private int updated = 0;

	public GroupsImporter(ApiService server, Domain domain, LocalMatcher matcher) {
		super(server, matcher);
		setDomain(domain);
	}
	
	@Override
	public void execute() throws ContentMgmtException  {
		int i=0;
		//Transaction transaction = null;
		try {
			IResultSet<ApiProxy> groups = getServer().getGroups();
			//transaction = beginTransaction();
			while (groups.hasNext()) {
				ApiProxy proxy = groups.next();
				IGroup remote = getServer().get(IGroup.class, proxy.getHRef());
				KbeeGroup local = getLocal(KbeeGroup.class, remote);
				if (local==null || remote.getLastModifiedDate().isAfter(local.getLastModifiedOffsetDateTime()) || forceUpdate()) {
					if (local == null) {
						if (remote.isCanonical()) {
							local = (KbeeGroup)getCanonical(remote.getName());
						}
						if (local == null) {
							local = (KbeeGroup)createGroup();
						}	
						setLocal(remote, local);
					}	
					syncGroup(remote, local);
					update(local);
					updated++;
				 	logger.info("Group " + local.getDisplayName());
				}
//				if (i++%20==0) {
//					transaction.commit();
//					transaction = beginTransaction();
//				}
				setProgress(i);
			}
			getContentDao().flush();
			//transaction.commit();
		}
		catch (Throwable e) {
			//transaction.rollback();
			logger.error(e);
			throw new ContentMgmtException(e);
		}
	}

	@Override
	public int getTotal() {
		if (total == 0) {
			IResultSet<ApiProxy> groups = getServer().getGroups();
			total = (int)groups.getSize();
		}
		return total;
	}
	
	@Override
	public String getResult() {
		String result = "<p>"+String.valueOf(getTotal())+" groups processed. ";
		result += String.valueOf(updated)+" groups updated</p>";
		return result;
	}
	
	private void syncGroup(IGroup remote, Group local) {
		local.setName(remote.getName());
		((KbeeGroup)local).setCanonical(remote.isCanonical());
		((KbeeGroup)local).setAreaCode(remote.getArea());
		local.setLastModifiedOffsetDateTime(remote.getLastModifiedDate());
	}
	
	private Group createGroup() throws ContentCreationException {
		return ServiceLocator.getService(SecurityContentMgmtService.class).createGroup();
	}
	
	private Group getCanonical(String groupname) {
		if ("Domain Admin".equals(groupname))
			groupname = "domain-admin";
		if ("Content Base".equals(groupname))
			groupname = "library";
		if ("Information Model".equals(groupname))
			groupname = "information-model";
		if ("Datasets Members".equals(groupname))
			groupname = "dataset-values";
		for (Group group : getContentSecurityDao().getGroups(getSessionDomain())) {
			if (group.isCanonical() && groupname.toLowerCase().equals(group.getName().toLowerCase())) {
				return group;
			}
		}
		return null;
	}
	
	private ContentSecurityDao getContentSecurityDao() {
		return (ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
}