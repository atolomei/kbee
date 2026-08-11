package kbee.api.model;

import java.io.Serializable;

public class ITransaction  implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long id;
	private ApiProxy target;
	
	public ITransaction() {
	}
	
	public ITransaction(Long id, ApiProxy target) {
		setId(id);
		setTarget(target);
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public ApiProxy getTarget() {
		return target;
	}
	
	public void setTarget(ApiProxy target) {
		this.target = target;
	}
}
