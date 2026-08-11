package kbee.replica;

import java.time.OffsetDateTime;
import java.util.List;

import com.novamens.content.multidimensional.FacetService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.multidimensional.KbeeFacetWrapper;
import com.novamens.kbee.json.KbeeJson;

import kbee.api.model.IFacet;
import kbee.api.model.IKeyValue;

public class FacetReplicaHandler extends AbstractReplicaHandler<IFacet, KbeeFacetWrapper> {

	public FacetReplicaHandler(Replica replica, IFacet ifacet) {
		super(replica, ifacet);
	}
	
	@Override
	protected void replicateIn(KbeeFacetWrapper local) {
		IFacet remote = getObject();
		local.setDisplayName(remote.getDisplayName());
		KbeeJson visibility = new KbeeJson();
		for (IKeyValue keyvalue : remote.getVisibility()) {
			visibility.put(keyvalue.getKey(), keyvalue.getValue());
		}
		local.setDomain(getSessionDomain());
		local.setVisibility(visibility);
		local.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		local.setLastModifiedUser(getSessionUser());
	}
	
	@Override
	protected KbeeFacetWrapper createLocal() {
		IFacet remote = getObject();
		KbeeFacetWrapper wrapper = null;
		Facet local = null;
		for (Facet facet : getFacets()) {
			if (remote.getName().equals(facet.getName())) {
				local = facet;
				break;
			}
		}
		if (local!=null) {
			wrapper = new KbeeFacetWrapper(local);
			wrapper.setDomain(getSessionDomain());
			update(wrapper);
		}
		return wrapper;
	}
	
	private List<Facet> getFacets() {
		return getSessionDomain().getService(FacetService.class).getFacets(getIndex());
	}
	
	private Index getIndex() {
		return getSessionDomain().getService(JavaIndexerService.class).getIndex();
	}
}