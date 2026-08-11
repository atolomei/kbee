package com.novamens.content.web.admin.markup.datamanagement;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.service.DomainLifeCycleService;
import com.novamens.dom.Domain;
import com.novamens.dom.DomainType;
import com.novamens.kbee.content.service.KbeeDomainLifeCycleService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeRuntimeException;


public class DomainDefaultTemplateCreationPanel extends Panel {
	 
	private static final long serialVersionUID = 1L;
	 
	static private final String ORGANIZATION 		= "Novamens";

	static private Logger logger = LogManager.getLogger(DomainDefaultTemplateCreationPanel.class.getName());

	
	/** -----------------------------------------------------------------------------------
	 */
	public DomainDefaultTemplateCreationPanel(String id) {
		super(id);
		
		add(new AjaxLink<Object>("create"){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				
				throw new KbeeRuntimeException("not supported anymore");
				
				//DomainLifeCycleService service = ServiceLocator.getService(DomainLifeCycleService.class);
				//try {
					
					// service.createDefaultDomainTemplate(true);
					
				//} catch (ContentMgmtException | ContentCreationException e) {
				//	e.printStackTrace();
				//}
			}
		});

		add(new AjaxLink<Object>("delete"){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				DomainLifeCycleService service = ServiceLocator.getService(DomainLifeCycleService.class);
				try {
						Domain dom = getContentDao().findDomainByName(KbeeDomainLifeCycleService.DEFAULT_DOMAIN_NAME);
						if (dom!=null && dom.getDomainType()==DomainType.SYSTEM) { 
								service.wipe(dom);
								logger.info("Template Domain deleted.");
						}
						else {
							logger.info("Template Domain does not exists.");
						}
					
				} catch (ContentMgmtException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/** -----------------------------------------------------------------------------------
	 */
	private ContentDao getContentDao() {
		BeansService beans = ServiceLocator.getService(BeansService.class);
		ContentDao dao = (ContentDao) beans.getBean("contentDao");
		return dao;
	}
}
