package kbee.web.object;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.wicket.markup.html.modal.Modal;

import kbee.web.model.object.AuditTrailObjectPanel;

/**
 * 
 * Modal Window for Content Audit Trail
 * @param <T>
 */
public class AuditTrailModal<T extends Content> extends Modal {
	private static final long serialVersionUID = 1L;
	
	public AuditTrailModal(String id) {
		super(id);
		setTitle("modal.audittrail.title");
		setSubtitle("modal.audittrail.subtitle");
		setBody(new AuditTrailObjectPanel<T>("body"));
		setButtons(Modal.Close);
	}
	
	@SuppressWarnings({ "unchecked", "serial" })
	public void open(AjaxRequestTarget target, IModel<T> model) {
		
		setParameters(model.getObject().getTitle());
		setSubtitleParameters(String.valueOf(model.getObject().getOId()));
		
		if (get("modal-dialog")==null)
			super.addComponents();
		
		WebMarkupContainer modal_dialog = (WebMarkupContainer)get("modal-dialog");
		
		Label title = new Label("title", getTitle());
		title.setEscapeModelStrings(false);		
		modal_dialog.addOrReplace(title);
		
		Label subtitle = new Label("subtitle", getSubtitle());
		subtitle.setEscapeModelStrings(false);
		modal_dialog.addOrReplace(subtitle);
		
		((AuditTrailObjectPanel<T>)getBody()).setModel(model);

		super.open(target, new Modal.Handler() {
			@Override
			public void onClick(AjaxRequestTarget target, Button button) {
				
			}
		});	
	}
}