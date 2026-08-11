package com.novamens.kbee.content.io;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;

import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.QuestionAnswerDao;
import com.novamens.content.questionanswer.Answer;
import com.novamens.content.questionanswer.Question;
import com.novamens.content.social.Comment;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.dao.KbeeQuestionAnswerDao;
import com.novamens.kbee.content.questionanswer.KbeeAnswer;
import com.novamens.kbee.content.questionanswer.KbeeQuestion;
import com.novamens.kbee.content.social.KbeeComment;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;

public class XLSQAImport extends XLSAbstractParser {

	private org.apache.logging.log4j.Logger logger = LogManager.getLogger(this.getClass().getName());

	QuestionAnswerDao qadao = KbeeQuestionAnswerDao.getInstance();
	
	public XLSQAImport(File file) {
		super(file);
	}

	/**
	 * Process row
	 * 
	 */
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
		
		if (getState()==State.CONTENT_TYPE) {
			setContentType(sheet.getCell(1, row).getContents().trim());
		}
				
		if (getState()!=State.DATA)
			return;
		
		if (getContentType()==null) 
			return;
		
		if (getContentType().toLowerCase().equals("answer"))
			processAnswer(sheet, row);
			
		if (getContentType().toLowerCase().equals("comment"))	
			processComment(sheet, row);
			
		if (getContentType().toLowerCase().equals("question"))
			processQuestion(sheet, row);
	}
	
	/**
	 * Answer
	 * 
	 * @param sheet
	 * @param row
	 */
	private void processAnswer(Sheet sheet, int row) {

		Map<String, String> data = new HashMap<String, String>();
		
		put(data, "name", sheet.getCell(1, row).getContents().trim());
		put(data, "question", sheet.getCell(2, row).getContents().trim());
		put(data, "user", sheet.getCell(3, row).getContents().trim());
		put(data, "date", "7/4/2014");
		put(data, "title", sheet.getCell(5, row).getContents().trim());
		put(data, "text",  sheet.getCell(6, row).getContents().trim());
		
		// Save Answer
		Answer answer = KbeeAnswer.createFromMap(data);
		
		if (answer!=null) {
					try {
						getDao().save(answer);
						logger.info("Added Answer: " + answer.toString());
					} catch (ContentMgmtException e) {
						logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
					}
				}
	}
 	/**
	 * Comment
	 * 
	 * @param sheet
	 * @param row
	 */
	private void processComment(Sheet sheet, int row) {

		Map<String, String> data = new HashMap<String, String>();
		
		String ctype = sheet.getCell(2, row).getContents();

		put(data, "name", sheet.getCell(1, row).getContents().trim());
		put(data, "text", sheet.getCell(5, row).getContents().trim());
		put(data, "user", sheet.getCell(4, row).getContents().trim());
		put(data, "date", "7/4/2014");		
				
		if (ctype.toLowerCase().trim().equals("question")) {
			put(data, "parent", sheet.getCell(3, row).getContents().trim());

			String qname = sheet.getCell(3, row).getContents().trim();
			Question question = qadao.findQuestionByName(qname, getDomain().getId());
			
			if (question!=null) {
				Comment comment = new KbeeComment();
				comment.setDateSubmitted(OffsetDateTime.now());
				comment.setText(data.get("text"));

				User user = ServiceLocator.getService(SecurityService.class).findUserByUsername(data.get("user"));
				
				if (user!=null) {
					comment.setUser(user);
					question.addComment(comment);
				}
				else
					logger.error("User does not exist: " +data.get("user"));
			}
			else
				logger.error("Question does not exist: " +qname);
			
		}
		else if (ctype.toLowerCase().trim().equals("answer")){
			put(data, "parent", sheet.getCell(3, row).getContents().trim());

			String aname = sheet.getCell(3, row).getContents().trim();
			Answer answer = qadao.findAnswerByName(aname, getDomain().getId());
			
			if (answer!=null) {
				Comment comment = new KbeeComment();
				comment.setDateSubmitted(OffsetDateTime.now());
				comment.setText(data.get("text"));

				User user = ServiceLocator.getService(SecurityService.class).findUserByUsername(data.get("user"));

				
				if (user!=null) {
					comment.setUser(user);
					answer.addComment(comment);
				}
				else
					logger.error("User does not exist: " +data.get("user"));
			}
			else
				logger.error("Answer does not exist: " +aname);
		}
		else
			return;
	}
	
	/**
	 * Question
	 * 
	 * @param sheet
	 * @param row
	 */
	private void processQuestion(Sheet sheet, int row) {
	
		Cell cell;
		CellType type;

		Map<String, String> data = new HashMap<String, String>();
		
		put(data, "name", sheet.getCell(1, row).getContents().trim());
		put(data, "title", sheet.getCell(2, row).getContents().trim());
		put(data, "text", sheet.getCell(3, row).getContents().trim());
		
		// classifiers
		//
		//
		int n = 4;		
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
		
		// Save Question
		Question question = KbeeQuestion.createFromMap(data);
		if (question!=null) {
			try {
				getDao().save(question);
				logger.info("Added Question: " + question.toString());
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
