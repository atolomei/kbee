package com.novamens.kbee.timer;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.FlushModeType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;

import com.novamens.timer.Timer;
import com.novamens.timer.TimerDao;

public class KbeeTimerDao implements TimerDao {

	private SessionFactory sessionFactory;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeTimerDao.class.getName());
	
	public KbeeTimerDao() {
	}
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public void update(Timer timer) {
		sessionFactory.getCurrentSession().save(timer);
	}
	
	@Override
	public void delete(Timer timer) {
		sessionFactory.getCurrentSession().delete(timer);
	}
	
	@Override
	public List<Timer> getTimersAt(OffsetDateTime time) {
		CriteriaBuilder criteriabuilder = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<KbeeTimer> criteria = criteriabuilder.createQuery(KbeeTimer.class);
		Root<KbeeTimer> timerscriteria = criteria.from(KbeeTimer.class);
		ParameterExpression<OffsetDateTime> timeparameter = criteriabuilder.parameter(OffsetDateTime.class);
		criteria.select(timerscriteria).where(criteriabuilder.lessThan(timerscriteria.get("dueDate"), timeparameter));
		TypedQuery<KbeeTimer> query = sessionFactory.getCurrentSession().createQuery(criteria);
		query.setHint("org.hibernate.cacheable", true);
		query.setFlushMode(FlushModeType.COMMIT);
		query.setParameter(timeparameter, time);
		
		logger.debug(query.toString());
		
		List<KbeeTimer> kbeetimers =  query.getResultList();
		List<Timer> timers =  new ArrayList<Timer>();
		timers.addAll(kbeetimers);
		return timers;
	}
}
