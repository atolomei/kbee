package kbee.web.content.template;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;

import com.novamens.content.base.Content;
import com.novamens.wicket.markup.html.modal.Modal;


/**
 * 
 * Modal Window for Content Audit Trail
 * @param <T>
 */
@SuppressWarnings("serial")
public class BrowserModal<T extends Content> extends Modal {
	private static final long serialVersionUID = 1L;
	
	public class CreateButton extends Button {
		public CreateButton() {
			super("modal.template.button.create", "btn btn-lg btn-default");
		}
		public boolean isVisible() {
 			return true; 
		}
		@Override
		public boolean closeOnClick() {
 			return true; 
		}
	};
	
	
	public BrowserModal(String id) {
		super(id);
		setTitle("modal.template.title");
		setSubtitle("modal.template.subtitle");
		setBody(new BrowserPanel<Content>("body"));
		setButtons(new CreateButton(), Modal.Cancel);
	}
	
	public void open(AjaxRequestTarget target, Handler handler) {
		
		//setParameters(model.getObject().getTitle());
		//setSubtitleParameters(String.valueOf(model.getObject().getOId()));
		
		if (get("modal-dialog")==null)
			super.addComponents();
		
		WebMarkupContainer modal_dialog = (WebMarkupContainer)get("modal-dialog");
		
		Label title = new Label("title", getTitle());
		title.setEscapeModelStrings(false);		
		modal_dialog.addOrReplace(title);
		
		Label subtitle = new Label("subtitle", getSubtitle());
		subtitle.setEscapeModelStrings(false);
		modal_dialog.addOrReplace(subtitle);
		
		//((AuditTrailObjectPanel<T>)getBody()).setModel(model);

		super.open(target, handler);
	}
}