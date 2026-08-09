package kbee.web.resource;

import java.util.Iterator;
import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.event.Event;
import com.novamens.event.EventListener;

import kbee.web.eform.EAjaxRefreshEvent;

public class ImageViewerBehavior extends Behavior implements EventListener {
	private static final long serialVersionUID = 1L;
	
	protected static final ResourceReference JS = new JavaScriptResourceReference(ImageViewerBehavior.class, "viewer/viewer.js");
	protected static final ResourceReference CSS = new CssResourceReference(ImageViewerBehavior.class, "viewer/viewer.css");
	
	Component component;

	public void bind(Component component) {
		this.component = component;
	}
	
	public boolean listen(Event event) {
		return event instanceof EAjaxRefreshEvent;
	}
	
	public void onEvent(Event event) {
		if (((EAjaxRefreshEvent)event).getRequestTarget()!=null) {
			String script =	"setTimeout(function () {try { top.viewer.update(); } catch(e) { alert(e); } }, 2000)";
			((EAjaxRefreshEvent)event).getRequestTarget().appendJavaScript(script);
		}
	}
	
	@Override
	public void beforeRender(Component component) {
		disableImages(component.getPage().iterator());
	}
	
	@Override
	public void afterRender(Component component) {
		Optional<AjaxRequestTarget> target = RequestCycle.get().find(AjaxRequestTarget.class);
		if (target!=null && target.isPresent()) {
			target.get().appendJavaScript(getScript(component));
		}
	}
	
	@Override
	public void renderHead(final Component component, final IHeaderResponse response) {
		super.renderHead(component, response);
		
		response.render(JavaScriptHeaderItem.forReference(JS));
		response.render(CssHeaderItem.forReference(CSS));
		
		response.render(OnDomReadyHeaderItem.forScript(getScript(component)));
	}
	
	protected String getScript(Component component) {
		String script = "";
		script += "var galley = document.getElementById('"+component.getMarkupId()+"');";
		script += "top.viewer = new Viewer(galley, {";
		script += "});";
		return script;
	}
	
	protected void disableImages(Iterator<Component> components) {
		while (components.hasNext()) {
			Component component = components.next();
			if (component instanceof ResourceViewPanel<?>) {
				((ResourceViewPanel<?>)component).setLinksEnabled(false);
			}
			if (component instanceof MarkupContainer) {
				disableImages(((MarkupContainer)component).iterator());
			}
		}
	}

}