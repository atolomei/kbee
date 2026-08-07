package com.novamens.content.web.base.page.component;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import com.novamens.beans.BeansService;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.resource.KBFile;
import com.novamens.content.web.media.GalleriePanel;
import com.novamens.content.web.media.ImagePanel;
import com.novamens.content.web.media.VideoPanel;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.FSUtils;
import kbee.util.PropertiesFactory;
import kbee.web.console.AjustableHeightBehavior;
import kbee.web.util.UriHelper;

/** -------------------------------------------------------------------------------
 * 
 * @param <ResourceContainer>
 *
 */

@Deprecated
@SuppressWarnings("serial")
public class KbeeGalleryWebPage extends KbeeResourceContainerWebPage {
			
	private static final long serialVersionUID = 8949573775757243607L;

	private static final ResourceReference CSS 		 = new CssResourceReference(KbeeGalleryWebPage.class, "gallery.css");
	private static final ResourceReference ICON_NEXT = new PackageResourceReference(KbeeGalleryWebPage.class, "arrow-right-white.png");
	private static final ResourceReference ICON_PREV = new PackageResourceReference(KbeeGalleryWebPage.class, "arrow-left-white.png");

	static final private org.apache.logging.log4j.Logger logger = LogManager.getLogger(KbeeGalleryWebPage.class.getName());
	
	static final String THUMBNAIL_HEIGHT  = PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.thumbnail.large.height", "280px");

	private boolean galleryMode = true;

	private int index = 0;

	private GalleriePanel<ResourceContainer> gallery = null;
	
	private int xmax_width;
	private int xmax_height;
	private boolean showAll = false;					// permite que se vean todas las imagenes, incluyendo las que no son grupos publicables 	
	private boolean single_image_mode = false;			// para mostrar solamente 1 imagen
	
	/** -----------------------------------------------------------------------------------------------------------------
	 */ 	
	public abstract class ListModel<L> implements IModel<List<L>> {
		private static final long serialVersionUID = 6957154488046877293L;
		public void setObject(List<L> list) {
		 }
		 public void detach() {
		 }
	}
	
	/** -----------------------------------------------------------------------------------------------------------------
	 */ 	
	protected KbeeGalleryWebPage() {
	}
	
	/** -----------------------------------------------------------------------------------------------------------------
	 */ 	
	public KbeeGalleryWebPage(PageParameters parameters) {
		ResourceContainer container = getContent(parameters);
		if (container!=null) {
			setModel(new ObjectModel<ResourceContainer>(container));
			initialize(-1, false, false);
		}
		else {
			logger.error("Container is null");
		}
	}
	
	/** -----------------------------------------------------------------------------------------------------------------
	 */ 	
	public KbeeGalleryWebPage(IModel<ResourceContainer> model) {
		setModel(model);
		setPageParameters();
		initialize(-1, false, false);
	}

	/** -----------------------------------------------------------------------------------------------------------------
	 */ 	
	public KbeeGalleryWebPage(IModel<ResourceContainer> model, boolean showAll) {
		setModel(model);
		setPageParameters();
		initialize(-1, showAll, false);
	}

	/** -----------------------------------------------------------------------------------------------------------------
	 */ 	
	public KbeeGalleryWebPage(IModel<ResourceContainer> model, int index) {
		setModel(model);
		setPageParameters();
		initialize(index, false, false);
	}

	/** -----------------------------------------------------------------------------------------------------------------
	 */ 	
	public KbeeGalleryWebPage(IModel<ResourceContainer> model, int index, boolean showAll) {
		setModel(model);
		setPageParameters();
		initialize(index, showAll, false);
	}
	
	/** -----------------------------------------------------------------------------------------------------------------
	 */ 	
	public KbeeGalleryWebPage(IModel<ResourceContainer> model, int index, boolean showAll, boolean single_image) {

		setModel(model);
		setPageParameters();
		
		initialize(index, showAll, single_image);
	}
	
	/** -----------------------------------------------------------------------------------------------------------------
	 * 	
	 * @param model
	 * @param standalone
	 * 
	 */
	protected void initialize(int index, boolean showAll, boolean single_image) {
		
		setOutputMarkupId(true);
		
		this.showAll			=	showAll;
		this.single_image_mode	=	single_image;
		
		
		// ACA SACAR DE JS CLIENTE ALEJO
		//
/*		xmax_width 	= WebSession.get().getClientInfo().getProperties().getBrowserWidth();
		xmax_height = WebSession.get().getClientInfo().getProperties().getBrowserHeight()-60;
				
		if (xmax_height<0)
			
			
			
			xmax_height=768;
		
		if (xmax_width<0)
			xmax_width=1280;
	*/							
 		setPageTitle( new Model<String>( getModel().getObject().getTitle()));
		setPageDescription( new Model<String>(getModel().getObject().toString()));
		setPageKeywords(getModel().getObject().toString());
		
		addHeader();
		
		WebMarkupContainer canvas = new WebMarkupContainer("canvas");
		add(canvas);
		canvas.add(new AjustableHeightBehavior(60));
		
		if (index==-1) {
			canvas.add(getGallery());
			canvas.add((new WebMarkupContainer("viewer").setVisible(false)));

		} else {
			canvas.add((getGallery()).setVisible(false));
			Panel panel = getViewer(new ObjectModel<KBFile>(getModel().getObject().getFiles().get(index)), true);
			panel.setVisible(true);
			canvas.add(panel);
			setGalleryMode(false);
		}
	}

