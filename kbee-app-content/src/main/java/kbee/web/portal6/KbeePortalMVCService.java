package kbee.web.portal6;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import com.novamens.beans.BeansService;
import com.novamens.portal6.model.Area;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.PageSection;

import com.novamens.portal6.model.PortalObject;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;
import com.novamens.wicket.util.DummyBlockPanel;

import kbee.web.portal6.factory.PanelPortalModel;
import kbee.web.portal6.factory.PortalObjectInternalPanelFactory;
import kbee.web.portal6.library.PortalSimpleTextPanel;
import kbee.web.portal6.panel.PortalErrorPanel;

public class KbeePortalMVCService implements PortalMVCService {
				
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePortalMVCService.class.getName());

	
	private Map<String, String> block_viewer = new ConcurrentHashMap<String, String>(16, 0.9f, 1); 
	private Map<String, String> block_dataprovider = new ConcurrentHashMap<String, String>(16, 0.9f, 1);

	
	
	KbeePortalMVCService() {
 	}

	@Override
	public void register(String key, String viewer_class_name, String dataprovider_class_name) {
		
		if (viewer_class_name!=null)
			block_viewer.put(key, viewer_class_name);
		
		if (dataprovider_class_name!=null)
			block_dataprovider.put(key, dataprovider_class_name);
	}
	
	
	@Override
	public void registerDataProvider(String key, String class_name) {
		block_dataprovider.put(key, class_name);
	}

	
	@Override
	public void registerViewer(String key, String class_name) {
		block_viewer.put(key, class_name);
	}
	
	
	@SuppressWarnings("unchecked")
	public Panel getEditor(String key, String id, PortalObject p_obj) {

		
		// Map -----------------------------------------
		//
		// 
		
		if (this.block_dataprovider.containsKey(key)) {
			
			String cname=this.block_dataprovider.get(key);
		
			logger.debug(key+" | Name ->  "+ cname);
			
			try {
				
				Constructor<?> co = Class.forName(cname).getDeclaredConstructor(new Class[] {String.class});
				Panel panel = (Panel) co.newInstance(id);
				
				if (panel instanceof PanelPortalModel) {
					if 		(p_obj instanceof Block)					((PanelPortalModel<Block>) panel).setPortalModel(new ObjectModel<Block>( (Block) p_obj));
					else if (p_obj instanceof Area)						((PanelPortalModel<Area>) panel).setPortalModel(new ObjectModel<Area>( (Area) p_obj));
					else if (p_obj instanceof PageSection)				((PanelPortalModel<PageSection>) panel).setPortalModel(new ObjectModel<PageSection>( (PageSection) p_obj));
					else 												((PanelPortalModel<PortalObject>) panel).setPortalModel(new ObjectModel<PortalObject>(p_obj));
				}
				
				
				
				
				return panel;
			}
			catch (Exception e) {
				logger.error(e);
				return new PortalErrorPanel<Block>(id, e);
			}
		}
		
		// beans -----------------------------------------
		//
		
		try {
			java.util.Map<String, PortalObjectInternalPanelFactory> beans = ServiceLocator.getService(BeansService.class).getBeansOfType(PortalObjectInternalPanelFactory.class);
			
			 for (Entry<String, PortalObjectInternalPanelFactory> entry: beans.entrySet()) {
			
				 if (key.equals(entry.getValue().getKey())) {
					 
					 logger.debug(entry.getKey()+" | Name ->  "+entry.getValue().getDisplayName()+" | Panel -> "+entry.getValue().getClassName());
					 
					 Panel panel = entry.getValue().create(id);
					  
					  if (panel==null) {
								if      (p_obj instanceof Block)							return new PortalErrorPanel<Block>(id, new ObjectModel<Block>( (Block) p_obj), new Model<String>(this.getClass().getName() + " | can not create DataProvider panel -> " + entry.getValue()));
								else if (p_obj instanceof Area)								return new PortalErrorPanel<Area>(id, new ObjectModel<Area>((Area) p_obj), new Model<String>(this.getClass().getName() + " | can not create DataProvider panel -> " + entry.getValue()));
								else if (p_obj instanceof PageSection)						return new PortalErrorPanel<PageSection>(id, new ObjectModel<PageSection>(  (PageSection) p_obj), new Model<String>(this.getClass().getName() + " | can not create DataProvider panel -> " + entry.getValue()));
								else														return new PortalErrorPanel<PortalObject>(id, new ObjectModel<PortalObject>(p_obj), new Model<String>("| can not create DataProvider panel for key -> " + key));
									
					  }
							
					  if (panel instanceof PanelPortalModel) {						
						  	if      (p_obj instanceof Block)				((PanelPortalModel<Block>) panel).setPortalModel(new ObjectModel<Block>( (Block) p_obj));
							else if (p_obj instanceof Area)					((PanelPortalModel<Area>) panel).setPortalModel(new ObjectModel<Area>( (Area) p_obj));
							else if (p_obj instanceof PageSection)			((PanelPortalModel<PageSection>) panel).setPortalModel(new ObjectModel<PageSection>( (PageSection) p_obj));
							else 											((PanelPortalModel<PortalObject>) panel).setPortalModel(new ObjectModel<PortalObject>(p_obj));
					  }
						return panel;  	
					}
			 }
				 
	
		} catch (Exception e) {
			logger.error(e);
			if (p_obj instanceof Block)									return new PortalErrorPanel<Block>(id, new ObjectModel<Block>( (Block) p_obj), e);
			else if (p_obj instanceof Area)								return new PortalErrorPanel<Area>(id, new ObjectModel<Area>((Area) p_obj), e);
			else if (p_obj instanceof PageSection)						return new PortalErrorPanel<PageSection>(id, new ObjectModel<PageSection>((PageSection) p_obj), e);
			return new PortalErrorPanel<PortalObject>(id, new ObjectModel<PortalObject>(p_obj), new Model<String>( this.getClass().getName() + " can not create DataProvider panel for key -> " + key));
		}

		
		
		// error  -----------------------------------------
		//
		if (p_obj instanceof Block)									return new PortalErrorPanel<Block>(id, new ObjectModel<Block>( (Block) p_obj), new Model<String>(this.getClass().getName() + " | can not create  DataProvider panel for key -> " + key));
		else if (p_obj instanceof Area)								return new PortalErrorPanel<Area>(id, new ObjectModel<Area>((Area) p_obj), new Model<String>(this.getClass().getName() +" | can not create  DataProvider panel for key -> " + key));
		else if (p_obj instanceof PageSection)						return new PortalErrorPanel<PageSection>(id, new ObjectModel<PageSection>(  (PageSection) p_obj), new Model<String>(this.getClass().getName() + " | can not create  DataProvider panel for key -> " + key));
		
		return new PortalErrorPanel<PortalObject>(id, new ObjectModel<PortalObject>(p_obj), new Model<String>( this.getClass().getName() +  " | can not create DataProvider panel for key -> " + key));

	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public Panel getViewer(String key, String id, PortalObject p_obj) {
		
		// Map -----------------------------------------
		//
		if (block_viewer.containsKey(key)) {
			
			String cname=block_viewer.get(key);
		
			
			if (key.equals("block-portal-text")) {
				cname=PortalSimpleTextPanel.class.getName();
			}
			
				
			logger.debug(key+" | Name ->  "+ cname);
			
			try {
				
				Constructor<?> co = Class.forName(cname).getDeclaredConstructor(new Class[] {String.class});
				Panel panel = (Panel) co.newInstance(id);
				
				if (panel instanceof PanelPortalModel) {
					if 		(p_obj instanceof Block)					((PanelPortalModel<Block>) panel).setPortalModel(new ObjectModel<Block>( (Block) p_obj));
					else if (p_obj instanceof Area)						((PanelPortalModel<Area>) panel).setPortalModel(new ObjectModel<Area>( (Area) p_obj));
					else if (p_obj instanceof PageSection)				((PanelPortalModel<PageSection>) panel).setPortalModel(new ObjectModel<PageSection>( (PageSection) p_obj));
					else 												((PanelPortalModel<PortalObject>) panel).setPortalModel(new ObjectModel<PortalObject>(p_obj));
				}
				
				
				
				
				return panel;
			}
			catch (Exception e) {
				logger.error(e);
				return new PortalErrorPanel<Block>(id, e);
			}
		}
		
		// beans -----------------------------------------
		//
		
		try {
			java.util.Map<String, PortalObjectInternalPanelFactory> beans = ServiceLocator.getService(BeansService.class).getBeansOfType(PortalObjectInternalPanelFactory.class);
			
			 for (Entry<String, PortalObjectInternalPanelFactory> entry: beans.entrySet()) {
			
				 if (key.equals(entry.getValue().getKey())) {
					 
					 logger.debug(entry.getKey()+" | Name ->  "+entry.getValue().getDisplayName()+" | Panel -> "+entry.getValue().getClassName());
					 
					 Panel panel = entry.getValue().create(id);
					  
					  if (panel==null) {
								if      (p_obj instanceof Block)							return new PortalErrorPanel<Block>(id, new ObjectModel<Block>( (Block) p_obj), new Model<String>("can not create panel -> " + entry.getValue()));
								else if (p_obj instanceof Area)								return new PortalErrorPanel<Area>(id, new ObjectModel<Area>((Area) p_obj), new Model<String>("can not create panel -> " + entry.getValue()));
								else if (p_obj instanceof PageSection)						return new PortalErrorPanel<PageSection>(id, new ObjectModel<PageSection>(  (PageSection) p_obj), new Model<String>("can not create panel -> " + entry.getValue()));
								else														return new PortalErrorPanel<PortalObject>(id, new ObjectModel<PortalObject>(p_obj), new Model<String>("can not create panel for key -> " + key));
									
					  }
							
					  if (panel instanceof PanelPortalModel) {						
						  	if      (p_obj instanceof Block)				((PanelPortalModel<Block>) panel).setPortalModel(new ObjectModel<Block>( (Block) p_obj));
							else if (p_obj instanceof Area)					((PanelPortalModel<Area>) panel).setPortalModel(new ObjectModel<Area>( (Area) p_obj));
							else if (p_obj instanceof PageSection)			((PanelPortalModel<PageSection>) panel).setPortalModel(new ObjectModel<PageSection>( (PageSection) p_obj));
							else 											((PanelPortalModel<PortalObject>) panel).setPortalModel(new ObjectModel<PortalObject>(p_obj));
					  }
						return panel;  	
					}
			 }
				 
	
		} catch (Exception e) {
			logger.error(e);
			if (p_obj instanceof Block)									return new PortalErrorPanel<Block>(id, new ObjectModel<Block>( (Block) p_obj), e);
			else if (p_obj instanceof Area)								return new PortalErrorPanel<Area>(id, new ObjectModel<Area>((Area) p_obj), e);
			else if (p_obj instanceof PageSection)						return new PortalErrorPanel<PageSection>(id, new ObjectModel<PageSection>((PageSection) p_obj), e);
			return new PortalErrorPanel<PortalObject>(id, new ObjectModel<PortalObject>(p_obj), new Model<String>(this.getClass().getName() +  " can not create panel for key -> " + key));
		}

		
		
		// error  -----------------------------------------
		//
		if (p_obj instanceof Block)									return new PortalErrorPanel<Block>(id, new ObjectModel<Block>( (Block) p_obj), new Model<String>("can not create panel for key -> " + key));
		else if (p_obj instanceof Area)								return new PortalErrorPanel<Area>(id, new ObjectModel<Area>((Area) p_obj), new Model<String>("can not create panel for key -> " + key));
		else if (p_obj instanceof PageSection)						return new PortalErrorPanel<PageSection>(id, new ObjectModel<PageSection>(  (PageSection) p_obj), new Model<String>("can not create panel for key -> " + key));
		
		return new PortalErrorPanel<PortalObject>(id, new ObjectModel<PortalObject>(p_obj), new Model<String>(this.getClass().getName() +  " can not create panel for key -> " + key));

	}
	
	
}
