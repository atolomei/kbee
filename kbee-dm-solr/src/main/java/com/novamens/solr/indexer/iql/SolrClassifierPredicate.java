package com.novamens.solr.indexer.iql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.dao.MemberDao;
import com.novamens.content.iql.ClassifierPredicate;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.PersonMember;
import com.novamens.content.user.UserProfile;
import com.novamens.content.user.UserService;
import com.novamens.security.User;
import com.novamens.service.ServiceLocator;

public class SolrClassifierPredicate extends SolrAbstractPredicate implements ClassifierPredicate {
	private Classifier classifier;
	private MemberDao memberDao;

	@Override
	public String getHelpValueTypeDescription() {
		return "Dataset Value -> " + 
				((classifier!=null && classifier.getDataSet()!=null) ? classifier.getDataSet().getName() : "null");
	}
	
	@Override
	public boolean isInformatioModel() {
		return true;
	}
	
	public boolean isCanonical() {
		return false;
	}
	
	public String getCode(String argument) {
		
		StringBuilder code = new StringBuilder();
		
		if ("any".equals(argument.toLowerCase())) {
			return getPath() + ":[* TO *]";
		}
		
		if ("user".equals(argument.toLowerCase())) {
			DataSetMember person = getPerson(getSessionUserProfile());
			argument = person!=null ? String.valueOf(person.getId()) : "0";
		}
		
		if (argument!=null) {
			argument = argument.trim();
		}
		
		if (argument!=null && argument.startsWith("\"")) {
			argument = argument.substring(1);
		}
		
		if (argument!=null && argument.endsWith("\"")) {
			argument = argument.substring(0, argument.length()-1);
		}
		
		String boost =  null;
		if (argument!=null && argument.contains("^")) {
			int i = argument.indexOf("^");
			if (i<argument.length()) {
				String value = argument.substring(i+1);
				if (isDigits(value)) {
					boost = value;
					argument = argument.substring(0, i);
				}
			}
		}
		
		if (isDigits(argument)) {
			if (getClassifier().getDataSet().isHierachical()) {
				DataSetMember member = findMemberById(argument);
				if (member!=null) {
					int p=0;
					code.append(getPath() + ":(");
					for (String path : getPaths(member)) {
						if (p>0) code.append(" OR ");
						code.append(path+"*");
						p++;
					}
					code.append(")");
				}
				else {
					code.append(getPath() +":x");
				}
			}
			else {
				code.append(getPath() + ":" + argument);
			}
		}
		else {
			List<DataSetMember> members = getMembers(argument);
			if (members.size()>0) 
				code.append("("+getPath() + ":(");
			
			int i=0;
			
			for (DataSetMember member : members) {
				if (i>0) code.append(" OR ");
				if (getClassifier().getDataSet().isHierachical()) {
					int p = 0;
					for (String path : getPaths(member)) {
						if (p>0) code.append(" OR ");
						code.append(path+"*");
						p++;
					}
				}
				else {
					code.append(String.valueOf(member.getId()));
				}
				i++;
			}
			
			if (i>0) 
				code.append("))");
			
			if (members.isEmpty()) {
				code.append(getPath() +":x");
			}
		}
		
		if (boost!=null) {
			code.append("^"+boost);
		}
	
		return code.toString();
	}
	
	
	@SuppressWarnings("unchecked")
	public boolean evaluate(Object object, Object argument) {
		if (!(object instanceof Content)) return false;
		
		boolean evaluation = false;
	
		List<DataSetMember> members;
		
		if (argument instanceof List<?>) {
			if (!((List<?>)argument).isEmpty() && ((List<?>)argument).get(0) instanceof DataSetMember) 
				members = (List<DataSetMember>)argument;
			else
				return false;
		}
		else {
			if (argument instanceof String) {
				if (isDigits((String)argument)) {
					Content content = (Content)object;
					for (Classification classification : content.getClassification()) {
						if (classification!=null && classification.getClassifier()!=null && classification.getClassifier().equals(getClassifier())) {
							if (classification.getDataSetMember().getId().equals(Long.valueOf((String)argument))) {
								return true;
							}	
							else {
								if (getClassifier().getDataSet().isHierachical()) {
									for (String path : getPaths(classification.getDataSetMember())) {
										if (path.contains((String)argument)) {
											String ids[] = path.split("/");
											for (String id : ids) {
												if (id.trim().equals(argument)) {
													return true;
												}
											}
										}
									}
								}
							} 
						}
					}
					return false;
				}
				else {
					if ("null".equals(argument)) {
						return ((Content)object).getClassification(getClassifier()).isEmpty();
					}
					else {
						members = getMembers((String)argument);
					}
				}	
			}	
			else
				return false;
		}
		
		Content content = (Content)object;
		
		for (Classification classification : content.getClassification()) {
			if (classification!=null && classification.getClassifier().equals(getClassifier())) {
				for (DataSetMember member : members) {
					if (classification.getDataSetMember().getId().equals(Long.valueOf((String)member.getId()))) {
						evaluation = true;
						break;
					}
					
				}
			}
		}
		
		return evaluation;
	}
	
