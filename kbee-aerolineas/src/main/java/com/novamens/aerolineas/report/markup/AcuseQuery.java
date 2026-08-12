package com.novamens.aerolineas.report.markup;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.entity.Person;
import com.novamens.content.model.Classification;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.EntityMember;
import com.novamens.content.notification.Notification;
import com.novamens.content.security.ContentSecurityDao;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserRole;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;

import kbee.web.report.ReportQuery;
import kbee.web.report.Row;

public class AcuseQuery extends ReportQuery {
			
	private static final long serialVersionUID = 1L;
					
	static final int FIELD_DATEREAD = 4;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(AcuseQuery.class.getName());
 	
	public AcuseQuery() {
		super();
	}
	
	public Content getContent() {
		Serializable contentId = (Serializable)getParameters().get("content");
		if (contentId==null) return null;
		Content content = getContentDao().findContentById(contentId);
		return content;		
	}
	
	protected List<Row> getRows() {
		ArrayList<Row> rows;
		
		rows = new ArrayList<Row>();
		
		Map<Serializable, Object[]> users = getUsers();
		System.out.println("USERS " + users.size());
			
		for (Notification notification : getNotifications()) {
			Object[] userdata = users.get(notification.getReceiver().getId());
			if (userdata!=null) {
				userdata[0] = notification.getOffsetDateTimeSent();
				if (notification.getDateRead()!=null) {
					userdata[FIELD_DATEREAD] = notification.getDateRead();
				}
			}
		}
		
		for (Object[] userdata : users.values()) {
			Row row = new Row();
			try {
				row.putValue("datepublish", (Serializable)userdata[0]);
				row.put("user", (String)userdata[1]);
				row.put("user-firstname", (String)userdata[2]);
				row.put("group", (String)userdata[3]);
				row.putValue("dateread", (Serializable)userdata[FIELD_DATEREAD]);
				rows.add(row);
			} catch (Exception e) {
				Row r=getErrorRow(rows, e.getClass().getSimpleName());
				if (r!=null) rows.add(r);
				logger.error(e);
			}
		}
		
		final String sort = getParameters().get("sort")==null ? "user" : (String)getParameters().get("sort");
		final String ascending = getParameters().get("ascending")==null ? "true" : (String)getParameters().get("ascending");
		
		if (sort!=null) {
			Collections.sort(rows, new Comparator<Row>() {
				@Override
				public int compare(Row a, Row b) {
					try {
						if ("user".equals(sort)) {
							return "true".equals(ascending) ? 
								a.get("user").toLowerCase().compareToIgnoreCase(b.get("user").toLowerCase()) :
								b.get("user").toLowerCase().compareToIgnoreCase(a.get("user").toLowerCase());
						}
						if ("group".equals(sort)) {
							return "true".equals(ascending) ? 
								a.get("user").compareToIgnoreCase(b.get("group")) :
								b.get("user").compareToIgnoreCase(a.get("group"));
						}
						if ("dateread".equals(sort)) {
							OffsetDateTime datea = (OffsetDateTime)a.getValue("dateread");
							OffsetDateTime dateb = (OffsetDateTime)b.getValue("dateread");
							if ("true".equals(ascending)) {
								if (datea==null) return -1;
								if (dateb==null) return 1;
								return datea.compareTo(dateb);
							}
							else {
								if (datea==null) return 1;
								if (dateb==null) return -1;
								return dateb.compareTo(datea);
							}
						}
						return 0;
					}
					catch (Exception e) {
						logger.error(e);
						return 0;
					}
					finally {
					}
				}
			}); 
		}
		
		return rows;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Notification> getNotifications() {
		List<Notification> notifications = new ArrayList<Notification>();
		Content content = getContent();
		if (content == null)
			return notifications;
		String hql = "FROM KbeeContentPublishNotification N WHERE N.deleteOnAccept=false AND N.content.id= " + content.getId().toString() + " order by N.datesent";
		org.hibernate.query.Query<?> query = getSessionFactory().getCurrentSession().createQuery(hql);
		List results = query.list();
		if (results.isEmpty()) {
			return notifications;
		}	
		return results;
	}
	
	private Map<Serializable, Object[]> getUsers() {
		Content content = getContent();
		if (content==null) 
			return null;
		
		Map<Serializable, Object[]> users = new HashMap<Serializable, Object[]>();
		for (Classification classification : getContent().getClassification()) {
			if (classification.getClassifier().getAlias().equals("acuserecibo")) {
				System.out.println("CLASSIFIER " + classification.getClassifier().getName());
				if (classification.getDataSetMember()!=null)
				for (Person member : getUsers(classification.getDataSetMember())) {
					System.out.println("PERSON " + member.getDisplayName());
					UserProfile profile = member.getProfile(UserProfile.class);
					if (profile!=null && profile.getUser()!=null) {
						User user = profile.getUser();
						Object data[] = new Object[5];
						data[0] = null;  // timestamp created
						data[1] = member.getLastName();
						data[2] = member.getFirstName();
						data[3] = classification.getDataSetMember().getDisplayName();
						data[FIELD_DATEREAD] = null; // timestamp read
						users.put(user.getId(), data);
						System.out.println("USER " + member.getDisplayName());
					}
				}
			}
		}
		
		return users;
	}
	
	public Set<Person> getUsers(DataSetMember member) {
		Set<Person> users= new HashSet<Person>();
		if (!(member instanceof EntityMember)) return users;
		EntityMember entity = (EntityMember)member;
		for (UserRole userRole :  getSecurityDao().findUserRolesByEntityMember(entity)) {
			if ("acuse".equals(userRole.getRole().getAlias())) {
				Person person = userRole.getPerson();
				if (person!=null)
				users.add(person);
			}
		}
		return users;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private ContentSecurityDao getSecurityDao() {
		return	(ContentSecurityDao)ServiceLocator.getService(BeansService.class).getBean("contentSecurityDao");
	}
	
	private SessionFactory getSessionFactory() {
		return (SessionFactory)ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
	}
}
