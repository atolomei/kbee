package com.novamens.kbee.content.io;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.model.KbeeClassifier;
import com.novamens.kbee.content.model.KbeeValueMember;
import com.novamens.kbee.content.model.KbeeValueSet;
import com.novamens.kbee.domain.KbeeDomain;
import com.novamens.util.KbeeRuntimeException;

import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;

public class XLSModelImport extends XLSAbstractParser {
	
	private org.apache.logging.log4j.Logger logger = LogManager.getLogger(this.getClass().getName());
	
	public XLSModelImport(File xls) {
		super(xls);
	}
	
	public void execute() throws IOException {
		read();
	}
	
	/** -----------------------------------------------------------------------------
	 * @param sheet
	 * @param row
	 */
	@Override
	protected void processRow(Sheet sheet, int row) {

		Cell cell;
		CellType type;

		if (getState()==State.INITIAL)
				return;
			
		if (getState()==State.DOMAIN) {
			
			cell = sheet.getCell(1, row);
    		type = cell.getType();
    		
    		if (type == CellType.LABEL && cell.getContents().trim().length()>0) {
    				Map<String, String> domaindata = new HashMap<String, String>();
    				domaindata.put("name", sheet.getCell(1, row).getContents());
    				addDomain(domaindata);
    				return;
    		}
		}
			
		if (getState()==State.USERS) {
			
			if (isEmpty(sheet, row, 7))
				return;

			Map<String, String> userdata = new HashMap<String, String>();
			userdata.put("username", sheet.getCell(1, row).getContents().trim());
			userdata.put("firstName", sheet.getCell(2, row).getContents().trim());
			userdata.put("lastName", sheet.getCell(3, row).getContents().trim());
			userdata.put("email", sheet.getCell(4, row).getContents().trim());
			userdata.put("password", sheet.getCell(5, row).getContents().trim());
			
			if (getDomain()!=null) {
				userdata.put("domain_id", String.valueOf(getDomain().getId()));
				addUser(userdata);
			}
			else {
				if (sheet.getCell(6, row).getContents().toString().trim().length()>0) {
					 String domainname = sheet.getCell(6, row).getContents().toString().trim();
					 Domain xdomain;
					 xdomain = getDao().findDomainByName(domainname);
					if (xdomain!=null) {
							userdata.put("domain_id", xdomain.getId().toString());
							addUser(userdata);
					}
					else
						logger.error("domain not found: " + domainname);
				}
			}
			return;
		}
		
		
		if (getState()==State.DATASETS) {

			if (isEmpty(sheet, row, 3))
				return;
			
			Map<String, String> datasetdata = new HashMap<String, String>();
			datasetdata.put("name", sheet.getCell(1, row).getContents().trim());
			datasetdata.put("type", sheet.getCell(2, row).getContents().trim());
			if (getDomain()!=null)
				datasetdata.put("domain_id", getDomain().getId().toString());
			addDataSet(datasetdata);
			return;
		}
		
		
		if (getState()==State.CLASSIFIERS) {
			
			if (isEmpty(sheet, row, 3))
				return;

			Map<String, String> cldata = new HashMap<String, String>();
			cldata.put("name", sheet.getCell(1, row).getContents().trim());
			cldata.put("dataset", sheet.getCell(2, row).getContents().trim());
			if (getDomain()!=null)
				cldata.put("domain_id", getDomain().getId().toString());
			addClassifier(cldata);
			return;
		}
		
		if (getState()==State.DATASETMEMBERS) {
			if (isEmpty(sheet, row, 3))
				return;
			Map<String, String> dmdata = new HashMap<String, String>();
			dmdata.put("dataset", sheet.getCell(1, row).getContents().trim());
			dmdata.put("value", sheet.getCell(2, row).getContents().trim());
			if (getDomain()!=null)
				dmdata.put("domain_id", getDomain().getId().toString());

			addDataSetMember(dmdata);
			return;
		}
	}
	
	/** -----------------------------------------------------------------------------
	 * 
	 * @param dmdata
	 */
	private void addDataSetMember(Map<String, String> dmdata) {
		try {
			DataSetMember datasetmember = KbeeValueMember.createFromMap(dmdata);
			if (datasetmember!=null) { 
				try {
					getDao().save(datasetmember);
					logger.info("Added DataSetMember: " + datasetmember.toString());
				} catch (ContentMgmtException e) {
					logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
				}
			}
		} catch (KbeeRuntimeException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
		}
	}
	/** -----------------------------------------------------------------------------
	 * 
	 * @param cldata
	 */
	private void addClassifier(Map<String, String> cldata) {
		/*try {
			Classifier classifier = KbeeClassifier.createFromMap(cldata);
			if (classifier!=null) { 
				try {
					getDao().save(classifier);
					logger.info("Added Classifier: " + classifier.toString());
				} catch (ContentMgmtException e) {
					logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
				}
			}
		} catch (KbeeRuntimeException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
		}*/
	}

	/** -----------------------------------------------------------------------------
	 * DataSet
	 */
	private void addDataSet(Map<String, String> datasetdata) {
		try {
			DataSet dataset = KbeeValueSet.createFromMap(datasetdata);
			if (dataset!=null) { 
				try {
					getDao().save(dataset);
					logger.info("Added DataSet: " + dataset.toString());
				} catch (ContentMgmtException e) {
					logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
				}
			}
		} catch (KbeeRuntimeException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
		}
	}
	/** -----------------------------------------------------------------------------
	 * 
	 * @param userdata
	 */
	private void addUser(Map<String, String> userdata) {
		/*try {
			 User user = UserImpl.createFromMap(userdata);
			if (user!=null) { 
				try {
					getDao().save(user);
					logger.info("Added User: " + user.toString());
				} catch (IOException e) {
					logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
				}
			}
		} catch (KbeeRuntimeException e) {
			logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
		}*/
	}
	/** -----------------------------------------------------------------------------                                 
	 * 
	 * @param domaindata
	 * 
	 */
	private void addDomain(Map<String, String> domaindata) {
		
		/*
		Domain domain = getDao().findDomainByName(domaindata.get("name"));
		if (domain==null) { 
			domain = KbeeDomain.createFromMap(domaindata);
			try {
				setDomain(domain);
				getDao().save(domain);
				logger.info("Adding Domain: " + domain.toString());
			} catch (ContentMgmtException e) {
				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			}
		}
		else
			logger.info("Setting existing Domain : " + domain.toString());
			*/
	}

	@Override
	protected void finalize() {
		// TODO Auto-generated method stub
		
	}
	
	
	

}
