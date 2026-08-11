package com.novamens.kbee.content.user;

import java.io.Serializable;
import java.util.List;

import org.hibernate.query.Query;
import org.hibernate.SessionFactory;

import com.novamens.content.model.LabelScope;
import com.novamens.content.user.UserLabel;
import com.novamens.content.user.UserLabelDao;
import com.novamens.security.User;

public class KbeeUserLabelDao implements UserLabelDao {

	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public UserLabel findLabelById(Serializable id) {
		return (KbeeUserLabel)sessionFactory.getCurrentSession().load(KbeeUserLabel.class, id);
	};
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<UserLabel> findLabelsByUser(User user) {
		String hql = "FROM KbeeUserLabel L WHERE L.user.id= '" + user.getId().toString() +"' order by lower(L.label)";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setCacheable(true);
		query.setCacheRegion("query");
		List results = query.list();
		return results;
	};
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<UserLabel> findLabelsByUser(User user, LabelScope scope) {
		String hql = "FROM KbeeUserLabel L WHERE L.user.id= '" + user.getId().toString() +"'  and  L.scope="+String.valueOf(scope.getId()) + "  order by lower(L.label)";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setCacheable(true);
		query.setCacheRegion("query");
		List results = query.list();
		return results;
	};

	public void update(UserLabel label) {
		sessionFactory.getCurrentSession().save(label);
	}

	@Override
	public void delete(UserLabel label) {
		sessionFactory.getCurrentSession().delete(label);
	}
	
	@Override
	public void setLabels(User user, List<UserLabel> list) {

		List<UserLabel> src = findLabelsByUser(user);
		
		if (src==null || src.isEmpty()) {
		}
		else {
//			Map<String, UserLabel> map = new HashMap<String, UserLabel>();
//			for (UserLabel label: list) {
//			}
		}
	}


}
