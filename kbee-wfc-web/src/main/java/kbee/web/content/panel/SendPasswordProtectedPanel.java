package kbee.web.content.panel;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptContentHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.base.Content;
import com.novamens.content.form.EForm;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.service.UrlService;
import com.novamens.kbee.content.document.KbeeTreeIDoc;
import com.novamens.kbee.content.form.EFormAbstractField;
import com.novamens.kbee.content.form.KbeeEMemForm;
import com.novamens.kbee.content.form.KbeeEMembersListField;
import com.novamens.kbee.content.form.KbeeEStringField;
import com.novamens.kbee.content.form.KbeeEStringModel;
import com.novamens.kbee.content.form.KbeeEStringPropertyModel;
import com.novamens.kbee.content.form.KbeeETextField;
import com.novamens.kbee.content.form.KbeeExternalFormValuesModel;
import com.novamens.wicket.markup.html.form.TextAreaField;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.markup.html.form.Form.Disposition;


import kbee.web.eform.ObjectFormEditor;
import kbee.web.panel.AlertPanel;

public class SendPasswordProtectedPanel<T  extends Content> extends ObjectFormEditor<T> {
											
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SendPasswordProtectedPanel.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	private static int NOT_GENERATED= 0;
	private static int GENERATED= 1;
    
    private int status = NOT_GENERATED;
    private WebMarkupContainer copy_contanier;
    
	private Form<?> form;

	private String link;
	private String password;
	
	public SendPasswordProtectedPanel(String id, IModel<T> model) {
		super(id, model, false, false);
	}


	@Override
	protected Form<?> getWicketForm() {
		return form;
	}

    
	@Override
	public void onInitialize() {
		super.onInitialize();
		setOutputMarkupId(true);
		setEditionEnabled(true);
	}
	
	
	/**
	 * 
	 * 
	 */
	protected EForm getForm(T object) {
		KbeeEMemForm eform = new KbeeEMemForm();
			return eform;
	}

	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
	
	
		//if (status==GENERATED) {
			Component textarea = ta.getInput();
			StringBuffer script = new StringBuffer();
			script.append("function copyToClipboard() {");
			script.append("		var copyTextArea = document.getElementById('"+textarea.getMarkupId()+"');");
			script.append("		copyTextArea.focus();");
			script.append("		copyTextArea.select();");
			script.append("		try {");
			script.append("			navigator.clipboard.writeText(copyTextArea.value);");
			script.append("			document.getElementById('copy-feedback').innerHTML = '<span>"+ " "  +getLabelString("copy-feedback")+"</span>';");
			script.append("		} catch (err) {");
			script.append("			console.log('Oops, unable to copy');");
			script.append("		}");
			script.append("}");
			response.render(new JavaScriptContentHeaderItem(script.toString(), "copy"));
		// }
		
		
	}
	
	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}	
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String link) {
		this.password = link;
	}	
	

	TextAreaField<String> ta;
	
	public void onBeforeRender() {
		super.onBeforeRender();

		link = null;
		password = null;
		status=NOT_GENERATED;
		
		
		AlertPanel<Void> pa=new AlertPanel<Void>("alert-text",AlertPanel.INFO,  null, getLabel("password-protected"), getLabel("alert-text"));
		pa.setIcon("fa-duotone fa-lock-keyhole");
		addOrReplace(pa);
		
		form = new com.novamens.wicket.markup.html.form.Form<Void>("form", Disposition.VERTICAL);
		
		
		addOrReplace(form);
											
		form.add(new TextField<String>("password", new PropertyModel<String>(this, "password")));
		
		AjaxLink<Void> ln = new AjaxLink<Void>("generate", null) {

			private static final long serialVersionUID = 1L;
			
			
			@Override
			public void onClick(AjaxRequestTarget target) {
						status = GENERATED;
						setPassword(((TextField<String>) form.get("password")).getValue());
						String p=getPassword();
						link = SendPasswordProtectedPanel.this.getModel().getObject().getService(UrlService.class).getPublicUrl(p);
						@SuppressWarnings("unchecked")
						TextAreaField<String> ln = (TextAreaField<String>) form.get("link");
						ln.setValue(link);
						//copy_contanier.setVisible(true);
						target.add(form);
			}
		};
		
		form.add(ln);
		
		
		ta  = new TextAreaField<String>("link", new PropertyModel<String>(this, "link"), 5, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				return true;
				//return status==GENERATED;
			}
			
			@Override
			public boolean isEnabled() {
				return false;
			}
			
		};
		
		form.add(ta);
		addOrReplace(form);


		copy_contanier=new WebMarkupContainer("copy-container");
		copy_contanier.setVisible(true);
		form.add(copy_contanier);

		
		
		
	}
	
	
	
	
	
	
}
