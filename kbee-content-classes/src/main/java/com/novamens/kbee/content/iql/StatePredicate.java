package com.novamens.kbee.content.iql;

import com.novamens.dom.ObjectState;
import com.novamens.indexer.iql.CalculatedPredicate;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.solr.indexer.iql.SolrAbstractPredicate;

public class StatePredicate extends SolrAbstractPredicate implements CalculatedPredicate {
	
	public StatePredicate() {
		setName("internalstate");
	}
	
	
	public boolean isInformationModel() {
		return false;
	}

	public boolean isCanonical() {
		return true;
	}

	@Override
	public String getHelpValueTypeDescription() {
		return 	ObjectState.ENABLED.name().toLowerCase() 	+ " | " +
				ObjectState.ARCHIVED.name().toLowerCase() + " | " +
				ObjectState.DELETED.name().toLowerCase()  + " | " + 
				ObjectState.DRAFT.name().toLowerCase();
	}

	public String getCode(String argument) {
		ObjectState state = getState(argument);
		return "(state:" + (state==null ? "-" : String.valueOf(state.getId())) + ")";
	}
	
	public boolean evaluate(Object object, Object argument) {
		if (argument==null) return false;
		if (!(object instanceof AbstractObject)) return false;
		ObjectState state = getState(argument.toString());
		return ((AbstractObject)object).getState().equals(state);
	}
		
	protected ObjectState getState(String argument) {
		if (argument==null) return null;
		for (ObjectState state : ObjectState.values()) {
			if (state.name().toLowerCase().equals(argument.toLowerCase()) || 
					String.valueOf(state.getId()).equals(argument)) {
				return state;
			}
		}
		return null;
	}
}
