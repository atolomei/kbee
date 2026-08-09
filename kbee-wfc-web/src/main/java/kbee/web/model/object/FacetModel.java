package kbee.web.model.object;

import java.util.List;

import org.apache.wicket.model.IModel;

import com.novamens.content.multidimensional.FacetService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.service.Index;
import com.novamens.service.ServiceLocator;

public class FacetModel implements IModel<Facet> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String facetname;
	private Facet facet;
	public FacetModel(Facet facet) {
		this.facet = facet;
		this.facetname = facet.getName();
	}
	public Facet getObject() {
		if (facet==null) {
			for (Facet facet : getFacets()) {
				if (facet.getName().equals(facetname)) {
					this.facet = facet;
					break;
				}
			}
		}
		return facet;
	}
	
	public void detach() {
		this.facet = null;
	}
	
	private List<Facet> getFacets() {
		return getDomain().getService(FacetService.class).getFacets(getIndex());
	}
	
	private Index getIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	private Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
}