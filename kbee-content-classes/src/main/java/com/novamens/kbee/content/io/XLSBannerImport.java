package com.novamens.kbee.content.io;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;

import com.novamens.content.ad.Banner;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.ContentService;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.ad.KbeeBanner;

import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;

public class XLSBannerImport extends XLSAbstractParser {

	private org.apache.logging.log4j.Logger logger = LogManager.getLogger(this.getClass().getName());

	public XLSBannerImport(File file) {
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
    				Domain domain = (Domain) getDao().findDomainByName(sheet.getCell(1, row).getContents());
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
		put(data, "domain_id", getDomain().getId().toString());
		put(data, "title", sheet.getCell(2, row).getContents().trim());
		put(data, "text", sheet.getCell(3, row).getContents().trim());
		put(data, "link", sheet.getCell(4, row).getContents());
		put(data, "ga", sheet.getCell(5, row).getContents());
		put(data, "external", sheet.getCell(6, row).getContents());
		
		// classifiers
		int n = 8;		
		getClassifiersStr(n, sheet, row);
		
		StringBuilder clas = new StringBuilder();
		boolean done = false;
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

		
		data.put("classification", clas.toString());
		String imagestr = sheet.getCell(7, row).getContents().trim();
		
		// file
		KBFile kbfile = addImage(imagestr);
		if (kbfile!=null)
			data.put("image1", kbfile.getId().toString());
		else {
			return;
		}
		
		Banner banner = KbeeBanner.createFromMap(data);
		
		if (banner!=null) {
			try {
				// getDao().save(banner);
				ContentService contentService = banner.getService(ContentService.class);
				contentService.update();
				
				logger.info("Added Banner: " + banner.toString());
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
