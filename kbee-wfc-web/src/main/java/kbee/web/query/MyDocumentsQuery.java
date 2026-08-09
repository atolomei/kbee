package kbee.web.query;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.model.Classifier;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.service.Index;

public class MyDocumentsQuery extends ContentQuery {
	
	private static final long serialVersionUID = 1L;
	
	public MyDocumentsQuery(Index index) {
		super(index);
		
		getFilterParameters().put("type", "idoc");
		getParameters().put("sort", "title_sort");
		getParameters().put("ascending", "false");
		getFilterParameters().put("state", String.valueOf(ObjectState.ENABLED.getId()));
		
	}
	
	public String getIqlStatement() {
		
		StringBuilder str = new StringBuilder();
		
		boolean terms = false;
		
		for (Classifier classifier : getUserClassifiers()) {
			if (str.length() > 0) {
				str.append(" OR ");
				terms = true;
			}
			str.append(classifier.getPredicate() + "(user)");
		}
		
		if (terms)
			return  "(" + str.toString() + ")";
		else 
			return str.toString();
	}


	public String getStatement() {
		String iqlStatement = getIqlStatement();

		// nada si no hay my docs
		String statement  = "".equals(iqlStatement) ?  "head:x" : getIqlClause(iqlStatement);			
				
		statement += " AND (inworkspace:true OR head:true)";
		String securitystatement = getReadersStatement();
		if (!"".equals(securitystatement)) {
			return statement + " AND " + securitystatement;
		}
		return statement;
	}
	
	private List<Classifier> getUserClassifiers() {
		List<Classifier> classifiers = new ArrayList<>();
		for (Classifier classifier : getContentDao().getClassifiers(getDomain())) {
			if (classifier.isMyDocument()) {
				if (classifier.getPredicate()!=null) {
					classifiers.add(classifier);
				}
			}
		}
		return classifiers;
	}
	
}