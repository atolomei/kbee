package kbee.web.searcher.page;

import java.io.IOException;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.base.Content;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.resource.KBFile;
import com.novamens.portal6.model.Site;
import com.novamens.util.KbeeRuntimeException;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.FSUtils;
import kbee.web.page.InvisibleImage;
import kbee.web.resource.WebResourceReference;
import kbee.web.util.ResourceUriHelper;


public class SearcherDetailVideoPage<T extends Content> extends SearcherDetailPage<T> {
																							
	private static final long serialVersionUID = 1L;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SearcherDetailVideoPage.class.getName());
	
	IModel<KBFile> media_model;
	

	public SearcherDetailVideoPage(PageParameters parameters) {
		super(parameters);
		setMediaModel(getModel());
		setSpecificCss("video");
		
	}

	
	public SearcherDetailVideoPage(IModel<T> model, IModel<Site> site_model) {
		super(model, site_model);
		setMediaModel(model);
		setSpecificCss("video");
	}
	
	
	/**
	 * do not include the "top" link
	 */
	@Override
	public boolean isFooterRequired() {
		return false;
	}
	
	
	 @Override
	 public void onDetach() {
		 if( media_model !=null)
			 media_model.detach();
		 super.onDetach();
	 }

	/**
	 * @param model
	 */
	public void setMediaModel(IModel<T> model) {
		
		if (model.getObject() instanceof ResourceContainer) {
			ResourceContainer rc = (ResourceContainer) model.getObject();
			List<KBFile> list = rc.getFiles();
			if (getModel().getObject().getContentTemplate().isVideo()) {
				for(KBFile file: list) {
					if (file.isVideo())  {
						media_model = new ObjectModel<KBFile>(file);
						return;
					}
				}
				
			}
			else if (getModel().getObject().getContentTemplate().isAudio()) {
				for(KBFile file: list) {
					if (file.isAudio())  {
						media_model = new ObjectModel<KBFile>(file);
						return;
					}
				}
			}
			else if (getModel().getObject().getContentTemplate().isImage()) {
				for(KBFile file: list) {
					if (file.isImage())  {
						media_model = new ObjectModel<KBFile>(file);
						return;
					}
				}
			}
			
			if (list.size()>0) {
				for(KBFile file: list) {
					if (file.isImage() || file.isAudio() || file.isVideo())  {
						media_model = new ObjectModel<KBFile>(file);
						return;
					}
				}
				KBFile file = list.get(0);
				media_model = new ObjectModel<KBFile>(file);
				return;
			}
		}
	}
	

	public IModel<KBFile> getMediaModel() {
		return media_model;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		if (!hasPermissions())
			throw new KbeeRuntimeException("Not authorized");
		
		
		if (getMediaModel()==null ||getMediaModel().getObject()==null)
				throw new KbeeRuntimeException("No media content");

		 if 	 (isVideo())		 addVideoPlayer();
		 else if (isAudio())		 addAudioPlayer();
		 else if (isImage())		 addImageViewer();
		 else						 addImageViewer();
			 
		 Label title 			= new Label("video-title", getMediaModel().getObject().getTitle());
		 Label description 		= new Label("video-description", getMediaModel().getObject().getDescription());

		 description.setVisible(getMediaModel().getObject().getDescription()!=null && getMediaModel().getObject().getDescription().length()>0);
		 description.setEscapeModelStrings(false);

		 WebMarkupContainer tc= new WebMarkupContainer("title-container");
		 tc.setVisible(false); // isAudio() && getModel().getObject().getTitle()!=null && getModel().getObject().getTitle().length()>0);
		 tc.add(title);
		 ((WebMarkupContainer) get("video-panel")).add(tc);
		 
		 WebMarkupContainer dc= new WebMarkupContainer("desc-container");
		 dc.setVisible(isAudio() && getMediaModel().getObject().getDescription()!=null && getMediaModel().getObject().getDescription().length()>0);
		 dc.add(description);
		 ((WebMarkupContainer) get("video-panel")).add(dc);
		
	}
	

	
	
	
	private void addImageViewer() {

		WebMarkupContainer vpanel = new WebMarkupContainer("video-panel");
		add(vpanel);
		vpanel.add(new AttributeModifier("class", "video-panel image"));
		vpanel.add(new AttributeModifier("style", "width: 100%; background: inherit;"));
		
		WebMarkupContainer container = new WebMarkupContainer("player-container") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
					return true;
			}
		}; 
		vpanel.add(container);
		container.add( (new Label("player", "No video or audio  file").setVisible(false)));
		container.add(getResourceReference());
		container.add(new AttributeModifier("style", "width:100%; height: initial;"));
		
	 }

	
	
	protected Image getResourceReference() {
		Image image;
		WebResourceReference wref = new WebResourceReference(getMediaModel().getObject());
			image = new Image ( "img", wref) { 
				private static final long serialVersionUID = 1L;
				protected boolean shouldAddAntiCacheParameter()	{
					return false;
				}
			};
		return image;
	}

	/**
	 */ 
	private void addVideoPlayer() {
		
		WebMarkupContainer  vpanel = new WebMarkupContainer("video-panel");
		add(vpanel);
		vpanel.add(new AttributeModifier("class", "video-panel video"));
							
		WebMarkupContainer container = new WebMarkupContainer("player-container") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
					return true;
			}
		}; 

		vpanel.add(container);
		KBFile video_file = getMediaModel().getObject();
		
		try {
			if (video_file==null || (!video_file.isBinaryFile())) {
					container.add(new Label("player", "No file"));
					container.add(new InvisibleImage("img"));
					return;
			}
		} catch (IOException e) {
			 logger.error(e);
			container.add(new Label("player", "No file"));
			container.add(new InvisibleImage("img"));
			return;
		}
		
		String filehref  =	ResourceUriHelper.getInstance().getHref(video_file);
 		String s_width = "100%";
 		
 		/*										
		String html5VideoStr = "<video   width=\"" + s_width  + "\" controls><source src=\"" + filehref + "\" type=\"video/mp4\"> +"
						+  "<source src=\"" + filehref + "\" type=\"video/ogg\"> + "
						+  "<source src=\"" + filehref + "\" type=\"video/mov\"> + "
						+  "Your browser does not support the audio element.</video>";
						
		*/
		//ResourceReference resourceReference = new WebResourceReference(video_file);
		//Video video = new Video("player_v2", resourceReference);
		//container.add(video);
		//container.add(new InvisiblePanel("player_v3"));
		
		
		String html5VideoStr = "<video id='my-video' class='video-js' controls preload='auto' "
				+ "width='"+s_width+"' height='60vh' data-setup='{}'> "
				+ "<source src='"+ filehref + "' type='video/mp4'> "
				+ "<source src='"+filehref+"' type='video/webm'>   "
				+ "<p class='vjs-no-js'>  To view this video please enable JavaScript, and consider upgrading to a web browser that "
				+ "<a href='https://videojs.com/html5-video-support/' target='_blank'>supports HTML5 video</a></p></video>";
		
		
		container.add(new Label("player", html5VideoStr).setEscapeModelStrings(false));
		container.add(new InvisibleImage("img"));

	}
 
	
	/**
	 * 
	 */
	private void addAudioPlayer() {

		WebMarkupContainer  vpanel = new WebMarkupContainer("video-panel");
		add(vpanel);
		
		vpanel.add(new AttributeModifier("class", "video-panel audio"));
		
		WebMarkupContainer  container = new WebMarkupContainer("player-container") {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isVisible() {
					return true;
			}
		}; 

		vpanel.add(container);
		
		KBFile video_file = getMediaModel().getObject();
		
		try {
			if (video_file==null || (!video_file.isBinaryFile())) {
					container.add(new Label("player", "No file"));
					container.add(new InvisibleImage("img"));
					return;
			}
		} catch (IOException e) {
			 logger.error(e);
			container.add(new Label("player", "No file"));
			container.add(new InvisibleImage("img"));
			return;
		}
		
		String filehref  =	ResourceUriHelper.getInstance().getHref(video_file);
		String html5AudioStr = "<audio  style=\"width:100%;\" controls><source src=\"" + filehref + "\" type=\"audio/mpeg\">";
		//String html5AudioStr = "<audio id='audio' class='video-js vjs-default-skin' controls preload='auto' width='100%' height='60vh' data-setup='{}'>  <source src=\""+ filehref +"\" type='audio/mp3'> </audio>";
		container.add(new Label("player", html5AudioStr).setEscapeModelStrings(false));
		
		container.add(new InvisibleImage("img"));
		// ResourceReference resourceReference = new WebResourceReference(video_file);
		// Audio audio = new Audio("player_v3", resourceReference);
		// container.add(audio);
		// container.add(new InvisiblePanel("player_v2"));
	 }
	
	protected boolean isVideo() {
		return FSUtils.isVideo(getMediaModel().getObject().getFileName());
	}

	
	protected boolean isAudio() {
		return FSUtils.isAudio(getMediaModel().getObject().getFileName());		
	}
		

	protected boolean isImage() {
		return FSUtils.isImage(getMediaModel().getObject().getFileName());		
	}

}
