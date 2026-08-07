package com.novamens.kbee.content.version;

import java.time.OffsetDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Assert;

import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.version.VersionService;
import com.novamens.dom.ObjectState;
import com.novamens.dom.Versionable;
import com.novamens.lock.LockScope;
import com.novamens.lock.ObjectLockService;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


/**
 * 
 *
 */
public class KbeeVersionService implements VersionService {
			
	static private Logger logger = LogManager.getLogger(KbeeVersionService.class.getName());

	private Versionable<?> versionable;
	private ContentDao contentDao;
	
	KbeeVersionService() {
	}
	
	KbeeVersionService(Object object) {
		Assert.isInstanceOf(Versionable.class, object);
		this.versionable = (Versionable<?>)object;
	}
	
	public Object checkout() {
		Object snapshot = null;
		try {
			ObjectLockService lockService = versionable.getService(ObjectLockService.class);
 			lockService.lock(LockScope.EXCLUSIVE);
			User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
			Assert.isTrue(user!=null, "no user");
			((Content)versionable).setLocked(true);
			snapshot = versionable.clone();
			((Content)snapshot).setCommentsEnabled(true);
			((Content)snapshot).setWorkspace((Long)user.getId());

			((Content)snapshot).setLastModifiedUser(user);
			((Content)snapshot).setLastModifiedOffsetDateTime(OffsetDateTime.now());
			
			((Versionable<?>)snapshot).setHeadVersion(false);
			((Versionable<?>)snapshot).setVersion(((Versionable<?>)versionable).getNextVersion());
			((Versionable<?>)snapshot).setPreviousVersion(versionable);

			getContentDao().save((Content)snapshot);
			getContentDao().save((Content)versionable);
			
		}
		catch (Exception e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + e.getClass().getName());
			throw new RuntimeException(e);
		}
		return snapshot;
	};

	/**
	 * 
	 * 
	 */
	public void checkin() {
		try {
			
			Content headVersion = (Content)versionable.getPreviousVersion();

			if (headVersion!=null) {
				ObjectLockService lockService = headVersion.getService(ObjectLockService.class);
				lockService.unlock();
				((Content)headVersion).setLocked(false);
				((Versionable<?>)headVersion).setHeadVersion(false);
				getContentDao().save((Content)headVersion);
			}
			
			
			// Session user applied as checkin user
			//
			User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
			
			((Content) versionable).setCheckinOffsetDateTime(OffsetDateTime.now());
			versionable.setLastModifiedOffsetDateTime(OffsetDateTime.now());
			versionable.setLastModifiedUser(user);
			versionable.setHeadVersion(true);
			((Content)versionable).setWorkspace(null);
			getContentDao().save((Content)versionable);
			
			// ---
			// if there is a Rule associated to this content oid -> update version
			// ---
			
		}
		catch (Exception e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());

			throw new RuntimeException(e);
		}
	};
	
	
	/**
	 * 
	 */
	public void dropCheckout() {
		try {
			Content headVersion = (Content)versionable.getPreviousVersion();
			User user = ServiceLocator.getService(SecurityService.class).getSessionUser();
			if (headVersion!=null) {
				ObjectLockService lockService = headVersion.getService(ObjectLockService.class);
				lockService.unlock();
				((Content)headVersion).setLocked(false);
				((Content)headVersion).incVersionCounter();
				getContentDao().save((Content)headVersion);
				
				versionable.setState(ObjectState.DELETED);
				((Content)versionable).setWorkspace(null);
				versionable.setLastModifiedUser(user);
				versionable.setLastModifiedOffsetDateTime(OffsetDateTime.now());
				getContentDao().save((Content)versionable);
			}
			else {
				versionable.setState(ObjectState.DELETED);
				versionable.setLastModifiedUser(user);
				versionable.setLastModifiedOffsetDateTime(OffsetDateTime.now());
				((Content)versionable).setWorkspace(null);
				getContentDao().save((Content)versionable);
			}
		}
		catch (Exception e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			logger.error(e.getStackTrace());
			throw new RuntimeException(e);
		}
 	}
	
	
	public ContentDao getContentDao() {
			return contentDao;
	}
	
	public void setContentDao(ContentDao dao) {
		this.contentDao = dao;
	}
}
