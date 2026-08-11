package com.novamens.kbee.content.support;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;

import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.security.User;


public class KbeeSupportService implements SupportService, EventListener {

	private SessionFactory sessionFactory;
	
	static private Logger logger = LogManager.getLogger(KbeeSupportService.class.getName());
	
	private Map<String, List<Tip>> tips_general  = new ConcurrentHashMap<String, List<Tip>>(16, 0.9f, 1);
	private Map<String, List<Tip>> tips_security = new ConcurrentHashMap<String, List<Tip>>(16, 0.9f, 1);
	private Map<String, List<Tip>> tips_model    = new ConcurrentHashMap<String, List<Tip>>(16, 0.9f, 1);
	private Map<String, List<Tip>> tips_portal   = new ConcurrentHashMap<String, List<Tip>>(16, 0.9f, 1);
	
	
	
	/** -------------------------------------------------------------------------------------
	 * 
	 * Todos los dias debe salir uno que esta 12 lugares mas adelante que el de ayer.
	 * Los 12 lugares salteados son para que los que hicieron next no vean lo que ya vieron (hasta 12).
	 * 
	 * Para que la sucesión no sea cíclica siempre en los mismo n, n+12*1, n+12*2, ....
	 * cada mes el n cambia, asi el siguiente mes la sucesión será: (n+1), (n+1)+12*1, (n+1)+12*2, .... 
	 *
	 */
	
	@Override
	public Tip getTipOfTheDay(User user, String area) {

		
		if (user==null)
			return null;
		
		String lang = (user.getLocale()!=null ? user.getLocale().getLanguage() :  Locale.getDefault().getLanguage());
		
		Tip tip = null;
		
		synchronized (getMap(area)) {
			
			List<Tip> list = getTips(lang, area);

			if (list.isEmpty())
				return null;

			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(System.currentTimeMillis());
			int base_day_index = cal.get(Calendar.DAY_OF_YEAR) + cal.get(Calendar.MONTH) + cal.get(Calendar.HOUR_OF_DAY);
			int aux = ( Math.abs(base_day_index % list.size()))  * 12;
			int tip_index = Math.abs(aux  % list.size());
			tip = list.get(tip_index);
		}
		
		return tip; 
	}
	
	/** -------------------------------------------------------------------------------------
	 */
	@Override
	public synchronized void evict() {
			
		logger.info("Support Service evict all");
		
		this.tips_general.clear();
		this.tips_security.clear();
		this.tips_model.clear();
		this.tips_portal.clear();
			
	}

	/** -------------------------------------------------------------------------------------
	 */

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/** -------------------------------------------------------------------------------------
	 */
	private synchronized void load(String lang, String area) {

		List<Tip> lang_tips = Collections.synchronizedList(new ArrayList<Tip>());
		String hql = "FROM KbeeTip K where K.lang='" + lang +"' and  K.area='"+area+"' order by lower(K.title)";
		org.hibernate.query.Query<?> query = sessionFactory.getCurrentSession().createQuery(hql);
		query.setCacheable(true);
		query.setCacheRegion("query");

		@SuppressWarnings("unchecked")
		List<Tip> results = (List<Tip>)query.list();

		int n = 0;
		for (Tip tip: results) { 
			tip.setIndex(n++);
			lang_tips.add(tip);
		}
		
		getMap(area).put(lang, lang_tips);
	}

	/** -------------------------------------------------------------------------------------
	 */

	@Override
	public Tip getNext(User user, String area, Tip src_tip) {
		return getNext(user, area, src_tip.getIndex());
	}
	
	/** -------------------------------------------------------------------------------------
	 */

	@Override
	public Tip getRandomNext(User user, String area) {
		return getRandomNext(user, area, Tip.GENERAL);
	}

	
	/** -------------------------------------------------------------------------------------
	 */

	@Override
	public Tip getRandomNext(User user, String area, String default_area) {
		int index = Double.valueOf(Math.random() * 100000).intValue();
		Tip tip= getNext(user, area, index);
		if (tip!=null)
			return tip;
		return getNext(user, default_area, index);
	}
		
	/** -------------------------------------------------------------------------------------
	 */

	@Override
	public Tip getNext(User user, String area, int src_index) {
		
		int index = src_index + 1;
		String lang = user.getLocale().getLanguage();
		
		Tip tip = null;

		if (lang==null)
				lang=Locale.ENGLISH.getLanguage();

		synchronized (getMap(area)) {
			List<Tip> list = getTips(lang, area);
	
			if (list.isEmpty())
				return null;
			
			int tip_index = index % list.size();
			tip = list.get(tip_index);
		}
		
		return tip;
	}

	
	@Override
	public boolean listen(Event event) {
		if (event instanceof EvictCacheServiceEvent)
			return true;
		return false;
	}

	@Override
	public void onEvent(Event event) {
			logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + event.getClass().getName());
			if (event instanceof EvictCacheServiceEvent)
				evict();
	}

    
	
	/** -------------------------------------------------------------------------------------
	 */

	private List<Tip> getTips(String lang, String area) {
		
		if (getMap(area).get(lang)==null)
			load(lang, area);
		
		return getMap(area).get(lang);
	}

	/** -------------------------------------------------------------------------------------
	 */

	private Map<String, List<Tip>> getMap(String area) {
		
		if (area.equals(Tip.GENERAL))
			return tips_general;
		
		if (area.equals(Tip.PORTAL))
			return tips_portal;
		
		if (area.equals(Tip.SECURITY))
			return tips_security;
		

		if (area.equals(Tip.MODEL))
			return tips_model;

		return tips_general;
	}

}
