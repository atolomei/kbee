package com.novamens.kbee.content.io;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.document.IDoc;
import com.novamens.content.resource.KBFile;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.document.KbeeIDoc;

import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;

public class XLSIDOCImport extends XLSAbstractParser {
	
	private org.apache.logging.log4j.Logger logger = LogManager.getLogger(this.getClass().getName());
	
	public XLSIDOCImport(File file) {
		super(file);
	}

	@Override
	public void execute() throws IOException {
		read();
	}

	@Override
	protected void processRow(Sheet sheet, int row) {

		Cell cell;
		CellType type;

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

		if (getState()==State.FILEDIRECTORY) {
			cell = sheet.getCell(1, row);
    		type = cell.getType();
			if (type == CellType.LABEL && cell.getContents().trim().length()>0)
				setRoot(sheet.getCell(1, row).getContents());
			return;
		}
		
		if (getState()!=State.DATA)
			return;
		
		Map<String, String> data = new HashMap<String, String>();
		
		put(data, "name", sheet.getCell(1, row).getContents().trim());
		put(data, "title", sheet.getCell(2, row).getContents().trim());
		
		// file
		String filestr = sheet.getCell(4, row).getContents().trim();
		KBFile kbfile = addImage(filestr);

		if (kbfile!=null)
			data.put("file1", kbfile.getId().toString());
		else {
			return;
		}
		
		// classifiers
		String str = getClassifiersStr(5, sheet, row);
		if (str.length()>0)
				data.put("classification", str);
		
		IDoc idoc = null; /*KbeeIDoc.createFromMap(data);*/
		
		if (idoc!=null) {
			try {
				getDao().save(idoc);
				logger.info("Added IDoc: " + idoc.toString());
			} catch (ContentMgmtException e) {
				logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
			}
		}
	}

	@Override
	protected void finalize() {
		// TODO Auto-generated method stub
		
	}

}
