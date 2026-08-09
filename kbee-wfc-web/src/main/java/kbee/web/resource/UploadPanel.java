package kbee.web.resource;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;

import com.novamens.content.base.Resource;
import com.novamens.kbee.wicket.markup.html.console.panel.AfterUploadEvent;
import com.novamens.kbee.wicket.markup.html.console.panel.ConsoleSidePanel;


@SuppressWarnings("serial")
public class UploadPanel extends ConsoleSidePanel implements ResourcesPanel {
	private static final long serialVersionUID = 1L;
			
	private String upload_errors;

	public UploadPanel(String id) {
		super(id);
		this.setOutputMarkupId(true);
		WebMarkupContainer rv = new WebMarkupContainer("resources-view");
		rv.setOutputMarkupId(true);
		add(rv);
	}

	
	public void addUploadError(String s) {
		if (upload_errors!=null)
			upload_errors+= (" | "+s);
		else
			upload_errors=s;
	}
	
	public void onClose(AjaxRequestTarget target) {
	}
	
	public void onAfterUpload(AjaxRequestTarget target) {
	}
	
	public void onUpload(Resource resource) {
	}
	
	public void add(Resource resource) {
		onUpload(resource);
	}
	
	public void addVersion(Resource resource, Resource version) {
		onUpload(resource);
	}
	
	public void resetUploadErrors() {
		this.upload_errors=null;
	}
	
	public String getUploadErrors() {
		return this.upload_errors;
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		AjaxLink<Void> cl=new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
					resetUploadErrors();
					onClose(target);
			}
		};
		
		add(cl);
		
		add(new kbee.web.uploader.UploadBehavior() {
			@Override
			public boolean isEnabled() {
				return true;
			}
			@Override
			protected String getUrl() {
				return "/formupload?path="+UploadPanel.this.getPath();
			}
			@Override
			protected String getDropElement() {
				return "resources-panel";
			}
			@Override
			public void bind(Component component) {
				Component parent = component;
				if (parent!=null) {
					boolean found = false;
					for (Behavior behavior : parent.getBehaviors()) {
						if (behavior instanceof RefreshBehavior) {
							found = true;
							break;
						}
					}
					if (!found)
					parent.add(new RefreshBehavior(parent.getMarkupId()));
				}
			}
			@Override
			public Component getResourcesPanel() {
				return UploadPanel.this.get("resources-view");
			}
			@Override
			protected void onUpload(AjaxRequestTarget target, String componentId) {
				onAfterUpload(target);
				fire (new AfterUploadEvent(target)); 
			}
			@Override
			protected String getBrowseButton() {
				return "pickfiles";
			}
		});
	}

}
