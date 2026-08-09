package kbee.web.query;

import com.novamens.dom.ObjectState;
import com.novamens.indexer.service.Index;
import com.novamens.security.User;

public class RecycleBinQuery2 extends ContentQuery {

	private static final long serialVersionUID = 1L;

	public RecycleBinQuery2(Index index, User user) {
		super(index);
		getParameters().put("state", String.valueOf(ObjectState.DELETED.getId()));
	}


}
