package kbee.web.command;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.base.Resource;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.document.IDoc;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentId;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentFactoryService;
import com.novamens.content.service.ContentService;
import com.novamens.content.user.UserProfile;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.dom.Versionable;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.kbee.content.domain.provisioning.DomainModelBuilderService;
import com.novamens.kbee.content.model.KbeeClassification;
import com.novamens.kbee.content.user.UserPropertyService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;

public class ConvertMyBoxCommand extends AsyncCommand {

	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ConvertMyBoxCommand.class.getName());
							
	int processed = 0;
	int total_domains=1;
	int total_files_created = 0;
	
	public ConvertMyBoxCommand() {
		setName("Convert MyBox Command");
	}


	@Override
	protected void executeAsync() {
		try {
			
			logger.debug("Starting " + this.getClass().getSimpleName());
			
			super.setState(CommandState.RUNNING);
			super.setDateStarted(OffsetDateTime.now());
			 
			com.novamens.hibernate.session.Session.open();
	
			this.total_domains=getContentDao().getDomains().size();
			this.processed=0;
			this.total_files_created=0;
			
			for (Domain domain : getContentDao().getDomains()) {
 				logger.debug("Process > " + domain.getName());
 				logger.debug("-------------------------------------------------");
 				process(domain);
 				setProgress(Double.valueOf((this.processed++) * 100.0 / this.total_domains).doubleValue());
 			}
			
 			setProgress(100.0);
			end();
			super.setState(CommandState.COMPLETED);


		} catch (Exception e) {
				logger.error(e);
				super.setState(CommandState.ERROR);
				super.setResult(e.getClass().getSimpleName());
				super.setResultComments(e.getMessage());
				stop();
		}
		finally {
			
				super.setResultComments("Created files -> " + String.valueOf(this.total_files_created) +" | Domains -> " + String.valueOf(this.processed));
				super.setDateTerminated(OffsetDateTime.now());
				com.novamens.hibernate.session.Session.close();
				logger.debug("done  " + OffsetDateTime.now().toString());
		}
	}

	public long getTotalItemsProcessed() {
		return processed;
	}
	
	public long getTotalItems() {
		return this.total_domains;
	}
	
	/**
	 * @param domain
	 */
	private void process(Domain domain) {

		Transaction transaction = null;
		boolean is_ok = false;
		
		try {
			ServiceLocator.getService(SecurityService.class).authenticate("root@"+domain.getName());
			super.setStatusInfo(domain.getName());
			transaction = beginTransaction();
			domain.getService(DomainModelBuilderService.class).buildResourcesContentTemplateNoTrx();
			is_ok = true;
			
		} catch (Exception e) {
			logger.error(e);
			super.setStatusInfo(e.getClass().getSimpleName() + " " + e.getMessage());
		}
		
		finally {
			if (transaction!=null) {
				if (is_ok)
					transaction.commit();
				else {
					transaction.rollback();
					return;
				}
			}
		}
		
		// --------------------
		// Domain's users
		//
		List<UserProfile> list = getContentDao().findUserProfileByDomain(domain);
		
		for (UserProfile up: list) {
			
			Transaction u_transaction = null;
			boolean u_is_ok = false;
			try {
				u_transaction = beginTransaction();		
				convertUploadAndCreateContainer(up, domain);
				u_is_ok = true;
			}	catch (Exception e) {
				logger.error(e);
				super.setStatusInfo(e.getClass().getSimpleName() + " " + e.getMessage());
			} finally {
				
				if (u_transaction!=null) {
					if (u_is_ok) {
						u_transaction.commit();
					}
					else {
						u_transaction.rollback();
					}
				}
			}
			
		} // for
	}
				
	/**
	 * @param user
	 */
	
	public void convertUploadAndCreateContainer(UserProfile user_p, Domain domain) {
		
		try {
			
			IDoc idoc = getUploadAndCreateContainer(user_p);
			
			if (idoc==null) {
				logger.debug("no upload and create container -> " + user_p.getUser().getUserName());
				return;
			}
			
			ContentTemplate template = null;
			
			for (ContentTemplate t: getContentDao().getTemplates(domain)) {
				if (t.getAlias()!=null && t.getAlias().equals(ContentTemplate.RESOURCES)) {
					template=t;
					break;
				}
			}
			
			if (template==null)
				return;

			DataSet da = null;
			Classifier type = null;
			DataSetMember me = null;
			
			for (ClassifierTemplate c: template.getClassifiers()) {
				if (c.getClassifier().isContentType()) {
					type=c.getClassifier();
					da=c.getClassifier().getDataSet();
					break;
				}
			}
			
			if (da!=null) 
				me = getContentDao().findMemberByValue(da, getContentDao().findSystemParameterValueByKey("datasetvalue.mybox.strvalue", "Resource"));
			
			List<KBFile> list = idoc.getFiles();
			
			
			for (KBFile file: list) {
				
				IDoc c = (IDoc) ServiceLocator.getService(ContentFactoryService.class).create(template.getName(), true, true);
				c.addResource(file);
				
				c.setTitle(file.getTitle());
				c.setLastModifiedOffsetDateTime(file.getLastModifiedOffsetDateTime());
				c.setLastModifiedUser(file.getLastModifiedUser());
				c.setState(ObjectState.DRAFT);
				c.setWorkspace((Long) user_p.getUser().getId());
				c.setDomain(domain);
				((Versionable<?>)c).setHeadVersion(false);
				
				if (me!=null) {
					Classification cl=new KbeeClassification(type, me, c);
					c.addClassification(cl);
				}
				
				this.total_files_created++;
				
				logger.debug("IDoc -> " + c.getTitle()!=null?c.getTitle():"no title"  +" | "+ (user_p.getUser()!=null?user_p.getUser().getUserName():"") + " | #" + String.valueOf(this.total_files_created));
				
				c.getService(ContentService.class).updateNoTrx();

			}
			
			idoc.setResources(new ArrayList<Resource>());
			idoc.setState(ObjectState.DELETED);
			idoc.getService(ContentService.class).updateNoTrx();
				
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	/**
	 * @Override
	 */
	public IDoc getUploadAndCreateContainer(UserProfile user_p) throws ContentMgmtException {

		UserPropertyService service = ((KbeeUser) user_p.getUser()).getService(UserPropertyService.class);
		
		String cid = (String) service.getProperty("upload-and-create-container");
		
		if (cid!=null) {
			try {
				IDoc ret = (IDoc) getContentDao().findContentById(new ContentId(cid));
			if (ret!=null)
				return ret;
			}
			catch (Exception e) {
				throw new ContentMgmtException(e);
			}
		}
		return null;
	}
	 
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
}
