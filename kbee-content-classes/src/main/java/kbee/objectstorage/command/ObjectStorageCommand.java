package kbee.objectstorage.command;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.service.ServiceLocator;

public abstract class ObjectStorageCommand extends AsyncCommand {
						
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ObjectStorageCommand.class.getName());

	private Domain domain;
	
	private Integer maxtomigrate;
	

	public Domain getTargetDomain() {
		
		if (this.domain!=null)
			return this.domain;
		
		String ds= (String) getParameters().get("domain");
		
		if (ds!=null) {

			logger.debug("domain -> " + ds);
			this.domain = getContentDao().findDomainByName(ds);
			
			if (this.domain==null) {
				try {
					Long did = Long.valueOf(ds);
					this.domain = getContentDao().findDomainById(did);
				} catch (Exception e) {
					logger.error(e);
					return null;
				}
			}
		}
		
		else {
			logger.debug("domain -> null");
			return null;
		}
		
		if (this.domain==null)
			logger.debug("domain is null");
		
		return this.domain;
		
	}




	public int getMaxToProcess() {
		
		if (maxtomigrate!=null)
			return maxtomigrate.intValue();
		
		String ds= (String) getParameters().get("max");
		
		if (ds!=null) {
			try {
				maxtomigrate = Integer.valueOf(ds);
			} catch (Exception e) {
				maxtomigrate = Integer.valueOf(1000);	
			}
		}
		
		if (maxtomigrate==null)
			maxtomigrate = Integer.valueOf(1000);
		
		return maxtomigrate.intValue();
		
	}

	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	

	@Override
	protected  void initCommand() {
		super.initCommand();
		
		this.domain = null;
		this.maxtomigrate = null;
		
	}

	
	
}
