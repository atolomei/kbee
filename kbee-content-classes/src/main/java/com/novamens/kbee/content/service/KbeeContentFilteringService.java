package com.novamens.kbee.content.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeSet;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.questionanswer.Question;
import com.novamens.content.service.ContentFilteringService;
import com.novamens.content.social.SocialService;
import com.novamens.dom.Domain;
import com.novamens.hibernate.query.HibernateQuery;
import com.novamens.service.ServiceLocator;


/**
 * Service that provides filtered contents 
 * such as recommendations, most recent, featured contents, etc.
 * BusinessSystemService
 *
 */
public class KbeeContentFilteringService implements ContentFilteringService {

	static private long CACHE_DURATION = 1000 * 60 * 60 * 12; // 12 hours
	static private long CACHE_DURATION_HEAVY_PROCESSING = 1000 * 60 * 60; // 1 hour

	private class QuestionAux implements Comparator<QuestionAux>, Comparable<QuestionAux> {
		public double rank;
		public Question question;
		public QuestionAux(Question question, double rank) {
			this.question=question;
			this.rank=rank;
		}
		@Override
	   public int compareTo(QuestionAux d){
		      return (this.rank<d.rank?-1:1);
		   }
		@Override
		public int compare(QuestionAux a, QuestionAux b) {
			return (a.rank<b.rank?-1:1);
		}
		public boolean lowerThan(QuestionAux b) {
			return compare(this,b)<0;
		}
	}

	
	@SuppressWarnings("serial")
	private class CTotal implements Serializable {
		public long total;
		public long lastquery; 
		public String key; // key is contentClassName + domainid
		
		public CTotal(String key, long total, long lastquery) {
			this.total = total;
			this.lastquery=lastquery;
			this.key=key;
		}
	}

	private List<Question> list;
	private long time = System.currentTimeMillis();
	private Map<String, CTotal> mapTotals = new HashMap<String, CTotal>();
	private SessionFactory sessionFactory = null;

	
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public com.novamens.indexer.query.Query getQuestionsUnanswered(Domain domain) {
		String hql = "FROM KbeeQuestion U WHERE U.domain.id= '" + domain.getId().toString() +"' AND U.num_answers=0 order by U.lastModifiedDate desc";
		HibernateQuery hqy = new HibernateQuery();
		// hqy.setSessionFactory(getSessionFactory());
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("filter", "Preguntas sin respuestas.");
		hqy.setParameters(map);
		hqy.setStatement(hql);
		return hqy;
	}
	/**
	 * 
	 * @param contentClassName
	 * @param domain
	 * @param max
	 * @return
	 */
	@Override
	@SuppressWarnings("unchecked")
	public  List<? extends Content> getRecent(String contentClassName, Domain domain, int max) {

		String strclass  = getContentDao().findContentClassByName(contentClassName).getJavaClass(); 
		String hql = "FROM "+ strclass +" U WHERE U.domain.id= '" + domain.getId().toString() +"' order by U.lastModifiedDate desc";
		Query query = sessionFactory.getCurrentSession().createQuery(hql);
		List<?> results = query.list();
		
		if (results==null)
			return null;
		
		if (results.size()<max)
			return (List<? extends Content>) results;
		
		return (List<? extends Content>) results.subList(0, max);
	}
	
	/**
	 * 
	 * @param contentClassName
	 * @param domain
	 * @param value
	 * @return
	 */
	private long add(String contentClassName, Domain domain,  int value) {
		String key = contentClassName+"-"+domain.getId().toString();
		if (mapTotals.containsKey(key)) {
			CTotal ctotal=(CTotal) mapTotals.get(key);
			ctotal.total += value;
			return ctotal.total;
		}
		else {
			mapTotals.put(key, new CTotal(key, value, System.currentTimeMillis()));
			return value;
		}
	}
	
	@Override
	public synchronized long increment(String contentClassName, Domain domain) {
		return add(contentClassName,domain, 1);
	}
	
