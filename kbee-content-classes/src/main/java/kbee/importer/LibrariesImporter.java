package kbee.importer;

import java.time.OffsetDateTime;
import java.util.List;

import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.library.Library;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.library.KbeeLibrary;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.security.acl.Group;
import com.novamens.service.ServiceLocator;

import kbee.api.model.IGroup;
import kbee.api.model.ILibrary;
import kbee.api.service.ApiService;

public class LibrariesImporter extends Importer {
	
	private int total = 0;
	private int updated = 0;

	public LibrariesImporter(ApiService server, Domain domain, LocalMatcher matcher) {
		super(server, matcher);
		setDomain(domain); 
	}
	
	@Override
	public void execute() throws ContentMgmtException  {
		int i=0;
		try {
			for (ILibrary remote : getRemoteLibraries()) {
				Library local = getLocal(KbeeLibrary.class, remote);
				if (local==null || remote.getLastModifiedDate().isAfter(local.getLastModifiedOffsetDateTime()) || forceUpdate()) {
					if (local == null) {
						local = createLibrary(remote.getName());
						setLocal(remote, local);
					}
					syncLibrary(remote, (KbeeLibrary)local);
					update(local);
					updated++;
					logger.info("Library "+local.getDisplayName());
				}
				else {
					logger.info("Library "+local.getDisplayName() + " not modified");
				}
				setProgress(++i);
			}
		}
		catch (Throwable e) {
			logger.error(e);
			throw new ContentMgmtException(e);
		}
	}
	
	@Override
	public int getTotal() {
		if (total == 0) {
			total = getRemoteLibraries().size();
		}
		return total;
	}

	@Override
	public String getResult() {
		String result = "<p>"+String.valueOf(getTotal())+" libraries processed. ";
		result += String.valueOf(updated)+" libraries updated</p>";
		return result;
	}
	
	private void syncLibrary(ILibrary remote, KbeeLibrary local) throws ContentMgmtException {
		local.setName(remote.getDisplayName());
		local.setDisplayName(remote.getDisplayName());
		local.setKey(remote.getName());
		local.setCriteria(remote.getCriteria());
		local.setCanonical(remote.isCanonical()) ;
		local.setLastModifiedOffsetDateTime(remote.getLastModifiedDate());
		if (remote.getReaders()!=null) {
			IGroup igroup = getServer().getGroup(remote.getReaders().getId());
			Group group = getLocalGroup(igroup);
			local.setReaders(group);
		}
	}
	
	private Library createLibrary(String name) throws ContentCreationException {
		//Object library = ServiceLocator.getService(ContentFactoryService.class).createLibrary(name);
		//return (Library)library;
		UserProfile userProfile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		
		KbeeLibrary library = new KbeeLibrary();
		library.setName(name);
	
		String key = name;
		
		if (key.length()>24)
			key=key.substring(0, 23);
		
		logger.debug("Library name " + name + " key:  " + key.toLowerCase());
		
		library.setKey(key.toLowerCase().replaceAll("[°,¡!?¿:\\/\"-().\\s]", "-"));
		
		library.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		library.setCreationOffsetDateTime(OffsetDateTime.now());
		library.setLastModifiedUser(userProfile.getUser());
		library.setState(ObjectState.ENABLED);
		library.setDomain(getSessionDomain());
		library.setCriteria("ishead(true)");
		library.setOrder(getContentDao().getLibraries(getSessionDomain()).size());
		
		// ----
		//
//		KbeeGroup readers = new KbeeGroup();
//		readers.setCanonical(true);
//		readers.setName(name);
//		readers.setArea(KbeeArea.CONTENT);
//		readers.setLastModifiedOffsetDateTime(OffsetDateTime.now());
//		readers.setCreationOffsetDateTime(OffsetDateTime.now());
//		readers.setLastModifiedUser(userProfile.getUser());
//		readers.setDomain(domain);
//		getSecurityDao().save(readers);
//		
//		library.setReaders(readers);
		getContentDao().save((Library)library);
		
		// ServiceLocator.getService(SiteFactoryService.class).createLibrarySite(library);
		
		return library;
	}
	
	private Group getLocalGroup(IGroup igroup) {
		Group local = null;
		if (igroup.isCanonical()) {
			for (Group group : getSecurityDao().getGroups(getSessionDomain())) {
				if (igroup.getName().equals(group.getName())) {
					local = group;
					break;
				}
			}
		}
		if (local==null) {
			local = getLocal(KbeeGroup.class, igroup);
		}
		return local;
	}
	
	private List<ILibrary> getRemoteLibraries() {
		return getServer().getLibraries();
	}
}
