package kbee.web.content.menu;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;

import com.novamens.content.base.Content;
import com.novamens.content.security.ContentSystemSecurityService;
import com.novamens.content.service.ContentService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.actions.AjaxMenuItemPanelV5;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.markup.html.modal.Dialog.Button;

import kbee.web.error.ApplicationErrorPage;

@SuppressWarnings("serial")
public class DeleteMenuItem<T extends Content> extends AjaxMenuItemPanelV5<T> {
	private static final long serialVersionUID = 1L;
	
	private WebMarkupContainer container;

	public DeleteMenuItem(String id) {
		super(id);
	}
	
	public DeleteMenuItem(String id, WebMarkupContainer container) {
		super(id);
		this.container = container;
	}
	
	public void onClick(AjaxRequestTarget target) {
//		if (!getContent().isLocked()) {
//			try {
//				getContent().getService(ContentService.class).recycle();
//				refresh(target);
//			} 
//			catch (Exception e) {
//				setResponsePage( new ApplicationErrorPage<>(e));
//			}
//		}
		if (getContent().isLocked()) {
			return;
		}

		getModal().open(target, 
			getItemLabel("contextmenu.delete.confirmation", getModel().getObject().getTitle()), 
			Dialog.Delete, 
			new Dialog.Handler() {
				@Override
				public void onClick(AjaxRequestTarget target, Button button) {
					if (button.key().equals(Dialog.Delete.key())) {
						try {
							getContent().getService(ContentService.class).recycle();
							refresh(target);
						}	
						catch (Exception e) {
							setResponsePage( new ApplicationErrorPage<>(e));
						}
					}
				}	
			});
	}
	
	@Override 
	public String getLabel() {
		return getItemLabelString("contextmenu.delete");
	}
	
	@Override
	public String getWorkingLabel() {
		return getItemLabelString("contextmenu.delete.working");
	}
	
	@Override
	public boolean isEnabled() {
		return !getContent().isLocked() && 
			isDeleteable();
	}
	
	@Override
	public boolean isVisible() {
		return !getContent().isLocked() && 
			isDeleteable();
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		if (container.get("confirmation-modal")==null ||
			!(container.get("confirmation-modal") instanceof Modal)) {
			container.addOrReplace(new ConfirmationDialog("confirmation-modal"));
		}
	}
	
	protected ConfirmationDialog getModal() {
		return (ConfirmationDialog) container.get("confirmation-modal");
	}
	
	protected void refresh(AjaxRequestTarget target) {
		
	}
	
	protected boolean isDeleteable() {
		return ServiceLocator
			.getService(ContentSystemSecurityService.class)
			.isDeleteable(getContent());
	}
	
	public Content getContent() {
		return (Content)getModel().getObject();
	}
};