	@Override
	public  synchronized long decrement(String contentClassName, Domain domain) {
		return add( contentClassName,domain, -1);		
	}

							
	@Override
	public long getTotalUsers(Domain domain) {
		
		long currentTime = System.currentTimeMillis();
		String key = "User"+"-"+domain.getId().toString();
						
		if (mapTotals.containsKey(key) && (currentTime-((CTotal) mapTotals.get(key)).lastquery)<CACHE_DURATION)
			return ((CTotal) mapTotals.get(key)).total;
		
		synchronized (this) {
			time = System.currentTimeMillis();
			String strclass = "KbeeUserProfile";
			
			String hql = "SELECT COUNT(*) FROM "+strclass + " U WHERE U.domain.id= '" + domain.getId().toString() +"'";
			Query query = sessionFactory.getCurrentSession().createQuery(hql);
			
			for(Iterator<?> it=query.iterate();it.hasNext();)
			   	mapTotals.put(key, new CTotal(key,((Long) it.next()).longValue(), currentTime));   
			
			return ((CTotal) mapTotals.get(key)).total;
		}
		
	}
	
	@Override
	@Transactional
	public  long getTotalContents(String contentClassName, Domain domain) {
						
			long currentTime = System.currentTimeMillis();
			String key = contentClassName+"-"+domain.getId().toString();
																
			if (mapTotals.containsKey(key) && (currentTime-((CTotal) mapTotals.get(key)).lastquery)<CACHE_DURATION)
				return ((CTotal) mapTotals.get(key)).total;
			
			synchronized (this) {
				time = System.currentTimeMillis();
				String strclass = getContentDao().findContentClassByName(contentClassName).getJavaClass();
				
				String hql = "SELECT COUNT(*) FROM "+strclass + " U WHERE U.domain.id= '" + domain.getId().toString() +"'";
				Query query = sessionFactory.getCurrentSession().createQuery(hql);
				
				for(Iterator<?> it=query.iterate();it.hasNext();)
				   	mapTotals.put(key, new CTotal(key,((Long) it.next()).longValue(), currentTime));   
				
				return (CTotal) mapTotals.get(key)!=null ?  ((CTotal) mapTotals.get(key)).total : 0;
		}
	}

	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	/**
	 * Featured
	 * 
	 * @param contentClassName
	 * @param domain
	 * @param max
	 * @return
	 */
	
	@Override
	@SuppressWarnings("unchecked")
	public  List<? extends Content> getFeatured(String contentClassName, Domain domain, int max) {
														
		synchronized(this) {
		
			long currentTime = System.currentTimeMillis();
			
			if (list!=null && (currentTime-time)<CACHE_DURATION_HEAVY_PROCESSING)
				return list;
		
			String strclass  = getContentDao().findContentClassByName(contentClassName).getJavaClass();
			
			String hql = "FROM "+ strclass +" U WHERE U.domain.id= '" + domain.getId().toString() +"' order by U.lastModifiedDate desc";
				
			Query query = sessionFactory.getCurrentSession().createQuery(hql);
			List<?> results = query.list();
		
			time = System.currentTimeMillis();
			
			if (results==null)
				return null;
	
			if (results.size()<=max)
				return (List<? extends Content>) results;
		
			int counter=0;
			int XMAX = 100;
			Iterator<?> it = results.iterator();
		 
			TreeSet<QuestionAux> set = new TreeSet<QuestionAux>();
		
			while (counter++<XMAX && it.hasNext()) {
			Question question = (Question) it.next();
			double ranking = (double) question.getService(SocialService.class).getVotes() * 0.8 + (double) question.getNumAnswers() * 0.2;
			QuestionAux qx = new QuestionAux(question, ranking);

			if (set.isEmpty() || set.size()<max)
					set.add(qx);
			else {
				if ((set.first()).lowerThan(qx)) {
					set.pollFirst();
					set.add(qx);
					}
				}
			}
			Stack<Question> st = new Stack<Question>();
			for (QuestionAux q: set) 
					st.push(q.question);

			list = new ArrayList<Question>();
			
			while (!st.isEmpty() && list.size()<max) { 
				list.add(st.pop());
			}
			return list;
		}
	}
}
