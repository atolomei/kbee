package com.novamens.content.web.media;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.resource.KBFile;
import com.novamens.util.SimpleImageInfo;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.NumberFormatter;
import kbee.util.PropertiesFactory;
import kbee.web.resource.WebThumbnailReference;

public class GalleriePanel<T extends ResourceContainer> extends Panel {

	private static final long serialVersionUID = 3715321506120118480L;
										
	static final private org.apache.logging.log4j.Logger logger = LogManager.getLogger(GalleriePanel.class.getName());

	
	static final String THUMBNAIL_HEIGHT  = PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.thumbnail.large.height", "280px");

	private List<IModel<KBFile>> files;
	private ListView<IModel<KBFile>> filesview;
	
	private boolean showAll = false;
	private boolean is_single_image_mode=false;
	private IModel<T> model;

	

	public abstract class ListModel<L> implements IModel<List<L>> {
		private static final long serialVersionUID = 6957154488046877293L;
		public void setObject(List<L> list) {
		}
		public void detach() {
		}
	}


	public GalleriePanel(String id, IModel<T> model, boolean showAll, boolean isSingleImage) {
		super(id);
		initialize(model, showAll, isSingleImage);
	}
	
	public IModel<T> getModel() {
		return model;
	}
	
	public T getModelObject() {
		return getModel().getObject();
	}
	
	public void setModel(IModel<T> model) {
		this.model = model;
	}
	


	public void onReturn() {}
	public void onSelect(AjaxRequestTarget target, IModel<KBFile> model2, int index) {}


	public List<IModel<KBFile>> getFiles() {
		return files;
	}
	
	private boolean isShowAll() {
		return this.showAll;
	}


	@Override
	public void onDetach() {
		if (files==null) {
			super.onDetach();
			return;
		}
		for (IModel<KBFile> fmodel : files) 
			fmodel.detach();
		@SuppressWarnings("unchecked")
		ListView<IModel<KBFile>> view = (ListView<IModel<KBFile>>)get("main-area:resource-list-container:file");
		for (IModel<KBFile> fmodel : view.getList()) 					
			fmodel.detach();
		super.onDetach();
	}

	/** ---------------------------------------------------------------------------------------
	 * @param model
	 */
	private void initialize(IModel<T> model, boolean showAll, boolean isSingleImage) {
		
		this.showAll=showAll;
		this.is_single_image_mode=isSingleImage;
		
		setOutputMarkupId(true);
		setModel(model);
		
		WebMarkupContainer mainarea = new WebMarkupContainer("main-area");
		
		mainarea.add(new AttributeModifier("class", "gallery-body " + " fullw"));
		
		add(mainarea);
		
		WebMarkupContainer resourcesContainer = new WebMarkupContainer("resource-list-container");
		
		ResourceContainer rc = (ResourceContainer) getModelObject();
		
		setFiles(rc.getFiles());
		
		ListModel<IModel<KBFile>> filesmodel = new ListModel<IModel<KBFile>>() {
	 		private static final long serialVersionUID = 2123596551168128000L;
			public List<IModel<KBFile>> getObject() {
				return getFiles();
			}
		};
		
		filesview = new ListView<IModel<KBFile>>("file", filesmodel) {
		 
			private static final long serialVersionUID = -7932829944279578339L;
	
			protected void populateItem(ListItem<IModel<KBFile>> item){
	
				final int index = item.getIndex();
				final KBFile file = item.getModelObject().getObject();
				
				com.novamens.security.User user = file.getLastModifiedUser();
				String dateformatted =  file.getLastModifiedOffsetDateTimeColloquial();
		
				String wh = null;
				
				if (file.getWidth()>0 && file.getHeight()>0) {
					wh = String.format(". %d x %d", file.getWidth(), file.getHeight());
				}
				else {
						SimpleImageInfo imageInfo;
						try {
							if (kbee.util.FSUtils.isImage(file.getFile())) {
								imageInfo = new SimpleImageInfo(file.getFile());
								wh = String.format(". %d x %d", imageInfo.getWidth(), imageInfo.getHeight());
							}
							else
								wh="";
						
						} catch (IOException e) {
							logger.error(e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
							wh="";
						}
				}
																						
				StringResourceModel rsalt = new StringResourceModel("gallery.alt", this, null);
				rsalt.setParameters(file.getTitle(), file.getName(), user.getFirstLastName(), NumberFormatter.formatFileSize(file.getSize()), wh, dateformatted, file.getDescription(120));
				
				WebMarkupContainer thumbnailcontainer = new WebMarkupContainer("thumbnail");
				WebMarkupContainer metainfocontainer = new WebMarkupContainer("metainfo");
				metainfocontainer.setVisible(false);
							
					AjaxLink<Void> filelink = new AjaxLink<Void>("file-link") {
						private static final long serialVersionUID = 1L;
						@Override
						public void onClick(AjaxRequestTarget target) {
							IModel<KBFile> model=getFiles().get(index);
							GalleriePanel.this.onSelect(target, model, index);
						}
					};
					
					WebThumbnailReference imagereference;
					
					if (GalleriePanel.this.getModelObject() instanceof Content)
						imagereference = new WebThumbnailReference(file, (Content) GalleriePanel.this.getModelObject());
					else
						imagereference = new WebThumbnailReference(file, (ResourceContainer) GalleriePanel.this.getModelObject());
					
					Image thumbnail = new Image("file-thumbnail", imagereference) { 
						private static final long serialVersionUID = 1L;
						protected boolean shouldAddAntiCacheParameter()	{
							return false;
						}
					};
					
					thumbnail.add(new AttributeModifier("class", "gallery-image"));
					thumbnail.add((new AttributeModifier("alt",   rsalt.getString()+"\n"+wh)));
					thumbnail.add((new AttributeModifier("title", rsalt.getString())));
	
					thumbnail.add((new AttributeModifier("max-height",   THUMBNAIL_HEIGHT)));
					 
					
					filelink.add(thumbnail);
					thumbnailcontainer.add(filelink);
					
					item.add(thumbnailcontainer);
					item.add(metainfocontainer);
				
					StringBuilder clazz = new StringBuilder();
					clazz.append("file");
					item.add(new AttributeModifier("class", clazz.toString()));
			}
		};
		resourcesContainer.add(filesview);
		mainarea.add(resourcesContainer);
	}	


	private void setFiles(List<KBFile> files) {
	
		if (files==null)
			return;
		
		this.files = new ArrayList<IModel<KBFile>>();
		
		// TODO: Ver el grupo migracion como emprolijar esto
		//
		for (KBFile file: files) {
			
			if (file==null)
				break;
			
			if ((isShowAll()) || 
				(file.getGroup()==null) ||
				((file.getGroup()!=null) && (file.getGroup().getName()!=null && !(file.getGroup().getName().startsWith("migra"))))
			   ) {
				// Video
				if (file.isVideo())
					this.files.add(new ObjectModel<KBFile>(file));
			
				// imagenes
				else if (file.isImage())
					this.files.add(new ObjectModel<KBFile>(file));
			 }
		}
	}



	@SuppressWarnings("unused")
	private boolean isSingleImageMode() {
		return this.is_single_image_mode;
	}



}
