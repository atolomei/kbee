 package kbee.web.console.action;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import com.novamens.wicket.markup.html.actions.Action;
import com.novamens.wicket.markup.html.actions.ActionPanel;

// extends ActionPanel<T> 

public abstract class UploadAction<T> extends ActionPanel<T> implements Action {
	private static final long serialVersionUID = 1L;
	public UploadAction(String id, IModel<String> label) {
		super(id, label);
		add(new Label("action-label", getLabelModel()));
	}
	
	public UploadAction(String id) {
		super(id, new Model<String>("Upload"));
	}

	protected void execute() {

	}
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(UploadAction.class,"js/plupload/plupload.full.min.js")));
		
		String script = "var uploader = new plupload.Uploader({"+
				"runtimes : 'html5, html4',"+
				"browse_button : 'pickfiles', "+
				"drop_element: 'consoledata', "+
				"url : \"/contentupload\", " +
				"filters : {"+
				"	max_file_size : '30000mb',"+
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
				"		document.getElementById('console').innerHTML = '';"+
				"		$('#consoledata').css('position','inherit');"+
				"		document.getElementById('pickfiles').onclick = function() {"+
				"			uploader.start();"+
				"			return false;"+
				"		};"+
				"	},"+
				"	FilesAdded: function(up, files) {"+
				"		top.filesUploaded = 0; " + 
				"		top.filesAdded = 0; "+ 
				"		plupload.each(files, function(file) {"+
				"			file.name = file.name.replace('á', 'a'); "+
				"			file.name = file.name.replace('Á', 'A'); "+
				"			file.name = file.name.replace('é', 'e'); "+
				"			file.name = file.name.replace('É', 'E'); "+
				"			file.name = file.name.replace('í', 'i'); "+
				"			file.name = file.name.replace('Í', 'I'); "+
				"			file.name = file.name.replace('ó', 'o'); "+
				"			file.name = file.name.replace('Ó', 'O'); "+
				"			file.name = file.name.replace('ú', 'u'); "+
				"			file.name = file.name.replace('Ú', 'U'); "+
				"			file.name = file.name.replace('Ñ', 'n'); "+
				"			file.name = file.name.replace('ñ', 'N'); "+
				"			file.name = file.name.replace('#', '-'); "+
				"			top.filesAdded = top.top.filesAdded+1; "+
			    "			document.getElementById('consoledata').innerHTML = '<div class=\"fileprogress\"><div class=\"link\"><span>' + file.name + '</span></div><div id=\"' + file.id + '\" class=\"info\">' + plupload.formatSize(file.size) + '<b></b></div></div>'+document.getElementById('consoledata').innerHTML;"+
				"			setTimeout(function () { uploader.start(); }, 500);"+
				"		})"	+ 
				"	},"+
				"	FileUploaded: function(up, files) {"+
				"		top.filesUploaded=top.filesUploaded+1; " + 
				"		if (top.filesUploaded>=top.top.filesAdded) { " + 
				"			setTimeout(function () { refreshconsole();}, 500);" + 
				"		};"+		
				"	},"+
				"	UploadProgress: function(up, file) {"+
				"		document.getElementById(file.id).getElementsByTagName('b')[0].innerHTML = '&nbsp;<span class=\"percent\">' + file.percent + \"%</span>\";"+
				"	},"+
				"	Error: function(up, err) {"+
				"		document.getElementById('console').innerHTML += \"\\nError #\" + err.code + \": \" + err.message"+
				"	}"+
				"}"+
				"});"+
				"uploader.init();";

		if (isEnabled()) {
			response.render(OnLoadHeaderItem.forScript(script));
		}
	}
}