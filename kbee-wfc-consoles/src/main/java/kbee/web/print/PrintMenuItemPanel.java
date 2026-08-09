package kbee.web.print;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceNode;
import com.novamens.content.resource.KBFile;
import com.novamens.content.service.UrlService;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;

public abstract class PrintMenuItemPanel<T extends Resource> extends  AjaxMenuItemPanelV5<T> {
	private static final long serialVersionUID = 1L;
	
	public PrintMenuItemPanel(String id) {
		super(id);
	}
	
	public void onClick(AjaxRequestTarget target) {
		Resource resource = getModelObject();
		//String uri = resource.getId() + "/" + resource.getPath();
		String uri = resource.getService(UrlService.class).getUrl();
		String script = "printJS('"+uri+"');";
		target.appendJavaScript(script);
  	}
	
	@Override
	public boolean isVisible() {
		return isFile() && getModelObject().getName().toLowerCase().endsWith(".pdf");
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		add(new PrintBehavior());
	}
	
	private boolean isFile() {
		return getModelObject() instanceof KBFile ||
		(getModelObject() instanceof ResourceNode && ((ResourceNode)getModelObject()).getResource() instanceof KBFile);
	}
}
