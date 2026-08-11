package kbee.api.model;

import java.io.Serializable;
import java.util.List;

public class IResponse<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private List<T> page;
	private long size;
	private long offset = 0;
	private long pageSize;
	private List<IFacet> facets;
	
	public void setPage(List<T> page) {
		this.page = page;
	}
	
	public List<T> getPage() {
		return page;
	}
	
	public T get(long index) {
		return page.get((Long.valueOf(index)).intValue());
	}
	
	public void setSize(long size) {
		this.size = size;
	}
	
	public long getSize() {
		return size;
	}
	
	public void setOffset(long offset) {
		this.offset = offset;
	}
	
	public long getOffset() {
		return offset;
	}
	
	public void setPageSize(long size) {
		this.pageSize = size;
	}
	
	public long getPageSize() {
		return pageSize;
	}
	
	public List<IFacet> getFacets() {
		return facets;
	}

	public void setFacets(List<IFacet> facets) {
		this.facets = facets;
	}
}
