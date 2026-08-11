package com.novamens.kbee.content.query;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.model.DataSetMember;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.multidimensional.ClassifierFacet;
import com.novamens.kbee.content.multidimensional.ClassifierHierarchicalFacet;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.multidimensional.SolrCube;
import com.novamens.solr.indexer.query.SolrParametersQuery;

public class MemberQuery extends SolrParametersQuery {
	private static final long serialVersionUID = 1L;

																				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(MemberQuery.class.getName());

	
	public MemberQuery(Index index, DataSetMember member) {
		super(index);
		getParameters().put("domain", String.valueOf(getDomain().getId()));
		getParameters().put("-type", "rule");
		setAsParameter(member);
	}
	
	@Override
	public String getStatement() {
		return super.getStatement();
	}
	
	public void setAsParameter(DataSetMember member) {
		List<String> members = new ArrayList<String>();
		for (Facet facet : getFacets()) {
			if (facet instanceof ClassifierFacet) {
				ClassifierFacet  classifierfacet = (ClassifierFacet)facet; 
				if (((ClassifierFacet)facet).getDisplayName().equals(member.getDataSet().getName())) {
					members.add(((ClassifierFacet)facet).getMember(member).getPath());
				}
				else {
					if (classifierfacet.getClassifier()!=null && classifierfacet.getClassifier().getDataSet().equals(member.getDataSet())) {
						members.add(classifierfacet.getMember(member).getPath());
					}
				}
			}
			if (facet instanceof ClassifierHierarchicalFacet) {
				ClassifierHierarchicalFacet  classifierfacet = (ClassifierHierarchicalFacet)facet; 
				if (classifierfacet.getDisplayName().equals(member.getDataSet().getName())) {
					members.add(classifierfacet.getMember(member).getPath());
				}
				else {
					if (classifierfacet.getClassifier()!=null && classifierfacet.getClassifier().getDataSet().equals(member.getDataSet())) {
						members.add(classifierfacet.getMember(member).getPath());
					}
				}
			}
		}
		
		
		// VER CON ALEJO
		//
		if (members.isEmpty()) {
			getParameters().put("type", "[nonexistent]");
			logger.error("Members is empty");
		}
		
		getParameters().put("members", members);
	}
	
	public List<Facet> getFacets() {
		List<Facet> facets = new ArrayList<Facet>();
		facets.addAll(((SolrCube)getIndex().getCube()).getFacets());
		return facets;
	}
	
	public Domain getDomain() {
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		Domain domain = profile.getDomain();
		return domain;
	}
}
