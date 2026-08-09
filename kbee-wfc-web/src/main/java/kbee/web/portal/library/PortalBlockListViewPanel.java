package kbee.web.portal.library;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.document.IDoc;
import com.novamens.content.service.ContentService;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.portal.service.ViewService;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.PortalViewRender;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.ViewBK;
import com.novamens.portal6.model.block.ListViewBlock;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.cursor.CursorListModel;
import kbee.web.cursor.ModelListCursor;
import kbee.web.dashboard.DashboardListWidgetPanel;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.portal6.factory.PanelPortalModel;
import kbee.web.searcher.page.SearcherDetailDocumentPage;


/**
 * List of Views
 * 
 */
public class PortalBlockListViewPanel extends DashboardListWidgetPanel<ViewBK>  implements PanelPortalModel<Block>, PortalViewRender  {
			
	IModel<Block> model;
	private String zid;
	private Locale locale;
	private  IModel<Site> sitemodel;

	private boolean show_meta = true;
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(PortalBlockListViewPanel.class.getName());

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public PortalBlockListViewPanel(String id) {
		this(id, null);
	}
	
	public PortalBlockListViewPanel(String id, IModel<Block> model) {
		super(id);
		this.model=model;
		KbeeUser us = (KbeeUser) getSessionUser();
		locale=us.getLocale();
		zid = ServiceLocator.getService(DateTimeService.class).getMapZoneIds().get(us.getTimeZone());
	}
	

	@Override
	public void onInitialize() {
		
		//if (getSiteModel()!=null) {
		//	setPreferencesKey( getSiteModel().getObject().getKey());
		//	setTitle( new Model<String>(getSiteModel().getObject().getTitle()));
		//}
		
		setHelp(true);
		
		if (getPortalModel().getObject().getCustomValuesJson()!=null) {
			show_meta = (getPortalModel().getObject().getCustomValuesJson().get("show_meta")==null || getPortalModel().getObject().getCustomValuesJson().get("show_meta").equals("yes"));
		}
			
		
		setTitle( new Model<String>(getPortalModel().getObject().getTitle()));
		
		List<IModel<ViewBK>> li = new ArrayList<IModel<ViewBK>>();
		
		if (getPortalModel().getObject() instanceof ListViewBlock) { 
			
			try {
			for ( ViewBK v: ((ListViewBlock) getPortalModel().getObject()).getItems()) {
				li.add( new ObjectModel<ViewBK>(v));
			}
			} catch (Exception e) {
				logger.error(e);
				
			}
		}
		else {
			logger.error("Block must be of class " +  ListViewBlock.class.getName());
		}
		
		setItems(li);
		
		
		
		
		super.onInitialize();
		
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		if (this.model!=null)
			this.model.detach();
		
		if (sitemodel!=null)
			sitemodel.detach();
	}
	
	protected IModel<String> getLabelContainerCss() {
		return new Model<String>("label-container c40");
	}
	
	@Override
	protected boolean isMenuVisible() {
		return false;
	}
	
	
	protected IModel<String> getListTitle() {
		return new Model<String>(getPortalModel().getObject().getTitle());
	}
	

	@Override
	public void setPortalModel(IModel<Block> model) {
		this.model=model;	
	}
	
	
	public IModel<Site> getSiteModel() {
		if (this.sitemodel==null) {
			if (getPortalModel()!=null) {
				this.sitemodel=new ObjectModel<Site>( model.getObject().getSite());
			}
		}
		return this.sitemodel;
}

	
	
	public void setSiteModel(ObjectModel<Site> objectModel) {
			this.sitemodel=objectModel;
	}

	

	@Override
	public IModel<Block> getPortalModel() {
		return this.model;
	}
	
	/**
	 * 
	 */
	
	@Override
	protected IModel<String> getItemLabelMeta(IModel<ViewBK> modelObject) {
		
		
		if (!show_meta)
			return null;
			
		
		StringBuilder str = new StringBuilder();
		
		try {
			
			 // String ty=modelObject.getObject().getService(ViewService.class).getSubtitle();
			
			String ty=modelObject.getObject().getSubtitle();
			
			if (ty!=null &&  ty.length()>0) {
				str.append(ty);
			}
			else {
				String ta=modelObject.getObject().getDescription();
				if (ta!=null &&  ta.length()>0) {
					str.append(ta);
				}
			}
			
			OffsetDateTime date=modelObject.getObject().getLastModifiedOffsetDateTime();
			
			if (date!=null) {
				ZonedDateTime zd = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of(zid));
				String tst = ServiceLocator.getService(DateTimeService.class).timeElapsed(zd, ZoneId.of(zid), locale, DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
				str.append(" - "+ tst);
			}

		} catch (Exception e) {
			
			logger.error(e);
			str.append(e.getClass().getName());
			
		}
		return new Model<String>(str.toString());
		
		
	}

	
	
	@Override
	protected void onClick(IModel<ViewBK> model, int index) {
		try {
			
			List<IModel<ViewBK>> mi= new ArrayList<IModel<ViewBK>>();
			getItems().forEach(item -> 
			{	
					 mi.add(new ObjectModel<ViewBK>((ViewBK) item.getObject()));
			});
			CursorListModel<ViewBK> cursor = new CursorListModel<ViewBK> (mi, index);

			// TODO VER AT
			//
			//new  ModelListCursor<ViewBK>(cursor)
			
			SearcherDetailDocumentPage<IDoc> pa=new SearcherDetailDocumentPage<IDoc>(new ObjectModel<IDoc>((IDoc) model.getObject()),getSiteModel());
			setResponsePage(pa);
			
		} catch (Exception e) {
			logger.error(e);
			setResponsePage( new ApplicationErrorPage<>(e));
			
		}
	}
	
	
	protected boolean isExpand() {
		return false;
	}
	
	
	
	protected String getName() {
		return getSiteModel().getObject().getKey();
	}
	
	

	protected boolean isIconVisible() {
		return false;
	}

	@Override
	protected String getListContainerCss() {
		if (!show_meta)
			return "onecol";
		else
			return "twocol";
	}


	

}
