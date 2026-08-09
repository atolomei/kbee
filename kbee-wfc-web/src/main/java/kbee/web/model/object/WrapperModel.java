package kbee.web.model.object;

import java.util.List;

import org.apache.wicket.model.IModel;

import com.novamens.content.multidimensional.FacetService;
import com.novamens.content.multidimensional.FacetWrapper;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.multidimensional.KbeeFacetWrapper;
import com.novamens.kbee.json.KbeeJson;
import com.novamens.service.ServiceLocator;

public class WrapperModel implements IModel<Facet> {
	private static final long serialVersionUID = 1L;
	
	private String displayName;
	private String facetname;
	private Facet facet;
	private KbeeJson visibility;
	
	public WrapperModel(Facet facet) {
		this.facet = getWrapper(facet);
		this.facetname = facet.getName();
	}
	
	public Facet getObject() {
		if (facet==null) {
			for (Facet facet : getFacets()) {
				if (facet.getName().equals(facetname)) {
					this.facet = getWrapper(facet);
					if (displayName!=null) {
						((KbeeFacetWrapper)this.facet).setDisplayName(displayName);
					}
					if (visibility!=null) {
						((KbeeFacetWrapper)this.facet).setVisibility(visibility);
					}
					break;
				}
			}
		}
		return facet;
	}

	@Override
	public void detach() {
		if (this.facet!=null) {
			this.displayName = this.facet.getDisplayName();
			this.visibility = (KbeeJson)((KbeeFacetWrapper)this.facet).getVisibility();
			this.facet = null;
		}
	}
	
	
	private FacetWrapper getWrapper(Facet facet) {
		if (facet instanceof FacetWrapper) {
			return (FacetWrapper)facet;
		}
		else {
			KbeeFacetWrapper wrapper = new KbeeFacetWrapper(facet);
			wrapper.setDomain(getDomain());
			return wrapper;
		}
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