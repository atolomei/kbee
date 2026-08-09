package kbee.web.form;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;

import kbee.wicket.tinymce.settings.LinkPlugin;
import kbee.wicket.tinymce.settings.PastePlugin;
import kbee.wicket.tinymce.settings.TinyMCESettings;

import com.novamens.wicket.markup.html.form.TextAreaField;

@SuppressWarnings("serial")
public class TextEditorField extends TextAreaField<String> {
	private static final long serialVersionUID = 1L;
	
	static private String DEFAULT_FORMATS = "style_formats : ["
			+ "{title : 'Heading 2'		, block : 'h2', 			   classes : 'title'},"
			+ "{title : 'Highlight inline'		, inline : 'span',     classes : 'highlight-inline'},"
			+ "{title : 'Parameter'				, inline : 'span',     classes : 'parameter'},"
			+ "{title : 'Meta variable'			, inline : 'span',     classes : 'metavariable'},"
			+ "{title : 'Table styles'},"
			+ "{title : 'Table row 1', selector : 'tr', classes : 'tablerow1'}]";
	
	private String style_formats = DEFAULT_FORMATS;
	
	public class OnBlurListner extends AbstractDefaultAjaxBehavior {
		@Override
		protected void respond(AjaxRequestTarget target) {
			Request request = RequestCycle.get().getRequest();
			String text = request.getRequestParameters().getParameterValue("text").toString();
			TextEditorField.this.setValue(text);
			TextEditorField.this.onUpdate(target);
		}
		@Override
		protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
			super.updateAjaxAttributes(attributes);
			attributes.getDynamicExtraParameters().add("var content = tinymce.activeEditor.getContent(); return { text: content };");
		}
	}

	public TextEditorField(String id, IModel<String> model) {
		super(id, model);
		setOutputMarkupId(true);
	}
	
	public void addText(AjaxRequestTarget target, String text) {
		target.appendJavaScript("tinymce.activeEditor.insertContent('"+ text+"');");
	}

	protected String getStyleFormats() {
		return this.style_formats;
	}
	
	protected TinyMCESettings.Theme getTheme() {
		return TinyMCESettings.Theme.modern;
	}

	protected void setStyleFormats(String s) {
		style_formats =s;
	}
	
	protected TextArea<?> getTextField() {
		
		TinyMCESettings settings = new TinyMCESettings(getTheme());
		
		settings.register(new LinkPlugin());
		settings.register(new PastePlugin());
		
		settings.addCustomSetting(getStyleFormats());
		
		settings.addCustomSetting("content_css : \"/css/tiny-simple-html.css\"");
		
//		OnBlurListner listener = new OnBlurListner();
//		add(listener);
		
		//settings.addCustomSetting("init_instance_callback: function (editor) { editor.on('blur', function (e) { "+listener.getCallbackScript()+" }); }");
		
		TextArea<String> texteditor = new TextArea<String>("input", new PropertyModel<String>(this, "value")) {
			public boolean isVisible() {
				return isEditionEnabled();
			}
		};
		

		
		//texteditor.add(new TinyMceBehavior(settings));
		
		texteditor.setOutputMarkupPlaceholderTag(true);
		texteditor.setOutputMarkupId(true);
		texteditor.add(new AttributeModifier("rows","8"));
		texteditor.add(new AttributeModifier("cols","40"));
		
		return texteditor;
	}
	
	
	public boolean isEditionEnabled() {
		return getEditor().isEditionEnabled();
	}
}
