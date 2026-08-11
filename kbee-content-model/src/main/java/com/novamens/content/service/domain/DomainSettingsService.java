package com.novamens.content.service.domain;

import java.util.List;

import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.notes.Billboard;
import com.novamens.dom.Json;
import com.novamens.service.BusinessObjectService;

public interface DomainSettingsService extends BusinessObjectService  {

						
	static final String START_HOUR 			= "WorkStartHour";
	static final String END_HOUR 			= "WorkEndHour";
	 
	 
	 static final String EMAIL_SERVICE_STATUS 			= "emailServiceStatus";
	 static final String EMAIL_SERVICE_NO_REPLY 		= "emailServiceNoReply";
	 
	 static final String CONSOLES_SHOW_RESOURCES 		= "consolesShowResources";
	 static final String CONSOLES_ENABLE_TEMPLATE 		= "consolesEnableTemplate";
	 static final String CONSOLES_PERSISTS_LABELS 		= "consolesPersistLabels";
	 static final String CONSOLES_TEMPLATE_NAME 		= "consolesTemplateName";
	 static final String WORKFLOW_ENABLE_PENDING_TASKS	= "workflowEnablePendingTasks";
	 static final String RESTRICT_ACCOUNT_INFO_EDITION  = "restrictAccountInfoEdition";
	 static final String TIP_OF_THE_DAY					= "tipOfTheDay";
	 static final String PORTAL							= "portal";
	 static final String CALENDAR_NON_WORKABLE_DAYS 	= "nonworkabledays";
	 
	 static final String CUTOFF_TIME					= "cutoffTime";
	 static final String LIBRARY						= "library";
	 
	 
	public void save(Billboard note) throws ContentMgmtException;
	public Billboard createBillboard () throws ContentCreationException, ContentMgmtException;
	
	
	public Billboard createRegularAlert(String title, String text) throws ContentCreationException, ContentMgmtException;
	public Billboard createBillboard(String title, String text) throws ContentCreationException, ContentMgmtException;
	
	public Billboard createBillboard(String title, String text, boolean isAlert) throws ContentCreationException, ContentMgmtException;
	
	
	public Billboard createWelcomeBillboard() throws ContentCreationException, ContentMgmtException;
	public List<Billboard> getBillboards();
	public void update(Billboard note) throws ContentMgmtException;
		
		
	public void SetValues(Json values);
	public Json getValues();
	
	public void SetValues(String category, Json values);
	public Json getValues(String category);

	
	public void delete();
	public void delete(String category);
	
	public void update(DomainSettings domainSettings);
	public DomainSettings create();
	
	public String get(String label, String category);
	public String get(String label);
	
	void remove(Billboard note) throws ContentMgmtException;

	void SetValues(Json settings, List<String> updatedParts);


	
	
	
	
	
	
	
	
	
	

}
