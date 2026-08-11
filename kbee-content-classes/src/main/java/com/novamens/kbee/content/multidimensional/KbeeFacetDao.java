package com.novamens.kbee.content.multidimensional;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.springframework.util.Assert;

import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.ModelElement;

import com.novamens.content.multidimensional.FacetDao;
import com.novamens.content.multidimensional.FacetWrapper;
import com.novamens.dom.Domain;
import com.novamens.event.AppDeleteEvent;
import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

public class KbeeFacetDao implements FacetDao, EventListener {
																									
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeFacetDao.class.getName());

	private SessionFactory sessionFactory;
	private Map<Serializable, List<KbeeFacetWrapper>> wrappersmap = Collections.synchronizedMap(new HashMap<Serializable, List<KbeeFacetWrapper>>());
	
	public List<Facet> getFacets(Index index, Domain domain) {
		
		if (index.getCube()==null ||  index.getCube().getFacets()==null)
			return  new ArrayList<Facet>();
		
		return getWrappers(index.getCube().getFacets(), domain);
	}
	
	public List<Facet> getFacets(Query query, Domain domain) {
		return getWrappers(query.getFacets(), domain);
	}
	
	public boolean listen(Event event) {
		if (event instanceof EvictCacheServiceEvent)
			return true;
		
		return (event.getObject() instanceof Classifier 	|| 
				event.getObject() instanceof Attribute 		|| 
				event.getObject() instanceof DataSet 		|| 
				event.getObject() instanceof FacetWrapper); 
	}
	
	public void onEvent(Event event) {
		
		if (event instanceof EvictCacheServiceEvent) {
			wrappersmap.clear();
			return;
		}
		
		if (getSessionUser()!=null) {
			wrappersmap.remove(((KbeeUser)getSessionUser()).getDomain().getId());
		}
		else {
			wrappersmap.clear();
		}
		if (event instanceof AppDeleteEvent && event.getObject() instanceof ModelElement) {
			onDelete((ModelElement)event.getObject());
		}
	}
	
	public Facet getWrapper(Facet facet, Domain domain) {
		for (Facet wrapper : getWrappers(domain)) {
			if (wrapper.getName().equals(facet.getName())) {
				((KbeeFacetWrapper)wrapper).setFacet(facet);
				return wrapper;
			}
		}
		return null;
	}
	
	public void save(Facet facet) {
		Assert.isInstanceOf(KbeeFacetWrapper.class, facet, "no wrapper");
		if (((KbeeFacetWrapper)facet).getCreationOffsetDateTime()==null)
			((KbeeFacetWrapper)facet).setCreationOffsetDateTime(OffsetDateTime.now());
		
		((KbeeFacetWrapper)facet).setLastModifiedOffsetDateTime(OffsetDateTime.now());
		((KbeeFacetWrapper)facet).setLastModifiedUser(getSessionUser());
		
		getSessionFactory().getCurrentSession().save(facet);
	}
	
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}
	
	private List<Facet> getWrappers(Collection<Facet> facets, Domain domain) {
		List<Facet> wrappers = new ArrayList<Facet>();
		for (Facet facet : facets) {
			Facet wrapper = getWrapper(facet, domain);
			if (wrapper == null) {
				wrapper = new KbeeFacetWrapper(facet);
				((KbeeFacetWrapper)wrapper).setDomain(domain);
				wrappers.add(wrapper);
			}
			else {
				wrappers.add(wrapper);
			}
		}
		//Locale locale = getSessionUser()!=null ? getSessionUser().getLocale() : Locale.getDefault();
		Collections.sort(wrappers, new Comparator<Facet>() {
			@Override
			public int compare(Facet a, Facet b) {
				try {
					String aname = a.getDisplayName().toLowerCase();
					String bname = b.getDisplayName().toLowerCase();
					return aname.compareTo(bname);
				} 
				catch (Exception e) {
					logger.error(e);
					return 0;
				}
			}
		});
		return wrappers;
	}
	
	@SuppressWarnings({"unchecked"})
	private List<KbeeFacetWrapper> getWrappers(Domain domain) {
		
		if (domain == null) 
			return new ArrayList<KbeeFacetWrapper>();
		
		List<KbeeFacetWrapper> wrappers = null;
		if (wrappers == null) {
			synchronized (this) {
				String hql = "FROM KbeeFacetWrapper W WHERE W.domain.id=" + domain.getId().toString();
				org.hibernate.query.Query<?> query = getSessionFactory().getCurrentSession().createQuery(hql);
				query.setCacheable(true);
				query.setCacheRegion("query");
				wrappers = (List<KbeeFacetWrapper>)query.list();
				if (wrappers.isEmpty())
					return new ArrayList<KbeeFacetWrapper>();
			}
		}
		return wrappers;
	}
	
	@SuppressWarnings("deprecation")
	private synchronized void onDelete(ModelElement element) {
		Domain domain = element.getDomain();
		Index index = domain.getService(JavaIndexerService.class).getIndex();
		for (KbeeFacetWrapper wrapper : getWrappers(domain)) {
			boolean facetfound = false, done = false;
			for (Facet facet : index.getCube().getFacets()) {
				if (facet.getName().equals(wrapper.getName())) {
					facetfound = true;
				}
				if (facetfound) {
					if (facet instanceof ClassifierHierarchicalFacet) {
						if (((ClassifierHierarchicalFacet)facet).getClassifier().getId().equals(element.getId())) {
							delete(wrapper);
							done = true;
						}
					}
					if (facet instanceof DateFacet && element instanceof Attribute) {
						if (facet.getName().equals(((Attribute)element).getUniqueName()+"member")) {
							delete(wrapper);
							done = true;
						}
					}
					break;
				}
			}
			if (!facetfound) {
				delete(wrapper);
				break;
			}
			if (done) {
				break;
			}
		}
	}
	
	private void delete(KbeeFacetWrapper wrapper) {
		getSessionFactory().getCurrentSession().delete(wrapper);
	}
	
	private User getSessionUser() {
		try {
			return ServiceLocator.getService(SecurityService.class).getSessionUser();
		} 
		catch (Exception e) {
			return null;
		}
	}
}