package kbee.web.uploader;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

@SuppressWarnings("serial")
public class UploadBehavior extends Behavior {
	private static final long serialVersionUID = 1L;
	
	private String behaviorId;
	private String script;
	
	public class RefreshBehavior extends AbstractDefaultAjaxBehavior {
		public RefreshBehavior(String id) {
			UploadBehavior.this.behaviorId = id;
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
			StringBuilder script = new StringBuilder();
			script.append("function refreshfiles"+behaviorId+"(component) {\n");
			script.append("top.component=component;\n");
			script.append(getCallbackScript());
			script.append("}\n");
			response.render(JavaScriptHeaderItem.forScript(script.toString(), "refreshfiles"+behaviorId));
		}
		public String getId() {
			return behaviorId;
		}
		@Override
		protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
			super.updateAjaxAttributes(attributes);
			attributes.getDynamicExtraParameters().add("return {component: top.component};");
		}
	}

	@Override
	public void renderHead(Component component, IHeaderResponse response) {
		
		if (!isEnabled()) {	
			return;
		}	

		response.render(JavaScriptHeaderItem.forReference(
			new JavaScriptResourceReference(UploadBehavior.class, "js/plupload236/plupload.full.min.js")));
		
		String resourcesView = getResourcesPanel().getMarkupId();
		
		String script = "var uploader"+resourcesView+" = new plupload.Uploader({"+
				"runtimes : 'html5, html4', "+
				"browse_button : '"+getBrowseButton()+"', "+
				"drop_element: '"+getDropElement()+"', "+
		        "select_folder:1,"+ 
				"url : \"" + getUrl() +"\", " +
				"filters : {"+
				"	max_file_size : '300000mb',"+
				"	mime_types: ["+
				"		{title : \"Image files\", extensions : \"jpg,gif,png,webp\"},"+
				"		{title : \"Pdf files\",   extensions : \"pdf\"},"+
				"		{title : \"Zip files\",   extensions : \"zip\"},"+
				"		{title : \"All files\",   extensions : \"*\"},"+
				"		{title : \"MS Office\",   extensions : \"doc,docx,xls,xlsx,ppt,pptx\"}"+
				"	]"+
				"},"+
				"flash_swf_url : '/plupload/js/Moxie.swf',"+
				"silverlight_xap_url : '/plupload/js/Moxie.xap',"+
				"init: {"+
				"	PostInit: function() {"+
				"		var panel = $('div.moxie-shim');panel.css({top: '', bottom: 15});"+
				"		document.getElementById('"+getBrowseButton()+"').onclick = function() {"+
				"			uploader"+resourcesView+".start();"+
				"			return false;"+
				"		};"+
				"	},"+
				
		        "	BeforeUpload: function (up, file) {"+
		        "source = file.getSource(); "+
		"			up.setOption('multipart_params', {"+
		        "				relativePath: source.relativePath"+
		        "			});"+
		        "	},"+
		
				"	FilesAdded: function(up, files) {"+
				"		top.filesUploaded = 0;"  + 
				"		top.filesAdded = 0; if (top.uploading) return;"+ 
				"		plupload.each(files, function(file) {"+
				"			file.name = file.name.replace('á', 'a'); file.name = file.name.replace('Á', 'A'); "+
				"			file.name = file.name.replace('é', 'e'); file.name = file.name.replace('É', 'E'); "+
				"			file.name = file.name.replace('í', 'i'); file.name = file.name.replace('Í', 'I'); "+
				"			file.name = file.name.replace('ó', 'o'); file.name = file.name.replace('Ó', 'O'); "+
				"			file.name = file.name.replace('ú', 'u'); file.name = file.name.replace('Ú', 'U'); "+
				"			file.name = file.name.replace('ñ', 'n'); file.name = file.name.replace('Ñ', 'N'); "+
				"			file.name = file.name.replace('æ', 'a'); file.name = file.name.replace('#', '-'); "+
				"			top.filesAdded = top.filesAdded+1; "+
				"			document.getElementById('"+resourcesView+"').innerHTML = " +
				"			'<div style=\"padding-left: 10px;\">" +
				"				<span class=\"file-name-upload\" >' + file.name + '</span> <span class=\"file-size-upload\"> (' + plupload.formatSize(file.size) + ')</span>" +
				"				<div class=\"progress\">" +
		 		"					<div id=\"' + file.id + '\" class=\"progress-bar\" role=\"progressbar\" aria-valuenow=\"0\" aria-valuemin=\"0\" aria-valuemax=\"100\">"+
				"						<span class=\"sr-only\" style=\"position: unset;\">' + file.percent + '%' + '</span>"+
		  		"					</div>"+
				"				</div>"+
				"			</div>' + document.getElementById('"+resourcesView+"' ).innerHTML;top.uploading=true;"+
				"				setTimeout(function () { uploader"+resourcesView+".start(); top.uploading=true; }, 500);"+
				"			})"	+ 
				"	},"+
				"	FileUploaded: function(up, files) {"+
				"		var panel = $('div.moxie-shim'); panel.css({top: '', bottom: 15}); top.uploading=false; top.filesUploaded=top.filesUploaded+1; " + 
				"		if (top.filesUploaded>=top.filesAdded) { " + 
				"			setTimeout(function () { try { refreshfiles"+behaviorId+"('"+component.getMarkupId()+"'); } catch(e) { alert(e); } }, 500);" + 
				"		};"+
				"	},"+
				"	UploadProgress: function(up, file) {"+
				"		document.getElementById(file.id).style.width = file.percent+'%';"+
				"		document.getElementById(file.id).innerHTML = '<span class=\"sr-only\" style=\"position: unset;\">' + file.percent + '%</span>';"+
				"	},\r\n"+
				"	Error: function(up, err) {\r\n"+
				"		var m = err.response;"	+
				"		var i = m.indexOf('<html>');" +
				"		if (i>=0) m = m.substring(0,i);" +
				"		alert(m);"	+
				"		document.getElementById('console').innerHTML += \"\\nError #\" + err.code + \": \" + err.message;"+
				"		setTimeout(function () { refreshfiles"+behaviorId+"('"+component.getMarkupId()+"'); }, 2000);"+
				"	}"+
				"}"+
				"});"+
				"uploader"+resourcesView+".init();top.uploading=false;";
		
       this.script = script;


		if (isEnabled()) {
			response.render(OnLoadHeaderItem.forScript(script));
		}
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
	
	protected String getDropElement() {
		return null;
	}
	
	protected String getBrowseButton() {
		return "pickfiles";
	}	
}
 