package kbee.web.form;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;


import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.value.IValueMap;
import org.apache.wicket.validation.IValidator;

import com.novamens.content.resource.KBFile;
import com.novamens.content.user.UserService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Domain;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.util.KbeeFileUtils;
import com.novamens.util.SimpleImageInfo;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.NumberFormatter;
import kbee.web.uploader.UploadBehavior;

@SuppressWarnings("serial")
public class FileUploadField extends Field<KBFile> {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(FileUploadField.class.getName());

	private IModel<KBFile> model;
	private  boolean isRemoveVisible = false;
	private  boolean isInfoVisible = false;
	
	public class RefreshUpload extends AbstractDefaultAjaxBehavior {
		@Override
		protected void respond(AjaxRequestTarget target) {
			FileUploadField.this.onUpdate(target);
		}
		@Override
		public void renderHead(Component component, IHeaderResponse response) {
			super.renderHead(component, response);
			String function = "refresh"+FileUploadField.this.getMarkupId();
			String f = String.format(function+"=%s", getCallbackFunction());
			response.render(JavaScriptHeaderItem.forScript(f, function));
		}
	}
	
	public class ControlFragment extends Fragment {
		
		WebMarkupContainer sub_container;
		Label subtitle;
		
		public ControlFragment(String id) {
			super(id, "control-fragment", FileUploadField.this);
			
			sub_container = new  WebMarkupContainer("subtitle-container");
			add(sub_container);
			sub_container.setVisible(  getSubtitle()!=null );
			subtitle = new Label("subtitle", getSubtitle());
			subtitle.setEscapeModelStrings(false);
			sub_container.add(subtitle);
			
			WebMarkupContainer input = new WebMarkupContainer("input") {
				protected void onComponentTag(final ComponentTag tag) {
					IValueMap attributes = tag.getAttributes();
					if (getEditor() != null && !getEditor().isEditionEnabled()) {
						attributes.put("disabled", "disabled");
					}
					super.onComponentTag(tag);
				}
			};
			
			WebMarkupContainer file = new WebMarkupContainer("file");
			
			file.add(new AttributeModifier("title", new Model<String>() {
					public String getObject() {
						if (getValue()!=null)
							return getUploadInfo(getValue());
						return "";
					}
				} 
			));
			
			Label fileName = new Label("file-name", new Model<String>() {
				public String getObject() {
					if (getValue()!=null) {
						return getValue().getName();
					}
					else
						return "";
				}
			}); 
			
			fileName.add(new AttributeModifier("id", "file-name"+FileUploadField.this.getMarkupId()));
			//fileName.setMarkupId("file-name"+FileUploadField.this.getMarkupId());
	 		
			file.add(fileName);
			
			input.add(file);
			
			add(input);
			
			add(new WebMarkupContainer("pickfiles")  {
				protected void onComponentTag(final ComponentTag tag) {
					IValueMap attributes = tag.getAttributes();
					if (getEditor() !=null && !getEditor().isEditionEnabled()) {
						attributes.put("disabled", "disabled");
					}
					attributes.put("id", "pickfiles"+FileUploadField.this.getMarkupId());
					super.onComponentTag(tag);
				}
			});
			
			WebMarkupContainer fic = new WebMarkupContainer ("file-info-container");
			

			WebMarkupContainer ficon = new WebMarkupContainer ("icon");
			
			try { 
				ficon.add(new AttributeModifier("class", (getModel()!=null && getModel().getObject() !=null? getModel().getObject().getGlyphIcon() : "fa-duotone fa-file-image")));
			} 
			catch (Exception e) {
				logger.error(e);
			}
			fic.add(ficon);

			
			AjaxLink<Void> fiopen=new AjaxLink<Void>("openfile") {
				@Override
				public void onClick(AjaxRequestTarget target) {
					FileUploadField.this.onOpenFile(target);
				}
			};
			fic.add(fiopen);

			if (getTabIndex()>0)
				fic.add(new AttributeModifier("tabindex", getTabIndex()));
			
			AjaxLink<Void> rl=new AjaxLink<Void>(id) {
				@Override
				public void onClick(AjaxRequestTarget target) {
					FileUploadField.this.onRemoveFile(target);
				}
				
			};
			fic.add(rl);
			
			fic.setVisible(isFileInfoVisible());
			add(fic);
			
			WebMarkupContainer progresspanel = new WebMarkupContainer("upload-progress");
			progresspanel.setOutputMarkupId(true);
			WebMarkupContainer progressbar = new WebMarkupContainer("progress-bar");
			progressbar.setOutputMarkupId(true);
			progresspanel.add(progressbar);
			add(progresspanel);
		}
	}
	
	
	public FileUploadField(String id) {
		this(id, null, false, Width.W12, null);
	}
	
