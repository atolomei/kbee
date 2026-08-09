package kbee.web.uploader;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;

@SuppressWarnings("serial")
public class UploadTusBehavior extends Behavior {
	private static final long serialVersionUID = 1L;
	
	private String behaviorId;
	private String script;
	
	public class RefreshBehavior extends AbstractDefaultAjaxBehavior {
		public RefreshBehavior(String id) {
			UploadTusBehavior.this.behaviorId = id;
		}
		@Override
		protected void respond(AjaxRequestTarget target) {
			Request request = RequestCycle.get().getRequest();
			String component = request.getRequestParameters().getParameterValue("component").toString("");
			onUpload(target, component);
		}
		@Override
		public void renderHead(final Component component, final IHeaderResponse response) {
		    super.renderHead(component, response);

		    String functionName = "refreshtusfiles" + behaviorId;
		    String callbackUrl = getCallbackUrl().toString();

		    String script =
		        "window." + functionName + " = function(component) {\n" +
		        "   console.log('Calling " + functionName + "', component);\n" +
		        "   Wicket.Ajax.get({\n" +
		        "       u: '" + callbackUrl + "',\n" +
		        "       ep: { component: component }\n" +
		        "   });\n" +
		        "};\n";

		    response.render(JavaScriptHeaderItem.forScript(
		        script,
		        functionName + "-" + component.getMarkupId()
		    ));
		}
		public String getId() {
			return behaviorId;
		}
		@Override
		protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
			super.updateAjaxAttributes(attributes);
		}
	}
	
	@Override
	public void renderHead(Component component, IHeaderResponse response) {

	    if (!isEnabled()) {
	        return;
	    }

	    response.render(CssHeaderItem.forUrl(
	    	    "https://releases.transloadit.com/uppy/v5.2.1/uppy.min.css"));

	    	response.render(JavaScriptHeaderItem.forUrl(
	    	    "https://releases.transloadit.com/uppy/v5.2.1/uppy.min.js"));

	    	response.render(JavaScriptHeaderItem.forUrl(
	    	    "https://releases.transloadit.com/uppy/locales/v5.0.0/es_ES.min.js"));
	    	
	    	String script =
		    	    "if (window.kbeeUppy) {" +
		    	    "   window.kbeeUppy.destroy();" +
		    	    "   window.kbeeUppy = null;" +
		    	    "}" +
		    	    
		    	    "var completedFiles = 0;" +
		    	    "var totalFiles = 0;" + 
		    	    "var destinationInput = document.getElementById('inputId');" +
		    	    "var destinationId = destinationInput ? destinationInput.dataset.destinationId : '';" +
		    	    "var defaultDestinationId = destinationId;" +

		    	    "var uppy = new Uppy.Uppy({" +
		    	    "locale: Uppy.locales.es_ES,"+
		    	    "   autoProceed: true," +
		    	    "   restrictions: {" +
		    	    "       maxNumberOfFiles: null," +
		    	    "       maxTotalFileSize: 1024 * 1024 * 1024" +
		    	    "   }," +
		    	    "   meta: {" +
		    	    "       destinationId: destinationId" +
		    	    "   }" +
		    	    "});" +

		    	    "window.kbeeUppy = uppy;" +

					"uppy.use(Uppy.Dashboard, {" +
					"   inline: true," +
					"   locale: Uppy.locales.es_ES," +
					"   target: '#uppy-dashboard'," +
					"   width: '100%'," +
					"   height: 360," +
					"   animateOpenClose: false," +
					"   proudlyDisplayPoweredByUppy: false," +
					"   showProgressDetails: true," +
					"   hideUploadButton: true," +
					"   showSelectedFiles: true," +
					"   disableThumbnailGenerator: true," +
//					"   note: 'Arrastre archivos o carpetas'" +
					"});" +
		    	    "uppy.use(Uppy.Tus, {" +
		    	    "   endpoint: '/api/upload'," +
		    	    "   retryDelays: [0,1000,3000,5000]" +
		    	    "});" +

		    	    "uppy.on('file-added', function(file) {" +
		    	    "   totalFiles++;" +
		    	    "   var currentDestinationId = uppy.getState().meta.destinationId;" +
		    	    "   uppy.setFileMeta(file.id, {" +
		    	    "       destinationId: currentDestinationId," +
//		    	    "       destinationId: destinationId," +
		    	    "       relativePath: file.meta.relativePath || file.name," +
		    	    "       originalFileName: file.name" +
		    	    "   });" +
		    	    "});" +

					"uppy.on('upload-success', function(file, response) {" +
					"   var uploadUrl = response.uploadURL;" +
					"   var uploadId = uploadUrl.substring(uploadUrl.lastIndexOf('/') + 1);" +
					
					"   fetch('/api/upload/complete', {" +
					"       method: 'POST'," +
					"       headers: {'Content-Type':'application/json'}," +
					"       body: JSON.stringify({" +
					"           uploadId: uploadId," +
					"           meta: file.meta" +
					"       })" +
					"   }).then(function(r) {" +
					"       if (!r.ok) {" +
					"           return r.text().then(function(text) {" +
					"               throw new Error(text || 'Complete failed');" +
					"           });" +
					"       }" +
					"       completedFiles++;" +
					"       console.log('Complete OK: ' + file.name);" +
					"       if (completedFiles === totalFiles) {" +
					"           setTimeout(function () {" +
					"   	    	uppy.clear();" +
					"       		uppy.setMeta({destinationId: defaultDestinationId});" +
					"               refreshtusfiles" + behaviorId + "('" + component.getMarkupId() + "');" +
					"           }, 2000);" +
					"       }" +
					"   }).catch(function(e) {" +
					"       console.error('Complete ERROR:', e);" +
					"       uppy.setFileState(file.id, {" +
					"           error: e.message || 'Error completing upload'" +
					"       });" +
					"       uppy.info('Error processing ' + file.name + ': ' + e.message, 'error', 8000);" +
					
					"           setTimeout(function () {" +
					"   	    	uppy.clear();" +
					"       		uppy.setMeta({destinationId: defaultDestinationId});" +
					"           }, 2000);" +
					
					
					"   });" +
					"});" +

		    	    "uppy.on('complete', function(result) {" +
		    	    "   console.log('Upload completo', result);" +
		    	    "});";

		    response.render(OnDomReadyHeaderItem.forScript(script));
	}
	
	public String getScript() {
		return script;
	}
	
	public boolean isEnabled() {
		return false; 
	}	
	
	public Component getResourcesPanel() {
		return null;
	}
	
	protected void onUpload(AjaxRequestTarget target, String component) {
		
	}
	
	protected void setBehaviorId(String id) {
		this.behaviorId = id;
	}
	
	protected String getUrl() {
		return null;
	}
	
//	protected String getDropElement() {
//		return null;
//	}
//	
//	protected String getBrowseButton() {
//		return "pickfiles";
//	}	
}
 