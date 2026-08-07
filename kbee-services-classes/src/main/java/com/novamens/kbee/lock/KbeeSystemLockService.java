package com.novamens.kbee.lock;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.model.ContentId;
import com.novamens.lock.Lock;
import com.novamens.lock.LockDao;
import com.novamens.lock.LockScope;
import com.novamens.lock.ObjectLockedException;
import com.novamens.lock.SystemLockService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.spring.transaction.TransactionSynchronization;

public class KbeeSystemLockService implements SystemLockService {
	private Set<ContentId> memorylocks = new HashSet<ContentId>(); 
	private LockDao lockDao;
	
	private class LockTransactionSynchronization extends TransactionSynchronization {
		private ContentId contentId;
		public LockTransactionSynchronization(ContentId contentId) {
			this.contentId = contentId;
		}
		@Override
		public void afterCompletion(int status) {
			memorylocks.remove(this.contentId);
		}	
	}

	public Lock lock(Object object, LockScope scope) {
		Assert.isInstanceOf(Content.class, object);
		
		ContentId contentId = new ContentId((Content)object);
		
		synchronized (this) {
			if (memorylocks.contains(contentId)) {
				throw new ObjectLockedException(contentId.toString());
			}
			memorylocks.add(contentId);
			new LockTransactionSynchronization(contentId);
		}
		
		List<Lock> locks = getLockDao().findByObject(contentId.toString());
		
		if (!locks.isEmpty()) {
			throw new ObjectLockedException(contentId.toString());
		}	
		
		User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
		
		KbeeLock lock = new KbeeLock();
		lock.setObjectId(contentId.toString());
		lock.setScope(scope);
		lock.setUser(user);
		
		getLockDao().save(lock);
		
		return lock;
	};
	
	public Lock lock(Object object, LockScope scope, long timeout) {
		return null;
	}
	
	public void unlock(Lock lock) {
		getLockDao().delete(lock);
	}
	
	public List<Lock> getLocks(Object object) {
		return getLockDao().findByObject((new ContentId((Content)object)).toString());
	}
	
	public void setLockDao(LockDao dao) {
		this.lockDao = dao;
	}
	
	public LockDao getLockDao() {
		return this.lockDao;
	}
}
