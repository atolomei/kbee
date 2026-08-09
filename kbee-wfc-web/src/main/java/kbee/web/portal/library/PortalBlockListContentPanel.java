package kbee.web.portal.library;


import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.service.ContentService;
import com.novamens.datetime.DateTimeService;
import com.novamens.dom.Json;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.kbee.template.KbeeContentTemplateModel;
import com.novamens.kbee.text.KbeeTextTemplate;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.PortalViewRender;
import com.novamens.portal6.model.Site;
import com.novamens.portal6.model.block.ListBlock;
import com.novamens.service.ServiceLocator;
import com.novamens.text.TextTemplate;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.logging.Logger;
import kbee.web.console.CursorNavigator;
import kbee.web.cursor.CursorListModel;
import kbee.web.dashboard.DashboardListWidgetPanel;
import kbee.web.dashboard.LabelPanel;
import kbee.web.error.ApplicationErrorPage;
import kbee.web.error.ErrorPanel;
import kbee.web.portal6.factory.PanelPortalModel;
import kbee.web.searcher.page.SearcherDetailDocumentPage;

@SuppressWarnings("serial")
public class PortalBlockListContentPanel extends DashboardListWidgetPanel<Content> implements PanelPortalModel<Block>, PortalViewRender {
	private static final long serialVersionUID = 1L;
			
	private static Logger logger = Logger.getLogger(PortalBlockListContentPanel.class.getName());

	private  IModel<Block> model;
	private  String zid;
	private  Locale locale;
	private  IModel<Site> sitemodel;
	
	private boolean isExpand = false;
	private IModel<String> freemakerAbstract = null;
	
	private DateTimeService service = ServiceLocator.getService(DateTimeService.class);
	
	private class PortalContentModel extends ObjectModel<Content> {
		private  IModel<Site> sitemodel;
		public PortalContentModel(IModel<Site> sitemodel, Content content) {
			super(content);
			this.sitemodel = sitemodel;
		}
		public Content getObject() {
			Content content = super.getObject();
			if (sitemodel.getObject().isDisplayValidVersion()) {
				Content version = content.getService(ContentService.class).getValidVersion();
				if (version!=null && !content.equals(version)) {
					content = version;
				}
			}
			return content;
		}	
	}
	
	public PortalBlockListContentPanel(String id) {
		this(id, null);
	}
	
	public PortalBlockListContentPanel(String id, IModel<Block> model) {
		super(id);
		this.model=model;
		setViewModeCriteria("comfortable");
		KbeeUser us = (KbeeUser) getSessionUser();
		locale=us.getLocale();
		zid = ServiceLocator.getService(DateTimeService.class).getMapZoneIds().get(us.getTimeZone());
	}
	
	@Override
	public void onInitialize() {
		
		setTitle( new Model<String>(getPortalModel().getObject().getTitle()));
		
		setHelp(true);
		
		Json json = getPortalModel().getObject().getCustomValuesJson();
		
		if (json!=null && json.getString("abstract")!=null) {			
			freemakerAbstract = new Model<String>(json.getString("abstract"));
		}
		
		
		if (json!=null && json.getString("expander")!=null) {
			isExpand = json.getString("expander").equals("true");
		}
		
		List<IModel<Content>> li = new ArrayList<IModel<Content>>();
		try {
			if (getPortalModel().getObject() instanceof ListBlock) { 
				@SuppressWarnings("unchecked")
				List<Content> c_list = ((ListBlock<Content>) getPortalModel().getObject()).getItems();
				for ( Content c: c_list) {
					li.add(new PortalContentModel(getSiteModel(), c));
				}
			}
			else
				logger.error("Block must be of class -> " + ListBlock.class.getName());
		
		} 
		catch (Exception e) {
			logger.error(e);
		}
		
		setItems(li);
		
		super.onInitialize();
	}
	
	@Override
	protected String getBodyStyle() {
		String css = model.getObject().getCss();
		return css;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		
		this.service=null;
		
		if (this.model!=null)
			this.model.detach();
		
		if (this.sitemodel!=null)
			this.sitemodel.detach();
	}
	
	
	
