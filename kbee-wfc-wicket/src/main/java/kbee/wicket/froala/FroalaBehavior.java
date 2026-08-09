package kbee.wicket.froala;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import kbee.util.PropertiesFactory;

public class FroalaBehavior extends Behavior {
	private static final long serialVersionUID = 3L;
	
	static final String key = PropertiesFactory.getInstance("kbee").getProperties().getProperty("froala.key");
	
	//private boolean rendered = false;
	
	public class InitListener extends AbstractDefaultAjaxBehavior {
		private static final long serialVersionUID = 1L;
		public InitListener() {
		}
		@Override
		protected void respond(AjaxRequestTarget target) {
			onInit(target);
		}
		@Override
		public void renderHead(final Component component, final IHeaderResponse response) {
			super.renderHead(component, response);
			StringBuilder script = new StringBuilder();
			script.append("function onFroalaInit"+getComponent().getMarkupId()+"() {\n");
			script.append(getCallbackScript());
			script.append("}\n");
			response.render(JavaScriptHeaderItem.forScript(script.toString(), "onFroalaInit"+getComponent().getMarkupId()));
		}
	}
	
	public class UpdateListener extends AbstractDefaultAjaxBehavior {
		private static final long serialVersionUID = 1L;
		public UpdateListener() {
		}
		@Override
		protected void respond(AjaxRequestTarget target) {
			Request request = RequestCycle.get().getRequest();
			String text = request.getRequestParameters().getParameterValue("body").toString("");
			onUpdate(target, text);
		}
		@Override
		public void renderHead(final Component component, final IHeaderResponse response) {
			super.renderHead(component, response);
		}
	}

	private Component component;

	public FroalaBehavior()	{
	}
	
	public boolean isEditionEnabled() {
		return true;
	}

	@Override
	public void renderHead(Component c, IHeaderResponse response) {
		super.renderHead(c, response);
		
		 //if (rendered) return;
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(FroalaBehavior.class, "node_modules/froala-editor/js/froala_editor.pkgd.min.js")));
//		
//		response.render(JavaScriptHeaderItem.forReference(
//				new JavaScriptResourceReference(FroalaBehavior.class, "node_modules/froala-editor/js/plugins/image_manager.min.js")));
		
		response.render(CssHeaderItem.forReference(
				new CssResourceReference(FroalaBehavior.class, "node_modules/froala-editor/css/froala_editor.pkgd.min.css")));

		response.render(wrapScript(getSetupScript()));
		
		//rendered = true;
	}
	
	protected HeaderItem wrapScript(String settingScript){
		return OnDomReadyHeaderItem.forScript(settingScript);
	}

	protected String getSetupScript()	{
		StringBuffer script = new StringBuffer();
		script.append("top.editor"+getComponent().getMarkupId()+" = new FroalaEditor('#"+getComponent().getMarkupId()+"', {"+
			(key!=null ? ("key: '"+key.trim()+"',") : "") +
		    // Set custom buttons.
		    "toolbarButtons: {\n"+
		    	"'moreText': {"+
		    	"  'buttons': ['bold', 'italic', 'underline', 'strikeThrough', 'subscript', 'superscript', 'fontFamily', 'fontSize', 'textColor', 'backgroundColor', 'inlineClass', 'inlineStyle', 'clearFormatting']"+
		    	"},\n"+
		    	"'moreParagraph': {"+
		    	"  'buttons': ['alignLeft', 'alignCenter', 'formatOLSimple', 'alignRight', 'alignJustify', 'formatOL', 'formatUL', 'paragraphFormat', 'paragraphStyle', 'lineHeight', 'outdent', 'indent', 'quote']"+
		    	"},\n"+
		    	"'moreRich': {"+
//		    	"  'buttons': ['insertLink', 'insertImage', 'insertVideo', 'insertTable', 'emoticons', 'fontAwesome', 'specialCharacters', 'embedly', 'insertFile', 'insertHR']"+
		    	"  'buttons': ['insertLink', 'insertImage', 'insertVideo', 'insertTable', 'emoticons', 'fontAwesome', 'specialCharacters', 'embedly', 'insertHR']"+
		    	"},\n"+
		    	"'moreMisc': {"+
		    	"  'buttons': ['undo', 'redo', 'fullscreen', 'print', 'getPDF', 'spellChecker', 'selectAll', 'html', 'help']"+	
		    	"}\n"+
		    "},\n"+
		    "htmlAllowedEmptyTags: ['textarea', 'a', 'iframe', 'object', 'video', 'style', '.fa', '.fr-emoticon', '.fr-inner', 'path', 'line', 'hr', 'i'],\n"+
 			"imageUpload: false,\n" +
 			(getImagesUrl()!=null ? "imageManagerLoadURL: '"+getImagesUrl()+"'," : "")+ 
 			
		    (!isEditionEnabled() ? "toolbarInline: true, charCounterCount: false,\n" : "")+
		    "toolbarButtonsXS: [['undo', 'redo'], ['bold', 'italic', 'underline']],\n"+
		    "initOnClick: "+ (initOnClick() ? "true,\n" : "false,\n")+
		    (getPlaceHolderText()!=null ? "placeholderText: '"+getPlaceHolderText()+"',\n" : "") +
  			"events: {"+
		      " 'initialized': function () {\n"+
//		      "		editor.events.focus();" + 
		      "		onFroalaInit"+getComponent().getMarkupId()+"();"+
		      (!isEditionEnabled() ? "editor"+getComponent().getMarkupId()+".edit.off();\n" : "")+
		      "  }\n"+
		      "}\n"+
		"});");
		
		return script.toString();
	}

	@Override
	public void bind(Component component) {
		super.bind(component);
		if (isMarkupIdRequired())
			component.setOutputMarkupId(true);
		component.add(new InitListener());
		this.component = component;
	}
	
	protected void onInit(AjaxRequestTarget target) {
	}
	
	protected void onUpdate(AjaxRequestTarget target, String text) {
	}
	
	protected String getImagesUrl() {
		return null;
	}
	
	protected boolean initOnClick() {
		return true;
	}
	
	protected String getPlaceHolderText() {
		return null;
	}

	protected boolean isMarkupIdRequired() {
		return true;
	}

	protected Component getComponent() {
		return component;
	}
}
