package com.novamens.kbee.content.social;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.questionanswer.Answer;
import com.novamens.content.questionanswer.Question;
import com.novamens.content.social.Comment;
import com.novamens.content.social.CommunityManager;
import com.novamens.content.user.UserProfile;
import com.novamens.kbee.content.questionanswer.QuestionAnswerService;
import com.novamens.kbee.content.service.datamanagement.DirectoryZipper;
import com.novamens.service.ServiceLocator;
			
public class KbeeCommunityManager implements CommunityManager {
			
	static Logger logger = LogManager.getLogger(KbeeCommunityManager.class.getName());
	
	
	/**
	 * 
	 * Returns estimation of probability that this Content to deserve to be removed.
	 * 0: no remvoe
	 * 1: remove
	 * 
	 * Reports = Long
	 * ConfidenceLevel = int
	 * Max = double
	 */
	
	static private final double CONFIDENCE_THRESHOLD 	 = 3.0;
	static private final double PENALIZTION_USER_CREATED  = 2.0;
	static private final double PENALIZTION_USER_MODIFIED = 4.0; 
	static private final double INAPPROPIATE_THRESHOLD = 0.95;

	 /**
	  * 	
	  * @param content
	  * @return
	  */
	 
	 @Transactional
	 public boolean checkAndRemoveContent(Content content) {
		 
		 double prob = getProbabiltyNotAppropiate(content); 
		 
		 if (prob>INAPPROPIATE_THRESHOLD) {

			 ContentDao dao = (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
			 
			 if (content instanceof Answer) {
			
				 penalizeContentRemoved(content);
				 // ServiceLocator.getService(QuestionAnswerService.class).delete((Answer) content);
				 
				 
				 try {
					 	dao.delete(content);
					 	return true;
					 	//
					 	// dao question -> ajustar sumarizador (en un evento ?)
					 	//
					 	//
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.error(e);
					

				}
				 
				 
				 
			 }
			 else if (content instanceof Question) {
				 // penalizeContentRemoved(content);
				 // no esta implementado aun.
				 	return true;
			 }
			 else if (content instanceof Comment) {
				 // penalizeContentRemoved(content);
				 ServiceLocator.getService(QuestionAnswerService.class).delete((Comment) content);
				 return true;
			 }
		 }
		 return false;
	 }

	 /**
	 *
	 * 
	 */
	 public void penalizeContentRemoved(Content content) {
		
		ContentDao dao = (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
		UserProfile userProfileModified = dao.findUserProfileByUser(content.getLastModifiedUser());

		UserProfile userProfileCreation = null;
		
		if (content instanceof Question) {
			userProfileCreation = dao.findUserProfileByUser( ((Question) content).getUser());
		}
		else if (content instanceof Answer) {
			userProfileCreation = dao.findUserProfileByUser( ((Answer) content).getUser());
		}
		
		if (userProfileCreation!=null) { 
			userProfileCreation.decrementConfidenceLevel(PENALIZTION_USER_CREATED);
		}
		
		userProfileModified.decrementConfidenceLevel(PENALIZTION_USER_MODIFIED);
		
		//
		// Borra el contenido y penaliza las 2 person en forma transaccional.
		//
		// Como graba la Person ?
		//
		// ServiceLocator.getService(QuestionAnswerService.class).update(getQuestion());
		//
		//
	}
	
	 /**
	  * 
	  * @param content
	  */
	public void updateConfidenceLevels(Content content) {
			//
		    // usuario que la creo. [p,r,c]:(1,2,0.5) -> [10 dias]
		    // si tiene votos:  [p,r]: mas de 5 (1,1.5), mas 10(2,3), mas de 30(3,5) -> [60 dias]
		    //
			// guardo la fecha mas reciente procesada. Cada vez que se despierta el demon
		    // inflación: 10% anual.
			//
	}
	/**
	 * 
	 */
	public double getProbabiltyNotAppropiate(Content content) {
		Long reports = (Long) content.getService(PropertyService.class).getProperty("reports");
		if (reports==null||reports==0)
			return 0;
		ContentDao dao = (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
		UserProfile userProfile = dao.findUserProfileByUser(content.getLastModifiedUser());
		double confidenceLevel = userProfile.getConfidenceLevel();
		double MAX   = confidenceLevel * CONFIDENCE_THRESHOLD;
		double delta = (double) (MAX - reports.doubleValue());
		if (delta <= 0.0)
			return 1.0;
		double prob = 1.0-(delta/MAX);
		return prob;
	}
}
