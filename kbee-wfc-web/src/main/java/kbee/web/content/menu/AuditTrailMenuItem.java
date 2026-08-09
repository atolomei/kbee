package kbee.web.content.menu;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;

import com.novamens.content.base.Content;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.object.AuditTrailModal;

public class AuditTrailMenuItem<T extends Content> extends AjaxMenuItemPanelV5<T> {
	private static final long serialVersionUID = 1L;
	
	private WebMarkupContainer container;
	
	public AuditTrailMenuItem(String id, WebMarkupContainer container) {
		super(id);
		this.container = container;
	}
	
	@SuppressWarnings("unchecked")
	public void onClick(AjaxRequestTarget target) {
		Modal modal = getModal();
		ObjectModel<Content> model = new ObjectModel<Content>((Content)getModelObject());
		((AuditTrailModal<Content>)modal).open(target, model);
	}
	
	@Override 
	public String getLabel() {
		return getItemLabelString("contextmenu.audittrail");
	}

	@Override 
	public boolean isEnabled() {
		return isWriteable();
	}
	
	@Override 
	public boolean isVisible() {
		return isWriteable();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		if (container.get("audittrail-modal")==null ||
			!(container.get("audittrail-modal") instanceof Modal)) {
			container.addOrReplace(new AuditTrailModal<Content>("audittrail-modal"));
		}
	}
	
	protected Modal getModal() {
		return (Modal) container.get("audittrail-modal");
	}
	
	public Content getContent() {
		return (Content)getModel().getObject();
	}
	
	protected boolean isWriteable() {
		return ServiceLocator
			.getService(ContentSystemSecurityService.class)
			.isDeleteable(getContent());
	}
}
