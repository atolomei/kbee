package com.novamens.content.web.media;


import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.content.base.Content;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.resource.KBFile;

import kbee.util.FSUtils;
import kbee.util.PropertiesFactory;
import kbee.web.resource.WebResourceReference;
import kbee.web.resource.WebThumbnailReference;
import kbee.web.util.ResourceUriHelper;


public class VideoPanel<T extends ResourceContainer> extends MediaPanel {

	static String sheight = PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.content.web.video.player.height", "510");
	static String swidth  = PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.content.web.video.player.width", "850");
	
	static int height;
	static int width;
	
	private static final long serialVersionUID = 58399388763227255L;
	
	static  {
		try {
			height=Integer.valueOf(sheight).intValue();
			width=Integer.valueOf(swidth).intValue();
		} catch (Exception e) {
			height=510;
			width=850;
		}
	}
	
	private IModel<T> contentModel;
	

	private Page page = null;

	

	public VideoPanel(String id, IModel<KBFile> model, IModel<T> contentmodel, int max_width, int max_height) {
		super(id, model);
		
		this.contentModel = contentmodel;
		
		WebMarkupContainer mp = new WebMarkupContainer("media-panel");
		WebMarkupContainer pc = new WebMarkupContainer("player-container");
	 		
		if (isAudio(model)) {
			
			String filehref  =	ResourceUriHelper.getInstance().getHref(model.getObject());

			String html5AudioStr = "<audio  style=\"width:100%;\" controls><source src=\"" + filehref + "\" type=\"audio/mpeg\">";
												
			pc.add(new Label("player", html5AudioStr).setEscapeModelStrings(false));
			
			int delta = (model.getObject().getDescription()!=null && model.getObject().getDescription().length()>0?42:0); 
					
			pc.add(new AttributeModifier("style", "margin-top:"+String.valueOf(((int)(max_height-height-delta)/2))+"px;"));
			mp.add(pc);
			
			WebMarkupContainer dcon = new WebMarkupContainer("metadata-container"); 
			
			Label description = new Label("description", model.getObject().getDescription());
			dcon.add(description);
			mp.add(dcon);
			
			mp.add(new AttributeModifier("style", "margin-left:auto; margin-right:auto; float: none; width:"+ String.valueOf(width)+"px;"));
			add(mp);
			
		}
		else {
		
		//String image = getImageUrl(model, contentmodel);
		//String filehref = RequestCycle.get().urlFor(resourcereference, null).toString();
		String filehref  =	ResourceUriHelper.getInstance().getHref(model.getObject());
		
		//String stplayer = "<script type=\"text/javascript\">jwplayer(\"myElement\").setup({file:\""+filehref+"\",image: \"" + image +"\",width: "+ width +", height: "+height+"});</script>";
		//pc.add((new Label("player", stplayer)).setEscapeModelStrings(false));
		
			String s_width = "100%";
			
			String html5VideoStr = "<video  width=\"" + s_width  + "\" controls><source src=\"" + filehref + "\" type=\"video/mp4\"> +"
					+  "<source src=\"" + filehref + "\" type=\"video/ogg\"> + "
					+  "Your browser does not support the audio element.</video>";
	
			pc.add(new Label("player", html5VideoStr).setEscapeModelStrings(false));
			
			int delta = (model.getObject().getDescription()!=null && model.getObject().getDescription().length()>0?42:0); 
					
			pc.add(new AttributeModifier("style", "margin-top:"+String.valueOf(((int)(max_height-height-delta)/2))+"px;"));
			mp.add(pc);
			
			WebMarkupContainer dcon = new WebMarkupContainer("metadata-container"); 
			
			Label description = new Label("description", model.getObject().getDescription());
			dcon.add(description);
			mp.add(dcon);
			
			mp.add(new AttributeModifier("style", "margin-left:auto; margin-right:auto; float: none; width:"+ String.valueOf(width)+"px;"));
			add(mp);
		}
		
	}
	
	
	protected ResourceReference getReference(IModel<KBFile> model, IModel<T> contentmodel2) {
		return  new WebResourceReference(getModel().getObject(), (Content) contentmodel2.getObject());
	}

	public void onDetach() {
		getObjectModel().detach();
		super.onDetach();
	}
	
	public Page getGalleryReturnPage() {
		return page;
	}
	
	public void setGalleryReturnPage(Page page) {
		this.page=page;
	}

	
	public String getCss() {
		return "";
	}
	

	protected IModel<T> getObjectModel() {
		return contentModel;
	}
	
	
	
	
	/** 
	 * 
	 * @param model
	 * @param contentmodel
	 * @return
	 */
	protected String getImageUrl(IModel<KBFile> model, IModel<T> contentmodel) {

		ResourceReference imagereference;
		String image = null;

		// Thumbnail
		//
		if (getObjectModel().getObject() instanceof ResourceContainer) {
			List<KBFile> list = ((ResourceContainer) getObjectModel().getObject()).getFiles();
			for (KBFile file: list) {
				if (FSUtils.isImage(file.getName())) {
					imagereference = new WebResourceReference(file, (Content) getObjectModel().getObject());
					image = RequestCycle.get().urlFor(imagereference, null).toString();
					break;
				}
			}
		}
			
		// Si no tiene imagen intenta generar Thumbnail del THSeruer
		// 
		if (image==null && contentmodel.getObject() instanceof Content) {
			imagereference = new WebThumbnailReference(model.getObject(), (Content) contentmodel.getObject());
			image = RequestCycle.get().urlFor(imagereference, null).toString();
		}

		return image;
		
	}
	
	protected boolean isVideo(IModel<KBFile> mod) {
		try {
			return FSUtils.isVideo(mod.getObject().getFileName());
		} catch (Exception e) {
			return false;
		} 
	}
	
	protected boolean isAudio(IModel<KBFile> mod) {
		try {			
			return FSUtils.isAudio(mod.getObject().getFileName());
		} catch (Exception e) {
			return false;
		} 
	}
	
}
