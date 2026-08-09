package kbee.web.portal6.panel;

import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;

import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.AreaSection;

import kbee.web.event.wicket.ErrorEvent;

public class PortalAreaSectionBottomBar extends PortalPanel<Area> {
			
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalAreaSectionBottomBar.class.getName());
	
	
	private AreaSection area_section;
	
	public PortalAreaSectionBottomBar(String id, IModel<Area> model, AreaSection area_serction) {
		super(id, model);
		this.area_section=area_section;
		setOutputMarkupId(true);
	}
	
	public PortalAreaSectionBottomBar(String id, IModel<Area> model, AreaSection area_serction, Map<String, String> parameters) {
		super(id, model, parameters);
		this.area_section=area_section;
		setOutputMarkupId(true);
		 
	}
	
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		
		AjaxLink<Area> up=new AjaxLink<Area>("add", getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					//PortalIWebPanel.this.moveUp(target, getModel());
				} 
				catch (Exception e) {
					logger.error(e);
					fire(new ErrorEvent<Area>(target, getModel(), e));
				}
			}
		};
		addOrReplace(up);
		
		
	}

}
