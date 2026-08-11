package com.novamens.kbee.content.service;


import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.base.Content;
import com.novamens.content.properties.PropertyService;
import com.novamens.content.service.Label;
import com.novamens.content.service.LabelsService;
import com.novamens.content.user.UserLabel;
import com.novamens.content.user.UserLabelDao;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.service.IndexerException;
import com.novamens.indexer.service.JavaIndex;


/**
 * 
 *
 */
public class KbeeLabelsService implements LabelsService {
	
	private Content content = null;
	private UserLabelDao labelDao;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeLabelsService.class.getName());
	
	public KbeeLabelsService() {
	}

	
	public KbeeLabelsService(Content content) {
		 this.content = content;
	}


	public void setLabel(String label) {
		List<String> labels = getLabels();
		if (!labels.contains(label)) {
			labels.add(label);
			setProperty(labels);
		}
	};


	@Transactional
	public void setLabelForAssign() {
		
		List<String> labels = getLabels();
		if (!labels.contains(Label.ASSIGNED)) {
			labels.add(Label.ASSIGNED);
			setProperty(labels);
		}
	}


	@Transactional
	public void setLabel(UserLabel label) {
		List<String> labels = getLabels();
		String labelstr = "u"+label.getId();
		if (!labels.contains(labelstr)) {
			labels.add(labelstr);
			setProperty(labels);
			reindex(getContent());
		}
	};


	@Transactional
	public void removeLabel(String label) {
		List<String> labels = getLabels();
		if (labels.contains(label)) {
			labels.remove(label);
			setProperty(labels);
		}
	};


	@Transactional
	public void removeLabel(UserLabel label) {
		List<String> labels = getLabels();
		String labelstr = "u"+label.getId();
		if (labels.contains(labelstr)) {
			labels.remove(labelstr);
			setProperty(labels);
			reindex(getContent());
		}
	};


	@Transactional
	public void removeUserLabelById(String id) {
		List<String> labels = getLabels();
		String labelstr = "u"+id;
		if (labels.contains(labelstr)) {
			labels.remove(labelstr);
			setProperty(labels);
			reindex(getContent());
		}
	};


	public void removeAll() {
		getContent().getService(PropertyService.class).removeProperty("labels");
	}
	

	public boolean labeled(String label) {
		return getLabels().contains(label);
	};


	public boolean labeled(UserLabel label) {
		return getLabels().contains("u"+label.getId());
	};
	


	public List<String> getLabels() {
		List<String> labels = new ArrayList<String>();
		String strvalue = (String)getContent().getService(PropertyService.class).getProperty("labels");
		if (strvalue==null) 
			return labels;
		StringTokenizer tokenizer = new StringTokenizer(strvalue, ";");
		while (tokenizer.hasMoreTokens()) {
			labels.add(tokenizer.nextToken().trim());
		}
		return labels;
	};
	

	public List<UserLabel> getUserLabels() {
		List<UserLabel> labels = new ArrayList<UserLabel>();
		
		String strvalue = (String)getContent().getService(PropertyService.class).getProperty("labels");		
	
		if (strvalue==null) 
			return labels;
		
		StringTokenizer tokenizer = new StringTokenizer(strvalue, ";");
		
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken().trim();
			if (token.startsWith("u")) {
				try {
					UserLabel label = labelDao.findLabelById(Long.valueOf(token.substring(1)));
					if (label!=null)
						labels.add(label);
					else {
						logger.warn("label is null.");
					}
					
				} catch (org.hibernate.ObjectNotFoundException e) {
						logger.warn(e);
				}
			}
		}
		return labels;
	}


	public Content getContent() {
		return content;
	}


	public void setLabelDao(UserLabelDao dao) {
		labelDao = dao;
	}
	

	private void setProperty(List<String> labels) {
		StringBuffer buffer = new StringBuffer();
		boolean first = true;
		for (String label : labels) {
			if (!first) buffer.append(";");
			buffer.append(label);
			first = false;
		}
		getContent().getService(PropertyService.class).setProperty("labels", buffer.toString());
	}
	

	private void reindex(Content content) {
		try { 
			JavaIndex index =  (JavaIndex)content.getDomain().getService(JavaIndexerService.class).getIndex();
			index.index(content);
		}
		catch (IndexerException e) {
			logger.error(e);
		}
	}
}
