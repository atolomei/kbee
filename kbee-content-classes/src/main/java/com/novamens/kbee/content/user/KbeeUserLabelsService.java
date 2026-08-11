package com.novamens.kbee.content.user;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.content.model.LabelScope;
import com.novamens.content.user.UserLabel;
import com.novamens.content.user.UserLabelDao;
import com.novamens.content.user.UserLabelsService;
import com.novamens.logging.DataSetValueCreateEvent;
import com.novamens.logging.DataSetValueDeleteEvent;
import com.novamens.logging.DataSetValueUpdateEvent;

import com.novamens.security.User;

/**
 * <p>Etiquetas de un usuario a un contenido.</p>
 */
public class KbeeUserLabelsService implements UserLabelsService  {
	private User user;
	private UserLabelDao labelDao;

	/** Logger that works synchronously (in the TRX thread) */
	static private Logger txlogger = LogManager.getLogger("TxLogger");

	
	public KbeeUserLabelsService() {
		
	}
	
	public KbeeUserLabelsService(User user) {
		 this.user = user;
	}
	
	@Override
	public List<UserLabel> getLabels() {
		return getLabelDao().findLabelsByUser(getUser());
	}
	
	
	@Override
	public List<UserLabel> getLabels(LabelScope scope) {
		return getLabelDao().findLabelsByUser(getUser(), scope);
	}
	
	@Override
	@Transactional 
	public void update(UserLabel label)  {
		if (label.getCss()!=null)
			label.setCss(label.getCss().toLowerCase());
		getLabelDao().update(label);
		txlogger.info(new DataSetValueUpdateEvent(label,"update"));
	}

	@Override
	@Transactional 
	public void delete(UserLabel label)  {
		getLabelDao().delete(label);
		txlogger.info(new DataSetValueDeleteEvent(label, "delete"));
	}

	@Override
	@Transactional 
	public UserLabel create(User user) {
		KbeeUserLabel label = new KbeeUserLabel();
		label.setUser(user);
		getLabelDao().update(label);
		txlogger.info(new DataSetValueCreateEvent(label,"create"));
		return label;
	}
	
	@Override
	@Transactional 
	public UserLabel create() {
		KbeeUserLabel label = new KbeeUserLabel();
		label.setUser(getUser());
		getLabelDao().update(label);
		txlogger.info(new DataSetValueCreateEvent(label,"create"));
		return label;
	}

	@Override
	public User getUser() {
		return user;
	}
	
	public UserLabelDao getLabelDao() {
		return labelDao;
	}
	
	public void setLabelDao(UserLabelDao dao) {
		this.labelDao = dao;
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED) 
	public UserLabel create(String strlabel) {
		KbeeUserLabel label = new KbeeUserLabel();
		label.setUser(getUser());
		label.setScope(LabelScope.Application);
		label.setLabel(strlabel);
		label.setCss( UserLabel.CSS[ Math.abs(strlabel.hashCode()) % UserLabel.CSS.length ].toLowerCase());
		getLabelDao().update(label);
		txlogger.info(new DataSetValueCreateEvent(label,"create"));
		return label;
	}
	

	
	
	/**
	 * This method is useb by the DomainCreation tool to assign the root user of the
	 * domain as creator instead of user root@kbee
	 * 
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED) 
	public UserLabel create(String strlabel, User user_creator) {
		KbeeUserLabel label = new KbeeUserLabel();
		label.setUser(getUser());
		label.setScope(LabelScope.Application);
		label.setLabel(strlabel);
		label.setCss( UserLabel.CSS[ Math.abs(strlabel.hashCode()) % UserLabel.CSS.length ].toLowerCase());
		getLabelDao().update(label);
		DataSetValueCreateEvent event = new DataSetValueCreateEvent(label,"create");
		event.setUserLabel(label, user_creator);
		txlogger.info(event);
		return label;
	}

	
	@Override
	@Transactional 
	public UserLabel create(String strlabel, String css) {
		KbeeUserLabel label = new KbeeUserLabel();
		label.setUser(getUser());
		label.setCss(css);
		label.setLabel(strlabel);
		getLabelDao().update(label);
		txlogger.info(new DataSetValueCreateEvent(label,"create"));

		return label;
	}
	
	@Override
	@Transactional 
	public UserLabel create(String strlabel, String css, LabelScope scope) {
		KbeeUserLabel label = new KbeeUserLabel();
		label.setUser(getUser());
		if (css!=null)
			label.setCss(css.toLowerCase());
		label.setLabel(strlabel);
		label.setScope(scope);
		getLabelDao().update(label);
		txlogger.info(new DataSetValueCreateEvent(label,"create"));
		return label;

	}
	
	@Override
	@Transactional 
	public UserLabel create(String strlabel, String css, LabelScope scope, User user_creator) {
		KbeeUserLabel label = new KbeeUserLabel();
		label.setUser(getUser());
		if (css!=null)
			label.setCss(css.toLowerCase());
		label.setLabel(strlabel);
		label.setScope(scope);
		getLabelDao().update(label);
		
		DataSetValueCreateEvent event = new DataSetValueCreateEvent(label,"create");
		event.setUserLabel(label, user_creator);
		txlogger.info(event);
		return label;

	}
	
}
