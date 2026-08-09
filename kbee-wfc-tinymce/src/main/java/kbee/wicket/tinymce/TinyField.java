package kbee.wicket.tinymce;

import java.util.Optional;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;

//import kbee.wicket.tinymce.plugins.AnchorPlugin;
//import kbee.wicket.tinymce4.TinyMceBehavior;
//import kbee.wicket.tinymce4.settings.CodePlugin;
//import kbee.wicket.tinymce4.settings.FullScreenPlugin;
//import kbee.wicket.tinymce4.settings.ImagePlugin;
//import kbee.wicket.tinymce4.settings.LinkPlugin;
//import kbee.wicket.tinymce4.settings.MediaPlugin;
//import kbee.wicket.tinymce4.settings.PastePlugin;
//import kbee.wicket.tinymce4.settings.TablePlugin;
//import kbee.wicket.tinymce4.settings.TinyMCESettings;
//import kbee.wicket.tinymce4.settings.VisualBlocksPlugin;
//import kbee.wicket.tinymce4.settings.WordcountPlugin;

import com.novamens.content.base.Content;
import com.novamens.wicket.markup.html.form.HtmlField;

import kbee.wicket.tinymce8.TinyMCESettings;
import kbee.wicket.tinymce8.TinyMCESettings.Theme;
import kbee.wicket.tinymce8.TinyMceBehavior;

