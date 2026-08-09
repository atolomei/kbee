package kbee.web.query;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.FacetOptions;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.QueryBuilder;
import com.novamens.indexer.query.ResultSet;
import com.novamens.kbee.wicket.markup.html.console.list.ListPanel;
import com.novamens.security.Auditable;
import com.novamens.security.Identifiable;


public class ListModelQuery<T> implements Query, IDetachable {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ListModelQuery.class.getName());

	private Map<String, Object> parameters;
	private List<IModel<T>> list;

	private String sort_criteria = null;
	private String sort_order = null;
	
	public ListModelQuery(List<IModel<T>> list) {
		this.list=list;
		sort();
	}
	
	@Override
	public void detach() {
			if (list!=null) 
				list.forEach( item -> item.detach());
	}
	
	@Override
	public QueryBuilder getBuilder() {
		return null;
	}

	
	public List<IModel<T>> getListModel() {
		return getList();
	}
	
	@Override
	public ResultSet execute() {
		sort();
		ListModelResultSet<T> resultset=new ListModelResultSet<T>(getList());
		return resultset;
	}

	@Override
	public Map<String, Object> getParameters() {
		if (this.parameters==null) 
			this.parameters = new HashMap<String, Object>();
		return this.parameters;
	}

	@Override
	public void setParameters(Map<String, Object> parameters) {
	}
	
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
		return null;
	}

	protected List<IModel<T>> getList() {
		return list;
	}
	
	private void sort() {
		
		if (this.list!=null) {

				String sort = null; 
				String sort_order = null;
		
				if (getParameters().containsKey("sort")) 
					sort = (String) getParameters().get("sort");
				
				if (getParameters().containsKey("ascending")) 
					sort_order = (String) getParameters().get("ascending");

				if (sort==null)
					sort="title";
				
				if (sort_order==null)
					sort_order="true";
				
				if (this.sort_criteria==null || this.sort_order==null) {
					if (sort.equals("title"))
						sort_title(sort_order);
					else
						sort_modified(sort_order);
					return;
				}
				
				if (!(sort.equals(this.sort_criteria) && sort_order.equals(this.sort_order))) {
					if (sort.equals("title"))
						sort_title(sort_order);
					else
						sort_modified(sort_order);
				}
			
		}		
	}
		
	private void sort_title(final String sort_order) {
			
		this.sort_criteria = "title";
		this.sort_order=sort_order;
			
			Collections.sort(this.list, new Comparator<IModel<T>>() {
				@Override
				public int compare(IModel<T> o1, IModel<T> o2) {
					 try {
						 
						 T dom1;
						 T dom2;
						 
						 
						 if (sort_order.equals("true")) {
								dom1 =   o1.getObject();
								dom2 =   o2.getObject();
							}
							else {
								dom2 =   o1.getObject();
								dom1 =   o2.getObject();
							}

						 
						 
						 if ( (o1 instanceof Identifiable) && (o2 instanceof Identifiable)) {
								 if ( ((Identifiable) dom1).getDisplayName()==null)
									return ((Identifiable)dom2).getDisplayName()==null?0:1;
								
								if ( ((Identifiable) dom2).getDisplayName()==null)
									return -1;
								return ((Identifiable) dom1).getDisplayName().compareToIgnoreCase( ((Identifiable) dom2).getDisplayName());
						 }
						 else 
							 logger.error(" must be subclass of Identifiable -> " + dom1.getClass().getName());
						 
						 return 0;
							
					 } catch (Exception e) {
							return 0;
						}
				}
			});

	}

	private void sort_modified(final String sort_order) {
		
		this.sort_criteria = "modified";
		this.sort_order=sort_order;
		
		Collections.sort(this.list, new Comparator<IModel<T>>() {
			@Override
			public int compare(IModel<T> o1, IModel<T> o2) {
				 
				 T dom1;
				 T dom2;
				 
				if (sort_order.equals("true")) {
					dom1 =   o1.getObject();
					dom2 =   o2.getObject();
				}
				else {
					dom2 =   o1.getObject();
					dom1 =   o2.getObject();
				}
				 
				if (dom1 instanceof Auditable && dom2 instanceof Auditable) { 
					 if (((Auditable) dom1).getLastModifiedOffsetDateTime()==null)
						return  ((Auditable)dom2).getLastModifiedOffsetDateTime()==null?0:1;
					if ( ((Auditable)dom2).getLastModifiedOffsetDateTime()==null)
						return -1;
					return ((Auditable)dom1).getLastModifiedOffsetDateTime().compareTo(((Auditable)dom2).getLastModifiedOffsetDateTime());
				}
				
				logger.error(" must be subclass of Auditable -> " + dom1.getClass().getName());
				return 0;
			}
		});

}

}
