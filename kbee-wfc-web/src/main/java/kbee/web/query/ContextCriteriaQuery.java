package kbee.web.query;

import java.util.List;
import java.util.Map;

import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.text.template.VariableResolver;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.FacetOptions;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.QueryBuilder;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.text.template.KbeeContentTextTemplate;
import com.novamens.kbee.content.util.ContentVariableResolver;
import com.novamens.solr.indexer.query.SolrParametersQuery;

@SuppressWarnings("serial")
public class ContextCriteriaQuery implements Query, IDetachable {
	
	private static final long serialVersionUID = 1L;
	
	private IModel<Content> model;
	private String criteria;
	
	public ContextCriteriaQuery(IModel<Content> model, String criteria) {
		setCriteria(criteria);
		setModel(model);
	}
	
	public IModel<Content> getModel() {
		return model;
	}
	
	public void setModel(IModel<Content> model) {
		this.model = model;
	}
	
	public String getCriteria() {
		return criteria;
	}
	
	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}
	
	public QueryBuilder getBuilder() {
		return null;
	}
	
	public ResultSet execute() {
		return getQuery().execute();
	}
	
	public Map<String, Object> getParameters() {
		return null;
	}
	
	public void setParameters(Map<String, Object> parameters) {
	}
	
	public void setParameter(String name, Object value) {
	}
	
	public void setOptions(Map<String, FacetOptions> options) {
	}
	
	public String getTitle() {
		return null;
	}
	
	public List<Facet> getFacets() {
		return null;
	}
	
	public void detach() {
		if (model!=null)
			model.detach();
	}
	
//	@Override
//	public String toString() {
//		StringBuilder str = new StringBuilder();
//		if (getModel().getObject()!=null)
//			str.append(getModel().getObject().getOId().toString() + "/" + getModel().getObject().getId().toString() + ". " + getModel().getObject().getTitle() + " | ");
//		str.append(getQuery().toString());
//		getModel().detach();
//		return str.toString();
//	}

	
	
	/**
	 * the quey must be a SolrParametersQuery
	 * which can combine parameters with iql sentences.
	 * @return
	 */
	public Query getQuery() {
	
		KbeeContentTextTemplate template = new KbeeContentTextTemplate(getCriteria());
		
		VariableResolver resolver = new ContentVariableResolver<Content>(getModel(), null) {
			@Override
			public String getDefaultValue() {
				return "null";
			}
		};
		
		String statement = template.getText(resolver);
		SolrParametersQuery query = new SolrParametersQuery(getIndex()) {
			public IqlService getIqlService() {
				return getDomain().getService(IqlService.class);
			}
		};
		query.getParameters().put("domain", String.valueOf(getDomain().getId()));
		query.getParameters().put("sort", "relevance");
		query.getParameters().put("ascending", "false");
		query.getParameters().put("state", String.valueOf(ObjectState.ENABLED.getId()));
		query.getParameters().put("head", "true");
		query.getParameters().put("iql", statement);
		return query;
	}
	
	protected Index getIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
	
	protected Domain getDomain() {
		return model.getObject().getDomain();
	}
}
