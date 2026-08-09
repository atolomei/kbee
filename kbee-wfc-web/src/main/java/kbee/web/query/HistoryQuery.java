package kbee.web.query;

import java.io.Serializable;

import com.novamens.content.base.Content;
import com.novamens.hibernate.query.HibernateQuery;

public class HistoryQuery extends HibernateQuery {
	private static final long serialVersionUID = 1L;
	Serializable oid; 

	public HistoryQuery(Content content) {
		this.oid = content.getOId();
	}
	
	@Override
	public String getStatement() {
		return "from KbeeContent C where C.oid=" + (oid!=null?oid.toString():"null") + "  order by C.lastModifiedDate desc";
	}
}
 