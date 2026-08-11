package com.novamens.kbee.content.webapi.command;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.service.ContentService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.security.Identifiable;
import com.novamens.transaction.Transaction;

public class DeleteFilesCommand extends ApiCommand {
			
	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(DeleteFilesCommand.class.getName());
	
	private int index = 0;
	private int total = 0;
	private String criteria;
	private boolean recycle;
	private List<Serializable> files = null; 
	
	public DeleteFilesCommand(String criteria, boolean recycle) {
		super("Delete Batch Commnad");
		this.criteria = criteria;
		this.recycle = recycle;
	}

	public void executeAsync() {
		
		Transaction transaction = null;
		try {
			com.novamens.hibernate.session.Session.open();
			authenticate(getUserName());
			transaction = beginTransaction();
			for(index = 0; index<getFiles().size(); index++) {
				Content content = getApiDao().findContentById((Long)getFiles().get(index));
				if (recycle) {
					content.getService(ContentService.class).recycle();
				}
				else {
					content.getService(ContentService.class).deleteAllVersions();
				}
				if (index++%2==0) {
					transaction.commit();
					transaction = beginTransaction();
				}
			}
			index--;
			transaction.commit();
			end();
		}
		catch (ContentMgmtException e) {
			e.printStackTrace();
			transaction.rollback();
			setResult(e.getMessage());
			stop();
		}
		finally {
			com.novamens.hibernate.session.Session.close();
		}
	}
	
	@Override
	public double getProgress() {
		try {
			if (getTotal()==0) return 0;
			double progress = (double)(index)/(double)getTotal()*100;
			return progress;
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public int getTotal() {
		if (total>0) return total;
		total = getFiles().size();
		return total;
	}
	
	public String getCriteria() {
		return criteria;
	}
	
	private List<Serializable> getFiles() {
		if (files!=null) return files;
		files = new ArrayList<Serializable>();
	 	ResultSet resultSet = null;
		try {
			resultSet = getApiDao().executeIql(getCriteria());
			
			while (resultSet.hasNext()) {
				files.add(((Identifiable)resultSet.next().getObject()).getId());
			}
			
			return files;
		}
		finally {
			if (resultSet!=null)
			 	resultSet.close();
		}
	}
}
