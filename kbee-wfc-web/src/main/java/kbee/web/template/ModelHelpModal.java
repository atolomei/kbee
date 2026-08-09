package kbee.web.template;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;

import com.novamens.text.TemplateModelInfo;
import com.novamens.wicket.markup.html.modal.Modal;


/**
 * 
 * Modal Window for Content Audit Trail
 * @param <T>
 */
@SuppressWarnings("serial")
public class ModelHelpModal extends Modal {
	private static final long serialVersionUID = 1L;
	
	public ModelHelpModal(String id) {
		super(id);
		setTitle("modal.modelhelp.title");
		setSubtitle("modal.modelhelp.subtitle");
		setBody(new ModelHelpPanel("body"));
		setButtons(Modal.Close);
	}
	
	public void open(AjaxRequestTarget target, TemplateModelInfo model) {
		
		if (get("modal-dialog")==null)
			super.addComponents();
		
		WebMarkupContainer modal_dialog = (WebMarkupContainer)get("modal-dialog");
		
		Label title = new Label("title", getTitle());
		title.setEscapeModelStrings(false);		
		modal_dialog.addOrReplace(title);
		
		Label subtitle = new Label("subtitle", getSubtitle());
		subtitle.setEscapeModelStrings(false);
		modal_dialog.addOrReplace(subtitle);
		
		((ModelHelpPanel)getBody()).setModel(model);

		super.open(target, new Modal.Handler() {
			@Override
			public void onClick(AjaxRequestTarget target, Button button) {
				
			}
		});	
	}
}