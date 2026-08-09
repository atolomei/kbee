package kbee.web.query;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.entity.Person;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.DataSetType;
import com.novamens.content.model.PersonMember;
import com.novamens.content.user.UserProfile;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.multidimensional.ClassifierFacet;
import com.novamens.kbee.content.multidimensional.ClassifierHierarchicalFacet;
import com.novamens.kbee.content.multidimensional.RelationFacet;
import com.novamens.kbee.security.acl.KbeeGroup;
import com.novamens.security.User;
import com.novamens.security.acl.Group;
import com.novamens.solr.indexer.multidimensional.SolrCube;

public class ConsoleQuery extends DomainSolrQuery  {
	
	private static final long serialVersionUID = 1L;
	
	public ConsoleQuery(Index index) {
		super(index);
	}
	
	public void setAsParameter(DataSetMember member) {
		setAsParameter(member, true);
	}
	
	public void setAsParameter(DataSetMember member, boolean descendants) {
		setAsParameter(member, null, descendants);
	}
	
	public void setAsParameter(DataSetMember member, String facetname, boolean descendants) {
		List<String> members = new ArrayList<String>(); 
		for (Facet facet : getFacets()) {
			if (facet instanceof ClassifierFacet) {
				ClassifierFacet  classifierfacet = (ClassifierFacet)facet; 
				if (((ClassifierFacet)facet).getDisplayName().equals(member.getDataSet().getName()) ||
						(facetname!=null && facetname.equals(facet.getDisplayName()))) {
					members.add(((ClassifierFacet)facet).getMember(member).getPath());
				}
				else {
					if (facetname==null && classifierfacet.getClassifier()!=null && classifierfacet.getClassifier().getDataSet().equals(member.getDataSet())) {
						members.add(classifierfacet.getMember(member).getPath());
					}
				}
			}
			else
			if (facet instanceof ClassifierHierarchicalFacet) {
				ClassifierHierarchicalFacet  classifierfacet = (ClassifierHierarchicalFacet)facet; 
				if ((facetname==null && classifierfacet.getDisplayName().equals(member.getDataSet().getName())) || 
					(facetname!=null && facetname.equals(facet.getDisplayName()))) {
					String path = member.getDataSet().isHierachical() && descendants
						? classifierfacet.getMember(member).getPath()+"*"
						: classifierfacet.getMember(member).getPath();		
					members.add(path);
				}
				else {
					if (facetname==null && classifierfacet.getClassifier()!=null && classifierfacet.getClassifier().getDataSet().equals(member.getDataSet())) {
						members.add(classifierfacet.getMember(member).getPath());
					}
				}
			}
			else
			if (facet instanceof RelationFacet) {
				if (((RelationFacet)facet).getClassName().equals("user") && member.getDataSet().getDataSetType().equals(DataSetType.USER)) {
					Person person = ((PersonMember)member).getPerson();
					User user = person.getProfile(UserProfile.class).getUser();
					members.add(facet.getName() + "/" + user.getId());
					break;
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
	
	@Override
	public boolean includeFacets() {
		return true;
	}
	
	protected String getReaders(Group group, String readers) {
		
		StringBuilder str =new StringBuilder();
		String id = ((KbeeGroup)group).getId().toString();
		if (readers.contains(" "+id)) 
			return readers;
		str.append(readers);
		if (!" ".equals(readers)) 
			str.append(", ");
		str.append(((KbeeGroup)group).getId());
		String ret = str.toString();
		for (Group parent : ((KbeeGroup)group).getGroups()) {
			ret = getReaders(parent, ret);
		}
		return ret;
	}
	
	protected String getPath(DataSetMember member) {
		for (Facet facet : getFacets()) {
			if (facet instanceof ClassifierFacet) {
				ClassifierFacet  classifierfacet = (ClassifierFacet)facet; 
				if (((ClassifierFacet)facet).getDisplayName().equals(member.getDataSet().getName())) {
					return ((ClassifierFacet)facet).getMember(member).getPath();
				}
				else {
					if (classifierfacet.getClassifier()!=null && classifierfacet.getClassifier().getDataSet().equals(member.getDataSet())) {
						return classifierfacet.getMember(member).getPath();
					}
				}
			}
			else
			if (facet instanceof ClassifierHierarchicalFacet) {
				ClassifierHierarchicalFacet  classifierfacet = (ClassifierHierarchicalFacet)facet; 
				if (classifierfacet.getDisplayName().equals(member.getDataSet().getName())) {
					return classifierfacet.getMember(member).getPath();
				}
				else {
					if (classifierfacet.getClassifier()!=null && classifierfacet.getClassifier().getDataSet().equals(member.getDataSet())) {
						return classifierfacet.getMember(member).getPath();
					}
				}
			}
		}
		return null;
	}

}
