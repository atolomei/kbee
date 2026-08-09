package kbee.web.resource;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.dao.ContentDao;
import com.novamens.dom.Proxy;
import com.novamens.kbee.content.base.KbeeResourceTag;
import com.novamens.kbee.wicket.markup.html.console.panel.ViewMode;
import com.novamens.kbee.wicket.markup.html.page.AbstractKbeeWebPage;
import com.novamens.kbee.wicket.model.ModelPanel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.DateField;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.FileUtil;

import kbee.web.uploader.UploadBehavior;


@SuppressWarnings("serial")
public class BrowserPanel<T extends Content> extends ModelPanel<T>{
	private static final long serialVersionUID = 1L;
	
	private String resourceType;
													
	protected static final ResourceReference BS_CSS = new CssResourceReference(Form.class, Form.BOOTSTRAP);
	
	private static final ResourceReference CSS_KBEE_BOOTSTRAP			 = new CssResourceReference(AbstractKbeeWebPage.class, "kbeebootstrap.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_0_360      = new CssResourceReference(AbstractKbeeWebPage.class, "kb-0-360.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_361_800    = new CssResourceReference(AbstractKbeeWebPage.class, "kb-361-800.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_801_1200   = new CssResourceReference(AbstractKbeeWebPage.class, "kb-801-1200.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1201_1600  = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1201-1600.css");
	private static final ResourceReference CSS_KBEE_BOOTSTRAP_1601  	 = new CssResourceReference(AbstractKbeeWebPage.class, "kb-1601.css");

	protected static final ResourceReference BS_JS = new JavaScriptResourceReference(DateField.class, com.novamens.wicket.markup.html.form.Form.BOOTSTRAP_JS);
	
	public class RefreshBehavior extends AbstractDefaultAjaxBehavior {
		@Override
		protected void respond(AjaxRequestTarget target) {
			target.add(BrowserPanel.this.get("resources-view"));
		}
		@Override
		public void renderHead(final Component component, final IHeaderResponse response) {
			super.renderHead(component, response);
			StringBuilder script = new StringBuilder();
			script.append("function refreshfiles"+BrowserPanel.this.getMarkupId()+"() {\n");
			script.append(getCallbackScript());
			script.append("}\n");
			response.render(JavaScriptHeaderItem.forScript(script.toString(), "refreshfiles"+BrowserPanel.this.getMarkupId()));
		}
	}
	
	public BrowserPanel(String id, String resourceType, IModel<T> model) {
		super(id, model);
		
		this.resourceType = resourceType;
		
		WebMarkupContainer view = new WebMarkupContainer("resources-view");
		
		view.setOutputMarkupId(true);
		
		view.add(new ListView<IModel<Resource>>("resource", new PropertyModel<List<IModel<Resource>>>(this, "resources")) {
			public void populateItem(final ListItem<IModel<Resource>> item) {
				item.add(new ResourceViewPanel<T>("resource-view", item.getModelObject(), model) {
					public ViewMode getViewMode() {
						return ViewMode.THUMBNAIL_LARGE;
					}
				});
				Component image_link = item.get("resource-view:image-container:image-link");
				image_link.add(new AttributeModifier("onclick", "returnvalue('"+item.getModelObject().getObject().getName()+"');"));
			}
		});
		add(view);
		add(new RefreshBehavior());
	}

	
	
	public List<IModel<Resource>> getResources() {
		List<IModel<Resource>> models = new ArrayList<IModel<Resource>>();
		List<Resource> resources = getTag()!=null
			? ((ResourceContainer)getModelObject()).getResources(getTag().getName())
			: ((ResourceContainer)getModelObject()).getResources();
		for (Resource resource : resources) {
			if ("image".equals(resourceType)) {
				if (FileUtil.isImage(resource.getName()))
					models.add(new ObjectModel<Resource>(resource));
			}
			else {
				if ("media".equals(resourceType)) {
					if (FileUtil.isVideo(resource.getName()) || resource.getName().endsWith("mp3"))
						models.add(new ObjectModel<Resource>(resource));
				}
				else {
					models.add(new ObjectModel<Resource>(resource));
				}
			}
		}
		return models;
	}
	
	public ResourceTag getTag() {
		for (ResourceTag tag : getModelObject().getContentTemplate().getResourceTags()) {
			if ("image".equals(tag.getAlias().toLowerCase())) {
				return tag;
			}
		}
		return null;
	}
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings().getJQueryReference()));  
		response.render(CssHeaderItem.forReference(BS_CSS));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_0_360));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_361_800 ));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_801_1200));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1201_1600));
		response.render(CssHeaderItem.forReference(CSS_KBEE_BOOTSTRAP_1601));
		response.render(JavaScriptHeaderItem.forReference(BS_JS));
		//response.render(JavaScriptHeaderItem.forReference(TINY_JS));
		
		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(UploadBehavior.class, "js/plupload236/plupload.full.min.js")));
		
		String tagid = getTag()!=null?String.valueOf(((KbeeResourceTag)getTag()).getId()):null;

		//response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(UploadAction.class,"js/plupload/plupload.full.min.js")));
		
		String resourcesview = get("resources-view").getMarkupId();
		
		String script = "var uploader = new plupload.Uploader({"+
				"runtimes : 'html5, html4',"+
				"browse_button : 'pickfiles', "+
				"drop_element: 'resources-panel', "+
				"url : \"/upload?id="+ getModel().getObject().getId() +
					"&class="+getContentClass(getModel().getObject()) +
					"&public=true" +
					(tagid!=null?"&group="+tagid:"") + 
					"&class="+getContentClass(getModel().getObject()) + "\", " +
				"filters : {"+
				"	max_file_size : '40000mb',"+
				"	mime_types: ["+
				"		{title : \"Image files\", extensions : \"jpg,gif,png,webp\"}"+
				"	]"+
				"},"+
				"flash_swf_url : '/plupload/js/Moxie.swf',"+
				"silverlight_xap_url : '/plupload/js/Moxie.xap',"+
				"init: {"+
				"	PostInit: function() {"+
				"		document.getElementById('pickfiles').onclick = function() {"+
				"			uploader.start();"+
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
				"			file.name = file.name.replace('ñ', 'n'); file.name = file.name.replace('Ñ', 'N');  file.name.replace('#', '-'); "+
				"			top.filesAdded = top.filesAdded+1; "+
				"			document.getElementById('"+resourcesview+"').innerHTML = " +
				
				"'<div>" +
				"<span>' + file.name + ' ' + plupload.formatSize(file.size) + '</span>" +
				"<div class=\"progress\">" +
		 		"	<div id=\"' + file.id + '\" class=\"progress-bar\" role=\"progressbar\" aria-valuenow=\"0\" aria-valuemin=\"0\" aria-valuemax=\"100\">"+
				"		<span class=\"sr-only\" style=\"position: unset;\">' + file.percent + '%' + '</span>"+
		  		"	</div>"+
				"</div>"+
				"</div>' + document.getElementById('"+resourcesview+"' ).innerHTML;"+
				"			setTimeout(function () { uploader.start(); }, 500);"+
				"		})"	+ 
				"	},"+
				"	FileUploaded: function(up, files) {"+
				"		top.filesUploaded=top.filesUploaded+1; " + 
				"		if (top.filesUploaded>=top.top.filesAdded) { " + 
				"			setTimeout(function () { refreshfiles"+getMarkupId()+"(); }, 500);" + 
				"		};"+
				"	},"+
				"	UploadProgress: function(up, file) {"+
				"		document.getElementById(file.id).style.width = file.percent+'%';"+
				"		document.getElementById(file.id).innerHTML = '<span class=\"sr-only\" style=\"position: unset;\">' + file.percent + '%</span>';"+
				"	},"+
				"	Error: function(up, err) {"+
				"		document.getElementById('console').innerHTML += \"\\nError #\" + err.code + \": \" + err.message"+
				"	}"+
				"}"+
				"});"+
				"uploader.init();";
		
		response.render(OnLoadHeaderItem.forScript(script));
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao) ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private String getContentClass(Content content) {
		return Proxy.getClassName(content).toLowerCase(); // TODO VER LOWERCASE
	}
}