	/** ------------------------------------------------------------------------------------------------
	 */ 
	protected int getMaxWidth() {
		return this.xmax_width;
	}	

	/** ------------------------------------------------------------------------------------------------
	 */ 
	protected int getMaxHeight() {
		return this.xmax_height;
	}

	/** ------------------------------------------------------------------------------------------------
	 */ 
	private boolean isShowAll() {
		return this.showAll;
	}
	
	/** ------------------------------------------------------------------------------------------------
	 * @return
	 */
	private GalleriePanel<ResourceContainer> getGallery() {
		
		if (gallery != null)
			return gallery;
		
		gallery = new GalleriePanel<ResourceContainer>("gallery", getModel(), isShowAll(), isSingleImageMode()) {
			private static final long serialVersionUID = -5560117755590291281L;
			@Override
			public void onSelect(AjaxRequestTarget target, IModel<KBFile> mod, int index) {
				KbeeGalleryWebPage.this.onSelect(target, mod, index);
			}
		};
		return gallery;
	}

	/** ------------------------------------------------------------------------------------------------
	 * 
	 * @param target
	 * @param mod
	 * @param ix
	 */
	protected void onSelect(AjaxRequestTarget target, IModel<KBFile> mod, int ix) {
		onSelect(target,  mod, ix, true);
	}

	/** ------------------------------------------------------------------------------------------------
	 *
	 */ 
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Panel getViewer(IModel<KBFile> mod,  boolean fitsize) {
		
		Panel panel = null;
		
		IModel<ResourceContainer> contentmodel = new ObjectModel<ResourceContainer>((ResourceContainer)getModel().getObject());
			if (isVideo(mod) || isAudio(mod)) 
				panel = new VideoPanel("viewer", mod, contentmodel, getMaxWidth(), getMaxHeight());
			else {
				if (fitsize)
					panel = new ImagePanel("viewer", mod, contentmodel, getMaxWidth(), getMaxHeight());
				else
					panel = new ImagePanel("viewer", mod,  contentmodel, 0, 0);
			}

		return panel;
	}
	
	/** -----------------------------------------------------------------------------------------------------------------
	 * 
	 * @param target
	 * @param mod
	 * @param ix
	 */
	protected void onSelect(AjaxRequestTarget target, IModel<KBFile> mod, int ix, boolean fitsize) {

		Panel panel=getViewer(mod, fitsize);
		setIndex(ix);
		getGallery().setVisible(false);

		get("canvas:viewer").replaceWith(panel);
		KbeeGalleryWebPage.this.get("canvas:viewer").setVisible(true);
		setIndex(ix);

		if (getGalleryMode()) {
			target.add(KbeeGalleryWebPage.this.get("canvas").getParent());
			target.add(get("header"));
			
		} else {
			target.add(KbeeGalleryWebPage.this.get("canvas"));
			target.add(get("header:nav"));
			target.add(get("header:index"));
		}
		setGalleryMode(false);
	}
	
	/** -----------------------------------------------------------------------------------------------------------------
	 */
	public void onReturn() {}
	
	
	/** -----------------------------------------------------------------------------------------------------------------
	 */
	private void addHeader() {

		WebMarkupContainer header = new WebMarkupContainer("header"); 
		Label title = new Label("title", getModelObject().getTitle());
	
		Link<Void> returnlink = new Link<Void>("return-link") {
			@Override
			public void onClick() {
			}
		};
	
		returnlink.add(new AttributeModifier("href", "javascript:close();"));

		Label indexlabel= new Label("index", new Model<String>("modelindex") {
				private static final long serialVersionUID = -3094847397904565519L;

				public String getObject() {
					return String.valueOf(getIndex()+1)+"/"+gallery.getFiles().size();
				}
			}) {
				private static final long serialVersionUID = -6751247788426172765L;
				@Override
				public boolean isVisible() {
					return !getGalleryMode() && !isSingleImageMode();
				}
			};
			
		indexlabel.setOutputMarkupId(true);
		header.add(indexlabel);

			WebMarkupContainer nav = new WebMarkupContainer("nav") {
				private static final long serialVersionUID = -7627639131882567837L;
				@Override
				public boolean isVisible() {
					return !getGalleryMode() && gallery.getFiles().size()>1 && !isSingleImageMode();
				}
			};
			nav.setOutputMarkupId(true);
			add(nav);
			
			AjaxLink<Void> prev = new AjaxLink<Void>("prev") {
				private static final long serialVersionUID = 1684871554283466385L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					onSelect(target, gallery.getFiles().get(getIndex()-1), getIndex()-1);
				}

				@Override
				public boolean isEnabled() {
					return !getGalleryMode() && getIndex()>0;
				}
				
				@Override
				public boolean isVisible() {
					return !getGalleryMode() && gallery.getFiles().size()>1;
				}
			};
			
