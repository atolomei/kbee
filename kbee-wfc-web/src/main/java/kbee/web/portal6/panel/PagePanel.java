package kbee.web.portal6.panel;



import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.dom.ObjectState;
import com.novamens.portal6.model.Page;
import com.novamens.portal6.model.PageSection;
import com.novamens.portal6.model.PageSectionDisposition;
import com.novamens.portal6.model.PortalViewMode;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.DummyBlockPanel;

import kbee.web.portal6.IPageWebPanel;

import kbee.web.portal6.PortalObjectViewerRenderService;
import kbee.web.portal6.event.PortalAjaxEvent;

/**
 * structure view -> all panels with border
 * debug view -> all panels with borders + invisible panels -> replaced by dummy
 */

public class PagePanel extends PortalIWebPanel<Page> implements IPageWebPanel {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PagePanel.class.getName());
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private IModel<PageSection> top_ps;
	private IModel<PageSection> bottom_ps;
	private List<IModel<PageSection>> list_main_left_ps;
	private IModel<PageSection> main_right_ps;
	
	private WebMarkupContainer page_p;
	
	
	private boolean show_archived = false;
	private boolean show_deleted = false;
	//private boolean render_payload = false;
	
	boolean show_controller;
	
	
	/** -------------------------
	 * 
	 * 
	 * production
	 * edit
	 * debug-structure-visible
	 * debug-structure-all
	 */
	
	
	public PagePanel(String id, IModel<Page> model, PortalViewMode view_mode,  Map<String, String>  parameters) {
		super(id, model, view_mode, parameters);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
	
		setOutputMarkupId(true);
		
		if (getParameters()!=null) {
			if (getParameters().containsKey(PortalAjaxEvent.ARCHIVED_VISIBLE))			show_archived=getParameters().get(PortalAjaxEvent.ARCHIVED_VISIBLE).equals(String.valueOf(PortalAjaxEvent.SHOW_ARCHIVED_YES));
			if (getParameters().containsKey(PortalAjaxEvent.DELETED_VISIBLE))			show_deleted=getParameters().get(PortalAjaxEvent.DELETED_VISIBLE).equals(String.valueOf(PortalAjaxEvent.SHOW_DELETED_YES));
			if (getParameters().containsKey(PortalAjaxEvent.CONTROLLER_VISIBLE))		show_controller=getParameters().get(PortalAjaxEvent.CONTROLLER_VISIBLE).equals(String.valueOf(PortalAjaxEvent.SHOW_CONTROLLER_YES));
			// if (getParameters().containsKey(PortalAjaxEvent.PAYLOAD_VISIBLE))		render_payload=getParameters().get(PortalAjaxEvent.PAYLOAD_VISIBLE).equals(String.valueOf(PortalAjaxEvent.SHOW_PAYLOAD_YES));
		}
		 
		list_main_left_ps = new ArrayList<IModel<PageSection>>();
		
		/* we support only 1 page section on the main panel by the moment */
		for (PageSection ps:getModel().getObject().getPageSections()) {
			
			if		(ps.getPageSectionDisposition()==PageSectionDisposition.TOP) 		top_ps = new ObjectModel<PageSection>(ps);
			else if	(ps.getPageSectionDisposition()==PageSectionDisposition.BOTTOM)		bottom_ps = new ObjectModel<PageSection>(ps);
			else if	(ps.getPageSectionDisposition()==PageSectionDisposition.RIGHT)		main_right_ps = new ObjectModel<PageSection>(ps);
			else
				list_main_left_ps.add(new ObjectModel<PageSection>(ps));
		}
		
			logger.debug("top -> " + (top_ps!=null? top_ps.toString() : "null"));
			logger.debug("right -> " + (main_right_ps!=null? main_right_ps.getObject().toString() : "null"));
			logger.debug("bottom -> " + (bottom_ps!=null? bottom_ps.toString() : "null"));
		
		
			page_p = new WebMarkupContainer("page");
			
			if (getModel().getObject().getCss()!=null)
				this.page_p.add( new AttributeModifier("class", getModel().getObject().getCss() + " ps-panel-container"));
			
			xAdd(page_p);
				
		addTopPanel();
		addMainPanel();
		addBottomPanel();
		
	}

	public void onDetach() {
		super.onDetach();
		
		if (top_ps!=null)
			top_ps.detach();
		
		if (bottom_ps!=null)
			bottom_ps.detach();
		
		if (list_main_left_ps!=null)
			list_main_left_ps.forEach(item -> item.detach());
		
		if (main_right_ps!=null)
			main_right_ps.detach();
	}

	@Override
	public void addListeners() {
		super.addListeners();
	}

	
	
	protected void addMainPanel() {
		
		WebMarkupContainer mps 		= new WebMarkupContainer("main-ps");
		WebMarkupContainer mps_l 	= new WebMarkupContainer("main-left-ps");
		WebMarkupContainer mps_r 	= new WebMarkupContainer("main-right-ps");
		WebMarkupContainer mps_ll 	= new WebMarkupContainer("main-left-list-ps");
		
		String l_css = "ps-main-left ";
		String r_css = "ps-main-right ";
		
		if (main_right_ps!=null) {
			l_css += "w70";
			r_css += "w30";
		}
		else {
			l_css += "w100";
			r_css += "";
		}
		
		mps.add(mps_l);
		mps.add(mps_r);
		mps_l.add(mps_ll);

		try {

			if (list_main_left_ps==null || list_main_left_ps.size()==0)
				mps_ll.add(getVoidPanel("left-ps", new Model<String>("left-ps"), new Model<String>("[no panel]") ));
			else {
					mps_l.add(new AttributeModifier("class", l_css ));
					
					// we support only 1 PS on the main panel
					PageSection ps=list_main_left_ps.get(0).getObject();
					
					if (ps.getState()==ObjectState.ENABLED) {
						mps_ll.add(ps.getService(PortalObjectViewerRenderService.class).build("left-ps", -1, getViewMode(), getParameters()));
					}
					else if (ps.getState()==ObjectState.ARCHIVED && this.isShowArchived()) {
						mps_ll.add(ps.getService(PortalObjectViewerRenderService.class).build("left-ps", -1, getViewMode(), getParameters()));
					}
					else if (ps.getState()==ObjectState.DELETED && this.isShowDeleted()) {
						mps_ll.add(ps.getService(PortalObjectViewerRenderService.class).build("left-ps", -1, getViewMode(), getParameters()));
					}
					else
						mps_ll.add(getVoidPanel("left-ps", new Model<String>("left-ps"), new Model<String>("[no panel]") ));
				}
			} catch (Exception e) {
				logger.error(e);
				mps_ll.addOrReplace(new PortalErrorPanel<Page>("left-ps", getModel(), e));	
			}
		
		try {
			if (main_right_ps==null  || list_main_left_ps.size()==0)
				mps_r.add(getVoidPanel("right-ps", new Model<String>("right-ps"), new Model<String>("[no panel]")));
			else  {
				
				if (main_right_ps.getObject().getState()==ObjectState.ENABLED) {
					mps_r.add(new AttributeModifier("class", r_css ));
					mps_r.add(new DummyBlockPanel("right-ps", new Model<String>("right-ps"), new Model<String>("[dummy]"), null));
				}
				else if (main_right_ps.getObject().getState()==ObjectState.ARCHIVED && this.isShowArchived()) {
					mps_r.add(new AttributeModifier("class", r_css ));
					mps_r.add(new DummyBlockPanel("right-ps", new Model<String>("right-ps"), new Model<String>("[dummy]"), null));
				}
				else if (main_right_ps.getObject().getState()==ObjectState.DELETED && this.isShowDeleted()) {
					mps_r.add(new AttributeModifier("class", r_css ));
					mps_r.add(new DummyBlockPanel("right-ps", new Model<String>("right-ps"), new Model<String>("[dummy]"), null));
				}
				else
					mps_r.add(getVoidPanel("right-ps", new Model<String>("right-ps"), new Model<String>("[no panel]")));
			}
			page_p.add(mps);
			
		} catch (Exception e) {
			logger.error(e);
			mps_r.addOrReplace(new PortalErrorPanel<Page>("right-ps", getModel(), e));	
		}
	}

	
	/**
	 * 
	 * 
	 * 
	 */
	protected void addTopPanel() {
		try {
			if (top_ps==null) {
				page_p.add(getVoidPanel("top-ps", new Model<String>("top-ps" ), new Model<String>("[no panel]")));
				return;
			}
			
			if 		(top_ps.getObject().getState()==ObjectState.ENABLED) 						page_p.add(top_ps.getObject().getService(PortalObjectViewerRenderService.class).build("top-ps", -1, getViewMode(), getParameters()));
			else if (top_ps.getObject().getState()==ObjectState.ARCHIVED && isShowArchived())	page_p.add(top_ps.getObject().getService(PortalObjectViewerRenderService.class).build("top-ps", -1, getViewMode(), getParameters()));
			else if (top_ps.getObject().getState()==ObjectState.DELETED && isShowDeleted())		page_p.add(top_ps.getObject().getService(PortalObjectViewerRenderService.class).build("top-ps", -1, getViewMode(), getParameters()));
			else
				page_p.add(getVoidPanel("top-ps", new Model<String>("top-ps" ), new Model<String>("[no panel]")));
			
		} catch (Exception e) {
			page_p.addOrReplace(new PortalErrorPanel<Page>("top-ps", getModel(), e));	
		}
	}
	


	/**
	 * 
	 * 
	 */
	protected void addBottomPanel() {
		try {
		
			if (bottom_ps==null) {
				page_p.add(getVoidPanel("bottom-ps", new Model<String>("bottom-ps"), new Model<String>("[no panel]")));
				return;
			}
			
			if (bottom_ps.getObject().getState()==ObjectState.ENABLED) 				page_p.add(bottom_ps.getObject().getService(PortalObjectViewerRenderService.class).build("bottom-ps", -1,  getViewMode(), getParameters()));
			else if (bottom_ps.getObject().getState()==ObjectState.ARCHIVED && isShowArchived())				page_p.add(bottom_ps.getObject().getService(PortalObjectViewerRenderService.class).build("bottom-ps", -1,  getViewMode(), getParameters()));
			else if (bottom_ps.getObject().getState()==ObjectState.DELETED && isShowDeleted())				page_p.add(bottom_ps.getObject().getService(PortalObjectViewerRenderService.class).build("bottom-ps", -1,  getViewMode(), getParameters()));
			else				page_p.add(getVoidPanel("bottom-ps", new Model<String>("bottom-ps"), new Model<String>("[no panel]")));
			
		
		} catch (Exception e) {
			logger.error(e);
			page_p.addOrReplace(new PortalErrorPanel<Page>("bottom-ps",  getModel(), e));	
		}
	}
	
	@Override
	public IModel<String> getClassInfo() {
		StringBuilder str = new StringBuilder();
		str.append("<span class=\"highlight\">"+getModel().getObject().getClassKey()+"</span>");
		
		str.append( (getModel().getObject().isHome()?"  <span class=\"highlight\">(Home)</span>":""));
		str.append( (!getModel().getObject().isRegularPage()?"  <span class=\"highlight\">(Site Sections)</span>":""));
		
		return new Model<String>(str.toString());
	}

	protected boolean isMoveUpEnabled() {return false;}
	protected boolean isMoveDownEnabled() {return false;}
	protected boolean isEditEnabled() {return true;}
	protected boolean isAddEnabled() {return false;}
	
	@Override
	protected boolean isMoveEnabled() {return false;}
	
	
	protected boolean isArchiveEnabled() {return !getModel().getObject().isHome();}
	protected boolean isDeleteEnabled() {return !getModel().getObject().isHome();}
	
	
	protected IModel<String>  getMenuLabel(String string) {
		return getLabel(string);
	}
	 
	

	
	private boolean isShowDeleted() {
		return this.show_deleted;
	}
	
	private boolean isShowArchived() {
		return this.show_archived;
	}
	
	

}
