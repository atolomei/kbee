package com.novamens.content.web.api.command;



import java.time.OffsetDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;

import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.Multiplicity;
import com.novamens.dao.SecurityDao;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.command.AbstractCommand;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeLabelSet;
import com.novamens.kbee.security.KbeeSecurityDao;
import com.novamens.logging.DataSetValueCreateEvent;
import com.novamens.logging.ModelCreateEvent;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
		

/**
 * 
 * 
 * see {@code content-datamanagement-context.xml}
 * in project kbee-idoc
 * 
 * com.novamens.content.web.api.command.APIInformationModelCommand
 * 
 *
 */			
public class APICreateLabelsCommand extends AbstractCommand {

	static Logger logger = LogManager.getLogger(APICreateLabelsCommand.class.getName());

	// Logger sincronico en la TRX
	static private Logger txlogger = LogManager.getLogger("TxLogger");

	
	public APICreateLabelsCommand() {
		
	}
	
	/**
	 * This Command works from the Scheduler (Trx is managed by the Scheduler)
	 */
	@Override
	public void execute() {
		
		CommandState finalState = CommandState.UNKNOWN;
				
		try {

			setDateStarted(OffsetDateTime.now());
			setState(CommandState.RUNNING);
			
			int errno = 0;
			int total = getContentDao().getDomains().size();
			
			if (total>0) {
				
				Double d_total = Double.valueOf(total);
				Double curr = Double.valueOf(0);
				
				for (Domain domain: getContentDao().getDomains()) {
					
					try {
						createLabelsIfNotExists(domain);
						curr = curr + 1.0;
						setProgress(Double.valueOf(curr/d_total).doubleValue());
						
					} catch (Exception e) {
						logger.error(" {} | {} | {} | {}", (getSessionUser()!=null?getSessionUser().getUserName():"null"), e.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
						logger.error(e.getClass().getName(), e);
						errno++;
						if (errno>3) {
							finalState=CommandState.ERROR;
							break;
							
						}
					}
				}
			}
			
			if (errno==0) {
				finalState=CommandState.COMPLETED;
				setProgress(100.00);
			}
			
		
		} finally {
			
			setState(finalState);
			setDateTerminated(OffsetDateTime.now());
		}
		
	}

	private ContentDao getContentDao() {
		 BeansService beans = ServiceLocator.getService(BeansService.class);
		 return  (ContentDao) beans.getBean("contentDao");
	}
	
	private User getSessionUser() {
		try {
			return ServiceLocator.getService(SecurityService.class).getSessionUser();
		} catch (Exception e) {
			return null;
		}
	}

	

	
	/**
	 * 
	 */
	public void createLabelsIfNotExists(Domain domain) {
		
		String label_name = getContentDao().findSystemParameterValueByKey("dataset_label.name", "Label");
		DataSet dt_label=getContentDao().findDataSetByName(label_name,  domain.getId());
	
		if (dt_label==null) {
			
			logger.debug("Creating labels for Domain " + domain.getDisplayName());
			
			User root = getRootUser(domain);
			
			KbeeLabelSet d_tag = new KbeeLabelSet();
			
			d_tag.setDomain(domain);
			d_tag.setCanonical(true);
			d_tag.setReadonly(false);
			d_tag.setName(getContentDao().findSystemParameterValueByKey("dataset_label.name", "Label"));
			d_tag.setLastModifiedUser(root);
			getContentDao().save(d_tag);
			txlogger.info(new ModelCreateEvent(d_tag, "create"));
			
			String vals=getContentDao().findSystemParameterValueByKey("dataset_label.values", "follow up; duplicate; delete; draft");
			String vs[] = vals.split(";");
			for (String str: vs )
				addDataSetMember(d_tag, str);
			KbeeClassifier c_tag = new KbeeClassifier();
			c_tag.setDomain(domain);
			c_tag.setName(d_tag.getName());
			c_tag.setAPIClassifier(false);
			c_tag.setUniqueName("tag"); // tiene que ser consistente con el esquema solr fijo en schema.xml
			c_tag.setPredicate("tag");
			c_tag.setMultiplicity(Multiplicity.M0N);
			c_tag.setContentType(false);
			c_tag.setMetadataSubtitle(false);
			c_tag.setRuleCondition(false);
			c_tag.addDataSet(d_tag);
			c_tag.setLastModifiedUser(root);
			getContentDao().save(c_tag);
			txlogger.info(new ModelCreateEvent(c_tag, "create"));
		}
		else
			logger.debug("Domain " + domain.getDisplayName() + " has Labels ");
	}
	
	
	private void addDataSetMember(DataSet dataset, String value) throws ContentMgmtException {
		User domain_root = getRootUser(dataset.getDomain());
		DataSetMember mt_1 = dataset.createMember();
		mt_1.setDomain(dataset.getDomain());
		mt_1.setCreationOffsetDateTime(OffsetDateTime.now());
		mt_1.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		mt_1.setLastModifiedUser(domain_root);
		mt_1.setState(ObjectState.ENABLED);
		mt_1.setStrValue(value);
		getContentDao().save(mt_1);
		txlogger.info(new DataSetValueCreateEvent(mt_1, "create"));
	}

	
	private User getRootUser(Domain domain) {
		return ((KbeeSecurityDao) getSecurityDao()).findUserByName("root@"+ domain.getName());
	}
	
	protected SecurityDao getSecurityDao() {
		return	(SecurityDao)ServiceLocator.getService(BeansService.class).getBean("securityDao");
	}
	
	
}
