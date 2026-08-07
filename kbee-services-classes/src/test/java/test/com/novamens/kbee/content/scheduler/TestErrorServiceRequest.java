package test.com.novamens.kbee.content.scheduler;


import com.novamens.scheduler.AbstractServiceRequest;

public class TestErrorServiceRequest extends AbstractServiceRequest {
	private static final long serialVersionUID = 1L;
	
	public void execute() {
		throw new RuntimeException("error");
	}
}
