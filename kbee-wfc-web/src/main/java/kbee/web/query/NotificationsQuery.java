package kbee.web.query;

import java.io.Serializable;

import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.notification.NotificationState;
import com.novamens.content.notification.NotificationType;
import com.novamens.hibernate.query.HibernateQuery;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;

public class NotificationsQuery extends HibernateQuery {
	private static final long serialVersionUID = 1L;
	
	private boolean unreads = false;
	private NotificationType type = null;
	private Serializable userId; 
	
	public NotificationsQuery(User user) {
		userId = user.getId();
	}
	
	@Override
	public String getStatement() {
		
		StringBuilder statement = new StringBuilder();
		
		String criteria = "N.receiver.id=" + userId.toString() + " and N.notification_state=" + String.valueOf(NotificationState.PENDING.getId());
		
		if (type!=null) {
			criteria += " and N.type=" + type.getId(); 
		}
		
		if (unreads) {
			criteria += " and N.dateread is null"; 
		}
		
		statement.append("FROM KbeeNotification N WHERE ");
		statement.append(criteria);
		statement.append(" order by N.datesent desc");
		
		String sizeQuery = "select count (*) FROM KbeeNotification N WHERE " + criteria;
		setSizeQuery(sizeQuery);
		
		setStatement(statement.toString());

		return statement.toString();
	}
	
		
	public NotificationType getType() {
		return type;
	}

	public void setType(NotificationType type) {
		this.type = type;
	}
	

	public boolean isUnreads() {
		return unreads;
	}

	public void setUnreads(boolean unreads) {
		this.unreads = unreads;
	}

	@Override
	public SessionFactory getSessionFactory() {
		return (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
	}
}