package com.novamens.kbee.content.io;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.orgchart.OrgChart;
import com.novamens.content.service.ContentService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.orgchart.KbeeOrgChart;

import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;

public class XLSOrgChartImport extends XLSAbstractParser {

	private org.apache.logging.log4j.Logger logger = LogManager.getLogger(this.getClass().getName());
	
	final private static int NUM_COL = 8;
	
	
	private Map<String, String> data = new HashMap<String, String>();
	
	public XLSOrgChartImport(File file) {
		super(file);
	
	}

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
    				Domain domain = getDao().findDomainByName(sheet.getCell(1, row).getContents());
    				if (domain!=null) { 
    					setDomain(domain);
    					return;
    				}
    				else
    					logger.error("Can not set Domain " + sheet.getCell(1, row).getContents());
    		}
    		return;
		}

		if (getState()==State.ORG_NAME) {
			put(data, "name", sheet.getCell(1, row).getContents().trim());
			return;
		}

		
		if (getState()==State.ORG_DESCRIPTION) {
			put(data, "description", sheet.getCell(1, row).getContents().trim());
			return;
		}
		
		
		if (getState()==State.ORG_MISSION) {
			put(data, "mission", sheet.getCell(1, row).getContents().trim());
			return;
		}
		
		if (getState()==State.DATA) {
			
			if (sheet.getCell(1, row).getContents()==null || sheet.getCell(1, row).getContents().length()==0)
				return;
			
			StringBuilder str =  new StringBuilder();

			for (int i=0; i<NUM_COL; i++) {
				
				if (i>0)
					str.append(";");
				
				if (sheet.getCell(i, row).getContents()!=null && sheet.getCell(i, row).getContents().length()>0)
					str.append(sheet.getCell(i, row).getContents().trim().replaceAll(";", "\\;")); 
				else
					str.append("null"); 
			}
			
			if (str.length()>0)
				put(data, "node", str.toString());
			
			return;
		}
	}

	@Override
	protected void finalize() {
		
		OrgChart orgchart = KbeeOrgChart.createFromMap(data);

		if (orgchart!=null) {
			try {
				ContentService contentService = orgchart.getService(ContentService.class);
				contentService.update();
				
				logger.info("Added OrgChart: " + orgchart.toString());
			} catch (ContentMgmtException e) {
				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			}
		}

		
		
	}
}
