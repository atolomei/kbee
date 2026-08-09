package com.novamens.wicket.markup.html.actions;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;

@SuppressWarnings("serial")
public abstract class UploadMenuItemPanelV5<T> extends  MenuItemPanelV5<T> {
	private static final long serialVersionUID = 1L;
	
	private boolean head = false;
	private String dropElement;
	
	public UploadMenuItemPanelV5(String id, String dropElement) {
		super(id);
		this.dropElement = dropElement;
	}
	
	public UploadMenuItemPanelV5(String id) {
		super(id);
	}
	
	@Override
	public void onClick() {
	}
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		String linkid = get("lcontainer:item-link").getMarkupId();
		String viewid =  getResourcesView().getMarkupId();
		String uploaderid = getMarkupId();
		
		String script = "if (typeof(uploader"+uploaderid+") != \"undefined\") { uploader"+uploaderid+".destroy(); };";  
		
		script += "var uploader"+linkid+"= new plupload.Uploader({"+
				"runtimes : 'html5',"+
				"browse_button : '"+linkid+"', "+
				(dropElement!=null ? "drop_element: '"+dropElement+"', " : "") + 
				"url : \""+getUploadUrl()+"\", " +
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
				"		document.getElementById('"+linkid+"').onclick = function() {"+
				"			uploader"+linkid+".start();"+
				"			return false;"+
				"		};"+
				"	},"+
				"	FilesAdded: function(up, files) {"+
				"		top.filesUploaded = 0; " + 
				"		top.filesAdded = 0; "+ 
				"		plupload.each(files, function(file) {"+
				"			file.name = file.name.replace('á', 'a'); file.name = file.name.replace('Á', 'A'); "+
				"			file.name = file.name.replace('é', 'e'); file.name = file.name.replace('É', 'E'); "+
				"			file.name = file.name.replace('í', 'i'); file.name = file.name.replace('Í', 'I'); "+
				"			file.name = file.name.replace('ó', 'o'); file.name = file.name.replace('Ó', 'O'); "+
				"			file.name = file.name.replace('ú', 'u'); file.name = file.name.replace('Ú', 'U'); "+
				"			file.name = file.name.replace('ñ', 'n'); file.name = file.name.replace('Ñ', 'N'); "+
				"			file.name = file.name.replace('æ', 'a'); file.name = file.name.replace('#', '-'); "+
				"			top.filesAdded = top.filesAdded+1; "+
				"			document.getElementById('"+viewid+"').innerHTML = " +
				"			'<div>" +
				"			<span class=\"file-name-upload\" >' + file.name + '</span> <span class=\"file-size-upload\"> (' + plupload.formatSize(file.size) + ')</span>" +
				"			<div class=\"progress\">" +
		 		"			<div id=\"' + file.id + '\" class=\"progress-bar\" role=\"progressbar\" aria-valuenow=\"0\" aria-valuemin=\"0\" aria-valuemax=\"100\">"+
				"			<span class=\"sr-only\" style=\"position: unset;\">' + file.percent + '%' + '</span>"+
		  		"			</div>"+
				"			</div>"+
				"			</div>' + document.getElementById('"+viewid+"' ).innerHTML;"+
				"			setTimeout(function () { uploader"+linkid+".start(); }, 500);"+
				"		})"	+ 
				"	},"+
				"	FileUploaded: function(up, files) {"+
				"		top.filesUploaded=top.filesUploaded+1; " + 
				"		if (top.filesUploaded>=top.top.filesAdded) { " + 
				"			setTimeout(function () { "+getRefreshFunction()+"; }, 500);" + 
				"		};"+
				"	},"+
				"	UploadProgress: function(up, file) {"+
				"		document.getElementById(file.id).style.width = file.percent+'%';"+
				"		document.getElementById(file.id).innerHTML = '<span class=\"sr-only\" style=\"position: unset;\">' + file.percent + '%</span>';"+
				"	},"+
				"	Error: function(up, err) {"+
				"		document.getElementById('console').innerHTML += \"\\nError #\" + err.code + \": \" + err.message;"+
				"		setTimeout(function () { "+getRefreshFunction()+"; }, 2000);"+
				"	}"+
				"}"+
				"});"+
				"uploader"+linkid+".init();";

		if (isEnabled() && !head) {
			response.render(OnLoadHeaderItem.forScript(script));
			head = true;
		}
	}	
	
	protected abstract String getUploadUrl();
	
	protected abstract String getRefreshFunction();
	
	protected abstract Component getResourcesView();
	
	@Override
	protected AbstractLink getNewLink(String id) {
		Link<?> link = new Link<Void>(id) {
			public void onClick() {
			}
		};
		link.setOutputMarkupId(true);
		return link;
	}
}
