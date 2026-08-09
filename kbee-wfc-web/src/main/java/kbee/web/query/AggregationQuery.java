package kbee.web.query;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Member;
import com.novamens.indexer.service.Index;
import com.novamens.indexer.service.IndexerException;
import com.novamens.solr.indexer.multidimensional.SolrMember;
import com.novamens.solr.indexer.query.SolrParametersQuery;

public class AggregationQuery extends SolrParametersQuery {
	private static final long serialVersionUID = 1L;
	
	public AggregationQuery(Index index, DataSet aggregation, DataSetMember aggregator) {
		super(index);
		
		getParameters().put("type", "datasetmember");
		getParameters().put("sort", "modified");
		getParameters().put("dataset", String.valueOf(aggregation.getId()));
		getParameters().put("state", String.valueOf(ObjectState.ENABLED.getId()));
		getParameters().put("domain", String.valueOf(aggregator.getDomain().getId()));
		getParameters().put("ascending", "false");
		
		List<String> members = new ArrayList<String>();
		members.add(getMember(aggregation, aggregator).getPath());
		getParameters().put("members", members);
	}
	
	public Member getMember(DataSet aggregation, DataSetMember aggregator) {
		SolrMember member = new SolrMember();
		Classifier classifier = getClassifier(aggregation, aggregator);
		member.setPath(classifier.getUniqueName()+"member/"+aggregator.getId());
		return member;		
	}
	
	public void setSort(String property, boolean ascending) {
		getParameters().put("sort", property);
		getParameters().put("ascending", ascending?"true":"false");
	}
	
	public Classifier getClassifier(DataSet aggregation, DataSetMember aggregator) {
		Classifier classifier = null;
		boolean found = false;
		for (ModelElementTemplate template : aggregation.getStructure()) {
			if (template.getElement() instanceof Classifier) {
				if (template.getElement() instanceof Classifier) {
					classifier = (Classifier)template.getElement();
					if (classifier.getDataSet().equals(aggregator.getDataSet())) {
						if (found) {
							throw new IndexerException("ambiguous aggreation classifier");
						}
						found = true;
					}
				}
			}
		}
		if (!found) {
			throw new IndexerException("aggreation classifier not found");
		}
		return classifier;
	}
}