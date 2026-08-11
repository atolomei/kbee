package kbee.importer;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.util.Assert;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.model.KbeePersonMember;
import com.novamens.security.Identifiable;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;

import kbee.api.model.ApiObject;
import kbee.api.model.ApiProxy;
import kbee.api.model.ApiUser;
import kbee.api.service.ApiService;

@Deprecated
public class Importer {
	
	private ApiService server;
	private LocalMatcher matcher;
	private Domain domain;
	private int progress = 0;
	
	protected Logger logger = LogManager.getLogger("Migration");
	
	public Importer(ApiService server, LocalMatcher matcher) {
		setServer(server);
		setMatcher(matcher);
	}
	
	public void execute() throws ContentMgmtException  {
		
	}
	
	public void setServer(ApiService server) {
		this.server = server;
	}
	
	public ApiService getServer() {
		return this.server;
	}
	
	
	public LocalMatcher getMatcher() {
		return matcher;
	}

	public void setMatcher(LocalMatcher matcher) {
		this.matcher = matcher;
	}

	public int getTotal() {
		return 0;
	}
	
	public String getResult() {
		return "";
	}
	
	public int getProgress() {
		return progress;
	}
	
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	
	protected void setProgress(int progress) {
		this.progress = progress;
	}
		
	protected <T> T getLocal(Class<T> localclass, ApiObject remote) {
		Long localid = getMatcher().getLocal(remote);
		if (localid!=null) {
			return getCurrentSession().get(localclass, localid);
		}
		return null;
	}
	
	protected void setLocal(ApiObject remote, Identifiable local) throws IOException {
		getMatcher().setLocal(remote, local);
	}
	
	protected User getLocalUser(ApiProxy userproxy) {
		Assert.isTrue("user".equals(userproxy.getRel()), "no user");;
		ApiUser remote = getServer().get(ApiUser.class, userproxy.getHRef());
		if (remote!=null) {
			Person local = getLocal(KbeePersonMember.class, remote);
			UserProfile userprofile = local.getProfile(UserProfile.class);
			if (userprofile!=null) {
				return userprofile.getUser();
			}
		}
		return null;
	}
	
	protected OffsetDateTime getOffsetDateTime(Date date) {
		return OffsetDateTime.ofInstant(date.toInstant(), ZoneOffset.systemDefault());
	}
	
	protected void update(Object object) {
		getCurrentSession().save(object);
	}
	
	protected void info(String message) {
		logger.info(message);
	}
	
	private Session getCurrentSession() {
		return getSessionFactory().getCurrentSession();	
	}
	
	private SessionFactory getSessionFactory() {
		return (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
	}
	
	protected Transaction beginTransaction()  {
		return ServiceLocator.getService(TransactionService.class).beginTransaction(false);
	}
	
	protected Domain getSessionDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected ContentSecurityDao getSecurityDao() {
		return	(ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	protected Domain getDomain() {
		return domain;
	}
	
	protected boolean forceUpdate() {
		return true;
	}
	
	protected User getUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
	
	protected void setDomain(Domain domain) {
		this.domain = domain;
	}
	
}
