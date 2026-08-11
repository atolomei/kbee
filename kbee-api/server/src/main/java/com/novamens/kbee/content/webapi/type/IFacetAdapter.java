package com.novamens.kbee.content.webapi.type;

import java.util.ArrayList;
import java.util.List;

import com.novamens.indexer.query.Facet;
import com.novamens.kbee.content.multidimensional.KbeeFacetWrapper;
import com.novamens.kbee.json.KbeeJson;

import kbee.api.model.IFacet;
import kbee.api.model.IKeyValue;

public class IFacetAdapter implements Adapter<Facet, IFacet> {
	
	public IFacetAdapter() {
	}
	
	public IFacet adapt(Facet facet) {
		
		IFacet ifacet = new IFacet();
		
		ifacet.setId(String.valueOf(facet.getId()));
		ifacet.setDisplayName(facet.getDisplayName());
		ifacet.setName(facet.getName());
		ifacet.setDomain(((KbeeFacetWrapper)facet).getDomain().getName());
		if (((KbeeFacetWrapper)facet).getId()!=null)
			ifacet.setId(String.valueOf(((KbeeFacetWrapper)facet).getId()));
		ifacet.setState(String.valueOf(((KbeeFacetWrapper)facet).getState().name()));
		ifacet.setLastModifiedDate(((KbeeFacetWrapper)facet).getLastModifiedOffsetDateTime());
		
		KbeeJson jsonvisibility = (KbeeJson)((KbeeFacetWrapper)facet).getVisibility();
		if (jsonvisibility!=null) {
			List<IKeyValue> visibility = new ArrayList<IKeyValue>(); 
			for (Object key : jsonvisibility.getData().keySet()) {
				visibility.add(new IKeyValue(key.toString(), ((KbeeFacetWrapper)facet).isVisible(key.toString()) ? "true" : "false"));
			}
			ifacet.setVisibility(visibility);
		}

		return ifacet;	
	}
}