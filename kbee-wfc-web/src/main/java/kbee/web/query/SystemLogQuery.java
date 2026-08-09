package kbee.web.query;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.hibernate.query.HibernateQuery;
import com.novamens.service.ServiceLocator;

public class SystemLogQuery extends HibernateQuery {
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger( SystemLogQuery.class.getName());

	private static final long serialVersionUID = 1L;

	public SystemLogQuery() {
		getParameters().put("fromdate", OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS));
		getParameters().put("todate", OffsetDateTime.now().plusDays(1).truncatedTo(ChronoUnit.DAYS));

	}
	
	@Override
	public String getStatement() {
		
		Domain domain = ServiceLocator.getService(UserService.class).getDomain();
		
		StringBuilder statement = new StringBuilder();
		
		
		statement.append("from AbstractLogEvent R where R.domainId= " + domain.getId().toString());
		
		StringBuilder sizestatement = new StringBuilder();
		sizestatement.append("select count (*) FROM AbstractLogEvent R WHERE R.domainId= " + domain.getId().toString());

		String typefilter = getTypeFilter();
		if (typefilter!=null ) {
			statement.append(" and "+typefilter);
			sizestatement.append(" and "+ typefilter);
		}
		
		String timefilter = getTimeFilter();
		if (timefilter!=null) {
			statement.append(" and "+timefilter);
			sizestatement.append(" and "+ timefilter);
		}
		
		String userfilter = getUserFilter();
		if (userfilter!=null) {
			statement.append(" and "+userfilter);
			sizestatement.append(" and "+ userfilter);
		}
		
		logger.debug(sizestatement.toString());
		setSizeQuery(sizestatement.toString());
		
		String str_order = ((getParameters().get("ascending")!=null && getParameters().get("ascending").equals("true")) ? "":" desc");
		
		statement.append(" order by R.time " + str_order);
		setStatement(statement.toString());
		logger.debug(statement.toString());
		
		return statement.toString();
		
	}
	
	private String getTimeFilter() {
		
		String from_date_q ="";
		String to_date_q  ="";
		String date_q  ="";

		if (getParameters().get("fromdate")!=null && (!getParameters().get("fromdate").equals("null"))) {
			OffsetDateTime from_date = (OffsetDateTime) getParameters().get("fromdate");
			from_date_q = " R.time >='" + DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(from_date)+"'";
		}
		
		if (getParameters().get("todate")!=null && (!getParameters().get("todate").equals("null"))) {
			OffsetDateTime to_date = (OffsetDateTime) getParameters().get("todate");
			to_date = to_date.truncatedTo(ChronoUnit.DAYS).plusDays(1);
			to_date_q = (from_date_q.length()>0?" and " : "") + " R.time <='" + DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(to_date)+"'";
		}
		
		date_q  =  from_date_q +  to_date_q;
		if (date_q.length()>0)
			date_q="("+date_q+")";
		
		return date_q;
		
		
	}
	
	private String getUserFilter() {
		String filter = null;
		if (getParameters().get("username")!=null && (!getParameters().get("username").equals("null"))) {
			filter = "(upper(user.lastname) like '"+getParameters().get("username").toString().toUpperCase()+"%' or ";
			filter += "upper(user.firstname) like '"+getParameters().get("username").toString().toUpperCase()+"%')";
		}
		return filter;
	}
	
	private String getTypeFilter() {
		String filter = null;
		if (getParameters().get("type")!=null && (!getParameters().get("type").equals("null"))) {
			String type = (String)getParameters().get("type");
			if (!"All".equals(type)) {
				if (type.equals("Security")) {
					filter = "(R.class='LoginEvent' or R.class='LogoutEvent' or R.class='SecurityUpdateEvent' or R.class='SecurityDeleteEvent' or R.class='UserUpdateEvent')";
				}
				if (type.equals("Content")) {
					filter = "(R.class='UpdateEvent' or R.class='CheckinEvent' or R.class='RemoveEvent' or R.class='CheckoutEvent' or R.class='ReadEvent')";
				}
				if (type.equals("Workflow")) {
					filter = "(R.class='TaskStartEvent' or R.class='TaskPendingEvent' or R.class='TaskEndEvent')";
				}
			}
		}
		return filter;
	}	
}