				Image previmagen = new Image("prev-icon", ICON_PREV) {
				private static final long serialVersionUID = -1717807117654620712L;
				protected boolean shouldAddAntiCacheParameter()	{
					return false;
				}
			};
			
			previmagen.add(new AttributeModifier("title", "previous"));
			previmagen.add(new AttributeModifier("alt", "previous"));
			prev.add(previmagen);
			
			previmagen.setOutputMarkupId(true);
			prev.setOutputMarkupId(true);

			
			AjaxLink<Void> next = new AjaxLink<Void>("next") {
				private static final long serialVersionUID = 2431637224437970087L;
				@Override
				public void onClick(AjaxRequestTarget target) {
					onSelect(target, gallery.getFiles().get(getIndex()+1), getIndex()+1);
				}
				@Override
				public boolean isEnabled() {
					return !getGalleryMode() && getIndex()<gallery.getFiles().size()-1;
				}
				
				@Override
				public boolean isVisible() {
					return !getGalleryMode() && gallery.getFiles().size()>1;
				}
			};
			
			Image nextimagen = new Image("next-icon", ICON_NEXT) {
				private static final long serialVersionUID = 7009731822584111418L;
				protected boolean shouldAddAntiCacheParameter()	{
					return false;
				}
			};

			nextimagen.add(new AttributeModifier("title", "next"));
			nextimagen.add(new AttributeModifier("alt", "next"));
			next.add(nextimagen);
			nextimagen.setOutputMarkupId(true);
			next.setOutputMarkupId(true);
			
			nav.add(prev);
			nav.add(next);
			header.add(nav);
			
		AjaxLink<Void> viewmode = new AjaxLink<Void>("viewmode") {
			private static final long serialVersionUID = -3900836273946150511L;
			@Override
			public void onClick(AjaxRequestTarget target) {
					getGallery().setVisible(true);
					KbeeGalleryWebPage.this.get("canvas:viewer").setVisible(false);
					setGalleryMode(true);
					target.add(KbeeGalleryWebPage.this);
			}
			
			@Override
			public boolean isVisible() {
				return !isSingleImageMode();
			}
		};
		
		Label viewmodelabel = new Label("label", new Model<String>() {
			private static final long serialVersionUID = -2045090226172628334L;
			public String getObject() {
				return getGalleryMode()?"viewer":"gallery"; 					
			}
		}) {
			private static final long serialVersionUID = 1L;
			public boolean isVisible() {
				return !getGalleryMode() && !isSingleImageMode() && gallery.getFiles().size()>1;
			}
		};
		
		viewmode.add(viewmodelabel);
		header.add(viewmode);
		header.add(returnlink);
		header.add(title);
		add(header);
		header.setOutputMarkupId(true);
		
	}
	
	/** -----------------------------------------------------------------------------------------------------------------
	 */
	@Override
	public void onDetach() {
		if (gallery!=null)
			gallery.detach();
		super.onDetach();
	}

	/** -----------------------------------------------------------------------------------------------------------------
	 */
	public boolean isSingleImageMode() {
		return single_image_mode;
	}

	/** -----------------------------------------------------------------------------------------------------------------
	 */
	protected ResourceReference getCssResource() {
		return CSS;
	}
	
	/** -----------------------------------------------------------------------------------------------------------------
	 */
	protected ResourceContainer getContent(PageParameters parameters) {
		ResourceContainer content = null;
		if (parameters.get("oid")!=null && !"".equals(parameters.get("oid").toString())) {
			String oid = parameters.get("oid").toString();
			if (oid!=null)
				content = (ResourceContainer) getContentDao().findContentByOId(oid);
			return content;
		}	
		return content;
	}

	/** -----------------------------------------------------------------------------------------------------------------
	 */
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	/** -----------------------------------------------------------------------------------------------------------------
	 */
	private void setGalleryMode(boolean mode) {
		galleryMode=mode;
	}
	
	/** -----------------------------------------------------------------------------------------------------------------
	 */
	private boolean getGalleryMode() {
		return galleryMode;
	}

	/** -----------------------------------------------------------------------------------------------------------------
	 */
	protected void setPageParameters() {
		try {
			getPageParameters().set("site", "site");
			getPageParameters().set("oid", getModelObject().getOId().toString());
			getPageParameters().set("title", UriHelper.getInstance().getTitle(getModelObject()));
		} catch (RuntimeException e) {
			getPageParameters().set("site", "site");
			getPageParameters().set("oid", "na");
			getPageParameters().set("title", "na");
		}
	}

	/** -----------------------------------------------------------------------------------------------------------------
	 */
	private int getIndex() 					{return index;}

	/** -----------------------------------------------------------------------------------------------------------------
	 */
	private void setIndex(int ix) 			{index=ix;}

	
	/** -----------------------------------------------------------------------------------------------------------------
	 */
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
