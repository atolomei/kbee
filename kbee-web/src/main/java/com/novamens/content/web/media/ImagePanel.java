package com.novamens.content.web.media;

import java.io.IOException;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.resource.KBFile;
import com.novamens.kbee.wicket.util.GenericPhoto;

import kbee.web.resource.WebResourceReference;
import kbee.web.resource.WebThumbnailReference;




public class ImagePanel extends MediaPanel {
									
	

	private static final long serialVersionUID = -7291445540812388938L;

	int MAX_WIDTH;
	int MAX_HEIGHT;

	public ImagePanel(String id, IModel<KBFile> model, IModel<ResourceContainer> contentmodel, int max_width, int max_height) {
		super(id, model);

		WebMarkupContainer dcon = new WebMarkupContainer("description-container"); 
		
		String des = getDescription(model, contentmodel);
		
		Label label = new Label("description", des);
		dcon.add(label);
		add(dcon);
		
		int te = 0;
		if (model.getObject().getDescription()!=null && max_height>0) {
			te= 18+14*(1+(int)(model.getObject().getDescription().length()/140));
		}
		
		try {
			if (kbee.util.FSUtils.isImage(model.getObject().getFile())) {
				addImage(model, contentmodel,max_width,max_height-te); 
			}
			else {
				addThumbnail(model, contentmodel, max_width,max_height);
			}
		
		} catch (IOException e) {
			addThumbnail(model, contentmodel, max_width,max_height);
		}
	}


	protected String getDescription(IModel<KBFile> model, IModel<ResourceContainer> contentmodel) {
		return model!=null?model.getObject().getDescription(240):"";
	}
	
	/**
	 * 
	 * @param model
	 * @param contentmodel
	 * @param max_width
	 * @param max_height
	 */
	private void addThumbnail(IModel<KBFile> model, IModel<ResourceContainer> contentmodel, int max_width, int max_height) {
		
		WebMarkupContainer icon = new WebMarkupContainer("image-container"); 
		add(icon);
		
		WebThumbnailReference imagereference;
		imagereference = new WebThumbnailReference(model.getObject(), contentmodel.getObject());
		
		Image thumbnail = new Image("image", imagereference) { 
			private static final long serialVersionUID = 1L;
			protected boolean shouldAddAntiCacheParameter()	{
				return false;
			}
		};
		
		thumbnail.add(new AttributeModifier("class", "gallery-image"));
		icon.add(thumbnail);
		
		get("description-container").add(new AttributeModifier("style", "float: none; margin: auto; text-align: center;min-width:1024px;"));
		
	}
	
	/** ---------------------------------------------------------------------------------------------------
	 * @param model
	 * @param contentmodel
	 * @param max_width
	 * @param max_height
	 */										
	
	private void addImage(IModel<KBFile> model, IModel<ResourceContainer> contentmodel, int max_width, int max_height) {
		
		//int h_margin = 0;  
		//int w_margin = 0;
		//int width    = 0;
		//int height   = 0;
		
		//MAX_WIDTH = max_width;
		//MAX_HEIGHT = max_height;

		// int width_src = 0, height_src = 0;
		
		/*
		try {
			if (com.novamens.util.FSUtils.isImage(model.getObject().getFile())) {
				if (model.getObject().getFile() instanceof KBFile) {
					width_src  = ((KBFile) model.getObject().getFile()).getWidth();
					height_src  = ((KBFile) model.getObject().getFile()).getHeight();
				}
				if (width_src==0 || height_src==0) {	
					SimpleImageInfo imageInfo;
					imageInfo = new SimpleImageInfo(model.getObject().getFile());
					width_src  = imageInfo.getWidth();
					height_src = imageInfo.getHeight();
				}
			}
			else {
				width_src  = 200;
				height_src = 320;
			}
	
			} catch (IOException e) {
				width_src  = 200;
				height_src = 320;
			}
		
		*/
		
		//int delta;
		//if (max_height>0) {
		//		width = width_src;
		//		height = height_src;

		//		if (height_src>MAX_HEIGHT) { 
		//			delta=height_src-MAX_HEIGHT;
			//		width=(int)( (width_src*(height_src-delta)) / height_src);
			//		height = MAX_HEIGHT;
		//		}
		
		//		if (width>MAX_WIDTH) { 
		//			delta=width-MAX_WIDTH;
		//			height=(int)((height*(width-delta))/width);
		//			width=(int)((height*width_src)/height_src);
		//		}
		
		//		h_margin = (int) ((MAX_HEIGHT - height)/2);  
		//		w_margin = (int) ((MAX_WIDTH - width)/2);
		
		// } else {
		
		//
		//
			
		//		MAX_WIDTH = 980;
		//		MAX_HEIGHT = 768-58;
		
		//	if (width_src<MAX_WIDTH) 
		//		w_margin = (int) ((MAX_WIDTH - width_src)/2);
			
		//	if (height_src<MAX_HEIGHT) 
		//		h_margin = (int) ((MAX_HEIGHT - height_src)/2);
		// }
		
		
		WebMarkupContainer icon = new WebMarkupContainer("image-container"); 
		add(icon);
		
		Image image = getResourceReference(model, contentmodel);
		icon.add(image);

		 
		
	}


	/**
	 * This method may be overriden by subclasses in kbee-portal
     *
	 * @param model
	 * @param contentmodel
	 * @return
	 */
	protected Image getResourceReference(IModel<KBFile> model, IModel<ResourceContainer> contentmodel) {
		Image image;
		WebResourceReference wref;
		if (contentmodel.getObject() instanceof Content) {
			wref = new WebResourceReference(model.getObject(), (Content) contentmodel.getObject());
			image = new Image ( "image", wref) { 
				private static final long serialVersionUID = 1L;
	
				protected boolean shouldAddAntiCacheParameter()	{
					return false;
				}
			};
		} else {
			return new GenericPhoto("image");
		}
		return image;
		
	}	
  }

