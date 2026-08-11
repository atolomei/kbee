package com.novamens.kbee.portal.model.library;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.kbee.portal.model.KbeeBlock;
import com.novamens.portal6.model.ViewBKIQL;
import com.novamens.portal6.model.block.ListBlock;
import com.novamens.service.ServiceLocator;

@Entity
@PrimaryKeyJoinColumn(name = "po_id")
@Table(name = "po_block_generic_content_list")
public class KbeeBlockGenericContentList extends KbeeBlock implements ListBlock<Content> {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeBlockGenericContentList.class.getName());
	
	/**
	 * IQL
	 * Query (saved query)
	 */
	public KbeeBlockGenericContentList() {
	}
	
	public KbeeBlockGenericContentList(String title) {
		setTitle(title);
	}
	
	public void setExpander(String expander) {
		
	}
	
	public String getExpander() {
		return null;
	}
	
	@Override
	public List<Content> getItems() {

		try {
			
			if (getCustomValuesJson()!=null) {
				if (getCustomValuesJson().get("query_type")!=null) {
						String qt = getCustomValuesJson().get("query_type").toString();
						if (qt.equals(ViewBKIQL.IQL_TYPE)) {
							if ( (getCustomValuesJson().get("statement")!=null) ) {
								String iql= getCustomValuesJson().get("statement").toString();
								logger.debug(iql);
								if (iql!=null) {
									String sort = (String)getCustomValuesJson().get("sort");
									List<Content> list=getPortalDao().getLibrarySiteIQLContents(getSite(), iql, 20, sort);
									return list;
								}
							}			
						}
						else {
							String qery= getCustomValuesJson().get("statement").toString();
							logger.debug(qery);
							if (qery!=null) {
								// TODO AT
								// iql="head=true,domain=250,-isknowledgebase=true,member=pmcmember/50632,-isexternal=true,-istemplate=true,sort=modified,state=1,type=[text, idoc],ascending=false";
								List<Content> list=getPortalDao().getLibrarySiteQueryContents(getSite(), qery, 30);
									return list;
							}
						}
				}
			}
			
		} catch (Exception e) {
			logger.error(e);
		}
		return new ArrayList<Content>();
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
}


