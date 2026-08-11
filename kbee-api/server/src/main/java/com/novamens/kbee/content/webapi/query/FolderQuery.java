package com.novamens.kbee.content.webapi.query;


import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.service.DataSetMemberService;
import com.novamens.indexer.service.Index;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.query.SolrParametersQuery;
import lombok.Builder;
import lombok.Getter;

@Getter
public class FolderQuery extends SolrParametersQuery {
    private static final long serialVersionUID = 1L;

    private Site site;
    private DataSet dataset;
    private DataSetMember folder;

    @Builder
    public FolderQuery(
            Index index,
            Site site,
            DataSet dataset,
            DataSetMember folder) {

        super(index);

        this.site = site;
        this.dataset = dataset;
        this.folder = folder;
        
		getParameters().put("sort", "type asc, title_sort asc");
    }

	@Override
	public String getSolrStatement() {
		StringBuffer s = new StringBuffer();
		s.append("(head:true AND type:idoc AND ");
		s.append(getClassifier().getUniqueName()+
			"member:"+
			folder.getService(DataSetMemberService.class).getPaths().get(0));
		s.append(") OR (");
		s.append("type:datasetmember AND parent:"+
			getFolder().getId());	
		s.append(")");
		return s.toString();
	}
	
	@Override
	public String getSolrFilterStatement() {
		return "";
	}
	
	protected Classifier getClassifier() {
		for (Classifier classifier : getContentDao().getClassifiers(getDataset().getDomain())) {
			if (classifier.getDataSet().equals(getDataset())) {
				return classifier;
			}
		}
		return null;
	}
	
	protected ContentDao getContentDao() {		
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}