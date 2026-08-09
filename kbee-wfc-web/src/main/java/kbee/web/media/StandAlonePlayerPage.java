package kbee.web.media;

import java.io.IOException;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.novamens.content.resource.KBFile;
import com.novamens.util.KbeeRuntimeException;

import kbee.util.FSUtils;
import kbee.web.page.AbstractApplicationPage;
import kbee.web.page.InvisibleImage;
import kbee.web.resource.WebResourceReference;
import kbee.web.util.ResourceUriHelper;

public class StandAlonePlayerPage extends AbstractApplicationPage<KBFile> {
	private static final long serialVersionUID = 1L;
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(StandAlonePlayerPage.class.getName());
	
	private IModel<KBFile> model;
	
	public StandAlonePlayerPage(PageParameters parameters) {
		throw new KbeeRuntimeException("Can not access this page by url");
	}
	
	public StandAlonePlayerPage(IModel<KBFile> model) {
		setModel(model);
	}

	public void onInitialize() {
		super.onInitialize();
		setTopNavigation(new PlayerNavigationBar("navigation", getModel()));
		setPageTitle(new Model<String>(getModel().getObject().getDisplayName()));
		addCompoents();
	}
	
	@Override
	public void onDetach() {
		getModel().detach();
		super.onDetach();
	}
	
	protected boolean hasLateralMenu() {
		return false;
	}
	
	public IModel<KBFile> getModel() {
		return model;
	}
	
	public void setModel( IModel<KBFile> model) {
		this.model = model;
	}

	@Override
	protected Panel getFooter() {
		return null;
	}
	
	protected boolean isVideo() {
		return FSUtils.isVideo(getModel().getObject().getFileName());
	}
	
	protected boolean isAudio() {
		return FSUtils.isAudio(getModel().getObject().getFileName());		
	}

	protected boolean isImage() {
		return FSUtils.isImage(getModel().getObject().getFileName());		
	}
		
	/**
	 * This method may be overriden by subclasses in kbee-portal
     *
	 * @param model
	 * @param contentmodel
	 * @return
	 * 
	 * , IModel<ResourceContainer> contentmodel
	 * 
	 */
	protected Image getResourceReference() {
		Image image;
		WebResourceReference wref;
		wref = new WebResourceReference(getModel().getObject());
		image = new Image ( "img", wref) { 
			private static final long serialVersionUID = 1L;
			protected boolean shouldAddAntiCacheParameter()	{
				return false;
			}
		};
		return image;
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
		
		 /**
		  * 
		  */
		 private void addCompoents() {
			 
			 if (isVideo())				 addVideoPlayer();
			 else if (isAudio())		 addAudioPlayer();
			 else if (isImage())		 addImageViewer();
			 else						 addImageViewer();
				 
			 Label title 			= new Label("video-title", getModel().getObject().getTitle());
			 Label description 		= new Label("video-description", getModel().getObject().getDescription());

			 description.setVisible(getModel().getObject().getDescription()!=null && getModel().getObject().getDescription().length()>0);
			 description.setEscapeModelStrings(false);

			 WebMarkupContainer tc= new WebMarkupContainer("title-container");
			 tc.setVisible(!isImage() && getModel().getObject().getTitle()!=null && getModel().getObject().getTitle().length()>0);
			 tc.add(title);
			 ((WebMarkupContainer) get("video-panel")).add(tc);
			 
			 WebMarkupContainer dc= new WebMarkupContainer("desc-container");
			 dc.setVisible(!isImage() && getModel().getObject().getDescription()!=null && getModel().getObject().getDescription().length()>0);
			 dc.add(description);
			 ((WebMarkupContainer) get("video-panel")).add(dc);
				
		 }
		 
		 
			/**
			 * 
			 * 
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
				KBFile video_file = getModel().getObject();
				
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
			 * 
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
				
				KBFile video_file = getModel().getObject();
				
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
				//ResourceReference resourceReference = new WebResourceReference(video_file);
				//Audio audio = new Audio("player_v3", resourceReference);
				//container.add(audio);
				//container.add(new InvisiblePanel("player_v2"));
				
				
			 }

}