	public FileUploadField(String id, Width width) {
		this(id, null, false, width, null);
	}
	
	public FileUploadField(String id, boolean required) {
		this(id, null, required, Width.W12, null);
	}
	
	public FileUploadField(String id, boolean required, IValidator<KBFile> validator) {
		this(id, null, required, Width.W12, validator);
	}
	
	public FileUploadField(String id, IModel<KBFile> model) {
		this(id, model, false, Width.W12, null);
	}
	
	public FileUploadField(String id, IModel<KBFile> model, boolean required, IValidator<KBFile> validator) {
		this(id, model, required, Width.W12, validator);
	}
	
	public FileUploadField(String id, IModel<KBFile> model, boolean required, Width width, IValidator<KBFile> validator) {
		super(id, model);
		
		setOutputMarkupId(true);
		
		setRequired(required);
		
		setWidth(width);
		
		if (model!=null) 
			setValue(model.getObject());
		
		if (validator!=null)
			add(validator);
		
		
	}

	
	public void onInitialize() {
		super.onInitialize();
		
		setOutputMarkupId(true);
	
		Label label = new Label("label", new StringResourceModel("property."+getProperty(), FileUploadField.this, null));
		label.add(new AttributeModifier("for", new Model<String>() {
			public String getObject() {
				return getInput()!=null ? getInput().getMarkupId() : "input";
			}
		}));
		label.add(new AttributeModifier("class", new Model<String>() {
			public String getObject() {
				return getDisposition()==null||getDisposition()==Disposition.HORIZONTAL ? "col-lg-2 control-label" : "control-label";
			}
		}));
		
		label.setEscapeModelStrings(false);
		add(label);
		
		add(new WebMarkupContainer("readonly") {
			public boolean isVisible() {
				return FileUploadField.this.isReadOnly();
			}
		});		
	}
	
	
	public void onRemove(AjaxRequestTarget target) {
	}
	
	
	
	public void onUpdate(AjaxRequestTarget target) {
	}
	

	
	
	public void setFileInfoVisible(boolean b) {
		this.isInfoVisible=b;
	}
	

	protected void onOpenFile(AjaxRequestTarget target) {
		// TODO Auto-generated method stub
		
	}


	protected void onRemoveFile(AjaxRequestTarget target) {
		
	}

	public boolean isFileInfoVisible() {
		return this.isInfoVisible;
	}
	
	
	public void setRemoveVisible(boolean b) {
		this.isRemoveVisible=b;
	}
	
	public boolean isRemoveVisible() {
		return this.isRemoveVisible;
	}
	
	
	@Override
	public void updateModel() {

		KBFile file = getValue();
		
		if (file!=null) {
			if (getModel() != null && getModel().getObject()!=null && !getModel().getObject().getName().equals(file.getName()) || getModel().getObject()==null && file!=null) {
				if (getEditor()!=null) 
					getEditor().setUpdatedPart(((Label)get("label")).getDefaultModelObjectAsString().toLowerCase());

				try {
						logger.debug(file.toString());
						file.setSHA256(KbeeFileUtils.calculateSHA256String(file.getFile()));
						setImageDimensions(file);
					} 
					catch (Exception e1) {
						logger.error(e1);
					}
				
				
				getModel().setObject(file);
			}
		}
	}
	
	protected void setImageDimensions(KBFile file) throws IOException {
		try {
			if (kbee.util.FSUtils.isImage(file.getFile())) {
				SimpleImageInfo imageInfo;
				int nw, nh;
				try {
					imageInfo = new SimpleImageInfo(file.getFile());
					nw  = imageInfo.getWidth();
					nh = imageInfo.getHeight();
				}
				catch (IOException e) {
					nw = 0;
					nh = 0;
				}
				file.setWidth(nw);
				file.setHeight(nh);
			}
		} catch (Exception e ) {
			logger.error(e);
		}
	}
	
	public Component getInput() {
		if (getDisposition()==null || getDisposition()==Disposition.HORIZONTAL) {
			return get("horizontal-layout:control:input");
		}
		else {
			return get("control:input");
		}
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		
		if (get("horizontal-layout")==null) {
			WebMarkupContainer layout = new WebMarkupContainer("horizontal-layout");
			layout.add(new AttributeModifier("class", getWidth().getCss()));
			layout.add(new ControlFragment("control"));
			add(layout);
			add(new ControlFragment("control"));
			add(new RefreshUpload());
			if (getDisposition()==null || getDisposition()==Disposition.HORIZONTAL) {
				get("control").setVisible(false);
			}
			else {
				layout.setVisible(false);
			}
		}
	}
	
	@Override
	public KBFile getValue() {
		try {
		if (model!=null) 
			return model.getObject();
		return null;
		} catch ( Exception e) {
			return null;
		}
	}


