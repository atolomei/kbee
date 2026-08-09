package kbee.web.console;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.model.DataSetMember;
import com.novamens.indexer.query.Query;
import com.novamens.indexer.query.Suggestion;
import com.novamens.kbee.wicket.markup.html.console.event.ClickEvent;
import com.novamens.kbee.wicket.markup.html.event.OnSearchSuggestionEvent;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;

import kbee.util.logging.Logger;
import kbee.web.query.ConsoleQuery;

@SuppressWarnings("serial")
public abstract class ContentConsolePage<T extends Content> extends ConsolePage<T>  {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(ContentConsolePage.class.getName());
	
	final protected boolean is_root = ServiceLocator.getService(SecurityService.class).isRoot();
	final protected boolean is_domain_admin = ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final protected boolean is_archive	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.ARCHIVE.getId());
	final protected boolean is_support 	= ServiceLocator.getService(SecurityService.class).isMember(KbeeGlobalRole.SUPPORT.getId());

	private  long t1, t2;
	
	public ContentConsolePage() {
		this(null);
	}
	
	public ContentConsolePage(Query query) {
		super(query);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void addListeners() {
		super.addListeners();
		
		add(new WicketEventListener<OnSearchSuggestionEvent>() {
			@Override
			public void onEvent(OnSearchSuggestionEvent event) {
				if (event.getSuggestion()!=null) {
					Object object = event.getSuggestion().getObject();
					if (object instanceof IModel && ((IModel<?>)object).getObject() instanceof DataSetMember) {
						String facet = event.getSuggestion().getFacet();
						getQuery().getParameters().remove("iql");
						((ConsoleQuery)getQuery()).setAsParameter(((IModel<DataSetMember>)object).getObject(), facet, true);
						((ConsoleQuery)getQuery()).getParameters().put("sort", "relevance");
						setResponsePage(getConsolePage(getQuery(), 0));
					}
					else
						if (object instanceof IModel && ((IModel<?>)object).getObject() instanceof Content) {
							//String url = getUrl((IModel<T>)object);
							fireScanAll(new ClickEvent<T>(null, (IModel<T>)object, 0));
							//event.getRequestTarget().appendJavaScript("var win = window.open('"+url+"', '_blank');");
						}
				}
			}
			@Override
			public boolean handle(com.novamens.event.Event event) {
				return event instanceof OnSearchSuggestionEvent;
			}
		});
	}
	
	@Override
	public void onAfterRender() {
		super.onAfterRender();
		t2 = System.currentTimeMillis();
		logger.debug("Render:" +String.valueOf(t2-t1));
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		t1 = System.currentTimeMillis();
	}
	
	protected Component newNavigationPanel() {
		return getMainTopbar();
	}
	
	@Override
	protected List<Suggestion> getSuggestions(String pattern) {
		return new ArrayList<Suggestion>();
	}
	
	@Override
	public boolean isClearAllSearch() {
		return true;
	}
	
	protected boolean isSuggester() {
		return false;
	}
	
	protected String getUrl(IModel<T> model) {
		StringBuilder str = new StringBuilder(); 
		str.append(model.getObject().getClassCode()+"/");
		Content content = model.getObject();
		str.append(String.valueOf(content.getOId()));
		return str.toString();
	}
}
