package com.novamens.kbee.portal.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.Content;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.PortalDao;
import com.novamens.portal.diagrammablesite.dao.PortalDiagrammableDao;
import com.novamens.portal.service.ViewService;
import com.novamens.portal6.model.ViewBK;
import com.novamens.portal6.model.ViewDetailContent;

/***
 * 
 * View Detail Service
 * 
 */
public class KbeeViewService implements ViewService {

	/**
	 * Logger Sync with the Trx Thread
	 */
	@SuppressWarnings("unused")
	static private Logger txlogger = LogManager.getLogger("TXLogger");

	@SuppressWarnings("unused")
	static final private org.apache.logging.log4j.Logger logger = LogManager.getLogger(KbeeViewService.class.getName());

	private PortalDao dao = null;
	
	private ViewBK view;

	public KbeeViewService() {
	}

	public KbeeViewService(ViewBK view) {
		this.view = view;
	}

	
	@Override
	public String getSubtitle() {
		return view.getSubtitle();
	}
	
	
	
	@Override
	@Transactional
	public void save() throws ContentMgmtException {
		getPortalDao().save(view);
		// txlogger.info();
	}

	@Override
	@Transactional
	public void delete() throws ContentMgmtException {
		getPortalDao().delete(view);
		// txlogger.info();
	}

	/**
	@Override
	@Transactional
	public void updateContent(Content content) throws ContentMgmtException {
		this.view.setContent(content);
		getPortalDao().save(view);
		// txlogger.info();
	}
**/
	
/**	
	public ViewDetailContent getViewDetailContent() {
		return this.view;
	}
**/
	
	public PortalDao getPortalDao() {
		return this.dao;
	}

	public void setPortalDao(PortalDao dao) {
		this.dao = dao;
	}

	
	
	

}
