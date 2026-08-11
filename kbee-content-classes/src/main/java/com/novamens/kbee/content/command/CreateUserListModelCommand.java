package com.novamens.kbee.content.command;


import java.time.OffsetDateTime;

import com.novamens.beans.BeansService;
import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Domain;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;


/**
 * 
 * 
 */
@Deprecated
public class CreateUserListModelCommand extends AsyncCommand {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(CreateUserListModelCommand.class.getName());

	private int processed = 0;
	private int total_domains=1;
	
	public CreateUserListModelCommand() {
		setName("Create DataSet and Classifier User List");
	}
	
	/**
	 * 
	 */
	@Override
	protected void executeAsync() {
		
		try {
			
			logger.debug("Starting " + this.getClass().getSimpleName());
			com.novamens.hibernate.session.Session.open();
			total_domains=getContentDao().getDomains().size();
			for (Domain domain : getContentDao().getDomains()) {
 				logger.debug("Process > " + domain.getName());
 				process(domain);
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
					
				super.setDateTerminated(OffsetDateTime.now());
				com.novamens.hibernate.session.Session.close();
				logger.debug("done  " + OffsetDateTime.now().toString());
		}
		
	}
	
	
	private void process(Domain domain) {
		
		try {

			ServiceLocator.getService(SecurityService.class).authenticate("root@"+domain.getName());
													
			setProgress( Double.valueOf((this.processed++) * 100.0 / this.total_domains).doubleValue());
			
			
			//boolean d_found=false;
			//DataSet d_user_list = null;
			
//			for(DataSet dataset: getContentDao().getDataSets(domain)) {
//				if (dataset.getAlias().equals("user-list") && dataset.isExternal()) {
//					//d_found=true;
//					//d_user_list=dataset;
//					break;
//				}
//			
////				if (!d_found)
////					d_user_list = addUserListDataSet(domain);
//			}

			
			//boolean c_found=false;
			//Classifier c_user_list = null;
				
//			for(Classifier classifier: getContentDao().getClassifiers(domain)) {
//					if (classifier.getAlias().equals("user-list") && classifier.getDataSet().getId()!=null && classifier.getDataSet().getId().equals(d_user_list.getId())) {
//						//c_found=true;
//						//c_user_list=classifier;
//						break;
//					}
//			}
					
			//if (!c_found)
			//	c_user_list = addUserListClassifier(domain);
			
		 
			
		} catch (Exception e) {
			
			logger.error(e);
			super.setStatusInfo(e.getClass().getSimpleName() + " " + e.getMessage());
		}
	}
	
	
	
	 

	
//	private Classifier addUserListClassifier(Domain domain) {
//		try {
//			Classifier cla = (Classifier) ServiceLocator.getService(ObjectFactoryService.class).createClassifier();
//			cla.setName("User List");
//			cla.setAlias("user-list");
//			((KbeeClassifier)cla).setDefaultStructure(true);
//			((KbeeClassifier)cla).setMultiplicity(Multiplicity.M0N);
//			return cla;
//		}
//		catch (Exception e) {
//			logger.error(e);
//			throw new KbeeRuntimeException(e);
//		}
//	}


//	private DataSet addUserListDataSet(Domain domain) {
//		try {
//			Object dataset = ServiceLocator.getService(ObjectFactoryService.class).createDataSet(DataSetType.EXTERNAL);
//			((DataSet) dataset).setName("User List");
//			((KbeeDataSet) dataset).setAlias("user-list");;
//			return (DataSet) dataset;
//		}
//		catch (Exception e) {
//			logger.error(e);
//			throw new KbeeRuntimeException(e);
//		}
//		
//		
//	}


	
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

}
