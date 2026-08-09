package kbee.web.service;


import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.content.reportsubscription.ReportExportSchedule;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.web.report.ReportFactory;

/**
 * 
 */
public class KbeeReportsLibraryService implements ReportsLibraryService, EventListener {
													
	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeReportsLibraryService.class.getName());
	
	private List<ReportFactory> factories = null;;
	
	private Map<Domain, List<ReportFactory>> domainfactories = Collections.synchronizedMap(new HashMap<Domain, List<ReportFactory>>());
	private Map<Serializable, List<ReportFactory>> user_reports = Collections.synchronizedMap(new HashMap<Serializable, List<ReportFactory>>());
												
	
		
	@Override
	public boolean hasReports(Domain domain) {
	
		
		
		return getReports(domain).size()>0; 
	}
	
	
	

	@Override
	public List<ReportFactory> getUserSessionReports() {

		if (user_reports.containsKey(getSessionUser().getId()))
			return user_reports.get((getSessionUser().getId()));

		synchronized (this) {
			List<ReportFactory> user_factories = new ArrayList<ReportFactory>();
			final List<ReportFactory> factories = getReports(getDomain());
			for (ReportFactory factory : factories) {
				try {
					if(factory.getReport().isReadable())
						user_factories.add(factory);
				} 
				catch (Exception e) {
					logger.error(e, "Probably incorrect Spring XML config.");
				}
			}
			user_reports.put(getSessionUser().getId(), user_factories);
			/*
			if (logger.isDebugEnabled()) {
				user_factories.forEach(item -> logger.debug(item.getDisplayName()));
			}*/
		}
		return user_reports.get((getSessionUser().getId()));
	}

	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

		
	@Override
	public List<ReportFactory> getReports(Domain domain) {
		
		if (factories == null) {
			BeansService bs = ServiceLocator.getService(BeansService.class);
			factories = new ArrayList<ReportFactory>();
			Map<String, ReportFactory> beans = bs.getBeansOfType(ReportFactory.class);
			for (String bean : beans.keySet()) {
				ReportFactory reportFactory = (ReportFactory) bs.getBean(bean);
					factories.add(reportFactory);
			}
		}

		List<ReportFactory> domainfactories = this.domainfactories.get(domain);
		
		if (domainfactories == null) {
			domainfactories = new ArrayList<ReportFactory>();
			for (ReportFactory factory : factories) {
				try {
					if ((factory.getReport().getDomain()==null || factory.getReport().getDomain().equals(domain))) 
						domainfactories.add(factory);
				} 
				catch (Exception e) {
					logger.error(e, "Probably incorrect Spring XML config.");
				}
			}
			this.domainfactories.put(domain, domainfactories);
		}
		return domainfactories;
	}


	@Override
	public List<ReportExportSchedule> getUserDomainReportExportSchedules() {
		Collection<ReportExportSchedule> schedules = ServiceLocator.getService(BeansService.class).getBeansOfType(ReportExportSchedule.class).values();
		String currentDomainName = getDomain().getName();
		return schedules.stream().filter(s -> s.getDomainNames() == null || s.getDomainNames().contains(currentDomainName)).collect(Collectors.toList());
	}
	
	
	public void evict() {
		this.user_reports.clear();
		this.domainfactories.clear();
		this.factories=null;
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}

	@Override
	public boolean listen(Event event) {
		if (event instanceof EvictCacheServiceEvent)
			return true;
		return false;
	}

	@Override
	public void onEvent(Event event) {
			if (event instanceof EvictCacheServiceEvent)
				evict();
	}
	protected User getSessionUser() {
		try {
			return ServiceLocator.getService(SecurityService.class).getSessionUser();
		} catch (Exception e) {
				logger.error(e);
			return null;
		}
	}

}
