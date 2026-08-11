package com.novamens.kbee.content.webapi.type;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.multidimensional.FacetService;
import com.novamens.content.multidimensional.FacetWrapper;
import com.novamens.dom.Domain;
import com.novamens.indexer.query.Facet;
import com.novamens.indexer.query.Member;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.ResultSet;
import com.novamens.solr.indexer.query.SolrResultSet;

import kbee.api.model.IFacet;
import kbee.api.model.IMember;
import kbee.api.model.IResponse;

public class ResultSetAdapter<T1, T2> implements Adapter<ResultSet, IResponse<T2>>{
	
	private Adapter<T1, T2> adapter;
	private int pageSize;
	private boolean includeFacets = false;
	private Domain domain = null;
	String context;
	
	public ResultSetAdapter(Adapter<T1, T2> adapter, int pageSize) {
		this(adapter, pageSize, false);
	}
	
	public ResultSetAdapter(Adapter<T1, T2> adapter, int pageSize, boolean includeFacets) {
		this.adapter = adapter;
		this.pageSize = pageSize;
		this.includeFacets = includeFacets;
	}
	
	public ResultSetAdapter(Domain domain, String context, Adapter<T1, T2> adapter, int pageSize, boolean includeFacets) {
		this.domain = domain;
		this.context = context;
		this.adapter = adapter;
		this.pageSize = pageSize;
		this.includeFacets = includeFacets;
	}
	
	public Adapter<T1, T2> getAdapter() {
		return adapter;
	}
	
	@SuppressWarnings("unchecked")
	public IResponse<T2> adapt(ResultSet resultSet) {
		List<T2> page = new ArrayList<T2>();
		int i = 0;
		while (resultSet.hasNext() && i<pageSize) {
			T1 object = (T1)resultSet.next().getObject();
			if (object!=null) {
				T2 iobject = getAdapter().adapt(object);
				if (iobject!=null) {
					page.add(iobject);
					i++;
				}
			}
			else {
				// System.out.println("null");
			}
		}
		IResponse<T2> iResultSet = new IResponse<T2>();  
		iResultSet.setPage(page);
		iResultSet.setPageSize(pageSize);
		iResultSet.setSize(resultSet.size());
		
		if (includeFacets) {
			iResultSet.setFacets(getFacets(resultSet));
		}
		
		return iResultSet;
	}
	
	private List<IFacet> getFacets(ResultSet resultSet) {
		List<IFacet> facets = new ArrayList<IFacet>();
 		for (Facet facet : getVisibleFacets(resultSet)) {
			IFacet ifacet = new IFacet();
			
			ifacet.setId(String.valueOf(((FacetWrapper)facet).getFacet().getId()));
			ifacet.setName(facet.getName());
			ifacet.setDisplayName(facet.getDisplayName());
			List<IMember> imembers = new ArrayList<IMember>();
			List<Member> members = resultSet.getMembers(facet, 200);
			//if (members.size()>1) {
				for (Member member : members) {
					IMember imember = new IMember();
					imember.setId(member.getPath());
					imember.setPath(member.getPath());
					imember.setDisplayName(member.getDisplayName());
					imember.setCount(member.getCount());
					imembers.add(imember);
				}
				ifacet.setValues(imembers);
				facets.add(ifacet);
			//}
		}
		return facets;
		
	}
	
	private Domain getDomain() {
		return domain;
	}
	
	private List<Facet> getVisibleFacets(ResultSet resultSet) {
		List<Facet> facets = new ArrayList<Facet>();
		if (resultSet instanceof SolrResultSet) {
			Query query = ((SolrResultSet)resultSet).getQuery();
			for (Facet facet : getDomain().getService(FacetService.class).getFacets(query)) {
				if (context==null || ((FacetWrapper)facet).isVisible(context)) {
					facets.add(facet);
				}
			};
		}
		return facets;
	}
}
