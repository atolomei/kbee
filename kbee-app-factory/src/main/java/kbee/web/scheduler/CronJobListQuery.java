package kbee.web.scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novamens.dom.Domain;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.FacetOptions;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.QueryBuilder;
import com.novamens.indexer.query.ResultSet;
import com.novamens.scheduler.AbstractCronJobRequest;
import com.novamens.scheduler.SchedulerService;
import com.novamens.service.ServiceLocator;

public class CronJobListQuery implements Query {
			
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static private kbee.util.logging.Logger logger = new kbee.util.logging.Logger(CronJobListQuery.class.getName());
	
	private Map<String, Object> parameters;

	public CronJobListQuery() {
	}
	
	public CronJobListQuery(Domain domain) {
			getParameters().put("domain", domain.getId());
	}

	@Override
	public ResultSet execute() {
		
		List<AbstractCronJobRequest> xlist = new ArrayList<AbstractCronJobRequest>();
		
		try {
			

			
			
			
			
			Object o = getParameters().get("text");
			
			String text = null;
			
			if (o instanceof String) 
				text = (String) getParameters().get("text");
			
			
			boolean is_text = (text!=null);

			
			if (is_text) {
					for (AbstractCronJobRequest c: ServiceLocator.getService(SchedulerService.class).getCronJobs()) {
						if (c.getDisplayName()!=null && c.getDisplayName().toLowerCase().trim().contains(text.toLowerCase().trim())) {
							xlist.add(c);
						}
					}
			}
			else { 	
					xlist.addAll(ServiceLocator.getService(SchedulerService.class).getCronJobs());	
			}
			
			String asc = (String) getParameters().get("ascending");
			if (getParameters().containsKey("sort")) {
				String sort = (String) getParameters().get("sort");
				if (sort.equals("title_sort")) {
					sortName(xlist, asc);
				}
				//else if (sort.equals("next")) {
				//	sortName(xlist, asc);
				//
				//}
			}

				return new CronJobRequestListResultSet(xlist);
				}
				
				catch (Exception e) {
					logger.error(e);
					return new CronJobRequestListResultSet(xlist);
				}
	}

	
	@Override
	public QueryBuilder getBuilder() {
		return null;
	}

	
	@Override
	public Map<String, Object> getParameters() {
		if (parameters==null) 
			parameters = new HashMap<String, Object>();
		return parameters;
	}


	@Override
	public void setParameters(Map<String, Object> parameters) {
		this.parameters=parameters;
	}
	
	@Override
	public void setParameter(String name, Object value) {
	}

	@Override
	public void setOptions(Map<String, FacetOptions> options) {
	}

	@Override
	public String getTitle() {
		return null;
	}
	
	@Override
	public List<Facet> getFacets() {
		return new ArrayList<Facet>();
	}
	
	/**
	private void sortStatus(List<Command> list, final String order) {
		Collections.sort(list, new	 Comparator<Command>() {
			@Override
			public int compare(Command a, Command b) {
				try {
						if (a.getState() == CommandState.RUNNING && b.getState() != CommandState.RUNNING)
							return (order.equals("true"))?-1:1;
						
						if (b.getState() == CommandState.RUNNING && a.getState() != CommandState.RUNNING)
							return (order.equals("true"))?1:-1;
		
						if (a.getState() == b.getState()) {
						
							if (order.equals("true"))
								return b.getDateStarted().compareTo(a.getDateStarted());
							else
								return a.getDateStarted().compareTo(b.getDateStarted());
						}
		
						if (order.equals("true"))
							return a.getState().getLabel().compareToIgnoreCase(b.getState().getLabel());
						else
							return b.getState().getLabel().compareToIgnoreCase(a.getState().getLabel());
						
				} catch (RuntimeException e) {

					return 0;
				}
			}
		});
		
	}
	

	private void sortStart(List<Command> list, final String order) {
		
		
		Collections.sort(list, new	 Comparator<Command>() {
			@Override
			public int compare(Command a, Command b) {
				try {
						if (order.equals("true"))
								return a.getDateStarted().compareTo(b.getDateStarted());
						else
							return b.getDateStarted().compareTo(a.getDateStarted());
								
						
				} catch (RuntimeException e) {
					return 0;
				}
			}
		});
		
	}
	
	*/							
	private void sortName(List<AbstractCronJobRequest> list, final String order) {
		
			Collections.sort(list, new	 Comparator<AbstractCronJobRequest>() {
				@Override
				public int compare(AbstractCronJobRequest a, AbstractCronJobRequest b) {
					try {
							if (a.getName()==null && b.getName()==null)
								return 0;
					
							else if (a.getName()==null && b.getName()!=null)
								return (order.equals("true"))?-1:1;
							
							else if (a.getName()!=null && b.getName()==null)
								return (order.equals("true"))?1:-1;
							
							if (order.equals("true"))
								return a.getName().compareToIgnoreCase(b.getName());
							else
								return b.getName().compareToIgnoreCase(a.getName());
							
					} catch (RuntimeException e) {
						return 0;
					}
				}
			});
			
	}
	
}
