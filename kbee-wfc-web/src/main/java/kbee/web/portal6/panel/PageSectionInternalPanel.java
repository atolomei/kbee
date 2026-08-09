package kbee.web.portal6.panel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.dom.ObjectState;
import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.PageSection;
import com.novamens.portal6.model.PortalViewMode;
import com.novamens.wicket.model.ListModel;

import kbee.web.error.ErrorPanel;
import kbee.web.portal6.PortalObjectViewerRenderService;
import kbee.web.portal6.event.PortalAjaxEvent;

/**
 * 
 * PRODUCTION -> enabled, areas with no internal panel -> no
 * 
 * EDIT -> all areas. when area has no internal panel ?
 * 
 *  
 * 
 * RENDER ARCHIVED
 * RENDER DELETED
 * 
 * 
 *
 */
public class PageSectionInternalPanel extends PortalPanel<PageSection> {
			
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PageSectionInternalPanel.class.getName());


	private WebMarkupContainer al;
	private int tab_index = -1;
	private PortalViewMode view_mode;
	
	Map<String, String> parameters;
	
	boolean show_deleted;
	boolean show_archived;
	boolean show_controller;
	
	
	
	public PageSectionInternalPanel(String id, IModel<PageSection> model, PortalViewMode view_mode, Map<String, String> parameters) {
		super(id, model);
			this.view_mode=view_mode;
			this.parameters=parameters;
	}

	
	public PortalViewMode getViewMode() {
			return this.view_mode;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		_list=null;
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		
		
		if (getParameters()!=null) {
			if (getParameters().containsKey(PortalAjaxEvent.ARCHIVED_VISIBLE))				show_archived=getParameters().get(PortalAjaxEvent.ARCHIVED_VISIBLE).equals(String.valueOf(PortalAjaxEvent.SHOW_ARCHIVED_YES));
			if (getParameters().containsKey(PortalAjaxEvent.DELETED_VISIBLE))				show_deleted=getParameters().get(PortalAjaxEvent.DELETED_VISIBLE).equals(String.valueOf(PortalAjaxEvent.SHOW_DELETED_YES));
			if (getParameters().containsKey(PortalAjaxEvent.CONTROLLER_VISIBLE))			show_controller=getParameters().get(PortalAjaxEvent.CONTROLLER_VISIBLE).equals(String.valueOf(PortalAjaxEvent.SHOW_CONTROLLER_YES));
		}
		 
		
		al = new WebMarkupContainer("area-list");
		add(al);
		
		

		try {
				ListModel<Area> lm = new ListModel<Area>(new Model<Panel>(this), "areas");
				
				org.apache.wicket.markup.html.list.ListView<Area> lp = new ListView<Area>("area", lm) {
					private static final long serialVersionUID = 1L;
					@Override
					protected void populateItem(ListItem<Area> item) {
						try {
							Area area=item.getModelObject();
							Map<String, String> map = getParameters();
							Panel panel = area.getService(PortalObjectViewerRenderService.class).build("area-panel", -1, getViewMode(), map);
							item.add(panel);
							item.setOutputMarkupId(true);
						} 
						catch (Exception e) {
							item.addOrReplace(new PortalErrorPanel<Area>("area-panel", item.getModel(), e));
							logger.error(e);
						}	
					}
					
				};
				
				al.add(lp);
		} catch (Exception e) {
			logger.error(e);
			al.addOrReplace(new PortalErrorPanel<PageSection>("areas", getModel(), e));
		}
	}

	
	List<Area> _list;
	
	/**
	 * @return
	 */
	public List<Area> getAreas() {

		if (_list!=null)
			return _list;
		
		_list = new ArrayList<Area>();
		
		if (!this.show_archived && !this.show_deleted) {
		for (Area a: getModel().getObject().getAreas(ObjectState.ENABLED))
			_list.add(a);
			return _list;
		}
			
			for (Area a: getModel().getObject().getAreas()) {
				if (a.getState()==ObjectState.ENABLED)
					_list.add(a);
				else if (a.getState()==ObjectState.ARCHIVED && this.show_archived)
					_list.add(a);
				else if (a.getState()==ObjectState.DELETED && this.show_deleted)
					_list.add(a);
		}
		return _list;
			

		//if (tab_index==-1) {
		//}
		//else {
		//	for (Area a: getModel().getObject().getAreas(ObjectState.ENABLED)) {
		//		if (a.getBlocks(ObjectState.ENABLED).size()>0)
		//				_list.add(a);
		//	}
		//}
		
		
	}
	
	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

}