	public List<DataSetMember> getMembers(String argument) {
		List<DataSetMember> members; 
		if (getClassifier().isHierarchical() && argument.contains("/")) {
			String name = argument.substring((argument.lastIndexOf("/")));
			members = getMemberDao().findMembersLike(name);
			members = filterByPath(members, argument);
		}
		else {
			members = getMemberDao().findMembersLike(argument);
		}
		return members;
	}
	
	public DataSetMember findMemberById(String argument) {
		return getMemberDao().findMemberById(argument);
	}
	
	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}
	
	public Classifier getClassifier() {
		return classifier;
	}
	
	public MemberDao getMemberDao() {
		return memberDao;
	}
	
	public void setMemberDao(MemberDao dao) {
		this.memberDao = dao;
	}
	
	private List<DataSetMember> filterByPath(List<DataSetMember> members, String path) {
		List<DataSetMember> filtered = new ArrayList<>();
		for (DataSetMember member : members) {
			if (getPathNames(member).contains(path)) {
				filtered.add(member);
			}
		}
		return filtered;
	}
	
	private UserProfile getSessionUserProfile() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile();
	}
	
	private DataSetMember getPerson(UserProfile userprofile) {
		List<DataSetMember> members = getContentDao().findMembersByEntity(userprofile.getPerson());
		for (DataSetMember member : members) {
			if (member.getDataSet().equals(getClassifier().getDataSet())) {
				return member;	
			}
		}
		return null;
	}
	
		
	private List<String> getPaths(DataSetMember member) {
	    return getPaths(member, new HashSet<>(), DataSetMember::getId);
	}

	private List<String> getPathNames(DataSetMember member) {
	    return getPaths(member, new HashSet<>(), DataSetMember::getDisplayName);
	}

	private List<String> getPaths(
	        DataSetMember member,
	        Set<String> visited,
	        Function<DataSetMember, Serializable> mapper
	) {
	    List<String> paths = new ArrayList<>();

	    if (!visited.add(String.valueOf(member.getId()))) {
	        return paths; // ciclo detectado
	    }
	    
	    member = findMemberById(String.valueOf(member.getId()));
	    
	    List<DataSetMember> parents = member.getParents();

	    if (parents != null && !parents.isEmpty()) {
	        for (DataSetMember parent : parents) {
	            List<String> parentPaths = getPaths(parent, new HashSet<>(visited), mapper);

	            for (String parentPath : parentPaths) {
	                paths.add(parentPath + "/" + mapper.apply(member));
	            }
	        }
	    }

	    if (paths.isEmpty()) {
	        paths.add(String.valueOf(mapper.apply(member)));
	    }

	    return paths;
	}
	
	private boolean isDigits(String argument) {
		for (int c=0; c<argument.length(); c++) {
			if (!Character.isDigit(argument.charAt(c))) {
				return false;
			}
		}
		return true;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
}
