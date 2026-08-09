package kbee.web.searcher.page;

import java.io.Serializable;
import java.util.Map;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.content.base.Content;
import com.novamens.content.model.ContentId;
import com.novamens.content.service.ContentService;
import com.novamens.content.userlist.UserList;
import com.novamens.indexer.query.ValueFilter;
import com.novamens.kbee.portal.model.SearcherSiteQuery;
import com.novamens.kbee.wicket.markup.html.console.panel.MyListsApplyUserListEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.portal6.model.Site;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.modal.Modal;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.content.panel.ShareModal;
import kbee.web.event.wicket.ClickSendByEmailEvent;
import kbee.web.model.service.ObjectModelService;
import kbee.web.searcher.searchform.SearcherOnChangeEvent;

/**
 * @param <T>
 */
@SuppressWarnings("serial")
public class SearcherDetailPage<T extends Content> extends AbstractSearcherPage<T> {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SearcherDetailPage.class.getName());
	
	@SuppressWarnings("unchecked")
	public SearcherDetailPage(PageParameters parameters) {
		setOutputMarkupId(true);
		try {
			Site site =  getSite(parameters);
			if (site!=null) 
				setSiteModel(new ObjectModel<Site>(site));
			T content = getContent(parameters);		
			if (content!=null) {
				setModel((IModel<T>) ServiceLocator.getService(ObjectModelService.class).getObjectModel(content));
				setPageTitle(new Model<String>(content.getTitle()));
				addModals();
			}	
			addModals();
		} 
		catch (Exception e) {
			logger.error(e);
		}
	}

	/**
	 * 
	 */
	public SearcherDetailPage(IModel<T> model, IModel<Site> model_site) {
		setOutputMarkupId(true);
		setModel(model);
		setSiteModel(model_site);
		getPageParameters().set("oid", getModel().getObject().getOId().toString());
		if (getSiteModel()!=null)
			getPageParameters().set("siteurl", getSiteModel().getObject().getUrl().toString());
		setPageTitle(new Model<String>(model.getObject().getTitle()));
		addModals();
	}

	@Override
	protected void addModals() 	 {
		super.addModals();
	}
	
	/**
	 * 
	 */
	@Override
	protected void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<SearcherOnChangeEvent>() {
			public void onEvent(SearcherOnChangeEvent event) {
				try {
					Map<String, Object> parameters = event.getParameters();
					parameters.put("writeables", isEditableOn() ? "true" : "false");
					SearcherSiteQuery sq = new SearcherSiteQuery(getSiteModel().getObject(), getIndex(), parameters);
					SearcherResultsPage s=new SearcherResultsPage(getSiteModel(), sq);
					setResponsePage(s);
				
				} 
				catch (Exception e) {
					logger.error(e);
				}		
			}
		});
	
		add(new WicketEventListener<ClickSendByEmailEvent<T>>() {
			@SuppressWarnings("unchecked")
			@Override
			public void onEvent(ClickSendByEmailEvent<T> event) {
				Modal modal = SearcherDetailPage.this.getSendByEmailModal();
				((ShareModal<T>)modal).open(event.getRequestTarget(), getModel());
			}
		});
		
		add(new WicketEventListener<MyListsApplyUserListEvent>() {
			@Override
			public void onEvent(MyListsApplyUserListEvent event) {
				IModel<UserList> list= event.getUserList();
				if (getSiteModel()!=null) {
					SearcherSiteQuery sq = new SearcherSiteQuery(getSiteModel().getObject(), getIndex());
					ValueFilter filter = new ValueFilter("userlist", String.valueOf(list.getObject().getId()), list.getObject().getDisplayName());
					sq.getParameters().put("userlist", filter);
					setResponsePage(new SearcherResultsPage(getSiteModel(),sq));
					list.detach();
				}
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof MyListsApplyUserListEvent;
			}
		});
	}
	
	/**
	 */
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	@SuppressWarnings("unchecked")
	protected T getContent(PageParameters parameters) {
		T content = null;		
		StringValue oid = parameters.get("oid");
		if (!oid.isNull() && !oid.isEmpty()) {
			content = (T)getContentDao().findContentByOId(Long.valueOf(oid.toString()));
			if (content!=null && getSiteModel()!=null && getSiteModel().getObject().isDisplayValidVersion()) {
				content = (T)content.getService(ContentService.class).getValidVersion();
			}
		}	
		return content;
	}
	
	protected Modal getSendByEmailModal() {
		return (Modal) get("send-email-modal");
	}
	
	@Override
	protected boolean isSearchForm() {
		return true;
	}

	@Override
	protected boolean hasLateralMenu() {
		return false;
	}
	
	/** 
	 * Reports 
	 **/
	


	protected String getPageType() {
		return "search-det";
	}
	
	protected String getContentTitle() {
		return getModel().getObject().getTitle();
	}
										
	protected String getStatsPageTitle() {
		return getModel().getObject().getTitle();
	}
	
	protected Long getStatsPageId() {
		return Long.valueOf(0);
	} 
									
	protected String getObjectId() 	{
		return null;
	}
	
	protected String getContentId() {
		return new ContentId(getModel().getObject()).toString();
	}	  			
	
	@Override
	protected Serializable getContentOId() {
		return getModel().getObject().getOId();
	}
	
	@Override				
	protected Serializable getCId() {
		return getModel().getObject().getId();
	}
	
	@Override
	protected Integer getContentVersion() {return Integer.valueOf(getModel().getObject().getVersion());}
	
	@Override
	protected boolean isExplorerOn() {
		return false;
	}
}
