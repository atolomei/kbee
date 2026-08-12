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
			"https://releases.transloadit.com/uppy/v5.2.1/uppy.min.css"
		));

		response.render(JavaScriptHeaderItem.forUrl(
			"https://releases.transloadit.com/uppy/v5.2.1/uppy.min.js"
		));

		response.render(JavaScriptHeaderItem.forUrl(
			"https://releases.transloadit.com/uppy/locales/v5.0.0/es_ES.min.js"
		));

		String componentId = component.getMarkupId();
		String instanceKey = "uppy_" + componentId;
		String script =

			"(function() {" +

			"   window.kbeeUppyInstances =" +
			"       window.kbeeUppyInstances || {};" +

			"   var instanceKey = '" + instanceKey + "';" +
			"   var componentId = '" + componentId + "';" +

			"   var root = document.getElementById(componentId);" +

			"   if (!root) {" +
			"       console.error(" +
			"           'No se encontró el componente Uppy:'," +
			"           componentId" +
			"       );" +
			"       return;" +
			"   }" +

		/*
		 * Destruye solamente la instancia de este componente.
		*/
			"   var previousInstance =" +
			"       window.kbeeUppyInstances[instanceKey];" +

			"   if (previousInstance) {" +
			"       previousInstance.destroy();" +
			"       delete window.kbeeUppyInstances[instanceKey];" +
			"   }" +

		/*
		 * Busca elementos solamente dentro del componente.
		*/
			"   var destinationInput =" +
			"       root.querySelector('.kbee-destination');" +

			"   var dashboardElement =" +
			"       root.querySelector('.kbee-uppy-dashboard');" +

			"   if (!dashboardElement) {" +
			"       console.error(" +
			"           'No se encontró el Dashboard de Uppy:'," +
			"           componentId" +
			"       );" +
			"       return;" +
			"   }" +

			"   var destinationId = destinationInput" +
			"       ? destinationInput.dataset.destinationId" +
			"       : '';" +

			"   var defaultDestinationId = destinationId;" +
			"   var retryInProgress = false;" +
			"   var refreshScheduled = false;" +
			"   var retriedFileId = null;"+

		/*
		 * Configuración principal.
		*/
			"var uppy = new Uppy.Uppy({" +
			"   locale: Uppy.locales.es_ES," +
			"   autoProceed: true," +
			"   restrictions: {" +
			"       maxNumberOfFiles: null," +
			"       maxTotalFileSize: 1024 * 1024 * 1024" +
			"   }," +
			"   meta: {" +
			"       destinationId: destinationId" +
			"   }" +
			"});" +

			"window.kbeeUppyInstances[instanceKey] = uppy;" +

		/*
		 * Dashboard.
		*/
			"uppy.use(Uppy.Dashboard, {" +
			"   inline: true," +
			"   locale: Uppy.locales.es_ES," +
			"   target: dashboardElement," +
			"   width: '100%'," +
			"   height: 360," +
			"   animateOpenClose: false," +
			"   proudlyDisplayPoweredByUppy: false," +
			"   showProgressDetails: true," +
			"   hideUploadButton: true," +
			"   hideAfterFinish: false," +
			"   showSelectedFiles: true," +
			"   disableThumbnailGenerator: true" +
			"});" +

		/*
		 * Tus uploader.
		*/
			"uppy.use(Uppy.Tus, {" +
			"   endpoint: '/api/upload'," +
			"   retryDelays: [0, 1000, 3000, 5000]" +
			"});" +

		/*
		 * Agrega metadatos al archivo.
		*/
			"uppy.on('file-added', function(file) {" +

			"   var currentDestinationId =" +
			"       uppy.getState().meta.destinationId;" +

			"   uppy.setFileMeta(file.id, {" +
			"       destinationId: currentDestinationId," +
			"       relativePath:" +
			"           file.meta.relativePath || file.name," +
			"       originalFileName: file.name" +
			"   });" +
			"});" +

		/*
		 * Conserva la URL devuelta por Tus.
		*/
			"uppy.on('upload-success', function(file, uploadResponse) {" +

			"   if (uploadResponse && uploadResponse.uploadURL) {" +
			"       uppy.setFileState(file.id, {" +
			"           uploadURL: uploadResponse.uploadURL" +
			"       });" +
			"   }" +
			"});" +

		/*
		 * Ejecuta /api/upload/complete para un archivo.
		*/
			"function completeUploadedFile(fileId, processedState) {" +

			"   var file = uppy.getFile(fileId);" +

			"   if (!file) {" +
			"       return Promise.reject(" +
			"           new Error(" +
			"               'No se encontró el archivo ' + fileId" +
			"           )" +
			"       );" +
			"   }" +

			"   var uploadUrl = file.uploadURL;" +

		/*
		 * Fallback por si Uppy guardó la respuesta
		 * dentro de file.response.
		*/
			"   if (!uploadUrl && file.response) {" +
			"       uploadUrl = file.response.uploadURL;" +
			"   }" +

			"   if (!uploadUrl) {" +
			"       return Promise.reject(" +
			"           new Error(" +
			"               'No se encontró la URL Tus para ' +" +
			"               file.name" +
			"           )" +
			"       );" +
			"   }" +

		/*
		 * Elimina el query string si existiera.
		*/
			"   var cleanUploadUrl = uploadUrl.split('?')[0];" +

			"   var uploadId = cleanUploadUrl.substring(" +
			"       cleanUploadUrl.lastIndexOf('/') + 1" +
			"   );" +

		/*
		 * Indica que comenzó el postprocesamiento real
		 * de este archivo.
		*/
			"   uppy.emit('postprocess-progress', file, {" +
			"       mode: 'indeterminate'," +
			"       message:" +
			"           'Procesando en el servidor: ' + file.name" +
			"   });" +

			"   return fetch('/api/upload/complete', {" +
			"       method: 'POST'," +
			"       headers: {" +
			"           'Content-Type': 'application/json'" +
			"       }," +
			"       body: JSON.stringify({" +
			"           uploadId: uploadId," +
			"           meta: file.meta" +
			"       })" +
			"   })" +

			"   .then(function(httpResponse) {" +

			"       if (!httpResponse.ok) {" +
			"           return httpResponse.text()" +
			"               .then(function(text) {" +
			"                   throw new Error(" +
			"                       text ||" +
			"                       'Error HTTP ' +" +
			"                       httpResponse.status" +
			"                   );" +
			"               });" +
			"       }" +

			"       processedState.completed++;" +

			"       var progressValue =" +
			"           processedState.total === 0" +
			"               ? 1" +
			"               : processedState.completed /" +
			"                   processedState.total;" +

			"       uppy.emit('postprocess-progress', file, {" +
			"           mode: 'determinate'," +
			"           message:" +
			"               'Procesados ' +" +
			"               processedState.completed +" +
			"               ' de ' + processedState.total," +
			"           value: progressValue" +
			"       });" +

		/*
		 * El retry terminó correctamente:
		 * se elimina el error anterior del archivo.
		*/
			"       uppy.setFileState(file.id, {" +
			"           error: null" +
			"       });"+

			"       console.log(" +
			"           'Complete OK: ' + file.name +" +
			"           ' (' + processedState.completed +" +
			"           '/' + processedState.total + ')'" +
			"       );" +
			"   })" +

			"   .catch(function(error) {" +

			"       console.error(" +
			"           'Complete ERROR para ' + file.name," +
			"           error" +
			"       );" +

			"       uppy.setFileState(file.id, {" +
			"           error:" +
			"               error.message ||" +
			"               'Error procesando el archivo'" +
			"       });" +

			"       throw error;" +
			"   });" +
			"}" +

		/*
		 * Procesamiento con concurrencia limitada.
		*/
			"function processFilesWithConcurrency(fileIds, concurrency) {" +

			"   var nextIndex = 0;" +
			"   var errors = [];" +

			"   var processedState = {" +
			"       completed: 0," +
			"       total: fileIds.length" +
			"   };" +

			"   function worker() {" +

			"       function processNext() {" +

			"           var currentIndex = nextIndex++;" +

			"           if (currentIndex >= fileIds.length) {" +
			"               return Promise.resolve();" +
			"           }" +

			"           var fileId = fileIds[currentIndex];" +

			"           return completeUploadedFile(" +
			"               fileId," +
			"               processedState" +
			"           )" +

		/*
		 * Guarda el error, pero permite que continúe
		 * el procesamiento de los demás archivos.
		*/
			"           .catch(function(error) {" +

			"               errors.push({" +
			"                   fileId: fileId," +
			"                   error: error" +
			"               });" +
			"           })" +

			"           .then(processNext);" +
			"       }" +

			"       return processNext();" +
			"   }" +

			"   var workers = [];" +

			"   var workerCount = Math.min(" +
			"       concurrency," +
			"       fileIds.length" +
			"   );" +

			"   for (var i = 0; i < workerCount; i++) {" +
			"       workers.push(worker());" +
			"   }" +

			"   return Promise.all(workers)" +

			"       .then(function() {" +

		/*
		 * Finaliza el estado de postprocesamiento
		 * de todos los archivos.
		*/
			"           fileIds.forEach(function(fileId) {" +

			"               var file = uppy.getFile(fileId);" +

			"               if (file) {" +
			"                   uppy.emit('postprocess-complete', file);" +
			"               }" +
			"           });" +

			"           if (errors.length > 0) {" +

			"               if (retryInProgress) {" +
			"                   retryInProgress = false;" +
			"                   retriedFileId = null;" +
			"               }" +

			"               throw new Error(" +
			"                   errors.length +" +
			"                   ' archivo(s) no pudieron procesarse'" +
			"               );" +

			"           }" +

			"           uppy.info(" +
			"               fileIds.length === 1" +
			"                   ? 'El archivo fue procesado correctamente'" +
			"                   : 'Todos los archivos fueron procesados'," +
			"               'success'," +
			"               2000" +
			"           );" +

			"           if (retryInProgress && fileIds.length === 1) {" +

			"               var retriedId = fileIds[0];" +
			"               var retriedFile = uppy.getFile(retriedId);" +

			"               if (retriedFile && !retriedFile.error) {" +
			"                   refreshAfterSingleRetry(retriedId);" +
			"               } else {" +
			"                   retryInProgress = false;" +
			"                   retriedFileId = null;" +
			"               }" +

			"           }" +
			"       });" +
			"}" +

			"uppy.on('upload-retry', function(fileOrId) {" +

			"   var fileId =" +
			"       typeof fileOrId === 'string'" +
			"           ? fileOrId" +
			"           : fileOrId.id;" +

			"   console.log('Retry iniciado:', fileId);" +

			"   retryInProgress = true;" +
			"   retriedFileId = fileId;" +

			"});" +

		/*
		 * Uppy espera la finalización de esta promesa
		 * antes de emitir el evento complete.
		*/
			"uppy.addPostProcessor(function(fileIds) {" +
			"   return processFilesWithConcurrency(fileIds, 4);" +
			"});" +


			"function scheduleFilesRefresh() {" +

			"   if (refreshScheduled) {" +
			"       return;" +
			"   }" +

			"   refreshScheduled = true;" +

			"   setTimeout(function() {" +

			"       uppy.clear();" +

			"       uppy.setMeta({" +
			"           destinationId: defaultDestinationId" +
			"       });" +

			"       refreshtusfiles" + behaviorId +
			"('" + component.getMarkupId() + "');" +

			"       refreshScheduled = false;" +
			"       retryInProgress = false;" +
			"       retriedFileId = null;" +

			"   }, 1200);" +

			"}" +

			"function refreshAfterSingleRetry(fileId) {" +

			"   if (refreshScheduled) {" +
			"       return;" +
			"   }" +

			"   refreshScheduled = true;" +

			"   setTimeout(function() {" +

			"       var file = uppy.getFile(fileId);" +

			"       if (file && !file.error) {" +
			"           uppy.removeFile(fileId);" +
			"       }" +

			"       refreshtusfiles" + behaviorId +
			"('" + component.getMarkupId() + "');" +

			"       refreshScheduled = false;" +
			"       retryInProgress = false;" +
			"       retriedFileId = null;" +

			"   }, 2200);" +

			"}" +

		/*
		 * Se ejecuta después de Tus y del postprocesador.
		*/
			"uppy.on('complete', function(result) {" +

			"   console.log(" +
			"       'Upload y procesamiento completos'," +
			"       result" +
			"   );" +

		/*
		 * El retry individual se resuelve desde
		 * processFilesWithConcurrency().
		*/
			"   if (retryInProgress) {" +
			"       return;" +
			"   }" +

			"   var failedFiles = uppy.getFiles().filter(function(file) {" +
			"       return !!file.error;" +
			"   });" +

			"   if (failedFiles.length > 0) {" +

			"       uppy.info(" +
			"           failedFiles.length +" +
			"               ' archivo(s) finalizaron con error'," +
			"           'error'," +
			"           10000" +
			"       );" +

			"       return;" +
			"   }" +

		/*
		 * Upload inicial completamente exitoso.
		*/
			"   scheduleFilesRefresh();" +

			"});" +

			"uppy.on('error', function(error) {" +

			"   console.error(" +
			"       'Error general de Uppy:'," +
			"       error" +
			"   );" +

			"   uppy.info(" +
			"       error.message ||" +
			"           'Error procesando los archivos'," +
			"       'error'," +
			"       10000" +
			"   );" +

			"});" +

			"})();";

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
}
 