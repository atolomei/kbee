package kbee.web.portal6;


import org.hibernate.SessionFactory;

import com.novamens.beans.BeansService;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.hibernate.query.HibernateQuery;
import com.novamens.indexer.query.Filter;
import com.novamens.service.ServiceLocator;

public class SitesHibernateQuery extends HibernateQuery {

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SitesHibernateQuery.class.getName());

	private static final long serialVersionUID = 1L;

	public SitesHibernateQuery() {
		getParameters().put("status", String.valueOf(ObjectState.ENABLED.getId()));
	}

	public String getDomainStr() {
		return "R.domain.id=" + String.valueOf(getDomain().getId());
	}

	@Override
	public String getStatement() {

		StringBuilder statement = new StringBuilder();
		statement.append("from KbeeSite R " + getWhere());
		statement.append(getOrderBy());

		setStatement(statement.toString());
		String sizeQuery = "select count (*) FROM KbeeSite R " + getWhere();
		setSizeQuery(sizeQuery);

		return statement.toString();
	}

	@Override
	public SessionFactory getSessionFactory() {
		return (SessionFactory) ServiceLocator.getService(BeansService.class).getBean("sessionFactory");
	}

	
	protected String getStates() {
		String status = (String) getParameters().get("status");
		if (status != null)
			return " R.state=" + status + " ";
		else
			return " R.state!=" + String.valueOf(ObjectState.DELETED.getId()) + " ";
	}

	/**
	 * @return
	 */
	protected String getWhere() {

		StringBuilder str = new StringBuilder();

		Object textparameter = getParameters().get("text");
		String text = textparameter != null && textparameter instanceof Filter
				? (String) ((Filter) textparameter).getValue()
				: (textparameter != null ? (String) textparameter : null);
		String txt = null;

		if (text != null) {
			if (text.startsWith("'") && text.endsWith("'")) {
				txt = " WHERE ( lower(R.name) like " + text.toLowerCase() + " OR lower(R.title) like "
						+ text.toLowerCase() + " OR lower(R.subtitle) like " + text.toLowerCase();
			} else if (text.startsWith("where:") && text.length() > 6) {
				txt = " WHERE (" + text.substring(6);
			} else {
				txt = " WHERE ((lower(R.name)    like '%" + text.toLowerCase().replace(" ", "%")
						+ "%' OR lower(R.title)    like '%" + text.toLowerCase().replace(" ", "%")
						+ "%' OR lower(R.subtitle) like '%" + text.toLowerCase().replace(" ", "%") + "%') ";
			}
			str.append(txt);
			str.append(" AND ");
		} else
			str.append(" WHERE (");

		str.append(getStates());
		str.append(" AND " + getDomainStr());
		str.append(" )");

		return str.toString();
	}

	/**
	 * @return
	 */
	protected String getOrderBy() {

		StringBuilder str = new StringBuilder();

		String str_order = ((getParameters().get("ascending") != null
				&& getParameters().get("ascending").equals("true")) ? "" : " desc");

		if ("title".equals(getParameters().get("sort"))) {			str.append(" order by lower(R.title) " + str_order);
		} else if ("url".equals(getParameters().get("sort"))) {		str.append(" order by lower(R.url)" + str_order);
		} else if ("name".equals(getParameters().get("sort"))) {	str.append(" order by lower(R.name)" + str_order);
		} else if ("status".equals(getParameters().get("sort"))) {	str.append(" order by R.state" + str_order);
		} else if ("type".equals(getParameters().get("sort"))) {    str.append(" order by R.type" + str_order);
		} else if ("modified".equals(getParameters().get("sort"))) {
			str.append(" order by R.lastModifiedDate " + str_order);
		}

		return str.toString();
	}

	/**
	 * 
	 */
	private Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}

}
