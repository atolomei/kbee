package kbee.web.content.panel;


import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptContentHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.base.Content;
import com.novamens.content.service.UrlService;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.TextAreaField;

import com.novamens.wicket.markup.html.form.Form.Disposition;

import kbee.web.panel.AlertPanel;

@SuppressWarnings("serial")
public class PublicLinkPanel<T extends Content> extends ModelPanel<T> {

	private static final long serialVersionUID = 1L;
	
	private String link = "http://...";
	
	public PublicLinkPanel(String id) {
		this(id, null);
	}
	
	public PublicLinkPanel(String id, IModel<T> model) {
		super(id, model);
		setLink(model.getObject().getService(UrlService.class).getPublicUrl());
	}

	
	@Override
	public void onInitialize() {
		super.onInitialize();

		AlertPanel<Void> pa=new AlertPanel<Void>("alert-text",AlertPanel.INFO, null, getLabel("share"), getLabel("alert-text"));
		pa.setIcon("fa-duotone fa-link");
		addOrReplace(pa);
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		form.add(new TextAreaField<String>("link", new PropertyModel<String>(this, "link"), 10, 0) {
			public boolean isEnabled() {
				return false;
			}
		}); 
		add(form);
	}
	
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		Component textarea = ((TextAreaField<?>)get("form:link")).getInput();
		StringBuffer script = new StringBuffer();
		script.append("function copyToClipboard() {");
		script.append("		var copyTextArea = document.getElementById('"+textarea.getMarkupId()+"');");
		script.append("		copyTextArea.focus();");
		script.append("		copyTextArea.select();");
		script.append("		try {");
		script.append("			navigator.clipboard.writeText(copyTextArea.value);");
		script.append("			document.getElementById('copy-feedback').innerHTML = '<span>"+getLabelString("copy-feedback")+"</span>';");
		script.append("		} catch (err) {");
		script.append("			console.log('Oops, unable to copy');");
		script.append("		}");
		script.append("}");
		response.render(new JavaScriptContentHeaderItem(script.toString(), "copy"));
	}
	
	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}


}