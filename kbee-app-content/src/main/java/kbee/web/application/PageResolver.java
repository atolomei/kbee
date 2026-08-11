package kbee.web.application;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.component.IRequestablePage;

public interface PageResolver {
    Class<? extends IRequestablePage> resolve(Request request);
}