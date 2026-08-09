package kbee.wicket.tinymce8;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.resource.PackageResourceReference;

import kbee.wicket.tinymce8.TinyMCESettings.Theme;

public class TinyMceBehavior extends AbstractDefaultAjaxBehavior {
	private static final long serialVersionUID = 1L;

	private boolean rendered = false;

	TinyMCESettings settings;
	
	public TinyMceBehavior(TinyMCESettings settings) {
		this.settings = settings;
	}
	
    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        super.renderHead(component, response);

        component.setOutputMarkupId(true); // 🔥 clave

        response.render(JavaScriptHeaderItem.forReference(
            new PackageResourceReference(TinyMceBehavior.class, "tinymce/tinymce.min.js")
        ));
        response.render(JavaScriptHeaderItem.forReference(
        	    new PackageResourceReference(TinyMceBehavior.class, "tinymce/langs/es.js")
        	));
        
        
        
        String script ="";
        if (rendered) {
        	String tryToRemoveJS = "try{tinyMCE.remove(tinyMCE.get('%s'));}catch(e){}\n";
			script+= String.format(tryToRemoveJS, getComponent().getMarkupId());
        }
        String callback = getCallbackScript().toString();
        
        Theme theme = settings.getTheme();
        
        script += String.format(
        	    "tinymce.init({" +
        	    "   selector: '#%s'," +
        	    "   license_key: 'gpl'," +
        	    "   language: 'es'," +
        	    "   height: 300," +
        	    "   branding: false," +
        	    "   promotion: false," +
        	    "   menubar: %s," +
        	    "   toolbar: 'undo redo | bold italic | alignleft aligncenter alignright | code'," +
        	    "   setup: function (editor) {" +

        	    "       editor.on('init', function () {" +
        	    "           editor.focus();" +
        	    "           %s" +
        	    "       });" +

        	    "       editor.on('change keyup', function () {" +
        	    "           editor.save();" +
        	    "       });" +

//        	    "       editor.on('blur', function () {" +
//        	    "           editor.save();" +
//        	    "           %s" +
//        	    "       });" +

        	    "   }" +
        	    "});",

        	    component.getMarkupId(),
        	    Theme.advanced.equals(theme) ? "true" : "false",
        	    callback
        	   // onBlurScript()
        	);
        rendered=true;
        response.render(OnDomReadyHeaderItem.forScript(script));
    }
    
    public static String getDestroyScript(String id) {
    	return "if (tinymce.get('"+id+"')) {  tinymce.get('"+id+"').remove();}";
    }
    
    protected String onBlurScript() {
    	return null;
    }

    @Override
    protected void respond(AjaxRequestTarget target) {
        System.out.println("TinyMCE init event received ✔");
    }
}