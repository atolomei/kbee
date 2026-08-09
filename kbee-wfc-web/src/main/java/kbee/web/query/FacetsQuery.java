package kbee.web.query;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.model.IModel;

import com.novamens.content.multidimensional.FacetService;
import com.novamens.content.user.UserService;

import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.FacetOptions;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.QueryBuilder;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.multidimensional.KbeeFacetWrapper;
import com.novamens.security.Identifiable;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.model.object.FacetModel;

public class FacetsQuery implements Query {
	private static final long serialVersionUID = 1L;
	
	HashMap<String, Object> parameters = new HashMap<String, Object>();
	
	public FacetsQuery() {
	}
	
	public QueryBuilder getBuilder() {
		return null;
	}
	
	public ResultSet execute() {
		
		List<Facet> facets = getDomain().getService(FacetService.class).getFacets(getIndex());
		
		String text = (String) getParameters().get("text");
		boolean is_text = (text!=null);
		
		
		List<IModel<Facet>> models = new ArrayList<IModel<Facet>>();

		for (Facet facet : facets) {
			if (!is_text || (facet.getDisplayName()!=null && facet.getDisplayName().toLowerCase().trim().contains(text.toLowerCase().trim()))) {
				if (facet instanceof Identifiable && ((Identifiable)facet).getId()!=null)  {
					models.add(new ObjectModel<Facet>(facet));
				}
				else {
					models.add(new FacetModel(facet));
				}
			}
		}
		
		String sort = (String)getParameters().get("sort");
		boolean ascending = "true".equals(getParameters().get("ascending"));
		
		if ("modified".equals(sort)) {
			Collections.sort(models, new Comparator<IModel<Facet>>() {
				@Override
				public int compare(IModel<Facet> a, IModel<Facet> b) {
					try {
						OffsetDateTime ta = a.getObject() instanceof KbeeFacetWrapper ? ((KbeeFacetWrapper)a.getObject()).getLastModifiedOffsetDateTime() : null;
						OffsetDateTime tb = b.getObject() instanceof KbeeFacetWrapper ? ((KbeeFacetWrapper)b.getObject()).getLastModifiedOffsetDateTime() : null;
						if (ascending) {
							return ta!=null && tb!=null && ta.isAfter(tb) ? -1 : 1; 
						}
						else {
							return ta!=null && tb!=null && ta.isAfter(tb) ? 1 : -1; 
						}
					} 
					catch (Exception e) {
						return 0;
					}
				}
			});
		}
		
		else if ("title_sort".equals(sort)) {
			Collections.sort(models, new Comparator<IModel<Facet>>() {
				@Override
				public int compare(IModel<Facet> a, IModel<Facet> b) {
					try {
						if (ascending) {
							return a.getObject().getDisplayName().compareToIgnoreCase(b.getObject().getDisplayName())>0 ? 1 : -1; 
						}
						else {
							return a.getObject().getDisplayName().compareToIgnoreCase(b.getObject().getDisplayName())>0 ? -1 : 1; 
						}
					} 
					catch (Exception e) {
						return 0;
					}
				}
			});
		}
		
		ListModelResultSet<Facet> resulSet = new ListModelResultSet<Facet>(models);
		return resulSet;
	}
	
	public Map<String, Object> getParameters() {
		return parameters;
	}
	
	public void setParameters(Map<String, Object> parameters) {
		
	}
	
	@Override
	public void setParameter(String name, Object value) {
		
	}
	
	public void setOptions(Map<String, FacetOptions> options) {
		
	}
	
	public String getTitle() {
		return null;
	}
	
	public List<Facet> getFacets() {
		return null;
	}
	
	private Index getIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	private Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}