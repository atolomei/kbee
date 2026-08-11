package kbee.importer;

import java.time.OffsetDateTime;
import java.util.List;

import com.novamens.content.base.ContentCreationException;
import com.novamens.content.base.ContentMgmtException;
import com.novamens.content.multidimensional.FacetService;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.multidimensional.KbeeFacetWrapper;
import com.novamens.kbee.json.KbeeJson;

import kbee.api.model.IFacet;
import kbee.api.model.IKeyValue;
import kbee.api.service.ApiService;

@Deprecated
public class FacetsImporter extends Importer {
	
	private int total = 0;
	private int updated = 0;

	public FacetsImporter(ApiService server, Domain domain, LocalMatcher matcher) {
		super(server, matcher);
		setDomain(domain); 
	}
	
	@Override
	public void execute() throws ContentMgmtException  {
		int i=0;
		try {
			for (IFacet remote : getRemoteFacets()) {
				if (remote.getId()!=null) {
					KbeeFacetWrapper local = getLocal(KbeeFacetWrapper.class, remote);
					if (local==null || local.getLastModifiedOffsetDateTime()==null || remote.getLastModifiedDate().isAfter(local.getLastModifiedOffsetDateTime()) || forceUpdate()) {
						if (local == null) {
							local = createFacet(remote);
							if (local!=null) {
								setLocal(remote, local);
							}
						}
						if (local!=null) {
							syncFacet(remote, local);
							update(local);
							updated++;
						}
						logger.info("Facet "+local.getDisplayName());
					}
					else {
						if (local!=null)
						logger.info("Facet "+local.getDisplayName() + " not modified");
					}
				}
				setProgress(++i);
			}
			getContentDao().flush();
		}
		catch (Throwable e) {
			logger.error(e);
			throw new ContentMgmtException(e);
		}
	}
	
	@Override
	public int getTotal() {
		if (total == 0) {
			total = getRemoteFacets().size();
		}
		return total;
	}

	@Override
	public String getResult() {
		String result = "<p>"+String.valueOf(getTotal())+" facets processed. ";
		result += String.valueOf(updated)+" facets updated</p>";
		return result;
	}
	
	private void syncFacet(IFacet remote, KbeeFacetWrapper local) throws ContentMgmtException {
		local.setDisplayName(remote.getDisplayName());
		KbeeJson visibility = new KbeeJson();
		for (IKeyValue keyvalue : remote.getVisibility()) {
			visibility.put(keyvalue.getKey(), keyvalue.getValue());
		}
		local.setDomain(getSessionDomain());
		local.setVisibility(visibility);
		local.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		local.setLastModifiedUser(getUser());
	}
	
	private KbeeFacetWrapper createFacet(IFacet remote) throws ContentCreationException {
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
	
	private List<IFacet> getRemoteFacets() {
		return getServer().getFacets();
	} 
}