@SuppressWarnings("serial")
public class TinyField extends HtmlField {
	private static final long serialVersionUID = 1L;
	private boolean initialized = false;
	TextArea<String> texteditor;
	
	
	public class SaveBehavior extends AbstractDefaultAjaxBehavior {
		@Override
		protected void respond(AjaxRequestTarget target) {
			Request request = RequestCycle.get().getRequest();
			String text = request.getRequestParameters().getParameterValue("text").toString();
			TinyField.this.setValue(text);
			onClose(target);
			target.appendJavaScript(TinyMceBehavior.getDestroyScript(texteditor.getMarkupId()));;
		}
		@Override
		public void renderHead(final Component component, final IHeaderResponse response) {
			super.renderHead(component, response);
			StringBuilder script = new StringBuilder();
			String id = getMarkupId();
			script.append("function savetext"+TinyField.this.getMarkupId()+"(editor) {\n");
			script.append("tinymce.triggerSave();");
			script.append("var content = tinymce.activeEditor.getContent();");
			script.append("submit();");
			script.append(getCallbackScript());
			script.append("}\n");
			response.render(JavaScriptHeaderItem.forScript(script.toString(), "savetext"+id));
		}
		@Override
		protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
			super.updateAjaxAttributes(attributes);
			attributes.getDynamicExtraParameters().add("var content = tinymce.activeEditor.getContent(); return { text: content };");
		}
	}

	public TinyField(String id) {
		super(id);
		WebMarkupContainer toolbar = new WebMarkupContainer("froala-toolbar");
		WebMarkupContainer linkcontainer = new WebMarkupContainer("link-container") {
			@Override
			public boolean isVisible() {
				return initialized && includeClose() && isEditionEnabled();
			}
		};
		linkcontainer.add(new AjaxLink<Void>("close-link") {
			public void onClick(AjaxRequestTarget target) {
				onClose(target);
			}
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
			}		        
		});
		toolbar.add(linkcontainer);
		toolbar.setOutputMarkupId(true);
		addOrReplace(toolbar);
	}


	public TinyField(String id, IModel<String> model) {
		super(id, model);
		setOutputMarkupId(true);
		WebMarkupContainer toolbar = new WebMarkupContainer("froala-toolbar");
		WebMarkupContainer linkcontainer = new WebMarkupContainer("link-container") {
			@Override
			public boolean isVisible() {
				return initialized && includeClose() && isEditionEnabled();
			}
		};
		linkcontainer.add(new AjaxLink<Void>("close-link") {
			public void onClick(AjaxRequestTarget target) {
			}
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				AjaxCallListener myAjaxCallListener = new AjaxCallListener() {
					@Override 
					public CharSequence getBeforeHandler(Component component) {
						return getCloseScript();
					}
				};
				attributes.getAjaxCallListeners().add(myAjaxCallListener);
			}		        
		});
		toolbar.add(linkcontainer);
		toolbar.setOutputMarkupId(true);
		addOrReplace(toolbar);
		toolbar.add(new SaveBehavior());
	}
	
	public TinyField(String id, IModel<String> model, int rows, int cols) {
		super(id, model);
		setRows(rows);
		setRows(cols);
		setOutputMarkupId(true);
	}	

	@Override
	public String getFocusScript() {
		return null;
	}
	
	@Override
	public String getCloseScript() {
		return "savetext"+getMarkupId()+"(null);";	
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (!isEditionEnabled()) {
				Optional<AjaxRequestTarget> target =
					    RequestCycle.get().find(AjaxRequestTarget.class);
					if (target.isPresent()) {
						target.get().appendJavaScript(getCloseScript());;
					}
			}
	}
	
	public String getMode() {
		return null;
	}
	
	public void updateAjaxCloseAttributes(AjaxRequestAttributes attributes) {
		AjaxCallListener myAjaxCallListener = new AjaxCallListener() {
			@Override 
			public CharSequence getBeforeHandler(Component component) {
				return getCloseScript();
			}
		};
		attributes.getAjaxCallListeners().add(myAjaxCallListener);		
	}
	
	public void addText(AjaxRequestTarget target, String text) {
	}
	
	protected String getBaseUrl() {
		return null;
	}
	
	protected Content getContent() {
		return null;
	}
		
	protected TextArea<?> getTextField() {
		
		texteditor = new TextArea<String>("input", new PropertyModel<String>(this, "value")) {
			@Override
			public boolean isVisible() {
				return isEditionEnabled();
			}
			@Override
			public void updateModel() {
  				TinyField.this.updateModel();
				super.updateModel();

			}
		};

		
		texteditor.add(new AjaxFormComponentUpdatingBehavior("input") {
			protected void onUpdate(AjaxRequestTarget target) {
				TinyField.this.onUpdate(target);
 				if (TinyField.this.hasFeedback()) {
 					TinyField.this.validate();
					target.add(TinyField.this);
				}
			}
			protected void onError(AjaxRequestTarget target, RuntimeException e) {
				target.add(TinyField.this);
			}
		});
			
		TinyMCESettings settings = "Tiny".equals(getMode())
				? new TinyMCESettings(Theme.simple)
				: new TinyMCESettings(Theme.advanced);
		texteditor.add(new TinyMceBehavior(settings) {
		    @Override
		    protected void respond(AjaxRequestTarget target) {
				initialized=true;
				target.add(TinyField.this.get("froala-toolbar"));
		    }
		    protected String onBlurScript() {
		    	return getCloseScript();
		    }
		});
		texteditor.setOutputMarkupPlaceholderTag(true);
		texteditor.setOutputMarkupId(true);
		texteditor.add(new AttributeModifier("rows","35"));
		texteditor.add(new AttributeModifier("cols","40"));
		
		return texteditor;
	}
	
	public boolean isEditionEnabled() {
		return getEditor().isEditionEnabled();
	}
	
	public void onClose(AjaxRequestTarget target) {
		Request request = RequestCycle.get().getRequest();
		String text = request.getRequestParameters().getParameterValue("text").toString("");
		setValue(text);
		//onUpdate(target);
		target.add(this);
		initialized=false;
	}
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		if (getContent()!=null) {
			response.render(JavaScriptHeaderItem.forScript(getBrowserJS(), "tinymcebrowser"));
		}
	}
	
	protected boolean includeClose() {
		return true;
	}
	
	protected String getBrowserJS() {
		StringBuffer script = new StringBuffer();
		script.append("function kbeeResourceBrowser (callback, value, meta) {");
		script.append("	var cmsURL = \"/browser2\";");
		script.append("	cmsURL = cmsURL + \"?type=\" + meta.filetype;");
		script.append("	cmsURL = cmsURL + \"&content=" + String.valueOf(getContent().getId())+"\";");
		script.append("	top.cb = callback;");
		script.append("	top.tiny = tinyMCE.activeEditor;");
		script.append("	tinyMCE.activeEditor.windowManager.openUrl({");
		script.append("		url : cmsURL,");
		script.append("		title : \"Resources\",");
		script.append("		width : 1200,"); 
		script.append("		height : 700,");
		script.append("	}, ");
		script.append("	{");
		script.append("		oninsert: function (url, objVals) {");
		script.append("			callback(url, objVals);");
		script.append("		}");
		script.append("	});");
		script.append("}\r\n");
		
		String s = "function onChangeText(callback) {\r\n"+
				"		editor = tinyMCE.activeEditor; const headings = editor.getBody().querySelectorAll('h1, h2, h3, h4, h5, h6');\r\n"+
				"		for (let i = 0; i < headings.length; i++) {\r\n"+
				"			if (!headings[i].id) {\r\n"+
				"				headings[i].id = Math.random().toString(32).substr(2, 5);\r\n"+
				"			}\r\n"+
				"   	 }\r\n"+
				"   	 onUpdateText(editor);\r\n"+
				"}\r\n";
		
		script.append(s);
		
		s = 		"function setCursor(id) {\r\n"+
				"	let nodeStrong = tinyMCE.activeEditor.dom.select(id);"+
		"tinyMCE.activeEditor.selection.setCursorLocation(nodeStrong[0].firstChild, 2);"+
        "tinyMCE.activeEditor.focus();"+
				"}\r\n";
		
		script.append(s);


		
		return script.toString();
	}
}
