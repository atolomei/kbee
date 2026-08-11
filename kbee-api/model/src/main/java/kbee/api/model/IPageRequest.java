package kbee.api.model;

import java.io.Serializable;

public interface IPageRequest<T> extends Serializable{
	public IResponse<T> execute(long offset);
}
