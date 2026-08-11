package com.novamens.kbee.content.io;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.resource.KBFile;
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;

import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;


public abstract class XLSAbstractParser {

	private ContentDao dao;
	private String root = null;
	private Domain domain = null;
	private File file;	
	private String contentType;
	
	protected void setRoot(String root) {
		this.root = root;
	}
	
	protected String getRoot() {
		return this.root;
	}
	
	private org.apache.logging.log4j.Logger logger = LogManager.getLogger(this.getClass().getName());
	
	protected Domain getDomain() {
		return this.domain;
	}

	protected ContentDao getDao() {
		return dao;
	}

	
	protected void setDomain(Domain dom) {
		this.domain=dom;
	}
	
	
	protected KBFile addImage(String imagestr) {
		
		KBFile kbfile = null;
		if ( imagestr!=null &&  imagestr.length()>0  &&  !imagestr.equals("null") ) {
			Map<String, String> imgdata = new HashMap<String, String>();

			imgdata.put("name", imagestr);
			imgdata.put("image", getRoot() + File.separator + imagestr);
			//kbfile = KBFileImpl.createFromMap(imgdata);
			try {
				
				getDao().save(kbfile);
				
				//logger.info("Added File: " + kbfile.toString());
	 			return kbfile;
				
			} catch (ContentMgmtException e) {
				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
				return null;
			}
		}
		return kbfile;
	}
	
	
	protected void put(Map<String, String> data, String key, String value ) {
		if (key!=null) {
			if (value!=null && !value.equals("null"))
				data.put(key, value);
		}
	}

	public XLSAbstractParser(File file) {
		this.file=file;
		BeansService beans = ServiceLocator.getService(BeansService.class);
		dao = (ContentDao) beans.getBean("contentDao");
	}
	 
	protected File getFile() { 
		return file; 
	}
	
	public enum State {
		INITIAL 		(0, "Initial"),
		DOMAIN 			(1, "Domain"), 
		USERS 			(2, "Users"),
		CONTENT_TYPE 	(3, "Content type"),
		DATASETS 		(4, "Datasets"),
		CLASSIFIERS		(5, "Classifiers"),
		DATASETMEMBERS	(6, "DataSetMembers"),
		DATA	 		(7, "Datasets"),
		ERROR 			(8, "Error"),
		FILEDIRECTORY	(9, "Directory"),
		ORG_NAME 		(10, "OrgName"),
		ORG_MISSION 	(12, "OrgMission"),
		ORG_DESCRIPTION (13, "OrgDirectory"),
		FINAL 			(14, "Final");
		private String label;
	    private int id;
	    private  State(int code, String label) {this.label = label;this.id = code;}
	    public String toString() {return ("id: " + getId() + "  label: "+ getLabel());} 
	    public String getLabel() {return label;}
	    public int getId() {return id;}
	}
	
	private State state = State.INITIAL;
	
	public State getState() { 
		return state;
	}

	public String getContentType() {
		return contentType;
	}
	
	public void setContentType(String type) {
		this.contentType = type;
	}
	
	protected void setState(State st) {
		state = st;
	}

	protected void changeState(Cell cell) {
		
		String content = cell.getContents().toLowerCase().trim();
		
		if (content.equals("domain")) {
			state=State.DOMAIN;
		}
		else if (content.equals("users")){
			state=State.USERS;
		}
		else if (content.equals("data")){
			state=State.DATA;
		}
		else if (content.equals("dataset")){
			state=State.DATASETS;
		}
		else if (content.equals("classifier")){
			state=State.CLASSIFIERS;
		}
		else if (content.equals("datasetmember")){
			state=State.DATASETMEMBERS;
		}
		else if (content.equals("directory")){
			state=State.FILEDIRECTORY;
		}
		else if (content.equals("Content type")){
			state=State.CONTENT_TYPE;
		}
	}

	
	/**
	 * 
	 * @throws IOException
	 */
	protected void read() throws IOException  {
	    File inputWorkbook = file;
	    Workbook w;
	    try {
	    	w = Workbook.getWorkbook(inputWorkbook);
	    	for (int tabs=0; tabs<w.getNumberOfSheets();tabs++) {
	    		Sheet sheet = w.getSheet(tabs);
	    		setState(State.INITIAL);
	    		for (int i = 0; i < sheet.getRows(); i++) {
	    			Cell cell = sheet.getCell(0, i);
	    			CellType type = cell.getType();
	    			if (type == CellType.LABEL) 
	    				changeState(cell); 
	    			else 
	    				processRow(sheet, i);
	    		}
	    	}
	    	
	    	finalize();
	    	
	    } catch (BiffException e) {
	    		logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
	    }
	  }

	protected boolean isEmpty(Sheet sheet, int row, int tocol){
		for (int col=0; col<tocol; col++) {
			if (sheet.getCell(col, row).getContents().toString().trim().length()>0)
				return false;
		}
		return true;
	}

	protected String getClassifiersStr(int start, Sheet sheet, int row) {
		StringBuilder clas = new StringBuilder();
		boolean done = false;
		Cell cell;
		CellType type;
		int n = start;
		
		while (!done && n<sheet.getColumns()) {
			cell = sheet.getCell(n, row);
			type = cell.getType();
			String value =  cell.getContents().trim();
			if (type == CellType.LABEL && value.length()>0) {
				if (clas.length()>0) 
					clas.append(";");
				clas.append(value);
			}
			else
				done = true;
			n++;
		}
		return clas.toString();
	}
	
	public void execute() throws IOException {
		read();
	}
	
	protected abstract void processRow(Sheet sheet, int row);
	protected abstract void finalize();
	
	
}
