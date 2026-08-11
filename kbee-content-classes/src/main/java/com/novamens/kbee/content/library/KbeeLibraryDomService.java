package com.novamens.kbee.content.library;

import com.novamens.content.base.ConstraintException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.library.Library;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.service.KbeeDomService;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.SiteService;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component @Scope("prototype")
public class KbeeLibraryDomService extends KbeeDomService<KbeeLibrary, Library> {

	static kbee.util.logging.Logger logger = new kbee.util.logging.Logger(LogManager.getLogger(KbeeLibraryDomService.class.getName()));

	
	/**
	 *  The Group and Rule are removed by the Cascade propagation in Hibernate
	 *  see {@link KbeeSecuredValue}. For this reason we log three remove Events.
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete()  throws ContentMgmtException, ConstraintException {
		super.delete();
		
		Site site=getPortalDao().getLibrarySite(getObject());
		
		if (site!=null) {
			try  {
				site.getService(SiteService.class).delete();
			} catch (Exception e) {
				logger.error(e);
				site.setState(ObjectState.DELETED);
				site.getService(SiteService.class).save();
			}
		}
		
		
		// delete Library Group
		
		
	}
	
	
	private void updateSite() {
	
		Site site=getPortalDao().getLibrarySite(getObject());
		if (site!=null) {
			try  {
				site.setTitle(getObject().getDisplayName());
				site.setDescription(getObject().getDescription());
				site.getService(SiteService.class).save();
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void update()  throws ContentMgmtException {
		super.update();
		updateSite();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void update(String part)  throws ContentMgmtException {
		super.update(part);
		updateSite();
	}
	
	@Override
	@Transactional
	public void update(List<String> parts)  throws ContentMgmtException {
		super.update(parts);
		updateSite();
	}
}