	@Override
	protected boolean isExpandVisible() {
		return isExpand;
	}
	
	protected IModel<String> getListTitle() {
		return new Model<String>(getPortalModel().getObject().getTitle());
	}
	

	
	@Override
	protected WebMarkupContainer getMoreInfoPanel(IModel<Content> modelObject) {
		try {
			
			if( freemakerAbstract ==null )
				return new InvisiblePanel("more-info-container");
			
			IModel<String> s=getItemAbstract(modelObject);
			return new LabelPanel("more-info-container",s);
			
		} catch (Exception e) {
			logger.error(e);
			return new ErrorPanel("more-info-container", e);
		}
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
	protected IModel<String> getItemLabelMeta(IModel<Content> modelObject) {
		
		StringBuilder str = new StringBuilder();
		
		try {
			
			String ty = modelObject.getObject().getService(ContentService.class).getConsoleSubtitle();
			
			if (ty != null &&  ty.length()>0) {
				str.append(ty);
			}
			else {
				String ta=modelObject.getObject().getContentTypeClassificationAsString();
				
				if (ta!=null &&  ta.length()>0) {
					str.append(ta);
				}
				
				String st=modelObject.getObject().getWorkflowStatusClassificationAsString();
				
				if (st!=null &&  st.length()>0) {
					if (ta!=null && ta.length()>0)
						str.append(", ");
					str.append(st);
				}
				
				
			}
			
			OffsetDateTime date=modelObject.getObject().getLastModifiedOffsetDateTime();
			
			if (date!=null) {
				ZonedDateTime zd = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of(getZid()));
				String tst = getDateTimeService().timeElapsed(zd, ZoneId.of(getZid()), getSessionUserLocale(), DateTimeService.DATE_COLlOQUIAL_AGO, "ago");
				str.append(" - "+ tst);
			}
			

		} catch (Exception e) {
			logger.error(e);
			str.append(e.getClass().getName());
		}
		
		
		
		
		return new Model<String>(str.toString());
		
	}

	
	
	@Override
	protected IModel<String> getItemAbstract(IModel<Content> modelObject) {
		try {
			KbeeContentTemplateModel model = new KbeeContentTemplateModel(modelObject.getObject());
			TextTemplate template = new KbeeTextTemplate(freemakerAbstract.getObject());
			String text = template.process(model);
			return new Model<String>(text);
		} catch (Exception e) {
			logger.error(e);
			return new Model<String>( e.getClass().getSimpleName() + " | " +  e.getMessage());
		}
	}
	
	
	protected DateTimeService getDateTimeService() {
		if (service==null)
			service = ServiceLocator.getService(DateTimeService.class);			
		return service;
	}
	
	@Override
	protected void onClick(IModel<Content> model, int index) {
		try {
				CursorListModel<Content> cursor = new CursorListModel<Content>(getItems(), index);
				SearcherDetailDocumentPage<Content> page = new SearcherDetailDocumentPage<Content>(new ObjectModel<Content>((Content) model.getObject()),getSiteModel());
				CursorNavigator<Content> nav =  new CursorNavigator<Content>(cursor, index);
				page.setNavigator(nav);
				setResponsePage(page);
			
		} catch (Exception e) {
			logger.error(e);
			setResponsePage( new ApplicationErrorPage<>(e));
			
		}
	}
	
	@Override
	protected boolean isMenuVisible() {
		return false;
	}
	
	protected String getName() {
		return getSiteModel().getObject().getKey();
	}

	protected boolean isIconVisible() {
		return false;
	}

	protected String getListContainerCss() {
		return (getViewModeCriteria().equals("comfortable") ?"cozy" : "standard");
	}
	protected IModel<String> getLabelContainerCss() {
		return new Model<String>(getViewModeCriteria().equals("comfortable") ? "label-container c100" :  "label-container c40");
	}

	protected String getZid() {
		return zid;
	}

	protected Locale getSessionUserLocale() {
		return locale;
	}

}
