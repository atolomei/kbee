package kbee.web.datamanagement;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.ITab;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.hibernate.SessionFactory;

import com.novamens.content.web.admin.markup.ActionsPanel;
import com.novamens.content.web.admin.markup.XStdLink;
import com.novamens.dom.Domain;
import com.novamens.kbee.wicket.markup.html.areainfo.AreaInfoPanel;
import com.novamens.kbee.wicket.markup.html.areainfo.GridInfoPanel;
import com.novamens.kbfs.FileServerS3;
import com.novamens.service.ServiceLocator;
import com.novamens.thumbnail.ThumbnailService;
import com.novamens.wicket.util.BCElement;
import com.novamens.wicket.util.MenuBreadCrumbPanel;

import kbee.util.Tuple;
import kbee.web.editor.DomainObjectMainPanel;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.nav.DataManagementBC;
import kbee.web.objectstorage.ObjectStorageMainPanel;


public class ThumbnailServicePanel extends DomainObjectMainPanel<Domain>  {
			
	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ObjectStorageMainPanel.class.getName());


	
    public ThumbnailServicePanel(String id, IModel<Domain> model) {
        super(id, model);
        setOutputMarkupId(true);
    }
    
    @Override
    protected void onInitialize() {
        super.onInitialize();

    	setOutputMarkupId(true); 
    	
    	AreaInfoPanel area = new AreaInfoPanel("panel");

    	area.addPanel(new GridInfoPanel("element", serviceInfo(), new Model<String>("Thumbnail"), true));
		area.setSections(AreaInfoPanel.ONE_SECTION);
		area.setCss("col-lg-12");
		
		ActionsPanel actions = new ActionsPanel("actions", new Model<String>("Actions"));
		
		XStdLink x0 = new XStdLink( new Model<String>("Reset Thumbnail Service")) {
			private static final long serialVersionUID = 1L;


			@Override
			public boolean isEnabled() {
				return true;
			}
			
			@Override
			public void onClick() {
				
				try {
					ServiceLocator.getService(ThumbnailService.class).removeAll();
					setResponsePage(new ThumbnailServicePage());
					
				} 
				catch (Exception e) {
					logger.error(e);
					setResponsePage(new ApplicationErrorPage<Void>(e));				
				}
				
			}
		};


		
		XStdLink x1 = new XStdLink( new Model<String>("Clean Hibernate cache")) {
			private static final long serialVersionUID = 1L;


			@Override
			public boolean isEnabled() {
				return true;
			}
			
			@Override
			public void onClick() {
					
				try {
						ServiceLocator.getService(com.novamens.event.EventService.class).fire(new com.novamens.kbee.event.EvictCacheServiceEvent());
						getContentDao().cleanHibernateCache();
						setResponsePage(new ThumbnailServicePage());
				}
				catch (Exception e) {
					logger.error(e);
					setResponsePage(new ApplicationErrorPage<Void>(e));				
				}
				
			}
		};


		XStdLink x3 = new XStdLink( new Model<String>("Reconnect Amazon S3")) {
			private static final long serialVersionUID = 1L;


			@Override
			public boolean isEnabled() {
				return true;
			}
			
			@Override
			public void onClick() {
				
				try {
					
					
					FileServerS3 s3=ServiceLocator.getService(FileServerS3.class);
					s3.reconnect();
					setResponsePage(new ThumbnailServicePage());
					
				} 
				catch (Exception e) {
					logger.error(e);
					setResponsePage(new ApplicationErrorPage<Void>(e));				
				}
				
			}
		};

		
		
		actions.add(x1);
		actions.add(x0);
		
		FileServerS3 s3=ServiceLocator.getService(FileServerS3.class);

		if (s3!=null && s3.isEnabled())
			actions.add(x3);
		
		
		area.setActionsPanel(actions);
		add(area);
    }
    
    

	/**
	 * @return
	 */
	private List<Tuple> serviceInfo() {
		List<Tuple> data = new ArrayList<Tuple>();
		try {
			ThumbnailService service=ServiceLocator.getService(ThumbnailService.class);
				Double per;
				Double total = Double.valueOf(service.getCacheHits()+service.getCacheMiss());
				if (total.doubleValue()>0.0) {
					per = Double.valueOf( Double.valueOf(service.getCacheHits()) /total.doubleValue());
				}
				else
					per = Double.valueOf(0);
					data.add(new Tuple ("Cache hits", String.valueOf(service.getCacheHits()) + " <span class=\"ago\"> ( " + String.format("%6.2f", per.doubleValue()*100.0) +" % ) </span>"));
					data.add(new Tuple ("Cache miss", String.valueOf(service.getCacheMiss())));
		}
		catch (Exception e) {
			logger.error(e);
			data.add(new Tuple( "Error",  e.getClass().getName()));
		}
		return data;
	}
	

	/**
	 * @return
	 */
	private List<Tuple> hibernateInfo() {
		List<Tuple> data = new ArrayList<Tuple>();
		try {
			
			
			
			
		}
		catch (Exception e) {
			logger.error(e);
			data.add(new Tuple( "Error",  e.getClass().getName()));
		}
		return data;
	}



}
