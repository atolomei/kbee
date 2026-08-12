package com.novamens.solr.indexer.iql;

import java.time.OffsetDateTime;
import java.util.ArrayList;
 
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import com.novamens.content.dao.MemberDao;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.text.Text;
import com.novamens.dom.Domain;
import com.novamens.indexer.iql.IqlService;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.query.TextQuery;
import com.novamens.indexer.service.IndexerException;
import com.novamens.indexer.service.JavaIndex;
import com.novamens.repository.DomRepository;
import com.novamens.repository.DomRepositoryService;
import com.novamens.security.acl.Acl;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;

@SuppressWarnings("serial")
public class SolrMemberDao implements MemberDao {
														
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SolrMemberDao.class.getName());
	
	private Domain domain;
	private JavaIndex index;
	private String dataSetId;
	private String memberType = "datasetmember";
	
	public class SolrDataSetMember extends SolrProxy implements DataSetMember {
		public SolrDataSetMember(String id) {
			super(id);
		}
		public DataSet getDataSet() {
			return null;
		}
		public void setDataSet(DataSet dataset) {
		}		
		public List<DataSetMember> getParents() {
			return null;
		}	
		public int	getLevel() {
			return 0;
		}	
		public Acl getACL() {
			return null;
		}	
		public Object getValue() {
			return null;
		}	
		public String getStrValue() {
			return null;
		}
		public OffsetDateTime getDateValue() {
			return null;
		}	
		public void setStrValue(String value) {
		}	
		public void setDateValue(OffsetDateTime value) {
		}
		public String getDisplayName() {
			return getStrValue();
		}
		public String getAlternativeDisplayName() {
			return null;
		}
		public void setAlternativeDisplayName(String dname) {
		}	
		public void setAttributeValue(String name, String value) {
		}
		public void setAttributeValues(Attribute attribute, List<String> values) {
		}
		public List<String> getAttributeValues(Attribute attribute) {
			return null;
		}
		public List<Classification> getClassification() {
			return null;
		}
		public List<Classification> getClassification(Classifier classifier) {
			return null;
		}
		public void setClassification(Classifier classifier, List<DataSetMember> members) {
		}
		public void setClassification(Classifier classifier, DataSetMember member) {
		}
		public Text getNotes() {
			return null;
		}
		public Map<String, List<String>> getAttributesAsMap() {
			return null;
		}
		@Override
		public void setNotes(String notes) {
			// TODO Auto-generated method stub
		}
		@Override
		public String getExternalId() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public void setExternalId(String externalId) {
			// TODO Auto-generated method stub
		}
		@Override
		public void addClassification(Classification clasi) {
			// TODO Auto-generated method stub
		}
		@Override
		public void addClassification(Classifier c, DataSetMember dm) {
			logger.warn("addClassification(Classifier c, DataSetMember dm) does nothing");
		}
		@Override
		public void removeAllClassification(Classifier classifier) {
			// TODO Auto-generated method stub
		}
		@Override
		public void removeClassification(Classification c) {
			// TODO Auto-generated method stub
		}
		@Override
		public String getDescription() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public String getAlias() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public boolean isOnlyRootEdit() {
			// TODO Auto-generated method stub
			return false;
		}
		@Override
		public void setName(String name) {
			// TODO Auto-generated method stub
		}
		@Override
		public String getKey() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public void setKey(String s) {
			// TODO Auto-generated method stub
		}
		@Override
		public String getConsoleDisplayName() {
			return getAlternativeDisplayName();
		}
		@Override
		public List<Classifier> getClassifiers() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public Map<String, List<String>> getClassificationAsMapString() {
			// TODO Auto-generated method stub
			return null;
		}
	};
	
	@Override
	public DataSetMember findMemberById(String value) {
		return getRepository(DataSetMember.class).findById(Long.valueOf(value));
	}
	
	@Override
	public DataSetMember findMemberByExternalId(String value) {
		return getRepository(DataSetMember.class).findByExternalId(value);
	}
	
	@Override
	public List<DataSetMember> findMembersLike(String value) {
		try {
			List<DataSetMember> members = new ArrayList<DataSetMember>();
			DataSetMember member = null;
			TextQuery query = new TextQuery(getLikeStatement(value));
			query.setFaceted(false);
			QueryResponse response = (QueryResponse)getIndex().execute(query);
			SolrDocumentList resultSet = response.getResults();
			for (int i = 0; i<resultSet.size(); i++) {
				SolrDocument solrdocument = resultSet.get(i);
				String title = solrdocument.getFieldValue("title").toString();
				String id = solrdocument.getFieldValue("id").toString();
				int m = id.indexOf("#");
				if (m>=0) id = id.substring(m+1);
				member = new SolrDataSetMember(id);
				members.add(member);
				if (title!=null && value.toLowerCase().equals(title.toLowerCase())) {
					members.clear();
					members.add(member);
					break;
				}
			}	
			return members;
		}
		catch (IndexerException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public List<DataSetMember> findAll(String iqlstatement) {
		try {
			
			List<DataSetMember> members = new ArrayList<DataSetMember>();
			
			SolrParametersQuery query = new SolrParametersQuery(getIndex()) {
				public IqlService getIqlService() {
					return getDomain().getService(IqlService.class);
				}
			};
			query.getParameters().put("domain", String.valueOf(getDomain().getId()));
			query.getParameters().put("type", "datasetmember");
			query.getParameters().put("iql", iqlstatement);
			
			ResultSet result = query.execute();
			
			while (result.hasNext()) {
				members.add((DataSetMember)result.next().getObject());
			}
			return members;
		}
		catch (IndexerException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}
	}
	
	public DataSetMember findMemberById(Long id) {
		return null;
	}
	
	public void setDataSetId(String id) {
		this.dataSetId = id;
	}
	
	public void setDataSet(DataSet dataset) {
		if (dataset!=null)
		this.dataSetId = String.valueOf(dataset.getId());
	}
	
	public String getDataSetId() {
		return this.dataSetId;
	}
	
	public void setMemberType(String type) {
		this.memberType = type;
	}

	public String getMemberType() {
		return this.memberType;
	}
	
	public void setIndex(JavaIndex index) {
		this.index = index;
	}
		
	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

	public JavaIndex getIndex() {
		return index== null ? (JavaIndex)getDomain().getService(JavaIndexerService.class).getIndex() : index;
	}
	
	private String getLikeStatement(String value) {
		StringBuffer statement = new StringBuffer();
		statement.append("type:");
		statement.append(getMemberType());
		statement.append(" AND ");
		if (!value.startsWith("\""))
			statement.append("\"");
		statement.append(value.trim());
		if (!value.endsWith("\""))
			statement.append("\"");
		if (getDataSetId()!=null) {
		statement.append(" AND ");
		statement.append("dataset:");
		statement.append(getDataSetId());
		}
		return statement.toString();
	}
	
	protected <R> DomRepository<R> getRepository(Class<R> objectclass) {
		DomRepository<R> repository = ServiceLocator.getService(DomRepositoryService.class).getRepository(objectclass);
		return repository;
	}
	
}
