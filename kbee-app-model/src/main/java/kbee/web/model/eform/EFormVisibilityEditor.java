package kbee.web.model.eform;


import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.form.EForm;
import com.novamens.content.service.DomService;
import com.novamens.kbee.content.form.KbeeEForm;
import com.novamens.kbee.content.form.ScriptEvaluator;
import com.novamens.wicket.markup.html.editor.ObjectEditor;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.InfoDialog;
import com.novamens.wicket.markup.html.form.TextAreaField;

import kbee.web.event.wicket.ErrorEvent;
import kbee.web.form.EditButtonsV5;

@SuppressWarnings("serial")
public class EFormVisibilityEditor extends ObjectEditor<EForm> {
	private static final long serialVersionUID = 1L;
			
//	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EFormVisibilityEditor.class.getName());
	
	public EFormVisibilityEditor(String id, IModel<EForm> model) {
		super(id, model);
		
		setEditionEnabled(false);
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		
		form.add(new TextAreaField<String>("visibilityCondition", 20, 40) {
			public boolean isHelpInfo() {
				return true;
			}
			@Override
			public void onHelp(AjaxRequestTarget target) {
				getHelpModal().open(target, () -> { return "How to write a Script"; }, getScriptHelp());
			}
		});
 		
		add(form);
		
		add(new EditButtonsV5<EForm>(this)  {
			@Override
			public boolean isEnabled() {
				return true;
			}
		});
		
		add(new InfoDialog("help-modal"));
	}	
	
	@Override
	public void update(AjaxRequestTarget target) {
		try {
			if (!getUpdatedParts().isEmpty()) {
				KbeeEForm eform = (KbeeEForm)getModelObject();
				eform.getService(DomService.class).update(getUpdatedParts());
				reset();
			}
		}
		catch (Exception e) {
			fire(new ErrorEvent<>(target, e));
		}
	}
	
	private IModel<String> getScriptHelp() {
		return new Model<String>(ScriptEvaluator.GetHelpText(getModelObject())
				+ "<br/>Example: content.getValue('tiporecibo')=='pdf' || content.getValue('tiporecibo')==null"	);
	}
	
	private InfoDialog getHelpModal() {
		return (InfoDialog) get("help-modal");
	}
}
