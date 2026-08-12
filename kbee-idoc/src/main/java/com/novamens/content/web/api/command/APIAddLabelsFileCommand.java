package com.novamens.content.web.api.command;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.novamens.beans.BeansService;

import com.novamens.content.command.CommandState;
import com.novamens.content.dao.ContentDao;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.LabelSet;

import com.novamens.dao.SecurityDao;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.command.AbstractCommand;

import com.novamens.kbee.content.model.KbeeClassifierTemplate;

import com.novamens.logging.ModelUpdateEvent;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
						
public class APIAddLabelsFileCommand extends AbstractCommand {

	static Logger logger = LogManager.getLogger(APIAddLabelsFileCommand.class.getName());

	// Logger sincronico en la TRX
	static private Logger txlogger = LogManager.getLogger("TxLogger");

	
	private List<String> res = new ArrayList<String>();
	
	public APIAddLabelsFileCommand() {
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
			
			res.clear();
			
			if (total>0) {
				
				Double d_total = Double.valueOf(total);
				Double curr = Double.valueOf(0);
				
				for (Domain domain: getContentDao().getDomains()) {
					
					try {
						
						if (!domain.getName().equals("kbee")) {
							addFileLabels(domain);
						}
						
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
		
			StringBuilder str = new StringBuilder();
			for (String s: res) {
				if (str.length()>0)
					str.append(" | ");
				str.append(s);
			}
			
			setResult("Total added: " + String.valueOf(res.size()));
			setResultComments(str.toString());
			
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
	 *
	 * 
	 */
	public void addFileLabels(Domain domain) {
		
		ContentTemplate ct_file = null;

		for (ContentTemplate ct: getContentDao().getTemplates(domain)) {
			if (ct.getName().equals(getContentDao().findSystemParameterValueByKey("content_class.file.name", "File"))) 
				ct_file = ct;
		}
		
		if (ct_file==null) 
			return;
		
		// User root = getRootUser(domain);

		for (ClassifierTemplate cl: ct_file.getClassifiers()) {
			if (cl.getClassifier().getDataSet() instanceof LabelSet) 
					return;
			
		}
		
		Classifier cl_label = null;
		
		for (Classifier c: getContentDao().getClassifiers(domain)) {
				if (c.getDataSet() instanceof LabelSet) { 
					cl_label = c;
					break;
				}
		}
		
		if (cl_label==null)
			return;
								
		ClassifierTemplate ctf_tag	= new KbeeClassifierTemplate(cl_label);	
		ctf_tag.setMetadataSubtitle(false);
		ct_file.addClassifier(ctf_tag);
		
		getContentDao().save(ct_file);
		txlogger.info(new ModelUpdateEvent(ct_file, "Add Label"));
		
		logger.debug("Adding Labels to File for Domain " + domain.getName());
		
	}
	

	

	//private User getRootUser(Domain domain) {
	//	return ((KbeeSecurityDao) getSecurityDao()).findUserByName("root@"+ domain.getName());
	//}
	
	protected SecurityDao getSecurityDao() {
		return	(SecurityDao)ServiceLocator.getService(BeansService.class).getBean("securityDao");
	}
	


}
