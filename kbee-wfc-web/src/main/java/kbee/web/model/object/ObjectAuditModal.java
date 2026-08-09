package kbee.web.model.object;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

// import com.novamens.content.web.object.markup.AuditTrailObjectPanel;
import com.novamens.kbee.wicket.util.DisplayNameExtractor;
import com.novamens.wicket.markup.html.modal.Modal;

public class ObjectAuditModal<T> extends Modal {
	private static final long serialVersionUID = 1L;
	
	public ObjectAuditModal(String id) {
		super(id);

		setTitle("modal.audittrail.title");
		setBody(new AuditTrailObjectPanel<T>("body", null));
		setButtons(Modal.Close);
	}
	
	public void open(AjaxRequestTarget target, IModel<T> model) {
		open(target, model, false);
	}
	
	@SuppressWarnings({ "unchecked", "serial" })
	public void open(AjaxRequestTarget target, IModel<T> model, boolean adjusth) {
		
		setParameters(DisplayNameExtractor.get(model.getObject()));
		
		WebMarkupContainer modal_dialog = (WebMarkupContainer)get("modal-dialog");
		
		Label title = new Label("title", getTitle());
		
		title.setEscapeModelStrings(false);		
		
		modal_dialog.addOrReplace(title);
		
		((AuditTrailObjectPanel<T>)getBody()).setAdjustHeight(adjusth);
		((AuditTrailObjectPanel<T>)getBody()).setModel(model);
		
		super.open(target, new Modal.Handler() {
			@Override
			public void onClick(AjaxRequestTarget target, Button button) {
			}
		});	
	}
}
