package com.novamens.kbee.lock;

import java.util.List;

import org.hibernate.query.Query;
import org.hibernate.SessionFactory;

import com.novamens.lock.Lock;
import com.novamens.lock.LockDao;

public class KbeeLockDao implements LockDao {
	private SessionFactory sessionFactory;

	
	public KbeeLockDao() {
	}

	public KbeeLockDao(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
 
	public void save(Lock lock) {
		sessionFactory.getCurrentSession().save(lock);
	}
	
	public void delete(Lock lock) {
		sessionFactory.getCurrentSession().delete(lock);
		sessionFactory.getCurrentSession().flush();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Lock> findByObject(String objectId){
		Query query = sessionFactory.getCurrentSession().createQuery("from KbeeLock where objectId=:objectId");
		query.setParameter("objectId", objectId);
		List list = query.list();
		return list;
	}
}
