package kbee.api.model;

import java.io.Serializable;

public class IResultSet<T> implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private long index = 0;
	
	private IResponse<T> response;
	private IPageRequest<T> pageRequest;
	
	public IResultSet(IPageRequest<T> pageRequest) {
		this.pageRequest = pageRequest;
	}
	
	public boolean hasNext() {
		if (response==null) {
			response = pageRequest.execute(0);
		}
		return response!=null && index<response.getSize();
	}
	
	public T next() {
		if (index>0 && index%getPageSize()==0 && hasNext()) {
			long offset = response.getOffset()+getPageSize();
			if (response.getOffset()==0) offset = offset+1;
			response = pageRequest.execute(offset);
		}
   		T object = response.get((index++)%getPageSize());
		return object;
	}
	
	public long getPageSize() {
		return response!=null ? response.getPageSize() : 0;
	}
	
	public long getSize() {
		if (response==null) {
			response = pageRequest.execute(0);
		}
		return response!=null ? response.getSize() : 0;
	}


}
