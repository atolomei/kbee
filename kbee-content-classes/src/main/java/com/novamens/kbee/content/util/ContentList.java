package com.novamens.kbee.content.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.novamens.content.base.Content;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.user.UserService;
import com.novamens.content.workflow.ClassificationRule;
import com.novamens.dom.Domain;
import com.novamens.indexer.java.JavaIndexerService;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.ResultSet;
import com.novamens.indexer.service.Index;
import com.novamens.kbee.content.multidimensional.ClassifierHierarchicalFacet;
import com.novamens.service.ServiceLocator;
import com.novamens.solr.indexer.multidimensional.SolrCube;
import com.novamens.solr.indexer.query.SolrParametersQuery;

public class ContentList implements Serializable, List<Content> {
	private static final long serialVersionUID = 1L;
	
	List<ClassificationRule> criteria;
	
	public ContentList(List<ClassificationRule> criteria) {
		this.criteria = criteria;
	}
	
	public List<ClassificationRule> getCriteria() {
		return criteria;
	}
	
	public Iterator<Content> iterator() {
		return getValues().iterator();
	}
	
	public void add(int i, Content content) {
	}
	
	public boolean addAll(Collection<? extends Content> c) {
		return false;
	}
	public boolean addAll(int index, Collection<? extends Content> c) {
		return false;
	}
	
	public void clear() {
		
	}
	
	public boolean isEmpty() {
		return getValues().isEmpty();
	}
	
	public int size() {
		return getValues().size();
	}
	
	public boolean add(Content arg0) {
		return false;
	}
	
	public Content	set(int index, Content element) {
		return null;
	}
	
	public boolean remove(Object arg0) {
		return false;
	}
	
	public Content remove(int i) {
		return null;
	}
	
	public boolean	removeAll(Collection<?> c) {
		return false;
	}
	
	public ListIterator<Content> listIterator(int i) {
		return null;
	}
	
	public ListIterator<Content> listIterator() {
		return null;
	}
	
	public boolean contains(Object o) {
		return getValues().contains(o);
	}
	
	public boolean	containsAll(Collection<?> c) {
		return getValues().containsAll(c);
	}
	
	public int lastIndexOf(Object o) {
		return getValues().lastIndexOf(o);
	}
	
	public int indexOf(Object o) {
		return getValues().indexOf(o);
	}
	
	public List<Content> subList(int fromIndex, int toIndex) {
		return getValues().subList(fromIndex, toIndex);
	}
	
	public Content get(int index) {
		return getValues().get(index);
	}
	
	public Object[] toArray() {
		return getValues().toArray();
	}
	
	public<T> T[] toArray(T[] a) {
		return getValues().toArray(a);
	}
	
	public boolean retainAll(Collection<?> c) {
		return false;
	}
	
	private List<Content> getValues() {
		List<Content> values = new ArrayList<Content>();
		if (criteria==null) return values;
		List<String> memberscriteria = new ArrayList<String>();
		for (ClassificationRule classificationrule : criteria) {
			DataSetMember member = classificationrule.getValue();
			if (member == null) return values;
			memberscriteria.addAll(getMembersCriteria(member));
		};
		if (memberscriteria.isEmpty()) return values;
		SolrParametersQuery query = new SolrParametersQuery(getIndex());
		query.getParameters().put("type", "[text, idoc]");
		query.getParameters().put("sort", "modified");
		query.getParameters().put("head", "true");
		query.getParameters().put("members", memberscriteria);
		ResultSet results = query.execute();
		while (results.hasNext()) {
			values.add((Content)results.next().getObject());
		}
		results.close();
		return values;
	}
	
	private Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getDomain();
	}
	
	private List<String> getMembersCriteria(DataSetMember member) {
		List<String> members = new ArrayList<String>();
		for (Facet facet : getFacets()) {
			if (facet instanceof ClassifierHierarchicalFacet) {
				ClassifierHierarchicalFacet  classifierfacet = (ClassifierHierarchicalFacet)facet; 
				if (classifierfacet.getClassifier()!=null && classifierfacet.getClassifier().getDataSet().equals(member.getDataSet())) {
					members.add(classifierfacet.getMember(member).getPath());
				}
			}
		}
		return members;
	}
	
	private List<Facet> getFacets() {
		List<Facet> facets = new ArrayList<Facet>();
		facets.addAll(((SolrCube)getIndex().getCube()).getFacets());
		return facets;
	}
	
	private Index getIndex() {
		return getDomain().getService(JavaIndexerService.class).getIndex();
	}
}
