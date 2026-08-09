package kbee.web.query;


import java.util.ArrayList;
import java.util.List;

import com.novamens.content.model.DataSetMember;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.dom.Domain;
import com.novamens.dom.ObjectState;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.ValueFilter;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.multidimensional.ClassifierFacet;
import com.novamens.kbee.content.multidimensional.ClassifierHierarchicalFacet;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.multidimensional.SolrCube;
import com.novamens.solr.indexer.multidimensional.SolrFacet;
import com.novamens.solr.indexer.query.SolrQuery;


public class FileFinderQuery extends SolrQuery {
	private static final long serialVersionUID = -3526108821395890502L;

	
	public FileFinderQuery(Index index) {
		super(index);
 	}
	
	public String getStatement() {
		StringBuffer statement = new StringBuffer();
		
		Domain domain = getDomain();
		
		statement.append("(type:idoc OR type:text)");
		
		
		// version head en donde está más copias de trabajo en los wkspaces.
		//
		statement.append("AND (head:true OR inworkspace:true OR state:"+ String.valueOf(ObjectState.DELETED.getId())+")");
		statement.append("AND domain:"+ String.valueOf(domain.getId()));
		
		String ps = getParametersStatement().trim();

		if (!"".equals(ps)) {
			if (!ps.startsWith("AND")) ps = "AND " + ps;
			statement.append(" "+ps);
		}

		return statement.toString();
	}
	
	public String getSolrStatement() {
		return getStatement();
	}
	
	public Domain getDomain() {
		UserProfile profile = ServiceLocator.getService(UserService.class).getSessionUserProfile();
		Domain domain = profile.getDomain();
		return domain;
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
		getParameters().put("members", members);
	}
	
	public List<Facet> getFacets() {
		List<Facet> facets = new ArrayList<Facet>();
		facets.addAll(((SolrCube)getIndex().getCube()).getFacets());
		return facets;
	}
	
	@SuppressWarnings("unchecked")
	private String getParametersStatement() {
		StringBuilder statement = new StringBuilder();
		for (String parameter : getParameters().keySet()) {
			if (parameter.equals("members")) {
				List<String> members = (List<String>)getParameters().get(parameter);
				for (String member : members) {
					if (member.contains("|")) {
						String ormembers[] = member.split("\\|");
						if (statement.length()>0) 
								statement.append(" AND ");
						
						statement.append("(");
						
						for (int m=0; m<ormembers.length; m++) {
							String ormember = ormembers[m];
							int i = ormember.indexOf("/");
							String facetname = ormember.substring(0,i);
							String memberid = ormember.substring(i+1);
							SolrFacet facet = (SolrFacet)getIndex().getCube().getFacet(facetname);
						
							if (m>0) 
								statement.append(" OR ");
							
							statement.append(facet.getName() +":" + memberid);
						}
						statement.append(")");
					}
					else {
						int i = member.indexOf("/");
						String facetname = member.substring(0,i);
						String memberid = member.substring(i+1);
						SolrFacet facet = (SolrFacet)getIndex().getCube().getFacet(facetname);
						
						if (statement.length()>0) 
							statement.append(" AND ");
						
						statement.append(facet.getName() +":" + memberid);
					}
				}
			}
			else {
				if (parameter.equals("domain")) {
					if (statement.length()>0) 
						statement.append(" AND ");
					statement.append("domain:"+(String)getParameters().get(parameter));
				}
				else
				if (!parameter.equals("sort") && !parameter.equals("ascending")) {
					if (statement.length()>0 && getParameters().get(parameter)!=null) 
						statement.append(" AND ");
					Object value = getParameters().get(parameter);
					if (value instanceof ValueFilter) {
						value = ((ValueFilter)value).getValue();
						statement.append(parameter+":"+(String)value);
					}
					else
					if (value instanceof String && !"".equals(value.toString())) {
						String strvalue = (String)value;
						if (strvalue.startsWith("[")) {
							strvalue = strvalue.substring(1, strvalue.length()-1);
							String values[] =strvalue.split(",");
							statement.append("(");
							int i = 0;
							for (String option : values) {
								if (i>0) 
									statement.append(" OR ");
								
								statement.append(parameter+":"+option.trim());
								i++;
							}
							statement.append(")");
						}
						else {
							if(((String)value).trim().contains(" "))
								statement.append(parameter+":("+(String)value+")");
							else
								statement.append(parameter+":"+(String)value);
						}	
					}
				}	
			}	
		}
		return statement.toString();
	}
}