	@Override
	public void setValue(KBFile value) {
		
		if (value==null) 
			model = null;
		else
			model=new ObjectModel<KBFile>(value);
		
		if (model!=null)
			model.setObject(value);
	}
	
	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		
		if (getEditor() != null && !getEditor().isEditionEnabled())
			return;
		
		String fieldId = getMarkupId();
		String pickbutton = "pickfiles"+fieldId;
		String fileNameLabel = "file-name"+fieldId;
		String progresspanel = get("control:upload-progress").getMarkupId();
		String progressbar = get("control:upload-progress:progress-bar").getMarkupId();

		response.render(JavaScriptHeaderItem.forReference(
				new JavaScriptResourceReference(UploadBehavior.class, "js/plupload236/plupload.full.min.js")));
		
		String mimeTypes = "mime_types: [{title : \"Image files\", extensions : \"jpg,gif,png,webp\"},] ";
		mimeTypes ="";
		String script = "var uploader"+fieldId+"= new plupload.Uploader({"+
				"runtimes : 'html5, html4',"+
				"browse_button : '"+pickbutton+"', "+
//				"drop_element: 'consoledata', "+
				"url : \"/formupload?path="+getPath()+"\", " +
				"filters : {"+
				"	max_file_size : '10000mb',"+
					mimeTypes +
				"},"+
				"flash_swf_url : '/plupload/js/Moxie.swf',"+
				"silverlight_xap_url : '/plupload/js/Moxie.xap',"+
				"init: {"+
				"	PostInit: function() {"+
				"		document.getElementById('"+pickbutton+"').onclick = function() {"+
				"			uploader"+fieldId+".start();"+
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
				"			file.name = file.name.replace('ñ', 'n'); "+
				"			file.name = file.name.replace('Ñ', 'N'); "+
				"			file.name = file.name.replace('#', '-'); "+
				"			top.filesAdded = top.filesAdded+1; "+
			    "			document.getElementById('"+fileNameLabel+"').innerHTML = file.name ;" +
				"			setTimeout(function () { uploader"+fieldId+".start(); }, 500);"+
				"		})"	+ 
				"	},"+
				"	FileUploaded: function(up, files) {"+
				"		top.filesUploaded=top.filesUploaded+1; " + 
				"			setTimeout(function () { "+
				"				document.getElementById('"+progresspanel+"').className = 'kv-upload-progress hide';"+
				"				refresh"+fieldId+"(); " +
				"			}, 500);" + 
				"	},"+
				"	UploadProgress: function(up, file) {"+
				"		document.getElementById('"+progresspanel+"').className = 'kv-upload-progress';"+
				"		document.getElementById('"+progressbar+"').style.width = file.percent+'%';"+
				"		document.getElementById('"+progressbar+"').innerHTML = file.percent + '%';"+
				"	},"+
				"	Error: function(up, err) {"+
				"		document.getElementById('console').innerHTML += \"\\nError #\" + err.code + \": \" + err.message"+
				"	}"+
				"}"+
				"});"+
				"uploader"+fieldId+".init();";

		if (isEnabled()) {
			response.render(OnLoadHeaderItem.forScript(script));
		}
	}
	
	
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		if (model!=null)
			model.detach();
	}
	
	protected String getUploadInfo(KBFile file) {
		
		if (file==null)
			return null;

		OffsetDateTime date = file.getUploadOffsetDateTime();
		User user = file.getUploadUser();

		if (date==null)	
			date = OffsetDateTime.now();
		
		String size;
		
		if (file.getSize()>0)
			size=NumberFormatter.formatFileSize(file.getSize());
		else 
			size="0 bytes";
		
		DateTimeService service = ServiceLocator.getService(DateTimeService.class);
		
		User session_user = getUser();
		
		String zid = null;
		
		if (session_user!=null)
			zid=service.getMapZoneIds().get(session_user.getTimeZone());

		if (zid==null)
			zid=ZoneId.systemDefault().getId();
		
		
		String dateformatted = service.timeElapsed(date, 
				                                   ZoneId.of(zid), 
				                                   getLocale(), 
				                                   DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
		String wxh;

		int w = file.getWidth();
		if (w>0) {
			int h = file.getHeight();
			wxh = " · " + String.valueOf(w)+" x "+String.valueOf(h) + " pixels";
		}
		else
			wxh = "";
		
		String fln; 
		
		if (user==null || user.getFirstLastName()==null)
				fln="na";
		else
			fln = user.getFirstLastName();
		
		StringResourceModel rs = new StringResourceModel("file.uploadedby", this, null);
		rs.setParameters(fln, dateformatted, size, wxh);
		return rs.getString();
	}
	
	protected Domain getDomain() {
		return ServiceLocator.getService(UserService.class).getSessionUserProfile().getDomain();
	}

	protected KbeeUser getUser() {
		return (KbeeUser)ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
 