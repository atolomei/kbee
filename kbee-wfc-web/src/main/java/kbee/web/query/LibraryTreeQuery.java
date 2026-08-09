package kbee.web.query;

import java.io.Serializable;
import java.util.Map;

import com.novamens.content.library.Library;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.indexer.query.Criteria;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;

import kbee.web.dataset.DataSetNode;

public class LibraryTreeQuery extends ContentQuery {
	private static final long serialVersionUID = 1L;
	
	String nodeId = null;
	Serializable dataSetId = null;
	Serializable domainId = null;
	String classifierName = null;
	
	public LibraryTreeQuery(Index index) {
		super(index);
		getFilterParameters().remove("state");
		getFilterParameters().put("type", "idoc");
	}
	
	public LibraryTreeQuery(DataSet dataSet, Library library, Index index) {
		super(index);
		
		getFilterParameters().remove("state");
		getFilterParameters().remove("head");
		
		getFilterParameters().put("type", "[idoc, datasetmember]");
		
		setLibrary(library);
		
		dataSetId = dataSet.getId();
		domainId = dataSet.getDomain().getId();
		Classifier classifier = getClassifier(dataSet);
		classifierName = classifier!=null ? classifier.getUniqueName() : "x";
	}
	
	@Override
	public Map<String, Object> getParameters() {
		return super.getParameters();
	}
	
	@Override
	public void setParameter(String name, Object value) {
		if (value==null) {
			nodeId=null;
			getParameters().remove(name);
		}
		else {
			if ("node".equals(name))
				setNode(((DataSetNode)value).getObject());
			else
				super.setParameter(name, value);
		}
	}
	
	public void setLibrary(Library library) {
		if (library==null)
			return;
		Criteria criteria = library.getCriteria();
		if (criteria!=null) {
			Map<String, Object> parameters = criteria.getParameters();
			for (String parametername : parameters.keySet()) {
				getParameters().put(parametername, parameters.get(parametername));
			}
		}
	}

	@Override
	public ResultSet execute() {
		String sortfield = (String)getParameters().get("sort");
		if (sortfield==null || !sortfield.startsWith("type")) {
			if (sortfield==null) sortfield="";
			sortfield = "type asc" + (sortfield==null ? "" : ", " + sortfield);
			getParameters().put("sort", sortfield);
		}
		getParameters().put("sort", "type asc, title_sort asc");
		return super.execute();
	}
	
	public String getSortField() {
		return "type asc, " + super.getSortField();
	}
	
	
	@Override
	public String getStatement() {
		String statement = super.getStatement();
		if (nodeId!=null) {
			statement = "("+statement+") OR (type:datasetmember AND parent:"+nodeId;
			// secured set??
			String securitystatement = getReadersStatement();
			if (!"".equals(securitystatement)) {
				statement += " AND " + securitystatement;
			}
			statement +=")";
		}
		else {
			statement  = "(("+statement+") AND NOT ";
			statement += classifierName + "name:* AND domain:"+ domainId + ")";
			statement += " OR (type:datasetmember AND NOT parent:* AND dataset:"+dataSetId;
			String securitystatement = getReadersStatement();
			if (!"".equals(securitystatement)) {
				statement += " AND " + securitystatement;
			}
			statement +=")";
		}
		return statement;
	}	
	
	public void setNode(DataSetMember member) {
		nodeId = String.valueOf(member.getId());
		setAsParameter(member, false);
		getParameters().put("type", "idoc");
	}
	
	private Classifier getClassifier(DataSet dataSet) {
		for (Classifier classifier : getContentDao().getClassifiers(getDomain())) { 
			if (classifier!=null && classifier.getDataSet().equals(dataSet) && classifier.isHierarchical()) {
				return classifier;
			}	
		}
		return null;
	}
	
//	protected String getReadersStatement() {
//		
//		String statement = "";
//		
//		SecurityService service = ServiceLocator.getService(SecurityService.class);
//		
//		User user = service.getSessionUser();
//		
//		boolean admin = service.isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId()); 
//		boolean support = service.isMember(KbeeGlobalRole.SUPPORT.getId());
//		
//		if (!service.isRoot() && !admin && !support) {
//			List<String> readers = new ArrayList<>();
//			readers.add(String.valueOf(user.getId()));
//			for (Group group : user.getGroups()) {
//				readers = getReaders(group, readers);
//			}
//			for (String principal : readers) {
//				if ("".equals(statement))
//					statement += "reader:(";
//				else
//					statement += " OR ";
//				statement += principal;
//			}
//			statement +=")";
//		}	
//		
//		return statement;
//	}
//	
//	protected List<String> getReaders(Group group, List<String> readers) {
//		
//		String id = ((KbeeGroup)group).getId().toString();
//	
//		if (readers.contains(id)) 
//			return readers;
//		
//		readers.add(id);
//	
//		for (Group parent : ((KbeeGroup)group).getGroups()) {
//			readers = getReaders(parent, readers);
//		}
//		
//		return readers;
//	}
}
	