package kbee.web.model;

import com.novamens.hibernate.query.HibernateQuery;
import com.novamens.workflow.Procedure;

public class ProcessesQuery extends HibernateQuery {
	private static final long serialVersionUID = 1L;
	
	private long procedureid = 0;
	
	public ProcessesQuery(Procedure procedure) {
		try {
			procedureid = Long.valueOf((Long)procedure.getId());
		}
		catch (Exception e) {
		}
	}
	
	@Override
	public String getStatement() {
		
		String statement = "select count(*) from KbeeProcess P where P.procedure2.id= "+String.valueOf(procedureid);
		
		setStatement(statement);

		return statement;
	}
 
}
