package test.com.novamens.kbee.content;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit4.SpringRunner;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.kbee.content.io.XLSQAImport;
import com.novamens.service.ServiceLocator;

public class QATest extends AbstractTest {

	final private org.apache.logging.log4j.Logger logger = LogManager.getLogger(this.getClass().getName());
	
	@Override
	public void run() {
		
		// testModel();
		//
		// ContentService contentService = getDocument().getService(ContentService.class);
		// contentService.checkout();
		//
		//  contentService.save();
		//  contentService.delete();
		//	
		//
		testImportQA();
	}
	


	@Test
	public void testModel() {
		
		BeansService beans = ServiceLocator.getService(BeansService.class);
		ContentDao dao = (ContentDao) beans.getBean("contentDao");

		List<DataSet> 		ldataset =  dao.getDataSets("1");
		///List<DataSetMember> ldatasetmember = dao.getDataSetMember("1");
		List<Classifier> 	lclassifier =  dao.getClassifiers("1");  // getDomain().getId()
		
		
		logger.info("DataSet: ");
		for(DataSet ds: ldataset) {
			logger.info(ds.getName());
		}
		
		logger.info("DataSetMember: ");
//		for(DataSetMember dm: ldatasetmember) {
//			logger.info(dm.getDataSet().getName() + " -> " + dm.getStrValue());
//		}
		

		logger.info("Classifier: ");
		for(Classifier cl: lclassifier) {
			logger.info(cl.getName() + " -> " + cl.getDataSet().getName());
		}
		
	}
	
	
	public void testImportQA() {
 		File xlsmodel = new File("xlsmodel" + File.separator + "model-infojus.xls");
 		createDomainIfNotExists("Infojus",  xlsmodel);
	 	// Domain domain = (Domain) getContentDao().findSecEntityByName(Domain.class, "Infojus", null);
		// setDomain(domain);
 		File xlsq = new File("questionanswer" + File.separator + "questionanswer.xls");
		XLSQAImport xls = null; 
 		try {
			xls = new XLSQAImport(xlsq);
			xls.execute();
	 	} 
		catch (IOException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
		}
 	 }

}
